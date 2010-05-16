package au.com.miskinhill.rdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.id.djc.rdftemplate.selector.SelectorFactory;

import au.com.miskinhill.rdf.vocabulary.MHS;

@Component
public class EndnoteRepresentation implements Representation {
    
    private static final MediaType CONTENT_TYPE = new MediaType("application", "x-endnote-refer");
    private final Set<Resource> types = Collections.singleton(MHS.Article);
    private final SelectorFactory selectorFactory;
    
    @Autowired
    public EndnoteRepresentation(SelectorFactory s) {
        this.selectorFactory = s;
    }
    
    @Override
    public boolean canRepresent(Resource resource) {
        return RDFUtil.hasAnyType(resource, types);
    }

    @Override
    public MediaType getContentType() {
        return CONTENT_TYPE;
    }
    
    @Override
    public String getFormat() {
        return "end";
    }
    
    @Override
    public int getOrder() {
        return 6;
    }
    
    @Override
    public String getLabel() {
        return "Endnote";
    }
    
    @Override
    public String getDocs() {
        return "http://www.harzing.com/pophelp/exporting.htm";
    }

    @Override
    public String render(Resource article) {
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry("O", "Journal Article"));
        entries.add(new Entry("T", s("dc:title#string-lv", article)));
        for (RDFNode author: ns("dc:creator", article)) {
            entries.add(new Entry("A", s("foaf:surname#string-lv", author) +
                    ", " + s("foaf:givenNames#string-lv", author)));
        }
        entries.add(new Entry("J", s("dc:isPartOf/mhs:isIssueOf/dc:title#string-lv", article)));
        if (has("dc:isPartOf/dc:coverage", article)) {
            entries.add(new Entry("D", s("dc:isPartOf/dc:coverage#string-lv", article)));
        }
        if (has("dc:isPartOf/mhs:volume", article)) {
            entries.add(new Entry("V", s("dc:isPartOf/mhs:volume#string-lv", article)));
        }
        if (has("dc:isPartOf/mhs:issueNumber", article)) {
            entries.add(new Entry("N", s("dc:isPartOf/mhs:issueNumber#string-lv", article)));
        }
        if (has("dc:isPartOf/mhs:isIssueOf/dc:publisher", article)) {
            entries.add(new Entry("I", nameOrLiteral("dc:isPartOf/mhs:isIssueOf/dc:publisher", article)));
        }
        if (has("dc:isPartOf/mhs:isIssueOf/dc:identifier[uri-prefix='urn:issn:']", article)) {
            entries.add(new Entry("@", s("dc:isPartOf/mhs:isIssueOf/dc:identifier[uri-prefix='urn:issn:']#uri-slice(9)", article)));
        }
        if (article.getURI().startsWith("http://miskinhill.com.au/journals/")) {
            entries.add(new Entry("U", article.getURI()));
        } else if (has("mhs:availableFrom", article)) {
            entries.add(new Entry("U", s("mhs:availableFrom#uri", article)));
        }
        if (has("mhs:startPage", article) && has("mhs:endPage", article)) {
            entries.add(new Entry("P", s("mhs:startPage#string-lv", article) +
                    "\u2013" + s("mhs:endPage#string-lv", article)));
        }
        return endnote(entries);
    }
    
    private static final class Entry {
        private final String key;
        private final String value;
        public Entry(String key, String value) {
            this.key = key; this.value = value;
        }
        public String getKey() { return key; }
        public String getValue() { return value; }
    }
    
    private String nameOrLiteral(String expression, RDFNode node) {
        RDFNode result = selectorFactory.get(expression).withResultType(RDFNode.class).singleResult(node);
        if (result.isLiteral())
            return result.as(Literal.class).getValue().toString();
        return s("foaf:name#string-lv", result);
    }
    
    private boolean has(String expression, RDFNode node) {
        return !selectorFactory.get(expression).result(node).isEmpty();
    }
    
    private String s(String expression, RDFNode node) {
        return selectorFactory.get(expression).withResultType(String.class).singleResult(node);
    }
    
    private List<RDFNode> ns(String expression, RDFNode node) {
        return selectorFactory.get(expression).withResultType(RDFNode.class).result(node);
    }
    
    private String endnote(List<Entry> entries) {
        StringBuilder sb = new StringBuilder();
        for (Entry entry: entries) {
            sb.append("%");
            sb.append(entry.getKey());
            sb.append(" ");
            sb.append(entry.getValue().replaceAll("\\s+", " "));
            sb.append("\n");
        }
        return sb.toString();
    }

}
