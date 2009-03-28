#!/usr/bin/env python

import os, sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'lib'))
sys.path.insert(1, os.path.dirname(__file__))

import re, urllib

from webob import Request, Response
from webob import exc
from genshi.template import TemplateLoader, NewTextTemplate, TemplateNotFound
import lxml.etree
from lxml.builder import E

import rdfob
import representations

content_dir = '/home/dan/.www/miskinhill.com.au/content'

# for tests
def _rdf_imports():
    return [os.path.join(content_dir, 'meta.nt'), 
            os.path.join(content_dir, 'rdfschema', 'foaf.nt'), 
            os.path.join(content_dir, 'rdfschema', 'dcterms.nt')]

class MiskinHillApplication(object):

    def __init__(self, environ, start_response):
        self.environ = environ
        self.start = start_response

        self.graph = rdfob.Graph(*_rdf_imports())

        self.req = Request(environ)
        self.req.charset = 'utf8'
        self.req.content_dir = content_dir # XXX dodgy?

    METHODS = {
        '/': 'index', 
        '/about/': 'about', 
        '/contact/': 'contact', 
        '/journals/': 'journals_index', 
        '/unapi': 'unapi'
    }
    def __iter__(self):
        try:
            if self.req.path_info in self.METHODS:
                resp = getattr(self, self.METHODS[self.req.path_info])()
            else:
                resp = self.dispatch_rdf(self.req.path_info)
        except exc.HTTPException, e:
            resp = e
        return iter(resp(self.environ, self.start))

    def index(self):
        template = template_loader.load(os.path.join('html', 'index.xml'))
        body = template.generate(req=self.req).render('xhtml', doctype='xhtml')
        return Response(body, content_type='text/html')

    def about(self):
        template = template_loader.load(os.path.join('html', 'about.xml'))
        body = template.generate(req=self.req).render('xhtml', doctype='xhtml')
        return Response(body, content_type='text/html')

    def contact(self):
        template = template_loader.load(os.path.join('html', 'contact.xml'))
        body = template.generate(req=self.req).render('xhtml', doctype='xhtml')
        return Response(body, content_type='text/html')

    def journals_index(self):
        template = template_loader.load(os.path.join('html', 'journals_index.xml'))
        body = template.generate(req=self.req, 
                journals=[self.graph[rdfob.URIRef('http://miskinhill.com.au/journals/asees/')]] # XXX temp
                ).render('xhtml', doctype='xhtml')
        return Response(body, content_type='text/html')

    def unapi(self):
        if 'id' in self.req.GET:
            try:
                node = self.graph[rdfob.URIRef(self.req.GET['id'])]
            except KeyError:
                return exc.HTTPNotFound('URI not found in RDF graph')
            if 'format' in self.req.GET:
                try:
                    r = representations.BY_FORMAT[self.req.GET['format']]
                except KeyError:
                    return exc.HTTPNotAcceptable('Format %r not known' % self.req.GET['format'])
                if not node.is_any(r.rdf_types):
                    return exc.HTTPNotAcceptable('Format %r not acceptable for this URI' % self.req.GET['format'])
                return exc.HTTPFound(location=(node.uri + '.' + r.format).encode('utf8'))
            else:
                body = E.formats(id=node.uri, 
                        *(E.format(name=r.format, type=r.content_type, docs=r.docs) 
                          for r in representations.ALL if node.is_any(r.rdf_types)))
        else:
            body = E.formats(*(E.format(name=r.format, type=r.content_type, docs=r.docs) for r in representations.ALL))
        return Response(lxml.etree.tostring(body, encoding='utf8', xml_declaration=True),
                content_type='application/xml')

    def dispatch_rdf(self, path_info):
        decoded_uri = urllib.unquote('http://miskinhill.com.au' + 
                path_info).decode('utf8')
        format, decoded_uri = self.guess_format(decoded_uri)
        try:
            node = self.graph[rdfob.URIRef(decoded_uri)]
        except KeyError:
            return exc.HTTPNotFound('URI not found in RDF graph')
        try:
            r = representations.BY_FORMAT[format]
        except KeyError:
            return exc.HTTPNotFound('Format %r not known' % format)
        if not node.is_any(r.rdf_types):
            return exc.HTTPNotFound('Format %r not acceptable for this URI' % format)
        return r(self.req, node).response()

    def guess_format(self, decoded_uri):
        for r in representations.ALL:
            if decoded_uri.endswith('.' + r.format):
                return r.format, decoded_uri[:-(len(r.format) + 1)]
        # XXX should check Accept header too
        return 'html', decoded_uri

application = MiskinHillApplication

if __name__ == '__main__':
    import optparse
    parser = optparse.OptionParser(usage='%prog --port=PORT')
    parser.add_option('-p', '--port', type='int',
            help='Port to serve on (default: %default)')
    parser.add_option('-c', '--content-dir', type='string')
    parser.set_defaults(port=8082, content_dir=content_dir)
    options, args = parser.parse_args()
    content_dir = options.content_dir
    from wsgiref.simple_server import make_server
    server = make_server('', options.port, application)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print '^C'
