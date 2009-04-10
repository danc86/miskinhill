
import os, re, urllib, hashlib, urlparse
import lxml.html
from genshi import Markup
from lxml.html import builder as E

import rdfob

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

    def __init__(self):
        for field in CITATION_FIELDS:
            setattr(self, field, [])

    @classmethod
    def from_elem(cls, elem):
        citation = cls()
        citation._elem = elem
        for field in CITATION_FIELDS:
            setattr(citation, field, [normalize_whitespace(title_or_text(e))
                    for e in elem.find_class(field)])
        for genre in CITATION_GENRES:
            if has_class(elem, genre):
                citation.genre = genre
        return citation

    @classmethod
    def from_markup(cls, markup):
        return cls.from_elem(lxml.html.fragment_fromstring(markup))

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

    def id(self):
        h = hashlib.sha1()
        for k, vs in self.__dict__.iteritems():
            if not k.startswith('_'):
                h.update(k)
                for v in vs:
                    h.update(v.encode('utf16'))
        return h.hexdigest()

    def add_to_graph(self, graph, article_uri):
        self_uri = rdfob.URIRef('%s/citations/%s' % (article_uri, self.id()))
        if (self_uri, None, None) in graph:
            return
        graph.add((self_uri, rdfob.RDF_TYPE, rdfob.uriref('mhs:Citation')))
        graph.add((self_uri, rdfob.uriref('dc:isPartOf'), article_uri))
        graph.add((self_uri, rdfob.uriref('mhs:citationMarkup'), 
                rdfob.Literal(lxml.etree.tostring(self._elem, encoding=unicode, with_tail=False), 
                    datatype=rdfob.uriref('rdf:XMLLiteral'))))
        for cites in self.cites:
            graph.add((self_uri, rdfob.uriref('mhs:cites'), 
                    rdfob.URIRef(urlparse.urljoin(u'http://private.miskinhill.com.au/cited/', cites))))

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

def citations_from_content(filename):
    original_content = open(filename, 'rb').read().decode('utf8')
    root = lxml.html.fragment_fromstring(original_content)
    return [Citation.from_elem(e) for e in root.find_class('citation')]
