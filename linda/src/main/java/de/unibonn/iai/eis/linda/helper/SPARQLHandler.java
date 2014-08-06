package de.unibonn.iai.eis.linda.helper;


import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;

/**
 * @author gsingharoy
 *
 *This class handles SPARQL queries
 **/
public class SPARQLHandler {

	//This method executes a SPARQL query in DBPedia	
	public static ResultSet executeDBPediaQuery(String queryString){
		return executeQuery("http://dbpedia.org/sparql?timeout=60000",queryString);
	}
	
	//This method executes a SPARQL query in a RDF triple store
	public static ResultSet executeQuery(String uri, String queryString){
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(uri, query);
		try {
		    ResultSet results = qexec.execSelect();
		    return results;

		}
		finally {

		}
	}
	
	public static String getPrefixes(){
		String prefixes = "";
		prefixes += "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> ";
		prefixes += "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> ";
		prefixes += "PREFIX owl:<http://www.w3.org/2002/07/owl#> ";
		return prefixes;
	}
	

}
