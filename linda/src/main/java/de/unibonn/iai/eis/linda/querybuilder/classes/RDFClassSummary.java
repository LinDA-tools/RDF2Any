package de.unibonn.iai.eis.linda.querybuilder.classes;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

import de.unibonn.iai.eis.linda.helper.SPARQLHandler;

/**
 * @author gauravsingharoy
 * 
 *         This class will contain an RDF class and some summary items, viz,
 *         total objects and some object examples
 * 
 */
public class RDFClassSummary {

	public String dataset;
	public String uri;
	public String label;
	public Integer total_objects;
	public Set<Object> sample_objects;

	public RDFClassSummary(String dataset, String uri) {
		this.uri = uri;
		this.label = SPARQLHandler.getLabelFromNode(dataset, uri, "EN");
		this.dataset = dataset;
		this.total_objects = null;
		this.sample_objects = new HashSet<Object>();
	}


	public void generateSummaryItems(Integer limit){
		generateTotalObjectsCount();
		generateSampleObjects(limit);
	}
	
	public void generateTotalObjectsCount() {
		this.total_objects = 0;
		try {
			String countQuery = "";
			URL url = Resources.getResource("builder_queries/GetInstanceCount.sparql");
			countQuery = Resources.toString(url, Charsets.UTF_8);
			countQuery = countQuery.replace("%%Concept-URI%%",this.uri);
			System.out.println(countQuery);

			ResultSet countResultSet = SPARQLHandler.executeQuery(this.dataset,countQuery, true);
			if (countResultSet.hasNext()) {
				QuerySolution row = countResultSet.next();
				this.total_objects = row.get("totalcount").asLiteral().getInt();
			}
		} catch (Exception e) {
			System.out.println("Error in generating total objects count for " + this.uri + " : " + e.toString());
		}
	}

	public void generateSampleObjects(Integer limit) {
		try {
			
			String query = "";
			URL url = Resources.getResource("builder_queries/PopularInstances.sparql");
			query = Resources.toString(url, Charsets.UTF_8);
			query = query.replace("%%Concept-URI%%",this.uri);
			query = query.replace("%%limit%%", limit.toString());
			
			System.out.println(query);
			ResultSet rdfResultSet = SPARQLHandler.executeQuery(this.dataset, query);
			
			while(rdfResultSet.hasNext()){
				QuerySolution row = rdfResultSet.next();
				Map<String, String> objectMap = new HashMap<String, String>();
				objectMap.put("uri", row.get("instance").asResource().getURI());
				objectMap.put("label", row.get("label").asLiteral().getValue().toString());
				this.sample_objects.add(objectMap);
			}
			
		} catch (Exception e) {
			System.out.println("Error in generating sample objects for "+ this.uri + " : " + e.toString());
		}
	}
}
