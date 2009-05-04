#!/usr/bin/env python
# vim: set fileencoding=utf-8 :

import os, sys
import unittest
import lxml.etree, lxml.html

import rdfob
import representations

TESTDATA = os.path.join(os.path.dirname(__file__), 'testdata')

class MockRequest(object):

    def __init__(self):
        self.script_name = ''
        self.content_dir = TESTDATA

class NTriplesArticleRepresentationTest(unittest.TestCase):

    def setUp(self):
        graph = rdfob.Graph(os.path.join(TESTDATA, 'meta.nt'))
        node = graph[rdfob.URIRef(u'http://miskinhill.com.au/journals/test/1:1/article')]
        self.response = representations.NTriplesRepresentation(MockRequest(), node).response()

    def test_content_type(self):
        self.assertEquals('application/x-turtle', self.response.content_type)

class EndnoteArticleRepresentationTest(unittest.TestCase):

    def setUp(self):
        graph = rdfob.Graph(os.path.join(TESTDATA, 'meta.nt'))
        node = graph[rdfob.URIRef(u'http://miskinhill.com.au/journals/test/1:1/article')]
        self.response = representations.EndnoteRepresentation(MockRequest(), node).response()

    def test_content_type(self):
        self.assertEquals('application/x-endnote-refer', self.response.content_type)

    def test_markup_stripped(self):
        title, = [line for line in self.response.body.decode('utf8').splitlines() if line.startswith('%T')]
        self.assertEquals(u'%T Moscow 1937: the interpreter’s story', title)

class ModsArticleRepresentationTest(unittest.TestCase):

    def setUp(self):
        graph = rdfob.Graph(os.path.join(TESTDATA, 'meta.nt'))
        node = graph[rdfob.URIRef(u'http://miskinhill.com.au/journals/test/1:1/article')]
        self.response = representations.MODSRepresentation(MockRequest(), node).response()
        self.root = lxml.etree.fromstring(self.response.body)

    def test_content_type(self):
        self.assertEquals('application/mods+xml', self.response.content_type)
        self.assertEquals('inline', self.response.content_disposition)

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

class MarcxmlJournalRepresentationTest(unittest.TestCase):

    def setUp(self):
        graph = rdfob.Graph(os.path.join(TESTDATA, 'meta.nt'))
        node = graph[rdfob.URIRef(u'http://miskinhill.com.au/journals/test/')]
        self.response = representations.MARCXMLRepresentation(MockRequest(), node).response()
        self.root = lxml.etree.fromstring(self.response.body)

    def test_content_type(self):
        self.assertEquals('application/marcxml+xml', self.response.content_type)
        self.assertEquals('inline', self.response.content_disposition)

class HtmlJournalRepresentationTest(unittest.TestCase):

    def setUp(self):
        graph = rdfob.Graph(os.path.join(TESTDATA, 'meta.nt'))
        node = graph[rdfob.URIRef(u'http://miskinhill.com.au/journals/test/')]
        self.response = representations.HTMLRepresentation(MockRequest(), node).response()
        self.root = lxml.html.fromstring(self.response.body)

    def test_content_type(self):
        self.assertEquals('text/html', self.response.content_type)

    def test_issn_appears(self):
        issn, = self.root.find_class('issn')
        self.assertEquals('ISSN 12345678', issn.text.strip())

    def test_publisher_appears(self):
        publisher, = self.root.find_class('publisher')
        self.assertEquals('Published by Awesome Publishing House',
                publisher.text_content().strip())

    def test_publisher_is_linked(self):
        publisher, = self.root.find_class('publisher')
        a, = publisher.getchildren()
        self.assertEquals('a', a.tag)
        self.assertEquals('http://awesome.com', a.get('href'))

class HtmlArticleRepresentationTest(unittest.TestCase):

    def setUp(self):
        graph = rdfob.Graph(os.path.join(TESTDATA, 'meta.nt'))
        node = graph[rdfob.URIRef(u'http://miskinhill.com.au/journals/test/1:1/article')]
        self.response = representations.HTMLRepresentation(MockRequest(), node).response()
        self.root = lxml.html.fromstring(self.response.body)

    def test_content_type(self):
        self.assertEquals('text/html', self.response.content_type)

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

class HtmlReviewRepresentationTest(unittest.TestCase):

    def setUp(self):
        graph = rdfob.Graph(os.path.join(TESTDATA, 'meta.nt'))
        node = graph[rdfob.URIRef(u'http://miskinhill.com.au/journals/test/1:1/reviews/review')]
        self.response = representations.HTMLRepresentation(MockRequest(), node).response()
        self.root = lxml.html.fromstring(self.response.body)

    def test_content_type(self):
        self.assertEquals('text/html', self.response.content_type)

    def test_published_in(self):
        review_meta, = self.root.find_class('review-meta')
        published_in_links = review_meta.xpath('./p[1]/a')
        self.assertEquals('/journals/test/1:1/', published_in_links[0].get('href'))
        self.assertEquals('Vol. 1, Issue 1', published_in_links[0].text_content())
        self.assertEquals('/journals/test/', published_in_links[1].get('href'))
        self.assertEquals('Test Journal of Good Stuff', published_in_links[1].text_content())

if __name__ == '__main__':
    unittest.main()
