#!/usr/bin/env python
# vim:fileencoding=utf-8

import unittest
import cgi
import lxml.html

import viewutils

def x(s):
    return lxml.html.fragment_fromstring(s)

class NormalizeWhitespaceTest(unittest.TestCase):

    def test_nbsp(self):
        self.assertEquals(u'a b', viewutils.normalize_whitespace(u'a\u00a0b'))

    def test_newline(self):
        self.assertEquals(u'a b', viewutils.normalize_whitespace(u'a \n b'))

    def test_edges(self):
        self.assertEquals(u' a b ', viewutils.normalize_whitespace(u'\ta \n\tb '))

class HasClassTest(unittest.TestCase):

    def test_absent(self):
        self.assertEquals(False, viewutils.has_class(x('<span>asdf</span>'), 'class1'))

    def test_empty(self):
        self.assertEquals(False, viewutils.has_class(x('<span class="">asdf</span>'), 'class1'))

    def test_single(self):
        self.assertEquals(False, viewutils.has_class(x('<span class="class2">asdf</span>'), 'class1'))
        self.assertEquals(True, viewutils.has_class(x('<span class="class1">asdf</span>'), 'class1'))

    def test_multiple(self):
        self.assertEquals(False, viewutils.has_class(x('<span class="class2 class3">asdf</span>'), 'class1'))
        self.assertEquals(True, viewutils.has_class(x('<span class="class1 class2">asdf</span>'), 'class1'))

class AddCoinsToCitationTest(unittest.TestCase):

    def test_book(self):
        citation = x(u'''
                <span class="citation book"><span class="au">Charles Vinicombe Penrose</span>, 
                <em class="btitle">A Memoir of James Trevenen</em>, edited by 
                <span class="au">Christopher Lloyd</span> and 
                <span class="au">R.&nbsp;C. Anderson</span>, (<span class="place">London</span>: 
                <span class="pub">Navy Records Society</span>, <span class="date">1959</span>), 
                <span class="spage">90</span>–<span class="epage">91</span></span>
                ''')
        viewutils.add_coins_to_citation(citation)
        coins, = citation.find_class('Z3988')
        self.assertEquals('span', coins.tag)
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
        citation = x(u'''
                <span class="citation bookitem"><span class="au" title="Lydia 
                Black">Black</span><span class="atitle" title="“The Russians were 
                Coming…”" /><span class="au" title="Robin Inglis" /><span class="btitle" 
                title="Spain and the North Pacific Coast" /><span class="place" 
                title="Vancouver" /><span class="pub" title="Maritime Museum Society" 
                /><span class="date" title="1992" />, 
                <span class="spage">31</span>–<span class="epage">29</span></span>
                ''')
        viewutils.add_coins_to_citation(citation)
        coins, = citation.find_class('Z3988')
        self.assertEquals('span', coins.tag)
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
                'rft.epage': ['29']}, # wtf?
                cgi.parse_qs(coins.get('title')))

    def test_thesis(self):
        citation = x(u'''
                <span class="citation thesis"><span class="au">Anthony H. 
                Hull</span>, <em class="btitle">Spanish and Russian Rivalry 
                in the North Pacific Regions of the New World</em>, University of 
                Alabama PhD thesis, UMI microfilm, 
                <span class="spage">112</span>–<span class="epage">113</span></span>
                ''')
        viewutils.add_coins_to_citation(citation)
        coins, = citation.find_class('Z3988')
        self.assertEquals('span', coins.tag)
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
        citation = x(u'''
                <span class="citation proceeding"><span class="au">Valery O. 
                Shubin</span>, ‘<span class="atitle">Russian Settlements in the 
                Kuril Islands in the 18th and 19th centuries</span>’, 
                <em class="btitle">Russia in North America: Proceedings of 
                the 2nd International Conference on Russian America</em> 
                (<span class="place">Kingston and Fairbanks</span>: 
                <span class="pub">Limestone Press</span>, 
                <span class="date">1990</span>), 
                <span class="spage">425</span>–<span class="epage">450</span></span>
                ''')
        viewutils.add_coins_to_citation(citation)
        coins, = citation.find_class('Z3988')
        self.assertEquals('span', coins.tag)
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
        citation = x(u'''
                <span class="citation article"><span class="au" lang="ru">Ал.&nbsp;П. 
                Соколов</span>, «<span class="atitle" lang="ru">Приготовление 
                кругосветной экспедиции 1787 года, под начальством Муловского</span>», 
                <em class="jtitle" lang="ru">Записки Гидрографического Департамента 
                Морского Министерства</em>, 
                <span lang="ru">часть&nbsp;<span class="volume" title="6">VI</span></span>, 
                <span class="date">1848</span>&nbsp;г., 
                <span class="spage">142</span>–<span class="epage">191</span></span>
                ''')
        viewutils.add_coins_to_citation(citation)
        coins, = citation.find_class('Z3988')
        self.assertEquals('span', coins.tag)
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

class RelativeURLTest(unittest.TestCase):

    def test_uri(self):
        self.assertEquals('/asdf/test', viewutils.relative_url('http://miskinhill.com.au/asdf/test'))

    def test_unrelated_uri(self):
        self.assertEquals('http://example.com/asdf', viewutils.relative_url('http://example.com/asdf'))

if __name__ == '__main__':
    unittest.main()
