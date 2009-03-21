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

template_loader = TemplateLoader(
        os.path.join(os.path.dirname(__file__), 'templates'), 
        variable_lookup='strict', 
        auto_reload=True)

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
        body = template.generate(req=self.req).render('xhtml')
        return Response(body, content_type='text/html')

    def about(self):
        template = template_loader.load(os.path.join('html', 'about.xml'))
        body = template.generate(req=self.req).render('xhtml')
        return Response(body, content_type='text/html')

    def contact(self):
        template = template_loader.load(os.path.join('html', 'contact.xml'))
        body = template.generate(req=self.req).render('xhtml')
        return Response(body, content_type='text/html')

    def journals_index(self):
        template = template_loader.load(os.path.join('html', 'journals_index.xml'))
        body = template.generate(req=self.req, 
                journals=[self.graph[rdfob.URIRef('http://miskinhill.com.au/journals/asees/')]] # XXX temp
                ).render('xhtml')
        return Response(body, content_type='text/html')

    def unapi(self):
        # XXX centralise this somehow
        formats = {
            # XXX docs
            'marcxml': E.format(name='marcxml', type='application/marcxml+xml'), 
            'nt': E.format(name='nt', type='text/plain'), 
            'bib': E.format(name='bib', type='text/x-bibtex'), 
            'mods': E.format(name='mods', type='application/mods+xml'), 
            'end': E.format(name='end', type='application/x-endnote-refer')
        }
        if 'id' in self.req.GET:
            try:
                node = self.graph[rdfob.URIRef(self.req.GET['id'])]
            except KeyError:
                raise exc.HTTPNotFound('URI not found in RDF graph').exception
            if 'format' in self.req.GET:
                raise exc.HTTPFound(location=(node.uri + '.' + self.req.GET['format']).encode('utf8'))
            body = E.formats(id=node.uri, *[formats[f] for f in self.formats_for_type(node)])
        else:
            body = E.formats(*formats.itervalues())
        return Response(lxml.etree.tostring(body, encoding='utf8', xml_declaration=True),
                content_type='application/xml')

    FORMATS = {
        rdfob.uriref('mhs:Journal'): ['nt', 'mods', 'marcxml'], 
        rdfob.uriref('mhs:Issue'): ['nt'], 
        rdfob.uriref('mhs:Article'): ['nt', 'bib', 'mods', 'end'], 
        rdfob.uriref('mhs:Review'): ['nt'], # XXX do more!
        rdfob.uriref('mhs:Author'): ['nt']
    }
    def formats_for_type(self, node):
        for type in node.types:
            if type in self.FORMATS:
                return self.FORMATS[type]
        return []

    def dispatch_rdf(self, path_info):
        decoded_uri = urllib.unquote('http://miskinhill.com.au' + 
                path_info).decode('utf8')
        format, decoded_uri = self.guess_format(decoded_uri)
        try:
            node = self.graph[rdfob.URIRef(decoded_uri)]
        except KeyError:
            raise exc.HTTPNotFound('URI not found in RDF graph').exception
        try:
            if format == 'html':
                template = template_loader.load(os.path.join('html', 
                        self.template_for_type(node) + '.xml'))
                body = template.generate(req=self.req, node=node).render('xhtml')
                return Response(body, content_type='text/html')
            if format == 'marcxml':
                template = template_loader.load(os.path.join('marcxml', 
                        self.template_for_type(node) + '.xml'))
                body = template.generate(req=self.req, node=node).render('xml')
                return Response(body, content_type='application/marcxml+xml', 
                        headers={'Content-Disposition': 'inline'})
            elif format == 'nt':
                return Response(self.graph.serialized(rdfob.URIRef(decoded_uri)), 
                        content_type='text/plain; charset=UTF-8')
            elif format == 'bib':
                template = template_loader.load(os.path.join('bibtex', 
                        self.template_for_type(node) + '.txt'), 
                        cls=NewTextTemplate)
                body = template.generate(req=self.req, node=node).render()
                return Response(body, content_type='text/x-bibtex')
            elif format == 'mods':
                template = template_loader.load(os.path.join('mods', 
                        self.template_for_type(node) + '.xml'))
                body = template.generate(req=self.req, node=node).render('xml')
                return Response(body, content_type='application/mods+xml', 
                        headers={'Content-Disposition': 'inline'})
            elif format == 'end':
                template = template_loader.load(os.path.join('end', 
                        self.template_for_type(node) + '.txt'), 
                        cls=NewTextTemplate)
                body = template.generate(req=self.req, node=node).render()
                return Response(body, content_type='application/x-endnote-refer')
            else:
                assert False, 'not reached'
        except TemplateNotFound:
            raise exc.HTTPNotFound('Matching template not found').exception

    RDF_TEMPLATES = {
        rdfob.uriref('mhs:Journal'): 'journal', 
        rdfob.uriref('mhs:Issue'): 'issue', 
        rdfob.uriref('mhs:Article'): 'article',
        rdfob.uriref('mhs:Review'): 'review', 
        rdfob.uriref('mhs:Author'): 'author'
    }
    def template_for_type(self, node):
        for type in node.types:
            if type in self.RDF_TEMPLATES:
                return self.RDF_TEMPLATES[type]
        raise exc.HTTPNotFound('Matching template not found').exception

    EXTENSIONS = ['.nt', '.html', '.marcxml', '.bib', '.mods', '.end']
    def guess_format(self, decoded_uri):
        for extension in self.EXTENSIONS:
            if decoded_uri.endswith(extension):
                return extension[1:], decoded_uri[:-len(extension)]
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
