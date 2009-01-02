#!/bin/sh

find -name \*.ttl -exec cat {} \+ | rapper -i turtle -o ntriples - http://miskinhill.com.au/ >meta.nt
