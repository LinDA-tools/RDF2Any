package de.unibonn.iai.eis.linda.converters.impl.results;

import java.util.ArrayList;
import java.util.List;

import de.unibonn.iai.eis.linda.querybuilder.objects.RDFObject;

/**
 * @author gsingharoy
 * 
 * 
 * This class gives the JSON output when a class convert for JSON is called
 * 
 **/

public class JSONObjectsOutput {
	public String class_uri;
	public String class_name;
	public List<RDFObject> objects;
	public double time_taken;
	
	public JSONObjectsOutput(String class_uri, String class_name){
		this.class_uri = class_uri;
		this.class_name = class_name;
		this.objects = new ArrayList<RDFObject>();
	}
}
