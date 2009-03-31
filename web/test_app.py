#!/usr/bin/env python

import os
import unittest
import wsgiref.validate
from webob import Request, Response
import tempfile
import lxml.etree
import urllib

import rdfob
import app

class AppTestCase(unittest.TestCase):
        
    def setUp(self):
        app._original_graph = app.graph
        app.graph = rdfob.Graph(os.path.join(os.path.dirname(__file__), 'testdata', 'meta.nt'))

    def tearDown(self):
        app.graph = app._original_graph
        del app._original_graph
    
    def get_response(self, req):
        return req.get_response(wsgiref.validate.validator(app.MiskinHillApplication))

class UnAPITest(AppTestCase):

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

    def test_unknown_format(self):
        id = 'http://miskinhill.com.au/journals/test/'
        res = self.get_response(Request.blank('/unapi?' + urllib.urlencode({'id': id, 'format': 'notexist'})))
        self.assertEquals(406, res.status_int)
        res.body # to keep validator happy

    def test_unacceptable_format(self):
        id = 'http://miskinhill.com.au/journals/test/1:1/article'
        res = self.get_response(Request.blank('/unapi?' + urllib.urlencode({'id': id, 'format': 'marcxml'})))
        self.assertEquals(406, res.status_int)
        res.body # to keep validator happy

    def test_redirect_for_id_and_format(self):
        id = 'http://miskinhill.com.au/journals/test/'
        res = self.get_response(Request.blank('/unapi?' + urllib.urlencode({'id': id, 'format': 'nt'})))
        self.assertEquals(302, res.status_int)
        self.assertEquals(id + '.nt', res.location)
        res.body # to keep validator happy

class RDFDispatchTest(AppTestCase):

    def test_nonexistent_uri(self):
        res = self.get_response(Request.blank('/journals/test/1:1/no-such-article'))
        self.assertEquals(404, res.status_int)
        res.body # to keep validator happy

    def test_unacceptable_format(self):
        res = self.get_response(Request.blank('/journals/test/1:1/article.marcxml'))
        self.assertEquals(404, res.status_int)
        res.body # to keep validator happy

class StaticTemplatesTest(AppTestCase):

    def test_index(self):
        res = self.get_response(Request.blank('/'))
        self.assertEquals(200, res.status_int)
        self.assert_('<title>Miskin Hill</title>' in res.body)

    def test_about(self):
        res = self.get_response(Request.blank('/about/'))
        self.assertEquals(200, res.status_int)
        self.assert_('<title>About - Miskin Hill</title>' in res.body)

    def test_contact(self):
        res = self.get_response(Request.blank('/contact/'))
        self.assertEquals(200, res.status_int)
        self.assert_('<title>Contact - Miskin Hill</title>' in res.body)

    def test_journal_index(self):
        res = self.get_response(Request.blank('/journals/'))
        self.assertEquals(200, res.status_int)
        self.assert_('<title>Journals - Miskin Hill</title>' in res.body)

if __name__ == '__main__':
    unittest.main()
