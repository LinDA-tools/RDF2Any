package de.unibonn.iai.eis.linda.converters.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Element;
import org.topbraid.spin.model.ElementList;
import org.topbraid.spin.model.Filter;
import org.topbraid.spin.model.NamedGraph;
import org.topbraid.spin.model.Optional;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.model.Union;
import org.topbraid.spin.model.impl.TriplePatternImpl;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import de.unibonn.iai.eis.linda.converters.Converter;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;

/**
 * @author Judie Attard
 * 
 * Converts SPARQL Result set into triples
 *
 */
public class RDFConverter extends MainConverter implements Converter {
	
	private Model subset = ModelFactory.createDefaultModel();
	private Select query;
	private Set<String> resultVars = new HashSet<String>();
	

	
	@Override
	public void convert(OutputStream output, ResultSet rdfResults) throws IOException {
		this.convert(output, rdfResults, (RDFClass) null);
	}

	@Override
	public void convert(OutputStream output, ResultSet rdfResults, RDFClass forClass) throws IOException {
		resultVars.addAll(rdfResults.getResultVars());
		List<Element> listElements = this.query.getWhereElements();

		
		while(rdfResults.hasNext()){
			QuerySolution qrySol = rdfResults.next();
			
			for(Element element : listElements){
				if (element instanceof Filter) continue;
				
				if (element instanceof Optional){
					for(Element optElement : ((Optional) element).getElements()){
						this.triplify(optElement, qrySol);
					}
				} else if (element instanceof Union){
					for(Element optElement : ((Union) element).getElements()){
						this.triplify(optElement, qrySol);
					}
				} else if (element instanceof NamedGraph){
					for(Element optElement : ((NamedGraph) element).getElements()){
						this.triplify(optElement, qrySol);
					}
				} else if (element instanceof ElementList){
					for(Element optElement : ((ElementList) element).getElements()){
						this.triplify(optElement, qrySol);
					}
				} else {
					this.triplify(element, qrySol);
				}
			}
		}
		
		try {
			this.subset.write(output, "TURTLE");
		} catch (Exception e) {
			System.out.println("Error : " + e.toString());
		}
	}
	
	public void setQuery(String query){
		ARQ2SPIN arq2SPIN = new ARQ2SPIN(ModelFactory.createDefaultModel());
		
		ARQFactory arqFactory = ARQFactory.get(); 
		
		Query arqSelQuery = arqFactory.createQuery(query);
		
		this.query = (Select) arq2SPIN.createQuery( arqSelQuery, null); //using spin api to generate a jena model for textual sparql query
		
			List<Element> listElements = this.query.getWhereElements();
			
			for(Element element : listElements){
				if (element instanceof Filter) continue;
				
				if (element instanceof Union){
					for(Element optElement : ((Union) element).getElements()){
						System.out.println(optElement.toString());

					}
				} else  {
				}
			}

	}
	
	
	private void triplify(Element whereClause, QuerySolution qrySol){
		TriplePatternImpl triple = (TriplePatternImpl) whereClause;
		
		Resource qSubject = triple.getSubject();
		Property qPredicate = ModelFactory.createDefaultModel().createProperty(triple.getPredicate().getURI());
		RDFNode qObject = triple.getObject();
		
		Resource subject = qSubject;
		Property predicate = qPredicate;
		RDFNode object = qObject;
		
		if (this.resultVars.contains(qSubject.toString().replace("?",""))){
			subject = qrySol.getResource(qSubject.toString().replace("?",""));
		} 
		
		if (this.resultVars.contains(qObject.toString().replace("?",""))){
			object = qrySol.get(qObject.toString().replace("?",""));
		} 
		
		if ((subject != null) && (object != null)) this.subset.add(subject, predicate, object);
	}
	
	

	@Override
	public void convert(OutputStream output, ResultSet rdfResults, Map<String, List<Object>> propTable) throws IOException {
		// TODO Auto-generated method stub
	}

}
