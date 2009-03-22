#!/usr/bin/env python
# vim: set fileencoding=utf-8 :

import os, sys
import unittest
import tempfile
from genshi.template import NewTextTemplate
import lxml.etree, lxml.html

import rdfob
from app import template_loader

class MockRequest(object):

    def __init__(self):
        self.script_name = ''

class EndnoteArticleTemplateTest(unittest.TestCase):

    def setUp(self):
        graph = rdfob.Graph(os.path.join(os.path.dirname(__file__), 'testdata', 'templates', 'meta.nt'))
        node = graph[rdfob.URIRef(u'http://miskinhill.com.au/journals/test/1:1/article')]
        self.result = template_loader.load(os.path.join('end', 'article.txt'), 
                cls=NewTextTemplate).generate(node=node).render(encoding=None)

    def test_markup_stripped(self):
        title, = [line for line in self.result.splitlines() if line.startswith('%T')]
        self.assertEquals(u'%T Moscow 1937: the interpreter’s story', title)

class HtmlJournalTemplateTest(unittest.TestCase):

    def setUp(self):
        graph = rdfob.Graph(os.path.join(os.path.dirname(__file__), 'testdata', 'templates', 'meta.nt'))
        node = graph[rdfob.URIRef(u'http://miskinhill.com.au/journals/test/')]
        result = template_loader.load(os.path.join('html', 'journal.xml'))\
                .generate(req=MockRequest(), node=node).render('xhtml', encoding=None)
        self.root = lxml.html.fromstring(result, parser=lxml.html.XHTMLParser())

    def test_issn_appears(self):
        issn, = self.root.find_class('issn')
        self.assertEquals('ISSN 12345678', issn.text.strip())

if __name__ == '__main__':
    unittest.main()
