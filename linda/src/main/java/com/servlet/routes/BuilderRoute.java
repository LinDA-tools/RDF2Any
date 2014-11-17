package com.servlet.routes;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.lucene.queryparser.classic.ParseException;

import de.unibonn.iai.eis.linda.converters.impl.JSONConverter;
import de.unibonn.iai.eis.linda.converters.impl.results.JSONOutput;
import de.unibonn.iai.eis.linda.helper.SPARQLHandler;
import de.unibonn.iai.eis.linda.helper.output.JSONError;
import de.unibonn.iai.eis.linda.helper.output.ResultOK;
import de.unibonn.iai.eis.linda.querybuilder.ClassSearch;
import de.unibonn.iai.eis.linda.querybuilder.ObjectSearch;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;
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
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		String search = queryParams.getFirst("search");
		String dataset = queryParams.getFirst("dataset");
		System.out.println("START Searching for classes matching '" + search
				+ "' in dataset '" + dataset + "'");
		Double startMilliseconds = (double) System.currentTimeMillis();
		JSONConverter converter = new JSONConverter(SPARQLHandler.executeQuery(
				dataset, new ClassSearch(dataset, search).getSPARQLQuery()));
		Double endMilliseconds = (double) System.currentTimeMillis();
		converter.jsonOutput
				.setTimeTaken((endMilliseconds - startMilliseconds) / 1000);
		System.out.println("FINISH searching for classes ... ");
		return converter.jsonOutput;
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
		Integer limit = 5;
		if (strLimit != null) {
			try {
				limit = Integer.parseInt(strLimit);
			} catch (Exception e) {
			}
		}
		System.out.println("Start looking for example items for class "
				+ classUri + " (" + dataset + ")");
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
		System.out.println("Start looking for subclasses for class "
				+ classUri + " (" + dataset + ")");
		RDFClass rdfClass = new RDFClass(dataset, classUri);
		return rdfClass.getSubclassesHashMap();
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
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		String dataset = queryParams.getFirst("dataset");
		String classUri = queryParams.getFirst("class");
		RDFClass rdfClass = RDFClass.searchRDFClass(dataset, classUri);
		// rdfClass.generatePropertiesFromSPARQL();
		return new ClassPropertyOutput(rdfClass);
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

}
