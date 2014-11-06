package de.unibonn.iai.eis.linda.converters.impl.results.sesame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class JSONSesameOutput {
	public Map<String, Object> sparql;
	public Map<String, Object> results;
	private List<JSONSesameOutputResultBinding> resultBindings;

	public JSONSesameOutput(ResultSet rdfResultSet) {
		// TODO Auto-generated constructor stub
		this.sparql = new HashMap<String, Object>();
		this.results = new HashMap<String, Object>();
		this.resultBindings = new ArrayList<JSONSesameOutputResultBinding>();
		this.sparql.put("@xmlns", "http://www.w3.org/2005/sparql-results#");
		this.sparql.put("head", new JSONSesameOutputHead(rdfResultSet));
	}

	public void addResultBinding(QuerySolution row, List<String> resultVars) {
		this.resultBindings.add(new JSONSesameOutputResultBinding(row,
				resultVars));
	}

	public void setFinalResult() {
		this.results.put("result", this.resultBindings);
	}
}
