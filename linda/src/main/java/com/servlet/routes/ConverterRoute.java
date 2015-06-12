package com.servlet.routes;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.lucene.queryparser.classic.ParseException;

import com.sun.org.apache.bcel.internal.generic.NEW;

import de.unibonn.iai.eis.linda.converters.impl.CSVConverter;
import de.unibonn.iai.eis.linda.converters.impl.ConfiguredConverter;
import de.unibonn.iai.eis.linda.converters.impl.JSONConverter;
import de.unibonn.iai.eis.linda.converters.impl.RDBConverter;
import de.unibonn.iai.eis.linda.converters.impl.RDFConverter;
import de.unibonn.iai.eis.linda.converters.impl.results.JSONOutput;
import de.unibonn.iai.eis.linda.example.SPARQLExample;
import de.unibonn.iai.eis.linda.helper.CommonHelper;
import de.unibonn.iai.eis.linda.helper.InstanceExporter;
import de.unibonn.iai.eis.linda.helper.OutputStreamHandler;
import de.unibonn.iai.eis.linda.helper.SPARQLHandler;
import de.unibonn.iai.eis.linda.helper.output.JSONError;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;

@Path("/v1.0/convert/")
public class ConverterRoute {
	@GET
	@Path("csv-converter.csv")
	@Produces({ "application/csv" })
	public StreamingOutput getCSVConverter(@Context UriInfo uriInfo)
			throws UnsupportedEncodingException {
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		String query = queryParams.getFirst("query");
		String dataset = queryParams.getFirst("dataset");
		String forClass = queryParams.getFirst("for_class");
		String properties = queryParams.getFirst("properties");
		Boolean generateOntology = (queryParams.getFirst("generateOntology") == null) ? false : Boolean.parseBoolean(queryParams.getFirst("generateOntology"));
			
		if (forClass == null || forClass.equals("")) {
			System.out.println("START CSV conversion for query in dataset "
					+ dataset + " \n" + query);
			return OutputStreamHandler.getConverterStreamingOutput(
					new CSVConverter(), dataset, query, generateOntology);
		} else {
			System.out.println("START CSV conversion for query in dataset "
					+ dataset + " \n" + query);
			return OutputStreamHandler.getConverterStreamingOutput(
					new CSVConverter(), dataset, query, forClass, properties, generateOntology);
		}

	}

	@GET
	@Path("rdb-converter.sql")
	@Produces({ "application/sql" })
	public StreamingOutput getRDBConverter(@Context UriInfo uriInfo)
			throws UnsupportedEncodingException {
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		String query = queryParams.getFirst("query");
		String dataset = queryParams.getFirst("dataset");
		String forClass = queryParams.getFirst("for_class");
		String properties = queryParams.getFirst("properties");
		Boolean generateOntology = (queryParams.getFirst("generateOntology") == null) ? false : Boolean.parseBoolean(queryParams.getFirst("generateOntology"));
		
		if (forClass != null) {
			System.out.println("START RDB conversion for query of class ("
					+ forClass + ") in dataset " + dataset + " \n" + query);
			return OutputStreamHandler.getConverterStreamingOutput(
					new RDBConverter(), dataset, query, forClass, properties, generateOntology);
		} else {
			System.out.println("START RDB conversion for query in dataset "
					+ dataset + " \n" + query);
			return OutputStreamHandler.getConverterStreamingOutput(
					new RDBConverter(), dataset, query, generateOntology);
		}
	}
	
	@GET
	@Path("rdf-converter.ttl")
	@Produces({ "text/turtle" })
	public StreamingOutput getRDFConverter(@Context UriInfo uriInfo)
			throws UnsupportedEncodingException {
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		String query = queryParams.getFirst("query");
		String dataset = queryParams.getFirst("dataset");
		String forClass = queryParams.getFirst("for_class");
		String properties = queryParams.getFirst("properties");
		Boolean generateOntology = (queryParams.getFirst("generateOntology") == null) ? false : Boolean.parseBoolean(queryParams.getFirst("generateOntology"));
		
		if (forClass != null) {
			System.out.println("START RDF conversion for query of class ("
					+ forClass + ") in dataset " + dataset + " \n" + query);
			return OutputStreamHandler.getConverterStreamingOutput(
					new RDFConverter(), dataset, query, forClass, properties, generateOntology);
		} else {
			System.out.println("START RDF conversion for query in dataset "
					+ dataset + " \n" + query);
			return OutputStreamHandler.getConverterStreamingOutput(
					new RDFConverter(), dataset, query, generateOntology);
		}
	}

	@GET
	@Path("configured-converter")
	@Produces({ "application/txt" })
	public StreamingOutput getConfiguredConverter(@Context UriInfo uriInfo)
			throws UnsupportedEncodingException {
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		String query = queryParams.getFirst("query");
		String dataset = queryParams.getFirst("dataset");
		String forClass = queryParams.getFirst("for_class");
		String properties = queryParams.getFirst("properties");
		String variableDictionary = queryParams.getFirst("variable_dictionary");
		Boolean generateOntology = (queryParams.getFirst("generateOntology") == null) ? false : Boolean.parseBoolean(queryParams.getFirst("generateOntology"));
		
		// String variableDictionary =
		// "country::http://dbpedia.org/ontology/country,abstracts::http://dbpedia.org/ontology/abstract";
		String header = queryParams.getFirst("header");
		// String header = "<cities> ";
		String body = queryParams.getFirst("body");
		// String body =
		// "<city uri=\"$[=URI]\">\n<name>$[=NAME]</name>$[if country]\n<country uri=\"$[=country]\" />\n$[end]$[for abstract : abstracts]\n<abstract>$[=abstract]</abstract>\n$[end]</city>";
		String footer = queryParams.getFirst("footer");
		// String footer = "</cities>";
		if (forClass != null) {
			System.out
					.println("START configured conversion for query of class ("
							+ forClass + ") in dataset " + dataset + " \n"
							+ query);
			return OutputStreamHandler.getConverterStreamingOutput(
					new ConfiguredConverter(variableDictionary, header, body,
							footer), dataset, query, forClass, properties, generateOntology);
		} else {
			System.out.println("START RDB conversion for query in dataset "
					+ dataset + " \n" + query);
			return OutputStreamHandler.getConverterStreamingOutput(
					new ConfiguredConverter(variableDictionary, header, body,
							footer), dataset, query, generateOntology);
		}

	}

	@GET
	@Path("json")
	@Produces(MediaType.APPLICATION_JSON)
	public Object getJSONConverter(@Context UriInfo uriInfo)
			throws UnsupportedEncodingException, ParseException {
		try {
			MultivaluedMap<String, String> queryParams = uriInfo
					.getQueryParameters();
			String query = java.net.URLDecoder.decode(queryParams.getFirst("query"),"UTF-8");
			String dataset = queryParams.getFirst("dataset");
			String forClass = queryParams.getFirst("for_class");
			String properties = queryParams.getFirst("properties");
			String jsonOutputType = queryParams.getFirst("json_output_format");
			Boolean generateOntology = (queryParams.getFirst("generateOntology") == null) ? false : Boolean.parseBoolean(queryParams.getFirst("generateOntology"));

			Double startMilliseconds = (double) System.currentTimeMillis();
			JSONConverter converter = null;
			if (forClass == null || forClass.equals("")) {
				String outputFormat = "virtuoso";
				if (jsonOutputType != null
						&& jsonOutputType.equalsIgnoreCase("sesame"))
					outputFormat = "sesame";
				System.out
						.println("START JSON conversion for query in dataset "
								+ dataset + " \n" + query);

				converter = new JSONConverter(SPARQLHandler.executeQuery(
						dataset, query), outputFormat);
				
				if (generateOntology) InstanceExporter.exporter(query, dataset, "json");
			} else {
				System.out.println("START JSON conversion for query (class : "
						+ forClass + ") in dataset " + dataset + " \n" + query);
				RDFClass rdfForClass = RDFClass.searchRDFClass(dataset,
						forClass);
				if(properties != null && !properties.equals(""))
					rdfForClass.filterProperties(properties);
				converter = new JSONConverter(SPARQLHandler.executeQuery(
						dataset, query), rdfForClass);
				if (generateOntology) InstanceExporter.exporter(query, dataset, "json");

			}
			Double endMilliseconds = (double) System.currentTimeMillis();
			converter
					.setTimeTaken((endMilliseconds - startMilliseconds) / 1000);
			System.out.println("Time taken : "+((endMilliseconds - startMilliseconds) / 1000)+" seconds");
			System.out.println("FINISH JSON conversion ... ");
			if (forClass == null || forClass.equals("")) {
				if (converter.outputFormat.equals("sesame"))
					return converter.jsonSesameOutput;
				else
					return converter.jsonOutput;
			} else
				return converter.jsonObjectsOutput;
		} catch (Exception e) {
			System.out.println("Error : " + e.toString());
			return new JSONError("ERROR", e.toString());
		}
	}

}
