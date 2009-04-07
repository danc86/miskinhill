#!/usr/bin/env python
# vim:fileencoding=utf-8

import unittest
import cgi
import lxml.html

import citations
import rdfob

def x(s):
    return lxml.html.fragment_fromstring(s)

class NormalizeWhitespaceTest(unittest.TestCase):

    def test_nbsp(self):
        self.assertEquals(u'a b', citations.normalize_whitespace(u'a\u00a0b'))

    def test_newline(self):
        self.assertEquals(u'a b', citations.normalize_whitespace(u'a \n b'))

    def test_edges(self):
        self.assertEquals(u' a b ', citations.normalize_whitespace(u'\ta \n\tb '))

class HasClassTest(unittest.TestCase):

    def test_absent(self):
        self.assertEquals(False, citations.has_class(x('<span>asdf</span>'), 'class1'))

    def test_empty(self):
        self.assertEquals(False, citations.has_class(x('<span class="">asdf</span>'), 'class1'))

    def test_single(self):
        self.assertEquals(False, citations.has_class(x('<span class="class2">asdf</span>'), 'class1'))
        self.assertEquals(True, citations.has_class(x('<span class="class1">asdf</span>'), 'class1'))

    def test_multiple(self):
        self.assertEquals(False, citations.has_class(x('<span class="class2 class3">asdf</span>'), 'class1'))
        self.assertEquals(True, citations.has_class(x('<span class="class1 class2">asdf</span>'), 'class1'))

class CitationFromElemTest(unittest.TestCase):

    def test_book(self):
        citation = citations.Citation.from_elem(x(u'''
                <span class="citation book"><span class="au">Charles Vinicombe Penrose</span>, 
                <em class="btitle">A Memoir of James Trevenen</em>, edited by 
                <span class="au">Christopher Lloyd</span> and 
                <span class="au">R.&nbsp;C. Anderson</span>, (<span class="place">London</span>: 
                <span class="pub">Navy Records Society</span>, <span class="date">1959</span>), 
                <span class="spage">90</span>–<span class="epage">91</span></span>
                '''))
        self.assertEquals('book', citation.genre)
        self.assertEquals(['Charles Vinicombe Penrose', 'Christopher Lloyd', 'R. C. Anderson'], citation.au)
        self.assertEquals(['A Memoir of James Trevenen'], citation.btitle)
        self.assertEquals(['London'], citation.place)
        self.assertEquals(['Navy Records Society'], citation.pub)
        self.assertEquals(['1959'], citation.date)
        self.assertEquals(['90'], citation.spage)
        self.assertEquals(['91'], citation.epage)

    def test_bookitem(self):
        citation = citations.Citation.from_elem(x(u'''
                <span class="citation bookitem"><span class="au" title="Lydia 
                Black">Black</span><span class="atitle" title="“The Russians were 
                Coming…”" /><span class="au" title="Robin Inglis" /><span class="btitle" 
                title="Spain and the North Pacific Coast" /><span class="place" 
                title="Vancouver" /><span class="pub" title="Maritime Museum Society" 
                /><span class="date" title="1992" />, 
                <span class="spage">31</span>–<span class="epage">29</span></span>
                '''))
        self.assertEquals('bookitem', citation.genre)
        self.assertEquals(['Lydia Black', 'Robin Inglis'], citation.au)
        self.assertEquals([u'“The Russians were Coming…”'], citation.atitle)
        self.assertEquals(['Spain and the North Pacific Coast'], citation.btitle)
        self.assertEquals(['Vancouver'], citation.place)
        self.assertEquals(['Maritime Museum Society'], citation.pub)
        self.assertEquals(['1992'], citation.date)
        self.assertEquals(['31'], citation.spage)
        self.assertEquals(['29'], citation.epage) # sic

    def test_thesis(self):
        citation = citations.Citation.from_elem(x(u'''
                <span class="citation thesis"><span class="au">Anthony H. 
                Hull</span>, <em class="btitle">Spanish and Russian Rivalry 
                in the North Pacific Regions of the New World</em>, University of 
                Alabama PhD thesis, UMI microfilm, 
                <span class="spage">112</span>–<span class="epage">113</span></span>
                '''))
        self.assertEquals('thesis', citation.genre)
        self.assertEquals(['Anthony H. Hull'], citation.au)
        self.assertEquals(['Spanish and Russian Rivalry in the North Pacific '
                        'Regions of the New World'], citation.btitle)
        self.assertEquals(['112'], citation.spage)
        self.assertEquals(['113'], citation.epage)

    def test_proceeding(self):
        citation = citations.Citation.from_elem(x(u'''
                <span class="citation proceeding"><span class="au">Valery O. 
                Shubin</span>, ‘<span class="atitle">Russian Settlements in the 
                Kuril Islands in the 18th and 19th centuries</span>’, 
                <em class="btitle">Russia in North America: Proceedings of 
                the 2nd International Conference on Russian America</em> 
                (<span class="place">Kingston and Fairbanks</span>: 
                <span class="pub">Limestone Press</span>, 
                <span class="date">1990</span>), 
                <span class="spage">425</span>–<span class="epage">450</span></span>
                '''))
        self.assertEquals('proceeding', citation.genre)
        self.assertEquals(['Valery O. Shubin'], citation.au)
        self.assertEquals(['Russian Settlements in the Kuril Islands in the '
                        '18th and 19th centuries'], citation.atitle)
        self.assertEquals(['Russia in North America: Proceedings of the '
                        '2nd International Conference on Russian America'], citation.btitle)
        self.assertEquals(['Kingston and Fairbanks'], citation.place)
        self.assertEquals(['Limestone Press'], citation.pub)
        self.assertEquals(['1990'], citation.date)
        self.assertEquals(['425'], citation.spage)
        self.assertEquals(['450'], citation.epage)

    def test_article(self):
        citation = citations.Citation.from_elem(x(u'''
                <span class="citation article"><span class="au" lang="ru">Ал.&nbsp;П. 
                Соколов</span>, «<span class="atitle" lang="ru">Приготовление 
                кругосветной экспедиции 1787 года, под начальством Муловского</span>», 
                <em class="jtitle" lang="ru">Записки Гидрографического Департамента 
                Морского Министерства</em>, 
                <span lang="ru">часть&nbsp;<span class="volume" title="6">VI</span></span>, 
                <span class="date">1848</span>&nbsp;г., 
                <span class="spage">142</span>–<span class="epage">191</span></span>
                '''))
        self.assertEquals('article', citation.genre)
        self.assertEquals([u'Ал. П. Соколов'], citation.au)
        self.assertEquals([(u'Приготовление кругосветной экспедиции '
                        u'1787 года, под начальством Муловского')], citation.atitle)
        self.assertEquals([(u'Записки Гидрографического Департамента '
                        u'Морского Министерства')], citation.jtitle)
        self.assertEquals(['6'], citation.volume)
        self.assertEquals(['1848'], citation.date)
        self.assertEquals(['142'], citation.spage)
        self.assertEquals(['191'], citation.epage)

class CitationCoinsTest(unittest.TestCase):

    def test_book(self):
        citation = citations.Citation()
        citation.genre = 'book'
        citation.au = ['Charles Vinicombe Penrose', 'Christopher Lloyd', 'R. C. Anderson']
        citation.btitle = ['A Memoir of James Trevenen']
        citation.place = ['London']
        citation.pub = ['Navy Records Society']
        citation.date = ['1959']
        citation.spage = ['90']
        citation.epage = ['91']
        coins = citation.coins()
        self.assertEquals('span', coins.tag)
        self.assertEquals('Z3988', coins.get('class'))
        self.assertEquals({
                'ctx_ver': ['Z39.88-2004'], 
                'rft_val_format': ['info:ofi/fmt:kev:mtx:book'], 
                'rft.genre': ['book'], 
                'rft.au': ['Charles Vinicombe Penrose', 'Christopher Lloyd', 'R. C. Anderson'], 
                'rft.btitle': ['A Memoir of James Trevenen'], 
                'rft.place': ['London'], 
                'rft.pub': ['Navy Records Society'], 
                'rft.date': ['1959'], 
                'rft.spage': ['90'], 
                'rft.epage': ['91']}, 
                cgi.parse_qs(coins.get('title')))

    def test_bookitem(self):
        citation = citations.Citation()
        citation.genre = 'bookitem'
        citation.au = ['Lydia Black', 'Robin Inglis']
        citation.atitle = [u'“The Russians were Coming…”']
        citation.btitle = ['Spain and the North Pacific Coast']
        citation.place = ['Vancouver']
        citation.pub = ['Maritime Museum Society']
        citation.date = ['1992']
        citation.spage = ['31']
        citation.epage = ['29'] # sic
        coins = citation.coins()
        self.assertEquals('span', coins.tag)
        self.assertEquals('Z3988', coins.get('class'))
        self.assertEquals({
                'ctx_ver': ['Z39.88-2004'], 
                'rft_val_format': ['info:ofi/fmt:kev:mtx:book'], 
                'rft.genre': ['bookitem'], 
                'rft.au': ['Lydia Black', 'Robin Inglis'], 
                'rft.atitle': ['“The Russians were Coming…”'], 
                'rft.btitle': ['Spain and the North Pacific Coast'], 
                'rft.place': ['Vancouver'], 
                'rft.pub': ['Maritime Museum Society'], 
                'rft.date': ['1992'], 
                'rft.spage': ['31'], 
                'rft.epage': ['29']},
                cgi.parse_qs(coins.get('title')))

    def test_thesis(self):
        citation = citations.Citation()
        citation.genre = 'thesis'
        citation.au = ['Anthony H. Hull']
        citation.btitle = ['Spanish and Russian Rivalry in the North Pacific '
                        'Regions of the New World']
        citation.spage = ['112']
        citation.epage = ['113']
        coins = citation.coins()
        self.assertEquals('span', coins.tag)
        self.assertEquals('Z3988', coins.get('class'))
        self.assertEquals({
                'ctx_ver': ['Z39.88-2004'], 
                'rft_val_format': ['info:ofi/fmt:kev:mtx:book'], 
                'rft.genre': ['document'], 
                'rft.au': ['Anthony H. Hull'], 
                'rft.btitle': ['Spanish and Russian Rivalry in the North Pacific '
                        'Regions of the New World'], 
                'rft.spage': ['112'], 
                'rft.epage': ['113']}, 
                cgi.parse_qs(coins.get('title')))

    def test_proceeding(self):
        citation = citations.Citation()
        citation.genre = 'proceeding'
        citation.au = ['Valery O. Shubin']
        citation.atitle = ['Russian Settlements in the Kuril Islands in the '
                        '18th and 19th centuries']
        citation.btitle = ['Russia in North America: Proceedings of the '
                        '2nd International Conference on Russian America']
        citation.place = ['Kingston and Fairbanks']
        citation.pub = ['Limestone Press']
        citation.date = ['1990']
        citation.spage = ['425']
        citation.epage = ['450']
        coins = citation.coins()
        self.assertEquals('span', coins.tag)
        self.assertEquals('Z3988', coins.get('class'))
        self.assertEquals({
                'ctx_ver': ['Z39.88-2004'], 
                'rft_val_format': ['info:ofi/fmt:kev:mtx:book'], 
                'rft.genre': ['proceeding'], 
                'rft.au': ['Valery O. Shubin'], 
                'rft.atitle': ['Russian Settlements in the Kuril Islands in the '
                        '18th and 19th centuries'], 
                'rft.btitle': ['Russia in North America: Proceedings of the '
                        '2nd International Conference on Russian America'], 
                'rft.place': ['Kingston and Fairbanks'], 
                'rft.pub': ['Limestone Press'], 
                'rft.date': ['1990'], 
                'rft.spage': ['425'], 
                'rft.epage': ['450']}, 
                cgi.parse_qs(coins.get('title')))

    def test_article(self):
        citation = citations.Citation()
        citation.genre = 'article'
        citation.au = [u'Ал. П. Соколов']
        citation.atitle = [(u'Приготовление кругосветной экспедиции '
                        u'1787 года, под начальством Муловского')]
        citation.jtitle = [(u'Записки Гидрографического Департамента '
                        u'Морского Министерства')]
        citation.volume = ['6']
        citation.date = ['1848']
        citation.spage = ['142']
        citation.epage = ['191']
        coins = citation.coins()
        self.assertEquals('span', coins.tag)
        self.assertEquals('Z3988', coins.get('class'))
        self.assertEquals({
                'ctx_ver': ['Z39.88-2004'], 
                'rft_val_format': ['info:ofi/fmt:kev:mtx:journal'], 
                'rft.genre': ['article'], 
                'rft.au': [u'Ал. П. Соколов'.encode('utf8')], 
                'rft.atitle': [(u'Приготовление кругосветной экспедиции '
                        u'1787 года, под начальством Муловского').encode('utf8')], 
                'rft.jtitle': [(u'Записки Гидрографического Департамента '
                        u'Морского Министерства').encode('utf8')], 
                'rft.volume': ['6'], 
                'rft.date': ['1848'], 
                'rft.spage': ['142'], 
                'rft.epage': ['191']}, 
                cgi.parse_qs(coins.get('title')))

class CitationAddToGraphTest(unittest.TestCase):

    def setUp(self):
        self.graph = rdfob.Graph()

    def test_doesnt_add_to_graph_when_already_exists(self):
        citation = citations.Citation()
        citation.genre = 'book'
        citation.btitle = 'Book'
        class MockFullGraph(object):
            def __contains__(_, x): return True
            def add(_, x): self.fail()
        citation.add_to_graph(MockFullGraph(), u'http://miskinhill.com.au/journals/test/1:1/article')

    def test_book(self):
        citation = citations.Citation.from_elem(x(u'''
                <span class="citation book"><span class="au">Charles Vinicombe Penrose</span>, 
                <em class="btitle">A Memoir of James Trevenen</em>, edited by 
                <span class="au">Christopher Lloyd</span> and 
                <span class="au">R.&nbsp;C. Anderson</span>, (<span class="place">London</span>: 
                <span class="pub">Navy Records Society</span>, <span class="date">1959</span>), 
                <span class="spage">90</span>–<span class="epage">91</span></span>
                '''))
        citation.add_to_graph(self.graph._g, u'http://miskinhill.com.au/journals/test/1:1/article')
        citation_node, = self.graph.by_type('mhs:Citation')
        book = citation_node['mhs:cites']
        self.assert_(rdfob.uriref('mhs:Book') in book.types)
        self.assertEquals(set(['Charles Vinicombe Penrose', 'Christopher Lloyd', 'R. C. Anderson']), 
                set(book.getall('dc:creator')))
        self.assertEquals('A Memoir of James Trevenen', book['dc:title'])
        self.assertEquals('Navy Records Society', book['dc:publisher'])
        self.assertEquals('1959', book['dc:date'])

    def test_bookitem(self):
        citation = citations.Citation.from_elem(x(u'''
                <span class="citation bookitem"><span class="au" title="Lydia 
                Black">Black</span><span class="atitle" title="“The Russians were 
                Coming…”" /><span class="au" title="Robin Inglis" /><span class="btitle" 
                title="Spain and the North Pacific Coast" /><span class="place" 
                title="Vancouver" /><span class="pub" title="Maritime Museum Society" 
                /><span class="date" title="1992" />, 
                <span class="spage">31</span>–<span class="epage">29</span></span>
                '''))
        citation.add_to_graph(self.graph._g, u'http://miskinhill.com.au/journals/test/1:1/article')
        citation_node, = self.graph.by_type('mhs:Citation')
        book = citation_node['mhs:cites']
        self.assert_(rdfob.uriref('mhs:Book') in book.types)
        self.assertEquals(set(['Lydia Black', 'Robin Inglis']), 
                set(book.getall('dc:creator')))
        self.assertEquals('Spain and the North Pacific Coast', book['dc:title'])
        self.assertEquals('Maritime Museum Society', book['dc:publisher'])
        self.assertEquals('1992', book['dc:date'])

    def test_proceeding(self):
        citation = citations.Citation.from_elem(x(u'''
                <span class="citation proceeding"><span class="au">Valery O. 
                Shubin</span>, ‘<span class="atitle">Russian Settlements in the 
                Kuril Islands in the 18th and 19th centuries</span>’, 
                <em class="btitle">Russia in North America: Proceedings of 
                the 2nd International Conference on Russian America</em> 
                (<span class="place">Kingston and Fairbanks</span>: 
                <span class="pub">Limestone Press</span>, 
                <span class="date">1990</span>), 
                <span class="spage">425</span>–<span class="epage">450</span></span>
                '''))
        citation.add_to_graph(self.graph._g, u'http://miskinhill.com.au/journals/test/1:1/article')
        citation_node, = self.graph.by_type('mhs:Citation')
        book = citation_node['mhs:cites']
        self.assert_(rdfob.uriref('mhs:Book') in book.types)
        self.assertEquals('Valery O. Shubin', book['dc:creator'])
        self.assertEquals('Russia in North America: Proceedings of the '
                        '2nd International Conference on Russian America', book['dc:title'])
        self.assertEquals('Limestone Press', book['dc:publisher'])
        self.assertEquals('1990', book['dc:date'])

if __name__ == '__main__':
    unittest.main()
