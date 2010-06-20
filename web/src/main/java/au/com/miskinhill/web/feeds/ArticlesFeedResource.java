package au.com.miskinhill.web.feeds;

import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDF;
import com.sun.jersey.api.NotFoundException;
import org.apache.commons.collections15.ComparatorUtils;
import org.apache.commons.collections15.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.id.djc.rdftemplate.TemplateInterpolator;

import au.com.miskinhill.rdf.vocabulary.MHS;
import au.com.miskinhill.web.ProperURLCodec;

@Component
@Path("/feeds/articles")
public class ArticlesFeedResource extends AbstractAtomFeedResource {
    
    private static final URI BASE = URI.create("http://miskinhill.com.au/feeds/articles");
	
    private final Model model;
	private final XMLOutputFactory outputFactory;
	
	@Autowired
	public ArticlesFeedResource(Model model, TemplateInterpolator templateInterpolator,
	        XMLOutputFactory outputFactory, XMLEventFactory eventFactory) {
	    super(templateInterpolator, eventFactory);
		this.model = model;
		this.outputFactory = outputFactory;
	}
	
	private static final class ArticleFilter extends Filter<Resource> {
	    private final Resource journal;
	    public ArticleFilter(Resource journal) {
            this.journal = journal;
        }
	    @Override
	    public boolean accept(Resource o) {
	        Resource issue = o.getRequiredProperty(DCTerms.isPartOf).getObject().as(Resource.class);
            if (!issue.getURI().startsWith("http://miskinhill.com.au/journals/")) return false;
            if (journal != null && !issue.hasProperty(MHS.isIssueOf, journal)) return false;
            return true;
	    }
	}
	
	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public String getIssues(@QueryParam("journal") String journalUri) throws XMLStreamException {
	    Resource journal = null;
	    if (journalUri != null) {
	        journalUri = BASE.resolve(journalUri).toString();
	        journal = model.createResource(BASE.resolve(journalUri).toString());
	    }
	    List<AtomEntry> entries = new ArrayList<AtomEntry>();
	    for (Resource article: model.listSubjectsWithProperty(RDF.type, MHS.Article).filterKeep(new ArticleFilter(journal)).toList())
	        entries.add(renderEntry(article));
	    if (entries.isEmpty())
	        throw new NotFoundException("No articles exist for journal " + journalUri);
	    Collections.sort(entries, ComparatorUtils.reversedComparator(AtomEntry.PUBLISHED_COMPARATOR));
	    
	    List<XMLEvent> events = new ArrayList<XMLEvent>();
	    events.add(eventFactory.createStartElement(FEED_QNAME, null,
	            IteratorUtils.singletonIterator(eventFactory.createNamespace(ATOM_NS))));
	    if (journal != null) {
	        addTitleElement(events, journal.getRequiredProperty(DC.title).getObject().as(Literal.class).getString() + " journal articles");
	        addIdAndSelfLinkElements(events, "http://miskinhill.com.au/feeds/articles?journal=" + ProperURLCodec.encodeUrl(journalUri));
	    } else {
	        addTitleElement(events, "Miskin Hill journal articles");
	        addIdAndSelfLinkElements(events, "http://miskinhill.com.au/feeds/articles");
	    }
	    addUpdated(events, Collections.max(entries, ComparatorUtils.reversedComparator(AtomEntry.UPDATED_COMPARATOR)).getUpdated());
	    for (AtomEntry entry: entries)
	        events.addAll(entry.getEvents());
	    events.add(eventFactory.createEndElement(FEED_QNAME, null));
	    
	    StringWriter writer = new StringWriter();
	    XMLEventWriter destination = outputFactory.createXMLEventWriter(writer);
	    for (XMLEvent event: events)
            destination.add(event);
        destination.flush();
        return writer.toString();
	}

}
