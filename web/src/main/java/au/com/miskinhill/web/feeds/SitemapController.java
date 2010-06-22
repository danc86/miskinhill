package au.com.miskinhill.web.feeds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.http.MediaType;

import org.springframework.http.ResponseEntity;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import au.com.miskinhill.rdf.RDFUtil;
import au.com.miskinhill.rdf.Representation;
import au.com.miskinhill.rdf.RepresentationFactory;
import au.com.miskinhill.rdf.vocabulary.MHS;
import au.com.miskinhill.schema.sitemaps.Dataset;
import au.com.miskinhill.schema.sitemaps.Url;
import au.com.miskinhill.schema.sitemaps.Urlset;
import au.com.miskinhill.web.util.ResponseUtils;

@Controller
public class SitemapController {
	
	private static final String[] OTHER_URLS = {
		// homepage and /about/ are already in RDF, don't need to list them here
		"http://miskinhill.com.au/contact/",
		"http://miskinhill.com.au/journals/",
		"http://miskinhill.com.au/feeds/issues",
		"http://miskinhill.com.au/feeds/world"
	};
	private static final Pattern OUR_URLS =
		Pattern.compile("http://miskinhill\\.com\\.au/[^#]*");
	private static final String DATA_DUMP_URL = "http://miskinhill.com.au/feeds/world";
	
	private final Model model;
	private final RepresentationFactory representationFactory;
	
	@Autowired
	public SitemapController(Model model, RepresentationFactory representationFactory) {
		this.model = model;
		this.representationFactory = representationFactory;
	}
	
	@RequestMapping(value = "/feeds/sitemap", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Urlset> getSitemap() {
		Urlset urlset = new Urlset();
		for (String loc: OTHER_URLS) {
			urlset.add(new Url(loc));
		}
		List<String> rdfLocs = new ArrayList<String>();
		for (ResIterator i = model.listSubjects(); i.hasNext();) {
			Resource res = (Resource) i.next();
			if (res.getURI() != null && OUR_URLS.matcher(res.getURI()).matches()) {
                for (Representation representation: representationFactory.getRepresentationsForResource(res)) {
                    if (representation.getFormat().equals("html"))
                        rdfLocs.add(res.getURI());
                    else
                        rdfLocs.add(res.getURI() + "." + representation.getFormat());
                }
                if (RDFUtil.hasAnyType(res, Collections.singleton(MHS.IssueContent))
                        && res.getURI().startsWith("http://miskinhill.com.au/journals/"))
                    rdfLocs.add(res.getURI() + ".pdf");
			}
		}
		Collections.sort(rdfLocs);
		for (String loc: rdfLocs) {
			urlset.add(new Url(loc));
		}
		urlset.add(new Dataset(DATA_DUMP_URL));
		return ResponseUtils.createResponse(urlset, MediaType.TEXT_XML);
	}

}
