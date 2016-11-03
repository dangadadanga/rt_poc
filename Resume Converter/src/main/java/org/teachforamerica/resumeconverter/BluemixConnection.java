package org.teachforamerica.resumeconverter;

import java.io.FileInputStream; 
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Stores data used to connect to Bluemix Services
 * @author tmack
 *
 */
public class BluemixConnection {
	
	String documentConversionUser = null;
	String documentConversionPW = null;
	String alchemyKey = null;

	public BluemixConnection() {
		
	}
	
	public BluemixConnection(String documentConversionUser, String documentConversionPW, String alchemyKey) {
		this.documentConversionUser = documentConversionUser;
		this.documentConversionPW = documentConversionPW;
		this.alchemyKey = alchemyKey;
	}
	
	public String getAlchemyKey() {
		return alchemyKey;
	}
	
	public String getDocumentConversionUser() {
		return documentConversionUser;
	}

	public String getDocumentConversionPW() {
		return documentConversionPW;
	}
	
	
	public void setDocumentConversionUser(String documentConversionUser) {
		this.documentConversionUser = documentConversionUser;
	}

	public void setDocumentConversionPW(String documentConversionPW) {
		this.documentConversionPW = documentConversionPW;
	}

	public void setAlchemyKey(String alchemyKey) {
		this.alchemyKey = alchemyKey;
	}

	// TODO: read in credentials from a config file
	public boolean loadCredentials() {

		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = new FileInputStream("config.properties");

			// load a properties file
			prop.load(input);

			// get the property value and print it out
		
			this.setDocumentConversionUser(prop.getProperty("dc_user"));
			this.setDocumentConversionPW(prop.getProperty("dc_pw"));
			this.setAlchemyKey(prop.getProperty("alchemy_key"));
			
		
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				
				}
				return false;
			}
		}
		return true;
		}
	
	
	
	

}
