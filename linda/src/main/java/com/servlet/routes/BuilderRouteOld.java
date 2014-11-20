package com.servlet.routes;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import de.unibonn.iai.eis.linda.converters.impl.JSONConverter;
import de.unibonn.iai.eis.linda.helper.SPARQLHandler;
import de.unibonn.iai.eis.linda.helper.output.JSONError;
import de.unibonn.iai.eis.linda.querybuilder.search.ClassSearch;

@Path("/v0.9/builder/")
public class BuilderRouteOld {
	// This route is for the free text search of classes
	// This is a deprecated method as it just matches english words
	@GET
	@Path("classes")
	@Produces(MediaType.APPLICATION_JSON)
	public Object getClasses(@Context UriInfo uriInfo) {
		try{
		MultivaluedMap<String, String> queryParams = uriInfo
				.getQueryParameters();
		String search = queryParams.getFirst("search");
		String dataset = queryParams.getFirst("dataset");
		System.out.println("START Searching for classes matching '" + search
				+ "' in dataset '" + dataset + "'");
		Double startMilliseconds = (double) System.currentTimeMillis();
		JSONConverter converter = new JSONConverter(SPARQLHandler.executeQuery(
				dataset, new ClassSearch(dataset, search).getSPARQLQuery("EN")));
		Double endMilliseconds = (double) System.currentTimeMillis();
		converter.jsonOutput
				.setTimeTaken((endMilliseconds - startMilliseconds) / 1000);
		System.out.println("FINISH searching for classes ... ");
		return converter.jsonOutput;
		}catch(Exception e){
			System.out.println("Error : "+e.toString());
			return new JSONError("ERROR", e.toString());
		}
	}
}
