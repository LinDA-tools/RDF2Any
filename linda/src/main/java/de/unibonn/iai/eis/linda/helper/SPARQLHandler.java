package de.unibonn.iai.eis.linda.helper;


import java.util.regex.Pattern;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * @author gsingharoy
 *
 *This class handles SPARQL queries
 **/
public class SPARQLHandler {

	//This method executes a SPARQL query in DBPedia	
	public static ResultSet executeDBPediaQuery(String queryString){
		//System.out.println("Executing query .... ");
		//System.out.println(queryString);
		return executeQuery("http://dbpedia.org/sparql?timeout=60000",queryString);
	}
	
	//This method executes a SPARQL query in a RDF triple store
	public static ResultSet executeQuery(String uri, String queryString){
		Query query = QueryFactory.create(queryString);
		//System.out.println("Executing query .... ");
		//System.out.println(queryString);
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
	
	public static String getLabelName(RDFNode label){
		Integer languageIdentifierPoint = label.toString().length()-3;
		return label.toString().substring(0,languageIdentifierPoint);
	}
	public static String getLabelFromNode(String dataset, RDFNode node, String language){
		return getLabelFromNode(dataset, node.toString(), language);
	}
	public static String getLabelFromNode(String dataset, String node, String language){
		String label = "";
		String query = getPrefixes();
		query += "select distinct ?label where {<"+node+"> rdfs:label ?label. FILTER(langMatches(lang(?label), \""+language+"\"))} ";
		ResultSet labelResultSet = executeQuery(dataset, query);
		if(labelResultSet.hasNext()){
			label =  getLabelName(labelResultSet.next().get("label"));
		}
		return label;
	}
	
	public static String getXMLSchemaURI(){
		return "http://www.w3.org/2001/XMLSchema";
	}
	
	public static Boolean isDataTypeUri(String uri){
		return Pattern.compile(Pattern.quote(SPARQLHandler.getXMLSchemaURI()), Pattern.CASE_INSENSITIVE).matcher(uri).find();
	}
	
	public static Integer getIntegerValueOfLiteral(RDFNode n){
		return Integer.parseInt(n.toString().replaceAll("http://www.w3.org/2001/XMLSchema#integer", "").substring(0,n.toString().replaceAll("http://www.w3.org/2001/XMLSchema#integer", "").length()-2));
	}

	public static String getLabelLanguage(RDFNode label) {
		Integer languageIdentifierPoint = label.toString().length()-2;
		return label.toString().substring(languageIdentifierPoint,label.toString().length());
	}

}
