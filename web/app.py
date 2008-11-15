#!/usr/bin/env python

import re, os

from webob import Request, Response
from webob import exc
from genshi.template import TemplateLoader

from model import *

template_loader = TemplateLoader(
        os.path.join(os.path.dirname(__file__), 'templates', 'html'), 
        variable_lookup='strict', 
        auto_reload=True)

def ensure_trailing_slash(func):
    def _ensure_trailing_slash_wrapped_func(self, *args, **kwargs):
        if self.req.path_info[-1] != '/':
            raise exc.HTTPMovedPermanently(location=self.req.path_url + '/')
        else:
            return func(self, *args, **kwargs)
    return _ensure_trailing_slash_wrapped_func

class MiskinHillApplication(object):

    def __init__(self, environ, start_response):
        self.environ = environ
        self.start = start_response

        self.db = DbSession()

        self.req = Request(environ)
        self.req.charset = 'utf8'

    def __iter__(self):
        try:
            resp = self.dispatch(self.req.path_info)
        except exc.HTTPException, e:
            resp = e
        return iter(resp(self.environ, self.start))

    urls = [(r'/journals/?$', 'journals_index'), 
            (r'/journals/(\w+)/?$', 'journal'), 
            (r'/journals/([-\w]+)/([-\w]+)/([-\w]+)/?$', 'journal_issue')]
    urls = [(re.compile(patt), method) for patt, method in urls]
    def dispatch(self, path_info):
        for patt, method_name in self.urls:
            match = patt.match(self.req.path_info)
            if match:
                data = getattr(self, method_name)(
                        *[x.decode('utf8', 'ignore') for x in match.groups()])
                return self.render_template(method_name + '.xml', data)
        # no matching URI found, so give a 404
        raise exc.HTTPNotFound().exception

    def render_template(self, template_name, data):
        template = template_loader.load(template_name)
        body = template.generate(req=self.req, **data).render('html')
        return Response(body, content_type='text/html')

    @ensure_trailing_slash
    def journals_index(self):
        return {'journals': self.db.query(Journal).all()}

    @ensure_trailing_slash
    def journal(self, key):
        return {'journal': self.db.get(Journal, key)}

    @ensure_trailing_slash
    def journal_issue(self, journal, volume, issue):
        return {'issue': self.db.get(Issue, (journal, volume, issue))}

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
