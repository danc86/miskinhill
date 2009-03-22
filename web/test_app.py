#!/usr/bin/env python

import unittest
import wsgiref.validate
from webob import Request, Response
import tempfile
import lxml.etree
import urllib

import app

class UnAPITest(unittest.TestCase):

    TEST_META = """
<http://miskinhill.com.au/journals/test/> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://miskinhill.com.au/rdfschema/1.0/Journal> .
<http://miskinhill.com.au/journals/test/1:1/> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://miskinhill.com.au/rdfschema/1.0/Issue> .
<http://miskinhill.com.au/journals/test/1:1/one-article> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://miskinhill.com.au/rdfschema/1.0/Article> .
<http://miskinhill.com.au/journals/test/1:1/another-article> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://miskinhill.com.au/rdfschema/1.0/Article> .
<http://miskinhill.com.au/authors/test-author> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://miskinhill.com.au/rdfschema/1.0/Author> .
"""
        
    def setUp(self):
        self.meta = tempfile.NamedTemporaryFile()
        self.meta.write(self.TEST_META)
        self.meta.seek(0)
        app._original_rdf_imports = app._rdf_imports
        app._rdf_imports = lambda: [self.meta.name]

    def tearDown(self):
        app._rdf_imports = app._original_rdf_imports
        del app._original_rdf_imports
        del self.meta
    
    def get_response(self, req):
        return req.get_response(wsgiref.validate.validator(app.MiskinHillApplication))

    def test_formats(self):
        res = self.get_response(Request.blank('/unapi'))
        self.assertEquals(200, res.status_int)
        self.assertEquals('application/xml', res.content_type)
        root = lxml.etree.fromstring(res.body)
        self.assertEquals('formats', root.tag)
        for child in root.getchildren():
            self.assertEquals('format', child.tag)

    def test_formats_for_id(self):
        id = 'http://miskinhill.com.au/journals/test/'
        res = self.get_response(Request.blank('/unapi?' + urllib.urlencode({'id': id})))
        self.assertEquals(200, res.status_int)
        self.assertEquals('application/xml', res.content_type)
        root = lxml.etree.fromstring(res.body)
        self.assertEquals('formats', root.tag)
        self.assertEquals(id, root.get('id'))
        for child in root.getchildren():
            self.assertEquals('format', child.tag)

    def test_nonexistent_id(self):
        res = self.get_response(Request.blank('/unapi?id=notexist'))
        self.assertEquals(404, res.status_int)
        res.body # to keep validator happy

    def test_redirect_for_id_and_format(self):
        id = 'http://miskinhill.com.au/journals/test/'
        res = self.get_response(Request.blank('/unapi?' + urllib.urlencode({'id': id, 'format': 'nt'})))
        self.assertEquals(302, res.status_int)
        self.assertEquals(id + '.nt', res.location)
        res.body # to keep validator happy

if __name__ == '__main__':
    unittest.main()
