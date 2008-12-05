#!/usr/bin/env python

import os, cgi, subprocess, shutil
import genshi
import rdfob

graph = rdfob.Graph('rdf.nt')

def strip_html(s):
    return genshi.Markup(s).striptags()

def entities(s):
    return cgi.escape(s).encode('ascii', 'xmlcharrefreplace')

def generate_pdf(source, output, start_page, end_page, title, author):
    subprocess.check_call(['pdftk', source, 'cat', 
            '%d-%d' % (start_page, end_page),
            'output', output])
    p = subprocess.Popen(['pdftk', output, 'update_info', '-', 'output', output + '.meta'], 
            stdin=subprocess.PIPE)
    p.stdin.write('InfoKey: Title\nInfoValue: %s\nInfoKey: Author\nInfoValue: %s' % 
            (title, author))
    p.stdin.close()
    retcode = p.wait()
    if retcode:
        raise StandardError(retcode)
    shutil.move(output + '.meta', output)

def articles_from_issue(path, issue_filename):
    issue = graph[rdfob.URIRef('http://miskinhill.com.au/' + path)]
    for article in issue.reflexive('dc:isPartOf', type='mhs:Article'):
        assert article.uri.startswith(issue.uri), article.uri
        generate_pdf(os.path.join(path, issue_filename),
                os.path.join(path, article.uri.rsplit('/', 1)[1] + '.pdf'), 
                article['mhs:startPage'] + issue['mhs:frontMatterExtent'], 
                article['mhs:endPage'] + issue['mhs:frontMatterExtent'], 
                entities(strip_html(article['dc:title'])), 
                entities(strip_html(article['dc:creator']['foaf:name'])))
    for review in issue.reflexive('dc:isPartOf', type='mhs:Review'):
        assert review.uri.startswith(issue.uri + 'reviews/'), review.uri
        generate_pdf(os.path.join(path, issue_filename),
                os.path.join(path, 'reviews', review.uri.rsplit('/', 1)[1] + '.pdf'), 
                review['mhs:startPage'] + issue['mhs:frontMatterExtent'], 
                review['mhs:endPage'] + issue['mhs:frontMatterExtent'], 
                entities(strip_html('Review of ' + review['mhs:reviews']['dc:title'])), 
                entities(strip_html(review['dc:creator']['foaf:name'])))

os.chdir('content')
articles_from_issue('journals/asees/21:1-2/', 'final/asees07.pdf')
