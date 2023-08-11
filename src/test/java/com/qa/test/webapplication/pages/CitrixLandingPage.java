
package com.qa.test.webapplication.pages;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

import com.bsc.qa.framework.base.BasePage;
import com.bsc.qa.framework.factory.BrowserFactoryManager;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public class CitrixLandingPage extends BasePage {
	
	
	/////

	@FindAll({ @FindBy(how = How.XPATH, using = "/html/body/table/tbody/tr[2]/td/table/tbody/tr[2]/td[2]/form/a/font/font"),
		
	})
	
	@CacheLookup
	List<WebElement> resetPasswordLinkList;
	WebElement resetPasswordLink;
			
			
			public boolean isResetPasswordLinkDisplayed() {
				resetPasswordLink = listElement(resetPasswordLinkList);
				return resetPasswordLink.isDisplayed();
			}
			
			
			public Object isLinkDisplayed() {
				resetPasswordLink = listElement(resetPasswordLinkList);
				return resetPasswordLink;
			} 
		  
		 

	
	/////
	
	
	

	// ***********************************TEXTS********************************************************

	@FindAll({
		@FindBy(how=How.ID_OR_NAME,using="Enter user name"),
	})
	
	@CacheLookup
	WebElement userNameText;
	
	
	

	@FindAll({
		@FindBy(how=How.NAME,using="passwd")
	})
	@CacheLookup
	WebElement passwordTextbox;
	
	
	@FindAll({
		@FindBy(how=How.ID_OR_NAME,using="Log_On")
	})
	@CacheLookup
	WebElement loginButton;
	
	@FindAll({
		@FindBy(how=How.ID,using="errorMessageLabel")
	})
	@CacheLookup
	WebElement errorMessageText;
	
	@FindAll({
		@FindBy(how=How.ID,using="protocolhandler-welcome-installButton")
	})
	@CacheLookup
	WebElement detectButton;
	
	private WebDriver driver = BrowserFactoryManager.getDriver();


	public boolean verifyErrorText() {
		boolean verify = false;
		if (errorMessageText != null && errorMessageText.isDisplayed()) verify = true;
		return verify;
	}
	
	public boolean isLoginSuccessful() {
		return !driver.getPageSource().contains("Internal Server Error");
	}
	
	public void loginFunction(String userName, String password, ExtentTest logger) {
		logger.log(LogStatus.INFO, "Enter username");
		this.userNameText.sendKeys(userName);
		logger.log(LogStatus.INFO, "Enter password");
		this.passwordTextbox.sendKeys(password);
		logger.log(LogStatus.INFO, "Click submit button");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		loginButton.click();

	}
	
}

