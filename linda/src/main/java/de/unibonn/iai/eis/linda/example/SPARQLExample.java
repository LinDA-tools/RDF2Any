package de.unibonn.iai.eis.linda.example;

import java.io.ByteArrayOutputStream;


import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

import de.unibonn.iai.eis.linda.helper.SPARQLHandler;

/**
 * @author gsingharoy
 *
 *This is a sample class to understand how to manipulate RDF using Jena
 *
 *Will be removed later
 *
 */


public class SPARQLExample {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.print(exampleResultSet("text"));
	}

	public static String exampleResultSet(String type){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String queryString=
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
						"SELECT ?subject ?label "+
						"WHERE { ?subject rdfs:label ?label. "+
						"FILTER(langMatches(lang(?label), \"EN\"))} LIMIT 10 ";

		ResultSet results = SPARQLHandler.executeDBPediaQuery(queryString);
		if(type.equals("text")){
			if(results != null){
				ResultSetFormatter.out(baos, results);
				return baos.toString();
			}
			else{
				return "";
			}
		}
		else if(type.equals("csv")){
			return "CSV Converstion to be implemented soon";
		}
		else if(type.equals("html")){
			return "HTML Converstion to be implemented soon";
		}
		else if(type.equals("xml")){
			return "XML Converstion to be implemented soon";
		}
		else if(type.equals("json")){
			return "JSON Converstion to be implemented soon";
		}
		else{
			return "No Converstion type '"+type+"' recognized.";
		}
		
	}
}
