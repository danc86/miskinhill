
import datetime, re
import rdflib
from rdflib import URIRef, Namespace, BNode
from rdflib.Graph import ConjunctiveGraph
import RDFSClosure
import genshi

NAMESPACES = {
    'mhs': Namespace('http://miskinhill.com.au/rdfschema/1.0/'), 
    'dc': Namespace('http://purl.org/dc/terms/'), 
    'foaf': Namespace('http://xmlns.com/foaf/0.1/'), 
    'rdf': Namespace('http://www.w3.org/1999/02/22-rdf-syntax-ns#'), 
    'xsd': Namespace('http://www.w3.org/TR/xmlschema-2/#')
}
RDF_TYPE = NAMESPACES['rdf']['type']
RDF_SEQ = NAMESPACES['rdf']['Seq']

def uriref(qname):
    if isinstance(qname, (URIRef, BNode)): return qname
    ns, name = qname.split(':', 1)
    return NAMESPACES[ns][name]

class UniquenessError(ValueError): pass

class Graph(object):

    def __init__(self, *imports):
        self.store = rdflib.plugin.get('IOMemory', rdflib.store.Store)('rdfstore')
        self.store.open('')
        self._g = ConjunctiveGraph(self.store)
        for filename in imports:
            self._g.parse(filename, format='nt')
        #RDFSClosure.create_RDFSClosure(self._g)

    def __getitem__(self, subject):
        subject = uriref(subject)
        pos = list(self._g.predicate_objects(subject))
        if not pos:
            raise KeyError(subject)
        return GraphNode(subject, self, pos)

    def serialized(self, subject):
        subject = uriref(subject)
        subgraph = ConjunctiveGraph()
        subgraph += self._g.triples((subject, None, None))
        return subgraph.serialize(format='nt')

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
                return genshi.Markup(x)
            elif x.datatype == NAMESPACES['xsd']['date']:
                m = re.match('(\d{4})-(\d{2})-(\d{2})', x)
                if m:
                    return datetime.datetime(int(m.group(1)), int(m.group(2)), int(m.group(3)))
                else:
                    return x
            else:
                return x.toPython()

    def getone(self, predicate, as_uriref=False):
        predicate = uriref(predicate)
        objects = self._objects[predicate]
        if len(objects) > 1:
            raise UniquenessError(objects)
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
            return [self._node_or_literal(x) for x in self._objects[predicate]]

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
