#!/d/Bin/Python/python.exe
# -*- coding: utf-8 -*-
#
"""
This module is brute force implementation of the RDFS semantics on the top of RDFLib (with some caveats, see below). It also implements the
pD* semantics (also known as "ter Horst Semantics"), described in H.J.T. Horst, "Completeness, decidability and complexity of entailment 
for RDF Schema and a semantic extension involving the OWL vocabulary," Journal of Web Semantics,  vol. 3, 2005, pp. 79-115

What happens (when the L{RDFS_Semantics.closure()<_RDFS_Semantics.closure>} method is invoked) is
as follows:

	- The RDFS (and, possibly, the additional pD*) axiomatic triples are added to the graph. 
	Note: the module follows ter Horst's paper and adds only those C{rdf:_i} type
	predicates that do appear in the graph, thereby keeping this step finite.

	- There are a number of RDFS entailement rules in the U{RDF Semantics document<http://www.w3.org/TR/rdf-mt/>} that all expand the
	graph if certain types of triplets are in it. These are translated into relevant methods in Python. 
	These methods are invoked in a cycle and, in each cycle, the methods check whether the
	new triple to be added is already in the graph or not, i.e., whether a
	cycle through the entailement rules expands the graph or not. If not, then the graph expansion stops.

Note, however, that the module does I{not} implement the so called Datatype entailement rules, simply because the underlying RDFLib does
not implement the datatypes (ie, RDFLib will not make the literal "1.00" and "1.00000" identical, although
even with all the ambiguities on datatypes, this I{should} be made equal...). Also, the so-called extensional entailement rules 
(Section 7.3.1 in the RDF Semantics document) have not been implemented either.

The comments and references to the various rule follow the names as used in the U{RDF Semantics document<http://www.w3.org/TR/rdf-mt/>}.

I{This code relies on RDFLib 2.2.2. and higher.}

@author: U{Ivan Herman<http://www.ivan-herman.net>}

@license: This software is available for use under the U{W3C Software License<http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231>}

"""

"""
$Id: __init__.py,v 1.30 2008/03/19 12:53:33 ivan Exp $ $Date: 2008/03/19 12:53:33 $
"""

__version__ = "1.1"
__author__  = 'Ivan Herman'
__contact__ = 'Ivan Herman, ivan@w3.org'
__license__ = u'W3C® SOFTWARE NOTICE AND LICENSE, http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231'

import rdflib

from rdflib.BNode       import BNode
from rdflib.Namespace   import Namespace
from rdflib.Literal		 import Literal as rdflibLiteral
from rdflib.RDF			 import RDFNS  as ns_rdf
from rdflib.RDFS		 import RDFSNS as ns_rdfs
from rdflib.exceptions  import Error

from rdflib.RDF import Seq, Bag, Alt, Statement, Property, XMLLiteral, List
from rdflib.RDF import subject, predicate, object, type, value, first, rest, nil

from rdflib.RDFS import Resource, Class, subClassOf, subPropertyOf, comment, label, domain, range
from rdflib.RDFS import seeAlso, isDefinedBy, Literal, Container, ContainerMembershipProperty, member, Datatype

OWLNS = Namespace("http://www.w3.org/2002/07/owl#")
FunctionalProperty        = OWLNS["FunctionalProperty"]
InverseFunctionalProperty = OWLNS["InverseFunctionalProperty"]
SymmetricProperty         = OWLNS["SymmetricProperty"]
TransitiveProperty        = OWLNS["TransitiveProperty"]
sameAs                    = OWLNS["sameAs"]
inverseOf                 = OWLNS["inverseOf"]
equivalentClass           = OWLNS["equivalentClass"]
equivalentProperty        = OWLNS["equivalentProperty"]
Restriction               = OWLNS["Restriction"]
onProperty                = OWLNS["onProperty"]
hasValue                  = OWLNS["hasValue"]
someValuesFrom            = OWLNS["someValuesFrom"]
allValuesFrom             = OWLNS["allValuesFrom"]
differentFrom             = OWLNS["differentFrom"]
disjointWith              = OWLNS["disjointWith"]

######################################################################################################

class _Allocations :
	"""Allocations for bnodes and literals. This is simply a datastructure of two dictionaries
	running, so to say, in parallel: one keyed by bnodes and one by the corresponding literals.
	"""
	def __init__(self) :
		self.lg = {}
		self.gl = {}

class _Core :
	"""Core of the semantics management, dealing with the RDFS Semantic triples. The only
	reason to have it in a separate class is for an easier maintainability.
	
	This is a common superclass only. In the present module, it is subclassed by 
	a L{RDFS Closure<_RDFS_Semantics>} class and a L{pD* CLosure<_pD_Semantics>} classes. (The latter
	implements ter Horst's semantics for a reduced OWL.) There are some methods that are implemented in the
	subclasses only, ie, this class cannot be used by itself!
	
	@ivar Rules: array of instance methods that have to be called (with self as a parameter) to execute all the necessary rules. Each method should return True or False, depending on whether a new triplet has been added to the graph or not.
	@ivar IMaxNum: maximal index of C{rdf:_i} occurence in the graph
	@ivar lg_gl_Allocations: allocations for the lg/gl rules
	@type lg_gl_Allocations: L{_Allocations}
	@ivar graph: the real graph
	@type graph: rdflib.Graph
	"""
	def __init__(self,graph) :
		"""
		@param graph: the RDF graph to be extended
		@type graph: rdflib.Graph
		"""
		# Calculate the maximum 'n' value for the '_i' type predicates (see Horst's paper)
		n      = 1;
		maxnum = 0
		cont   = True
		while cont :
			cont = False
			predicate = ns_rdf[("_%d" % n)]
			for (s,p,o) in graph.triples((None,predicate,None)) :
				# there is at least one if we got here
				maxnum = n
				n += 1;
				cont = True
		self.IMaxNum           = maxnum
		
		self.graph             = graph
		self.lg_gl_Allocations = _Allocations()
		
		self.Rules = []
	
	def addAxiomaticTriples(self) :
		"""Add the axiomatic triples to the graph. 
		
		This is just a placeholder; the method should be defined in the subclasses.
		"""
		raise Exception("This method should be defined in a subclass!")

	def checkAndAddTriple(self,t) :
		"""Check if the triple is in the graph; if yes, returns False; if not, it adds the triple and returns True.
		@param t: the triple to be added to the graph, unless it is already there
		@type t: a 3-element tuple of (s,p,o)
		@rtype: Boolean
		"""
		try :
			if t in self.graph :
				return False
			else :
				self.graph.add(t)
				return True
		except :
			return False

	def closure(self,axioms=True) :
		"""Generate the RDFS closure the graph. This is the real core.

			For debug purposes, the second argument may be set to False to avoid adding the axiomatic triples. Though
			this is incorrect from the semantics point of view, it avoids clutters for debugging.

			Once the closure is finished, two more actions are taken. The blank nodes added to the
			closure to manage literals (the lg-gl rules pair in the Semantics document) are removed
			from the graph (including all triples they are used in). These nodes are there to ensure a smooth
			RDFS enclosure, but are not used by the outside user (and would just create confusion).

			@param axioms: turn on/off the axiomatic tuples
			@type axioms: Boolean
		"""
		# first, add the axiomatic triples
		if axioms :
			self.addAxiomaticTriples()

		# second cyclically go through all rules until no change happens
		changes = [ True ]
		i = 0
		while True in changes :
			# yes, there was a change, let us go again
			i += 1
			# print "... %d" % i
			# go through all rules, and collect the replies (whether any change has been done)
			changes = [ r(self) for r in self.Rules ]


		# the closure is done. At this point, the blank nodes introduced
		# for the literals become really unnecessary...
		for b in self.lg_gl_Allocations.gl :
			# I do this in two steps. I simply do not know how the
			# iterator of the graph implementation reacts if I silently
			# delete a relevant tuple from under its feet...
			# Better be careful
			d = [ (s,p,b) for (s,p,o) in self.graph.triples((None,None,b)) ]
			for t in d: self.graph.remove(t)

			d = [ (b,p,o) for (s,p,o) in self.graph.triples((b,None,None)) ]
			for t in d: self.graph.remove(t)

## RDFS Semantics class
class _RDFS_Semantics(_Core) :
	"""RDFS Semantics class, ie, implementation of the RDFS closure graph (modulo some caveats, see introduction"""
	def __init__(self,graph) :
		"""
		@param graph: the RDF graph to be extended
		@type graph: rdflib.Graph
		"""
		_Core.__init__(self,graph)
		self.Rules = [
			_RDFS_Semantics.Rule_lg, _RDFS_Semantics.Rule_gl, _RDFS_Semantics.genericRules
		]

	def addAxiomaticTriples(self) :
		"""
		Add all the axiomatic triples to the graph.
		"""
		# RDF Axiomatic triples, except for the _i properties that are added later
		self.checkAndAddTriple((type,type,Property))
		self.checkAndAddTriple((subject,type,Property))
		self.checkAndAddTriple((predicate,type,Property))
		self.checkAndAddTriple((object,type,Property))
		self.checkAndAddTriple((first,type,Property))
		self.checkAndAddTriple((rest,type,Property))
		self.checkAndAddTriple((value,type,Property))
		self.checkAndAddTriple((nil,type,List))

		# RDFS Axiomatic triples
		self.checkAndAddTriple((type,domain,Resource))
		self.checkAndAddTriple((domain,domain,Property))
		self.checkAndAddTriple((range,domain,Property))
		self.checkAndAddTriple((subPropertyOf,domain,Property))
		self.checkAndAddTriple((subClassOf,domain,Class))
		self.checkAndAddTriple((subject,domain,Statement))
		self.checkAndAddTriple((predicate,domain,Statement))
		self.checkAndAddTriple((object,domain,Statement))
		self.checkAndAddTriple((member,domain,Resource))
		self.checkAndAddTriple((first,domain,List))
		self.checkAndAddTriple((rest,domain,List))
		self.checkAndAddTriple((seeAlso,domain,Resource))
		self.checkAndAddTriple((isDefinedBy,domain,Resource))
		self.checkAndAddTriple((comment,domain,Resource))
		self.checkAndAddTriple((label,domain,Resource))
		self.checkAndAddTriple((value,domain,Resource))

		self.checkAndAddTriple((type,range,Class))
		self.checkAndAddTriple((domain,range,Class))
		self.checkAndAddTriple((range,range,Class))
		self.checkAndAddTriple((subPropertyOf,range,Property))
		self.checkAndAddTriple((subClassOf,range,Class))
		self.checkAndAddTriple((subject,range,Resource))
		self.checkAndAddTriple((predicate,range,Resource))
		self.checkAndAddTriple((object,range,Resource))
		self.checkAndAddTriple((member,range,Resource))
		self.checkAndAddTriple((first,range,Resource))
		self.checkAndAddTriple((rest,range,List))
		self.checkAndAddTriple((seeAlso,range,Resource))
		self.checkAndAddTriple((isDefinedBy,range,Resource))
		self.checkAndAddTriple((comment,range,Literal))
		self.checkAndAddTriple((label,range,Literal))
		self.checkAndAddTriple((value,range,Resource))

		self.checkAndAddTriple((Alt,subClassOf,Container))
		self.checkAndAddTriple((Bag,subClassOf,Container))
		self.checkAndAddTriple((Seq,subClassOf,Container))
		self.checkAndAddTriple((ContainerMembershipProperty,subClassOf,Property))

		self.checkAndAddTriple((isDefinedBy,subPropertyOf,seeAlso))

		self.checkAndAddTriple((XMLLiteral,type,Datatype))
		self.checkAndAddTriple((XMLLiteral,subClassOf,Literal))
		self.checkAndAddTriple((Datatype,subClassOf,Class))

		for i in xrange(1,self.IMaxNum+1) :
			ci = ns_rdf[("_%d" % i)]
			self.checkAndAddTriple((ci,type,Property))
			self.checkAndAddTriple((ci,type,ContainerMembershipProperty))
			self.checkAndAddTriple((ci,domain,Resource))
			self.checkAndAddTriple((ci,range,Resource))

		# rdfs valid triples; these would be inferred by the RDFS expansion, but it may make things
		# a bit faster to add these upfront
		self.checkAndAddTriple((Resource,type,Class))
		self.checkAndAddTriple((Class,type,Class))
		self.checkAndAddTriple((Literal,type,Class))
		self.checkAndAddTriple((XMLLiteral,type,Class))
		self.checkAndAddTriple((Datatype,type,Class))
		self.checkAndAddTriple((Seq,type,Class))
		self.checkAndAddTriple((Bag,type,Class))
		self.checkAndAddTriple((Alt,type,Class))
		self.checkAndAddTriple((Container,type,Class))
		self.checkAndAddTriple((List,type,Class))
		self.checkAndAddTriple((ContainerMembershipProperty,type,Class))
		self.checkAndAddTriple((Property,type,Class))
		self.checkAndAddTriple((Statement,type,Class))

		self.checkAndAddTriple((domain,type,Property))
		self.checkAndAddTriple((range,type,Property))
		self.checkAndAddTriple((subPropertyOf,type,Property))
		self.checkAndAddTriple((subClassOf,type,Property))
		self.checkAndAddTriple((member,type,Property))
		self.checkAndAddTriple((seeAlso,type,Property))
		self.checkAndAddTriple((isDefinedBy,type,Property))
		self.checkAndAddTriple((comment,type,Property))
		self.checkAndAddTriple((label,type,Property))

	def genericRules(self) :
		"""
			Go through the RDFS entailement rules rdf1, rdfs4-rdfs12, by extending the graph.
			@return: True if the graph has been extended as a result of at least one of the rules, False otherwise
			@rtype: Boolean
		"""
		TuplesToAdd = []
		for s,p,o in self.graph.triples((None,None,None)) :
			# rdf1
			TuplesToAdd.append((p,type,Property))
			# rdfs4a
			TuplesToAdd.append((s,type,Resource))
			# rdfs4b
			TuplesToAdd.append((o,type,Resource))
			if p == domain :
				# rdfs2
				for uuu,Y,yyy in self.graph.triples((None,s,None)) :
					TuplesToAdd.append((uuu,type,o))
			if p == range :
				# rdfs3
				for uuu,Y,vvv in self.graph.triples((None,s,None)) :
					TuplesToAdd.append((vvv,type,o))
			if p == subPropertyOf :
				# rdfs5
				for Z,Y,xxx in self.graph.triples((o,subPropertyOf,None)) :
					TuplesToAdd.append((s,subPropertyOf,xxx))
				# rdfs7
				for zzz,Z,www in self.graph.triples((None,s,None)) :
					TuplesToAdd.append((zzz,o,www))
			if p == type and o == Property :
				# rdfs6
				TuplesToAdd.append((s,subPropertyOf,s))
			if p == type and o == Class :
				# rdfs8
				TuplesToAdd.append((s,subClassOf,Resource))
				# rdfs10
				TuplesToAdd.append((s,subClassOf,s))
			if p == subClassOf :
				# rdfs9
				for vvv,Y,Z in self.graph.triples((None,type,s)) :
					TuplesToAdd.append((vvv,type,o))
				# rdfs11
				for Z,Y,xxx in self.graph.triples((o,subClassOf,None)) :
					TuplesToAdd.append((s,subClassOf,xxx))
			if p == type and o == ContainerMembershipProperty :
				# rdfs12
				TuplesToAdd.append((s,subPropertyOf,member))
			if p == type and o == Datatype :
				TuplesToAdd.append((s,subClassOf,Literal))
		if len(TuplesToAdd) == 0 :
			return False
		else :
			ex = [ self.checkAndAddTriple(t) for t in TuplesToAdd ]
			return True in ex

	def Rule_lg(self) :
		"""
			RDFS entailement rules "lg": add 'associated' Bnodes to literals.
			@return: True if the graph has been extended as a result of at least one of the rules, False otherwise
			@rtype: Boolean
		"""
		TuplesToAdd = []
		for uuu,aaa,lll in self.graph.triples((None,None,None)) :
			if isinstance(lll,rdflibLiteral) :
				if lll in self.lg_gl_Allocations.lg :
					bn = self.lg_gl_Allocations.lg[lll]
				else :
					bn = BNode()
					self.lg_gl_Allocations.lg[lll] = bn
					self.lg_gl_Allocations.gl[bn]  = lll
				TuplesToAdd.append((uuu,aaa,bn))
		if len(TuplesToAdd) == 0 :
			return False
		else :
			ex = [ self.checkAndAddTriple(t) for t in TuplesToAdd ]
			return True in ex

	def Rule_gl(self) :
		"""
			RDFS entailement rules "gl": add tuples with literals to 'associated' Bnodes.
			@return: True if the graph has been extended as a result of at least one of the rules, False otherwise
			@rtype: Boolean
		"""
		TuplesToAdd = []
		for uuu,aaa,lll in self.graph.triples((None,None,None)) :
			if lll in self.lg_gl_Allocations.gl :
				TuplesToAdd.append((uuu,aaa,self.lg_gl_Allocations.gl[lll]))
		if len(TuplesToAdd) == 0 :
			return False
		else :
			ex = [ self.checkAndAddTriple(t) for t in TuplesToAdd ]
			return True in ex

## pD* Semantics class
class _pD_Semantics(_RDFS_Semantics) :
	"""Implentation of the pD* semantics, ie, the semantics described in: H.J.T. Horst, "Completeness, decidability and complexity of entailment 
	for RDF Schema and a semantic extension involving the OWL vocabulary," Journal of Web Semantics,  vol. 3, 2005, pp. 79-115."""
	def __init__(self,graph) :
		"""
		@param graph: the RDF graph to be extended
		@type graph: rdflib.Graph
		"""
		_RDFS_Semantics.__init__(self,graph)
		self.Rules.append(_pD_Semantics.pDRules)

	def addAxiomaticTriples(self) :
		"""
		Add all the axiomatic triples to the graph. These are the same as the RDFS triples, plus some more, OWL specific ones.
		"""
		_RDFS_Semantics.addAxiomaticTriples(self)
		# pD* Axiomatic triples
		self.checkAndAddTriple((FunctionalProperty,subClassOf,Property))
		self.checkAndAddTriple((InverseFunctionalProperty,subClassOf,Property))
		self.checkAndAddTriple((SymmetricProperty,subClassOf,Property))
		self.checkAndAddTriple((TransitiveProperty,subClassOf,Property))
		self.checkAndAddTriple((sameAs,type,Property))
		self.checkAndAddTriple((inverseOf,type,Property))
		self.checkAndAddTriple((inverseOf,domain,Property))
		self.checkAndAddTriple((inverseOf,range,Property))
		self.checkAndAddTriple((equivalentClass,type,Property))
		self.checkAndAddTriple((equivalentProperty,type,Property))
		self.checkAndAddTriple((equivalentClass,domain,Class))
		self.checkAndAddTriple((equivalentClass,range,Class))
		self.checkAndAddTriple((equivalentClass,type,Property))
		self.checkAndAddTriple((equivalentClass,type,Property))
		self.checkAndAddTriple((equivalentProperty,domain,Property))
		self.checkAndAddTriple((equivalentProperty,range,Property))
		self.checkAndAddTriple((Restriction,subClassOf,Class))
		self.checkAndAddTriple((onProperty,domain,Restriction))
		self.checkAndAddTriple((onProperty,range,Property))
		self.checkAndAddTriple((equivalentProperty,range,Property))
		self.checkAndAddTriple((hasValue,domain,Restriction))
		self.checkAndAddTriple((someValuesFrom,domain,Restriction))
		self.checkAndAddTriple((someValuesFrom,range,Class))
		self.checkAndAddTriple((allValuesFrom,domain,Restriction))
		self.checkAndAddTriple((allValuesFrom,range,Class))
		self.checkAndAddTriple((differentFrom,type,Property))
		self.checkAndAddTriple((disjointWith,domain,Class))
		self.checkAndAddTriple((disjointWith,range,Class))

	def pDRules(self) :
		"""Implentation of the pD* specific rules. For details, see Herman ter Horst's paper..."""
		TuplesToAdd = []
		for s,p,o in self.graph.triples((None,None,None)) :
			# rdfp1
			if (p,type,FunctionalProperty) in self.graph :
				for X,Y,w in self.graph.triples((s,p,None)) :
					TuplesToAdd.append((w,sameAs,o))
			# rdfp2
			if (p,type,InverseFunctionalProperty) in self.graph :
				for u,Y,Z in self.graph.triples((None,p,o)) :
					TuplesToAdd.append((s,sameAs,u))
			# rdfp3
			if (p,type,SymmetricProperty) in self.graph :
				TuplesToAdd.append((o,p,s))
			# rdfp4
			if (p,type,TransitiveProperty) in self.graph :
				for X,Y,w in self.graph.triples((o,p,None)) :
					TuplesToAdd.append((s,p,w))
			# rdfp5a
			TuplesToAdd.append((s,sameAs,s))
			# rdfp5b
			TuplesToAdd.append((o,sameAs,o))
			# rdfp6, rdfp7
			if p == sameAs :
				# rdfp6
				TuplesToAdd.append((o,sameAs,s))
				# rdfp7
				for X,Y,w in self.graph.triples((o,sameAs,None)) :
					TuplesToAdd.append((s,sameAs,w))
			# rdfp8
			if p == inverseOf :
				# rdfp8ax
				for u,X,w in self.graph.triples((None,s,None)) :
					TuplesToAdd.append((w,o,u))
				# rdfp8bx
				for u,X,w in self.graph.triples((None,o,None)) :
					TuplesToAdd.append((w,s,u))
			# rdfp9
			if (s,type,Class) in self.graph and p == sameAs :
				TuplesToAdd.append((s,subClassOf,o))
			# rdfp10
			if (s,type,Property) in self.graph and p == sameAs :
				TuplesToAdd.append((s,subPropertyOf,o))
			# rdfp11
			for (X,Y,u) in self.graph.triples((s,sameAs,None)) :
				for (U,V,w) in self.graph.triples((o,sameAs,None)) :
					TuplesToAdd.append((u,p,w))
			# rdfp12a, rdfp12b
			if p == equivalentClass :
				TuplesToAdd.append((s,subClassOf,o))
				TuplesToAdd.append((o,subClassOf,s))
			# rdfp12c
			if p == subClassOf and (o,subClassOf,s) in self.graph :
				TuplesToAdd.append((s,equivalentClass,o))
			# rdfp13a, rdfp13b
			if p == equivalentProperty :
				TuplesToAdd.append((s,subPropertyOf,o))
				TuplesToAdd.append((o,subPropertyOf,s))
			# rdfp13c
			if p == subPropertyOf and (o,subPropertyOf,s) in self.graph :
				TuplesToAdd.append((s,equivalentProperty,o))
			# rdfp14a rdfsp14bx
			if p == hasValue :
				v = s
				w = o
				# rdfp14a
				for (X,Y,pp) in self.graph.triples((v,onProperty,None)) :
					for (u,U,V) in self.graph.triples((None,pp,w)) :
						TuplesToAdd.append((u,type,v))
				# rdfsp14bx
				for (X,Y,pp) in self.graph.triples((v,onProperty,None)) :
					for (u,U,V) in self.graph.triples((None,type,v)) :
						TuplesToAdd.append((u,pp,ww))
			# rdfsp15
			if p == someValuesFrom :
				v = s
				w = o
				for (X,Y,pp) in self.graph.triples((v,onProperty,None)) :
					for (u,Z,x) in self.graph.triples((None,pp,None)) :
						if (x,type,w) in self.graph :
							TuplesToAdd.append((u,type,v))
			# rdfsp16
			if p == allValuesFrom :
				v = s
				w = o
				for (X,Y,pp) in self.graph.triples((v,onProperty,None)) :
					for (u,U,V) in self.graph.triples((None,type,v)) :
						for (XX,YY,x) in self.graph.triples((u,pp,None)) :
							TuplesToAdd.append((x,type,w))


		if len(TuplesToAdd) == 0 :
			return False
		else :
			ex = [ self.checkAndAddTriple(t) for t in TuplesToAdd ]
			return True in ex


##################################################################################

def create_RDFSClosure(origGraph,targetGraph=None,addAxiomaticTriples=True) :
	"""Create an RDFS closure graph. The closure itself will be generated into the target graph, including all RDFS axiomatic
	triples, the original graph content, and the RDFS triples.
	
	The semantics of this closure is described in: H.J.T. Horst, "Completeness, decidability and complexity of entailment 
	for RDF Schema and a semantic extension involving the OWL vocabulary," Journal of Web Semantics,  vol. 3, 2005, pp. 79-115.

	@param origGraph: the original core graph
	@type origGraph: rdflib.Graph
	@param targetGraph: the target of the closure graph. If not None, all triples of the original graph
	will be added to this graph before generating the closure. If this value None, then the target graph is origGraph
	@type targetGraph: rdflib.Graph
	@param addAxiomaticTriples: by default, all axiomatic triples are generated, as requested by the RDFS Semantics. However, in case of debugging, it might be
	beneficial not to add those to the results
	@type addAxiomaticTriples: Boolean
	@return: the full closure graph
	@rtype: rdflib.Graph
	"""
	if targetGraph :
		graph = targetGraph
		# copy the content of the original graph
		graph += origGraph
	else :
		graph = origGraph
	# Generate the closure
	_RDFS_Semantics(graph).closure(addAxiomaticTriples)
	return graph

def create_pDClosure(origGraph,targetGraph=None,addAxiomaticTriples=True) :
	"""Create an RDFS closure graph. The closure itself will be generated into the target graph, including all RDFS axiomatic
	triples, the original graph content, and the RDFS triples. 

	The semantics of this closure is described in: H.J.T. Horst, "Completeness, decidability and complexity of entailment 
	for RDF Schema and a semantic extension involving the OWL vocabulary," Journal of Web Semantics,  vol. 3, 2005, pp. 79-115.

	@param origGraph: the original core graph
	@type origGraph: rdflib.Graph
	@param targetGraph: the target of the closure graph. If not None, all triples of the original graph
	will be added to this graph before generating the closure. If this value None, then the target graph is origGraph
	@type targetGraph: rdflib.Graph
	@param addAxiomaticTriples: by default, all axiomatic triples are generated, as requested by the RDFS Semantics. However, in case of debugging, it might be
	beneficial not to add those to the results
	@return: the full closure graph
	@rtype: rdflib.Graph
	"""
	if targetGraph :
		graph = targetGraph
		# copy the content of the original graph
		graph += origGraph
	else :
		graph = origGraph
	# Generate the closure
	_pD_Semantics(graph).closure(addAxiomaticTriples)
	return graph



