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
import com.hp.hpl.jena.vocabulary.DCTerms;
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
import org.springframework.web.context.request.WebRequest;

import au.id.djc.rdftemplate.TemplateInterpolator;

import au.com.miskinhill.rdf.vocabulary.MHS;
import au.com.miskinhill.web.ProperURLCodec;
import au.com.miskinhill.web.exception.NotFoundException;

@Controller
public class ArticlesFeedController extends AbstractAtomFeedController {
    
    private static final URI BASE = URI.create("http://miskinhill.com.au/feeds/articles");
	
    private final Model model;
	private final XMLOutputFactory outputFactory;
	
	@Autowired
	public ArticlesFeedController(Model model, TemplateInterpolator templateInterpolator,
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
	
	@RequestMapping(value = "/feeds/articles", method = RequestMethod.GET)
	public void getArticles(WebRequest request, HttpServletResponse response,
	        @RequestParam(value = "journal", required = false) String journalUri) throws Exception {
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
	    DateTime maxUpdated = Collections.max(entries, AtomEntry.UPDATED_COMPARATOR).getUpdated();
	    if (request.checkNotModified(maxUpdated.getMillis()))
	        return;
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
        addUpdated(events, maxUpdated);
	    for (AtomEntry entry: entries)
	        events.addAll(entry.getEvents());
	    events.add(eventFactory.createEndElement(FEED_QNAME, null));
	    
	    response.setContentType(MediaType.APPLICATION_ATOM_XML.toString());
	    response.setCharacterEncoding("UTF-8");
	    XMLEventWriter destination = outputFactory.createXMLEventWriter(response.getOutputStream(), "UTF-8");
	    for (XMLEvent event: events)
            destination.add(event);
        destination.flush();
	}

}