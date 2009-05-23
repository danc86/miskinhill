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

TESTDATA = os.path.join(os.path.dirname(__file__), 'testdata')

class AppTestCase(unittest.TestCase):
        
    def setUp(self):
        app._original_graph = app.graph
        app.graph = rdfob.Graph(os.path.join(os.path.dirname(__file__), 'testdata', 'meta.xml'))
        app._original_content_dir = app.content_dir
        app.content_dir = TESTDATA

    def tearDown(self):
        app.graph = app._original_graph
        del app._original_graph
        app.content_dir = app._original_content_dir
        del app._original_content_dir
    
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

    def test_extension_style(self):
        res = self.get_response(Request.blank('/journals/test/1:1/article.xml'))
        self.assertEquals(200, res.status_int)
        self.assertEquals('application/rdf+xml', res.content_type)
        res.body # to keep validator happy

    def test_content_negotiation(self):
        res = self.get_response(Request.blank('/journals/test/1:1/article', 
                accept='text/html; q=0.5, application/rdf+xml'))
        self.assertEquals(200, res.status_int)
        self.assertEquals('application/rdf+xml', res.content_type)
        res.body # to keep validator happy

    def test_accepting_no_known_types(self):
        res = self.get_response(Request.blank('/journals/test/1:1/article', 
                accept='application/foobar, text/vcard'))
        self.assertEquals(200, res.status_int)
        self.assertEquals('text/html', res.content_type)
        res.body # to keep validator happy

    def test_accepting_only_types_which_are_known_but_not_applicable(self):
        res = self.get_response(Request.blank('/journals/test/', 
                accept='application/atom+xml, application/x-endnote-refer'))
        self.assertEquals(200, res.status_int)
        self.assertEquals('text/html', res.content_type)
        res.body # to keep validator happy

    def test_default_content_type_is_html(self):
        res = self.get_response(Request.blank('/journals/test/1:1/article'))
        self.assertEquals(200, res.status_int)
        self.assertEquals('text/html', res.content_type)
        res.body # to keep validator happy

    def test_nonexistent_uri(self):
        res = self.get_response(Request.blank('/journals/test/1:1/no-such-article'))
        self.assertEquals(404, res.status_int)
        res.body # to keep validator happy

    def test_unacceptable_format(self):
        res = self.get_response(Request.blank('/journals/test/1:1/article.marcxml'))
        self.assertEquals(404, res.status_int)
        res.body # to keep validator happy

    def test_slash_redirect(self):
        res = self.get_response(Request.blank('/journals/test'))
        self.assertEquals(302, res.status_int)
        self.assertEquals('http://miskinhill.com.au/journals/test/', res.location)
        res.body # to keep validator happy

class StaticTemplatesTest(AppTestCase):

    def test_about(self):
        res = self.get_response(Request.blank('/about/'))
        self.assertEquals(200, res.status_int)
        self.assert_('<title>About - Miskin Hill</title>' in res.body)

    def test_about_without_slash(self):
        res = self.get_response(Request.blank('/about'))
        self.assertEquals(302, res.status_int)
        self.assertEquals('http://miskinhill.com.au/about/', res.location)
        res.body

    def test_contact(self):
        res = self.get_response(Request.blank('/contact/'))
        self.assertEquals(200, res.status_int)
        self.assert_('<title>Contact - Miskin Hill</title>' in res.body)

    def test_contact_without_slash(self):
        res = self.get_response(Request.blank('/contact'))
        self.assertEquals(302, res.status_int)
        self.assertEquals('http://miskinhill.com.au/contact/', res.location)
        res.body

    def test_journal_index(self):
        res = self.get_response(Request.blank('/journals/'))
        self.assertEquals(200, res.status_int)
        self.assert_('<title>Journals - Miskin Hill</title>' in res.body)

    def test_journal_index_without_slash(self):
        res = self.get_response(Request.blank('/journals'))
        self.assertEquals(302, res.status_int)
        self.assertEquals('http://miskinhill.com.au/journals/', res.location)
        res.body

if __name__ == '__main__':
    unittest.main()
