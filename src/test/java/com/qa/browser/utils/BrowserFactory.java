/**
 * @author gholla01
 */
package com.qa.browser.utils;

import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

import com.saucelabs.saucebindings.Browser;
import com.saucelabs.saucebindings.SauceOptions;
import com.saucelabs.saucebindings.SaucePlatform;
import com.saucelabs.saucebindings.SauceSession;
import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * @author gholla01
 * 
 *         This class is used to instantiate the browsers in runtime. Especially
 *         useful when different browsers are required for different testcases
 *         and for parallel execution. Note: DO NOT CODE webDriver code directly
 *         in the testcase or in page objects.
 */
public class BrowserFactory {

	public static SauceSession startSauceSession(String browserName, String platformName, String url,
			String implicitWait, String browserDimensions, String portalName, String testMethodName, String screenResolution) {
		SauceOptions sauceOptions = new SauceOptions();
		sauceOptions.setName(testMethodName);
		sauceOptions.setTunnelIdentifier("BlueShieldCA");
		sauceOptions.setParentTunnel("blueshieldca1");
		if (browserDimensions != null) {
			browserDimensions = browserDimensions.replace(",", "x");
			if (!("'800x600', '1024x768', '1152x864', '1280x768', '1280x800', '1280x960', '1400x1050', '1440x900', '1600x1200', '1680x1050', '1920x1080', '1920x1200', '2560x1600'").contains(browserDimensions)) {
				browserDimensions = "1600x1200";
			}
		} else {
			browserDimensions = "1600x1200";
		}
		sauceOptions.setScreenResolution(browserDimensions);
		if (screenResolution != null && !screenResolution.trim().equals("")) {
			sauceOptions.setScreenResolution(screenResolution);
		}
		if (platformName == null || "".equals(platformName.trim())) {
			platformName = "windows_10";
		}
		switch (platformName.toLowerCase()) {
		
		case "mac":
			sauceOptions.setPlatformName(SaucePlatform.MAC_MOJAVE);
			break;
		case "windows":
			sauceOptions.setPlatformName(SaucePlatform.WINDOWS_10);
			break;
		case "windows_10":
			sauceOptions.setPlatformName(SaucePlatform.WINDOWS_10);
			break;
		case "mac_sierra":
			sauceOptions.setPlatformName(SaucePlatform.MAC_SIERRA);
			break;
		case "windows_8":
			sauceOptions.setPlatformName(SaucePlatform.WINDOWS_8);
			break;
		case "windows_8_1":
			sauceOptions.setPlatformName(SaucePlatform.WINDOWS_8_1);
			break;
		case "mac_mojave":
			sauceOptions.setPlatformName(SaucePlatform.MAC_MOJAVE);
			break;
		case "mac_high_sierra":
			sauceOptions.setPlatformName(SaucePlatform.MAC_HIGH_SIERRA);
			break;
		default:
			sauceOptions.setPlatformName(SaucePlatform.WINDOWS_10);
			break;
		}

		if (browserName == null || "".equals(browserName.trim())) {
			browserName = "chrome";
		}
		switch (browserName.toLowerCase()) {
		case "firefox":
			sauceOptions.setBrowserName(Browser.FIREFOX);
			break;
		case "chrome":
			sauceOptions.setBrowserName(Browser.CHROME);
//			sauceOptions.setBrowserVersion("98");
			break;
		case "edge":
			sauceOptions.setBrowserName(Browser.EDGE);
			break;
		case "safari":
			sauceOptions.setBrowserName(Browser.SAFARI);
			if (platformName != null && !platformName.toLowerCase().contains("mac")) {
				sauceOptions.setPlatformName(SaucePlatform.MAC_MOJAVE);
			}
			break;
		case "ie":
			sauceOptions.setBrowserName(Browser.INTERNET_EXPLORER);
			break;
		default:
			sauceOptions.setBrowserName(Browser.CHROME);
			break;
		}
		SauceSession sauceSession = new SauceSession(sauceOptions);
		try {
			sauceSession.setSauceUrl(new URL(System.getenv("BQSA_HUB_URL")));
		} catch (MalformedURLException e) {
			System.out.println("ERROR: BQSA_HUB_URL env variable is missing");
			e.printStackTrace();
		}
		String sauceBuildName = portalName + " | " + browserName + " | " + System.getenv("BRANCH_NAME").replace("release/", "") + " | "
				+ System.getenv("ENVNAME") + " | " + System.getenv("TestType").replace("Test", "") + " | "
				+ System.getenv("BUILD_ID");
		// System.getenv("BUILD_URL");
		sauceSession.getSauceOptions().setBuild(sauceBuildName);
		Map<String, Object> customDataMap = new HashMap<String, Object>();
		customDataMap.put("url", url);
		sauceSession.getSauceOptions().setCustomData(customDataMap);
		sauceSession.start();
		// WebDriver webDriver = sauceSession.getDriver();
		// webDriver.get(url);
		// webDriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
		// SessionId sessionId = sauceSession.getDriver().getSessionId();
		return sauceSession;

	}

	/**
	 * Factory method to create an instance of the browser web webDriver and pass
	 * the page url to the browser.
	 * 
	 * @param browserName
	 *            Browser name
	 * @param sdriver
	 *            Selenium driver
	 * @param url
	 *            URL
	 * @param implicitWait
	 *            Implicit wait
	 * @param headless
	 *            Headless - true or false
	 * @param browserDimensions
	 *            Browser dimensions
	 * @return WebDriver 
	 */
	public static WebDriver startBrowser(String browserName, String sdriver, String url, String implicitWait,
			String headless, String browserDimensions) {
		WebDriver webDriver = null;
		// ChromeOptions chromeOptions = new ChromeOptions();
		// chromeOptions.addArguments("--ignore-certificate-errors");
		// chromeOptions.setAcceptInsecureCerts(true);
		// try {
		// webDriver = new RemoteWebDriver(new URL("http://bscwl803837:4444/wd/hub"),
		// chromeOptions);
		// } catch (MalformedURLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		if (browserName.toLowerCase().contains("firefox")) {
			if (browserName.toLowerCase().contains("-hub")) {
				try {
					webDriver = new RemoteWebDriver(new URL(System.getenv("BQSA_HUB_URL")), new FirefoxOptions());
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				webDriver = new FirefoxDriver();
			}
		} else if (browserName.toLowerCase().contains("opera")) {
			try {
				if (browserName.toLowerCase().contains("docker") || browserName.toLowerCase().contains("-hub")
						|| !InetAddress.getLocalHost().getCanonicalHostName().toLowerCase().contains("bscw")
						|| !InetAddress.getLocalHost().getCanonicalHostName().toLowerCase().contains("sacwdd")) {
					try {
						OperaOptions operaOptions = new OperaOptions();
						operaOptions.addArguments("--ignore-certificate-errors");
						// operaOptions.setAcceptInsecureCerts(true);
						DesiredCapabilities desiredCapabilitiesOpera = DesiredCapabilities.opera();
						desiredCapabilitiesOpera.setCapability(OperaOptions.CAPABILITY, operaOptions);
						Set<String> capabilitySet = operaOptions.getCapabilityNames();
						for (String capability : capabilitySet) {
							System.out.println(capability);
						}
						webDriver = new RemoteWebDriver(new URL(System.getenv("BQSA_HUB_URL")),
								desiredCapabilitiesOpera);
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					webDriver = new FirefoxDriver();
				}
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (browserName.toLowerCase().contains("chrome")) {
			//System.setProperty("webdriver.chrome.driver", sdriver);
			ChromeOptions chromeOptions = new ChromeOptions();

			// ChromeDriver is just AWFUL because every version or two it breaks unless you
			// pass cryptic arguments
			// AGRESSIVE: options.setPageLoadStrategy(PageLoadStrategy.NONE); //
			// https://www.skptricks.com/2018/08/timed-out-receiving-message-from-renderer-selenium.html
			// options.addArguments("start-maximized"); //
			// https://stackoverflow.com/a/26283818/1689770
			// chromeOptions.addArguments("enable-automation"); //
			// https://stackoverflow.com/a/43840128/1689770
			if (headless != null && !"".equals(headless) && !"false".equals(headless) && "true".equals(headless)) {
				chromeOptions.addArguments("--headless"); // only if you are ACTUALLY running headless
			}
			if (browserDimensions != null && !"".equals(browserDimensions)) {
				browserDimensions = browserDimensions.replace("x", ",");
				chromeOptions.addArguments("--window-size=" + browserDimensions); // https://stackoverflow.com/questions/51959986/how-to-solve-selenium-chromedriver-timed-out-receiving-message-from-renderer-exc
			}
			// chromeOptions.addArguments("log-level=3");
			chromeOptions.addArguments("--no-sandbox"); // https://stackoverflow.com/a/50725918/1689770
			chromeOptions.addArguments("--disable-infobars"); // https://stackoverflow.com/a/43840128/1689770
			chromeOptions.addArguments("--disable-dev-shm-usage"); // https://stackoverflow.com/a/50725918/1689770
			chromeOptions.addArguments("--disable-browser-side-navigation"); // https://stackoverflow.com/a/49123152/1689770
			chromeOptions.addArguments("--disable-gpu"); // https://stackoverflow.com/questions/51959986/how-to-solve-selenium-chromedriver-timed-out-receiving-message-from-renderer-exc
			chromeOptions.addArguments("--ignore-certificate-errors");
			chromeOptions.setAcceptInsecureCerts(true);
			Map<String, Object> experimentalOptions = new HashMap<String, Object>();
			experimentalOptions.put("download.default_directory", "%WORKSPACE%");
			chromeOptions.setExperimentalOption("prefs", experimentalOptions);

			// driver = new ChromeDriver(options);
			// webDriver = new ChromeDriver(options);
			chromeOptions.addArguments("--incognito");
			DesiredCapabilities desiredCapabilitiesChrome = DesiredCapabilities.chrome();
			desiredCapabilitiesChrome.setCapability("applicationCacheEnabled", false);
			desiredCapabilitiesChrome.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
			desiredCapabilitiesChrome.setCapability("tunnelIdentifier", "BlueShieldCA");
			if (browserName.toLowerCase().contains("-hub")) {
				String gridUrl = System.getenv("SAUCELABS_HUB_URL");
				if (gridUrl == null || "".equals(gridUrl)) {
					gridUrl = System.getenv("BQSA_HUB_URL");
				}
				System.out.println("Connecting to hub: " + gridUrl);
				try {
					webDriver = new RemoteWebDriver(new URL(gridUrl), desiredCapabilitiesChrome);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				//System.setProperty("webdriver.chrome.driver", sdriver);
				WebDriverManager.chromedriver().setup();
				webDriver = new ChromeDriver(desiredCapabilitiesChrome);
			}
			if (browserDimensions == null && "".equals(browserDimensions)) {
				webDriver.manage().window().maximize();
			}
			// webDriver.get("chrome://settings/clearBrowserData");
			// webDriver.switchTo().activeElement();
			// webDriver.findElement(By.cssSelector("#clearBrowsingDataConfirm")).click();
		} else if (browserName.equalsIgnoreCase("IE")) {
			System.setProperty("webdriver.ie.driver", sdriver + "\\IEDriverServer.exe");
			DesiredCapabilities caps = DesiredCapabilities.internetExplorer();
			caps.setCapability("ignoreZoomSetting", true);
			caps.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			InternetExplorerDriverService.Builder ies = new InternetExplorerDriverService.Builder();
			File ie_temp = new File(sdriver);
			ies.withExtractPath(ie_temp);
			InternetExplorerDriverService service = ies.build();
			webDriver = new InternetExplorerDriver(service, caps);
		} else if (browserName.equalsIgnoreCase("safari")) {
			webDriver = new SafariDriver();
		}
		if (implicitWait == null || "".equals(implicitWait)) {
			webDriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
			System.out.println("IMPLICIT WAIT: 20 seconds");
		} else {
			if (Boolean.parseBoolean(implicitWait)) {
				webDriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
				System.out.println("IMPLICIT WAIT: 20 seconds");
			} else {
				System.out.println("IMPLICIT WAIT: No");
			}
		}
		webDriver.get(url);
		return webDriver;
	}
}
