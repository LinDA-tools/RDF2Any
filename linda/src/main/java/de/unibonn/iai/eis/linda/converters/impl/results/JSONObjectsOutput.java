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
	public Map<String,Object> properties;
	public List<Object> objects;
	private Map<String, String> propertyDictionary;
	private RDFClass forClass;
	
	public JSONObjectsOutput(RDFClass forClass) {
		// TODO Auto-generated constructor stub
		this.dataset = forClass.dataset;
		this.classes = new ArrayList<Object>();
		this.properties = new HashMap<String,Object>();
		this.objects = new ArrayList<Object>();
		this.forClass = forClass;
		generatePropertiesForOutput();
		Map<String,Object> classDef = new HashMap<String,Object>();
		classDef.put("uri", forClass.uri);
		classDef.put("label",forClass.label);
		this.classes.add(classDef);
		
		
	}
	
	private void generatePropertiesForOutput(){
		this.propertyDictionary = new HashMap<String, String>();
		for(RDFClassProperty prop: this.forClass.properties){
			String propVar = prop.getPropertyUnderscoreVariableName();
			this.propertyDictionary.put(prop.uri, propVar);
			this.properties.put(propVar,prop);
		}
	}


}
