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

        self.graph = rdfob.Graph(
                os.path.join(os.path.dirname(__file__), '..', 'rdf.nt'), 
                os.path.join(os.path.dirname(__file__), '..', 'rdfschema', 'foaf.nt'), 
                os.path.join(os.path.dirname(__file__), '..', 'rdfschema', 'dcterms.nt'))

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
        rdfob.uriref('mhs:Review'): 'review.xml', 
        rdfob.uriref('mhs:Author'): 'author.xml'
    }
    def dispatch_rdf(self, path_info):
        decoded_uri = urllib.unquote('http://miskinhill.com.au' + 
                path_info).decode('utf8')
        format, decoded_uri = self.guess_format(decoded_uri)
        try:
            node = self.graph[rdfob.URIRef(decoded_uri)]
        except KeyError:
            raise exc.HTTPNotFound('URI not found in RDF graph').exception
        if format == 'text/html':
            template_name = None
            for type in node.types:
                if type in self.RDF_TEMPLATES:
                    template_name = self.RDF_TEMPLATES[type]
            if template_name is None:
                raise exc.HTTPNotFound('Matching template not found').exception
            template = template_loader.load(template_name)
            body = template.generate(req=self.req, node=node).render('xhtml')
            return Response(body, content_type='text/html')
        elif format == 'text/plain':
            return Response(self.graph.serialized(rdfob.URIRef(decoded_uri)), 
                    content_type='text/plain')
        else:
            assert False, 'not reached'

    def guess_format(self, decoded_uri):
        if decoded_uri.endswith(u'.nt'):
            format = 'text/plain' # XXX actually n-triples, should distinguish it?
            return format, decoded_uri[:-3]
        if decoded_uri.endswith(u'.html'):
            format = 'text/html'
            return format, decoded_uri[:-5]
        else:
            # XXX should check Accept header too
            return 'text/html', decoded_uri

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
