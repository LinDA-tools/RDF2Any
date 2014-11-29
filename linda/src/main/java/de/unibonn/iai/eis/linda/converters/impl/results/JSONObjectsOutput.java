package de.unibonn.iai.eis.linda.converters.impl.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClassProperty;
import de.unibonn.iai.eis.linda.querybuilder.objects.RDFObject;

/**
 * @author gsingharoy
 * 
 * 
 * This class gives the JSON output when a class convert for JSON is called
 * 
 **/

public class JSONObjectsOutput {
	public String dataset;
	public List<Object> classes;
	public List<Object> properties;
	public List<Object> objects;
	private Map<String, String> propertyDictionary;
	private RDFClass forClass;
	
	public JSONObjectsOutput(RDFClass forClass) {
		// TODO Auto-generated constructor stub
		this.dataset = forClass.dataset;
		this.classes = new ArrayList<Object>();
		this.properties = new ArrayList<Object>();
		this.objects = new ArrayList<Object>();
		Map<String,Object> classDef = new HashMap<String,Object>();
		classDef.put("uri", forClass.uri);
		classDef.put("label",forClass.label);
		this.classes.add(classDef);
		this.forClass = forClass;
		generatePropertyDictionary();
	}
	
	private void generatePropertyDictionary(){
		this.propertyDictionary = new HashMap<String, String>();
		for(RDFClassProperty prop: this.forClass.properties){
			this.propertyDictionary.put(prop.uri, prop.getPropertyUnderscoreVariableName());
		}
	}


}
