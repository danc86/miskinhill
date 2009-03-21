#!/usr/bin/env python

import sys, os
sys.path.append('lib')

import unittest
import xmlrunner
import figleaf, figleaf2clover
figleaf.start()

import test_app

loader = unittest.TestLoader()
suite = loader.loadTestsFromNames(['test_app'])
runner = xmlrunner.XMLTestRunner(open('test_results.xml', 'w'))
runner.run(suite)

figleaf.stop()
figleaf.write_coverage('figleaf.pickle', append=False)
figleaf2clover.convert(figleaf.get_info(), r'lib/|.*\.html|.*\.xml|.*\.txt|.*test_.*\.py', open('clover.xml', 'w'), 'web')
