/**
 * 
 */
package de.unibonn.iai.eis.linda.converters.impl;

import java.io.File;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author jattard
 *
 */
public interface Converter {
	
	 /**
	  * This method should convert the rdf data.
	  * @param Dataset to convert (may include multiple models)
	  */
	File convert(Dataset rdfDataset);

}
