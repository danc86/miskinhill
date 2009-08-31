#!/usr/bin/env python

import sys
import os
import subprocess
from cStringIO import StringIO
import RDF
import lxml.etree, lxml.html
from itertools import chain

import rdfob
import citations

RDF_NS = RDF.NS('http://www.w3.org/1999/02/22-rdf-syntax-ns#')
RDFS_NS = RDF.NS('http://www.w3.org/2000/01/rdf-schema#')
OWL_NS = RDF.NS('http://www.w3.org/2002/07/owl#')
DCTERMS_NS = RDF.NS('http://purl.org/dc/terms/')
PRISM_NS = RDF.NS('http://prismstandard.org/namespaces/1.2/basic/')
FOAF_NS = RDF.NS('http://xmlns.com/foaf/0.1/')
MHS_NS = RDF.NS('http://miskinhill.com.au/rdfschema/1.0/')

def load():
    g = RDF.Model()
    for dirpath, dirnames, filenames in os.walk(os.getcwd()):
        for filename in filenames:
            if filename.endswith('.nt') or filename.endswith('.ttl'):
                print >>sys.stderr, 'Loading %s ...' % os.path.join(dirpath, filename),
                g.load('file:' + os.path.join(dirpath, filename))
                print >>sys.stderr, '%d triples' % len(g)
    return g

def extract_responsibility(g):
    print >>sys.stderr, 'Extracting responsible roles ...',
    for stmt in chain(g.find_statements(RDF.Statement(subject=None, predicate=MHS_NS.responsibility, object=None)),
            g.find_statements(RDF.Statement(subject=None, predicate=DCTERMS_NS.title, object=None))):
        responsibility = lxml.html.fromstring(stmt.object.literal_value['string'])
        for anchor in responsibility.findall('a'):
            href = RDF.Uri(anchor.get('href'))
            if MHS_NS.Author in _types(g, href):
                rels = anchor.get('rel', '').split()
                if 'contributor' in rels:
                    predicate = DCTERMS_NS.contributor
                elif 'translator' in rels:
                    predicate = MHS_NS.translator
                else:
                    predicate = DCTERMS_NS.creator
                g.append(RDF.Statement(stmt.subject, predicate=predicate, object=href))
    print >>sys.stderr, '%d triples' % len(g)

def extract_citations(g):
    print >>sys.stderr, 'Extracting citation metadata ...',
    for item in chain(g.get_sources(RDF_NS.type, MHS_NS.Article), g.get_sources(RDF_NS.type, MHS_NS.Review)):
        if unicode(item.uri).startswith('http://miskinhill.com.au/journals/'):
            content = unicode(item.uri)[25:] + '.html'
            if os.path.exists(content):
                for citation in citations.citations_from_content(content, item.uri):
                    citation.add_to_graph(g)
    print >>sys.stderr, '%d triples' % len(g)

def _types(g, s):
    return g.get_targets(source=s, predicate=RDF_NS.type)

_memo = {}
def transitive_objects(g, subject, predicate):
    if (g, subject, predicate) in _memo:
        return _memo[(g, subject, predicate)]
    os = frozenset(g.get_targets(source=subject, predicate=predicate))
    retval = reduce(frozenset.union, [os] + [transitive_objects(g, o, predicate) for o in os])
    _memo[(g, subject, predicate)] = retval
    return retval

def infer(g):
    print >>sys.stderr, 'Inferring superclasses/superproperties/inverses ...',
    for s in frozenset(stmt.subject for stmt in g):
        for t in list(_types(g, s)):
            for ct in transitive_objects(g, t, RDFS_NS.subClassOf):
                g.append(RDF.Statement(s, RDF_NS.type, ct))
    for stmt in g:
        for cp in transitive_objects(g, stmt.predicate, RDFS_NS.subPropertyOf):
            g.append(RDF.Statement(stmt.subject, cp, stmt.object))
    for stmt in g.find_statements(RDF.Statement(None, OWL_NS.inverseOf, None)):
        p1, p2 = stmt.subject, stmt.object
        for substmt in g.find_statements(RDF.Statement(None, p1, None)):
            g.append(RDF.Statement(substmt.object, p2, substmt.subject))
        for substmt in g.find_statements(RDF.Statement(None, p2, None)):
            g.append(RDF.Statement(substmt.object, p1, substmt.subject))
    print >>sys.stderr, '%d triples' % len(g)

DOMAIN_OBJECT_EXCEPTIONS = frozenset([RDFS_NS.Resource, OWL_NS.Thing])
RANGE_PROPERTY_EXCEPTIONS = frozenset([DCTERMS_NS.publisher, DCTERMS_NS.identifier, DCTERMS_NS.coverage, PRISM_NS.publicationDate])
RANGE_OBJECT_EXCEPTIONS = frozenset([RDF.Node(uri_string='http://www.w3.org/TR/2000/CR-rdf-schema-20000327#Literal'), FOAF_NS.Document, OWL_NS.Thing])

def validate(g):
    print >>sys.stderr, 'Validating domains/ranges ...',
    for domain_constraint in RDF.Query('SELECT ?s ?o WHERE (?s <http://www.w3.org/2000/01/rdf-schema#domain> ?o)').execute(g):
        if not domain_constraint['s'].is_resource() or not domain_constraint['o'].is_resource():
            continue # for now
        if domain_constraint['o'] in DOMAIN_OBJECT_EXCEPTIONS:
            continue
        for x in RDF.Query('SELECT ?s WHERE (?s <%s> ?o)' % domain_constraint['s'].uri).execute(g):
            if domain_constraint['o'] not in _types(g, x['s']):
                raise ValueError('property %s on %s violates rdfs:domain constraint of %s (found %r)' % (domain_constraint['s'], x['s'], domain_constraint['o'], [str(x) for x in _types(g, x['s'])]))
    for range_constraint in RDF.Query('SELECT ?s ?o WHERE (?s <http://www.w3.org/2000/01/rdf-schema#range> ?o)').execute(g):
        if not range_constraint['s'].is_resource() or not range_constraint['o'].is_resource():
            continue # for now
        if range_constraint['s'] in RANGE_PROPERTY_EXCEPTIONS or range_constraint['o'] in RANGE_OBJECT_EXCEPTIONS:
            continue
        for x in RDF.Query('SELECT ?o WHERE (?s <%s> ?o)' % range_constraint['s'].uri).execute(g):
            if range_constraint['o'] in (RDF_NS.Literal, RDFS_NS.Literal):
                if not x['o'].is_literal():
                    raise ValueError('property %s to %s violates rdfs:range constraint of %s (not is_literal())' % (range_constraint['s'], x['o'], range_constraint['o']))
            else:
                if range_constraint['o'] not in _types(g, x['o']):
                    raise ValueError('property %s to %s violates rdfs:range constraint of %s (found %r)' % (range_constraint['s'], x['o'], range_constraint['o'], [str(x) for x in _types(g, x['o'])]))
    print >>sys.stderr, 'done'

def serialize(g):
    print >>sys.stderr, 'Serializing ...',
    ser = RDF.Serializer(name='rdfxml-abbrev')
    for prefix, namespace in rdfob.NAMESPACES.iteritems():
        ser.set_namespace(prefix, namespace[''].uri)
    s = ser.serialize_model_to_string(g)
    print >>sys.stderr, 'done'
    return s

def main():
    g = load()
    extract_responsibility(g)
    extract_citations(g)
    infer(g)
    validate(g)
    sys.stdout.write(serialize(g))

if __name__ == '__main__':
    main()
