#!/usr/bin/python

import os, cgi, subprocess, shutil, tempfile
from itertools import ifilter
from rdflib import RDF
from rdflib.graph import Graph
from rdflib.term import URIRef
from rdflib.namespace import Namespace
import rdflib.term
from genshi import XML, Markup, Stream

DC = Namespace('http://purl.org/dc/terms/')
FOAF = Namespace('http://xmlns.com/foaf/0.1/')
MHS = Namespace('http://miskinhill.com.au/rdfschema/1.0/')

graph = Graph()
graph.parse('meta.xml', format='xml')
rdflib.term.bind(RDF.XMLLiteral, XML)

def striptags(x):
    if isinstance(x, (Stream, Markup)):
        return Markup(x).striptags()
    else:
        return x

def entities(s):
    return cgi.escape(s).encode('ascii', 'xmlcharrefreplace')

def generate_pdf(source, output, start_page, end_page, title, author):
    f = tempfile.NamedTemporaryFile(prefix='mh_generate_pdf')
    subprocess.check_call(['pdftk', source, 'cat', 
            '%d-%d' % (start_page, end_page),
            'output', f.name])
    p = subprocess.Popen(['pdftk', f.name, 'update_info', '-', 'output', f.name + '.meta'], 
            stdin=subprocess.PIPE)
    p.stdin.write('InfoKey: Title\nInfoValue: %s\nInfoKey: Author\nInfoValue: %s' % 
            (title, author))
    p.stdin.close()
    retcode = p.wait()
    if retcode:
        raise StandardError(retcode)
    shutil.copy(f.name + '.meta', output)

def has_type(resource, type_):
    return (resource.identifier, RDF.type, type_) in resource.graph

def articles_from_issue(path, issue_filename):
    issue = graph.resource(URIRef(u'http://miskinhill.com.au/' + path))
    for article in ifilter(lambda r: has_type(r, MHS.Article), issue.subjects(DC.isPartOf)):
        assert article.identifier.startswith(issue.identifier), article.identifier
        generate_pdf(os.path.join(path, issue_filename),
                os.path.join(path, article.identifier.rsplit('/', 1)[1] + '.pdf'),
                article.value(MHS.startPage).toPython() + issue.value(MHS.frontMatterExtent).toPython(),
                article.value(MHS.endPage).toPython() + issue.value(MHS.frontMatterExtent).toPython(),
                entities(striptags(article.value(DC.title).toPython())),
                entities('; '.join(c.value(FOAF.name).toPython() for c in article.objects(DC.creator))))
    for review in ifilter(lambda r: has_type(r, MHS.Review), issue.subjects(DC.isPartOf)):
        assert review.identifier.startswith(issue.identifier + 'reviews/'), review.identifier
        generate_pdf(os.path.join(path, issue_filename),
                os.path.join(path, 'reviews', review.identifier.rsplit('/', 1)[1] + '.pdf'),
                review.value(MHS.startPage).toPython() + issue.value(MHS.frontMatterExtent).toPython(),
                review.value(MHS.endPage).toPython() + issue.value(MHS.frontMatterExtent).toPython(),
                entities('Review of ' + ' and '.join(striptags(b.value(DC.title).toPython()) for b in review.objects(MHS.reviews))),
                entities('; '.join(c.value(FOAF.name).toPython() for c in review.objects(DC.creator))))
    for obituary in ifilter(lambda r: has_type(r, MHS.Obituary), issue.subjects(DC.isPartOf)):
        assert obituary.identifier.startswith(issue.identifier), obituary.identifier
        if obituary.value(DC.title):
            title = striptags(obituary.value(DC.title).toPython())
        else:
            title = 'In memoriam %s' % obituary.value(MHS.obituaryOf).value(FOAF.name).toPython()
        generate_pdf(os.path.join(path, issue_filename),
                os.path.join(path, obituary.identifier.rsplit('/', 1)[1] + '.pdf'),
                obituary.value(MHS.startPageInFrontMatter).toPython(),
                obituary.value(MHS.endPageInFrontMatter).toPython(),
                entities(title),
                entities('; '.join(c.value(FOAF.name).toPython() for c in obituary.objects(DC.creator))))

#articles_from_issue('journals/asees/18:1-2/', 'final/ASEES 2004.pdf')
#articles_from_issue('journals/asees/19:1-2/', 'final/ASEESVol 19finalversion05.pdf')
#articles_from_issue('journals/asees/20:1-2/', 'final/ASEES 2006final.pdf')
#articles_from_issue('journals/asees/21:1-2/', 'final/asees07.pdf')
#articles_from_issue('journals/asees/22:1-2/', 'final/asees08.pdf')
#articles_from_issue('journals/asees/23:1-2/', 'final/asees09.pdf')
#articles_from_issue('journals/asees/24:1-2/', 'final/asees10.pdf')
articles_from_issue('journals/asees/25:1-2/', 'final/asees11.pdf')
