#!/usr/bin/env python
# vim: set fileencoding=utf-8 :

import os, sys
import unittest
import tempfile
from genshi.template import NewTextTemplate

import rdfob
from app import template_loader

class EndnoteArticleTemplateTest(unittest.TestCase):

    def setUp(self):
        graph = rdfob.Graph(os.path.join(os.path.dirname(__file__), 'testdata', 'templates', 'meta.nt'))
        node = graph[rdfob.URIRef(u'http://miskinhill.com.au/journals/test/1:1/article')]
        self.result = template_loader.load(os.path.join('end', 'article.txt'), 
                cls=NewTextTemplate).generate(node=node).render(encoding=None)

    def test_markup_stripped(self):
        title, = [line for line in self.result.splitlines() if line.startswith('%T')]
        self.assertEquals(u'%T Moscow 1937: the interpreter’s story', title)

if __name__ == '__main__':
    unittest.main()
