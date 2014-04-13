package de.unibonn.iai.eis.linda.converters.impl.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.QuerySolution;


/**
 * @author gsingharoy
 *
 *
 **/

public class JSONResult {
	public Boolean distict;
	public Boolean ordered;
	public List<Map<String,Map<String,String>>> bindings;
	public JSONResult(){
		this.distict = false;
		this.ordered = true;
		this.bindings = new ArrayList();

	}
	public void addBindingEntry(QuerySolution row, List<String> resultVars){
		Map<String, Map<String,String>> bindingEntry = new HashMap<String, Map<String,String>>();
		for(int i=0;i<resultVars.size();i++){
			Map<String,String> columnEntry = new HashMap<String,String>();
			columnEntry.put("value", row.get(resultVars.get(i)).toString());
			bindingEntry.put(resultVars.get(i), columnEntry);
		}
		this.bindings.add(bindingEntry);

	}
}
