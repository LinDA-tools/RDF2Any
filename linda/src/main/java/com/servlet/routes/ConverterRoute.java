package com.servlet.routes;

import java.io.ByteArrayOutputStream;
import java.io.CharConversionException;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.lucene.queryparser.classic.ParseException;
import org.glassfish.grizzly.http.util.URLDecoder;

import com.hp.hpl.jena.query.ResultSetFormatter;
import com.servlet.Main;

import de.unibonn.iai.eis.linda.converters.impl.CSVConverter;
import de.unibonn.iai.eis.linda.converters.impl.ConfiguredConverter;
import de.unibonn.iai.eis.linda.converters.impl.RDBConverter;
import de.unibonn.iai.eis.linda.converters.impl.RDFConverter;
import de.unibonn.iai.eis.linda.helper.InstanceExporter;
import de.unibonn.iai.eis.linda.helper.OutputStreamHandler;
import de.unibonn.iai.eis.linda.helper.SPARQLHandler;
import de.unibonn.iai.eis.linda.helper.output.JSONError;

@Path("/v1.0/convert/")
public class ConverterRoute {
	@GET
	@Path("csv-converter.csv")
	@Produces({ "application/csv" })
	public StreamingOutput getCSVConverter(@Context UriInfo uriInfo) throws UnsupportedEncodingException, ParseException {
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		String query = queryParams.getFirst("query");
		String dataset = queryParams.getFirst("dataset");
//		String forClass = queryParams.getFirst("for_class");
//		String properties = queryParams.getFirst("properties");
		Boolean generateOntology = (queryParams.getFirst("generateOntology") == null) ? false : Boolean.parseBoolean(queryParams.getFirst("generateOntology"));
		
		query = query.replace("&lt;", "<").replace("&gt;", ">");
		
		System.out.println("START CSV conversion for query in dataset "+ dataset + " \n" + query);
		return OutputStreamHandler.getConverterStreamingOutput(new CSVConverter(), dataset, query, generateOntology);
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

		query = query.replace("&lt;", "<").replace("&gt;", ">");

		Boolean generateOntology = (queryParams.getFirst("generateOntology") == null) ? false : Boolean.parseBoolean(queryParams.getFirst("generateOntology"));
		
		return OutputStreamHandler.getConverterStreamingOutput(new RDBConverter(), dataset, query, generateOntology);

		
//		if (forClass != null) {
//			System.out.println("START RDB conversion for query of class ("
//					+ forClass + ") in dataset " + dataset + " \n" + query);
//			return OutputStreamHandler.getConverterStreamingOutput(
//					new RDBConverter(), dataset, query, forClass, properties, generateOntology);
//		} else {
//			System.out.println("START RDB conversion for query in dataset "+ dataset + " \n" + query);
//			return OutputStreamHandler.getConverterStreamingOutput(new RDBConverter(), dataset, query, generateOntology);
//		}
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

		query = query.replace("&lt;", "<").replace("&gt;", ">");

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

		query = query.replace("&lt;", "<").replace("&gt;", ">");

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
//			String forClass = queryParams.getFirst("for_class");
//			String properties = queryParams.getFirst("properties");
//			String jsonOutputType = queryParams.getFirst("json_output_format");
			Boolean generateOntology = (queryParams.getFirst("generateOntology") == null) ? false : Boolean.parseBoolean(queryParams.getFirst("generateOntology"));
			
			query = query.replace("&lt;", "<").replace("&gt;", ">");			
			
			Double startMilliseconds = (double) System.currentTimeMillis();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ResultSetFormatter.outputAsJSON(outputStream, SPARQLHandler.executeQuery(dataset, query));
			String json = new String(outputStream.toByteArray());
			Double endMilliseconds = (double) System.currentTimeMillis();
//			String timeTaken = "\"time_taken\" : " + ((endMilliseconds - startMilliseconds) / 1000) + "\n\t}\n}";
//			json = json.replace("\t}\n}", timeTaken);
			System.out.println("Time taken : "+((endMilliseconds - startMilliseconds) / 1000)+" seconds");
			System.out.println("FINISH JSON conversion ... ");
			if (generateOntology) InstanceExporter.exporter(query, dataset, "json");
			if (!generateOntology) Main.incrementQueryCounter();
			
			return json;
		} catch (Exception e) {
			System.out.println("Error : " + e.toString());
			return new JSONError("ERROR", e.toString());
		}
	}

}
