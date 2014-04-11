package com.servlet;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import com.hp.hpl.jena.query.ResultSet;

import de.unibonn.iai.eis.linda.converters.impl.CSVConverter;
import de.unibonn.iai.eis.linda.example.SPARQLExample;
import de.unibonn.iai.eis.linda.helper.SPARQLHandler;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("/v1.0/example/")
public class MyResource {

	/**
	 * Method handling HTTP GET requests. The returned object will be sent
	 * to the client as "text/plain" media type.
	 *
	 * @return String that will be returned as a text/plain response.
	 */
	@GET
	@Path("text/")
	@Produces(MediaType.TEXT_PLAIN)
	public String getExample(@PathParam("text") String type) {
		return SPARQLExample.exampleResultSet(type);
	}
	@GET
	@Path("csv-converter.csv")
	@Produces({"application/csv"})
	public StreamingOutput getCSVExample(@PathParam("csv") String type) {

		return new StreamingOutput(){

			public void write(OutputStream output) throws IOException,
			WebApplicationException {
				try{
					String queryString=
							"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
									"SELECT ?subject ?label "+
									"WHERE { ?subject rdfs:label ?label. "+
									"FILTER(langMatches(lang(?label), \"EN\"))} LIMIT 4000 ";

					ResultSet results = SPARQLHandler.executeDBPediaQuery(queryString);
					CSVConverter csvConverter = new CSVConverter();
					csvConverter.convert(output, results);
				}catch(Exception e){
					throw new WebApplicationException(e);
				}

			}

		};

	}

}
