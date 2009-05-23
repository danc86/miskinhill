#!/usr/bin/env python

import sys
import rdflib
from rdflib.Graph import ConjunctiveGraph
import RDFSClosure

def main():
    store = rdflib.plugin.get('IOMemory', rdflib.store.Store)('rdfstore')
    store.open('')
    g = ConjunctiveGraph(store)
    g.parse(sys.stdin, format='nt')
    RDFSClosure.create_RDFSClosure(g)
    sys.stdout.write(g.serialize())

if __name__ == '__main__':
    main()
