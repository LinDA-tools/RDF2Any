package de.unibonn.iai.eis.linda.converters.impl.results.rdb.classes;

/**
 * @author gsingharoy
 *
 *class for owl:Thing
 *
 **/

public class Thing {
	public String uri;
	
	public Thing(String uri){
		this.uri = uri;
	}
	//this method returns the table script 
	public String createTableScript(){
		return "";
	}
	
}
