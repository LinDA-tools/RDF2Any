package de.unibonn.iai.eis.linda.converters.impl.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JSONOutput {
	public HashMap<String, List<String>> head;
	
	public JSONOutput(){
		this.head.put("link", new ArrayList<String>());
		this.head.put("vars", new ArrayList<String>());
	}
}
