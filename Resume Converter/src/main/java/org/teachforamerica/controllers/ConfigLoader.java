package org.teachforamerica.controllers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.commons.lang.StringEscapeUtils;


public class ConfigLoader {
	
	ArrayList<String> propertyList = new ArrayList<String>();
	
	public Map<String,String> getConfigs(String filePath) {

		Properties properties = new Properties();
		Map<String,String> credentials = new HashMap<String,String>();
		InputStream input = null;
		
		try {
			
			// load properties file
			input = new FileInputStream(filePath);
			properties.load(input);

			// get the property value and print it out
			properties.values();
			
			for(Entry<Object, Object> entry : properties.entrySet()) {
				
				credentials.put(entry.getKey().toString(),/* StringEscapeUtils.escapeJava(*/entry.getValue().toString());
			   
			}
			
		
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			return credentials;
		}
	
		}
	
	
	
	

}

 