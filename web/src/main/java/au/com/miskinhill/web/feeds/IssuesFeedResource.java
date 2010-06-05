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

import com.hp.hpl.jena.vocabulary.DC;

import com.sun.jersey.api.NotFoundException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.RDF;
import org.apache.commons.collections15.ComparatorUtils;
import org.apache.commons.collections15.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.id.djc.rdftemplate.TemplateInterpolator;

import au.com.miskinhill.rdf.vocabulary.MHS;

@Component
@Path("/feeds/issues")
public class IssuesFeedResource extends AbstractAtomFeedResource {
    
    private static final URI BASE = URI.create("http://miskinhill.com.au/feeds/issues");
	
    private final Model model;
	private final XMLOutputFactory outputFactory;
	
	@Autowired
	public IssuesFeedResource(Model model, TemplateInterpolator templateInterpolator,
	        XMLOutputFactory outputFactory, XMLEventFactory eventFactory) {
	    super(templateInterpolator, eventFactory);
		this.model = model;
		this.outputFactory = outputFactory;
	}
	
	private static final class IssueFilter extends Filter<Resource> {
	    private final Resource journal;
	    public IssueFilter(Resource journal) {
            this.journal = journal;
        }
	    @Override
	    public boolean accept(Resource o) {
            if (!o.getURI().startsWith("http://miskinhill.com.au/journals/")) return false;
            if (journal != null && !o.hasProperty(MHS.isIssueOf, journal)) return false;
            return true;
	    }
	}
	
	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public String getIssues(@QueryParam("journal") String journalUri) throws XMLStreamException {
	    Resource journal = null;
	    if (journalUri != null)
	        journal = model.createResource(BASE.resolve(journalUri).toString());
	    List<AtomEntry> entries = new ArrayList<AtomEntry>();
	    for (Resource issue: model.listSubjectsWithProperty(RDF.type, MHS.Issue).filterKeep(new IssueFilter(journal)).toList())
	        entries.add(renderEntry(issue));
	    if (entries.isEmpty())
	        throw new NotFoundException("No issues exist for journal " + journalUri);
	    Collections.sort(entries, ComparatorUtils.reversedComparator(AtomEntry.PUBLISHED_COMPARATOR));
	    
	    List<XMLEvent> events = new ArrayList<XMLEvent>();
	    events.add(eventFactory.createStartElement(FEED_QNAME, null,
	            IteratorUtils.singletonIterator(eventFactory.createNamespace(ATOM_NS))));
	    if (journal != null)
	        addTitleElement(events, journal.getRequiredProperty(DC.title).getObject().as(Literal.class).getString() + " journal issues");
	    else
	        addTitleElement(events, "Miskin Hill journal issues");
	    addIdAndSelfLinkElements(events, "http://miskinhill.com.au/feeds/issues");
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
