package org.teachforamerica.controllers;
import org.teachforamerica.models.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.node.NodeBuilder;

import static org.elasticsearch.common.xcontent.XContentFactory.*;

public class ElasticSearch {

	// TOOD: move to config file
	static String HOST = "localhost";
	Client client;
	static int PORT = 9200;
	public ElasticSearch() {
//		client = NodeBuilder.nodeBuilder()
//                .client(true)
//                .node()
//                .client();
		 client = new TransportClient()
		        .addTransportAddress(new InetSocketTransportAddress(HOST, 9300));
		
	}
	public IndexResponse put(String index, String name, String id, String source) {
		

		IndexRequest indexRequest = new IndexRequest(index, name, id);
		indexRequest.source(source);
		IndexResponse response = client.index(indexRequest).actionGet();
		System.out.println(response);
		
		return response;
	}

	public void closeClient() {
		client.close();
	}


	public static void getAllFields(Object o) {
		for(Field field : o.getClass().getDeclaredFields()){
		    System.out.println(field.getName());//or do other stuff with it
		   
		}
	}
	
	public static void objectToJSon() {
	try {
		
		XContentBuilder builder = jsonBuilder()
			    .startObject()
			        .field("user", "kimchy")
			        .field("postDate", new Date())
			        .field("message", "trying out Elasticsearch")
			    .endObject();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	}
}
