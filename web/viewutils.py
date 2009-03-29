
import os, re, urllib
from genshi import XML
import lxml.html
from lxml.html import builder as E

WHITESPACE_PATT = re.compile(r'\s+', re.UNICODE)
def normalize_whitespace(s):
    return WHITESPACE_PATT.sub(u' ', s)

def has_class(elem, cls):
    return u' %s ' % cls in u' %s ' % normalize_whitespace(unicode(elem.get('class', u'')))

def title_or_text(elem):
    return unicode(elem.get('title', elem.text_content()))

OPENURL_FIELDS = 'atitle jtitle btitle date volume issue spage epage issn isbn au place pub edition'.split()
def add_coins_to_citation(citation):
    values = {'ctx_ver': ['Z39.88-2004']}
    for field in OPENURL_FIELDS:
        values['rft.' + field] = [normalize_whitespace(title_or_text(e)) 
                for e in citation.find_class(field)]
    if has_class(citation, 'book'):
        values['rft_val_format'] = ['info:ofi/fmt:kev:mtx:book']
        values['rft.genre'] = ['book']
    if has_class(citation, 'bookitem'):
        values['rft_val_format'] = ['info:ofi/fmt:kev:mtx:book']
        values['rft.genre'] = ['bookitem']
    if has_class(citation, 'thesis'):
        values['rft_val_format'] = ['info:ofi/fmt:kev:mtx:book']
        values['rft.genre'] = ['document']
    if has_class(citation, 'proceeding'):
        values['rft_val_format'] = ['info:ofi/fmt:kev:mtx:book']
        values['rft.genre'] = ['proceeding']
    if has_class(citation, 'article'):
        values['rft_val_format'] = ['info:ofi/fmt:kev:mtx:journal']
        values['rft.genre'] = ['article']
    co = urllib.urlencode([(k, v.encode('utf8')) for (k, vs) in values.iteritems() for v in vs])
    citation.append(E.SPAN(E.CLASS('Z3988'), title=co))

def parsed_content(filename):
    original_content = open(filename, 'rb').read().decode('utf8')
    root = lxml.html.fragment_fromstring(original_content)
    for citation in root.find_class('citation'):
        add_coins_to_citation(citation)
    return XML(lxml.etree.tostring(root, encoding=unicode))

def relative_url(uri):
    if uri.startswith('http://miskinhill.com.au'):
        return uri[24:]
    return uri
