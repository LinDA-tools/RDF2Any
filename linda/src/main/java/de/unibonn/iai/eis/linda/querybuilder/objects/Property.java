package de.unibonn.iai.eis.linda.querybuilder.objects;

public class Property {
	public String uri;
	public String type;
	public String label;
	public String range;
	
	public Property(String uri, String type, String label, String range){
		this.uri = uri;
		this.type = type;
		this.label = label;
		this.range = range;
	}
}
