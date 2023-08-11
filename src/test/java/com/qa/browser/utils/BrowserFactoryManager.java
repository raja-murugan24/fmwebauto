package com.qa.browser.utils;

/**
 * @class BrowserFactoryManager
 * @author gholla01
 *  Manager class uses a concept in java called ThreadLocal variables.
 *  Useful in ensuring your parallel executions are thread safe
 */
import org.openqa.selenium.WebDriver;

import com.saucelabs.saucebindings.SauceSession;

/**
 * @author gholla01 BrowserFactoryManager - Introduced this class to ensure
 *         webdriver doesn't overlap in a parallel execution ThreadLocal
 *         provides thread-local variables to ensure thread safe executions.
 *         NOTE: Please do not use global static variable across the class for
 *         driver. Use getDriver and setWebDriver functions to ensure thread
 *         safe parallelism.
 */
public class BrowserFactoryManager {
	private static ThreadLocal<WebDriver> webDriverThreadLocal = new ThreadLocal<WebDriver>();
	private static ThreadLocal<SauceSession> sauceSessionThreadLocal = new ThreadLocal<>();
	public static String executionType = null;

	public static WebDriver getDriver() {
		return webDriverThreadLocal.get();
	}

	public static void setSauceSession(SauceSession sauceSession) {
		executionType = "sauce";
		sauceSessionThreadLocal.set(sauceSession);
	}
	public static void setWebDriver(WebDriver driver) {
		webDriverThreadLocal.set(driver);
	}

	public static SauceSession getSession() {
		return sauceSessionThreadLocal.get();
	}

}
