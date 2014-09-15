package de.unibonn.iai.eis.linda.querybuilder.classes;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;


import de.unibonn.iai.eis.linda.helper.SPARQLHandler;

public class RDFClassProperty {
	public String uri;

	public String type;
	public String label;
	public Integer count;
	public RDFClassPropertyRange range;
	public Boolean multiplePropertiesForSameNode;
	
	public RDFClassProperty(String uri,  String type, String label){
		this.uri = uri;
		this.count = 0;
		this.type = type;
		this.label = label;
		this.multiplePropertiesForSameNode = false;
	}
	
	public RDFClassProperty(String uri,  String type, String label, RDFClassPropertyRange range){
		this.uri = uri;
		this.count = 0;
		this.type = type;
		this.label = label;
		this.range = range;
		this.multiplePropertiesForSameNode = false;
	}
	
	//This method generates the count of the property
	
	public void generateCountOfProperty(String classUri, String dataset){
		String countQuery = SPARQLHandler.getPrefixes();
		countQuery += " SELECT ?c ?d  where {?c rdf:type <"+classUri+">. ?c <"+this.uri+"> ?d} ";
		ResultSet countResultSet = SPARQLHandler.executeQuery(dataset, countQuery);
		this.count = 0;
		String prevUri = "";
		while(countResultSet.hasNext()){
			QuerySolution row = countResultSet.next();
			String currUri = row.get("g").toString();
			if(prevUri.equals(currUri))
				this.multiplePropertiesForSameNode = true;
			prevUri = currUri;
			this.count++;
		}
		System.out.println("generated count for "+this.uri + " ("+this.count+")");
		
	}
	
	public String toString(){
		return "uri : "+this.uri+", type : "+this.type+", label : "+this.label+", range : {"+this.range.toString()+"}, count : "+this.count.toString();
	}
}
