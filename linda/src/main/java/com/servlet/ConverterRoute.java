package com.servlet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.StreamingOutput;

import de.unibonn.iai.eis.linda.converters.impl.CSVConverter;
import de.unibonn.iai.eis.linda.converters.impl.RDBConverter;
import de.unibonn.iai.eis.linda.helper.OutputStreamHandler;

@Path("/v1.0/convert/")
public class ConverterRoute {
	@GET
	@Path("csv-converter.csv")
	@Produces({"application/csv"})
	public StreamingOutput getCSVExample(@PathParam("query") String query) {

		return OutputStreamHandler.getConverterStreamingOutput(new CSVConverter(),query );

	}

	@GET
	@Path("rdb-converter.sql")
	@Produces({"application/sql"})
	public StreamingOutput getRDBExample(@PathParam("query") String query) {

		return OutputStreamHandler.getConverterStreamingOutput(new RDBConverter(), query);

	}
}
