
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
    rdfob.uriref('mhs:Author'): 'author'
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
    rdf_types = frozenset([rdfob.uriref('mhs:Author'), 
                           rdfob.uriref('mhs:Article'), 
                           rdfob.uriref('mhs:Review'), 
                           rdfob.uriref('mhs:Issue'), 
                           rdfob.uriref('mhs:Journal')])
    docs = 'http://www.w3.org/TR/REC-rdf-syntax/'

    def generate(self):
        return Response(self.node.graph.serialized(self.node.uri), 
                content_type=self.content_type)

class HTMLRepresentation(Representation):

    format = 'html'
    content_type = 'text/html'
    rdf_types = frozenset([rdfob.uriref('mhs:Author'), 
                           rdfob.uriref('mhs:Article'), 
                           rdfob.uriref('mhs:Review'), 
                           rdfob.uriref('mhs:Issue'), 
                           rdfob.uriref('mhs:Journal')])
    docs = 'http://www.w3.org/TR/xhtml1/'

    def generate(self):
        template = template_loader.load(os.path.join('html', template_for_type(self.node) + '.xml'))
        body = template.generate(req=self.req, node=self.node).render('xhtml', doctype='xhtml')
        return Response(body, content_type=self.content_type)

class MODSRepresentation(Representation):

    format = 'mods'
    content_type = 'application/mods+xml'
    rdf_types = frozenset([rdfob.uriref('mhs:Journal'), rdfob.uriref('mhs:Article')])
    docs = 'http://www.loc.gov/standards/mods/mods-userguide.html'

    def generate(self):
        template = template_loader.load(os.path.join('mods', template_for_type(self.node) + '.xml'))
        body = template.generate(req=self.req, node=self.node).render('xml')
        return Response(body, content_type=self.content_type, 
                headers={'Content-Disposition': 'inline'})

class MARCXMLRepresentation(Representation):

    format = 'marcxml'
    content_type = 'application/marcxml+xml'
    rdf_types = frozenset([rdfob.uriref('mhs:Journal')])
    docs = 'http://www.loc.gov/standards/marcxml/'

    def generate(self):
        template = template_loader.load(os.path.join('marcxml', template_for_type(self.node) + '.xml'))
        body = template.generate(req=self.req, node=self.node).render('xml')
        return Response(body, content_type=self.content_type, 
                headers={'Content-Disposition': 'inline'})

class BibTeXRepresentation(Representation):

    format = 'bib'
    content_type = 'text/x-bibtex'
    rdf_types = frozenset([rdfob.uriref('mhs:Article')])
    docs = 'http://en.wikipedia.org/wiki/BibTeX'

    def generate(self):
        template = template_loader.load(os.path.join('bibtex', template_for_type(self.node) + '.txt'), 
                cls=NewTextTemplate)
        body = template.generate(req=self.req, node=self.node).render()
        return Response(body, content_type=self.content_type)
        
class EndnoteRepresentation(Representation):

    format = 'end'
    content_type = 'application/x-endnote-refer'
    rdf_types = frozenset([rdfob.uriref('mhs:Article')])
    docs = 'http://www.harzing.com/pophelp/exporting.htm'

    def generate(self):
        template = template_loader.load(os.path.join('end', template_for_type(self.node) + '.txt'), 
                cls=NewTextTemplate)
        body = template.generate(req=self.req, node=self.node).render()
        return Response(body, content_type=self.content_type)

ALL = [NTriplesRepresentation, HTMLRepresentation, MODSRepresentation, 
       MARCXMLRepresentation, BibTeXRepresentation, EndnoteRepresentation]
BY_FORMAT = dict((r.format, r) for r in ALL)
