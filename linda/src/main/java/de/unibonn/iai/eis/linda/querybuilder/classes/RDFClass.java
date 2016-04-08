package de.unibonn.iai.eis.linda.querybuilder.classes;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.owlike.genson.annotation.JsonIgnore;

import de.unibonn.iai.eis.linda.helper.CommonHelper;
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
	public String status;
	
	public RDFClass(String dataset, String uri) {
		this.uri = uri;
		this.label = SPARQLHandler.getLabelFromNode(dataset, uri, "EN");
		this.dataset = dataset;
		this.properties = new ArrayList<RDFClassProperty>();
		this.status = "";
	}

	public RDFClass(String dataset, String uri, String label) {
		this.uri = uri;
		this.label = label;
		this.dataset = dataset;
		this.properties = new ArrayList<RDFClassProperty>();
		this.status = "";
	}

	// this method will generate properties for the object from SPARQL endpoint

	public void generatePropertiesFromSPARQL(Boolean doStatisticalQueries) {
		// Get dataType properties
		try{
			ResultSet dataTypeProperties = SPARQLHandler.executeQuery(this.dataset, getPropertiesInformationAndCountSPARQLQuery("DatatypeProperty"));
			addRdfResultSetToProperties(dataTypeProperties, "data",doStatisticalQueries);
			this.status = "Success";
		} catch (Exception e){
			try{
				ResultSet dataTypeProperties = SPARQLHandler.executeQuery(this.dataset, getPropertiesSPARQLQuery("DatatypeProperty"));
				addPropertyInformationAndCount(dataTypeProperties, "data");
				this.status = "Success";
			} catch (Exception e1){
				this.status = "Error retrieving data from "+this.dataset;
			}
		}
		// Get object properties
		try{
			ResultSet objectProperties = SPARQLHandler.executeQuery(this.dataset,getPropertiesInformationAndCountSPARQLQuery("ObjectProperty"));
			addRdfResultSetToProperties(objectProperties, "object",doStatisticalQueries);
			this.status = "Success";
		} catch (Exception e){
			try{
				ResultSet objectProperties = SPARQLHandler.executeQuery(this.dataset, getPropertiesSPARQLQuery("ObjectProperty"));
				addPropertyInformationAndCount(objectProperties, "object");
				this.status = "Success";
			} catch (Exception e1){
				this.status = "Error retrieving data from "+this.dataset;
			}
		}
	}

	// This method adds the ResultSet properties to the properties List
	public void addRdfResultSetToProperties(ResultSet resultSetProperties, String type, Boolean doStatisticalQueries) {
		while (resultSetProperties.hasNext()) {
			QuerySolution row = resultSetProperties.next();
			Resource propertyNode = row.get("property").asResource();
			Literal propertyLabel = row.getLiteral("label");
			String propertyNodeUri = propertyNode.getURI();
			Resource range = (row.get("range") == null) ? null : row.get("range").asResource();
			Literal rangeLabel = (row.get("rangeLabel") == null) ? null : row.getLiteral("rangeLabel");
			
			if (!isPropertyPresent(propertyNodeUri)) {
				RDFClassProperty p = new RDFClassProperty(propertyNodeUri, type, propertyLabel.getString(), new RDFClassPropertyRange("", ""));
				if (doStatisticalQueries) p.addCountOfProperty(row.get("cnt").asLiteral().getInt());
				p.addRange(range, rangeLabel, type);
				p.addHintExample(this.dataset);
				properties.add(p);
			}

		}
	}

	
	public void addPropertyInformationAndCount(ResultSet resultSetProperties, String type){
		while (resultSetProperties.hasNext()) {
			QuerySolution row = resultSetProperties.next();
			Resource propertyNode = row.get("property").asResource();
			Literal propertyLabel = row.getLiteral("label");
			String propertyNodeUri = propertyNode.getURI();
			
			if (!isPropertyPresent(propertyNodeUri)) {
				ResultSet info = SPARQLHandler.executeQuery(this.dataset, getInformationAndCountSPARQLQuery(propertyNodeUri));
				
				Resource range = null;
				Literal rangeLabel = null;
				Literal count = null;
				while(info.hasNext()){
					QuerySolution infoRow = info.next();
					range = (infoRow.get("range") == null) ? null : infoRow.get("range").asResource();
					rangeLabel = (infoRow.get("rangeLabel") == null) ? null : infoRow.getLiteral("rangeLabel");
					count = (infoRow.get("cnt") == null) ? null : infoRow.getLiteral("cnt");
				}
				
				RDFClassProperty p = new RDFClassProperty(propertyNodeUri, type, propertyLabel.getString(), new RDFClassPropertyRange("", ""));
				p.addCountOfProperty(count.getInt());
				p.addRange(range, rangeLabel, type);
				p.addHintExample(this.dataset);
				properties.add(p);
			}
		}
	}
	
	
	
	@JsonIgnore
	public String getPropertiesInformationAndCountSPARQLQuery(String propertyType) {
		String query = "";
		URL url = Resources.getResource("builder_queries/GetPropertiesInformationAndCount.sparql");
			
		try {
			query = Resources.toString(url, Charsets.UTF_8);
			query = query.replace("%%Concept-URI%%", this.uri);
			query = query.replace("%%Type%%", propertyType);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		System.out.println(query);
		return query;
	}
	
	@JsonIgnore
	public String getPropertiesSPARQLQuery(String propertyType) {
		String query = "";
		URL url = Resources.getResource("builder_queries/GetProperties.sparql");
			
		try {
			query = Resources.toString(url, Charsets.UTF_8);
			query = query.replace("%%Concept-URI%%", this.uri);
			query = query.replace("%%Type%%", propertyType);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		System.out.println(query);
		return query;
	}
	
	@JsonIgnore
	public String getInformationAndCountSPARQLQuery(String propertyURI) {
		String query = "";
		URL url = Resources.getResource("builder_queries/PropertyInformationAndCount.sparql");
			
		try {
			query = Resources.toString(url, Charsets.UTF_8);
			query = query.replace("%%Concept-URI%%", this.uri);
			query = query.replace("%%Property-URI%%", propertyURI);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		System.out.println(query);
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

	public void generateLuceneIndexes(IndexWriter w) {
		System.out.println("Creating indexes for class .. " + this.label + " <"
				+ this.uri + ">");
		for (RDFClassProperty property : this.properties) {
			try {
				addLucenePropertyDoc(property);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	// this method creates indexes in lucene for the properties
	@SuppressWarnings("deprecation")
	public void generateLuceneIndexes() throws IOException {
		deleteIndexes();
		System.out.println("Creating indexes for class .. " + this.label + " <"+ this.uri + ">");
		
		for (RDFClassProperty property : this.properties) {
			addLucenePropertyDoc(property);
		}

		addLuceneValidatorDoc();
	}
	
	
	private static String byteToHex(byte[] arr){
		 StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < arr.length; i++) {
	         sb.append(Integer.toString((arr[i] & 0xff) + 0x100, 16).substring(1));
	        }
	    return sb.toString();
	}
	
	private static byte[] createMD5(String s){
		 MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	    md.update(s.getBytes());
	 
	    return md.digest();
	}
	
	
	// this method adds a doc for property in lucene index
	public void addLucenePropertyDoc( RDFClassProperty property)
			throws IOException {
		StandardAnalyzer analyzer = new StandardAnalyzer(LuceneHelper.LUCENE_VERSION);
		File indexPath = new File(LuceneHelper.classPropertiesDir(this.dataset));
		
		IndexWriterConfig config = new IndexWriterConfig(LuceneHelper.LUCENE_VERSION,analyzer);
		
		IndexWriter w = new IndexWriter(FSDirectory.open(indexPath), config);
		
		Document d = new Document();
		
		d.add(new TextField("class_uri", "s" + byteToHex(createMD5(this.uri)) + "e",
				Field.Store.YES));
		d.add(new StringField("uri", property.uri, Field.Store.YES));
		d.add(new StringField("label", property.label, Field.Store.YES));
		d.add(new StringField("count", property.count.toString(),
				Field.Store.YES));
		d.add(new StringField("multiple_properties_for_same_node",
				property.multiplePropertiesForSameNode.toString(),
				Field.Store.YES));
		d.add(new StringField("type", property.type, Field.Store.YES));
		d.add(new StringField("range_uri", property.range.uri, Field.Store.YES));
		d.add(new StringField("range_label", property.range.label,
				Field.Store.YES));
		d.add(new StringField("hintExample",
				property.hintExample.toString(),
				Field.Store.YES));
		w.addDocument(d);

		w.close();
	}
	

	// this method searches for a matching class

	public static RDFClass searchRDFClass(String dataset, String classUri) throws ParseException {
		return searchRDFClass(dataset, classUri, true);
	}
	
	public static RDFClass reindexRDFClass(String dataset, String classUri) throws ParseException {
			System.out.println("Reindexing for " + classUri + ". Will create indexes now ..." );
			RDFClass resultClass = new RDFClass(dataset, classUri);
			resultClass.generatePropertiesFromSPARQL(true);
			try {
					resultClass.generateLuceneIndexes();
					System.out.println("Index entry created for " + classUri);
			} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
			return resultClass;
	}
	

	public static RDFClass searchRDFClass(String dataset, String classUri,
			Boolean forceIndexCreation) throws ParseException {
		RDFClass resultClass = new RDFClass(dataset, classUri);
		Boolean getFromSPARQL = false;
		if (resultClass.isIndexCreated()) {
			List<Document> hits = resultClass.getPropertyIndexDocuments();

			if (hits.size() > 0) {
				// properties in lucene index
				for (int i = 0; i < hits.size(); ++i) {
					Document d = hits.get(i);
					if (LuceneHelper.getUriFromIndexEntry(d.get("class_uri"))
							.equalsIgnoreCase(byteToHex(createMD5(classUri)))) {
						resultClass.properties
								.add(new RDFClassProperty(
										d.get("uri"),
										d.get("type"),
										d.get("label"),
										Integer.parseInt(d.get("count")),
										Boolean.parseBoolean(d
												.get("multiple_properties_for_same_node")),
										new RDFClassPropertyRange(d
												.get("range_uri"), d
												.get("range_label"))));

					}

				}

			} else {
				getFromSPARQL = true;
			}
		} else {
			getFromSPARQL = true;
		}
		if (getFromSPARQL) {
			// generating properties from SPARQL
			System.out.println("No indexed entry found for " + classUri
					+ ". Will create indexes now ..." );
			if (forceIndexCreation) {
				resultClass.generatePropertiesFromSPARQL(true);
				try {
					resultClass.generateLuceneIndexes();
					System.out.println("Index entry created for " + classUri);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				resultClass.generatePropertiesFromSPARQL(false);
			}
		}
		return resultClass;
	}

	public static void generateIndexesForDataset(String dataset)
			throws IOException {
		generateIndexesForDataset(dataset, false);
	}

	// this method creates indexes for all the classes of a dataset
	public static void generateIndexesForDataset(String dataset,
			Boolean forceNew) throws IOException {
		String classesQuery = SPARQLHandler.getPrefixes();
		List<String> failedClasses = new ArrayList<String>();
		classesQuery += " select distinct ?class where {?class rdf:type owl:Class.  ?o rdf:type ?class. ?class rdfs:label ?label. FILTER(langMatches(lang(?label), \"EN\"))}";
		ResultSet classesResultSet = SPARQLHandler.executeQuery(dataset,
				classesQuery);
		Integer classCounter = 0;

		while (classesResultSet.hasNext()) {
			classCounter++;
			System.out
					.println(classCounter.toString()
							+ ". ################################################################");
			QuerySolution row = classesResultSet.next();
			RDFClass classNode = new RDFClass(dataset, row.get("class")
					.toString());
			if (forceNew || (!forceNew && !classNode.isIndexCreated())) {
				try {
					System.out.println("Evaluating properties of "
							+ classNode.label + " <" + classNode.uri + ">");
					classNode.generatePropertiesFromSPARQL(true);
					classNode.generateLuceneIndexes();
				} catch (Exception e) {
					System.out.println("Failed to create index for the class "
							+ classNode.label + " <" + classNode.uri + ">");
					failedClasses.add(classNode.uri);
				}
			} else {
				System.out.println("Index already created for "
						+ classNode.label + " <" + classNode.uri
						+ ">. Moving to the next class ...");
			}
		}

		System.out.println("Finished creating indexes for "
				+ classCounter.toString() + " classes ... ");
		System.out.println("Failed classes : " + failedClasses.size());
		if (failedClasses.size() > 0) {
			for (String c : failedClasses) {
				System.out.println("Failed to create indexes for : " + c);
			}
		}
	}

	public Boolean isIndexCreated() {
		Boolean result = false;
		Document d = getValidatorIndexDocument();
		if (d != null) {
			result = true;
		}
		return result;
	}

	// this method writes a doc which confirms that indexes have been created
	// for the class

	public void addLuceneValidatorDoc() throws IOException {
		StandardAnalyzer analyzer = new StandardAnalyzer(
				LuceneHelper.LUCENE_VERSION);
		File indexPath = new File(
				LuceneHelper.classPropertiesValidatorDir(this.dataset));
		Directory index = new SimpleFSDirectory(indexPath);
		IndexWriterConfig config = new IndexWriterConfig(
				LuceneHelper.LUCENE_VERSION, analyzer);
		IndexWriter w = new IndexWriter(index, config);
		Document d = new Document();
		d.add(new TextField("uri", "s" + this.uri + "e",
				Field.Store.YES));
		w.addDocument(d);
		w.close();
	}

	private Document getValidatorIndexDocument() {
		Document resultD = null;
		try {
			StandardAnalyzer analyzer = new StandardAnalyzer(
					LuceneHelper.LUCENE_VERSION);
			File indexPath = new File(
					LuceneHelper.classPropertiesValidatorDir(this.dataset));
			if(!indexPath.exists()){
				analyzer.close();
				return null;
			}
			Directory index = new SimpleFSDirectory(indexPath);
			Query q;

			q = new QueryParser(LuceneHelper.LUCENE_VERSION, "uri", analyzer)
					.parse("s" + this.uri + "e");
			int hitsPerPage = 150;
			IndexReader reader;
			
			reader = DirectoryReader.open(index);
			
//			for (int i = 0; i < reader.numDocs(); i++){
//				System.out.println(reader.document(i).toString());
//			}
			
			IndexSearcher searcher = new IndexSearcher(reader);
			TopScoreDocCollector collector = TopScoreDocCollector.create(
					hitsPerPage, true);
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			if (hits.length > 0) {
				for (int i = 0; i < hits.length; ++i) {
					int docId = hits[i].doc;
					Document d = searcher.doc(docId);
					if (LuceneHelper.getUriFromIndexEntry(d.get("uri")).equalsIgnoreCase(this.uri+"")) {
						resultD = d;
						break;
					}
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return resultD;
	}

	@SuppressWarnings("deprecation")
	private List<Document> getPropertyIndexDocuments() {
		List<Document> docs = new ArrayList<Document>();
		try {
			StandardAnalyzer analyzer = LuceneHelper.getAnalyzer();
			File indexPath = new File(LuceneHelper.classPropertiesDir(dataset));
			Directory index = new SimpleFSDirectory(indexPath);
			Query q;

			q = new QueryParser(LuceneHelper.LUCENE_VERSION, "class_uri",
					analyzer).parse("s" + byteToHex(createMD5(this.uri)) + "e");

			int hitsPerPage = 300;
			IndexReader reader;

			reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			TopScoreDocCollector collector = TopScoreDocCollector.create(
					hitsPerPage, true);
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			if (hits.length > 0) {
				System.out.println("Found indexed properties for " + this.uri);
				for (int i = 0; i < hits.length; ++i) {
					int docId = hits[i].doc;
					Document d = searcher.doc(docId);
					if (LuceneHelper.getUriFromIndexEntry(d.get("class_uri"))
							.equalsIgnoreCase(byteToHex(createMD5(this.uri)) + "")) {
						docs.add(d);
					}

				}
			}
			reader.close();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return docs;
	}

	// This method deletes existing indexes
	public void deleteIndexes() {
		System.out.println("Deleting indexes of " + this.uri);
		if (isIndexCreated()) {
			try {
				StandardAnalyzer analyzer = new StandardAnalyzer(
						LuceneHelper.LUCENE_VERSION);
				File indexPath = new File(
						LuceneHelper.classPropertiesValidatorDir(dataset));
				Directory index = new SimpleFSDirectory(indexPath);
				IndexWriterConfig config = new IndexWriterConfig(
						LuceneHelper.LUCENE_VERSION, analyzer);
				IndexWriter vWriter = new IndexWriter(index, config);
				Query q;

				q = new QueryParser(LuceneHelper.LUCENE_VERSION, "uri",
						analyzer).parse("s" + byteToHex(createMD5(this.uri)) + "e");
				vWriter.deleteDocuments(q);
				vWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			StandardAnalyzer analyzer = new StandardAnalyzer(
					LuceneHelper.LUCENE_VERSION);
			File indexPath2 = new File(LuceneHelper.classPropertiesDir(dataset));
			Directory index;
			index = new SimpleFSDirectory(indexPath2);
			IndexWriterConfig config = new IndexWriterConfig(
					LuceneHelper.LUCENE_VERSION, analyzer);
			IndexWriter pWriter = new IndexWriter(index, config);
			Query q;

			q = new QueryParser(LuceneHelper.LUCENE_VERSION, "class_uri",
					analyzer).parse("s" + byteToHex(createMD5(this.uri)) + "e");
			pWriter.deleteDocuments(q);
			pWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// This method returns the RDFClassProperty by looking up the properties in
	// the class through String uri
	public RDFClassProperty getPropertyFromStringUri(String propertyUri) {
		RDFClassProperty foundProp = null;
		for (RDFClassProperty prop : this.properties) {
			if (prop.uri.equalsIgnoreCase(propertyUri)) {
				foundProp = prop;
				break;
			}
		}
		return foundProp;
	}

	@JsonIgnore
	public String getVariableName() {
		return CommonHelper.getVariableName(this.label, "thing");
	}

	/*
	 * START RDB related methods
	 */
	// This method returns the create table script for this class
	@JsonIgnore
	public String getTableCreationScript(Boolean allProperties) {
		Boolean existsForeignKey = false;
		List<String> tablesCreated = new ArrayList<String>();
		tablesCreated.add(getTableName());
		String result1 = "";
		String result2 = "";
		String result3 = "";

		if (allProperties)
			result1 += "\n\n\n-- START Table creation section for main class table";
		result1 += "\n\n\nDROP TABLE IF EXISTS " + getTableName()
				+ " CASCADE;\nCREATE TABLE " + getTableName()
				+ "\n(\nid int PRIMARY KEY,\nuri varchar(300),\nname text";

		if (allProperties) {
			for (RDFClassProperty property : this.properties) {
				if (!property.multiplePropertiesForSameNode) {
					result1 += ",\n" + property.getTableAttributeName() + " "
							+ property.getTableAttributeType();
					if (property.type.equals("object"))
						result1 += " REFERENCES "
								+ CommonHelper.getVariableName(
										property.range.label, "thing")
								+ "s(id)";
					if (property.range.isLanguageLiteral())
						result1 += ",\n" + property.getTableAttributeName()
								+ "Lang varchar(6)";
					if (property.type.equalsIgnoreCase("object"))
						existsForeignKey = true;
				}
			}
		}
		result1 += "\n);";
		if (allProperties)
			result1 += "\n\n\n-- END Table creation section for main class table";
		// Section to create tables for normalizations
		if (allProperties) {
			result2 += "\n\n\n-- START table creation scripts for normalized property tables";
			String classVariableName = getVariableName();
			for (RDFClassProperty property : this.properties) {
				if (property.multiplePropertiesForSameNode) {
					result2 += "\n\nDROP TABLE IF EXISTS "
							+ property.getTableName(this)
							+ " CASCADE;\nCREATE TABLE "
							+ property.getTableName(this)
							+ "\n(id int PRIMARY KEY,";
					result2 += "\n" + classVariableName + "_id int REFERENCES "
							+ getTableName() + "(id)" + ",";
					result2 += "\n" + property.getTableAttributeName() + " "
							+ property.getTableAttributeType();
					if (property.type.equals("object"))
						result2 += " REFERENCES "
								+ CommonHelper.getVariableName(
										property.range.label, "thing")
								+ "s(id)";
					if (property.range.isLanguageLiteral())
						result2 += ",\n" + property.getTableAttributeName()
								+ "Lang varchar(6)";
					result2 += "\n);";
				}
			}
			result2 += "\n\n\n-- END table creation scripts for normalized property tables";

			// Section to get all related tables
			result3 += "\n\n\n-- START table creation scripts properties pointing to other classes";
			for (RDFClassProperty property : this.properties) {
				if (property.type.equalsIgnoreCase("object")
						&& !property.range.label.equalsIgnoreCase("")) {
					RDFClass propertyRangeClass = new RDFClass(this.dataset,
							property.range.uri, property.range.label);
					if (!tablesCreated.contains(propertyRangeClass
							.getTableName())) {
						result3 += propertyRangeClass.getTableCreationScript();
						tablesCreated.add(propertyRangeClass.getTableName());
					}
				}
			}
			result3 += "\n\n\n-- END table creation scripts properties pointing to other classes";
		}

		return result3 + result1 + result2;
	}

	@JsonIgnore
	public String getTableCreationScript() {
		return getTableCreationScript(false);
	}

	@JsonIgnore
	public String getTableName() {
		return getVariableName() + "s";
	}

	// This method returns a list of tablenames needed to have this class
	@JsonIgnore
	public List<String> getTableNames() {
		List<String> tables = new ArrayList<String>();
		// adding the table for this class
		tables.add(getTableName());
		// adding related tables
		for (RDFClassProperty property : this.properties) {

			if (property.multiplePropertiesForSameNode) {
				tables.add(property.getTableName(this));
				if (property.type.equalsIgnoreCase("object")
						&& property.hasValidRange())
					tables.add(new RDFClass(this.dataset, property.range.uri,
							property.range.label).getTableName());
			} else {
				if (property.type.equalsIgnoreCase("object")
						&& property.hasValidRange())
					tables.add(new RDFClass(this.dataset, property.range.uri,
							property.range.label).getTableName());
			}

		}
		return tables;
	}

	/*
	 * END RDB related methods
	 */

	// this method removes the properties which are not present in the comma
	// separated properties string
	// if "all" is passed,then does not filter
	public void filterProperties(String properties) {
		if (properties != null && !properties.equalsIgnoreCase("all")) {
			String[] propertiesArr = properties.split(",");
			Iterator<RDFClassProperty> prop = this.properties.iterator();
			while (prop.hasNext()) {
				if (!Arrays.asList(propertiesArr).contains(prop.next().uri)) {
					prop.remove();
				}
			}
		}

	}

	@JsonIgnore
	public String toString() {
		String result = "uri : " + this.uri + ", dataset : " + this.dataset;
		for (Integer i = 0; i < properties.size(); i++) {
			result += "\n" + properties.get(i).toString();
		}

		return result;

	}

	// This method returns the subclasses hash map
	@JsonIgnore
	public Object getSubclassesHashMap() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("dataset", this.dataset);
		result.put("uri", this.uri);
		result.put("label", this.label);

		String query = SPARQLHandler.getPrefixes();
		query += " SELECT DISTINCT ?subclass_uri ?subclass_label WHERE {?subclass_uri rdfs:subClassOf <"
				+ this.uri
				+ ">. ?subclass_uri rdfs:label ?subclass_label.  FILTER(langMatches(lang(?subclass_label), 'EN')) } ";
		ResultSet rdfResultSet = SPARQLHandler
				.executeQuery(this.dataset, query);
		List<Object> subclasses = new ArrayList<Object>();
		while (rdfResultSet.hasNext()) {
			QuerySolution row = rdfResultSet.next();
			Map<String, String> subclassMap = new HashMap<String, String>();
			subclassMap.put("label",
					SPARQLHandler.getLabelName(row.get("?subclass_label")));
			subclassMap.put("uri", row.get("subclass_uri").toString());
			subclasses.add(subclassMap);
		}

		result.put("subclasses", subclasses);
		return result;
	}
	
	
	// DBPedia Locally
	public static void generateIndexesForDBPedia() throws IOException {
		boolean forceNew = false; 
		
		String classesQuery = SPARQLHandler.getPrefixes();
		List<String> failedClasses = new ArrayList<String>();
		classesQuery += " select distinct ?class where {?class rdf:type owl:Class. ?class rdfs:label ?label. FILTER(langMatches(lang(?label), \"en\"))}";
	
		ResultSet classesResultSet = SPARQLHandler.executeQuery("http://live.dbpedia.org/sparql",classesQuery);
		
		Integer classCounter = 0;

		while (classesResultSet.hasNext()) {
			classCounter++;
			System.out.println(classCounter.toString() + ". ################################################################");
			QuerySolution row = classesResultSet.next();
			RDFClass classNode = new RDFClass("http://dbpedia.org/sparql", row.get("class").toString());
			if (forceNew || (!forceNew && !classNode.isIndexCreated())) {
				try {
					System.out.println("Evaluating properties of "+ classNode.label + " <" + classNode.uri + ">");
					classNode.generatePropertiesFromSPARQL(true);
					classNode.generateLuceneIndexes();
				} catch (Exception e) {
					System.out.println("Failed to create index for the class "+ classNode.label + " <" + classNode.uri + ">");
					failedClasses.add(classNode.uri);
				}
			} else {
				System.out.println("Index already created for "+ classNode.label + " <" + classNode.uri + ">. Moving to the next class ...");
			}
		}

		System.out.println("Finished creating indexes for "+ classCounter.toString() + " classes ... ");
		System.out.println("Failed classes : " + failedClasses.size());
		if (failedClasses.size() > 0) {
			for (String c : failedClasses) {
				System.out.println("Failed to create indexes for : " + c);
			}
		}
	}
	
	public static RDFClass reindexLocalDBPediaRDFClass(String classUri) throws ParseException {
		System.out.println("Reindexing for " + classUri + ". Will create indexes now ..." );
		RDFClass resultClass = new RDFClass("http://live.dbpedia.org/sparql", classUri);
		resultClass.generatePropertiesFromSPARQL(true);
		try {
				resultClass.generateLuceneIndexes();
				System.out.println("Index entry created for " + classUri);
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		return resultClass;
}

}
