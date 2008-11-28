#!/usr/bin/env python

import os, sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'lib'))
sys.path.insert(1, os.path.dirname(__file__))

import re, urllib

from webob import Request, Response
from webob import exc
from genshi.template import TemplateLoader

import rdfob

template_loader = TemplateLoader(
        os.path.join(os.path.dirname(__file__), 'templates', 'html'), 
        variable_lookup='strict', 
        auto_reload=True)

class MiskinHillApplication(object):

    def __init__(self, environ, start_response):
        self.environ = environ
        self.start = start_response

        self.graph = rdfob.Graph(os.path.join(os.path.dirname(__file__), '..', 'rdf.nt'))

        self.req = Request(environ)
        self.req.charset = 'utf8'

    METHODS = {
        '/': 'index'
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
        return self.render_template('index.xml')

    RDF_TEMPLATES = {
        rdfob.uriref('mhs:Journal'): 'journal.xml', 
        rdfob.uriref('mhs:Issue'): 'issue.xml', 
        rdfob.uriref('mhs:Article'): 'article.xml',
        rdfob.uriref('mhs:Author'): 'author.xml'
    }
    def dispatch_rdf(self, path_info):
        uri = rdfob.URIRef(urllib.unquote(
                'http://miskinhill.com.au' + path_info).decode('utf8'))
        try:
            node = self.graph[uri]
        except KeyError:
            raise exc.HTTPNotFound().exception
        return self.render_template(self.RDF_TEMPLATES[node.type], 
                {'node': node})

    def render_template(self, template_name, data={}):
        template = template_loader.load(template_name)
        body = template.generate(req=self.req, **data).render('xhtml')
        return Response(body, content_type='text/html')

application = MiskinHillApplication

if __name__ == '__main__':
    import optparse
    parser = optparse.OptionParser(usage='%prog --port=PORT')
    parser.add_option('-p', '--port', type='int',
            help='Port to serve on (default: %default)')
    parser.set_defaults(port=8080)
    options, args = parser.parse_args()
    from wsgiref.simple_server import make_server
    server = make_server('', options.port, application)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print '^C'
