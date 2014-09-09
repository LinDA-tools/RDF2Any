package de.unibonn.iai.eis.linda.querybuilder.objects;

import java.util.List;

public class Object {

	/**
	 * @param args
	 */
	public String uri;
	public String dataset;
	public List<Property> properties;
	public Object(String dataset,String uri){
		this.uri = uri;
		this.dataset = dataset;
	}
	
	//this method will generate properties for the object
	public void generateProperties(){
		
	}
	

}
