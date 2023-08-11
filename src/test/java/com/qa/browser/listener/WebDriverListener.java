package com.qa.browser.listener;


import java.net.InetAddress;
import java.net.UnknownHostException;

import org.openqa.selenium.WebDriver;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import com.bsc.qa.framework.factory.BrowserFactory;
import com.bsc.qa.framework.factory.BrowserFactoryManager;
import com.saucelabs.saucebindings.SauceSession;

/**
 * @author gholla01 WebDriverListener class ensures driver is set in the
 *         BrowserFactoryManager before invocation of a test Method
 */
public class WebDriverListener implements IInvokedMethodListener {

	@Override
	public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
		if (method.isTestMethod()) {
			String browserName = System.getenv("BROWSER");
			if (browserName == null) {
				browserName = method.getTestMethod().getXmlTest().getLocalParameters().get("Browser_");
			}
			String sdriver = method.getTestMethod().getXmlTest().getLocalParameters().get("Driver_");
			String url = method.getTestMethod().getXmlTest().getLocalParameters().get("Url_");
			String platformName = method.getTestMethod().getXmlTest().getLocalParameters().get("PlatformName_");
            // TODO: PUT IN CHECK FOR ENVNAME_WEB NOT SET IN ENV
			url = url.replace("ENVNAME_WEB", System.getenv("ENVNAME_WEB"));
			String implicitWaitString = method.getTestMethod().getXmlTest().getLocalParameters().get("ImplicitWait_");
			String headlessString = method.getTestMethod().getXmlTest().getLocalParameters().get("Headless_");
			String browserDimensions = method.getTestMethod().getXmlTest().getLocalParameters()
					.get("BrowserDimensions_");
			String portalName = testResult.getTestContext().getSuite().getName();
			String executionType = method.getTestMethod().getXmlTest().getLocalParameters().get("ExecType_");
			String screenResolution = method.getTestMethod().getXmlTest().getLocalParameters().get("screenResolution");

			if ("SAUCE".equals(executionType)) {
				initSauceSession(method, browserName, url, platformName, implicitWaitString, browserDimensions,
						portalName, screenResolution);
			} else if ("LOCAL".equals(executionType)) {
				initWebDriver(browserName, sdriver, url, implicitWaitString, headlessString, browserDimensions);
			} else {
				try {
					if (InetAddress.getLocalHost().getCanonicalHostName().toLowerCase().contains("bscw")
							|| InetAddress.getLocalHost().getCanonicalHostName().toLowerCase().contains("sac")) {
						initWebDriver(browserName, sdriver, url, implicitWaitString, headlessString, browserDimensions);
					} else {
						initSauceSession(method, browserName, url, platformName, implicitWaitString, browserDimensions, portalName, screenResolution);
					}
				} catch (UnknownHostException e) {
					System.out.println("ERROR: network error. Cannot get host name.");
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @param browserName
	 * @param sdriver
	 * @param url
	 * @param implicitWaitString
	 * @param headlessString
	 * @param browserDimensions
	 */
	private void initWebDriver(String browserName, String sdriver, String url, String implicitWaitString,
			String headlessString, String browserDimensions) {
		WebDriver driver = BrowserFactory.startBrowser(browserName, sdriver, url, implicitWaitString, headlessString,
				browserDimensions);
		BrowserFactoryManager.setWebDriver(driver);
	}

	/**
	 * @param method
	 * @param browserName
	 * @param url
	 * @param platformName
	 * @param implicitWaitString
	 * @param browserDimensions
	 * @param portalName
	 */
	private void initSauceSession(IInvokedMethod method, String browserName, String url, String platformName,
			String implicitWaitString, String browserDimensions, String portalName, String screenResolution) {
		SauceSession sauceSession = BrowserFactory.startSauceSession(browserName, platformName, url, implicitWaitString,
				browserDimensions, portalName, method.getTestMethod().getMethodName(), screenResolution);
		BrowserFactoryManager.setSauceSession(sauceSession);
	}

	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
		if (method.isTestMethod()) {
			/*
			 * Future implementation if required. For now, we dont need to do anything in
			 * afterInvocation.
			 */
		}
	}
}

