package de.unibonn.iai.eis.linda.converters.impl.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;




public class JSONOutput {
	Map<String, Collection<String>> head; 

	
	public JSONOutput(){
		this.head =  new HashMap<String, Collection<String>>();
		this.head.put("link", new ArrayList<String>());
		this.head.put("vars", new ArrayList<String>());

	}
}
