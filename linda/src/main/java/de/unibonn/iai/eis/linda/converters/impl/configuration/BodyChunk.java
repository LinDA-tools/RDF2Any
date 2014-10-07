package de.unibonn.iai.eis.linda.converters.impl.configuration;

import java.util.List;

public class BodyChunk {
	public String type;
	//type can be text, unparsed, for, if 
	public List<BodyChunk> chunks;
	public String value;
	public String additionalValue;
	
	
}
