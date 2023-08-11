package com.qa.base.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class XmlUtils {

	/**
	 * Get content of xml file as String
	 * 
	 * @param xmlPath	XML file path
	 * @return json as String
	 * @throws IOException IOException
	 */
	public static String getXmlAsString(String xmlPath) throws IOException {
		return new String(Files.readAllBytes(Paths.get(xmlPath)));
	}

	/**
	 * Create an xml file required for testing using an xml template
	 * 
	 * @param templateXmlFilePath	Template xml file path
	 * @param requiredXmlFilePath	Required xml file path
	 * @param findString	Keyword to find
	 * @param replaceString	String to replace the keyword
	 * @throws IOException IOException
	 */
	public static void createXmlRequestFromTemplate(
			String templateXmlFilePath,
			String requiredXmlFilePath,
			String findString,
			String replaceString) throws IOException {
		String templateXml = new String(Files.readAllBytes(Paths.get(templateXmlFilePath)));
		if (replaceString != null) {
			templateXml = templateXml.replace(findString, replaceString);
		}
		System.out.println(requiredXmlFilePath);

		BufferedWriter writer = Files.newBufferedWriter(Paths.get(requiredXmlFilePath));
		writer.write(templateXml);
		writer.close();
	}

	/**
	 * Create an xml file required for testing using xml template
	 * 
	 * @param templateXmlFilePath	Template xml file path
	 * @param requiredXmlFilePath	Required xml file path
	 * @param dataMap	Test data map
	 * @throws IOException IOException
	 */
	public static void createXmlRequestFromTemplate(
			String templateXmlFilePath,
			String requiredXmlFilePath,
			Map<String, String> dataMap) throws IOException 
	{
		String xml = new String(Files.readAllBytes(Paths.get(templateXmlFilePath)));

		for (String key : dataMap.keySet()) {
			xml = xml.replace(key, dataMap.get(key).toString());
		}
		System.out.println(requiredXmlFilePath);

		BufferedWriter writer = Files.newBufferedWriter(Paths.get(requiredXmlFilePath));
		writer.write(xml);
		writer.close();
	}

}

