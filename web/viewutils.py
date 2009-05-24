
from genshi import XML, Markup
import lxml.html
from lxml.html import builder as E

import citations

def parsed_content(filename, article_uri):
    original_content = open(filename, 'rb').read().decode('utf8')
    root = lxml.html.fragment_fromstring(original_content)
    counter = 0
    for elem in root.find_class('citation'):
        counter += 1
        elem.set('id', 'citation-%d' % counter)
        citation = citations.Citation.from_elem(article_uri, counter, elem)
        elem.getchildren()[-1].tail = (elem.getchildren()[-1].tail or u'') + u' '
        elem.append(citation.coins())
        for uri in citation.cites_urirefs():
            elem.append(E.A(E.IMG(src='/images/silk/world_link.png', alt='[Citation details]'), 
                    E.CLASS('citation-link'), href=relative_url(uri.decode('utf8'))))
    return XML(lxml.etree.tostring(root, encoding=unicode))

def relative_url(uri):
    if uri.startswith('http://miskinhill.com.au'):
        return uri[24:]
    return uri

def linked_author(author_node):
    if isinstance(author_node, unicode):
        return author_node
    else:
        return Markup(u'<a href="%s">%s</a>' % (relative_url(author_node.uri), author_node['foaf:name']))
