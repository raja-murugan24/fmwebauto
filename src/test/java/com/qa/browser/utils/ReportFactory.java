package com.qa.browser.utils;

/**
 * @author gholla01
 */

import java.util.HashMap;
import java.util.Map;

import com.relevantcodes.extentreports.DisplayOrder;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;

/**
 * Factory class to create an instance of the extent report.
 * This class is used to instantiate the reporting in runtime. Especially useful for for parallel execution. 
 * 
 * @author gholla01
 *  
 */
public class ReportFactory {

	public static ExtentReports reporter;
//	public static CSVWriter csvWriter;
	public static Map<Long, String> threadToExtentTestMap = new HashMap<Long, String>();
	public static Map<String, ExtentTest> nameToTestMap = new HashMap<String, ExtentTest>();

	public synchronized static ExtentReports getExtentReport() {
		if (reporter == null) {
			// you can get the file name and other parameters here from a
			// config file or global variables
			reporter = new ExtentReports("test-output\\BSC-reports\\"+"Report.html", true, DisplayOrder.NEWEST_FIRST);
		}
		return reporter;
	}

//	public synchronized static CSVWriter getCsvReport() {
//		if (csvWriter == null) {
//			Writer writer;
//			try {
//				writer = Files.newBufferedWriter(Paths.get("test-output//bsc-reports//report.csv"));
//				csvWriter = new CSVWriter(writer);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				System.out.println("ERROR: creating report.csv failed.");
//				e.printStackTrace();
//			}
//		}
//		return csvWriter;
//	}
	
	/**
	 * Get testName from nameToTestMap
	 * 
	 * @param 	testName Test name
	 * @return 	testName from nameToTestMap
	 */
	public synchronized static ExtentTest getTest(String testName) {
		return nameToTestMap.get(testName);
	}

	/**
	 * At any given instance there will be only one test running in a thread. We
	 * are already maintaining a thread to extentest map. This method should be
	 * used after some part of the code has already called getTest with proper
	 * testcase name and/or description.
	 * 
	 * @return 	testName from nameToTestMap
	 */
	public synchronized static ExtentTest getTest() {
		Long threadID = Thread.currentThread().getId();

		if (threadToExtentTestMap.containsKey(threadID)) {
			String testName = threadToExtentTestMap.get(threadID);
			return nameToTestMap.get(testName);
		}	
		return null;
	}

	/**
	 * Close testName report after every test method 
	 * 
	 * @param	testName	Test name
	 */
	public synchronized static void closeTest(String testName) {

		if (!testName.isEmpty()) {
			ExtentTest test = getTest(testName);
			getExtentReport().endTest(test);
		}
	}


// WIP by gholla01
//	public synchronized static void closeTest(ExtentTest test) {
//		if (test != null) {
//			getExtentReport().endTest(test);
//		}
//	}
//
//	public synchronized static void closeTest()  {
//		ExtentTest test = getTest();
//		closeTest(test);
//	}

	/**
	 * Close extent report after test suite execution is complete
	 * 
	 */
	public synchronized static void closeReport() {
		if (reporter != null) {
			reporter.flush();
			
		}
//		try {
//			getCsvReport().close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}

}
