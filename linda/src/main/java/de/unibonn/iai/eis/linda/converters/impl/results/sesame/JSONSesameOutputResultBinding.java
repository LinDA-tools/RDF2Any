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
				Integer languageIdentifierPoint = node.toString().length() - 3;
				if (node.toString().charAt(languageIdentifierPoint) == '@') {
					// section if it is language literal
					Map<String, String> langLiteralHash = new HashMap<String, String>();
					langLiteralHash.put(
							"@xml:lang",
							node.toString().substring(
									languageIdentifierPoint + 1));
					langLiteralHash.put(
							"#text",
							node.toString().substring(0,
									languageIdentifierPoint));
					varHash.put("uri", langLiteralHash);
				} else {
					varHash.put("literal", node.toString());
				}
			} else {
				// section where the node is a uri
				varHash.put("uri", node.toString());
			}
			this.binding.add(varHash);
		}
	}
}
