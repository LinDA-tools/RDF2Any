package de.unibonn.iai.eis.linda.converters.impl.results.sesame;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class JSONSesameOutputResultBinding {
	public List<Object> binding;

	public JSONSesameOutputResultBinding(QuerySolution row,
			List<String> resultVars) {
		this.binding = new ArrayList<Object>();
		for (String resultVar : resultVars) {
			Map<String, Object> varHash = new HashMap<String, Object>();
			varHash.put("@name", resultVar);
			RDFNode node = row.get(resultVar);
			if (node instanceof Literal) {
				// section where the node is a literal
			} else {
				// section where the node is a uri
				varHash.put("uri", node.toString());
			}
			this.binding.add(varHash);
		}
	}
}
