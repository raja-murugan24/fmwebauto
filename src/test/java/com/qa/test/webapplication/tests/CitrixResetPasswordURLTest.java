package com.qa.test.webapplication.tests;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.bsc.qa.framework.base.BaseTest;
import com.bsc.qa.framework.factory.BrowserFactoryManager;
import com.qa.base.utils.Xslutils;
import com.qa.test.webapplication.pages.CitrixLandingPage;
import com.relevantcodes.extentreports.LogStatus;

public class CitrixResetPasswordURLTest extends BaseTest implements IHookable {

	public CitrixLandingPage citrixLandingPage;
	public Xslutils xslutils ;
	
	@DataProvider(name = "smpData")
	private Object[][] getData(Method method) {
		Object[][] data = null;
		Map<String, String> dataMap = new HashMap<String, String>();

		String xlsPath = "src/test/resources/CitrixResetPasswordURLTest.xlsx" ;
				
		dataMap = xslutils.getTestMethodData(xlsPath, "testCitrixResetPasswordLink");

		data = new Object[][] { { dataMap } };

		return data;
	}
	
	/**
	 * @Override run is a hook before @Test method
	 */
	@Override
	public void run(IHookCallBack callBack, ITestResult testResult) {
		initBrowser(testResult.getTestName(), testResult.getMethod()
				.getMethodName());
		reportInit(testResult.getTestContext().getName(), 
				callBack.getParameters()[0].toString(), testResult.getMethod()
						.getMethodName());
		softAssert = new SoftAssert();
		logger.log(LogStatus.INFO, "Starting test " + testResult.getName());
		callBack.runTestMethod(testResult);
		softAssert.assertAll();
		System.out.println("soft assert complete");
	}

	/**
	 * Run when the test method runs
	 */
	protected void initBrowser(String testCaseName, String testMethodName) {
		WebDriver driver = BrowserFactoryManager.getDriver();
		
		citrixLandingPage = PageFactory.initElements(driver,
				CitrixLandingPage.class);
		
		citrixLandingPage.setPage(driver, environment, browser,
				testCaseName);
	//	System.out.print(environment);
		
	}

	/**
	 * Test verify the links on Home Page
	 */
	@Test(dataProvider = "smpData")
	public void testCitrixResetPasswordLink(Map<String, String> data) {
		System.out.println("Testcase started: testCitrixResetPasswordLink");
		System.out.println (data.get("UserName").toString());
		System.out.println (data.get("Password").toString());
		citrixLandingPage.isLinkDisplayed();
		
	}

}
