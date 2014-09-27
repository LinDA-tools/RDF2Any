package de.unibonn.iai.eis.linda.querybuilder.classes;

import java.util.regex.Pattern;

import de.unibonn.iai.eis.linda.helper.SPARQLHandler;

/*
 * @author gauravsingharoy
 * 
 * */
public class RDFClassPropertyRange {
	public String uri;
	public String label;
	
	public RDFClassPropertyRange(String uri){
		this.uri = uri;
		this.label = "";
	}
	
	public RDFClassPropertyRange(String uri, String label){
		this.uri = uri;
		this.label = label;
	}
	
	public void generateRangeLabel(String dataset){
		if(SPARQLHandler.isDataTypeUri(this.uri)){
			//data type range
			this.label = this.uri.replaceAll(SPARQLHandler.getXMLSchemaURI()+"#", "");
		}
		else{
			//object type range
			this.label = SPARQLHandler.getLabelFromNode(dataset, this.uri, "EN");
		}
	}
	
	//returns true if range is a language literal
	public Boolean isLanguageLiteral(){
		Boolean result = false;
		if(this.uri.equalsIgnoreCase("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"))
			result = true;
		return result;
	}
	
	public String toString(){
		return "uri : "+this.uri+", label:"+this.label;
	}
}
