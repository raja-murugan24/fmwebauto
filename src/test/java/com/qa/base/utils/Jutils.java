package com.qa.base.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Utility to work with JSON
 * 
 * @author ainapu01
 *
 */
public class Jutils {

	static StringBuilder sbJsonElementInGivenJsonObject = new StringBuilder();
	static StringBuilder sbJsonElementInGivenJsonResponse = new StringBuilder();
	static StringBuilder sbJsonValueInGivenJsonResponse = new StringBuilder();
	static Map<String, JsonElement> responseElements;
	static String key = null;
	static String value = null;
	static JsonElement value1;
	static String timestamp = null;

	/**
	 * Initialize timestamo with formatting
	 */
	public static void init() {
		if (timestamp == null) {
			timestamp = DateTimeFormatter.ofPattern("yyyyMMddhhmmss").format(
					LocalDateTime.now());
		}
	}

	/**
	 * 
	 * @param json Json response to save
	 * @param testClassName Name of the test class
	 * @param testCaseName Name of the test case
	 * @param appendToFile Save response string to file? true/false
	 * @throws IOException IOException
	 */
	public static synchronized void saveJsonResponse(String json,
			String testClassName, String testCaseName, boolean appendToFile)
			throws IOException {
		appendToJsonFile(json, testClassName, testCaseName, "Responses");
	}

	/**
	 * Get JSON request object as String using a json template. The key-value pairs in the dataMap will be used to find the key as a keyword in the template and replace it with the value in the map for that key.
	 * 
	 * @param templateJsonFilePath Template file path
	 * @param dataMap HashMap with key-value pairs where key = keyword in the template that needs to be replaced and value = the actual value that the keyword needs to be replaced with.
	 * @param testClassName Test class name
	 * @param testCaseName Test case name
	 * @param appendToFile Save response string to file? true/false
	 * @return Json as a string
	 * @throws IOException IOException
	 */
	public static synchronized String getJsonRequestAsString(
			String templateJsonFilePath,
			Map<String, String> dataMap, 
			String testClassName,
			String testCaseName,
			boolean appendToFile
			) throws IOException {
		String json = new String(Files.readAllBytes(Paths
				.get(templateJsonFilePath)));

		for (String key : dataMap.keySet()) {
			json = json.replace(key, dataMap.get(key).toString());
		}

		if (appendToFile) {
			appendToJsonFile(json, testClassName, testCaseName, "Requests");
		}

		return json;
	}

	/**
	 * Get JSON request object as String using a json template. The findString is the keyword in the template that needs to be replaced with the replaceString to generated the request json.
	 * 
	 * @param templateJsonFilePath Template file path
	 * @param findString Keyword listed in the template that needs to be replaced
	 * @param replaceString Value that the keyword needs to be replaced with
	 * @param testClassName Test class name
	 * @param testCaseName Test case name
	 * @param appendToFile Save response string to file? true/false
	 * @return Json as a string
	 * @throws IOException IOException
	 */
	public static synchronized String getJsonRequestAsString(
			String templateJsonFilePath, String findString,
			String replaceString, String testClassName, String testCaseName,
			boolean appendToFile) throws IOException {
		String json = new String(Files.readAllBytes(Paths
				.get(templateJsonFilePath)));
		if (replaceString != null) {
			json = json.replace(findString, replaceString);
		}

		if (appendToFile) {
			appendToJsonFile(json, testClassName, testCaseName, "Requests");
		}
		return json;
	}

	/**
	 * Append request or response json to a generated json file
	 * 
	 * @param json Json as a string that needs to be saved
	 * @param testClassName Test class name
	 * @param testCaseName Test case name
	 * @param type Type: request or response
	 * @throws IOException IOException
	 */
	public static synchronized void appendToJsonFile(String json,
			String testClassName, String testCaseName, String type)
			throws IOException {
		init();
		boolean isNew = false;
		File directory = new File("src/test/resources/generated/");
		if (!directory.exists()) {
			directory.mkdirs();
		}
		File file = new File("src/test/resources/generated/" + testClassName
				+ "_" + type + "_" + timestamp + ".json");
		if (!file.exists()) {
			file.createNewFile();
			isNew = true;
		}

		BufferedWriter writer = new BufferedWriter(new FileWriter(
				"src/test/resources/generated/" + testClassName + "_" + type
						+ "_" + timestamp + ".json", true));
		if (isNew) {
			writer.append("{ \"data\": [");
		} else {
			writer.append(", ");
		}
		writer.append("{ \"id\": \"" + testCaseName
				+ "\", \"generated" + type + "\": " + json + "}");
		writer.close();
	}

	/**
	 * Close the json file
	 * 
	 * @param testClassName Test class name
	 * @param type Type: request or response
	 * @throws IOException IOException
	 */
	public static synchronized void closeJsonFile(String testClassName,
			String type) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(
				"src/test/resources/generated/" + testClassName + "_" + type
						+ ".json", true));
		writer.append("]}");
		writer.close();

	}

	/**
	 * Get content of json file as String
	 * 
	 * @param jsonPath
	 *            Json file path
	 * @return json as String
	 * @throws IOException
	 *             IOException
	 */
	public static synchronized String getJsonAsString(String jsonPath)
			throws IOException {
		return new String(Files.readAllBytes(Paths.get(jsonPath)));
	}

	/**
	 * Create a json file required for testing using a json template
	 * 
	 * @param templateJsonFilePath
	 *            Template JSON file path
	 * @param requiredJsonFilePath
	 *            Required JSON file path
	 * @param findString
	 *            Keyword to find in json
	 * @param replaceString
	 *            String to replace the keyword
	 * @throws IOException
	 *             IOException
	 */
	public static synchronized void createJsonRequestFromTemplate(
			String templateJsonFilePath, String requiredJsonFilePath,
			String findString, String replaceString) throws IOException {
		String templateJson = new String(Files.readAllBytes(Paths
				.get(templateJsonFilePath)));
		if (replaceString != null) {
			templateJson = templateJson.replace(findString, replaceString);
		}
		System.out.println(requiredJsonFilePath);

		BufferedWriter writer = Files.newBufferedWriter(Paths
				.get(requiredJsonFilePath));
		writer.write(templateJson);
		writer.close();
	}

	/**
	 * Converts a resultset to json array
	 * 
	 * @param resultSet
	 *            Resultset
	 * @return JSON array of resultset
	 * @throws Exception
	 *             Exception
	 */
	public static JSONArray convertToJSONArray(ResultSet resultSet)
			throws Exception {
		JSONArray jsonArray = new JSONArray();
		while (resultSet.next()) {
			int total_rows = resultSet.getMetaData().getColumnCount();
			for (int i = 0; i < total_rows; i++) {
				JSONObject obj = new JSONObject();
				obj.put(resultSet.getMetaData().getColumnLabel(i + 1),
						resultSet.getObject(i + 1));
				jsonArray.put(obj);
			}
		}
		return jsonArray;
	}

	/**
	 * Create a json file required for testing using json template
	 * 
	 * @param templateJsonFilePath
	 *            Template JSON file path
	 * @param requiredJsonFilePath
	 *            Required JSON file path
	 * @param dataMap
	 *            Test data map
	 * @throws IOException
	 *             IOException
	 */
	public static synchronized void createJsonRequestFromTemplate(
			String templateJsonFilePath, String requiredJsonFilePath,
			Map<String, String> dataMap) throws IOException {
		String json = new String(Files.readAllBytes(Paths
				.get(templateJsonFilePath)));

		for (String key : dataMap.keySet()) {
			json = json.replace(key, dataMap.get(key).toString());
		}
		System.out.println(requiredJsonFilePath);

		BufferedWriter writer = Files.newBufferedWriter(Paths
				.get(requiredJsonFilePath));
		writer.write(json);
		writer.close();
	}

	/**
	 * This Method will take JsonElement and response key or value(String obj)
	 * and will check if given response key or response key value is present in
	 * the given JsonELement. if yes, it will retun boolean value as TRUE else
	 * FALSE
	 * 
	 * @param jsonPath
	 *            JSON file path
	 * @param keyOrValue
	 *            Key or value to search for
	 * @return boolean value
	 */
	public static boolean isElementOrValuePresentInJsonResponse(
			JsonElement jsonPath, String keyOrValue) {
		boolean result = false;

		// Check whether jsonElement is JsonObject or not
		if (jsonPath.isJsonObject()) {
			Set<Entry<String, JsonElement>> setJsonObjectWithElements = ((JsonObject) jsonPath)
					.entrySet();
			// System.out.println(setJsonObjectWithElements);
			if (setJsonObjectWithElements != null) {
				// Iterate JSON Elements with Key values
				for (Entry<String, JsonElement> jsonElementInTheSet : setJsonObjectWithElements) {
					// System.out.println("Inside for loop");
					// System.out.println(en.getKey() + " : ");
					String key = jsonElementInTheSet.getKey();
					sbJsonElementInGivenJsonObject.append(key);
					isElementOrValuePresentInJsonResponse(
							jsonElementInTheSet.getValue(), keyOrValue);
				}

			}

			sbJsonElementInGivenJsonResponse = sbJsonElementInGivenJsonResponse
					.append(sbJsonElementInGivenJsonObject);
			// System.out.println("String Builder: "+sbJsonElementInGivenJsonResponse);
			if (sbJsonElementInGivenJsonResponse.toString()
					.contains(keyOrValue)) {
				result = true;
			}
		}

		// Check whether jsonElement is Arrary or not
		else if (jsonPath.isJsonArray()) {
			JsonArray jarr = jsonPath.getAsJsonArray();
			// Iterate JSON Array to JSON Elements
			for (JsonElement je : jarr) {
				isElementOrValuePresentInJsonResponse(je, keyOrValue);
			}
		}

		// Check whether jsonElement is NULL or not
		else if (jsonPath.isJsonNull()) {
			// print null
			// System.out.println("json value is null");
		}
		// Check whether jsonElement is Primitive or not
		else if (jsonPath.isJsonPrimitive()) {
			// print value as String
			// System.out.println("Primitive: " + jsonElement.getAsString());
			String value = jsonPath.getAsString();
			sbJsonValueInGivenJsonResponse.append(value);
			// System.out.println(sbJsonValueInGivenJsonResponse);

		}

		if (sbJsonValueInGivenJsonResponse.toString().contains(keyOrValue)) {
			result = true;
			// System.out.println(a);
		}
		return result;
	}

}
