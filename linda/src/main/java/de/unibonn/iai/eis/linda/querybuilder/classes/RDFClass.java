package de.unibonn.iai.eis.linda.querybuilder.classes;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.unibonn.iai.eis.linda.helper.SPARQLHandler;

public class RDFClass {

	/**
	 * @author gauravsingharoy
	 * 
	 * This class will contain an RDF class and its properties
	 * 
	 */
	public String uri;
	public String dataset;
	public List<Property> properties;
	public RDFClass(String dataset,String uri){
		this.uri = uri;
		this.dataset = dataset;
		this.properties = new ArrayList<Property>();
	}
	
	//this method will generate properties for the object from SPARQL endpoint
	public void generatePropertiesFromSPARQL(){
		//Get dataType properties
		ResultSet dataTypeProperties = SPARQLHandler.executeDBPediaQuery(getPropertiesSPARQLQuery("datatype"));
		addRdfResultSetToProperties(dataTypeProperties,"datatype");
	}
	
	//This method adds the ResultSet properties to the properties List 
	public void addRdfResultSetToProperties(ResultSet resultSetProperties, String type){
		while(resultSetProperties.hasNext()){
			System.out.println("inside result set..");
			QuerySolution row = resultSetProperties.next();
			RDFNode propertyNode = row.get("property");
			Literal propertyLabel = (Literal) row.get("label");
			properties.add(new Property(propertyNode.toString(), type, SPARQLHandler.getLabelName(propertyLabel), ""));
			
		}
	}
	
	//this method returns the query to get properties of a class
	public String getPropertiesSPARQLQuery(String propertyType){
		String query = SPARQLHandler.getPrefixes();
		query += "SELECT DISTINCT ?property ?label WHERE { ?concept rdf:type <"+this.uri+">. ?concept ?property ?o. ?property rdfs:label ?label. ";
		if(propertyType == "object" )
			query += " ?property rdf:type owl:ObjectProperty. ?property rdfs:range ?range. ";
		else if(propertyType == "datatype")
			query += " ?property rdf:type owl:DatatypeProperty. ";
		query += " FILTER(langMatches(lang(?label), 'EN'))} LIMIT 70";
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
