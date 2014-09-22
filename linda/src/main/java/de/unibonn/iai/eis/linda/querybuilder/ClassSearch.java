package de.unibonn.iai.eis.linda.querybuilder;

import de.unibonn.iai.eis.linda.helper.SPARQLHandler;
/*
 * @author gsingharoy
 * 
 * This class provides queries to search for classes in a dataset
 * */
public class ClassSearch {
	public String searchString;
	public String dataset;
	
	public ClassSearch( String dataset, String searchString){
		this.searchString = searchString;
		this.dataset = dataset;
	}
	
	public String getSPARQLQuery(){
		String query = SPARQLHandler.getPrefixes();
		query += " SELECT distinct ?class ?label ";
		query += " WHERE { ?class rdf:type owl:Class. ?class rdfs:label ?label. ?object rdf:type ?class. ";
		query += " FILTER(bound(?label) && langMatches(lang(?label), \"EN\") && REGEX(?label, \""+this.searchString+"\"))}";
		return query;
	}
	
}
