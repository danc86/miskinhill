
import os

from webob import Response
from genshi import Markup, XML
from genshi.template import TemplateLoader, NewTextTemplate

import rdfob

template_loader = TemplateLoader(
        os.path.join(os.path.dirname(__file__), 'templates'), 
        variable_lookup='strict', 
        auto_reload=True)

RDF_TEMPLATES = {
    rdfob.uriref('mhs:Journal'): 'journal', 
    rdfob.uriref('mhs:Issue'): 'issue', 
    rdfob.uriref('mhs:Article'): 'article',
    rdfob.uriref('mhs:Review'): 'review', 
    rdfob.uriref('mhs:Author'): 'author', 
    rdfob.uriref('mhs:Citation'): 'citation', 
    rdfob.uriref('sioc:Forum'): 'forum', 
    rdfob.uriref('rdfs:Class'): 'class', 
    rdfob.uriref('rdf:Property'): 'property', 
}
def template_for_type(node):
    for type in node.types:
        if type in RDF_TEMPLATES:
            return RDF_TEMPLATES[type]
    raise exc.HTTPNotFound('Matching template not found').exception

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

class NTriplesRepresentation(Representation):

    format = 'nt'
    label = 'NTriples'
    content_type = 'text/plain; charset=UTF-8'
    rdf_types = frozenset([rdfob.uriref('sioc:Forum'), 
                           rdfob.uriref('mhs:Citation'), 
                           rdfob.uriref('mhs:Author'), 
                           rdfob.uriref('mhs:Article'), 
                           rdfob.uriref('mhs:Review'), 
                           rdfob.uriref('mhs:Issue'), 
                           rdfob.uriref('mhs:Journal'), 
                           rdfob.uriref('rdfs:Class'), 
                           rdfob.uriref('rdf:Property')])
    docs = 'http://www.w3.org/TR/REC-rdf-syntax/'

    def generate(self):
        return self.node.graph.serialized(self.node.uri)

    def response(self):
        return Response(self.generate(), 
                content_type=self.content_type)

class RDFXMLRepresentation(Representation):

    format = 'xml'
    label = 'RDF/XML'
    content_type = 'application/rdf+xml'
    rdf_types = frozenset([rdfob.uriref('sioc:Forum'), 
                           rdfob.uriref('mhs:Citation'), 
                           rdfob.uriref('mhs:Author'), 
                           rdfob.uriref('mhs:Article'), 
                           rdfob.uriref('mhs:Review'), 
                           rdfob.uriref('mhs:Issue'), 
                           rdfob.uriref('mhs:Journal'), 
                           rdfob.uriref('rdfs:Class'), 
                           rdfob.uriref('rdf:Property')])
    docs = 'http://www.w3.org/TR/REC-rdf-syntax/'

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
                           rdfob.uriref('mhs:Citation'), 
                           rdfob.uriref('mhs:Author'), 
                           rdfob.uriref('mhs:Article'), 
                           rdfob.uriref('mhs:Review'), 
                           rdfob.uriref('mhs:Issue'), 
                           rdfob.uriref('mhs:Journal'), 
                           rdfob.uriref('rdfs:Class'), 
                           rdfob.uriref('rdf:Property')])
    docs = 'http://www.w3.org/TR/xhtml1/'

    def generate(self):
        template = template_loader.load(os.path.join('html', template_for_type(self.node) + '.xml'))
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

    def generate(self):
        template = template_loader.load(os.path.join('mods', template_for_type(self.node) + '.xml'))
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

    def generate(self):
        template = template_loader.load(os.path.join('marcxml', template_for_type(self.node) + '.xml'))
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
        template = template_loader.load(os.path.join('bibtex', template_for_type(self.node) + '.txt'), 
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
        template = template_loader.load(os.path.join('end', template_for_type(self.node) + '.txt'), 
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
        template = template_loader.load(os.path.join('atom', template_for_type(self.node) + '.xml'))
        return template.generate(req=self.req, node=self.node)

    def response(self):
        return Response(self.generate().render(), content_type=self.content_type)

ALL = [NTriplesRepresentation, RDFXMLRepresentation, HTMLRepresentation, MODSRepresentation, 
       MARCXMLRepresentation, BibTeXRepresentation, EndnoteRepresentation, AtomRepresentation]
BY_FORMAT = dict((r.format, r) for r in ALL)

def for_types(types):
    return [r for r in ALL if r.rdf_types.intersection(types)]
