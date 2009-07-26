#!/usr/bin/env python

import sys
import os
import subprocess
from cStringIO import StringIO
import RDF

RDF_NS = RDF.NS('http://www.w3.org/1999/02/22-rdf-syntax-ns#')
RDFS_NS = RDF.NS('http://www.w3.org/2000/01/rdf-schema#')
OWL_NS = RDF.NS('http://www.w3.org/2002/07/owl#')

def load():
    g = RDF.Model()
    for dirpath, dirnames, filenames in os.walk(os.getcwd()):
        for filename in filenames:
            if filename.endswith('.nt') or filename.endswith('.ttl'):
                print >>sys.stderr, 'Loading %s ...' % os.path.join(dirpath, filename),
                g.load('file:' + os.path.join(dirpath, filename))
                print >>sys.stderr, '%d triples' % len(g)
    return g

def _types(g, s):
    return g.get_targets(source=s, predicate=RDF_NS.type)

def transitive_objects(g, subject, predicate):
    os = frozenset(g.get_targets(source=subject, predicate=predicate))
    return reduce(frozenset.union, [os] + [transitive_objects(g, o, predicate) for o in os])

def close(g):
    print >>sys.stderr, 'Computing closure ...'
    for s in frozenset(n.subject for n in g):
        for t in list(_types(g, s)):
            for ct in transitive_objects(g, t, RDFS_NS.subClassOf):
                g.append(RDF.Statement(s, RDF_NS.type, ct))

def validate(g):
    print >>sys.stderr, 'Validating ...'
    for domain_constraint in RDF.Query('SELECT ?s ?o WHERE (?s <http://www.w3.org/2000/01/rdf-schema#domain> ?o)').execute(g):
        if not domain_constraint['s'].is_resource() or not domain_constraint['o'].is_resource():
            continue # for now
        if domain_constraint['o'] == OWL_NS.Thing:
            continue # ugh
        for x in RDF.Query('SELECT ?s WHERE (?s <%s> ?o)' % domain_constraint['s'].uri).execute(g):
            if domain_constraint['o'] not in _types(g, x['s']):
                raise ValueError('property %s on %s violates rdfs:domain constraint of %s (found %r)' % (domain_constraint['s'], x['s'], domain_constraint['o'], [str(x) for x in _types(g, x['s'])]))
    for range_constraint in RDF.Query('SELECT ?s ?o WHERE (?s <http://www.w3.org/2000/01/rdf-schema#range> ?o)').execute(g):
        if not range_constraint['s'].is_resource() or not range_constraint['o'].is_resource():
            continue # for now
        if range_constraint['o'] == OWL_NS.Thing:
            continue # ugh
        for x in RDF.Query('SELECT ?o WHERE (?s <%s> ?o)' % range_constraint['s'].uri).execute(g):
            if range_constraint['o'] == RDFS_NS.Literal:
                if not x['o'].is_literal():
                    raise ValueError('property %s to %s violates rdfs:range constraint of %s (not is_literal())' % (range_constraint['s'], x['o'], range_constraint['o']))
            else:
                if range_constraint['o'] not in _types(g, x['o']):
                    raise ValueError('property %s to %s violates rdfs:range constraint of %s (found %r)' % (range_constraint['s'], x['o'], range_constraint['o'], [str(x) for x in _types(g, x['o'])]))

def main():
    g = load()
    close(g)
    validate(g)
    sys.stdout.write(g.to_string())

if __name__ == '__main__':
    main()
