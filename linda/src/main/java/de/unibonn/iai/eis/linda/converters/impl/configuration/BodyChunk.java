package de.unibonn.iai.eis.linda.converters.impl.configuration;

import java.util.ArrayList;
import java.util.List;

public class BodyChunk {
	public String type;
	//type can be text, unparsed, for, if 
	public List<BodyChunk> chunks;
	public String value;
	public String additionalValue;
	public List<String> variables;
	
	
	public BodyChunk(String type){
		this.type = type;
		this.chunks = new ArrayList<BodyChunk>();
		this.variables = new ArrayList<String>();
		this.value = null;
		this.additionalValue = null;
	}
}
