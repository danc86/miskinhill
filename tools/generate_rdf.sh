#!/bin/sh

generate_rdfs_closure=$(dirname "$(readlink -n -f $0)")/generate_rdfs_closure.py

( find -name \*.ttl -exec cat {} \+ | rapper -i turtle -o ntriples - http://miskinhill.com.au/
  cat rdfschema/foaf.nt
  cat rdfschema/dcterms.nt ) \
| $generate_rdfs_closure >meta.xml
