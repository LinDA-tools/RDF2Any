package com.servlet;

import org.apache.commons.collections4.map.LRUMap;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.google.common.io.Files;

import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClassProperty;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import javax.json.stream.JsonGenerator;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://0.0.0.0:8081/rdf2any/";
    
    private static Map<String, RDFClass> allClassSearch = (Map<String, RDFClass>) Collections.synchronizedMap(new LRUMap<String, RDFClass>(100000));
    private static Map<String, String> labels = (Map<String, String>) Collections.synchronizedMap(new LRUMap<String, String>(100000));
//    private static Map<String, RDFClassProperty> allPropertiesSearch = (Map<String, RDFClassProperty>) Collections.synchronizedMap(new LRUMap<String, RDFClassProperty>(400));
    private static Long totalNumberOfQueries = 0l;
    private static File f = new File("/home/butterbur22/attard/RDF2Any/counter.txt");
    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig().packages("com.servlet.routes").property(JsonGenerator.PRETTY_PRINTING, true);
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
       
        try{
                server.start();
                System.out.println(String.format("Jersey app started with WADL available %sapplication.wadl\n", BASE_URI));      
                Thread.currentThread().join();
        }
        catch (Exception ioe){
                System.out.println(ioe.toString());
        } finally {
                if (server != null && server.isStarted()){
                        server.shutdownNow();
                }
        }
    }
    
    public static RDFClass addOrGetCachedClass(String classURI, RDFClass rdfClass){
    	if (rdfClass == null){
    		return allClassSearch.get(classURI);
    	} else {
    		allClassSearch.put(classURI, rdfClass);
    		return rdfClass;
    	}
    }
    
    public static String addOrGetCachedLabel(String uri, String label){
    	if (label == null){
    		return labels.get(uri);
    	} else {
    		labels.put(uri, label);
    		return label;
    	}
    }
    
//    public static RDFClassProperty addOrGetCachedProperty(String propertyURI, RDFClassProperty rdfClassProperty){
//    	if (rdfClassProperty == null){
//    		return allPropertiesSearch.get(propertyURI);
//    	} else {
//    		allPropertiesSearch.put(propertyURI, rdfClassProperty);
//    		return rdfClassProperty;
//    	}
//    }
    
    public static void incrementQueryCounter(){
   	 synchronized(totalNumberOfQueries){
   		 totalNumberOfQueries++;
   	 }
    }
    
    public synchronized static Long getTotalNumberOfQueries(){
   	 Long t = 0l;
   	 synchronized(totalNumberOfQueries){
   		 t = totalNumberOfQueries++;
   		 try {
				Files.write(t.toString().getBytes(), f);
			} catch (IOException e) {
				e.printStackTrace();
			}
   	 } 
   	 return t;
    }
    
    
}

