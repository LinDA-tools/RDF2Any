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
	
	public static List<String> getChunksFromString(String body){
		List<String> keywords = new ArrayList<String>();
		Boolean insideKeyword = false;
		Integer startPosition = 0;
		Boolean error = false;
		for (Integer i = 0; i < body.length(); i++) {

			if (body.charAt(i) == '$' && body.charAt(i + 1) == '[') {
				if (!insideKeyword) {
					startPosition = i;
					insideKeyword = true;
				} else {
					error = true;
				}
			} else if (body.charAt(i) == ']' && insideKeyword) {
				keywords.add(startPosition + "-" + i);
				insideKeyword = false;
			}
			if (error)
				break;
		}
		List<String> chunks = new ArrayList<String>();
		Integer otherLastIndex = 0;
		for(String keyword : keywords){
			Integer startIndex = Integer.parseInt(keyword.split("-")[0]);
			Integer endIndex = Integer.parseInt(keyword.split("-")[1]);
			chunks.add(body.substring(otherLastIndex,startIndex));
			chunks.add(body.substring(startIndex,endIndex +1));
			otherLastIndex = endIndex + 1;
		}
		if(otherLastIndex < body.length()){
			chunks.add(body.substring(otherLastIndex,body.length()));
		}
		return chunks;
	}
}
