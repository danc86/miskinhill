package au.com.miskinhill.rdftemplate.selector;

import static au.com.miskinhill.rdftemplate.selector.AdaptationMatcher.*;
import static au.com.miskinhill.rdftemplate.selector.PredicateMatcher.*;
import static au.com.miskinhill.rdftemplate.selector.SelectorMatcher.*;
import static au.com.miskinhill.rdftemplate.selector.SelectorComparatorMatcher.*;
import static au.com.miskinhill.rdftemplate.selector.TraversalMatcher.*;
import static org.junit.Assert.*;

import org.junit.Before;

import com.hp.hpl.jena.rdf.model.RDFNode;
import org.junit.Test;

public class SelectorParserUnitTest {
    
    private SelectorFactory factory;
    
    @Before
    public void setUp() {
        factory = new AntlrSelectorFactory();
    }
    
    @Test
    public void shouldRecogniseSingleTraversal() throws Exception {
        Selector<RDFNode> selector = factory.get("dc:creator").withResultType(RDFNode.class);
        assertThat(selector, selector(traversal("dc", "creator")));
    }
    
    @Test
    public void shouldRecogniseMultipleTraversals() throws Exception {
        Selector<RDFNode> selector = factory.get("dc:creator/foaf:name").withResultType(RDFNode.class);
        assertThat(selector, selector(
                traversal("dc", "creator"),
                traversal("foaf", "name")));
    }
    
    @Test
    public void shouldRecogniseInverseTraversal() throws Exception {
        Selector<RDFNode> selector = factory.get("!dc:isPartOf/!dc:isPartOf").withResultType(RDFNode.class);
        assertThat(selector, selector(
                traversal("dc", "isPartOf").inverse(),
                traversal("dc", "isPartOf").inverse()));
    }
    
    @Test
    public void shouldRecogniseSortOrder() throws Exception {
        Selector<RDFNode> selector = factory.get("!mhs:isIssueOf(mhs:publicationDate#comparable-lv)").withResultType(RDFNode.class);
        assertThat(selector, selector(
                traversal("mhs", "isIssueOf").inverse()
                .withSortOrder(selectorComparator(selector(traversal("mhs", "publicationDate"))
                    .withAdaptation(comparableLVAdaptation())))));
    }
    
    @Test
    public void shouldRecogniseReverseSortOrder() throws Exception {
        Selector<RDFNode> selector = factory.get("!mhs:isIssueOf(~mhs:publicationDate#comparable-lv)").withResultType(RDFNode.class);
        assertThat(selector, selector(
                traversal("mhs", "isIssueOf").inverse()
                .withSortOrder(selectorComparator(selector(traversal("mhs", "publicationDate"))
                    .withAdaptation(comparableLVAdaptation())).reversed())));
    }
    
    @Test
    public void shouldRecogniseComplexSortOrder() throws Exception {
        Selector<RDFNode> selector = factory.get("!mhs:reviews(dc:isPartOf/mhs:publicationDate#comparable-lv)").withResultType(RDFNode.class);
        assertThat(selector, selector(
                traversal("mhs", "reviews")
                .withSortOrder(selectorComparator(selector(traversal("dc", "isPartOf"), traversal("mhs", "publicationDate"))
                        .withAdaptation(comparableLVAdaptation())))));
    }
    
    @Test
    public void shouldRecogniseUriAdaptation() throws Exception {
        Selector<?> selector = factory.get("mhs:coverThumbnail#uri");
        assertThat(selector, selector(
                traversal("mhs", "coverThumbnail"))
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
                traversal("dc", "identifier")
                    .withPredicate(uriPrefixPredicate("urn:issn:")))
                .withAdaptation(uriSliceAdaptation(9)));
    }
    
    @Test
    public void shouldRecogniseUriPrefixPredicate() throws Exception {
        Selector<RDFNode> selector = factory.get(
                "!mhs:isIssueOf[uri-prefix='http://miskinhill.com.au/journals/'](~mhs:publicationDate#comparable-lv)")
                .withResultType(RDFNode.class);
        assertThat(selector, selector(
                traversal("mhs", "isIssueOf")
                    .inverse()
                    .withPredicate(uriPrefixPredicate("http://miskinhill.com.au/journals/"))
                    .withSortOrder(selectorComparator(selector(traversal("mhs", "publicationDate"))
                            .withAdaptation(comparableLVAdaptation())).reversed())));
    }
    
    @Test
    public void shouldRecogniseSubscript() throws Exception {
        Selector<String> selector = factory.get(
                "!mhs:isIssueOf(~mhs:publicationDate#comparable-lv)[0]/mhs:coverThumbnail#uri")
                .withResultType(String.class);
        assertThat(selector, selector(
                traversal("mhs", "isIssueOf")
                    .inverse()
                    .withSortOrder(selectorComparator(selector(traversal("mhs", "publicationDate"))
                            .withAdaptation(comparableLVAdaptation())).reversed())
                    .withSubscript(0),
                traversal("mhs", "coverThumbnail"))
                .withAdaptation(uriAdaptation()));
    }
    
    @Test
    public void shouldRecogniseLVAdaptation() throws Exception {
        Selector<Object> selector = factory.get("dc:language/lingvoj:iso1#lv").withResultType(Object.class);
        assertThat(selector, selector(
                traversal("dc", "language"),
                traversal("lingvoj", "iso1"))
                .withAdaptation(lvAdaptation()));
    }
    
    @Test
    public void shouldRecogniseTypePredicate() throws Exception {
        Selector<RDFNode> selector = factory.get("!dc:creator[type=mhs:Review]").withResultType(RDFNode.class);
        assertThat(selector, selector(
                traversal("dc", "creator").inverse().withPredicate(typePredicate("mhs", "Review"))));
    }
    
    @Test
    public void shouldRecogniseAndCombinationOfPredicates() throws Exception {
        Selector<RDFNode> selector = factory.get("!dc:creator[type=mhs:Review and uri-prefix='http://miskinhill.com.au/journals/']").withResultType(RDFNode.class);
        assertThat(selector, selector(
                traversal("dc", "creator").inverse()
                .withPredicate(booleanAndPredicate(
                    typePredicate("mhs", "Review"),
                    uriPrefixPredicate("http://miskinhill.com.au/journals/")))));
    }
    
    @Test
    public void shouldRecogniseUnion() throws Exception {
        Selector<RDFNode> selector = factory.get("!dc:creator | !mhs:translator").withResultType(RDFNode.class);
        assertThat((UnionSelector<RDFNode>) selector, unionSelector(
                selector(traversal("dc", "creator").inverse()),
                selector(traversal("mhs", "translator").inverse())));
    }
    
    @Test
    public void shouldRecogniseMultipleSortSelectors() throws Exception {
        Selector<RDFNode> selector = factory.get("!dc:creator(~dc:isPartOf/mhs:publicationDate#comparable-lv,mhs:startPage#comparable-lv)").withResultType(RDFNode.class);
        assertThat(selector, selector(
                traversal("dc", "creator").inverse()
                .withSortOrder(
                    selectorComparator(selector(traversal("dc", "isPartOf"), traversal("mhs", "publicationDate"))
                        .withAdaptation(comparableLVAdaptation())).reversed(),
                    selectorComparator(selector(traversal("mhs", "startPage")).withAdaptation(comparableLVAdaptation())))));
    }
    
    @Test
    public void shouldRecogniseFormattedDTAdaptation() throws Exception {
        Selector<?> selector = factory.get("dc:created#formatted-dt('d MMMM yyyy')");
        assertThat(selector, selector(traversal("dc", "created"))
                .withAdaptation(formattedDTAdaptation("d MMMM yyyy")));
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
