/**
 * 
 */
package de.unibonn.iai.eis.linda.converters;

import java.io.IOException;
import java.io.OutputStream;

import com.hp.hpl.jena.query.ResultSet;

/**
 * @author jattard
 *
 */
public interface Converter {

	/**
	 * This method should convert the rdf data.
	 * @param Dataset to convert (may include multiple models)
	 * @throws IOException 
	 */
	
	void convert(OutputStream output,ResultSet rdfResults) throws IOException;

}
