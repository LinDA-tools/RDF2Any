package com.servlet.routes;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;



import de.unibonn.iai.eis.linda.converters.impl.CSVConverter;
import de.unibonn.iai.eis.linda.converters.impl.RDBConverter;
import de.unibonn.iai.eis.linda.converters.impl.results.JSONOutput;


import de.unibonn.iai.eis.linda.example.SPARQLExample;
import de.unibonn.iai.eis.linda.helper.OutputStreamHandler;
import de.unibonn.iai.eis.linda.helper.SPARQLHandler;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("/v1.0/example/")
public class ExampleRoute {

	/**
	 * Method handling HTTP GET requests. The returned object will be sent
	 * to the client as "text/plain" media type.
	 *
	 * @return String that will be returned as a text/plain response.
	 */
	@GET
	@Path("text/")
	@Produces(MediaType.TEXT_PLAIN)
	public String getExample() {
		return SPARQLExample.exampleResultSet("text");
	}
	@GET
	@Path("csv-converter.csv")
	@Produces({"application/csv"})
	public StreamingOutput getCSVExample() {

		return OutputStreamHandler.getConverterStreamingOutput(new CSVConverter(),SPARQLExample.exampleQueryString(20000) );

	}

	@GET
	@Path("rdb-converter.sql")
	@Produces({"application/sql"})
	public StreamingOutput getRDBExample() {

		return OutputStreamHandler.getConverterStreamingOutput(new RDBConverter(), SPARQLExample.exampleQueryString(20000) );

	}
	
	@GET
	@Path("json/")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONOutput getJSONExample(){
		JSONOutput j = new JSONOutput();
		return j;
	}

}
