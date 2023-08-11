package com.qa.base.utils;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Base page class for common opertaions with page objects
 * @author gholla01
 *
 */
public class BasePage {

	protected WebDriver webDriver;
	protected String environment;
	protected String testCaseName;
	private String browser;

	/**
	 * 
	 */
	public BasePage() {
		super();
	}
	
	/**
	 * Initialize the parameters for the page
	 * 
	 * @param driver WebDriver
	 * @param environment Environment name
	 * @param browser Browser type
	 * @param testCaseName Test case name
	 */
	public void setPage(WebDriver driver, String environment, String browser, String testCaseName) {
		this.webDriver = driver;	
		this.environment = environment;
		this.browser = browser;
		this.testCaseName= testCaseName;	
	}

	/**
	 * ListElement Method is framework provided method
	 * It is used to return the valid element in the list 
	 * 
	 * @param listEement List of WebElements
	 * @return list element of type WebElement
	 */
	public WebElement listElement(List<WebElement> listEement) {
		for (WebElement lctr : listEement) {
			try{
				if(lctr.isDisplayed())
					return lctr;
			}catch(Exception e){				
				continue;
			}
		}
		return null;
	}
	
	/**
	 * Return listElement only if it is displayed
	 * 
	 * @param listElement List of WebElements
	 * @param timeout Timeout integer
	 * @return listElement as WebElement
	 */
	public WebElement listElement(List<WebElement> listElement, Integer timeout) {
		for (WebElement element : listElement) {
			try{
				if(isDisplayed(element, timeout))
					return element;
			}catch(Exception e){				
				continue;
			}
		}
		return null;
	}
	
	/**
	 * Return true if WebElement webElement is displayed
	 * 
	 * @param webElement WebElement
	 * @return true if webElement is displayed
	 */
    public Boolean isDisplayed(WebElement webElement) {
        try {
            return webElement.isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException exception) {
            return false;
        }
    }

    /**
     * Return true if WebElement webElement is displayed within the timeout limit
     * 
     * @param webElement WebElement
     * @param timeout Timeout Integer
     * @return true if webElement is displayed
     */
    public Boolean isDisplayed(WebElement webElement, Integer timeout) {
        try {
            WebDriverWait wait = new WebDriverWait(webDriver, timeout);
            wait.until(ExpectedConditions.visibilityOf(webElement));
        } catch (org.openqa.selenium.TimeoutException exception) {
            return false;
        }
        return true;
    }
	
    
	
	/**
	 * Get Browser name
	 * @return browser name as string
	 */
	public String getBrowser() {
		return browser;
	}

	/**
	 * Get environment name
	 * @return environment name as string
	 */
	public String getEnvironment() {
		return environment;
	}

}