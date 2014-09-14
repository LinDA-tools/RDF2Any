package de.unibonn.iai.eis.linda.querybuilder.classes;

public class RDFClassProperty {
	public String uri;

	public String type;
	public String label;
	public RDFClassPropertyRange range;
	
	public RDFClassProperty(String uri,  String type, String label){
		this.uri = uri;

		this.type = type;
		this.label = label;
	}
	
	public RDFClassProperty(String uri,  String type, String label, RDFClassPropertyRange range){
		this.uri = uri;

		this.type = type;
		this.label = label;
		this.range = range;
	}
	
	public String toString(){
		return "uri : "+this.uri+", type : "+this.type+", label : "+this.label+", range : {"+this.range.toString()+"}";
	}
}
