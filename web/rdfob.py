
import datetime, re
import rdflib
from rdflib import URIRef, Namespace, BNode, Literal
from rdflib.Graph import ConjunctiveGraph
import RDFSClosure
import genshi
import iso8601

NAMESPACES = {
    'mhs': Namespace('http://miskinhill.com.au/rdfschema/1.0/'), 
    'dc': Namespace('http://purl.org/dc/terms/'), 
    'foaf': Namespace('http://xmlns.com/foaf/0.1/'), 
    'rdf': Namespace('http://www.w3.org/1999/02/22-rdf-syntax-ns#'), 
    'rdfs': Namespace('http://www.w3.org/2000/01/rdf-schema#'), 
    'xsd': Namespace('http://www.w3.org/TR/xmlschema-2/#'), 
    'contact': Namespace('http://www.w3.org/2000/10/swap/pim/contact#'), 
    'geonames': Namespace('http://www.geonames.org/ontology#'), 
    'sioc': Namespace('http://rdfs.org/sioc/ns#'), 
    'awol': Namespace('http://bblfish.net/work/atom-owl/2006-06-06/#')
}
RDF_TYPE = NAMESPACES['rdf']['type']
RDF_SEQ = NAMESPACES['rdf']['Seq']

def uriref(qname):
    if isinstance(qname, (URIRef, BNode, Literal)): return qname
    ns, name = qname.split(':', 1)
    return NAMESPACES[ns][name]

class UniquenessError(ValueError): pass

class Graph(object):

    def __init__(self, *imports):
        self.store = rdflib.plugin.get('IOMemory', rdflib.store.Store)('rdfstore')
        self.store.open('')
        self._g = ConjunctiveGraph(self.store)
        for prefix, namespace in NAMESPACES.iteritems():
            self._g.bind(prefix, namespace)
        for filename in imports:
            self._g.parse(filename, format='nt')
        RDFSClosure.create_RDFSClosure(self._g)

    def __getitem__(self, subject):
        subject = uriref(subject)
        pos = list(self._g.predicate_objects(subject))
        if not pos:
            raise KeyError(subject)
        return GraphNode(subject, self, pos)

    def __contains__(self, subject):
        subject = uriref(subject)
        return bool(list(self._g.predicate_objects(subject))) # XXX avoid list()?

    def by_type(self, type):
        type = uriref(type)
        return [self[s] for s in self._g.subjects(RDF_TYPE, type)]

    def subjects(self):
        """ Returns an iterable across all subjects in the graph. """
        for s in self._g.subjects(RDF_TYPE, None):
            yield self[s]

    def serialized(self, subject, format='nt'):
        """ Returns serialized triples about the given graph node (triples 
        where it appears as subject, and the transitive closure with any blank 
        nodes encountered). """
        # XXX make serialized output order predictable/consistent!
        subject = uriref(subject)
        subgraph = ConjunctiveGraph()
        # copy namespace prefixes from _g
        for prefix, namespace in self._g.namespaces():
            subgraph.bind(prefix, namespace)
        # recursively add interesting triples
        visited = set()
        def add_triples(subject):
            if subject in visited: return
            visited.add(subject)
            for s, p, o in self._g.triples((subject, None, None)):
                subgraph.add((s, p, o))
                if isinstance(o, BNode):
                    add_triples(o)
            for s, p, o in self._g.triples((None, None, subject)):
                if isinstance(s, BNode):
                    add_triples(s)
        add_triples(subject)
        return subgraph.serialize(format=format)

class GraphNode(object):

    def __init__(self, uri, graph, pos):
        self.uri = uri
        self.graph = graph
        self._objects = {}
        for p, o in pos:
            self._objects.setdefault(p, []).append(o)
        if RDF_TYPE not in self._objects:
            raise ValueError('%r with unknown type' % uri)
        self.types = frozenset(self._objects[RDF_TYPE])

    def __repr__(self):
        return '<rdfob.GraphNode of %r in %r >' % (self.uri, self.graph)

    def _node_or_literal(self, x):
        if isinstance(x, (URIRef, BNode)):
            return self.graph[x]
        else:
            assert isinstance(x, rdflib.Literal)
            if x.datatype == NAMESPACES['rdf']['XMLLiteral']:
                return genshi.XML(x)
            elif x.datatype == NAMESPACES['xsd']['datetime']:
                return iso8601.parse_date(x)
            elif x.datatype == NAMESPACES['xsd']['date']:
                m = re.match('(\d{4})-(\d{2})-(\d{2})', x)
                if m:
                    return datetime.date(int(m.group(1)), int(m.group(2)), int(m.group(3)))
                else:
                    return x
            else:
                return x.toPython()

    def is_any(self, types):
        for type in types:
            if uriref(type) in self.types:
                return True
        return False

    _NO_DEFAULT = ()
    def getone(self, predicate, default=_NO_DEFAULT, as_uriref=False):
        predicate = uriref(predicate)
        objects = self._objects.get(predicate, [])
        if len(objects) > 1:
            raise UniquenessError(objects)
        if not objects:
            if default is not self._NO_DEFAULT:
                return default
            else:
                raise KeyError(predicate)
        if as_uriref:
            return objects[0]
        else:
            return self._node_or_literal(objects[0])

    def __getitem__(self, key):
        return self.getone(key)

    def __contains__(self, key):
        predicate = uriref(key)
        return predicate in self._objects

    def getall(self, predicate, as_uriref=False):
        predicate = uriref(predicate)
        if as_uriref:
            return self._objects[predicate]
        else:
            return [self._node_or_literal(x) for x in self._objects.get(predicate, [])]

    def reflexive(self, predicate, type=None):
        predicate = uriref(predicate)
        subjects = [self.graph[x] for x in self.graph._g.subjects(predicate, self.uri)]
        if type is None:
            return subjects
        else:
            type = uriref(type)
            return [s for s in subjects if type in s.types]

    def __iter__(self):
        assert self.type == RDF_SEQ, self.type
        return iter(self.getone(p) for p in self._objects.iterkeys() if p.startswith(NAMESPACES['rdf']['_']))

    def identifier(self, scheme):
        for identifier in self._objects.get(uriref('dc:identifier'), []):
            if identifier.startswith(scheme):
                return identifier[len(scheme):]
