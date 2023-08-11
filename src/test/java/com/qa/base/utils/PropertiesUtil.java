package com.qa.base.utils;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Utility to process properties
 * 
 * @author sgolla02
 *
 */
public class PropertiesUtil {
	
		private static String CSV_FILE = "src/test/resources/CSVToProperties.csv";
		private static String FILE_NAME = "";
		private static String KEY = "";
		private static String VALUE = "";
		public String ENVURL = "";
		
		/**
		 * Constructor for initializing
		 */
		public PropertiesUtil() {
			System.out.println("Creating properties files..");
			createPropertiesFile(CSV_FILE);
			System.out.println("Done!");
		}
		
		/**
		 * Main method
		 * 
		 * @param args	String args passed in command line
		 */
		public static void main(String[] args) {
			PropertiesUtil util = new PropertiesUtil();
	    	if (init(args)) {
	    		if (!"".equals(CSV_FILE)) {
	    			util.createPropertiesFile(CSV_FILE);
	    		}
	    		else {
	        		util.createPropertyFile(FILE_NAME, KEY, VALUE);
	    		}
	    	}
		}
		
		/**
		 * Get environment url from system env variable
		 * 
		 * @return	Environment url
		 */
		public String getEnvUrl(){
			ENVURL = System.getenv("ENVURL");
			return ENVURL;
		}

		/**
		 * Create a properties file from a csv file.
		 * 
		 * @param csvFileName	CSV file name
		 */
		private void createPropertiesFile(String csvFileName) {
			List<String[]> csvList = new ArrayList<String[]>();
			List<String> fileNamesList = new ArrayList<String>();
			String key = null;
			String propertyFileName = null;
			String value = null;
			Properties properties = new Properties();
			CSVReader reader;
			try {
				reader = new CSVReader(new FileReader(csvFileName));
			     csvList = reader.readAll();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String[] header = csvList.get(0);
			for (int hi = 1; hi < header.length; hi++) {
				fileNamesList.add(header[hi]);
				System.out.println(header[hi]);

				propertyFileName = header[hi] + ".properties";
				for (int i = 1; i < csvList.size(); i++) {
					key = csvList.get(i)[0];
					value = csvList.get(i)[hi];
					appendProperty(properties, key, value);
					System.out.println(propertyFileName + " " + key + " " + value);
				}
				createPropertyFile(propertyFileName, properties, false);
			}
		}

		/**
		 * Initialize static variables to be used  
		 * @param args	String of arguments
		 */
		private static boolean init(String[] args) {
			boolean initialized = false;
			if(args.length > 0) {
	        	for(int i=0;(i+1)<args.length; i=i+2) {
	        		if(args[i].equalsIgnoreCase("-f")) {
	        			FILE_NAME = args[i+1];
	        		} else if(args[i].equalsIgnoreCase("-k") || args[i].equalsIgnoreCase("-p") ) {
	        			KEY=args[i+1];
	        		} else if(args[i].equalsIgnoreCase("-v")) {
	        			VALUE =args[i+1];   
	        		} else if(args[i].equalsIgnoreCase("-c")) {
	        			CSV_FILE =args[i+1];   
	        		}
	        	}
	    		if ( (!"".equals(FILE_NAME) && !"".equals(KEY) && !"".equals(VALUE)) || !"".equals(CSV_FILE) ) {
	        		initialized = true;
	        	} else {
	    			System.out.println("Usage java -jar PropertiesUtil.jar -f <FILE_NAME> -k <KEY> -v <VALUE>");
	    			System.out.println("Usage java -jar PropertiesUtil.jar -c <CSV_FILE>");
	        	}
	        }
			return initialized;
		}
		
		/**
		 * Get value of the property based on key.
		 * @param propertiesFileName	Properties file name
		 * @param key	Property key
		 * @return	Property value
		 */
		public String getValue(String propertiesFileName, String key) {
			Properties properties = new Properties();
			InputStream input = null;
		 
			try {
				input = new FileInputStream(propertiesFileName);
		 		properties.load(input);	 
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return properties.getProperty(key);
		}
		
		/**
		 * Create a property and set a single key and value.
		 * @param key	Property key
		 * @param value	Property value
		 * @return properties	New properties object
		 */
		public Properties createProperty(String key, String value) {
			Properties property = new Properties();
			property.setProperty(key, value);
			return property;
		}
		
		/**
		 * Append a property to a properties object.
		 * @param properties	Properties object
		 * @param key	Property key
		 * @param value	Property value
		 */
		public void appendProperty(Properties properties, String key, String value) {
			properties.setProperty(key, value);
		}
		
		/**
		 * Create property file
		 * 
		 * @param propertiesFileName	Property file name
		 * @param properties	Properties object
		 * @param append	true = append
		 */
		public void createPropertyFile(String propertiesFileName, Properties properties, boolean append) {
			Writer output = null;

			try {
				output = new BufferedWriter(new FileWriter(propertiesFileName, append));
				properties.store(output, null);
			} 
			catch (IOException io) {
				io.printStackTrace();
			} 
			finally {
				if (output != null) {
					try {
						output.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		/**
		 * Create a single properties file for one key-value pair.
		 * @param propertiesFile	Properties file
		 * @param property	Property key
		 * @param value		Property value 
		 */
		public void createPropertyFile(String propertiesFile, String property, String value) {
			Properties properties = new Properties();
			properties.setProperty(property, value);
			createPropertyFile(propertiesFile, properties, false);
		}
		
		/**
		 * Load properties files into environment variables.
		 * 
		 * @param propertiesFileName	Properties file name
		 */
		public void setEnvironmentValue(String propertiesFileName) {
			Properties properties = new Properties();
			InputStream input = null;
		    System.out.println("Inside setEnvironmentValue..");
			try {
				input = new FileInputStream(propertiesFileName);
		 		properties.load(input);	 
		 		Set<Object> keys = properties.keySet();
		 		for (Object key : keys) {
		 			System.getenv().put((String) key, properties.get(key.toString()).toString());
		 		}

			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}
		
		/**
		 * Load properties from corresponding envName into env variables
		 * @param envName	Environment name
		 * @throws SecurityException	SecurityException  	
		 * @throws NoSuchFieldException NoSuchFieldException 
		 * @throws IllegalAccessException  IllegalAccessException 
		 * @throws IllegalArgumentException  IllegalArgumentException 
		 * @throws ClassNotFoundException  ClassNotFoundException 
		 */
		
		public void setEnvVariables(String envName) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException{
			String propertiesFileName = envName + ".properties";
			Properties properties = new Properties();
			InputStream input = null;
			System.out.println("Inside setEnvVariables..");
			try {
				input = new FileInputStream(propertiesFileName);
		 		properties.load(input);	
		 		Set<Object> keys = properties.keySet();
		 		Map<String, String> newenv = new HashMap<String, String>();
		 		for (Object key: keys) {
		 			String value = properties.getProperty((String) key);
		 			newenv.put((String) key, value);
		 		}
		 		 		
		 	    try
		 		   {
		 			     Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
		 			     Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
		 			     theEnvironmentField.setAccessible(true);
		 			     @SuppressWarnings("unchecked")
						 Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
		 			     env.putAll(newenv);
		 			     Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
		 			     theCaseInsensitiveEnvironmentField.setAccessible(true);
		 			     @SuppressWarnings("unchecked")
						 Map<String, String> cienv = (Map<String, String>)theCaseInsensitiveEnvironmentField.get(null);
		 			     cienv.putAll(newenv);
		 			     cienv.put("ENVURL", newenv.get("ENVURL"));
		 		
			}catch (NoSuchFieldException e) {
				e.printStackTrace();
		} 
	}catch (Exception a){
		a.printStackTrace();
	}
			
}


}
   
        
        
        




