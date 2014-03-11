package de.unibonn.iai.eis.linda.converters.impl;

import java.io.ByteArrayOutputStream;


import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

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

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String queryString=
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"SELECT ?subject ?label "+
				"WHERE { ?subject rdfs:label ?label } LIMIT 10 ";

		ResultSet results = SPARQLHandler.executeDBPediaQuery(queryString);
		if(results != null){
			ResultSetFormatter.out(baos, results);
	    	System.out.print(baos.toString());
		}
	}

}
