#!/usr/bin/env python

import re, os, urllib

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

        self.graph = rdfob.Graph('../rdf.nt')

        self.req = Request(environ)
        self.req.charset = 'utf8'

    def __iter__(self):
        try:
            resp = self.dispatch(self.req.path_info)
        except exc.HTTPException, e:
            resp = e
        return iter(resp(self.environ, self.start))

    TEMPLATES = {
        rdfob.uriref('mhs:Journal'): 'journal.xml', 
        rdfob.uriref('mhs:Issue'): 'journal_issue.xml', 
        rdfob.uriref('mhs:Article'): 'article.xml',
        rdfob.uriref('mhs:Author'): 'author.xml'
    }
    def dispatch(self, path_info):
        uri = rdfob.URIRef(urllib.unquote(
                'http://miskinhill.com.au' + path_info).decode('utf8'))
        try:
            node = self.graph[uri]
        except KeyError:
            raise exc.HTTPNotFound().exception
        return self.render_template(self.TEMPLATES[node.type], 
                {'node': node})

    def render_template(self, template_name, data):
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
