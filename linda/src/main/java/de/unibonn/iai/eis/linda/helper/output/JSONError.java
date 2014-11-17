package de.unibonn.iai.eis.linda.helper.output;

public class JSONError {
	public String error;
	public String description;
	
	public JSONError(String error, String description){
		this.error = error;
		this.description = description;
	}
}
