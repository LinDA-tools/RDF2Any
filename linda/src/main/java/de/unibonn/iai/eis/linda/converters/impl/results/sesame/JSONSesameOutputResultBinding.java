package de.unibonn.iai.eis.linda.converters.impl.results.sesame;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.unibonn.iai.eis.linda.helper.SPARQLHandler;

public class JSONSesameOutputResultBinding {
	public List<Object> binding;

	public JSONSesameOutputResultBinding(QuerySolution row,
			List<String> resultVars) {
		this.binding = new ArrayList<Object>();
		for (String resultVar : resultVars) {
			Map<String, Object> varHash = new HashMap<String, Object>();
			varHash.put("@name", resultVar);
			RDFNode node = row.get(resultVar);
			if (node != null && !node.toString().equals("")) {
				if (node instanceof Literal) {
					// section where the node is a literal
					if (node.toString().length() > 3) {
						Integer languageIdentifierPoint = node.toString()
								.length() - 3;
						if (node.toString().charAt(languageIdentifierPoint) == '@') {
							// section if it is language literal
							Map<String, String> langLiteralHash = new HashMap<String, String>();
							langLiteralHash.put("@xml:lang", node.toString()
									.substring(languageIdentifierPoint + 1));
							langLiteralHash.put("#text", node.toString()
									.substring(0, languageIdentifierPoint));
							varHash.put("literal", langLiteralHash);
						} else {
							if (((Literal) node).getDatatypeURI() == null) {
								// plain literal
								varHash.put("literal", node.toString());
							} else {
								// typed literal
								Map<String, String> langLiteralHash = new HashMap<String, String>();
								langLiteralHash.put("datatype",
										SPARQLHandler.getLiteralDataType(node));
								langLiteralHash.put("#text",
										SPARQLHandler.getLiteralValue(node));
								varHash.put("typed-literal", langLiteralHash);
							}
						}
					} else {
						if (((Literal) node).getDatatypeURI() == null) {
							// plain literal
							varHash.put("literal", node.toString());
						} else {
							// typed literal
							Map<String, String> langLiteralHash = new HashMap<String, String>();
							langLiteralHash.put("datatype",
									SPARQLHandler.getLiteralDataType(node));
							langLiteralHash.put("#text",
									SPARQLHandler.getLiteralValue(node));
							varHash.put("typed-literal", langLiteralHash);
						}
					}
				} else {
					// section where the node is a uri
					varHash.put("uri", node.toString());
				}
				this.binding.add(varHash);
			}
		}
	}
}
