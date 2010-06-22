package au.com.miskinhill.web.feeds;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.XMLEvent;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import org.apache.commons.collections15.ComparatorUtils;
import org.apache.commons.collections15.IteratorUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import au.id.djc.rdftemplate.TemplateInterpolator;

import au.com.miskinhill.rdf.vocabulary.MHS;
import au.com.miskinhill.web.ProperURLCodec;
import au.com.miskinhill.web.exception.NotFoundException;

@Controller
public class IssuesFeedController extends AbstractAtomFeedController {
    
    private static final URI BASE = URI.create("http://miskinhill.com.au/feeds/issues");
	
    private final Model model;
	private final XMLOutputFactory outputFactory;
	
	@Autowired
	public IssuesFeedController(Model model, TemplateInterpolator templateInterpolator,
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
	
	@RequestMapping(value = "/feeds/issues", method = RequestMethod.GET)
	public void getIssues(HttpServletResponse response,
	        @RequestParam(value = "journal", required = false) String journalUri) throws Exception {
	    Resource journal = null;
	    if (journalUri != null) {
	        journalUri = BASE.resolve(journalUri).toString();
	        journal = model.createResource(journalUri);
	    }
	    List<AtomEntry> entries = new ArrayList<AtomEntry>();
	    for (Resource issue: model.listSubjectsWithProperty(RDF.type, MHS.Issue).filterKeep(new IssueFilter(journal)).toList())
	        entries.add(renderEntry(issue));
	    if (entries.isEmpty())
	        throw new NotFoundException("No issues exist for journal " + journalUri);
	    Collections.sort(entries, ComparatorUtils.reversedComparator(AtomEntry.PUBLISHED_COMPARATOR));
	    
	    List<XMLEvent> events = new ArrayList<XMLEvent>();
	    events.add(eventFactory.createStartElement(FEED_QNAME, null,
	            IteratorUtils.singletonIterator(eventFactory.createNamespace(ATOM_NS))));
	    if (journal != null) {
	        addTitleElement(events, journal.getRequiredProperty(DC.title).getObject().as(Literal.class).getString() + " journal issues");
	        addIdAndSelfLinkElements(events, "http://miskinhill.com.au/feeds/issues?journal=" + ProperURLCodec.encodeUrl(journalUri));
	    } else {
	        addTitleElement(events, "Miskin Hill journal issues");
	        addIdAndSelfLinkElements(events, "http://miskinhill.com.au/feeds/issues");
	    }
	    DateTime maxUpdated = Collections.max(entries, AtomEntry.UPDATED_COMPARATOR).getUpdated();
        addUpdated(events, maxUpdated);
	    for (AtomEntry entry: entries)
	        events.addAll(entry.getEvents());
	    events.add(eventFactory.createEndElement(FEED_QNAME, null));
	    
        response.setContentType(MediaType.APPLICATION_ATOM_XML.toString());
        response.setDateHeader("Last-Modified", maxUpdated.getMillis());
        response.setCharacterEncoding("UTF-8");
        XMLEventWriter destination = outputFactory.createXMLEventWriter(response.getOutputStream(), "UTF-8");
        for (XMLEvent event: events)
            destination.add(event);
        destination.flush();
	}

}
