#!/usr/bin/env python

import unittest
import lxml.html

import viewutils

def x(s):
    return lxml.html.fragment_fromstring(s)

class NormalizeWhitespaceTest(unittest.TestCase):

    def test_nbsp(self):
        self.assertEquals(u'a b', viewutils.normalize_whitespace(u'a\u00a0b'))

    def test_newline(self):
        self.assertEquals(u'a b', viewutils.normalize_whitespace(u'a \n b'))

    def test_edges(self):
        self.assertEquals(u' a b ', viewutils.normalize_whitespace(u'\ta \n\tb '))

class HasClassTest(unittest.TestCase):

    def test_absent(self):
        self.assertEquals(False, viewutils.has_class(x('<span>asdf</span>'), 'class1'))

    def test_empty(self):
        self.assertEquals(False, viewutils.has_class(x('<span class="">asdf</span>'), 'class1'))

    def test_single(self):
        self.assertEquals(False, viewutils.has_class(x('<span class="class2">asdf</span>'), 'class1'))
        self.assertEquals(True, viewutils.has_class(x('<span class="class1">asdf</span>'), 'class1'))

    def test_multiple(self):
        self.assertEquals(False, viewutils.has_class(x('<span class="class2 class3">asdf</span>'), 'class1'))
        self.assertEquals(True, viewutils.has_class(x('<span class="class1 class2">asdf</span>'), 'class1'))

class AddCoinsToCitationTest(unittest.TestCase):

    pass # XXX write these

if __name__ == '__main__':
    unittest.main()
