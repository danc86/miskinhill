package au.com.miskinhill.domain.vocabulary;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class MHS {

	public static final String NS_URI = "http://miskinhill.com.au/rdfschema/1.0/";
	
	public static final Resource Journal = ResourceFactory.createResource(
			NS_URI + "Journal");
	public static final Resource Issue = ResourceFactory.createResource(
			NS_URI + "Issue");
	public static final Resource IssueContent = ResourceFactory.createResource(
			NS_URI + "IssueContent");
	public static final Resource Article = ResourceFactory.createResource(
			NS_URI + "Article");
	public static final Resource Book = ResourceFactory.createResource(
			NS_URI + "Book");
	public static final Resource Review = ResourceFactory.createResource(
			NS_URI + "Review");
	public static final Resource Author = ResourceFactory.createResource(
			NS_URI + "Author");
	public static final Resource Institution = ResourceFactory.createResource(
			NS_URI + "Institution");
	
}
