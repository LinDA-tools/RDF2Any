package de.unibonn.iai.eis.linda.querybuilder;

import de.unibonn.iai.eis.linda.helper.SPARQLHandler;

public class ClassSearch {
	public String dataset;
	public String searchString;
	
	public ClassSearch(String dataset, String searchString){
		this.dataset = dataset;
		this.searchString = searchString;
	}
	
	public String getSPARQLQuery(){
		String query = SPARQLHandler.getPrefixes();
		query += " SELECT distinct ?class ?label ";
		query += " WHERE { ?class rdf:type owl:Class. ?class rdfs:label ?label. ?object rdf:type ?class. ";
		query += " FILTER(bound(?label) && langMatches(lang(?label), \"EN\") && REGEX(?label, \""+this.searchString+"\"))}";
		return query;
	}
	
}
