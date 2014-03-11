package de.unibonn.iai.eis.linda.converters.impl;


import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;


public class SPARQLHandler {

	public static ResultSet executeDBPEDIAQuery(String queryString){
		return executeQuery("http://dbpedia.org/sparql",queryString);
	}
	
	public static ResultSet executeQuery(String uri, String queryString){
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(uri, query);
		try {
		    ResultSet results = qexec.execSelect();
		    return results;

		}
		finally {
		   qexec.close();
		}
	}

}
