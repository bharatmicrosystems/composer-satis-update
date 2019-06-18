package com.github.bharatmicrosystems.composer_satis_update;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

@SuppressWarnings("deprecation")
public class Main {
	
	final static Options options = new Options();
	public static String satisJsonPath = "/var/www/satis/satis.json";
	public static String outputJsonPath = satisJsonPath;
	
	public static void main(String args[]) throws IOException{
		final CommandLineParser parser = new BasicParser();
		setOptions();
	    CommandLine commandLine = null;
		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			new HelpFormatter().printHelp("java -jar satis-update.jar ", options, true);
		}
	    if(commandLine.hasOption("c")){
	    	
	    }else{
	    	new HelpFormatter().printHelp("java -jar satis-update.jar ", options, true);
	    	System.exit(1);
	    }
	    String composerJsonPath = getOption('c', commandLine);
	    String authJsonPath = getOption('a', commandLine);
	    String gitUrl = getOption('g', commandLine);
	    String satisHostName = getOption('h', commandLine);
	    satisJsonPath = getOption('j', commandLine);
		satisIndexUpdate(composerJsonPath, authJsonPath, gitUrl, satisHostName);
	}
	
	public static String getGitSuffix(String gitUrl){
		String[] parts = gitUrl.split("/");
		int len = parts.length-1;
		return parts[len-1]+"/"+parts[len];
	}
	
	public static void composerInstall(){
		new CommandHandler().executeCommand("composer install", new File("").getAbsoluteFile());
	}
	
	public static void composerUpdate(){
		new CommandHandler().executeCommand("composer update", new File("").getAbsoluteFile());
	}
	
	public static void runPHPUnitTest(){
		new CommandHandler().executeCommand("phpunit", new File("").getAbsoluteFile());
	}
	
	public static void satisIndexUpdate(String composerJsonPath, String authJsonPath, String gitUrl, String satisHostName) throws IOException{
	    File satisFile = new File(satisJsonPath);
	    String satisJsonStr = FileUtils.readFileToString(satisFile, "UTF-8");
	    JSONObject satisJSON = new JSONObject(satisJsonStr);
		
	    File composerFile = new File(composerJsonPath); 
		String composerJsonStr = FileUtils.readFileToString(composerFile, "UTF-8");
		JSONObject composerJSON = new JSONObject(composerJsonStr);
		
	    File authFile = new File(authJsonPath);
	    String authJsonStr= null;
	    JSONObject authJSON = null;
	    if(authFile.exists()){
	    	authJsonStr = FileUtils.readFileToString(authFile, "UTF-8");
			authJSON = new JSONObject(authJsonStr);	
	    }
		
		
		JSONObject outObject = new JSONObject();
		outObject.put("name", "Satis"); 
		outObject.put("homepage", satisHostName); 
		outObject.put("repositories", computeRepositories(composerJSON, satisJSON, gitUrl, satisHostName));
		outObject.put("require", computeRequire(composerJSON, satisJSON));
		outObject.put("require-dependencies", true);
		JSONObject archive = new JSONObject("{ \"directory\": \"dist\",  \"skip-dev\": true }");
		outObject.put("archive", archive);
		outObject.put("config", computeAuth(authJSON, satisJSON));
		outObject.put("require-all", false);
		
		//Write the string to output file
		FileUtils.writeStringToFile(new File(outputJsonPath), outObject.toString());
		
	}
	
	public static JSONArray computeRepositories(JSONObject composerJSON, JSONObject satisJSON, String gitUrl, String satisHostName){
		//Get the satisJSON repositories 
		JSONArray satisRepositories = satisJSON.getJSONArray("repositories");
		JSONArray outputRepositories = new JSONArray(satisRepositories.toString());
		JSONArray jsonRepositories = null;
		if(composerJSON.query("/repositories")!=null){
			jsonRepositories= composerJSON.getJSONArray("repositories");
		}else{
			jsonRepositories = new JSONArray();
			composerJSON.put("repositories", jsonRepositories);
		}
		jsonRepositories.put(new JSONObject("{"
					+ "\"type\" : \"vcs\","				
					+ "\"url\" : \""+gitUrl+"\""
					+ "}"));
		for(int i = 0 ; i<jsonRepositories.length(); i++ ){
			JSONObject jsonRepository = jsonRepositories.getJSONObject(i);
			boolean matchFound = false;
			for(int j = 0 ; j<satisRepositories.length(); j++){
				JSONObject satisRepository = satisRepositories.getJSONObject(j);
				if(satisRepository.getString("type").equals(jsonRepository.getString("type")) && satisRepository.getString("url").equals(jsonRepository.getString("url"))){
					//Match found
					matchFound = true;
					break;
				}
			}
			if(!matchFound && !jsonRepository.getString("url").contains(satisHostName)){
				addToPos(0, jsonRepository, outputRepositories);
			}
		}
		return outputRepositories;
	}
	
	public static void addToPos(int pos, JSONObject jsonObj, JSONArray jsonArr){
		   for (int i = jsonArr.length(); i > pos; i--){
		      jsonArr.put(i, jsonArr.get(i-1));
		   }
		   jsonArr.put(pos, jsonObj);
		}
	
	public static JSONObject computeRequire(JSONObject composerJSON, JSONObject satisJSON){
		//Get the satisJSON requires 
		JSONObject outputRequire = satisJSON.getJSONObject("require");
		if(composerJSON.query("/require-dev")!=null){
			JSONObject composerRequire = composerJSON.getJSONObject("require");
			Set<String> keySet = composerRequire.keySet();
			for(String key: keySet){
				outputRequire.put(key, computeVersion(outputRequire.getString(key), composerRequire.getString(key)));
			}
		}
		if(composerJSON.query("/require-dev")!=null){
			JSONObject composerRequireDev = composerJSON.getJSONObject("require-dev");
			Set<String> keyDevSet = composerRequireDev.keySet();
			for(String key: keyDevSet){
				outputRequire.put(key, computeVersion(outputRequire.getString(key), composerRequireDev.getString(key)));
			}
		}
		return outputRequire;
	}
	
	public static String computeVersion(String existing, String toAdd){
		return "*";
	}
	
	public static JSONObject computeAuth(JSONObject authJSON, JSONObject satisJSON){
		//Get the auth.json object
		JSONObject satisConfig = satisJSON.getJSONObject("config");
		if(authJSON==null){
			return satisConfig;
		}
		Set<String> keySet = authJSON.keySet();
		for(String httpBasicAuth:keySet){
			if(satisConfig.query("/"+httpBasicAuth)==null){
				satisConfig.put(httpBasicAuth, authJSON.get(httpBasicAuth));
			}else{
				//Object exists in satis.json, update the satis.json with contents
				JSONObject httpBasicSatis = satisConfig.getJSONObject(httpBasicAuth);
				Set<String> httpBasicAuthKeys = authJSON.getJSONObject(httpBasicAuth).keySet();
				for(String httpBasicAuthKey: httpBasicAuthKeys){
					httpBasicSatis.put(httpBasicAuthKey,authJSON.getJSONObject(httpBasicAuth).getJSONObject(httpBasicAuthKey));
				}
			}
		}
		return satisConfig;
	}
	
	public static void setOptions(){
		options.addOption("c","composerjson",true,"Composer JSON file path");
		options.addOption("a","authjson",true,"Auth JSON file path");
		options.addOption("g", "giturl", true, "Git url");
		options.addOption("h", "satishostname", true, "Satis host name");
		options.addOption("j", "satisjsonpath", true, "Satis json path");
	}
	
	public static String getOption(final char option, final CommandLine commandLine) {

	    if (commandLine.hasOption(option)) {
	        return commandLine.getOptionValue(option);
	    }

	    return StringUtils.EMPTY;
	}
	
	public static String separatorsToSystem(String path) {
	    if (path==null) return null;
	    if (File.separatorChar=='\\') {
	        // From Windows to Linux/Mac
	        return path.replace('/', File.separatorChar);
	    } else {
	        // From Linux/Mac to Windows
	        return path.replace('\\', File.separatorChar);
	    }
	}

}
