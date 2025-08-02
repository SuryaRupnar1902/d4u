package d4u.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.Set;

public class SalesForce {

    public static void main(String[] args) throws InterruptedException {
//        System.setProperty("webdriver.chrome.driver",
//                "C:\\Users\\LENOVO\\Downloads\\chromedriver-win64\\chromedriver.exe");
    	WebDriverManager.chromedriver().setup();
    	WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(40));
        Actions actions = new Actions(driver);

        try {
            driver.get("https://login.salesforce.com/?locale=in");
            driver.manage().window().maximize();

            // Login to Salesforce
            driver.findElement(By.name("username")).sendKeys("appadmin@godrejproperties.com");
            driver.findElement(By.id("password")).sendKeys("d4Eg9Dk7@hS2x");
            driver.findElement(By.name("Login")).click();

            // Search for offer
            WebElement search = wait.until(ExpectedConditions.elementToBeClickable(By.name("str")));
            search.sendKeys("OFFER-2024-10-0099628");
            Thread.sleep(3000);  // Allow suggestions to load
            driver.findElement(By.xpath("//input[@type='button']")).click();

            // Click on the offer ID using JavaScript Executor
            WebElement id = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("(//a[contains(@data-seclke, '01I9')])[2]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", id);

            // Create Booking
            driver.findElement(By.xpath("//input[@title='Create Booking']")).click();
            Thread.sleep(5000);
            // Add more applicants
//            WebElement addMoreApplicants = driver.findElement(By.xpath("//b[text()='Add more applicants']"));
//            addMoreApplicants.click();
            // Lookup Customer Name in a new window
            WebElement lookupIcon = driver.findElement(By.xpath("//img[@title=\"Customer Name Lookup (New Window)\"]"));
            lookupIcon.click();
            // Switch to the new window
            String mainWindow = driver.getWindowHandle();
            Set<String> allWindows = driver.getWindowHandles();
            for (String window : allWindows) {
                if (!window.equals(mainWindow)) {
                    driver.switchTo().window(window);
                    break;
                }
            }
            driver.manage().window().maximize();
            Thread.sleep(500);
            // Enter and submit sf ID            
            Thread.sleep(500);
            Actions actions1= new Actions(driver);
         // Tab key 4 times with a 0.2 sec wait
            for (int i = 0; i < 4; i++) {
                actions1.sendKeys("\uE004");  // Send the Tab key
                try {
                    Thread.sleep(200);  // Wait for 0.2 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();  // Handle interruption exception
                }
            }
            // Input the customer ID and press Enter
            actions1.sendKeys("003Id000003f718IAA").sendKeys("\uE007").perform();  // Perform the actions

            //  Tab key 20 times with a 0.2 sec wait
            for (int i = 0; i < 20; i++) {
                actions1.sendKeys("\uE004");  // Send the Tab key
                try {
                    Thread.sleep(200);  // Wait for 0.2 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Perform the actions queued up in the second loop
            actions1.sendKeys("\uE007");
            actions1.perform();
           Thread.sleep(2000);
             //Switch back to the main window
            driver.switchTo().window(mainWindow);

            // Enter sharing ratio
//            WebElement sharingRatio = driver.findElement(By.xpath(
//                    "//input[@id='thepage:form:pb:j_id52:applicants:1:j_id70']"));
//            sharingRatio.sendKeys("0.00");
//            Thread.sleep(5000);

            // Select TDS Date
            WebElement TdsDate = driver.findElement(By.xpath("(//span[@class='dateFormat'])[2]"));
            TdsDate.click();

            driver.findElement(By.xpath("//input[@type='submit']")).click();
            Thread.sleep(1000);
           driver.findElement(By.xpath("(//input[@type='submit'])[2]")).click();
            Thread.sleep(2000);

            WebElement dblClick = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("(//td[contains(text(),'Deal Status')]/following-sibling::td[contains(@class, 'dataCol')])[1]")));
            actions1.moveToElement(dblClick).doubleClick().perform();
            Thread.sleep(1000);

            // Handle the first dropdown (Approve)
             WebElement approveDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("(//select)[1]")));
            Select select = new Select(approveDropdown);
            select.selectByVisibleText("Approved");
            Thread.sleep(1000);

             WebElement status = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("(//div[text()='Application Form Filled'])[1]")));
            actions1.moveToElement(status).doubleClick().perform();
            Thread.sleep(500);

            WebElement statusDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("(//select)[1]")));
            select = new Select(statusDropdown);  
            select.selectByVisibleText("Deal Approved");
            Thread.sleep(1000);
                        
            driver.findElement(By.xpath("(//input[@value=\" Save \"])[1]")).click();

        } catch (Exception e) {
            e.printStackTrace();  // Print stack trace for debugging
      
        }
    }
}
