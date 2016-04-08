package de.unibonn.iai.eis.linda.helper;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Element;
import org.topbraid.spin.model.Filter;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.util.SPINExpressions;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;

public class InstanceExporter {

	// OpenRDF factory to create statement blocks
	private static ValueFactory factory = ValueFactoryImpl.getInstance();
	private static Resource spin_sparql_node = null; // holds the blank node for  sparql query
	
	// Static Classes Properties
	private static final org.openrdf.model.URI DATARESOURCE = factory.createURI("http://example.com/cqo#DataResource");
	private static final org.openrdf.model.URI TRANSFORMATION = factory.createURI("http://example.com/cqo#Transformation");
	private static final org.openrdf.model.URI QUERY = factory.createURI("http://example.com/cqo#Query");
	private static final org.openrdf.model.URI QUERYABLERESOURCE = factory.createURI("http://example.com/cqo#QueryableResource");
	private static final org.openrdf.model.URI NONQUERYABLERESOURCE = factory.createURI("http://example.com/cqo#NonQueryableResource");

	private static final org.openrdf.model.URI EXECUTION_TIME = factory.createURI("http://example.com/cqo#executionTime");
	private static final org.openrdf.model.URI HAS_QUERY = factory.createURI("http://example.com/cqo#hasQuery");
	private static final org.openrdf.model.URI EXECUTED_ON = factory.createURI("http://example.com/cqo#executedOn");//domain: Transformation
	private static final org.openrdf.model.URI RESULTS_IN = factory.createURI("http://example.com/cqo#resultsIn");
	private static final org.openrdf.model.URI RESULTED_FROM = factory.createURI("http://example.com/cqo#resultedFrom");
	private static final org.openrdf.model.URI WAS_DERIVED_FROM = factory.createURI("http://example.com/cqo#wasDerivedFrom");
	private static final org.openrdf.model.URI HAS_SERIALISATION = factory.createURI("http://example.com/cqo#hasSerialisation");
	private static final org.openrdf.model.URI WAS_ASSOCIATED_WITH = factory.createURI("http://www.w3.org/ns/prov#wasAssociatedWith"); //domain: Transformation, range: Agent
	private static final org.openrdf.model.URI WAS_ATTRIBUTED_TO = factory.createURI("http://www.w3.org/ns/prov#wasAttributedTo"); //domain: Resultset, range: Agent
	private static final org.openrdf.model.URI GENERATED_AT_TIME = factory.createURI("http://www.w3.org/ns/prov#generatedAtTime");//domain: Resultset


	private static final org.openrdf.model.URI HAS_QUERY_STRING = factory.createURI("http://example.com/cqo#hasQueryString");

	// Jena Model
	private static Model m;

	//repository endpoints
	private static String sesameServer = "http://localhost:8080/openrdf-sesame/";
	private static String repositoryID = "QueryRepository";
	private static Repository repo;
	static{
		repo = new HTTPRepository(sesameServer, repositoryID);
		try {
			repo.initialize();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static Map<String, Resource> blankNodes = null;
	
	//method for exporting data into triples
	public static void exporter(String queryString, String originalDataset, String converterType){
		m  = ModelFactory.createDefaultModel();
		blankNodes =  new HashMap<String, Resource>();
		spin_sparql_node = null;
		try {
			RepositoryConnection con = repo.getConnection();
			   
			//URI for Transformation
			Resource transformationURI = generateURI();
			
			//URI for original dataset
			Resource originalDatasetURI = factory.createURI(originalDataset);
			
			//URI for result set
			Resource resultsetURI = generateURI();
			
			//Date time of execution
			Literal execTime = factory.createLiteral(new Date());
	
			//original dataset format
			String originalFormat = "RDF";
			
			//resultset format
			String resultFormat = converterType;
	
			//Agent??
			
			//CREATING TRIPLES
			
			//transformation 
			con.add(transformationURI, RDF.TYPE, TRANSFORMATION);
			con.add(transformationURI, EXECUTED_ON, originalDatasetURI);
			con.add(transformationURI, RESULTS_IN, resultsetURI);
			con.add(transformationURI, EXECUTION_TIME, execTime);
			//con.add(transformationURI, WAS_ASSOCIATED_WITH,<AGENT>);

			con.add(originalDatasetURI, RDF.TYPE, QUERYABLERESOURCE);
			con.add(originalDatasetURI, HAS_SERIALISATION, factory.createLiteral(originalFormat));
			
			//check if data format for resultset is RDF or otherwise
			if (resultFormat.equals("RDF")){
				con.add(resultsetURI, RDF.TYPE, QUERYABLERESOURCE);
			}else{
				con.add(resultsetURI, RDF.TYPE, NONQUERYABLERESOURCE); 
			}
			con.add(resultsetURI, RESULTED_FROM, transformationURI);
			con.add(resultsetURI, WAS_DERIVED_FROM, originalDatasetURI);
			con.add(resultsetURI, HAS_SERIALISATION, factory.createLiteral(resultFormat));
			con.add(resultsetURI, GENERATED_AT_TIME, execTime);
			//con.add(resultsetURI, WAS_ATTRIBUTED_TO,<AGENT>);

			
			//query string, spin
			
			// Initialize system functions and templates
//			SPINModuleRegistry.get().init();
			
				ARQ2SPIN arq2SPIN = new ARQ2SPIN(m);
				ARQFactory arqFactory = ARQFactory.get(); 
				
				Query arqSelQuery = arqFactory.createQuery(queryString);
				
				//using spin api to generate a jena model for textual sparql query
				org.topbraid.spin.model.Select spinQuery = (Select) arq2SPIN.createQuery( arqSelQuery, null);
				
				//iterating through statements in model and converting to sesame from jena
				StmtIterator stmtIter = spinQuery.getModel().listStatements();
				while(stmtIter.hasNext()){
					Statement stmt = stmtIter.next();
					org.openrdf.model.Statement sesameStmt = jena2Sesame(stmt);
//					System.out.println(sesameStmt);
					con.add(sesameStmt);
				}
				//adding textual string
				con.add(transformationURI, HAS_QUERY, spin_sparql_node);
				con.add(spin_sparql_node, HAS_QUERY_STRING, factory.createLiteral(queryString));
				
				con.close();
				
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			//close connection
		}
				
				
	}


	private static org.openrdf.model.Resource generateURI(){
		String uri = "urn:"+UUID.randomUUID().toString();
		return factory.createURI(uri);
	}
	
	
	
	
	
	private static org.openrdf.model.Statement jena2Sesame(Statement jenaStmt){
		//variables for subject predicate object
		Node jena_subject = jenaStmt.asTriple().getSubject();
		Node jena_predicate = jenaStmt.asTriple().getPredicate();
		Node jena_object = jenaStmt.asTriple().getObject();
		
		Resource openrdf_subject = null;
		org.openrdf.model.URI openrdf_predicate = null;
		org.openrdf.model.Value openrdf_object = null;
		
		//convert subject
		if (jena_subject.isBlank()){
			String bNode = jena_subject.getBlankNodeId().toString();
			if (blankNodes.containsKey(bNode)){
				openrdf_subject = blankNodes.get(bNode);
			} else {
				openrdf_subject = factory.createURI(generateURI().toString());
				blankNodes.put(bNode, openrdf_subject);
			}
		} else {
			//if not blank it is a resource
			openrdf_subject = factory.createURI(jena_subject.getURI());
		}
				
		//convert predicate
		openrdf_predicate = factory.createURI(jena_predicate.getURI());
		
		//convert object
		if (jena_object.isBlank()){
			//blank node?
			String bNode = jena_object.getBlankNodeId().toString();
			if (blankNodes.containsKey(bNode)){
				openrdf_object = blankNodes.get(bNode);
			} else {
				openrdf_object = factory.createURI(generateURI().toString());
				blankNodes.put(bNode, (Resource) openrdf_object);
			}
		} else if (jena_object.isURI()){
			//resource?
			openrdf_object = factory.createURI(jena_object.getURI());
		} else {
			//literal
			String obj_value = jena_object.getLiteralValue().toString();
			if (jena_object.getLiteralDatatypeURI() == null) openrdf_object = factory.createLiteral(obj_value);
			else if (jena_object.getLiteralDatatypeURI().contains("XMLSchema#string")) openrdf_object = factory.createLiteral(obj_value);
			else if (jena_object.getLiteralDatatypeURI().contains("XMLSchema#double")) openrdf_object = factory.createLiteral(Double.parseDouble(obj_value));
			else if (jena_object.getLiteralDatatypeURI().contains("XMLSchema#boolean")) openrdf_object = factory.createLiteral(Boolean.parseBoolean(obj_value));
			else if (jena_object.getLiteralDatatypeURI().contains("XMLSchema#int")) openrdf_object = factory.createLiteral(Integer.parseInt(obj_value));
			else if (jena_object.getLiteralDatatypeURI().contains("XMLSchema#float")) openrdf_object = factory.createLiteral(Float.parseFloat(obj_value));
			else if (jena_object.getLiteralDatatypeURI().contains("XMLSchema#long")) openrdf_object = factory.createLiteral(Long.parseLong(obj_value));
			else openrdf_object = factory.createLiteral(obj_value);
		}
		
		//convert subject
		if (openrdf_predicate.toString().equals(RDF.TYPE.toString()) && openrdf_object.toString().equals("http://spinrdf.org/sp#Select"))
				spin_sparql_node = openrdf_subject;
		
		org.openrdf.model.Statement stmt = factory.createStatement(openrdf_subject, openrdf_predicate, openrdf_object);
		
		return stmt;
	}
	
	

	public static void main (String [] args){
		//String queryString = "SELECT DISTINCT * WHERE { ?s a <http://dbpedia.org/ontology/City> . ?s <http://dbpedia.org/ontology/distance> ?o . ?s <http://dbpedia.org/ontology/leader> ?leader . FILTER((?o < 3) || (?o > 10)) }";
//		String queryString = "SELECT DISTINCT * WHERE { ?s a <http://dbpedia.org/ontology/City> . } ";
		String queryString = "SELECT DISTINCT * {?city a <http://dbpedia.org/ontology/City> . ?city <http://dbpedia.org/ontology/leaderName> <http://dbpedia.org/resource/Ed_Fast> . ?city <http://dbpedia.org/ontology/elevation> ?elevation_ . FILTER (?elevation_ = 32)  }";
		String dataset = "http://live.dbpedia.org/sparql";
		String converterType = "RDF";
		exporter(queryString, dataset, converterType);
	}
	
}
