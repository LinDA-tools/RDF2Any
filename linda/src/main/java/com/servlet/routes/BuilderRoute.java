package com.servlet.routes;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.lucene.queryparser.classic.ParseException;

import com.servlet.Main;

import de.unibonn.iai.eis.linda.converters.impl.JSONConverter;
import de.unibonn.iai.eis.linda.converters.impl.results.JSONOutput;
import de.unibonn.iai.eis.linda.helper.SPARQLHandler;
import de.unibonn.iai.eis.linda.helper.output.JSONError;
import de.unibonn.iai.eis.linda.helper.output.ResultOK;
import de.unibonn.iai.eis.linda.querybuilder.search.ClassSearch;
import de.unibonn.iai.eis.linda.querybuilder.ObjectSearch;
import de.unibonn.iai.eis.linda.querybuilder.classes.MultipleRDFClass;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClassProperty;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClassSummary;
import de.unibonn.iai.eis.linda.querybuilder.output.ClassPropertyOutput;

@Path("/v1.0/builder/")
public class BuilderRoute {
	// This route is for the free text search of classes
	@GET
	@Path("classes")
	@Produces(MediaType.APPLICATION_JSON)
	public Object getClasses(@Context UriInfo uriInfo) {
		try{
			MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
			String search = queryParams.getFirst("search");
			String dataset = queryParams.getFirst("dataset");
			Boolean forceUriSearch =  Boolean.valueOf(queryParams.getFirst("force_uri_search"));

			System.out.println("START Searching for classes matching '" + search + "' in dataset '" + dataset + "' on " + (forceUriSearch ? "URIs" : "labels"));
			
			ClassSearch searchClasses = new ClassSearch(dataset, search);
			searchClasses.generateSearchedClassItems(forceUriSearch);
			System.out.println("FINISHED Searching for classes matching '"+ search + "' in dataset '" + dataset + "'");
			
			return searchClasses;
		}catch(Exception e){
			System.out.println("Error : "+e.toString());
			return new JSONError("ERROR", e.toString());
		}
	}
	
	/**
	 * get all classes in a dataset
	 * @param uriInfo 
	 * @return all classes in a dataset
	 */
	@GET
	@Path("classes/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Object getAllClasses(@Context UriInfo uriInfo) {
		try{
			MultivaluedMap<String, String> queryParams = uriInfo
					.getQueryParameters();
			String dataset = queryParams.getFirst("dataset");
			System.out.println("START Searching for all classes in dataset '"
					+ dataset + "'");
			ClassSearch searchClasses = new ClassSearch(dataset);
			searchClasses.generateAllClassItems();
			System.out
					.println("FINISHED Searching for all classes in dataset '"
							+ dataset + "'");
			return searchClasses;
		}catch(Exception e){
			System.out.println("Error : "+e.toString());
			return new JSONError("ERROR", e.toString());
		}
	}
	
	// This route is for returning examples of a class
	@GET
	@Path("classes/examples")
	@Produces(MediaType.APPLICATION_JSON)
	public Object getClassesExamples(@Context UriInfo uriInfo) {
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		String dataset = queryParams.getFirst("dataset");
		String classUri = queryParams.getFirst("class");
		String strLimit = queryParams.getFirst("limit");
		Integer limit = 10;
		if (strLimit != null) {
			try {
				limit = Integer.parseInt(strLimit);
			} catch (Exception e) {
			}
		}
		System.out.println("Start looking for example items for class "+ classUri + " (" + dataset + ")");
		RDFClassSummary rdfClassSummary = new RDFClassSummary(dataset, classUri);
		rdfClassSummary.generateSummaryItems(limit);
		return rdfClassSummary;
	}

	// This route is for returning subclasses of a class
	@GET
	@Path("classes/subclasses")
	@Produces(MediaType.APPLICATION_JSON)
	public Object getClassesSubclasses(@Context UriInfo uriInfo) {
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		String dataset = queryParams.getFirst("dataset");
		String classUri = queryParams.getFirst("class");
		System.out.println("Start looking for subclasses for class " + classUri + " (" + dataset + ")");
		RDFClass rdfClass = Main.addOrGetCachedClass(classUri, new RDFClass(dataset, classUri));
		return rdfClass.getSubclassesHashMap();
	}
	
	@GET
	@Path("classes/label")
	@Produces(MediaType.APPLICATION_JSON)
	public Object getLabelForClass(@Context UriInfo uriInfo) throws ParseException {
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		String dataset = queryParams.getFirst("dataset");
		String classUri = queryParams.getFirst("class");
		
		String label = Main.addOrGetCachedLabel(classUri, null);
		if (label == null){
			RDFClass rdfClass = Main.addOrGetCachedClass(classUri, RDFClass.searchRDFClass(dataset, classUri));
			if (rdfClass.label != null){
				label = Main.addOrGetCachedLabel(classUri, rdfClass.label);
			}
		}
		
		String retStr = "{";
		
		if (label != null)
			retStr += "\"label\" :\""+ 
					label.substring(0, 1).toUpperCase() 
					+ label.substring(1) +"\"";
		
		retStr += "}";
		
		return retStr;
	}
	
	// This route is for the free text search of objects
	@GET
	@Path("objects")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONOutput getObjects(@Context UriInfo uriInfo) {
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		String search = queryParams.getFirst("search");
		String dataset = queryParams.getFirst("dataset");
		String classes = queryParams.getFirst("classes");
		String forClass = queryParams.getFirst("for_class");
		String forProperty = queryParams.getFirst("for_property");
		String objectQuery = null;
		if (forClass != null && !forClass.equalsIgnoreCase("")
				&& forProperty != null && !forProperty.equalsIgnoreCase("")) {
			System.out.println("START Searching for objects of classes '"
					+ classes + "' having subject class '" + forClass
					+ "' matching '" + search + "' in dataset '" + dataset
					+ "'");
			objectQuery = new ObjectSearch(dataset, search, classes, forClass,
					forProperty).getSPARQLQuery();
		} else {
			System.out.println("START Searching for objects of classes '"
					+ classes + "' matching '" + search + "' in dataset '"
					+ dataset + "'");
			objectQuery = new ObjectSearch(dataset, search, classes)
					.getSPARQLQuery();
		}
		Double startMilliseconds = (double) System.currentTimeMillis();
		JSONConverter converter = new JSONConverter(SPARQLHandler.executeQuery(
				dataset, objectQuery));
		Double endMilliseconds = (double) System.currentTimeMillis();
		converter.jsonOutput
				.setTimeTaken((endMilliseconds - startMilliseconds) / 1000);
		System.out.println("FINISH searching for objects ... ");
		return converter.jsonOutput;
	}

	// this route is for getting properties
	@GET
	@Path("properties")
	@Produces(MediaType.APPLICATION_JSON)
	public ClassPropertyOutput getProperties(@Context UriInfo uriInfo)
			throws IOException, ParseException {
		try{
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		String dataset = queryParams.getFirst("dataset");
		
		int noClasses = queryParams.get("class").size();
		if (noClasses == 1){
			String classUri = queryParams.getFirst("class");
			RDFClass rdfClass = Main.addOrGetCachedClass(classUri, null);
			if ((rdfClass == null) || (rdfClass.properties.size() == 0)){
				rdfClass = Main.addOrGetCachedClass(classUri,RDFClass.searchRDFClass(dataset, classUri));
			}
			return new ClassPropertyOutput(rdfClass);
		} else {
			MultipleRDFClass multi = new MultipleRDFClass();
			List<String> classes = queryParams.get("class");
			for(String clazz : classes){
				RDFClass rdfClass = Main.addOrGetCachedClass(clazz, null);
				if ((rdfClass == null) || (rdfClass.properties.size() == 0)){
					rdfClass = Main.addOrGetCachedClass(clazz,RDFClass.searchRDFClass(dataset, clazz));
				}				
				multi.addRDFClass(rdfClass);
			}
			multi.generatePropertyVector();
			return new ClassPropertyOutput(multi);
		}
		} catch(Exception e){
			System.out.println("Error : "+e.getMessage());
			return null;
		}
	}
	
	@GET
	@Path("properties/label")
	@Produces(MediaType.APPLICATION_JSON)
	public Object getLabelForProperty(@Context UriInfo uriInfo) throws ParseException {
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		String dataset = queryParams.getFirst("dataset");
		String classUri = queryParams.getFirst("class");
		String propertyUri = queryParams.getFirst("property");
		
		String label = Main.addOrGetCachedLabel(propertyUri, null);
		if (label == null){
			RDFClass rdfClass = Main.addOrGetCachedClass(classUri, null);
			if (rdfClass == null){
				rdfClass = Main.addOrGetCachedClass(classUri,RDFClass.searchRDFClass(dataset, classUri));
			}
			
			RDFClassProperty prop = rdfClass.getPropertyFromStringUri(propertyUri);

			if (prop.label != null){
				label = Main.addOrGetCachedLabel(propertyUri, prop.label);
			}
		}
		
		String retStr = "{";
		
		if (!label.equals(""))
			retStr += "\"label\" :\""+ 
					label.substring(0, 1).toUpperCase() 
					+ label.substring(1) +"\"";
		
		retStr += "}";
		
		return retStr;
	}
	
	@GET
	@Path("properties/indexes/reindex")
	@Produces(MediaType.APPLICATION_JSON)
	public ClassPropertyOutput getPropertiesReindex(@Context UriInfo uriInfo)
			throws IOException, ParseException {
		try{
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		String dataset = queryParams.getFirst("dataset");
		String classUri = queryParams.getFirst("class");
		RDFClass rdfClass = RDFClass.reindexRDFClass(dataset, classUri);
		// rdfClass.generatePropertiesFromSPARQL();
		return new ClassPropertyOutput(rdfClass);
		} catch(Exception e){
			System.out.println("Error : "+e.getMessage());
			return null;
		}
	}

	// this route is for getting properties
	@GET
	@Path("properties/indexes/create")
	@Produces(MediaType.APPLICATION_JSON)
	public ResultOK getPropertiesIndexesCreate(@Context UriInfo uriInfo)
			throws IOException, ParseException {
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		String dataset = queryParams.getFirst("dataset");
		RDFClass.generateIndexesForDataset(dataset);
		// rdfClass.generatePropertiesFromSPARQL();
		return new ResultOK();
	}
	
	@GET
	@Path("properties/indexes/create/localdbpedia")
	@Produces(MediaType.APPLICATION_JSON)
	public ResultOK getPropertiesIndexesCreateLocalDBpedia(@Context UriInfo uriInfo)
			throws IOException, ParseException {
		
		try{
		RDFClass.generateIndexesForDBPedia();
		return new ResultOK();
		} catch(Exception e){
			System.out.println("Error : "+e.getMessage());
			return null;
		}
	}
	
	@GET
	@Path("properties/indexes/reindex/localdbpedia")
	@Produces(MediaType.APPLICATION_JSON)
	public ClassPropertyOutput getPropertiesReindexLocalDBpedia(@Context UriInfo uriInfo)
			throws IOException, ParseException {
		try{
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		String classUri = queryParams.getFirst("class");
		RDFClass rdfClass = RDFClass.reindexLocalDBPediaRDFClass(classUri);
		// rdfClass.generatePropertiesFromSPARQL();
		return new ClassPropertyOutput(rdfClass);
		} catch(Exception e){
			System.out.println("Error : "+e.getMessage());
			return null;
		}
	}

}
