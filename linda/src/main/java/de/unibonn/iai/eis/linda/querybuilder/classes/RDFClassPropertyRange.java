package de.unibonn.iai.eis.linda.querybuilder.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
			//this.label = this.uri.replaceAll(SPARQLHandler.getXMLSchemaURI()+"#", "");
			if(this.uri.contains("#")){
				this.label = this.uri.split("\\#")[1];
			}
			else
			{
				this.label = this.uri ;
			}
		}
		else{
			//object type range
			this.label = SPARQLHandler.getLabelFromNode(dataset, this.uri, "EN");
		}
	}
	
	//returns true if range is a language literal
	@JsonIgnore
	public Boolean isLanguageLiteral(){
		Boolean result = false;
		if(this.uri.equalsIgnoreCase("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"))
			result = true;
		return result;
	}
	
	@JsonIgnore
	public String toString(){
		return "uri : "+this.uri+", label:"+this.label;
	}
}
