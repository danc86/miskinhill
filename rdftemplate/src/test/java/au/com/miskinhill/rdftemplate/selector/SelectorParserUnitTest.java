package au.com.miskinhill.rdftemplate.selector;

import static au.com.miskinhill.rdftemplate.selector.AdaptationMatcher.*;
import static au.com.miskinhill.rdftemplate.selector.PredicateMatcher.*;
import static au.com.miskinhill.rdftemplate.selector.SelectorMatcher.selector;
import static au.com.miskinhill.rdftemplate.selector.TraversalMatcher.traversal;

import static org.junit.Assert.assertThat;

import org.junit.Test;

public class SelectorParserUnitTest {
    
    @Test
    public void shouldRecogniseSingleTraversal() throws Exception {
        Selector<?> selector = SelectorParser.parse("dc:creator");
        assertThat(selector, selector(traversal("dc", "creator")));
    }
    
    @Test
    public void shouldRecogniseMultipleTraversals() throws Exception {
        Selector<?> selector = SelectorParser.parse("dc:creator/foaf:name");
        assertThat(selector, selector(
                traversal("dc", "creator"),
                traversal("foaf", "name")));
    }
    
    @Test
    public void shouldRecogniseInverseTraversal() throws Exception {
        Selector<?> selector = SelectorParser.parse("!dc:isPartOf/!dc:isPartOf");
        assertThat(selector, selector(
                traversal("dc", "isPartOf").inverse(),
                traversal("dc", "isPartOf").inverse()));
    }
    
    @Test
    public void shouldRecogniseSortOrder() throws Exception {
        Selector<?> selector = SelectorParser.parse("!mhs:isIssueOf(mhs:publicationDate#comparable-lv)");
        assertThat(selector, selector(
                traversal("mhs", "isIssueOf").inverse()
                .withSortOrder(selector(traversal("mhs", "publicationDate"))
                    .withAdaptation(comparableLVAdaptation()))));
    }
    
    @Test
    public void shouldRecogniseReverseSortOrder() throws Exception {
        Selector<?> selector = SelectorParser.parse("!mhs:isIssueOf(~mhs:publicationDate#comparable-lv)");
        assertThat(selector, selector(
                traversal("mhs", "isIssueOf").inverse()
                .withSortOrder(selector(traversal("mhs", "publicationDate"))
                    .withAdaptation(comparableLVAdaptation()))
                .reverseSorted()));
    }
    
    @Test
    public void shouldRecogniseComplexSortOrder() throws Exception {
        Selector<?> selector = SelectorParser.parse("!mhs:reviews(dc:isPartOf/mhs:publicationDate#comparable-lv)");
        assertThat(selector, selector(
                traversal("mhs", "reviews")
                .withSortOrder(selector(traversal("dc", "isPartOf"), traversal("mhs", "publicationDate"))
                        .withAdaptation(comparableLVAdaptation()))));
    }
    
    @Test
    public void shouldRecogniseUriAdaptation() throws Exception {
        Selector<?> selector = SelectorParser.parse("mhs:coverThumbnail#uri");
        assertThat(selector, selector(
                traversal("mhs", "coverThumbnail"))
                .withAdaptation(uriAdaptation()));
    }
    
    @Test
    public void shouldRecogniseBareUriAdaptation() throws Exception {
        Selector<?> selector = SelectorParser.parse("#uri");
        assertThat(selector, selector().withAdaptation(uriAdaptation()));
    }
    
    @Test
    public void shouldRecogniseUriSliceAdaptation() throws Exception {
        Selector<?> selector = SelectorParser.parse("dc:identifier[uri-prefix='urn:issn:']#uri-slice(9)");
        assertThat(selector, selector(
                traversal("dc", "identifier")
                    .withPredicate(uriPrefixPredicate("urn:issn:")))
                .withAdaptation(uriSliceAdaptation(9)));
    }
    
    @Test
    public void shouldRecogniseUriPrefixPredicate() throws Exception {
        Selector<?> selector = SelectorParser.parse(
                "!mhs:isIssueOf[uri-prefix='http://miskinhill.com.au/journals/'](~mhs:publicationDate#comparable-lv)");
        assertThat(selector, selector(
                traversal("mhs", "isIssueOf")
                    .inverse()
                    .withPredicate(uriPrefixPredicate("http://miskinhill.com.au/journals/"))
                    .withSortOrder(selector(traversal("mhs", "publicationDate"))
                            .withAdaptation(comparableLVAdaptation()))
                    .reverseSorted()));
    }
    
    @Test
    public void shouldRecogniseSubscript() throws Exception {
        Selector<?> selector = SelectorParser.parse(
                "!mhs:isIssueOf(~mhs:publicationDate#comparable-lv)[0]/mhs:coverThumbnail#uri");
        assertThat(selector, selector(
                traversal("mhs", "isIssueOf")
                    .inverse()
                    .withSortOrder(selector(traversal("mhs", "publicationDate"))
                            .withAdaptation(comparableLVAdaptation()))
                    .reverseSorted()
                    .withSubscript(0),
                traversal("mhs", "coverThumbnail"))
                .withAdaptation(uriAdaptation()));
    }
    
    @Test
    public void shouldRecogniseLVAdaptation() throws Exception {
        Selector<?> selector = SelectorParser.parse("dc:language/lingvoj:iso1#lv");
        assertThat(selector, selector(
                traversal("dc", "language"),
                traversal("lingvoj", "iso1"))
                .withAdaptation(lvAdaptation()));
    }
    
    @Test(expected = InvalidSelectorSyntaxException.class)
    public void shouldThrowForInvalidSyntax() throws Exception {
        SelectorParser.parse("dc:creator]["); // this is a parser error
    }
    
    @Test(expected = InvalidSelectorSyntaxException.class)
    public void shouldThrowForUnrecognisedCharacter() throws Exception {
        SelectorParser.parse("dc:cre&ator"); // ... and this is a lexer error
    }
    
}
