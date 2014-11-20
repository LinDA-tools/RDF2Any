package de.unibonn.iai.eis.linda.querybuilder.search;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.owlike.genson.annotation.JsonIgnore;

import de.unibonn.iai.eis.linda.helper.SPARQLHandler;
/*
 * @author gsingharoy
 * 
 * This class provides queries to search for classes in a dataset
 * */
public class ClassSearch {
	public String search_string;
	public String dataset;
	public List<SearchedClassItem> searched_items;
	
	public ClassSearch( String dataset, String searchString){
		this.search_string = searchString;
		this.dataset = dataset;
		this.searched_items = new ArrayList<SearchedClassItem>();
	}
	
	public void generateSearchedClassItems(){
		ResultSet rdfResultSet = SPARQLHandler.executeQuery(dataset, getSPARQLQuery());
		Integer sequence = 0;
		String currClass = "";
		SearchedClassItem currSearchedClass = null;
		while(rdfResultSet.hasNext()){
			QuerySolution row = rdfResultSet.next();
			String loopClass = row.get("class").toString();
			if(!loopClass.equals(currClass)){
				//new class found. Will create a new SearchedClassItem
				if(currSearchedClass!=null)
					this.searched_items.add(currSearchedClass);
				sequence++;
				currSearchedClass = new SearchedClassItem(loopClass,sequence);
				currClass = loopClass.toString();
				
			}
			RDFNode label = row.get("label");
			currSearchedClass.addLabel(SPARQLHandler.getLabelLanguage(label), SPARQLHandler.getLabelText(label));
		}
		//adding the last searched classItem
		if(currSearchedClass!=null)
			this.searched_items.add(currSearchedClass);
	}
	
	@JsonIgnore
	public String getSPARQLQuery(String lang){
		String query = SPARQLHandler.getPrefixes();
		query += " SELECT distinct ?class ?label ";
		query += " WHERE { ?class rdf:type owl:Class. ?class rdfs:label ?label. ?object rdf:type ?class. ";
		query += " FILTER(bound(?label) && langMatches(lang(?label), \""+lang.toUpperCase()+"\") && REGEX(?label, \""+this.search_string+"\"))}";
		return query;
	}
	
	@JsonIgnore
	public String getSPARQLQuery(){
		String query = SPARQLHandler.getPrefixes();
		query += " SELECT distinct ?class ?label ";
		query += " WHERE { ?class rdf:type owl:Class. ?class rdfs:label ?label. ?object rdf:type ?class. ";
		query += " FILTER(bound(?label)  && REGEX(?label, \""+this.search_string+"\"))} ORDER BY ?class";
		return query;
	}
	
}
