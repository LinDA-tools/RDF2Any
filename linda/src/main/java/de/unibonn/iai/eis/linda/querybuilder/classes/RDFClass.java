package de.unibonn.iai.eis.linda.querybuilder.classes;

import java.util.List;

import de.unibonn.iai.eis.linda.helper.SPARQLHandler;

public class RDFClass {

	/**
	 * @param args
	 */
	public String uri;
	public String dataset;
	public List<Property> properties;
	public RDFClass(String dataset,String uri){
		this.uri = uri;
		this.dataset = dataset;
	}
	
	//this method will generate properties for the object
	public void generateProperties(){
		
	}
	
	//this method returns the query to get properties of a class
	public String getPropertiesSPARQLQuery(String propertyType){
		String query = SPARQLHandler.getPrefixes();
		query += "SELECT DISTINCT ?property ?label WHERE { ?concept rdf:type <"+this.uri+">. ?concept ?property ?o. ?property rdfs:label ?label. ";
		if(propertyType == "object" )
			query += " ?property rdf:type owl:ObjectProperty. ?property rdfs:range ?range. ";
		else if(propertyType == "datatype")
			query += " ?property rdf:type owl:DatatypeProperty. ";
		query += " FILTER(langMatches(lang(?label), 'EN'))} LIMIT 40";
		return query;
	}
	public String toString(){
		String result = "uri : "+this.uri+", dataset : "+this.dataset;
		for(Integer i=0;i<properties.size();i++){
			result += "\n" + properties.get(i).toString();
		}
		
		return result;
		
	}
	

}
