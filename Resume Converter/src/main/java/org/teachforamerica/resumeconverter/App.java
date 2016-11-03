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
import models.Role;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

 static String RESUME_FILEPATH =  "RESUME_FILEPATH_GOES_HERE";

 static String DOCUMENT_CONVERSION_VERSION = "2015-12-15"; // this is the version of document conversion to use
 // this is a configuration option to check a cache for saved converted documents 

 // this is used to help pull out a students university by matching it with this list
 final static String[] universityList = {
  "City College of New York",
  "New York University",
  "NYU",
  "Dartmouth College",
  "Rutgers University",
  "Hunter College",
  "Hunter College of New York"
 };

 public static void main(String[] args) {
	 
  // OBTAIN BLUEMIX CREDENTIALS
  BluemixConnection connection = new BluemixConnection();
  connection.loadCredentials(); // loads in credentials from config.properties file

  Candidate candidate = null;

	  String resumeFilepath = RESUME_FILEPATH;
	  String universityName = "UNIVERSITY_NAME_GOES_HERE";
	  int graduationYear = 2018; // PLACE CANDIDATES GRADUATION YEAR HERE
	  
	  JsonParser jsonParser = new JsonParser(); // used for json parsing
	  
	  // convert pdf resume to answer units 
	  String answerUnitsResume = convertToAnswerUnits(resumeFilepath, universityName, graduationYear, true, connection);
	  
	  // convert pdf resume to normalized text (this helps for parsing stuff)
	  String normalizedTextResume = convertToNormalizedText(resumeFilepath, universityName, graduationYear, true, connection);
	  
	  // this calls alchemy to have watson extract meaning from the answer units resume
	  CombinedResults rawProfile = getRawProfile(jsonParser.parse(answerUnitsResume).getAsJsonObject(), connection);
	  
	  // this builds a candidate object using all the results
	  candidate = loadCandidate(rawProfile, resumeFilepath, answerUnitsResume, normalizedTextResume);

	  candidate.prettyPrint(); // and here we print them out
	  
	  
	  //   candidate = buildCandidate(RESUME_FILEPATH + i + ".pdf", connection, checkCache);

 }


 /*
  * Creates a candidate from their resume 
  * checkCache: true to check if a converted document already exists
  * 
  */
 public static Candidate buildCandidate(String resumeFilePath, BluemixConnection connection, Boolean checkCache) {

  // CONVERT RESUME USING DOCUMENT CONVERSION
  String answerUnitsResume = null; // a version of a resume composed of answer units
  String textResume = null; // a pure text version of the resume 

  // CONVERT TO ANSWER UNITS
  convertToAnswerUnits(resumeFilePath, "UNIVERSITY", 2018, checkCache, connection);
  


  //  ALSO CONVERT TO NORMALIZED TEXT
  // TODO CHECK IF RESULTS ARE CACHED

  if (checkCache) {

   // TODO IF RESULTS ARE NOT CACHED FROM A PREVIOUS ITERATION 

  }

  // IF THE DOCUMENT DOESNT EXIST CREATE IT 

   textResume = convertResume(resumeFilePath, "normalized-text", connection);
   //System.out.println(textResume);


  

  // CONVERT ANSWER UNITS VERSION OF RESUME INTO A RAW PROFILE
  JsonParser jsonParser = new JsonParser();
  CombinedResults rawProfile = getRawProfile(jsonParser.parse(answerUnitsResume).getAsJsonObject(), connection);
  Candidate candidate = loadCandidate(rawProfile, resumeFilePath, answerUnitsResume, textResume);


  return candidate;

 }
 
 
 public static String convertToNormalizedText(String resumeFilePath, String university , int graduationYear, Boolean checkCache, BluemixConnection connection) {
	 String normalizedTextResume = null;
	  
	 // CHECK IF RESULTS ARE CACHED (for the sake of time and keeping bills low)
	  boolean resultsAreCached = false;
	  String filepath = getResumeCacheFilePath(university, graduationYear, resumeFilePath, "normalized-text");
	  if (checkCache) {
		   if(fileExists(filepath)) {
			 
			   // READ IN RESULTS FROM RESUME
			   normalizedTextResume = loadFile(filepath);
			   
			   resultsAreCached = true;
		   }
	  }

	  // IF THE DOCUMENT DOESNT EXIST CREATE IT 
	  if (resultsAreCached == false) {
		  // connects to document conversion service to convert resume
		  normalizedTextResume = convertResume(resumeFilePath, "normalized-text", connection);
		  //System.out.println(normalizedTextResume);
		  
		  cacheResume(filepath, normalizedTextResume); //  CACHE RESULTS 
	  }
	  
	  return normalizedTextResume;
 }
 
 public static String convertToAnswerUnits(String resumeFilePath, String university , int graduationYear, Boolean checkCache, BluemixConnection connection) {
	 String answerUnitsResume = null;
	  
	 // CHECK IF RESULTS ARE CACHED (for the sake of time and keeping bills low)
	  boolean resultsAreCached = false;
	  String filepath = getResumeCacheFilePath(university, graduationYear, resumeFilePath, "answer-units");
	  if (checkCache) {
		   if(fileExists(filepath)) {
			 
			   // READ IN RESULTS FROM RESUME
			   answerUnitsResume = loadFile(filepath);
			   
			   resultsAreCached = true;
		   }
	  }

	  // IF THE DOCUMENT DOESNT EXIST CREATE IT 
	  if (resultsAreCached == false) {
		  // connects to document conversion service to convert resume
		  answerUnitsResume = convertResume(resumeFilePath, "answer_units", connection);
		  
		  cacheResume(filepath, answerUnitsResume); //  CACHE RESULTS 
	  }
	  
	  return answerUnitsResume;
 }
 
 
 
 public static boolean fileExists(String filepath) {
	 File file = new File(filepath);
	 return file.exists();
 }
 
 // Saves a resume in the resume-cache folder
 public static boolean cacheResume(String filepath, String data) {
		
	if(filepath == null) {
		return false;
	}
	 
	try {
		if(fileExists(filepath)== false) {
			Path pathToFile = Paths.get(filepath);
			Files.createDirectories(pathToFile.getParent());
			Files.createFile(pathToFile);
		}
	} catch (IOException e1) {
		e1.printStackTrace();
	}
	

	
	try (FileWriter file = new FileWriter(filepath)) {
	
		file.write(data);
		file.close();
	} catch(IOException e) {
		e.printStackTrace();
	} 
	 
	 return true;
	 
 }
 
 public static String getResumeCacheFilePath(String university, int graduationYear, String resumeName, String format) {
	 
	 String filepath = null; // this is where we will save the resume data

	 // create the filepath to save the data
	 if(university == null) {
		 System.out.println("Error: Unable to create a file path, University is null");
		 return null;
	 }
	 
	// TODO: check for consistency
	 if(resumeName.lastIndexOf("\\") != -1) {
		 resumeName = resumeName.substring(resumeName.lastIndexOf("\\")+1,resumeName.length());
	 }
	 if(resumeName.lastIndexOf(".") != -1) {
		 resumeName = resumeName.substring(0,resumeName.lastIndexOf("."));
	 }

	 filepath = "resume-cache/" + university.toLowerCase() + "/" + graduationYear + "/" + format + "/" + resumeName;
	 
	 // if special things are needed to be done based on format do them here
	 if(format.contentEquals("answer-units")) {
		 filepath += ".json";
	 } else if(format.contentEquals("normalized-text")) {
		
		 filepath += ".txt";
		 
	 } else if(format.contentEquals("alchemy-profile")) {
		 filepath += ".json";
		 
	 } else {
		 System.out.println("Error: Unrecognized saving format:" + format);
		 return null;
	 }
	 
	 return filepath;
 }

 // TODO turn into a hash for speed
 public static String extractUniversityFromRoles(ArrayList<Role> roles, String[] University) {
  for (Role role: roles) {
   for (String university: universityList) {
    if (role.getOrganization().equalsIgnoreCase(university)) {
     return university;
    }

   }

  }
  return null;
 }

 /*
  * This builds up a candidates profile using all the data extracted from their resume
  */
 public static Candidate loadCandidate(CombinedResults rawProfile, String resumeFilePath, String answerUnitsResume, String textResume) {


  // THIS IS THE NEW CANDIDATE TO BE CREATED
  Candidate candidate = new Candidate(resumeFilePath);
  
  // its a bit tricky to figure out the name of the person from their resume a trick is to take a list of all persons and see if the name appears before the education section
  ArrayList<String> possibleCandidateNames = new ArrayList<String>();
  
 
  Matcher m;
  
  boolean nameSet = false;
  boolean universitySet = false;
  boolean gpaSet = false;
  
  // LOOK FOR INFORMATION FROM THE ENTITIES SECTION
  for (Entity entity: rawProfile.getEntities()) {
   if (entity.getType().equalsIgnoreCase("EmailAddress")) {
    candidate.setEmail(entity.getText());
   } else if (entity.getType().equalsIgnoreCase("Degree")) {
    candidate.setDegree(entity.getText());
   } else if (entity.getType().equalsIgnoreCase("Organization")) {
	  


    // CLEAN UP RESUME TITLES FROM TEXT 	note: (?i) is regex for case insensitive
    if (entity.getText().toLowerCase().startsWith("education")) {
     entity.setText(entity.getText().replaceFirst("(?i)education", "").trim());
    }
    if (entity.getText().toLowerCase().startsWith("experience")) {
     entity.setText(entity.getText().replaceFirst("(?i)experience", "").trim());
    }
    if (entity.getText().toLowerCase().startsWith("activities")) {
     entity.setText(entity.getText().replaceFirst("(?i)activities", "").trim());
    }
    if (entity.getText().toLowerCase().startsWith("affiliations")) {
     entity.setText(entity.getText().replaceFirst("(?i)affiliations", "").trim());
    }

    candidate.getOrganizations().add(entity.getText());
    candidate.getRoles().add(new Role(entity.getText()));


   } else if (entity.getType().equalsIgnoreCase("JobTitle")) {
    candidate.getKeywords().add(entity.getText());

   } else if(entity.getType().equalsIgnoreCase("Person")) {
	   if(nameSet == false) {
		   possibleCandidateNames.add(entity.getText());
	   }
   }

  }

  // TRY TO EXTRACT THE UNIVERSITY FROM THE LIST OF STUDENT ORGANIZATIONS 
  String candidateUniversity = extractUniversityFromRoles(candidate.getRoles(), universityList);
  if (candidateUniversity != null) {
   candidate.setUniversity(candidateUniversity);
   universitySet = true;
  }

  try {
   JSONObject jsonResume = new JSONObject(answerUnitsResume);
   JSONArray array = jsonResume.getJSONArray("answer_units");
   for (int i = 0; i < array.length(); i++) {

    // Grabs name of person from resume TODO verify for consistency
	 
	if(nameSet == false) {
    if (array.getJSONObject(i).getString("type").contentEquals("h3")) {
	     candidate.setName(array.getJSONObject(i).getString("title"));
	    } else if (array.getJSONObject(i).getString("type").contentEquals("h2")) {
	     candidate.setName(array.getJSONObject(i).getString("title"));
	    } 
    }
	}
  } catch (JSONException e) {
   e.printStackTrace();
  }


  String currentSection = "personal";

  
  for (String line: textResume.split("\n")) {
   if (line.trim().toLowerCase().contains("education")) {
    currentSection = "education";
   }
   if (line.trim().toLowerCase().contains("experience")) {
    currentSection = "experience";
   }


   // these are the common sections of a resume that contain role information
   if (line.trim().toLowerCase().contains("leadership") || line.trim().toLowerCase().contains("affiliations") || line.trim().toLowerCase().contains("affiliation") || line.trim().toLowerCase().contains("activities") || line.trim().toLowerCase().contains("volunteer")) {
    currentSection = "role";


   }


   if (line.trim().toLowerCase().contains("projects")) {
    currentSection = "projects";
   }
   
   if(currentSection.contentEquals("personal")) {
	   for(String possibleName : possibleCandidateNames) {
		   // this can also be a bit smarter by checking if there is only one name in the personal section, then its very likely that its the candidates
		   if(line.toLowerCase().contains(possibleName.toLowerCase())) {
			   
			   candidate.setName(possibleName);
		   }
	   }
   }

   // TRIES TO EXTRACT INFO FROM THE EDUCATION SECTION
   if (currentSection.contentEquals("education")) {
    if (line.toLowerCase().contains("college") || line.toLowerCase().contains("university")) {
     if (universitySet == false) {

      if (line.split(" ").length > 1) {
       if (line.split(" ")[0].toLowerCase().contentEquals("education")) {
        m = Pattern.compile("education", Pattern.CASE_INSENSITIVE).matcher(line);
        if (m.find()) {
         line = line.substring(m.end(), line.length()).trim();
        }
       }
      }

      candidate.setUniversity(line);
      universitySet = true;
     }
    }


    if (line.toLowerCase().contains("gpa")
    		|| line.toLowerCase().contains("g.p.a.")
    		|| line.toLowerCase().contains("g.p.a")) {
     if (gpaSet == false) {
      if (line.toLowerCase().contains("major")) {
       line = line.split("major")[0];
      }


      m = Pattern.compile("\\d\\.\\d\\d").matcher(line);

      if (m.find()) {
       candidate.setGPA(Double.parseDouble(m.group()));
       gpaSet = true;
      } else {

       m = Pattern.compile("\\d\\.\\d").matcher(line);
       if (m.find()) {

        candidate.setGPA(Double.parseDouble(m.group()));
        gpaSet = true;
       } else {
        System.out.println("NO MATCH");

       }

      }

     }
    }
   }

   String[] roleList = {
    "member",
    "advocate",
    "president",
    "vice president",
    "vp",
    "volunteer",
    "mentor",
    "journalist"
   };

   // TRIES TO EXTRACT INFORMATION FROM ROLE THEMED SECTIONS
   if (currentSection.contentEquals("role")) {
    //System.out.println(line);
    // role is on its own line
    // check out of list of roles if the role is alone

    // role is on the same line as organization TODO: same line multiple organizations
    for (String role: roleList) {
     if (line.toLowerCase().contains(role)) {
      for (Role candidateRole: candidate.getRoles()) {
       if (line.contains(candidateRole.getOrganization())) {
        candidateRole.setRole(role);
       }
      }

     }

    }

    // check if organization is in candidate organization list
    // if organization is not in list add new organization

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
    String configAsString = "{" + "\"conversion_target\":" + conversionTarget + "," + "\"pdf\":{" + "\"heading\":{" + "\"fonts\":[" + "{\"level\": 1, \"min_size\": 24, \"max_size\": 80}," + "{\"level\": 2, \"min_size\": 18, \"max_size\": 24, \"bold\": false, \"italic\": false}," + "{\"level\": 2, \"min_size\": 18, \"max_size\": 24, \"bold\": true}," + "{\"level\": 3, \"min_size\": 13, \"max_size\": 18, \"bold\": false, \"italic\": false}," + "{\"level\": 3, \"min_size\": 13, \"max_size\": 18, \"bold\": true}," + "{\"level\": 4, \"min_size\": 11, \"max_size\": 13, \"bold\": true, \"italic\": false}" + "]}}" + "}";

    JsonParser jsonParser = new JsonParser();
    JsonObject customConfig = jsonParser.parse(configAsString).getAsJsonObject();

    // answers are formatted in different ways 
    if (conversionTarget == "answer_units") {
     // send resume to Document Conversion to be converted
     return service.convertDocumentToAnswer(resumeFile, null, customConfig).execute().toString();

    } else if (conversionTarget == "normalized-text") {

     return service.convertDocumentToText(resumeFile, null, customConfig).execute();


    }

   } catch (IllegalArgumentException e) {
	   e.printStackTrace();
    // Missing or invalid parameter
   } catch (BadRequestException e) {
    // Missing or invalid parameter
	   e.printStackTrace();
   } catch (UnauthorizedException e) {
    // Access is denied due to invalid credentials
	   e.printStackTrace();
   } catch (InternalServerErrorException e) {
    // Internal Server Error
	   e.printStackTrace();
   }

   return null;

  }
  /*
   * This uses the alchemy service to extract entities and keywords from a resume in json format
   */
 public static CombinedResults getRawProfile(JsonObject resume, BluemixConnection connection) {

  final Map < String, Object > params = new HashMap < String, Object > ();
  params.put(AlchemyLanguage.TEXT, resume);
  params.put(AlchemyLanguage.EXTRACT, "entities,keywords,typed-rels");
  AlchemyLanguage service = new AlchemyLanguage();

  service.setApiKey(connection.getAlchemyKey());
  return service.getCombinedResults(params).execute();

 }
 
 // loads in all the contents of a file into a String
 public static String loadFile(String filepath) {
	// This will reference one line at a time
     String line = null;
     String data = "";

     try {
         // FileReader reads text files in the default encoding.
         FileReader fileReader = 
             new FileReader(filepath);

         // Always wrap FileReader in BufferedReader.
         BufferedReader bufferedReader = 
             new BufferedReader(fileReader);

         while((line = bufferedReader.readLine()) != null) {
             data+=line +'\n';
         }   

         // Always close files.
         bufferedReader.close();         
     }
     catch(FileNotFoundException ex) {
         System.out.println(
             "Unable to open file '" + 
            		 filepath + "'");                
     }
     catch(IOException ex) {
         System.out.println(
             "Error reading file '" 
             + filepath + "'");                  
       
     }
 
 return data;
 }
	 
 }
