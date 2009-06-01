#!/usr/bin/env python
# vim: set fileencoding=utf-8 :

import os, sys
import unittest
import tempfile
import lxml.etree, lxml.html

import rdfob
from representations import template_loader

TESTDATA = os.path.join(os.path.dirname(__file__), 'testdata')

class HtmlBookinfoTemplateTest(unittest.TestCase):

    def render(self, book_node):
        wrapper_template_file = tempfile.NamedTemporaryFile(suffix='.xml')
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
                book_node=book_node).render('xhtml', doctype='xhtml', encoding=None)

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

    def test_gbooksid(self):
        graph = rdfob.Graph()
        node = rdfob.BNode()
        graph._g.add((node, rdfob.RDF_TYPE, rdfob.uriref('mhs:Book')))
        graph._g.add((node, rdfob.uriref('dc:title'), rdfob.Literal('Some title')))
        graph._g.add((node, rdfob.uriref('dc:creator'), rdfob.Literal('Some Dude')))
        graph._g.add((node, rdfob.uriref('dc:date'), rdfob.Literal('1801', datatype=rdfob.uriref('xsd:date'))))
        graph._g.add((node, rdfob.uriref('dc:identifier'), rdfob.URIRef('http://books.google.com/books?id=12345')))
        root = lxml.html.fromstring(self.render(graph[node]))
        links, = root.find_class('links')
        a, = links.findall('a[@href="http://books.google.com/books?id=12345"]')
        self.assertEquals('Google Book Search', a.text_content().strip())

    def test_without_author(self):
        graph = rdfob.Graph()
        node = rdfob.BNode()
        graph._g.add((node, rdfob.RDF_TYPE, rdfob.uriref('mhs:Book')))
        graph._g.add((node, rdfob.uriref('dc:title'), rdfob.Literal('Some title')))
        root = lxml.html.fromstring(self.render(graph[node]))
        main, = root.find_class('main')
        self.assertEquals('Some title', main.text_content().strip())

    def test_cover_thumbnail(self):
        graph = rdfob.Graph()
        node = rdfob.BNode()
        graph._g.add((node, rdfob.RDF_TYPE, rdfob.uriref('mhs:Book')))
        graph._g.add((node, rdfob.uriref('dc:title'), rdfob.Literal('Some title')))
        graph._g.add((node, rdfob.uriref('dc:creator'), rdfob.Literal('Some Dude')))
        graph._g.add((node, rdfob.uriref('mhs:coverThumbnail'), rdfob.URIRef('http://example.com/thumb.gif')))
        root = lxml.html.fromstring(self.render(graph[node]))
        cover, = root.find_class('cover')
        img = cover.find('img')
        self.assertEquals('http://example.com/thumb.gif', img.get('src'))

    def test_russian_link(self):
        graph = rdfob.Graph()
        node = rdfob.BNode()
        graph._g.add((node, rdfob.RDF_TYPE, rdfob.uriref('mhs:Book')))
        graph._g.add((node, rdfob.uriref('dc:title'), rdfob.Literal(u'Русская книга', lang='ru')))
        graph._g.add((node, rdfob.uriref('dc:creator'), rdfob.Literal('Some Dude')))
        root = lxml.html.fromstring(self.render(graph[node]))
        links, = root.find_class('links')
        a = links.find('a[@href="http://www.ozon.ru/?context=search&text=%D0%F3%F1%F1%EA%E0%FF%20%EA%ED%E8%E3%E0%20Some%20Dude"]')
        self.assertEquals('Ozon.ru', a.text_content())

    def test_russian_link(self):
        graph = rdfob.Graph()
        node = rdfob.BNode()
        graph._g.add((node, rdfob.RDF_TYPE, rdfob.uriref('mhs:Book')))
        graph._g.add((node, rdfob.uriref('dc:title'), 
                rdfob.Literal(u'<span xmlns="http://www.w3.org/1999/xhtml" lang="ru">'
                    u'<em>Русская</em> книга</span>', datatype=rdfob.uriref('rdf:XMLLiteral'))))
        graph._g.add((node, rdfob.uriref('dc:creator'), rdfob.Literal('Some Dude')))
        root = lxml.html.fromstring(self.render(graph[node]))
        links, = root.find_class('links')
        a = links.find('a[@href="http://www.ozon.ru/?context=search&text=%D0%F3%F1%F1%EA%E0%FF%20%EA%ED%E8%E3%E0%20Some%20Dude"]')
        self.assertEquals('Ozon.ru', a.text_content())

    def test_available_from_link(self):
        graph = rdfob.Graph()
        node = rdfob.BNode()
        graph._g.add((node, rdfob.RDF_TYPE, rdfob.uriref('mhs:Book')))
        graph._g.add((node, rdfob.uriref('dc:title'), rdfob.Literal('Some title')))
        graph._g.add((node, rdfob.uriref('dc:creator'), rdfob.Literal('Some Dude')))
        graph._g.add((node, rdfob.uriref('dc:date'), rdfob.Literal('1801', datatype=rdfob.uriref('xsd:date'))))
        graph._g.add((node, rdfob.uriref('mhs:availableFrom'), rdfob.URIRef('http://example.com/teh-book')))
        root = lxml.html.fromstring(self.render(graph[node]))
        main, = root.find_class('main')
        a, = main.findall('.//a[@href="http://example.com/teh-book"]')
        self.assertEquals('Some title', a.text_content().strip())

class HtmlArticleinfoTemplateTest(unittest.TestCase):

    def render(self, node):
        wrapper_template_file = tempfile.NamedTemporaryFile(suffix='.xml')
        wrapper_template_file.write('''
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:xi="http://www.w3.org/2001/XInclude">
<xi:include href="${templates_dir}/html/_articleinfo.xml" />
<body>
${articleinfo(node)}
</body>
</html>''')
        wrapper_template_file.seek(0)
        template = template_loader.load(wrapper_template_file.name)
        return template.generate(templates_dir=os.path.dirname(os.path.abspath(__file__)) + '/templates',
                node=node).render('xhtml', encoding=None, doctype='xhtml')

    def setup_article(self, graph):
        # XXX just use meta.nt?
        journal = rdfob.BNode()
        graph._g.add((journal, rdfob.RDF_TYPE, rdfob.uriref('mhs:Journal')))
        graph._g.add((journal, rdfob.uriref('dc:title'), rdfob.Literal('Studies of something')))
        graph._g.add((journal, rdfob.uriref('dc:identifier'), rdfob.URIRef('urn:issn:12345678')))
        issue = rdfob.BNode()
        graph._g.add((issue, rdfob.RDF_TYPE, rdfob.uriref('mhs:Issue')))
        graph._g.add((issue, rdfob.uriref('mhs:isIssueOf'), journal))
        graph._g.add((issue, rdfob.uriref('mhs:volume'), rdfob.Literal(1)))
        graph._g.add((issue, rdfob.uriref('mhs:coverThumbnail'), rdfob.URIRef('http://example.com/thumb.gif')))
        article = rdfob.BNode()
        graph._g.add((article, rdfob.RDF_TYPE, rdfob.uriref('mhs:Article')))
        graph._g.add((article, rdfob.uriref('dc:isPartOf'), issue))
        graph._g.add((article, rdfob.uriref('dc:title'), rdfob.Literal('Some title')))
        graph._g.add((article, rdfob.uriref('dc:creator'), rdfob.Literal('Some Dude')))
        return article, issue, journal

    def test_worldcat_issn_link(self):
        graph = rdfob.Graph()
        article, issue, journal = self.setup_article(graph)
        root = lxml.html.fromstring(self.render(graph[article]))
        links, = root.find_class('links')
        a, = links.findall('a[@href="http://www.worldcat.org/search?q=issn:12345678"]')
        self.assertEquals('WorldCat', a.text_content().strip())

    def test_cover_thumbnail(self):
        graph = rdfob.Graph()
        article, issue, journal = self.setup_article(graph)
        root = lxml.html.fromstring(self.render(graph[article]))
        cover, = root.find_class('cover')
        img = cover.find('img')
        self.assertEquals('http://example.com/thumb.gif', img.get('src'))

    def test_article_available_from(self):
        graph = rdfob.Graph()
        article, issue, journal = self.setup_article(graph)
        graph._g.add((article, rdfob.uriref('mhs:availableFrom'), rdfob.URIRef('http://example.com/teh-article')))
        root = lxml.html.fromstring(self.render(graph[article]))
        main, = root.find_class('main')
        title = main.findall('a')[0]
        self.assertEquals('http://example.com/teh-article', title.get('href'))
        self.assertEquals('Some title', title.text_content().strip())

    def test_issue_available_from(self):
        graph = rdfob.Graph()
        article, issue, journal = self.setup_article(graph)
        graph._g.add((issue, rdfob.uriref('mhs:availableFrom'), rdfob.URIRef('http://example.com/teh-issue')))
        root = lxml.html.fromstring(self.render(graph[article]))
        issue_details, = root.find_class('issue')
        self.assertEquals(u'Vol.\u00a01', issue_details.find('a[@href="http://example.com/teh-issue"]').text_content().strip())

    def test_journal_available_from(self):
        graph = rdfob.Graph()
        article, issue, journal = self.setup_article(graph)
        graph._g.add((journal, rdfob.uriref('mhs:availableFrom'), rdfob.URIRef('http://example.com/teh-journal')))
        root = lxml.html.fromstring(self.render(graph[article]))
        issue_details, = root.find_class('issue')
        self.assertEquals('Studies of something', issue_details.find('a[@href="http://example.com/teh-journal"]').text_content())

if __name__ == '__main__':
    unittest.main()
