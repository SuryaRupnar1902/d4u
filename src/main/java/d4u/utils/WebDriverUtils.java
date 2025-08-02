package d4u.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;

public class WebDriverUtils {
	
    public static WebDriver initializeDriver() {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\LENOVO\\Downloads\\chromedriver-win64\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().window().maximize();
    	
//    	WebDriverManager.chromedriver().setup();
//    	WebDriver driver = new ChromeDriver();
//    	driver = new ChromeDriver();
        return driver;
    }
}
