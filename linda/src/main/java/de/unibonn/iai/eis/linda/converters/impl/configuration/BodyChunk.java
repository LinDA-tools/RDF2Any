package de.unibonn.iai.eis.linda.converters.impl.configuration;

import java.util.ArrayList;
import java.util.List;

public class BodyChunk {
	public String type;
	// type can be text, loop, condition, variable
	public List<BodyChunk> chunks;
	public String value;
	public String additionalValue;

	public BodyChunk(String type) {
		this.type = type;
		this.chunks = new ArrayList<BodyChunk>();
		this.value = null;
		this.additionalValue = null;
	}

	public static Boolean isTextChunk(String chunk) {
		if (chunk.charAt(0) == '$' && chunk.charAt(1) == '[')
			return false;
		else
			return true;
	}

	public static Boolean isEndChunk(String chunk) {
		Boolean result = false;
		if (chunk.charAt(0) == '$') {
			String keyword = BodyChunk.getKeywordFromChunk(chunk);
			if (keyword.equals("end"))
				result = true;
		}
		return result;
	}

	public static Boolean isVariableChunk(String chunk) {
		Boolean result = false;
		if (chunk.charAt(0) == '$') {
			if (chunk.charAt(2) == '=')
				result = true;
		}
		return result;
	}

	public static Boolean isConditionalChunk(String chunk) {
		Boolean result = false;
		if (chunk.charAt(0) == '$') {
			String keyword = BodyChunk.getKeywordFromChunk(chunk);
			if (keyword.split(" ")[0].equals("if"))
				result = true;
		}
		return result;
	}

	public static Boolean isLoopChunk(String chunk) {
		Boolean result = false;
		if (chunk.charAt(0) == '$') {
			String keyword = BodyChunk.getKeywordFromChunk(chunk);
			if (keyword.split(" ")[0].equals("for"))
				result = true;
		}
		return result;
	}

	public static String getValueOfConditionalChunk(String chunk) {
		String keyword = BodyChunk.getKeywordFromChunk(chunk);
		return keyword.substring(2, keyword.length()).trim();
	}

	public static String getValueOfVariableChunk(String chunk) {
		String keyword = BodyChunk.getKeywordFromChunk(chunk);
		return keyword.substring(1, keyword.length() ).trim();
	}

	public static String getValueOfLoopChunk(String chunk) {
		String keyword = BodyChunk.getKeywordFromChunk(chunk);
		return keyword.substring(3, keyword.length()).trim().replace(" ", "");
	}

	public static String getKeywordFromChunk(String chunk) {
		return chunk.substring(2, chunk.length() - 1).trim();
	}

	public static List<BodyChunk> getBodyChunksFromString(String body){
		return BodyChunk.getBodyChunksFromChunks(BodyChunk.getChunksFromString(body));
	}
	
	public static List<BodyChunk> getBodyChunksFromChunks(List<String> strChunks) {
		List<BodyChunk> chunks = new ArrayList<BodyChunk>();
		String insideStructure = "";
		Integer insideStructureCounter = 0;
		BodyChunk insideStructureChunk = null;
		for (String strChunk : strChunks) {
			if (BodyChunk.isTextChunk(strChunk)) {
				if (insideStructureCounter > 0) {
					insideStructure += strChunk;
				} else {
					BodyChunk txtChunk = new BodyChunk("text");
					txtChunk.value = strChunk;
					chunks.add(txtChunk);
				}
			} else if (BodyChunk.isVariableChunk(strChunk)) {
				if (insideStructureCounter > 0) {
					insideStructure += strChunk;
				} else {
					BodyChunk varChunk = new BodyChunk("variable");
					varChunk.value = BodyChunk
							.getValueOfVariableChunk(strChunk);
					chunks.add(varChunk);
				}
			} else if (BodyChunk.isConditionalChunk(strChunk)) {
				if (insideStructureCounter > 0) {
					insideStructure += strChunk;
				} else {
					insideStructureCounter++;
					insideStructureChunk = new BodyChunk("condition");
					insideStructureChunk.value = BodyChunk
							.getValueOfConditionalChunk(strChunk);
				}
			} else if (BodyChunk.isLoopChunk(strChunk)) {
				if (insideStructureCounter > 0) {
					insideStructure += strChunk;
				} else {
					insideStructureCounter++;
					insideStructureChunk = new BodyChunk("loop");
					insideStructureChunk.value = BodyChunk
							.getValueOfConditionalChunk(strChunk).split(":")[1];
					insideStructureChunk.additionalValue = BodyChunk
							.getValueOfConditionalChunk(strChunk).split(":")[0];
				}
			} else if (BodyChunk.isEndChunk(strChunk)) {
				insideStructureCounter--;
				if (insideStructureCounter > 0) {
					insideStructure += strChunk;
				} else {
					if (!insideStructure.equals("")) {
						List<BodyChunk> insideChunks = BodyChunk
								.getBodyChunksFromString(insideStructure);
						for(BodyChunk ic:insideChunks){
							insideStructureChunk.chunks.add(ic);
						}
					}
					insideStructure = "";
					chunks.add(insideStructureChunk);
					insideStructureChunk = null;
				}
			}

		}
		return chunks;
	}

	// this method returns a list of String chunks from a string. The chunks are
	// separated from keywords
	public static List<String> getChunksFromString(String body) {
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
		for (String keyword : keywords) {
			Integer startIndex = Integer.parseInt(keyword.split("-")[0]);
			Integer endIndex = Integer.parseInt(keyword.split("-")[1]);
			chunks.add(body.substring(otherLastIndex, startIndex));
			chunks.add(body.substring(startIndex, endIndex + 1));
			otherLastIndex = endIndex + 1;
		}
		if (otherLastIndex < body.length()) {
			chunks.add(body.substring(otherLastIndex, body.length()));
		}
		return chunks;
	}
	
	public String toString(){
		String result = "("+this.type+", "+this.value+", "+this.additionalValue;
		if(this.chunks.size() > 0){
			result += ", [";
			for(BodyChunk b:this.chunks){
				result += b.toString();
			}
			result += "]";
		}
		result += ")";
		return result;
	}
}
