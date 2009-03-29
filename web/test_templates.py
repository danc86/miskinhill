#!/usr/bin/env python
# vim: set fileencoding=utf-8 :

import os, sys
import unittest
import tempfile
from genshi.template import NewTextTemplate
import lxml.etree, lxml.html

import rdfob
from representations import template_loader

TESTDATA = os.path.join(os.path.dirname(__file__), 'testdata')

class MockRequest(object):

    def __init__(self):
        self.script_name = ''
        self.content_dir = TESTDATA

class EndnoteArticleTemplateTest(unittest.TestCase):

    def setUp(self):
        graph = rdfob.Graph(os.path.join(TESTDATA, 'meta.nt'))
        node = graph[rdfob.URIRef(u'http://miskinhill.com.au/journals/test/1:1/article')]
        self.result = template_loader.load(os.path.join('end', 'article.txt'), 
                cls=NewTextTemplate).generate(node=node).render(encoding=None)

    def test_markup_stripped(self):
        title, = [line for line in self.result.splitlines() if line.startswith('%T')]
        self.assertEquals(u'%T Moscow 1937: the interpreter’s story', title)

class ModsArticleTemplateTest(unittest.TestCase):

    def setUp(self):
        graph = rdfob.Graph(os.path.join(TESTDATA, 'meta.nt'))
        node = graph[rdfob.URIRef(u'http://miskinhill.com.au/journals/test/1:1/article')]
        result = template_loader.load(os.path.join('mods', 'article.xml')).generate(req=None, node=node).render('xml')
        self.root = lxml.etree.fromstring(result)

    def test_markup_stripped(self):
        title_xpath = lxml.etree.XPath('//mods:mods/mods:titleInfo/mods:title', 
                namespaces={'mods': 'http://www.loc.gov/mods/v3'})
        title, = title_xpath(self.root)
        self.assertEquals(0, len(title.getchildren()))

    def test_volume_number(self):
        volume_number, = lxml.etree.XPath('//mods:mods/mods:relatedItem[@type="host"]/mods:part/mods:detail[@type="volume"]/mods:number', 
                namespaces={'mods': 'http://www.loc.gov/mods/v3'})(self.root)
        self.assertEquals('1', volume_number.text)

    def test_issue_number(self):
        issue_number, = lxml.etree.XPath('//mods:mods/mods:relatedItem[@type="host"]/mods:part/mods:detail[@type="issue"]/mods:number', 
                namespaces={'mods': 'http://www.loc.gov/mods/v3'})(self.root)
        self.assertEquals('1', issue_number.text)

    def test_location_urls(self):
        urls = lxml.etree.XPath('//mods:mods/mods:location/mods:url', 
                namespaces={'mods': 'http://www.loc.gov/mods/v3'})(self.root)
        self.assertEquals(set([('Original print version', 'raw object', 'http://miskinhill.com.au/journals/test/1:1/article.pdf'), 
                               ('HTML version', 'object in context', 'http://miskinhill.com.au/journals/test/1:1/article')]), 
                set((u.get('displayLabel'), u.get('access'), u.text) for u in urls))

    def test_language(self):
        # comes from journal
        languages = lxml.etree.XPath('//mods:mods/mods:language/mods:languageTerm[@type="code" and @authority="rfc3066"]', 
                namespaces={'mods': 'http://www.loc.gov/mods/v3'})(self.root)
        self.assertEquals(set(['en', 'ru']), set(l.text for l in languages))

    def test_genre(self):
        genre, = lxml.etree.XPath('//mods:mods/mods:genre[@authority="marcgt"]', 
                namespaces={'mods': 'http://www.loc.gov/mods/v3'})(self.root)
        self.assertEquals('periodical', genre.text)

class HtmlJournalTemplateTest(unittest.TestCase):

    def setUp(self):
        graph = rdfob.Graph(os.path.join(TESTDATA, 'meta.nt'))
        node = graph[rdfob.URIRef(u'http://miskinhill.com.au/journals/test/')]
        result = template_loader.load(os.path.join('html', 'journal.xml'))\
                .generate(req=MockRequest(), node=node).render('xhtml', doctype='xhtml')
        self.root = lxml.html.fromstring(result)

    def test_issn_appears(self):
        issn, = self.root.find_class('issn')
        self.assertEquals('ISSN 12345678', issn.text.strip())

class HtmlArticleTemplateTest(unittest.TestCase):

    def setUp(self):
        graph = rdfob.Graph(os.path.join(TESTDATA, 'meta.nt'))
        node = graph[rdfob.URIRef(u'http://miskinhill.com.au/journals/test/1:1/article')]
        result = template_loader.load(os.path.join('html', 'article.xml'))\
                .generate(req=MockRequest(), node=node).render('xhtml', doctype='xhtml')
        self.root = lxml.html.fromstring(result)

    def test_coins_appear(self):
        for coins in self.root.find_class('Z3988'):
            self.assertEquals('span', coins.tag)
            self.assertEquals(None, coins.text)
            self.assert_(coins.get('title'))

    def test_published_in(self):
        article_meta, = self.root.find_class('article-meta')
        published_in_links = article_meta.xpath('./p[1]/a')
        self.assertEquals('/journals/test/1:1/', published_in_links[0].get('href'))
        self.assertEquals('Vol. 1, Issue 1', published_in_links[0].text_content())
        self.assertEquals('/journals/test/', published_in_links[1].get('href'))
        self.assertEquals('Test Journal of Good Stuff', published_in_links[1].text_content())

class HtmlReviewTemplateTest(unittest.TestCase):

    def setUp(self):
        graph = rdfob.Graph(os.path.join(TESTDATA, 'meta.nt'))
        node = graph[rdfob.URIRef(u'http://miskinhill.com.au/journals/test/1:1/reviews/review')]
        result = template_loader.load(os.path.join('html', 'review.xml'))\
                .generate(req=MockRequest(), node=node).render('xhtml', doctype='xhtml')
        self.root = lxml.html.fromstring(result)

    def test_published_in(self):
        review_meta, = self.root.find_class('review-meta')
        published_in_links = review_meta.xpath('./p[1]/a')
        self.assertEquals('/journals/test/1:1/', published_in_links[0].get('href'))
        self.assertEquals('Vol. 1, Issue 1', published_in_links[0].text_content())
        self.assertEquals('/journals/test/', published_in_links[1].get('href'))
        self.assertEquals('Test Journal of Good Stuff', published_in_links[1].text_content())

if __name__ == '__main__':
    unittest.main()
