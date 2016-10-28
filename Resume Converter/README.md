A few notes:

1. Uses Gradle to load dependencies (check out build.gradle file)
2. Loads in Bluemeix credentials from config.properties file, you can load this by creating a "config.properties" file in the top level directory of this project with this structure

dc_user=YOUR_DOCUMENT_CONVERSION_USER
dc_pw=YOUR_DOCUMENT_CONVERSION_PASSWORD
alchemy_key=YOUR_ALCHEMY_API_KEY

Important note: make sure not to push this config.properties file as it contains sensitive information

3. Main is located in src/main/java/App.java, make sure to put a filepath to a .pdf formated resume in the RESUME_FILEPATH variable at the beginning 
