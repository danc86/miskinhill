#!/usr/bin/env python
# vim:fileencoding=utf-8

import unittest

import viewutils

class RelativeURLTest(unittest.TestCase):

    def test_uri(self):
        self.assertEquals('/asdf/test', viewutils.relative_url('http://miskinhill.com.au/asdf/test'))

    def test_unrelated_uri(self):
        self.assertEquals('http://example.com/asdf', viewutils.relative_url('http://example.com/asdf'))

if __name__ == '__main__':
    unittest.main()
