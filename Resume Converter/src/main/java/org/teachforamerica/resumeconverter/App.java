package org.teachforamerica.resumeconverter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.*;
import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyLanguage;
import com.ibm.watson.developer_cloud.alchemy.v1.model.CombinedResults;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Entity;
import com.ibm.watson.developer_cloud.document_conversion.v1.DocumentConversion;
import com.ibm.watson.developer_cloud.service.exception.*;

import models.Candidate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author tmack
 *
 */
public class App {	
	
	static String RESUME_FILEPATH = "RESUME_FILEPATH_GOES_HERE";
	
	static String DOCUMENT_CONVERSION_VERSION = "2015-12-15"; // this is the version of document conversion to use
	static Boolean checkCache = true; // this is a configuration option to check a cache for saved converted documents 
	
	public static void main(String[] args) {
		
		// OBTAIN BLUEMIX CREDENTIALS
		BluemixConnection connection = new BluemixConnection();
		connection.loadCredentials(); // loads in credentials from config.properties file
		
		// BUILD CANDIDATE PROFILE
		Candidate candidate = buildCandidate(RESUME_FILEPATH, connection, checkCache);
		
		// PRINT CANDIDATE PROFILE 
		System.out.println(candidate.toString());
	}
	
	/*
	 * Creates a candidate from their resume 
	 * checkCache: true to check if a converted document already exists
	 */
	public static Candidate buildCandidate(String resumeFilePath, BluemixConnection connection, Boolean checkCache) {
		
		// CONVERT RESUME USING DOCUMENT CONVERSION
		String answerUnitsResume = null; // a version of a resume composed of answer units
		String textResume = null; // a pure text version of the resume 
			
			// CONVERT TO ANSWER UNITS
				// TODO CHECK IF RESULTS ARE CACHED
				boolean resultsAreCached = false;
				if(checkCache) {
					
					// TODO IF RESULTS ARE NOT CACHED FROM A PREVIOUS ITERATION 
					
					
				}
				
				// IF THE DOCUMENT DOESNT EXIST CREATE IT 
				if(resultsAreCached == false) {
					answerUnitsResume = convertResume(resumeFilePath, "answer_units", connection);
					//System.out.println(answerUnitsResume);
					
					
					// TODO CACHE RESULTS (for the sake of time and keeping bills low)
				}
				
				
			//  ALSO CONVERT TO NORMALIZED TEXT
				// TODO CHECK IF RESULTS ARE CACHED
				resultsAreCached = false;
				if(checkCache) {
					
					// TODO IF RESULTS ARE NOT CACHED FROM A PREVIOUS ITERATION 
					
				}
				
				// IF THE DOCUMENT DOESNT EXIST CREATE IT 
				if(resultsAreCached == false) {
					textResume = convertResume(resumeFilePath, "normalized_text", connection);
					//System.out.println(textResume);
					
					
					// TODO CACHE RESULTS (for the sake of time and keeping bills low)
				}
				
		// CONVERT ANSWER UNITS VERSION OF RESUME INTO A RAW PROFILE
		JsonParser jsonParser = new JsonParser();
		CombinedResults rawProfile = getRawProfile(jsonParser.parse(answerUnitsResume).getAsJsonObject(), connection);
			// TODO CHECK IF RESULTS ARE CACHED
		
			// TODO CACHE RESULTS
		
		// TODO EXTRACT DATA FROM CONVERTED DOCUMENTS TO BUILD PROFILE
		Candidate candidate = loadCandidate(rawProfile, resumeFilePath, answerUnitsResume, textResume);
		
		
		return candidate;
		
	}
	
	/*
	 * This builds up a candidates profile using all the data extracted from their resume
	 */
	public static Candidate loadCandidate(CombinedResults rawProfile, String resumeFilePath, String answerUnitsResume, String textResume) {
		
	
		// THIS IS THE NEW CANDIDATE TO BE CREATED
		Candidate candidate = new Candidate(resumeFilePath);
		Matcher m;
		// LOOK FOR INFORMATION FROM THE ENTITIES SECTION
		for(Entity entity: rawProfile.getEntities()) {
			if(entity.getType().equalsIgnoreCase("EmailAddress")) {
				candidate.setEmail(entity.getText());
			}
			else if(entity.getType().equalsIgnoreCase("Degree")) {
				candidate.setDegree(entity.getText());
			}
			else if(entity.getType().equalsIgnoreCase("Organization")) {
				candidate.getOrganizations().add(entity.getText());
			}
			else if(entity.getType().equalsIgnoreCase("JobTitle")) {
				candidate.getKeywords().add(entity.getText());
				
			}
		
		}
		
//		for(Keyword keyword: rawProfile.getKeywords()) {
//			System.out.println(keyword.getText()+"fasdfasdfasfasdfsdfsdfasdf");
//		}
		

		
		try {
			JSONObject jsonResume = new JSONObject(answerUnitsResume);
			JSONArray array = jsonResume.getJSONArray("answer_units");
			for(int i = 0; i < array.length(); i++) {
				// TODO verify for consistency
				if(array.getJSONObject(i).getString("type").contentEquals("h3")) {
					candidate.setName(array.getJSONObject(i).getString("title"));
				}
		
		
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		String currentSection = "personal";
		boolean schoolSet = false;
		boolean gpaSet = false;
		for(String line : textResume.split("\n")) {
			if(line.trim().toLowerCase().contains("education")){
				currentSection = "education";
			
			}
			
			if(currentSection.contentEquals("education")) {
				if(line.toLowerCase().contains("college") || line.toLowerCase().contains("university")) {
					if(schoolSet == false) {
					
						if(line.split(" ").length > 1) {
							if(line.split(" ")[0].toLowerCase().contentEquals("education")) {
							    m = Pattern.compile("education", Pattern.CASE_INSENSITIVE).matcher(line);
							    if(m.find()) {
							    	line = line.substring(m.end(), line.length()).trim();
							    }
							}
						}
						
						candidate.setUniversity(line);
						schoolSet = true;
					}
				}
			
		
				if(line.toLowerCase().contains("gpa")) {
					if(gpaSet == false) {
						if(line.toLowerCase().contains("major")) {
							line = line.split("major")[0];
						}
			
					
					m = Pattern.compile("\\d\\.\\d\\d").matcher(line);
				
				   if (m.find()) {
					   //System.out.println(m.start());
				
					   candidate.setGPA(Double.parseDouble(m.group()));
					   gpaSet = true;
				      }else {
				    
				    	  m = Pattern.compile("\\d\\.\\d").matcher(line);
				    	   if (m.find()) {
							
				    		   candidate.setGPA(Double.parseDouble(m.group()));
				    		   gpaSet = true;
						      }else {
						    	System.out.println("NO MATCH");
						    	 
						      }
				    	 
				      }
					
					} 
				}
			}
			
		}
		return candidate;
	}

	/*
	 * This uses the IBM Document Conversion to convert a resume from pdf to text, html, or json
	 */
	public static String convertResume(String resumeFilePath, String conversionTarget, BluemixConnection connection) {
		
		// Connect to service
		DocumentConversion service = new DocumentConversion(DOCUMENT_CONVERSION_VERSION);
		service.setUsernameAndPassword(connection.getDocumentConversionUser(), connection.getDocumentConversionPW());
		
		try {
				// Load the resume to be sent
				File resumeFile = new File(resumeFilePath);
				
				// This is a configuration file to be sent with the resume, its in json format
				String configAsString = "{" + "\"conversion_target\":" + conversionTarget + "}";
	
				JsonParser jsonParser = new JsonParser();
				JsonObject customConfig = jsonParser.parse(configAsString).getAsJsonObject();
				
				
				
				// answers are formatted in different ways 
				if(conversionTarget == "answer_units") {
					// send resume to Document Conversion to be converted
					return service.convertDocumentToAnswer(resumeFile, null, customConfig).execute().toString();

				} 
				else if(conversionTarget == "normalized_text") {
				
					return service.convertDocumentToText(resumeFile, null, customConfig).execute();
				
					
				}
				
			} catch (IllegalArgumentException e) {
			  // Missing or invalid parameter
			} catch (BadRequestException e) {
			  // Missing or invalid parameter
			} catch (UnauthorizedException e) {
			  // Access is denied due to invalid credentials
			} catch (InternalServerErrorException e) {
			  // Internal Server Error
			}
		
		return null;
		
	}
	/*
	 * This uses the alchemy service to extract entities and keywords from a resume in json format
	 */
	public static CombinedResults getRawProfile(JsonObject resume, BluemixConnection connection) {
		
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put(AlchemyLanguage.TEXT, resume);
		params.put(AlchemyLanguage.EXTRACT,"entities,keywords,typed-rels");
		AlchemyLanguage service = new AlchemyLanguage();
		
		service.setApiKey(connection.getAlchemyKey());
		return service.getCombinedResults(params).execute();
		

		
	}
}

