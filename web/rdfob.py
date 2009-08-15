
import datetime, re
import RDF
from RDF import Uri
import genshi
import iso8601

NAMESPACES = {
    'mhs': RDF.NS('http://miskinhill.com.au/rdfschema/1.0/'), 
    'dc': RDF.NS('http://purl.org/dc/terms/'), 
    'old-dc': RDF.NS('http://purl.org/dc/elements/1.1/'), # mainly exists for namespace prefix mapping in serialized XML
    'foaf': RDF.NS('http://xmlns.com/foaf/0.1/'), 
    'rdf': RDF.NS('http://www.w3.org/1999/02/22-rdf-syntax-ns#'), 
    'rdfs': RDF.NS('http://www.w3.org/2000/01/rdf-schema#'), 
    'xs': RDF.NS('http://www.w3.org/2001/XMLSchema#'), 
    'xsd': RDF.NS('http://www.w3.org/TR/xmlschema-2/#'), 
    'contact': RDF.NS('http://www.w3.org/2000/10/swap/pim/contact#'), 
    'geonames': RDF.NS('http://www.geonames.org/ontology#'), 
    'sioc': RDF.NS('http://rdfs.org/sioc/ns#'), 
    'awol': RDF.NS('http://bblfish.net/work/atom-owl/2006-06-06/#'),
    'lingvoj': RDF.NS('http://www.lingvoj.org/ontology#'), 
    'prism': RDF.NS('http://prismstandard.org/namespaces/1.2/basic/'), 
    'owl': RDF.NS('http://www.w3.org/2002/07/owl#')
}
RDF_TYPE = NAMESPACES['rdf']['type']
RDF_SEQ = NAMESPACES['rdf']['Seq']

def uriref(qname):
    if isinstance(qname, (RDF.Uri, RDF.Node)): return qname
    ns, name = qname.split(':', 1)
    return NAMESPACES[ns][name]

class UniquenessError(ValueError): pass

class Graph(object):

    def __init__(self, *imports):
        self._g = RDF.Model()
        for filename in imports:
            self._g.load('file:' + filename, name='rdfxml')

    def __getitem__(self, subject):
        subject = uriref(subject)
        stmts = list(self._g.find_statements(RDF.Statement(subject, None, None)))
        if not stmts:
            raise KeyError(subject)
        return GraphNode(stmts[0].subject, self, stmts)

    def __contains__(self, subject):
        subject = uriref(subject)
        return bool(list(self._g.find_statements(RDF.Statement(subject, None, None)))) # XXX avoid list()?

    def by_type(self, type):
        type = uriref(type)
        return [self[s] for s in self._g.get_sources(RDF_TYPE, type)]

    def subjects(self):
        """ Returns an iterable across all subjects in the graph. """
        for stmt in self._g.find_statements(RDF.Statement(None, RDF_TYPE, None)):
            yield self[stmt.subject]

    def serialized(self, subject, format='nt'):
        """ Returns serialized triples about the given graph node (triples 
        where it appears as subject, and the transitive closure with any blank 
        nodes encountered). """
        # XXX make serialized output order predictable/consistent!
        subject = uriref(subject)
        subgraph = RDF.Model()
        # recursively add interesting triples
        visited = set()
        def add_triples(subject):
            if subject in visited: return
            visited.add(subject)
            for stmt in self._g.find_statements(RDF.Statement(subject, None, None)):
                subgraph.append(stmt)
                if stmt.object.is_blank():
                    add_triples(stmt.object)
            for stmt in self._g.find_statements(RDF.Statement(None, None, subject)):
                if stmt.subject.is_blank():
                    add_triples(stmt.subject)
        # XXX need to figure out an efficient way to do this
        #for s in self._g.subjects():
        #    if self._g.absolutize(s, defrag=True) == subject:
        #        add_triples(s)
        add_triples(subject)
        ser = RDF.Serializer(name={'xml': 'rdfxml-abbrev', 'nt': 'ntriples'}[format])
        for prefix, namespace in NAMESPACES.iteritems():
            ser.set_namespace(prefix, namespace[''].uri)
        return ser.serialize_model_to_string(subgraph)

class GraphNode(object):

    def __init__(self, node, graph, stmts):
        self.node = node
        if node.is_resource():
            self.uri = node.uri
        self.graph = graph
        self._objects = {}
        for stmt in stmts:
            self._objects.setdefault(stmt.predicate, []).append(stmt.object)
        if RDF_TYPE not in self._objects:
            raise ValueError('%r with unknown type' % node)
        self.types = frozenset(self._objects[RDF_TYPE])

    def __repr__(self):
        return '<rdfob.GraphNode of %r in %r>' % (self.node, self.graph)

    def _node_or_literal(self, x):
        if not x.is_literal():
            return self.graph[x]
        else:
            if x.literal_value['datatype'] == NAMESPACES['rdf']['XMLLiteral'].uri:
                return genshi.XML(x.literal_value['string'])
            elif x.literal_value['datatype'] == NAMESPACES['xsd']['datetime'].uri:
                return iso8601.parse_date(x.literal_value['string'])
            elif x.literal_value['datatype'] == NAMESPACES['xsd']['date'].uri:
                m = re.match('(\d{4})-(\d{2})-(\d{2})', x.literal_value['string'])
                if m:
                    return datetime.date(int(m.group(1)), int(m.group(2)), int(m.group(3)))
                else:
                    return x.literal_value['string']
            elif x.literal_value['datatype'] == NAMESPACES['xs']['integer'].uri:
                return int(x.literal_value['string'])
            elif x.literal_value['datatype'] is None:
                return x.literal_value['string']
            else:
                raise ValueError(x.literal_value['datatype'])
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
            return self._objects.get(predicate, [])
        else:
            return [self._node_or_literal(x) for x in self._objects.get(predicate, [])]

    def reflexive(self, predicate, type=None):
        predicate = uriref(predicate)
        subjects = [self.graph[stmt.subject] for stmt in self.graph._g.find_statements(RDF.Statement(None, predicate, self.node))]
        if type is None:
            return subjects
        else:
            type = uriref(type)
            return [s for s in subjects if type in s.types]

    def __iter__(self):
        assert RDF_SEQ in self.types, self.types
        return iter(self.getone(p) for p in self._objects.iterkeys() if p.startswith(NAMESPACES['rdf']['_']))

    def identifier(self, scheme):
        for identifier in self._objects.get(NAMESPACES['dc']['identifier'], []):
            if unicode(identifier.uri).startswith(scheme):
                return unicode(identifier.uri)[len(scheme):]

    def __hash__(self):
        return hash((self.graph, self.node))

    def __eq__(self, other):
        return (self.graph is other.graph and self.node == other.node)
