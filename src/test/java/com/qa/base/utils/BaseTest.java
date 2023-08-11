package com.qa.base.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.SortedMap;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.asserts.SoftAssert;

import com.bsc.qa.framework.factory.BrowserFactoryManager;
import com.bsc.qa.framework.factory.ReportFactory;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;
import com.saucelabs.saucebindings.SauceSession;

import au.com.bytecode.opencsv.CSVWriter;

/*
 * This is a base Test class. All testng tests needs to inherited from
 * this class. It contains common reporting infrastructure
 * @author gholla01
 */
public class BaseTest implements IHookable {

	public ExtentTest logger;
	public ExtentReports report;
	public CSVWriter csvWriter;

	public static boolean isHeaderWritten = false;
	protected String testCaseName;
	protected String testMethodName;
	protected String browser;
	protected String environment;
	protected String url;
	protected String webDriver;
	protected String driverString;
	protected String headless;
	protected SoftAssert softAssert = null;
	protected SortedMap<String, SortedMap<String, String>> testDataCache = null;
	protected static boolean isJsonGenerated = false;
	protected static String artifactId = null;
	protected WebDriver driver = null;

	@Override
	public void run(IHookCallBack callBack, ITestResult testResult) {
		Map<String, String> dataMap = (Map<String, String>) callBack.getParameters()[0];
		if (dataMap != null && dataMap.get("STORY") != null && !dataMap.get("STORY").isEmpty()) {
			Reporter.getCurrentTestResult().setAttribute("requirement", dataMap.get("STORY"));
		}
		Reporter.getCurrentTestResult().setAttribute("labels", System.getenv("compname"));
		if (!Files.exists(Paths.get("src/test/resources/xrayCustomFields.json"))) {
			generateXrayJson(testResult);
		}
	}

	private static void generateXrayJson(ITestResult testResult) {
		String json = null;
		String fixversion = (System.getenv("BRANCH_NAME") == null) ? "" : System.getenv("BRANCH_NAME").replace("release/", "");
		String jiraTicketSummaryField = 
				testResult.getTestContext().getSuite().getName() + ": " 
				+ System.getenv("ENVNAME") + ": " 
				+ fixversion + ": " 
				+ DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(LocalDateTime.now());
		try {
			json = new String(Files.readAllBytes(Paths.get("src/test/resources/xrayTemplate.json")));
		} catch (IOException e) {
			System.out.println("WARNING: src/test/resources/xrayTemplate.json is missing!");
			e.printStackTrace();
		}

		String testType = System.getenv("testType");
		if (testType == null || "".equals(testType)) {
			testType = System.getenv("TestType");
		}
		json = json.replace("TEST_JIRA_TICKET_SUMMARY", jiraTicketSummaryField);
		json = json.replace("TEST_PHASE", testType.replace("Test", "").replace("test", ""));
		json = json.replace("JENKINS_JOB_URL", System.getenv("BUILD_URL"));
		json = json.replace("RELEASE_VERSION", fixversion);
		json = json.replace("ENVNAME", System.getenv("ENVNAME"));
		try {
			Files.write(Paths.get("src/test/resources/xrayCustomFields.json"), json.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/**
	 * Close test after test method
	 * 
	 * @param caller method caller
	 */
	@AfterMethod(alwaysRun = true)
	public void afterMethod(Method caller) {
		ReportFactory.closeTest(caller.getName());
	}

	public void initDriver(String testMethodName) {
		if ("sauce".equals(BrowserFactoryManager.executionType)) {
	    	SauceSession sauceSession = BrowserFactoryManager.getSession();
	        driver = sauceSession.getDriver();
	        Map<String, Object> customDataMap = sauceSession.getSauceOptions().getCustomData();
	        driver.get((String) customDataMap.get("url"));
		} else {
			driver = BrowserFactoryManager.getDriver();
		}
	}

	/**
	 * Initialize extent logger and report based on context and method name.
	 * 
	 * @param testContextName Test context name
	 * @param testMethodName  Test method name
	 */
	protected void reportInit(String testContextName, String testMethodName) {
		String envName = System.getenv("ENVNAME");
		logger = ReportFactory.getTest();
		report = ReportFactory.getExtentReport();
		logger = report.startTest(testContextName + ": " + testMethodName);
		loggerAssignCategory(testContextName, envName);
	}

	/**
	 * Tag test suite category in the report depending on name of the environment.
	 * 
	 * @param testContextName Test context name
	 * @param envName         Environment name
	 */
	public void loggerAssignCategory(String testContextName, String envName) {
		if (testContextName.toLowerCase().contains("smoke")) {
			logger.assignCategory("Smoke-" + envName);
		} else if (testContextName.toLowerCase().contains("reg")) {
			logger.assignCategory("Regression-" + envName);
		} else if (testContextName.toLowerCase().contains("func")) {
			logger.assignCategory("Functional-" + envName);
		} else {
			logger.assignCategory("Automation-" + envName);
		}
	}

	/**
	 * AfterMethod gets called automatically after every @Test execution. This is
	 * framework provided method. Ensure this method exists in every Tests class
	 * 
	 * @param result ITestResult
	 */
	@AfterMethod(alwaysRun = true)
	public void takeScreenShot(ITestResult result) {
		String sauceSessionUrl = "";
		SauceSession sauceSession = BrowserFactoryManager.getSession();
		if (sauceSession != null) {
			sauceSessionUrl = "<a href='https://app.saucelabs.com/tests/" + sauceSession.getDriver().getSessionId().toString() + "'>RECORDING</a>";
		}

		if (result != null) {

			// Here will compare if test is failing then only it will enter into if
			// condition
			TakesScreenshot ts = null;
			File source = null;
			String screen_shot_path = null;
			String image = null;

			if (ITestResult.FAILURE == result.getStatus()) {
				try {
					WebDriver driver = BrowserFactoryManager.getDriver();
					// Create reference of Takes Screenshot
					ts = (TakesScreenshot) driver;

					// Call method to capture screenshot
					source = ts.getScreenshotAs(OutputType.FILE);

					// Copy files to specific location here it will save all screenshot in our
					// project home directory and
					// result.getName() will return name of test case so that screenshot name will
					// be same
					// System.out.println("TESTCASENAME:"+testCaseName);
					screen_shot_path = System.getProperty("user.dir") + "\\test-output\\BSC-reports\\screenshots\\"
							+ result.getName() + "_" + System.currentTimeMillis();

					FileUtils.copyFile(source, new File(screen_shot_path + ".png"));

					System.out.println("Screenshot taken");
					System.setProperty("org.uncommons.reportng.escape-output", "false");

					image = logger.addScreenCapture(screen_shot_path + ".png");
					logger.log(LogStatus.ERROR, result.getThrowable());
					logger.log(LogStatus.FAIL, result.getName() + "_" + testCaseName + " failed " + sauceSessionUrl, image);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (ITestResult.SUCCESS == result.getStatus()) {
				logger.log(LogStatus.PASS, result.getName() + " verified successfully " + sauceSessionUrl);
			}

		}
		tearDown(result.getStatus());
		tearDown();
	}

	/**
	 * AfterClass gets called after all tests methods are done executed.
	 */
	@AfterMethod(alwaysRun = true)
	public void tearDown() {
		WebDriver driver = BrowserFactoryManager.getDriver();
		if (driver != null) {
			driver.quit();
		}
	}
	
	public void tearDown(int testStatus) {
		String sessionStatusString = "skipped";
		if (testStatus == ITestResult.SUCCESS) {
			sessionStatusString = "passed";
		}
		else if (testStatus == ITestResult.FAILURE) {
			sessionStatusString = "failed";
		}
		SauceSession sauceSession = BrowserFactoryManager.getSession();
		if (sauceSession != null) {
			WebDriver driver = sauceSession.getDriver();
			sauceSession.stop(sessionStatusString);
			if (driver != null) {
				driver.quit();
			}
		}
	}

	/**
	 * After suite will be responsible to close the report properly at the end You
	 * an have another afterSuite as well in the derived class and this one will be
	 * called in the end making it the last method to be called in test execution.
	 */
	@AfterSuite(alwaysRun = true)
	public void afterSuite() {
		ReportFactory.closeReport();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		replaceInFile();
	}

	/**
	 * Initialize extent logger and report.
	 * 
	 * @param testContextName	Test context name
	 * @param testCaseName	Test case name
	 * @param testMethodName	Test method name
	 */
	protected void reportInit(String testContextName, String testCaseName, String testMethodName) {
		String envName = System.getenv("ENVNAME");
		logger = ReportFactory.getTest();
		report = ReportFactory.getExtentReport();
		// report.addSystemInfo("Browser", broswerName);
		logger = report.startTest(testContextName + ":" + testMethodName + "--------->PARAMETERS: " + testCaseName);
		if (testContextName.toLowerCase().contains("smoke")) {
			logger.assignCategory("Smoke-" + envName);
		} else if (testContextName.toLowerCase().contains("reg")) {
			logger.assignCategory("Regression-" + envName);
		} else if (testContextName.toLowerCase().contains("func")) {
			logger.assignCategory("Functional-" + envName);
		} else {
			logger.assignCategory("Automation-" + envName);
		}
	}

//	/**
//	 * Initialize extent logger, extent report and csv reporter
//	 * 
//	 * @param testContextName Test context name
//	 * @param broswerName Browser name
//	 * @param testCaseName Test case name
//	 * @param testMethodName Test method name
//	 * @param enableCsvReporting Enable csv reporting
//	 */
//	protected void reportInit(String testContextName, String testCaseName, String testMethodName, boolean enableCsvReporting) {
//		reportInit(testContextName, testCaseName, testMethodName);  
//		if (enableCsvReporting) {
//			csvWriter = ReportFactory.getCsvReport();
//		} 
//	}	

//	/**
//	 * Write to csv report
//	 * 
//	 * @param csvData String... of csv column values to write
//	 * 
//	 * @return 
//	 */
//	public CSVWriter writeToCsvReport(String... csvData) {
//		StringBuilder csvBuilder = new StringBuilder();
//		for (String data: csvData) {
//			csvBuilder.append(data).append(",");
//		}
//		csvBuilder.deleteCharAt(csvBuilder.length() - 1);
//		csvWriter.writeNext(csvData);
//		try {
//			csvWriter.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			System.out.println("ERROR: Failed to write to csv");
//			e.printStackTrace();
//		}
//		return csvWriter;
//	}

	/**
	 * replaceInFile This method is added to replace the screenshot path in final
	 * Report.html. This utility method is added to support the archive feature in
	 * Jenkins. (Note that archive of reports are stored on Master (unix)).
	 */
	public static void replaceInFile() {
		try {
			File file = new File("test-output\\BSC-reports\\" + "Report.html");
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = "", oldtext = "";
			while ((line = reader.readLine()) != null) {
				oldtext += line + "\r\n";
			}
			reader.close();
			// To replace a line in a file
			String path = "'" + System.getProperty("user.dir") + "\\test-output\\BSC-reports\\screenshots\\";
			System.out.println(path);
			String newPath = "'screenshots\\";
			String newtext = oldtext.replaceAll(Matcher.quoteReplacement(path), Matcher.quoteReplacement(newPath));
			FileWriter writer = new FileWriter("test-output\\BSC-reports\\" + "Report.html");
			writer.write(newtext);
			writer.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}