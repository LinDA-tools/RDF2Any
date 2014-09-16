package de.unibonn.iai.eis.linda.querybuilder.classes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.unibonn.iai.eis.linda.helper.LuceneHelper;
import de.unibonn.iai.eis.linda.helper.SPARQLHandler;

public class RDFClass {

	/**
	 * @author gauravsingharoy
	 * 
	 *         This class will contain an RDF class and its properties
	 * 
	 */
	public String uri;
	public String dataset;
	public String label;
	public List<RDFClassProperty> properties;

	public RDFClass(String dataset, String uri) {
		this.uri = uri;
		this.label = SPARQLHandler.getLabelFromNode(dataset, uri, "EN");
		this.dataset = dataset;
		this.properties = new ArrayList<RDFClassProperty>();
	}

	// this method will generate properties for the object from SPARQL endpoint
	public void generatePropertiesFromSPARQL() {
		// Get dataType properties
		ResultSet dataTypeProperties = SPARQLHandler.executeQuery(this.dataset,
				getPropertiesSPARQLQuery("datatype"));
		addRdfResultSetToProperties(dataTypeProperties, "datatype");
		// Get object properties
		ResultSet objectProperties = SPARQLHandler.executeQuery(this.dataset,
				getPropertiesSPARQLQuery("object"));
		addRdfResultSetToProperties(objectProperties, "object");
		ResultSet schemaProperties = SPARQLHandler.executeQuery(this.dataset,
				getPropertiesSPARQLQuery("schema"));
		addRdfResultSetToProperties(schemaProperties, "schema");
	}

	public void addRdfResultSetToProperties(ResultSet resultSetProperties,
			String type) {
		addRdfResultSetToProperties(resultSetProperties, type, false);
	}

	// This method adds the ResultSet properties to the properties List
	public void addRdfResultSetToProperties(ResultSet resultSetProperties,
			String type, Boolean doStatisticalQueries) {
		while (resultSetProperties.hasNext()) {
			QuerySolution row = resultSetProperties.next();
			RDFNode propertyNode = row.get("property");
			Literal propertyLabel = (Literal) row.get("label");
			String propertyNodeUri = propertyNode.toString();
			if (!type.equalsIgnoreCase("schema")
					|| (type.equalsIgnoreCase("schema") && !isPropertyPresent(propertyNodeUri))) {
				RDFClassProperty p = new RDFClassProperty(propertyNodeUri,
						type, SPARQLHandler.getLabelName(propertyLabel),
						new RDFClassPropertyRange("", ""));
				if (doStatisticalQueries)
					p.generateCountOfProperty(this.uri, this.dataset);
				p.generateRange(this.dataset);
				properties.add(p);
			}

		}
	}

	// this method returns the query to get properties of a class
	public String getPropertiesSPARQLQuery(String propertyType) {
		String query = SPARQLHandler.getPrefixes();
		if (propertyType.equals("schema")) {
			query += "SELECT DISTINCT ?property ?label WHERE { ?property rdfs:domain <"
					+ this.uri
					+ ">. ?property rdfs:range ?range.  ?property rdfs:label ?label.";
		} else {
			query += "SELECT DISTINCT ?property ?label WHERE { ?concept rdf:type <"
					+ this.uri
					+ ">. ?concept ?property ?o. ?property rdfs:label ?label. ";

			if (propertyType.equals("object"))
				query += " ?property rdf:type owl:ObjectProperty. ?property rdfs:range ?range. ";
			else if (propertyType.equals("datatype"))
				query += " ?property rdf:type owl:DatatypeProperty. ";
		}
		query += " FILTER(langMatches(lang(?label), 'EN'))} LIMIT 50";
		return query;
	}

	private Boolean isPropertyPresent(String propertyUri) {
		Boolean result = false;
		for (RDFClassProperty p : properties) {
			if (p.uri.equalsIgnoreCase(propertyUri)) {
				result = true;
				break;
			}
		}
		return result;
	}

	//this method creates indexes in lucene for the properties
	public void generateLuceneIndexes() throws IOException{
		
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
		File indexPath = new File(LuceneHelper.classPropertiesDir(this.dataset));
		Directory index = new SimpleFSDirectory(indexPath);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);
		
	    IndexWriter w = new IndexWriter(index, config);
	    for(RDFClassProperty property:this.properties){
	    	addLuceneDoc(w, property);
	    }
	}
	
	//this method adds a doc for property in lucene index
	public void addLuceneDoc(IndexWriter w, RDFClassProperty property) throws IOException{
		Document d = new Document();
		d.add(new StringField("class_uri", this.uri, Field.Store.YES));
		d.add(new StringField("uri", property.uri, Field.Store.YES));
		d.add(new StringField("label", property.label, Field.Store.YES));
		d.add(new StringField("count", property.count.toString(), Field.Store.YES));
		d.add(new StringField("multiple_properties_for_same_node", property.multiplePropertiesForSameNode.toString(), Field.Store.YES));
		d.add(new StringField("type", property.type, Field.Store.YES));
		d.add(new StringField("range_uri", property.range.uri, Field.Store.YES));
		d.add(new StringField("range_label", property.range.label, Field.Store.YES));
		w.addDocument(d);
		System.out.println("Created index for "+property.toString());
	}
	
	public String toString() {
		String result = "uri : " + this.uri + ", dataset : " + this.dataset;
		for (Integer i = 0; i < properties.size(); i++) {
			result += "\n" + properties.get(i).toString();
		}

		return result;

	}

}
