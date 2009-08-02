#!/usr/bin/env python

import os, sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'lib'))
sys.path.insert(1, os.path.dirname(__file__))

import re, urllib

from webob import Request, Response
from webob import exc
from genshi.template import TemplateLoader, NewTextTemplate, TemplateNotFound
import lxml.etree, lxml.html
from lxml.builder import E

import rdfob
import representations
import citations

template_loader = TemplateLoader(
        os.path.join(os.path.dirname(__file__), 'templates', 'html'), 
        variable_lookup='strict', 
        auto_reload=True)

content_dir = '/home/dan/.www/miskinhill.com.au/content'

graph = None
def maybe_initialise_graph():
    global graph
    if graph is None: # XXX race here
        graph = rdfob.Graph(os.path.join(content_dir, 'meta.xml'))

class MiskinHillApplication(object):

    def __init__(self, environ, start_response):
        maybe_initialise_graph()

        self.environ = environ
        self.start = start_response

        self.req = Request(environ)
        self.req.charset = 'utf8'
        self.req.content_dir = content_dir # XXX dodgy?

    METHODS = {
        '/about/': 'about', 
        '/contact/': 'contact', 
        '/journals/': 'journals_index', 
        '/unapi': 'unapi', 
        '/sitemap.xml': 'sitemap', 
        '/rdfschema/1.0/': 'rdfschema_index',
        '/feeds/issues': 'issues_feed',
        '/feeds/world': 'world_rdf'
    }
    def __iter__(self):
        try:
            if self.req.path_info in self.METHODS:
                resp = getattr(self, self.METHODS[self.req.path_info])()
            elif self.req.path_info[-1] != '/' and (self.req.path_info + '/') in self.METHODS:
                resp = exc.HTTPFound(location='http://miskinhill.com.au' + self.req.path_info + '/')
            else:
                resp = self.dispatch_rdf(self.req.path_info)
        except exc.HTTPException, e:
            resp = e
        return iter(resp(self.environ, self.start))

    def about(self):
        template = template_loader.load('about.xml')
        body = template.generate(req=self.req).render('xhtml', doctype='xhtml')
        return Response(body, content_type='text/html')

    def contact(self):
        template = template_loader.load('contact.xml')
        body = template.generate(req=self.req).render('xhtml', doctype='xhtml')
        return Response(body, content_type='text/html')

    def journals_index(self):
        template = template_loader.load('journals_index.xml')
        body = template.generate(req=self.req, 
                journals=[j for j in graph.by_type('mhs:Journal') 
                          if j.uri.startswith(u'http://miskinhill.com.au/journals/')]
                ).render('xhtml', doctype='xhtml')
        return Response(body, content_type='text/html')

    def rdfschema_index(self):
        template = template_loader.load('rdfschema_index.xml')
        body = template.generate(req=self.req, 
                classes=[c for c in graph.by_type('rdfs:Class') 
                        if c.uri.startswith(u'http://miskinhill.com.au/rdfschema/')], 
                properties=[p for p in graph.by_type('rdf:Property') 
                        if p.uri.startswith(u'http://miskinhill.com.au/rdfschema/')]
                ).render('xhtml', doctype='xhtml')
        return Response(body, content_type='text/html')

    def unapi(self):
        if 'id' in self.req.GET:
            try:
                node = graph[rdfob.URIRef(self.req.GET['id'])]
            except KeyError:
                return exc.HTTPNotFound('URI not found in RDF graph')
            if 'format' in self.req.GET:
                try:
                    r = representations.BY_FORMAT[self.req.GET['format']]
                except KeyError:
                    return exc.HTTPNotAcceptable('Format %r not known' % self.req.GET['format'])
                if not r.can_represent(node):
                    return exc.HTTPNotAcceptable('Format %r not acceptable for this URI' % self.req.GET['format'])
                return exc.HTTPFound(location=(node.uri + '.' + r.format).encode('utf8'))
            else:
                body = E.formats(id=node.uri, 
                        *(E.format(name=r.format, type=r.content_type, docs=r.docs) 
                          for r in representations.ALL if r.can_represent(node)))
        else:
            body = E.formats(*(E.format(name=r.format, type=r.content_type, docs=r.docs) for r in representations.ALL))
        return Response(lxml.etree.tostring(body, encoding='utf8', xml_declaration=True),
                content_type='application/xml')

    def sitemap(self):
        template = template_loader.load('sitemap.xml')
        body = template.generate(req=self.req, 
                nodes=[s for s in graph.subjects()
                          if s.uri.startswith(u'http://miskinhill.com.au/')]
                ).render('xml')
        return Response(body, content_type='application/xml')

    def issues_feed(self):
        template = template_loader.load('../atom/issues_feed.xml')
        body = template.generate(req=self.req, 
                issues=sorted((i for i in graph.by_type('mhs:Issue') 
                               if unicode(i.uri).startswith(u'http://miskinhill.com.au/journals/')),
                              key=lambda i: i['mhs:onlinePublicationDate'], reverse=True)
                ).render('xml')
        return Response(body, content_type='application/atom+xml')

    def world_rdf(self):
        return Response(graph._g.to_string(name='rdfxml'),
                content_type='application/rdf+xml')

    def dispatch_rdf(self, path_info):
        decoded_uri = urllib.unquote('http://miskinhill.com.au' + path_info).decode('utf8')
        representation_cls = None

        # check for extension-style URL
        for r in representations.ALL:
            if decoded_uri.endswith('.' + r.format):
                representation_cls = r
                decoded_uri = decoded_uri[:-(len(r.format) + 1)]
                break

        try:
            node = graph[rdfob.Uri(decoded_uri)]
        except KeyError:
            if decoded_uri[-1] != '/' and rdfob.Uri(decoded_uri + '/') in graph:
                return exc.HTTPFound(location=(decoded_uri + '/').encode('utf8'))
            return exc.HTTPNotFound('URI not found in RDF graph')

        if representation_cls is not None:
            # extension-style URL was given, can we do it?
            if not representation_cls.can_represent(node):
                return exc.HTTPNotFound('Format %r not acceptable for this URI' % representation_cls.format)
        else:
            # do content negotiation
            best_content_type = self.req.accept.best_match(r.content_type for r in representations.for_node(node))
            if best_content_type is not None:
                representation_cls = representations.BY_CONTENT_TYPE[best_content_type]
            else:
                representation_cls = representations.for_node(node)[0]

        return representation_cls(self.req, node).response()

class Relativizer(object):

    def __init__(self, wrapped):
        self.wrapped = wrapped

    def __call__(self, environ, start_response):
        res = Request(environ).get_response(self.wrapped)
        if res.status_int == 200 and res.content_type == 'text/html':
            tree = lxml.html.fromstring(res.body, parser=lxml.html.XHTMLParser())
            tree.rewrite_links(self.do_it)
            res.body = lxml.html.tostring(tree, method='xml', encoding='utf8')
        return res(environ, start_response)

    def do_it(self, link):
        if link.startswith('http://miskinhill.com.au'):
            return link[24:]
        return link

application = Relativizer(MiskinHillApplication)

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
