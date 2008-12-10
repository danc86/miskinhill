#!/usr/bin/env python

import re

def tex2html(s):

    # nbsp
    s = re.sub(r'\~', '&nbsp;', s)

    # ampersands
    s = re.sub(r'\\&', '&amp;', s)

    # spaces
    s = re.sub(r'\\ ', ' ', s)
    s = re.sub(r'\\,', '&thinsp;', s)

    # emphasis
    s = re.compile(r'\\textit\{([^}]+)\}', re.M).sub(r'<em>\1</em>', s)

    # quotes
    s = re.sub(r'\\begin{quote}', '<q>', s)
    s = re.sub(r'\\end{quote}', '</q>', s)

    # footnotes
    s += '\n\n<h3>Footnotes</h3>'
    i = 0
    while True:
        i += 1
        m = re.compile(r'\\footnote\{([^}]+)\}', re.M).search(s)
        if not m: break
        s = s[:m.start()] + \
            ('<a href="#fn-%d" class="footnote-anchor">%d</a>' % (i, i)) + \
            s[m.end():] + \
            ('\n<div id="fn-%d" class="footnote"><p><span class="footnote-number">%d</span> ' % (i, i)) + \
            m.group(1) + \
            '</p></div>'

    # sections
    s = re.sub(r'\\subsection\{([^}]*)\}', r'<h3>\1</h3>', s)

    # containing element
    s = re.sub(r'\\begin\{article\}\{[^}]*\}\{[^}]*\}\{[^}]*\}', '', s)
    s = re.sub(r'\\end\{article\}', '', s)
    s = '<div xmlns="http://www.w3.org/1999/xhtml" class="article-body">' + s + '\n\n</div>'

    return s

if __name__ == '__main__':
    import sys
    sys.stdout.write(tex2html(open(sys.argv[1], 'r').read()))
