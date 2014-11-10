package de.unibonn.iai.eis.linda.querybuilder.classes;

import java.util.ArrayList;
import java.util.List;

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
	public String name;
	public Integer total_objects;
	public List<RDFObject> sample_objects;

	public RDFClassSummary(String dataset, String uri) {
		this.uri = uri;
		this.name = SPARQLHandler.getLabelFromNode(dataset, uri, "EN");
		this.dataset = dataset;
		this.total_objects = null;
		this.sample_objects = new ArrayList<RDFObject>();
	}

	public void generateSummaryItems() {
		generateTotalObjectsCount();
		generateSampleObjects();
	}

	public void generateTotalObjectsCount() {
		this.total_objects = 0;
		try {
			String countQuery = SPARQLHandler.getPrefixes();
			countQuery += " SELECT  (count(DISTINCT ?c) AS ?totalcount)  where {?c rdf:type <"
					+ this.uri + ">. } ";
			ResultSet countResultSet = SPARQLHandler.executeQuery(dataset,
					countQuery);
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

	}
}
