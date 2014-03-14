package com.servlet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.unibonn.iai.eis.linda.example.SPARQLExample;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("/example/{type}/")
public class MyResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getExample(@PathParam("type") String type) {
        return SPARQLExample.exampleResultSet(type);
    }
}
