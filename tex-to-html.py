#!/usr/bin/env python

import re

def tex2html(s):

    # nbsp
    s = re.sub(r'\~', '&nbsp;', s)

    # non-sentence spaces
    s = re.sub(r'\\ ', ' ', s)

    # emphasis
    s = re.compile(r'\\textit\{([^}]+)\}', re.M).sub(r'<em>\1</em>', s)

    # footnotes
    i = 0
    while True:
        i += 1
        s, n = re.compile(r'\\footnote\{([^}]+)\}', re.M).subn('<a href="#fn-%d" class="footnote-anchor">%d</a> <small id="fn-%d" class="footnote"><span class="footnote-number">%d</span> \\1</small>' % ((i,) * 4), s)
        if not n: break

    return s

if __name__ == '__main__':
    import sys
    sys.stdout.write(tex2html(open(sys.argv[1], 'r').read()))
