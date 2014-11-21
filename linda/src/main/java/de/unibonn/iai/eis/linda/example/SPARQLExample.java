package de.unibonn.iai.eis.linda.example;

import java.io.ByteArrayOutputStream;


import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.unibonn.iai.eis.linda.helper.SPARQLHandler;
import de.unibonn.iai.eis.linda.querybuilder.search.ClassSearch;

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

		//System.out.print(exampleResultSet("text"));
		ClassSearch c = new ClassSearch("dbpedia","animal");
		System.out.print(c.getSPARQLQuery());
		SPARQLHandler.executeDBPediaQuery(c.getSPARQLQuery());
	}

	//This function returns a sample SPARQL query String
	public static String exampleQueryString(){
		return exampleQueryString(300);
	}
	
	public static String exampleQueryString(Integer length){
		String queryString=
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
						"SELECT ?subject ?label "+
						"WHERE { ?subject rdfs:label ?label. "+
						"FILTER(langMatches(lang(?label), \"EN\"))} LIMIT "+length.toString()+" ";

		return queryString;
	}
	
	public static String exampleDataset(){
		return "http://dbpedia.org/sparql?timeout=60000";
	}
	

	public static String exampleResultSet(String type){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String queryString=	exampleQueryString();

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
			String strResult = "subject,label\n"; 
			while(results.hasNext()){
				 QuerySolution row= results.next();
				 RDFNode subject= row.get("subject");
				 Literal label= row.getLiteral("label");
				 strResult += subject.toString()+","+label.toString()+"\n";
			}
			return strResult;
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
