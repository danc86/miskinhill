
from genshi import XML
import lxml.html
from lxml.html import builder as E

import citations

def parsed_content(filename, article_uri):
    original_content = open(filename, 'rb').read().decode('utf8')
    root = lxml.html.fragment_fromstring(original_content)
    for elem in root.find_class('citation'):
        citation = citations.Citation.from_elem(elem)
        elem.getchildren()[-1].tail = (elem.getchildren()[-1].tail or u'') + u' '
        elem.append(citation.coins())
        elem.append(E.A(E.IMG(src='/images/silk/world_link.png', alt='[Citation details]'), 
                E.CLASS('citation-link'), 
                href='%s/citations/%s' % (relative_url(article_uri), citation.id())))
    return XML(lxml.etree.tostring(root, encoding=unicode))

def relative_url(uri):
    if uri.startswith('http://miskinhill.com.au'):
        return uri[24:]
    return uri
