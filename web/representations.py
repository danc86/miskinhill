
import os

from webob import Response
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

class NTriplesRepresentation(Representation):

    format = 'nt'
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
