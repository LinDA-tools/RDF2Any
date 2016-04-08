package de.unibonn.iai.eis.linda.querybuilder.search;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	final static Logger logger = LoggerFactory.getLogger(ClassSearch.class);

	
	public String search_string;
	public String dataset;
	public List<SearchedClassItem> searched_items;
	private Integer sequence;
	
	public ClassSearch(String dataset, String searchString) {
		this.search_string = searchString;
		this.dataset = dataset;
		this.searched_items = new ArrayList<SearchedClassItem>();
		this.sequence = 0;
	}
	
	public ClassSearch(String dataset) {
		this.search_string = "";
		this.dataset = dataset;
		this.searched_items = new ArrayList<SearchedClassItem>();
		this.sequence = 0;
	}
	
	public void generateSearchedClassItems() {
		generateSearchedClassItems(false);
	}
	
	/**
	 * generate all classes of a dataset 
	 */
	public void generateAllClassItems(){
		ResultSet rdfResultSet = SPARQLHandler.executeQuery(dataset, getAllClassSPARQLQuery());
		generateSearchedClassItemsFromResultSet(rdfResultSet);
	}
	
	public void generateSearchedClassItems(Boolean forceUriSearch) {
			ResultSet rdfResultSet = SPARQLHandler.executeQuery(dataset,getSPARQLQuery(forceUriSearch));
			generateSearchedClassItemsFromResultSet(rdfResultSet);
	}

	public void generateSearchedClassItemsFromResultSet(ResultSet rdfResultSet) {
		String currClass = "";
		SearchedClassItem currSearchedClass = null;
		while (rdfResultSet.hasNext()) {
			QuerySolution row = rdfResultSet.next();
			String loopClass = row.get("class").toString();
			if (!loopClass.equals(currClass)) {
				// new class found. Will create a new SearchedClassItem
				if (currSearchedClass != null)
					this.searched_items.add(currSearchedClass);
				this.sequence++;
				currSearchedClass = new SearchedClassItem(loopClass,
						this.sequence);
				currClass = loopClass.toString();

			}
			RDFNode label = row.get("label");
			if (label != null && label.toString() != null
					&& !label.toString().equals(""))
				currSearchedClass.addLabel(
						SPARQLHandler.getLabelLanguage(label),
						SPARQLHandler.getLabelText(label));
		}
		// adding the last searched classItem
		if (currSearchedClass != null)
			this.searched_items.add(currSearchedClass);
	}


	@JsonIgnore
	public String getSPARQLQuery(Boolean forceUriSearch) {
		
		String query = "";
		URL url = (forceUriSearch) ? Resources.getResource("builder_queries/SearchLabelsAndURIsQuery.sparql") :
				Resources.getResource("builder_queries/SearchLabelsQuery.sparql");
			
		try {
			query = Resources.toString(url, Charsets.UTF_8);
			query = query.replace("%%Search-String%%", this.search_string);
		} catch (IOException e) {
			logger.error("Error: {}",e.getMessage());
		}
		
		System.out.println(query);
		return query;
	}
	
	@JsonIgnore
	public String getAllClassSPARQLQuery() {
		String query = "";
		URL url = Resources.getResource("builder_queries/GetAllClasses.sparql");
		try {
			query = Resources.toString(url, Charsets.UTF_8);
		} catch (IOException e) {
			logger.error("Error: {}",e.getMessage());
		}
		
		return query;
	}

}
