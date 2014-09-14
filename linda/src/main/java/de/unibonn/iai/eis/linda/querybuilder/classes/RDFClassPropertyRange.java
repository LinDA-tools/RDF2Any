package de.unibonn.iai.eis.linda.querybuilder.classes;
/*
 * @author gauravsingharoy
 * 
 * */
public class RDFClassPropertyRange {
	public String uri;
	public String label;
	
	public RDFClassPropertyRange(String uri, String label){
		this.uri = uri;
		this.label = label;
	}
	
	public String toString(){
		return "uri : "+this.uri+", label:"+this.label;
	}
}
