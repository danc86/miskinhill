#!/usr/bin/env python

import re

from webob import Request, Response
from webob import exc

from model import *

class MiskinHillApplication(object):

    def __init__(self, environ, start_response):
        self.environ = environ
        self.start = start_response

        self.req = Request(environ)
        self.req.charset = 'utf8'

    def __iter__(self):
        try:
            resp = self.dispatch(self.req.path_info)
        except exc.HTTPException, e:
            resp = e
        return iter(resp(self.environ, self.start))

    urls = [(r'/journals/$', 'journals_index'), 
            (r'/journals/(\w+)/?', 'journal')]
    urls = [(re.compile(patt), method) for patt, method in urls]
    def dispatch(self, path_info):
        for patt, method_name in self.urls:
            match = patt.match(self.req.path_info)
            if match:
                return getattr(self, method_name)(
                        *[x.decode('utf8', 'ignore') for x in match.groups()])
        # no matching URI found, so give a 404
        raise exc.HTTPNotFound().exception

    def journals_index(self):
        s = DbSession()
        journals = s.query(Journal).all()
        return Response(repr([j.key for j in journals]), content_type='text/plain')

    def journal(self, key):
        s = DbSession()
        journal = s.get(Journal, key)
        return Response(journal.title, content_type='text/plain')

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
