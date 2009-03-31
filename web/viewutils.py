
from genshi import XML
import lxml.html

import citations

def parsed_content(filename):
    original_content = open(filename, 'rb').read().decode('utf8')
    root = lxml.html.fragment_fromstring(original_content)
    for elem in root.find_class('citation'):
        elem.append(Citation.from_elem(elem).coins())
    return XML(lxml.etree.tostring(root, encoding=unicode))

def relative_url(uri):
    if uri.startswith('http://miskinhill.com.au'):
        return uri[24:]
    return uri
