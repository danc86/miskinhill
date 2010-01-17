#!/usr/bin/env python

import os, cgi, subprocess, shutil
import rdfob
from viewutils import striptags

graph = rdfob.Graph('meta.xml')

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
    issue = graph[rdfob.Uri('http://miskinhill.com.au/' + path)]
    for article in issue.reflexive('dc:isPartOf', type='mhs:Article'):
        assert unicode(article.uri).startswith(unicode(issue.uri)), unicode(article.uri)
        generate_pdf(os.path.join(path, issue_filename),
                os.path.join(path, unicode(article.uri).rsplit('/', 1)[1] + '.pdf'), 
                article['mhs:startPage'] + issue['mhs:frontMatterExtent'], 
                article['mhs:endPage'] + issue['mhs:frontMatterExtent'], 
                entities(striptags(article['dc:title'])), 
                entities('; '.join(c['foaf:name'] for c in article.getall('dc:creator'))))
    for review in issue.reflexive('dc:isPartOf', type='mhs:Review'):
        assert unicode(review.uri).startswith(unicode(issue.uri) + 'reviews/'), unicode(review.uri)
        generate_pdf(os.path.join(path, issue_filename),
                os.path.join(path, 'reviews', unicode(review.uri).rsplit('/', 1)[1] + '.pdf'), 
                review['mhs:startPage'] + issue['mhs:frontMatterExtent'], 
                review['mhs:endPage'] + issue['mhs:frontMatterExtent'], 
                entities('Review of ' + ' and '.join(striptags(b['dc:title']) for b in review.getall('mhs:reviews'))), 
                entities('; '.join(c['foaf:name'] for c in review.getall('dc:creator'))))
    for obituary in issue.reflexive('dc:isPartOf', type='mhs:Obituary'):
        assert unicode(obituary.uri).startswith(unicode(issue.uri)), unicode(obituary.uri)
        generate_pdf(os.path.join(path, issue_filename),
                os.path.join(path, unicode(obituary.uri).rsplit('/', 1)[1] + '.pdf'), 
                obituary['mhs:startPageInFrontMatter'],
                obituary['mhs:endPageInFrontMatter'], 
                entities(striptags(obituary['dc:title'])), 
                entities('; '.join(c['foaf:name'] for c in obituary.getall('dc:creator'))))

articles_from_issue('journals/asees/19:1-2/', 'final/ASEESVol 19finalversion05.pdf')
#articles_from_issue('journals/asees/20:1-2/', 'final/ASEES 2006final.pdf')
#articles_from_issue('journals/asees/21:1-2/', 'final/asees07.pdf')
#articles_from_issue('journals/asees/22:1-2/', 'final/asees08.pdf')
