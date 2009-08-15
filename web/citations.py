
import os, re, urllib, hashlib, urlparse
import lxml.html
from genshi import Markup
from lxml.html import builder as E

import rdfob
import RDF

WHITESPACE_PATT = re.compile(r'\s+', re.UNICODE)
def normalize_whitespace(s):
    return WHITESPACE_PATT.sub(u' ', s)

def has_class(elem, cls):
    return u' %s ' % cls in u' %s ' % normalize_whitespace(unicode(elem.get('class', u'')))

def title_or_text(elem):
    return unicode(elem.get('title', elem.text_content()))

OPENURL_FIELDS = 'atitle jtitle btitle date volume issue spage epage issn isbn au place pub edition'.split()
CITATION_FIELDS = OPENURL_FIELDS + ['cites']
CITATION_GENRES = 'book bookitem thesis proceeding article'.split()

class Citation(object):

    def __init__(self, article_uri, number):
        self.article_uri = article_uri
        self.number = number
        for field in CITATION_FIELDS:
            setattr(self, field, [])

    @classmethod
    def from_elem(cls, article_uri, number, elem):
        citation = cls(article_uri, number)
        for field in CITATION_FIELDS:
            setattr(citation, field, [normalize_whitespace(title_or_text(e))
                    for e in elem.find_class(field)])
        for genre in CITATION_GENRES:
            if has_class(elem, genre):
                citation.genre = genre
        return citation

    @classmethod
    def from_markup(cls, article_uri, number, markup):
        return cls.from_elem(article_uri, number, lxml.html.fragment_fromstring(markup))

    def __repr__(self):
        return '<Citation %s %r>' % (self.id(), self.__dict__)

    def coins(self, as_markup=False):
        values = {'ctx_ver': ['Z39.88-2004']}
        for field in OPENURL_FIELDS:
            values['rft.' + field] = getattr(self, field)
        values['rft.genre'] = [self.genre]
        if self.genre in ('book', 'bookitem', 'proceeding'):
            values['rft_val_format'] = ['info:ofi/fmt:kev:mtx:book']
        elif self.genre == 'thesis':
            values['rft_val_format'] = ['info:ofi/fmt:kev:mtx:book']
            values['rft.genre'] = ['document'] # override, since OpenURL has no thesis genre
        elif self.genre in ('article'):
            values['rft_val_format'] = ['info:ofi/fmt:kev:mtx:journal']
        co = urllib.urlencode([(k, v.encode('utf8')) for (k, vs) in values.iteritems() for v in vs])
        elem = E.SPAN(E.CLASS('Z3988'), title=co)
        if as_markup:
            return Markup(lxml.etree.tostring(elem, encoding=unicode))
        else:
            return elem

    def add_to_graph(self, graph):
        self_uri = rdfob.Uri('%s#citation-%d' % (self.article_uri, self.number))
        graph.append(RDF.Statement(self_uri, rdfob.RDF_TYPE, rdfob.uriref('mhs:Citation')))
        graph.append(RDF.Statement(self_uri, rdfob.uriref('dc:isPartOf'), rdfob.Uri(self.article_uri)))
        for cites in self.cites_urirefs():
            graph.append(RDF.Statement(self_uri, rdfob.uriref('mhs:cites'), rdfob.Uri(cites.decode('utf8'))))

    def cites_urirefs(self):
        return [urlparse.urljoin('http://miskinhill.com.au/cited/', cites.encode('utf8'))
                for cites in self.cites]

    def page_ranges(self):
        starts = sorted(int(x) for x in self.spage)
        ends = sorted(int(x) for x in self.epage)
        parts = []
        while starts:
            s = starts.pop()
            if (starts and ends and ends[0] <= starts[0]) or ends:
                parts.append(u'%d\u2013%d' % (s, ends.pop()))
            else:
                parts.append(u'%d\u2013' % s)
        return u', '.join(parts)

def citations_from_content(filename, article_uri):
    original_content = open(filename, 'rb').read().decode('utf8')
    root = lxml.html.fragment_fromstring(original_content)
    return [Citation.from_elem(article_uri, n + 1, e) for n, e in enumerate(root.find_class('citation'))]
