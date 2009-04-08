#!/usr/bin/env python
# vim: set fileencoding=utf-8 :

import os, sys
import unittest
import tempfile
from genshi.template import NewTextTemplate, MarkupTemplate
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

    def test_publisher_appears(self):
        publisher, = self.root.find_class('publisher')
        self.assertEquals('Published by Awesome Publishing House',
                publisher.text_content().strip())

    def test_publisher_is_linked(self):
        publisher, = self.root.find_class('publisher')
        a, = publisher.getchildren()
        self.assertEquals('a', a.tag)
        self.assertEquals('http://awesome.com', a.get('href'))

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

class HtmlBookTemplateTest(unittest.TestCase):

    def render(self, book_node):
        wrapper_template_file = tempfile.NamedTemporaryFile()
        wrapper_template_file.write('''
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:xi="http://www.w3.org/2001/XInclude">
<xi:include href="${templates_dir}/html/_bookinfo.xml" />
<body>
${bookinfo(book_node)}
</body>
</html>''')
        wrapper_template_file.seek(0)
        template = template_loader.load(wrapper_template_file.name)
        return template.generate(templates_dir=os.path.dirname(os.path.abspath(__file__)) + '/templates',
                book_node=book_node).render('xhtml', doctype='xhtml')

    def test_without_publisher(self):
        graph = rdfob.Graph()
        node = rdfob.BNode()
        graph._g.add((node, rdfob.RDF_TYPE, rdfob.uriref('mhs:Book')))
        graph._g.add((node, rdfob.uriref('dc:title'), rdfob.Literal('Some title')))
        graph._g.add((node, rdfob.uriref('dc:creator'), rdfob.Literal('Some Dude')))
        graph._g.add((node, rdfob.uriref('dc:date'), rdfob.Literal('1801', datatype=rdfob.uriref('xsd:date'))))
        root = lxml.html.fromstring(self.render(graph[node]))
        publication, = root.find_class('publication')
        self.assertEquals('Published 1801', publication.text_content().strip())

    def test_without_date(self):
        graph = rdfob.Graph()
        node = rdfob.BNode()
        graph._g.add((node, rdfob.RDF_TYPE, rdfob.uriref('mhs:Book')))
        graph._g.add((node, rdfob.uriref('dc:title'), rdfob.Literal('Some title')))
        graph._g.add((node, rdfob.uriref('dc:creator'), rdfob.Literal('Some Dude')))
        graph._g.add((node, rdfob.uriref('dc:publisher'), rdfob.Literal('Some Publisher')))
        root = lxml.html.fromstring(self.render(graph[node]))
        publication, = root.find_class('publication')
        self.assertEquals('Published by Some Publisher', publication.text_content().strip())

    def test_without_date_or_publisher(self):
        graph = rdfob.Graph()
        node = rdfob.BNode()
        graph._g.add((node, rdfob.RDF_TYPE, rdfob.uriref('mhs:Book')))
        graph._g.add((node, rdfob.uriref('dc:title'), rdfob.Literal('Some title')))
        graph._g.add((node, rdfob.uriref('dc:creator'), rdfob.Literal('Some Dude')))
        root = lxml.html.fromstring(self.render(graph[node]))
        publication, = root.find_class('publication')
        self.assertEquals('', publication.text_content().strip())

    def test_with_only_gbooksid(self):
        graph = rdfob.Graph()
        node = rdfob.BNode()
        graph._g.add((node, rdfob.RDF_TYPE, rdfob.uriref('mhs:Book')))
        graph._g.add((node, rdfob.uriref('dc:title'), rdfob.Literal('Some title')))
        graph._g.add((node, rdfob.uriref('dc:creator'), rdfob.Literal('Some Dude')))
        graph._g.add((node, rdfob.uriref('dc:date'), rdfob.Literal('1801', datatype=rdfob.uriref('xsd:date'))))
        graph._g.add((node, rdfob.uriref('dc:identifier'), rdfob.URIRef('http://books.google.com/books?id=12345')))
        root = lxml.html.fromstring(self.render(graph[node]))
        links, = root.find_class('links')
        a, = links.findall('a')
        self.assertEquals('http://books.google.com/books?id=12345', a.get('href'))
        self.assertEquals('Google Book Search', a.text_content().strip())

if __name__ == '__main__':
    unittest.main()
