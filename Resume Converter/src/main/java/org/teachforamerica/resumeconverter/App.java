package org.teachforamerica.resumeconverter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.*;
import org.teachforamerica.controllers.BluemixConnection;
import org.teachforamerica.controllers.BulkLoader;
import org.teachforamerica.controllers.ConfigLoader;
import org.teachforamerica.controllers.ElasticSearch;
import org.teachforamerica.models.Prospect;
import org.teachforamerica.models.Role;

import com.ibm.watson.developer_cloud.alchemy.v1.AlchemyLanguage;
import com.ibm.watson.developer_cloud.alchemy.v1.model.CombinedResults;
import com.ibm.watson.developer_cloud.alchemy.v1.model.Entity;
import com.ibm.watson.developer_cloud.document_conversion.v1.DocumentConversion;
import com.ibm.watson.developer_cloud.service.exception.*;

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
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author tmack
 *
 */
public class App {

 // CONFIGURATION PROPERTIES
 static String CONFIG_FILEPATH = "config.properties";
 static String RESUME_FILEPATH = null;
 static String DOCUMENT_CONVERSION_VERSION = null; // this is the version of document conversion to use
 static String UNIVERSITY_NAME = null;
 static String GRADUATION_YEAR = null;
 static Boolean CHECK_CACHE = false;
 static Boolean RESUME_BOOK_MODE = false;
 static Boolean CHECK_RESUME_BOOK_CACHE = false;
 static String DEFAULT_RESUME_BOOK_SAVING_DIRECTORY = "resume-book//";
 static String RESUME_BOOK_SAVING_DIRECTORY = null;
 static String DEFAULT_RESUME_FILENAME = "resume ";

 // this is used to help pull out a students university by matching it with this list
 final static String[] universityList = {
  "City College of New York",
  "New York University",
  "Dartmouth College",
  "Rutgers University",
  "Hunter College of New York"
 };

 public static void main(String[] args) {

  // LOAD CONFIGURATION
  System.out.println("Loading Configurations");
  loadConfigs(CONFIG_FILEPATH);

  System.out.println("Loading resume(s)");
  // gets resume to be loaded (or resumes if in resume book mode)
  ArrayList<String> resumeFilePaths = getResumeFilePaths();

  // OBTAIN BLUEMIX CREDENTIALS
  System.out.println("Connecting to Bluemix");
  BluemixConnection connection = new BluemixConnection();
  connection.loadCredentials(); // loads in credentials from config.properties file
  
  // builds a propsect for each resume
  System.out.println("Building prospect(s)");
  Prospect prospect = null;
  ElasticSearch esConnection = new ElasticSearch(); // connection to elastic search
  int i = 0;
  for (String resumeFilePath: resumeFilePaths) {

   // builds prospect
   prospect = buildProspect(resumeFilePath, CHECK_CACHE, connection);
   System.out.println("\tBuilt: " + prospect.getName() + ", " + prospect.getEmail());
   
   // loads prospect into elastic search
   esConnection.put("prospects", "prospect", i++ +"", prospect.toJSON());
	
   System.out.println("\t\t(Elastic Seach) Indexed prospect to: " + "propsects/prospect/"+prospect.getEmail());
  }

  // cleans up and closes connections
  System.out.println("Closing Elastic Search Connection");
  esConnection.closeClient();
  System.exit(0);
 }
 
 public static void loadConfigs(String configFilePath) {
	  ConfigLoader cl = new ConfigLoader();
	  Map < String, String > configs = cl.getConfigs(configFilePath);
	  for (Entry < String, String > config: configs.entrySet()) {
	   String configToLoad = config.getKey();
	   if (configToLoad.contentEquals("resume_filepath")) {
	    RESUME_FILEPATH = config.getValue();

	   } else if (configToLoad.contentEquals("document_conversion_number")) {
	    DOCUMENT_CONVERSION_VERSION = config.getValue();

	   } else if (configToLoad.contentEquals("check_resume_book_cache")) {
	    if (config.getValue().equalsIgnoreCase("true")) {
	    	CHECK_RESUME_BOOK_CACHE = true;
	    } else {
	    	CHECK_RESUME_BOOK_CACHE = false;
	    }
	   } else if (configToLoad.contentEquals("resume_book_mode")) {
	    if (config.getValue().equalsIgnoreCase("true")) {
	     RESUME_BOOK_MODE = true;
	    } else {
	     RESUME_BOOK_MODE = false;
	    }
	   } else if (configToLoad.contentEquals("univeristy_name")) {
	    UNIVERSITY_NAME = config.getValue();


	   } else if (configToLoad.contentEquals("graduation_year")) {
	    GRADUATION_YEAR = config.getValue();

	   } else if (configToLoad.contentEquals("resume_book_saving_directory")) {
	    if (config.getValue().length() > 0) {
	     RESUME_BOOK_SAVING_DIRECTORY = config.getValue();
	    }
	   } else if (configToLoad.contentEquals("check_cache")) {
	    if (config.getValue().equalsIgnoreCase("true")) {
	     CHECK_CACHE = true;
	    } else {
	     CHECK_CACHE = false;
	    }


	   }
	  }

	  // TODO check what was loaded in 

	 }

 public static ArrayList<String> getResumeFilePaths() {
  ArrayList<String> resumeFilePaths = new ArrayList <String>();
  if (RESUME_BOOK_MODE) {

   // TODO check if resumes are already cached 
   if (CHECK_RESUME_BOOK_CACHE) {
	   if (RESUME_BOOK_SAVING_DIRECTORY == null) {
		   RESUME_BOOK_SAVING_DIRECTORY = DEFAULT_RESUME_BOOK_SAVING_DIRECTORY;
	    }
	   
	   // if directory already exists this assumes document is already split
	   if(new File(RESUME_BOOK_SAVING_DIRECTORY).isDirectory() && new File(RESUME_BOOK_SAVING_DIRECTORY).exists()) {
		   resumeFilePaths = listFilesForFolder(new File(RESUME_BOOK_SAVING_DIRECTORY)); 
		   
	   } else {
		   BulkLoader resumeBookLoader = new BulkLoader();
		   System.out.println("Loading resume book from " + RESUME_FILEPATH + " and saving to " + RESUME_BOOK_SAVING_DIRECTORY);
		      resumeBookLoader.loadResumeBook(RESUME_FILEPATH, RESUME_BOOK_SAVING_DIRECTORY, DEFAULT_RESUME_FILENAME);
		     
		     final File resumeBookDirectory = new File(RESUME_BOOK_SAVING_DIRECTORY);
		     resumeFilePaths = listFilesForFolder(resumeBookDirectory);
	   }
   } else {
	   BulkLoader resumeBookLoader = new BulkLoader();
    if (RESUME_BOOK_SAVING_DIRECTORY == null) {

    } else {

      System.out.println("Loading resume book from " + RESUME_FILEPATH + " and saving to " + RESUME_BOOK_SAVING_DIRECTORY);
      resumeBookLoader.loadResumeBook(RESUME_FILEPATH, RESUME_BOOK_SAVING_DIRECTORY, DEFAULT_RESUME_FILENAME);
     
     final File resumeBookDirectory = new File(RESUME_BOOK_SAVING_DIRECTORY);
     resumeFilePaths = listFilesForFolder(resumeBookDirectory);

    }
   }
  } else {
   resumeFilePaths.add(RESUME_FILEPATH);
  }
  return resumeFilePaths;
 }

 public static Prospect buildProspect(String resumeFilePath, Boolean checkCache, BluemixConnection connection) {
  JsonParser jsonParser = new JsonParser(); // used for working with json format.
  // convert pdf resume to answer units  
  String answerUnitsResume = convertToAnswerUnits(resumeFilePath, UNIVERSITY_NAME, GRADUATION_YEAR, checkCache, connection);

  // convert pdf resume to normalized text (this helps for parsing stuff)
  String normalizedTextResume = convertToNormalizedText(resumeFilePath, UNIVERSITY_NAME, GRADUATION_YEAR, checkCache, connection);

  // this calls alchemy to have watson extract meaning from the answer units resume
  CombinedResults rawProfile = getRawProfile(jsonParser.parse(answerUnitsResume).getAsJsonObject(), connection);

  // this builds a candidate object using all the results
  return loadCandidate(rawProfile, resumeFilePath, answerUnitsResume, normalizedTextResume);
 }

 public static ArrayList < String > listFilesForFolder(final File folder) {
  ArrayList < String > files = new ArrayList < String > ();
  for (final File fileEntry: folder.listFiles()) {
   if (fileEntry.isDirectory()) {
    listFilesForFolder(fileEntry);
   } else {
    files.add(fileEntry.getPath());

   }
  }
  return files;
 }

 public static String convertToNormalizedText(String resumeFilePath, String university, String graduationYear, Boolean checkCache, BluemixConnection connection) {
  String normalizedTextResume = null;

  // CHECK IF RESULTS ARE CACHED (for the sake of time and keeping bills low)
  boolean resultsAreCached = false;
  String filepath = getResumeCacheFilePath(university, graduationYear, resumeFilePath, "normalized-text");
  if (checkCache) {
   if (fileExists(filepath)) {

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

 public static String convertToAnswerUnits(String resumeFilePath, String university, String graduationYear, Boolean checkCache, BluemixConnection connection) {
  String answerUnitsResume = null;

  // CHECK IF RESULTS ARE CACHED (for the sake of time and keeping bills low)
  boolean resultsAreCached = false;
  String filepath = getResumeCacheFilePath(university, graduationYear, resumeFilePath, "answer-units");
  if (checkCache) {
   if (fileExists(filepath)) {

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

  if (filepath == null) {
   return false;
  }

  try {
   if (fileExists(filepath) == false) {
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
  } catch (IOException e) {
   e.printStackTrace();
  }

  return true;

 }

 public static String getResumeCacheFilePath(String university, String graduationYear, String resumeName, String format) {

  String filepath = null; // this is where we will save the resume data

  // create the filepath to save the data
  if (university == null) {
   System.out.println("Error: Unable to create a file path, University is null");
   return null;
  }

  // TODO: check for consistency
  if (resumeName.lastIndexOf("\\") != -1) {
   resumeName = resumeName.substring(resumeName.lastIndexOf("\\") + 1, resumeName.length());
  }
  if (resumeName.lastIndexOf(".") != -1) {
   resumeName = resumeName.substring(0, resumeName.lastIndexOf("."));
  }

  filepath = "resume-cache/" + university.toLowerCase() + "/" + graduationYear + "/" + format + "/" + resumeName;

  // if special things are needed to be done based on format do them here
  if (format.contentEquals("answer-units")) {
   filepath += ".json";
  } else if (format.contentEquals("normalized-text")) {

   filepath += ".txt";

  } else if (format.contentEquals("alchemy-profile")) {
   filepath += ".json";

  } else {
   System.out.println("Error: Unrecognized saving format:" + format);
   return null;
  }

  return filepath;
 }

 // TODO turn into a hash for speed
 public static String extractUniversityFromRoles(ArrayList < Role > roles, String[] University) {
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
 public static Prospect loadCandidate(CombinedResults rawProfile, String resumeFilePath, String answerUnitsResume, String textResume) {


  // THIS IS THE NEW CANDIDATE TO BE CREATED
  Prospect candidate = new Prospect(resumeFilePath);

  // its a bit tricky to figure out the name of the person from their resume a trick is to take a list of all persons and see if the name appears before the education section
  ArrayList < String > possibleCandidateNames = new ArrayList < String > ();


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

   } else if (entity.getType().equalsIgnoreCase("Person")) {
    if (nameSet == false) {
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

    if (nameSet == false) {
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

   if (currentSection.contentEquals("personal")) {
    for (String possibleName: possibleCandidateNames) {
     // this can also be a bit smarter by checking if there is only one name in the personal section, then its very likely that its the candidates
     if (line.toLowerCase().contains(possibleName.toLowerCase())) {

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


    if (line.toLowerCase().contains("gpa") || line.toLowerCase().contains("g.p.a.") || line.toLowerCase().contains("g.p.a")) {
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

   while ((line = bufferedReader.readLine()) != null) {
    data += line + '\n';
   }

   // Always close files.
   bufferedReader.close();
  } catch (FileNotFoundException ex) {
   System.out.println(
    "Unable to open file '" +
    filepath + "'");
  } catch (IOException ex) {
   System.out.println(
    "Error reading file '" + filepath + "'");

  }

  return data;
 }

}