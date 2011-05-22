package au.com.miskinhill.rdf;

import static java.lang.Math.*;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import au.id.djc.rdftemplate.selector.SelectorFactory;

import au.com.miskinhill.rdf.vocabulary.MHS;

@Component
public class BibtexRepresentation implements Representation {
    
    private final MediaType contentType = new MediaType("text", "x-bibtex");
    private final EnumSet<ShownIn> shownIn = EnumSet.of(ShownIn.HTMLAnchors, ShownIn.HTMLLinks, ShownIn.AtomLinks, ShownIn.Unapi);
    private final Set<Resource> types = Collections.singleton(MHS.Article);
    private final SelectorFactory selectorFactory;
    
    @Autowired
    public BibtexRepresentation(SelectorFactory s) {
        this.selectorFactory = s;
    }
    
    @Override
    public boolean canRepresent(Resource resource) {
        return RDFUtil.hasAnyType(resource, types);
    }

    @Override
    public MediaType getContentType() {
        return contentType;
    }
    
    @Override
    public Collection<MediaType> getContentTypeAliases() {
        return Collections.emptySet();
    }
    
    @Override
    public String getFormat() {
        return "bib";
    }
    
    @Override
    public int getOrder() {
        return 5;
    }
    
    @Override
    public String getLabel() {
        return "BibTeX";
    }
    
    @Override
    public String getDocs() {
        return "http://en.wikipedia.org/wiki/BibTeX";
    }
    
    @Override
    public boolean isShownIn(ShownIn place) {
        return shownIn.contains(place);
    }

    @Override
    public String render(Resource article) {
        LinkedHashMap<String, String> attributes = new LinkedHashMap<String, String>();
        attributes.put("journal", s("dc:isPartOf/mhs:isIssueOf/dc:title#string-lv", article));
        attributes.put("author", StringUtils.join(ss("dc:creator/foaf:name#string-lv", article), " and "));
        attributes.put("title", s("dc:title#string-lv", article).replaceAll("(.)(\\p{Lu}+)", "$1{$2}"));
        if (has("dc:isPartOf/mhs:isIssueOf/dc:identifier[uri-prefix='urn:issn:']", article)) {
            attributes.put("issn", s("dc:isPartOf/mhs:isIssueOf/dc:identifier[uri-prefix='urn:issn:']#uri-slice(9)", article));
        }
        if (has("dc:isPartOf/mhs:isIssueOf/dc:publisher", article)) {
            attributes.put("publisher", nameOrLiteral("dc:isPartOf/mhs:isIssueOf/dc:publisher", article));
        }
        if (has("dc:isPartOf/mhs:volume", article)) {
            attributes.put("volume", s("dc:isPartOf/mhs:volume#string-lv", article));
        }
        if (has("dc:isPartOf/mhs:issueNumber", article)) {
            attributes.put("number", s("dc:isPartOf/mhs:issueNumber#string-lv", article));
        }
        if (has("dc:isPartOf/dc:coverage", article)) {
            String coverage = s("dc:isPartOf/dc:coverage#string-lv", article);
            attributes.put("year", coverage.substring(max(coverage.length() - 4, 0), coverage.length()));
        }
        if (has("mhs:startPage", article) && has("mhs:endPage", article)) {
            attributes.put("pages", s("mhs:startPage#string-lv", article) +
                    "\u2013" + s("mhs:endPage#string-lv", article));
        }
        if (article.getURI().startsWith("http://miskinhill.com.au/journals/")) {
            attributes.put("url", article.getURI());
        } else if (has("mhs:availableFrom", article)) {
            attributes.put("url", s("mhs:availableFrom#uri", article));
        }
        return bibtex(id(article), attributes);
    }
    
    /** Exposed for testing. */
    String id(Resource article) {
        String start;
        if (!selectorFactory.get("dc:creator").result(article).isEmpty()) {
            String surname = selectorFactory.get("dc:creator(foaf:surname#comparable-lv)[0]/foaf:surname#string-lv").withResultType(String.class).singleResult(article);
            start = surname.replaceAll("\\P{LC}", "");
        } else {
            String title = selectorFactory.get("dc:title#string-lv").withResultType(String.class).singleResult(article);
            start = title.substring(0, min(title.length(), 32)).replaceAll("\\P{LC}", "");
        }
        String coverage = firstOrEmpty(selectorFactory.get("dc:isPartOf/dc:coverage#string-lv").withResultType(String.class).result(article));
        return start + coverage.substring(max(coverage.length() - 4, 0), coverage.length());
    }
    
    private String firstOrEmpty(Collection<String> strings) {
        if (strings.isEmpty()) return "";
        return strings.iterator().next();
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
    
    private List<String> ss(String expression, RDFNode node) {
        return selectorFactory.get(expression).withResultType(String.class).result(node);
    }
    
    private String bibtex(String id, Map<String, String> attributes) {
        StringBuilder sb = new StringBuilder();
        sb.append("@article{");
        sb.append(id);
        sb.append(",\n");
        for (Iterator<Map.Entry<String, String>> it = attributes.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, String> attribute = it.next();
            sb.append("    ");
            sb.append(attribute.getKey());
            sb.append(" = \"");
            sb.append(attribute.getValue().replace("\\", "\\\\").replace("\"", "\\\""));
            sb.append("\"");
            if (it.hasNext())
                sb.append(",");
            sb.append("\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

}
