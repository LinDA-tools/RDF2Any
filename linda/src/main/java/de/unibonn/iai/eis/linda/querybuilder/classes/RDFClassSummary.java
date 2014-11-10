package de.unibonn.iai.eis.linda.querybuilder.classes;

import java.util.ArrayList;
import java.util.List;

import de.unibonn.iai.eis.linda.helper.SPARQLHandler;
import de.unibonn.iai.eis.linda.querybuilder.objects.RDFObject;

/**
 * @author gauravsingharoy
 * 
 *         This class will contain an RDF class and some summary items, viz, total objects and some object examples
 * 
 */
public class RDFClassSummary {

	public String dataset;
	public String uri;
	public String name;
	public Integer total_objects;
	public List<RDFObject> sample_objects;
	
	public RDFClassSummary(String dataset, String uri){
		this.uri = uri;
		this.name = SPARQLHandler.getLabelFromNode(dataset, uri, "EN");
		this.dataset = dataset;
		this.total_objects = null;
		this.sample_objects = new ArrayList<RDFObject>();
	}
	
	public void generateSummaryItems(){
		generateTotalObjectsCount();
		generateSampleObjects();
	}
	
	public void generateTotalObjectsCount(){
		this.total_objects = 0;
	}
	
	public void generateSampleObjects(){
		generateSampleObjects(5);
	}
	public void generateSampleObjects(Integer limit){
		
	}
}
