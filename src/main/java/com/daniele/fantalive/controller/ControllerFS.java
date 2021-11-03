package com.daniele.fantalive.controller;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController
@RequestMapping({ "/fs/" })
public class ControllerFS {
	private static WebDriver driver;
	private static  JavascriptExecutor js;
	
	@GetMapping("/logga")
	public String getFile() throws Exception {
		System.setProperty("webdriver.chrome.driver", "/Users/d.carlucci/Downloads/chromedriver_win32/chromedriver.exe");
		System.out.println("INIZIO");
		driver = new ChromeDriver();
	    js = (JavascriptExecutor) driver;
	    driver.get("https://www.fanta.soccer/it/");
	    driver.manage().window().setSize(new Dimension(1382, 744));
	    driver.findElement(By.cssSelector(".css-47sehv")).click();
	    driver.findElement(By.cssSelector(".link-icon-text")).click();
	    {
	      WebElement element = driver.findElement(By.cssSelector(".link-icon-text"));
	      Actions builder = new Actions(driver);
	      builder.moveToElement(element).perform();
	    }
	    {
	      WebElement element = driver.findElement(By.tagName("body"));
	      Actions builder = new Actions(driver);
	      builder.moveToElement(element, 0, 0).perform();
	    }
	    driver.findElement(By.id("username")).click();
	    {
	      WebElement element = driver.findElement(By.id("username"));
	      element.clear();
	      element.sendKeys("bebocar");
	    }
	    driver.findElement(By.id("password")).click();
	    {
	      WebElement element = driver.findElement(By.id("password"));
	      element.clear();
	      element.sendKeys("Emmola");
	    }
	    driver.findElement(By.id("MainContent_wuc_Login1_btnLogin")).click();
	    js.executeScript("window.scrollTo(0,1)");
		System.out.println("FINITO!!");
		return "FINE";
	}
}
