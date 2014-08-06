package de.unibonn.iai.eis.linda.querybuilder;

import de.unibonn.iai.eis.linda.helper.SPARQLHandler;

public class ObjectSearch {

	/**
	 * @author gauravsingharoy
	 * 
	 * This class searches for objects of a class in an RDF dataset
	 */
	
		public String searchString;
		public String[] classes;
		public String dataset;
		
		public ObjectSearch(String dataset,  String searchString, String classes){
			this.searchString = searchString;
			this.classes = classes.split(",");
			this.dataset = dataset;
		}
		
		public String getSPARQLQuery(){
			String query = SPARQLHandler.getPrefixes();
			query += " SELECT distinct ?object ?label ";
			query += " WHERE { " ; 
			for(int i = 0; i< this.classes.length;i++){
				if(i>0){
					query += " UNION ";
				}
				query += " {?object rdf:type <"+this.classes[i]+">} ";
			}
			query += ". ?object rdfs:label ?label.  ";
			query += " FILTER(bound(?label) && langMatches(lang(?label), \"EN\") && REGEX(?label, \""+this.searchString+"\"))}";
			return query;
		}

	

}
