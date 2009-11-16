package au.com.miskinhill.rdftemplate.selector;

import static au.com.miskinhill.rdftemplate.TestNamespacePrefixMap.*;
import static au.com.miskinhill.rdftemplate.selector.AdaptationMatcher.*;
import static au.com.miskinhill.rdftemplate.selector.PredicateMatcher.*;
import static au.com.miskinhill.rdftemplate.selector.SelectorComparatorMatcher.*;
import static au.com.miskinhill.rdftemplate.selector.SelectorMatcher.*;
import static au.com.miskinhill.rdftemplate.selector.TraversalMatcher.*;
import static org.junit.Assert.*;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import org.junit.Before;
import org.junit.Test;

import au.com.miskinhill.rdftemplate.TestNamespacePrefixMap;

public class SelectorParserUnitTest {
    
    private AntlrSelectorFactory factory;
    
    @Before
    public void setUp() {
        factory = new AntlrSelectorFactory();
        factory.setNamespacePrefixMap(TestNamespacePrefixMap.getInstance());
    }
    
    @Test
    public void shouldRecogniseSingleTraversal() throws Exception {
        Selector<RDFNode> selector = factory.get("dc:creator").withResultType(RDFNode.class);
        assertThat(selector, selector(traversal(DCTerms.NS, "creator")));
    }
    
    @Test
    public void shouldRecogniseMultipleTraversals() throws Exception {
        Selector<RDFNode> selector = factory.get("dc:creator/foaf:name").withResultType(RDFNode.class);
        assertThat(selector, selector(
                traversal(DCTerms.NS, "creator"),
                traversal(FOAF_NS, "name")));
    }
    
    @Test
    public void shouldRecogniseInverseTraversal() throws Exception {
        Selector<RDFNode> selector = factory.get("!dc:isPartOf/!dc:isPartOf").withResultType(RDFNode.class);
        assertThat(selector, selector(
                traversal(DCTerms.NS, "isPartOf").inverse(),
                traversal(DCTerms.NS, "isPartOf").inverse()));
    }
    
    @Test
    public void shouldRecogniseSortOrder() throws Exception {
        Selector<RDFNode> selector = factory.get("!mhs:isIssueOf(mhs:publicationDate#comparable-lv)").withResultType(RDFNode.class);
        assertThat(selector, selector(
                traversal(MHS_NS, "isIssueOf").inverse()
                .withSortOrder(selectorComparator(selector(traversal(MHS_NS, "publicationDate"))
                    .withAdaptation(comparableLVAdaptation())))));
    }
    
    @Test
    public void shouldRecogniseReverseSortOrder() throws Exception {
        Selector<RDFNode> selector = factory.get("!mhs:isIssueOf(~mhs:publicationDate#comparable-lv)").withResultType(RDFNode.class);
        assertThat(selector, selector(
                traversal(MHS_NS, "isIssueOf").inverse()
                .withSortOrder(selectorComparator(selector(traversal(MHS_NS, "publicationDate"))
                    .withAdaptation(comparableLVAdaptation())).reversed())));
    }
    
    @Test
    public void shouldRecogniseComplexSortOrder() throws Exception {
        Selector<RDFNode> selector = factory.get("!mhs:reviews(dc:isPartOf/mhs:publicationDate#comparable-lv)").withResultType(RDFNode.class);
        assertThat(selector, selector(
                traversal(MHS_NS, "reviews")
                .withSortOrder(selectorComparator(selector(traversal(DCTerms.NS, "isPartOf"), traversal(MHS_NS, "publicationDate"))
                        .withAdaptation(comparableLVAdaptation())))));
    }
    
    @Test
    public void shouldRecogniseUriAdaptation() throws Exception {
        Selector<?> selector = factory.get("mhs:coverThumbnail#uri");
        assertThat(selector, selector(
                traversal(MHS_NS, "coverThumbnail"))
                .withAdaptation(uriAdaptation()));
    }
    
    @Test
    public void shouldRecogniseBareUriAdaptation() throws Exception {
        Selector<?> selector = factory.get("#uri");
        assertThat(selector, selector().withAdaptation(uriAdaptation()));
    }
    
    @Test
    public void shouldRecogniseUriSliceAdaptation() throws Exception {
        Selector<?> selector = factory.get("dc:identifier[uri-prefix='urn:issn:']#uri-slice(9)");
        assertThat(selector, selector(
                traversal(DCTerms.NS, "identifier")
                    .withPredicate(uriPrefixPredicate("urn:issn:")))
                .withAdaptation(uriSliceAdaptation(9)));
    }
    
    @Test
    public void shouldRecogniseUriPrefixPredicate() throws Exception {
        Selector<RDFNode> selector = factory.get(
                "!mhs:isIssueOf[uri-prefix='http://miskinhill.com.au/journals/'](~mhs:publicationDate#comparable-lv)")
                .withResultType(RDFNode.class);
        assertThat(selector, selector(
                traversal(MHS_NS, "isIssueOf")
                    .inverse()
                    .withPredicate(uriPrefixPredicate("http://miskinhill.com.au/journals/"))
                    .withSortOrder(selectorComparator(selector(traversal(MHS_NS, "publicationDate"))
                            .withAdaptation(comparableLVAdaptation())).reversed())));
    }
    
    @Test
    public void shouldRecogniseSubscript() throws Exception {
        Selector<String> selector = factory.get(
                "!mhs:isIssueOf(~mhs:publicationDate#comparable-lv)[0]/mhs:coverThumbnail#uri")
                .withResultType(String.class);
        assertThat(selector, selector(
                traversal(MHS_NS, "isIssueOf")
                    .inverse()
                    .withSortOrder(selectorComparator(selector(traversal(MHS_NS, "publicationDate"))
                            .withAdaptation(comparableLVAdaptation())).reversed())
                    .withSubscript(0),
                traversal(MHS_NS, "coverThumbnail"))
                .withAdaptation(uriAdaptation()));
    }
    
    @Test
    public void shouldRecogniseLVAdaptation() throws Exception {
        Selector<Object> selector = factory.get("dc:language/lingvoj:iso1#lv").withResultType(Object.class);
        assertThat(selector, selector(
                traversal(DCTerms.NS, "language"),
                traversal("http://www.lingvoj.org/ontology#", "iso1"))
                .withAdaptation(lvAdaptation()));
    }
    
    @Test
    public void shouldRecogniseTypePredicate() throws Exception {
        Selector<RDFNode> selector = factory.get("!dc:creator[type=mhs:Review]").withResultType(RDFNode.class);
        assertThat(selector, selector(
                traversal(DCTerms.NS, "creator").inverse().withPredicate(typePredicate(MHS_NS, "Review"))));
    }
    
    @Test
    public void shouldRecogniseAndCombinationOfPredicates() throws Exception {
        Selector<RDFNode> selector = factory.get("!dc:creator[type=mhs:Review and uri-prefix='http://miskinhill.com.au/journals/']").withResultType(RDFNode.class);
        assertThat(selector, selector(
                traversal(DCTerms.NS, "creator").inverse()
                .withPredicate(booleanAndPredicate(
                    typePredicate(MHS_NS, "Review"),
                    uriPrefixPredicate("http://miskinhill.com.au/journals/")))));
    }
    
    @Test
    public void shouldRecogniseUnion() throws Exception {
        Selector<RDFNode> selector = factory.get("!dc:creator | !mhs:translator").withResultType(RDFNode.class);
        assertThat((UnionSelector<RDFNode>) selector, unionSelector(
                selector(traversal(DCTerms.NS, "creator").inverse()),
                selector(traversal(MHS_NS, "translator").inverse())));
    }
    
    @Test
    public void shouldRecogniseMultipleSortSelectors() throws Exception {
        Selector<RDFNode> selector = factory.get("!dc:creator(~dc:isPartOf/mhs:publicationDate#comparable-lv,mhs:startPage#comparable-lv)").withResultType(RDFNode.class);
        assertThat(selector, selector(
                traversal(DCTerms.NS, "creator").inverse()
                .withSortOrder(
                    selectorComparator(selector(traversal(DCTerms.NS, "isPartOf"), traversal(MHS_NS, "publicationDate"))
                        .withAdaptation(comparableLVAdaptation())).reversed(),
                    selectorComparator(selector(traversal(MHS_NS, "startPage")).withAdaptation(comparableLVAdaptation())))));
    }
    
    @Test
    public void shouldRecogniseFormattedDTAdaptation() throws Exception {
        Selector<?> selector = factory.get("dc:created#formatted-dt('d MMMM yyyy')");
        assertThat(selector, selector(traversal(DCTerms.NS, "created"))
                .withAdaptation(formattedDTAdaptation("d MMMM yyyy")));
    }
    
    @Test
    public void shouldRecogniseRdfType() throws Exception {
        // was broken due to ANTLR being confused about the literal string "type" which was hardcoded to be a predicate
        Selector<RDFNode> selector = factory.get("rdf:type").withResultType(RDFNode.class);
        assertThat(selector, selector(traversal(RDF.getURI(), "type")));
    }
    
    @Test(expected = InvalidSelectorSyntaxException.class)
    public void shouldThrowForInvalidSyntax() throws Exception {
        factory.get("dc:creator]["); // this is a parser error
    }
    
    @Test(expected = InvalidSelectorSyntaxException.class)
    public void shouldThrowForUnrecognisedCharacter() throws Exception {
        factory.get("dc:cre&ator"); // ... and this is a lexer error
    }
    
}
