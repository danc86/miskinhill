
import os
import re

from webob import Response
from genshi import Markup, XML
from genshi.template import TemplateLoader, NewTextTemplate

import rdfob

template_loader = TemplateLoader(
        os.path.join(os.path.dirname(__file__), 'templates'), 
        variable_lookup='strict', 
        auto_reload=True)

class Representation(object):

    def __init__(self, req, node):
        self.req = req
        self.node = node

    def anchor(self):
        if self.node.uri.startswith('http://miskinhill.com.au/'):
            href = self.node.uri[24:]
        else:
            href = self.node.uri
        return Markup(u'<a href="%s.%s">%s</a>' % (href, self.format, self.label))

    def link(self):
        if self.node.uri.startswith('http://miskinhill.com.au/'):
            href = self.node.uri[24:]
        else:
            href = self.node.uri
        return XML(u'<link rel="alternate" type="%s" title="%s" href="%s.%s" />'
                % (self.content_type, self.label, href, self.format))

    @classmethod
    def can_represent(cls, node):
        return bool(cls.rdf_types.intersection(node.types))

class NTriplesRepresentation(Representation):

    format = 'nt'
    label = 'NTriples'
    content_type = 'application/x-turtle' # XXX should we actually serve turtle then?
    docs = 'http://www.w3.org/TR/REC-rdf-syntax/'

    @classmethod
    def can_represent(cls, node):
        return True

    def generate(self):
        return self.node.graph.serialized(self.node.uri)

    def response(self):
        return Response(self.generate(), 
                content_type=self.content_type)

class RDFXMLRepresentation(Representation):

    format = 'xml'
    label = 'RDF/XML'
    content_type = 'application/rdf+xml'
    docs = 'http://www.w3.org/TR/REC-rdf-syntax/'

    @classmethod
    def can_represent(cls, node):
        return True

    def generate(self):
        return self.node.graph.serialized(self.node.uri, format='xml')

    def response(self):
        return Response(self.generate(), 
                content_type=self.content_type)

class HTMLRepresentation(Representation):

    format = 'html'
    label = 'HTML'
    content_type = 'text/html'
    rdf_types = frozenset([rdfob.uriref('sioc:Forum'), 
                           rdfob.uriref('mhs:Author'), 
                           rdfob.uriref('mhs:Article'), 
                           rdfob.uriref('mhs:Book'), 
                           rdfob.uriref('mhs:Obituary'), 
                           rdfob.uriref('mhs:Review'), 
                           rdfob.uriref('mhs:Issue'), 
                           rdfob.uriref('mhs:Journal'), 
                           rdfob.uriref('rdfs:Class'), 
                           rdfob.uriref('rdf:Property')])
    docs = 'http://www.w3.org/TR/xhtml1/'

    _cited_types = frozenset([rdfob.uriref('mhs:Article'), rdfob.uriref('mhs:Book')])
    @classmethod
    def can_represent(cls, node):
        if node.uri.startswith('http://miskinhill.com.au/cited/') and not cls._cited_types.intersection(node.types):
            return False
        return super(HTMLRepresentation, cls).can_represent(node)

    def generate(self):
        for rdf_type in self.rdf_types:
            if rdf_type in self.node.types:
                # XXX dodgy?
                template_filename = re.match(r'.*?([A-Za-z]*)$', rdf_type).group(1).lower() + '.xml'
                break
        if template_filename == 'article.xml' and self.node.uri.startswith('http://miskinhill.com.au/cited/'):
            template_filename = 'cited_article.xml' # XXX dodgier?
        template = template_loader.load(os.path.join('html', template_filename))
        return template.generate(req=self.req, node=self.node)

    def response(self):
        return Response(self.generate().render('xhtml', doctype='xhtml'), 
                content_type=self.content_type)

class MODSRepresentation(Representation):

    format = 'mods'
    label = 'MODS'
    content_type = 'application/mods+xml'
    rdf_types = frozenset([rdfob.uriref('mhs:Journal'), rdfob.uriref('mhs:Article')])
    docs = 'http://www.loc.gov/standards/mods/mods-userguide.html'

    @classmethod
    def can_represent(cls, node):
        if not node.uri.startswith('http://miskinhill.com.au/journals/'):
            return False
        return super(MODSRepresentation, cls).can_represent(node)

    def generate(self):
        if rdfob.uriref('mhs:Journal') in self.node.types:
            template_filename = 'journal.xml'
        elif rdfob.uriref('mhs:Article') in self.node.types:
            template_filename = 'article.xml'
        template = template_loader.load(os.path.join('mods', template_filename))
        return template.generate(req=self.req, node=self.node)

    def response(self):
        return Response(self.generate().render('xml'), 
                content_type=self.content_type, 
                content_disposition='inline')

class MARCXMLRepresentation(Representation):

    format = 'marcxml'
    label = 'MARCXML'
    content_type = 'application/marcxml+xml'
    rdf_types = frozenset([rdfob.uriref('mhs:Journal')])
    docs = 'http://www.loc.gov/standards/marcxml/'

    @classmethod
    def can_represent(cls, node):
        if not node.uri.startswith('http://miskinhill.com.au/journals/'):
            return False
        return super(MARCXMLRepresentation, cls).can_represent(node)

    def generate(self):
        template = template_loader.load(os.path.join('marcxml', 'journal.xml'))
        return template.generate(req=self.req, node=self.node)

    def response(self):
        return Response(self.generate().render('xml'), 
                content_type=self.content_type, 
                content_disposition='inline')

class BibTeXRepresentation(Representation):

    format = 'bib'
    label = 'BibTeX'
    content_type = 'text/x-bibtex'
    rdf_types = frozenset([rdfob.uriref('mhs:Article')])
    docs = 'http://en.wikipedia.org/wiki/BibTeX'

    def generate(self):
        template = template_loader.load(os.path.join('bibtex', 'article.txt'), 
                cls=NewTextTemplate)
        return template.generate(req=self.req, node=self.node)

    def response(self):
        return Response(self.generate().render(), content_type=self.content_type)
        
class EndnoteRepresentation(Representation):

    format = 'end'
    label = 'Endnote'
    content_type = 'application/x-endnote-refer'
    rdf_types = frozenset([rdfob.uriref('mhs:Article')])
    docs = 'http://www.harzing.com/pophelp/exporting.htm'

    def generate(self):
        template = template_loader.load(os.path.join('end', 'article.txt'), 
                cls=NewTextTemplate)
        return template.generate(req=self.req, node=self.node)

    def response(self):
        return Response(self.generate().render(), content_type=self.content_type)

class AtomRepresentation(Representation):

    format = 'atom'
    label = 'Atom'
    content_type = 'application/atom+xml'
    rdf_types = frozenset([rdfob.uriref('sioc:Forum')])
    docs = 'http://www.ietf.org/rfc/rfc4287.txt'

    def generate(self):
        template = template_loader.load(os.path.join('atom', 'forum.xml'))
        return template.generate(req=self.req, node=self.node)

    def response(self):
        return Response(self.generate().render(), content_type=self.content_type)

# these are ordered by our preference, i.e. HTML first
ALL = [HTMLRepresentation, NTriplesRepresentation, RDFXMLRepresentation, MODSRepresentation, 
       MARCXMLRepresentation, BibTeXRepresentation, EndnoteRepresentation, AtomRepresentation]
BY_FORMAT = dict((r.format, r) for r in ALL)
BY_CONTENT_TYPE = dict((r.content_type, r) for r in ALL)

def for_node(node):
    return [r for r in ALL if r.can_represent(node)]
