package de.unibonn.iai.eis.linda.querybuilder.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import de.unibonn.iai.eis.linda.helper.SPARQLHandler;
import de.unibonn.iai.eis.linda.querybuilder.objects.RDFObject;

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
	public List<Object> sample_objects;

	public RDFClassSummary(String dataset, String uri) {
		this.uri = uri;
		this.label = SPARQLHandler.getLabelFromNode(dataset, uri, "EN");
		this.dataset = dataset;
		this.total_objects = null;
		this.sample_objects = new ArrayList<Object>();
	}

	public void generateSummaryItems() {
		generateSummaryItems(5);
	}

	public void generateSummaryItems(Integer limit){
		generateTotalObjectsCount();
		generateSampleObjects(limit);
	}
	
	public void generateTotalObjectsCount() {
		this.total_objects = 0;
		try {
			String countQuery = SPARQLHandler.getPrefixes();
			countQuery += " SELECT  (count(DISTINCT ?c) AS ?totalcount)  where {?c rdf:type <"
					+ this.uri + ">. } ";
			ResultSet countResultSet = SPARQLHandler.executeQuery(this.dataset,
					countQuery, true);
			if (countResultSet.hasNext()) {
				QuerySolution row = countResultSet.next();
				this.total_objects = SPARQLHandler.getIntegerValueOfLiteral(row
						.get("totalcount"));
			}
		} catch (Exception e) {
			System.out.println("Error in generating total objects count for "
					+ this.uri + " : " + e.toString());
		}
	}

	public void generateSampleObjects() {
		generateSampleObjects(5);
	}

	public void generateSampleObjects(Integer limit) {
		try {
			String query = SPARQLHandler.getPrefixes();
			query += " SELECT distinct ?object ?label ";
			query += " WHERE { ";
			query += " ?object rdf:type <"+this.uri+">.";
			query += "  ?object rdfs:label ?label.  ";
			query += " FILTER(bound(?label) && langMatches(lang(?label), \"EN\"))} LIMIT "+limit.toString();
			ResultSet rdfResultSet = SPARQLHandler.executeQuery(this.dataset,
					query, true);
			if(!rdfResultSet.hasNext()){
				String query2 = SPARQLHandler.getPrefixes();
				query2 += " SELECT distinct ?object ?label ";
				query2 += " WHERE { ";
				query2 += " ?object rdf:type <"+this.uri+">.";
				query2 += "  ?object rdfs:label ?label.  ";
				query += " } LIMIT "+limit.toString();
				rdfResultSet = SPARQLHandler.executeQuery(this.dataset,
						query, true);
			}
			
			while(rdfResultSet.hasNext()){
				QuerySolution row = rdfResultSet.next();
				Map<String, String> objectMap = new HashMap<String, String>();
				objectMap.put("uri", row.get("object").toString());
				objectMap.put("label", SPARQLHandler.getLabelName(row.get("label")));
				this.sample_objects.add(objectMap);
			}
		} catch (Exception e) {
			System.out.println("Error in generating sample objects for "
					+ this.uri + " : " + e.toString());
		}
	}
}
