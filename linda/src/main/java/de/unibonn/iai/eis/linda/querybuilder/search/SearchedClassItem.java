package de.unibonn.iai.eis.linda.querybuilder.search;

import java.util.HashMap;
import java.util.Map;

public class SearchedClassItem {
	public String uri;
	public Map<String, String> labels;
	public Integer sequence;
	
	public SearchedClassItem(String uri){
		this.uri = uri;
		this.labels = new HashMap<String, String>();
		this.sequence = 0;
	}
	
	public SearchedClassItem(String uri, Integer sequence){
		this.uri = uri;
		this.labels = new HashMap<String, String>();
		this.sequence = sequence;
	}
	
	public void addLabel(String language, String label){
		this.labels.put(language, label);
	}
}
