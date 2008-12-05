#!/bin/sh

find content -name \*.ttl -exec cat {} \+ | rapper -i turtle -o ntriples - http://miskinhill.com.au/ >rdf.nt
