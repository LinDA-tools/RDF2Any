package de.unibonn.iai.eis.linda.querybuilder;

import de.unibonn.iai.eis.linda.helper.SPARQLHandler;

public class ObjectSearch {

	/**
	 * @author gauravsingharoy
	 * 
	 *         This class searches for objects of a class in an RDF dataset
	 */

	public String searchString;
	public String[] classes;
	public String dataset;
	public String forClass;
	public String forProperty;

	public ObjectSearch(String dataset, String searchString, String classes) {
		this.searchString = searchString;
		this.classes = classes.split(",");
		this.dataset = dataset;
		this.forClass = null;
		this.forProperty = null;
	}

	public ObjectSearch(String dataset, String searchString, String classes,
			String forClass, String forProperty) {
		// TODO Auto-generated constructor stub
		this.searchString = searchString;
		this.classes = classes.split(",");
		this.dataset = dataset;
		this.forClass = forClass;
		this.forProperty = forProperty;
	}

	public String getSPARQLQuery() {
		String query = SPARQLHandler.getPrefixes();
		query += " SELECT distinct ?object ?label ";
		query += " WHERE { ";
		if (this.forClass != null && !this.forClass.equalsIgnoreCase("")
				&& this.forProperty != null
				&& !this.forProperty.equalsIgnoreCase("")) {
			query += " ?subject rdf:type <"+this.forClass+"> . ?subject <"+this.forProperty+"> ?object. ";
		}
		query += " { ";
		for (int i = 0; i < this.classes.length; i++) {
			if (i > 0) {
				query += " UNION ";
			}
			query += " {?object rdf:type <" + this.classes[i] + ">} ";
		}
		query += " }. ?object rdfs:label ?label.  ";
		query += " FILTER(bound(?label) && langMatches(lang(?label), \"EN\") && REGEX(?label, \""
				+ this.searchString + "\", \"i\"))}";
		return query;
	}

}
