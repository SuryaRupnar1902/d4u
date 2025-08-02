package d4u.tests;

import d4u.utils.DBUtils;
import d4u.utils.ExcelUtil;
import d4u.utils.WebDriverUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SalesforceTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private SoftAssert softAssert;  // SoftAssert instance

    @BeforeClass
    public void setUp() {
        driver = WebDriverUtils.initializeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(50));
        driver.manage().window().maximize();
        softAssert = new SoftAssert();  // Initialize SoftAssert
        
        // Initialize Excel file for logging
        ExcelUtil.initializeExcel();
    }
    @Test (singleThreaded = true)
    public void testCreateBookings() throws InterruptedException {
        Map<String, List<String>> offerToSFIDsMap = DBUtils.getOfferAndContactDetails();

        if (offerToSFIDsMap.isEmpty()) {
            System.out.println("No offers available");
            return;
        }

        for (Map.Entry<String, List<String>> entry : offerToSFIDsMap.entrySet()) {
            String offerName = entry.getKey();
            List<String> sfids = entry.getValue();

            System.out.println("Processing offer: " + offerName);

            if (sfids.isEmpty() || sfids.get(0) == null) {
                System.out.println("No Applicant Found for this Offer: " + offerName);
                continue;
            }
            try {
                String bookingId = null;
                String status = "Booking failed";  // Default status
                
                if (sfids.size() == 1) {
                    bookingId = createBookingWithSingleSFID(offerName, sfids.get(0));
                    status = "Success";
                } else if (sfids.size() == 2) {
                    bookingId = createBookingWithMultipleSFIDs(offerName, sfids);
                    status = "Success";
                } else {
                    System.out.println("This offer has more than 2 applicants.");
                }

                // Log booking result in Excel
                ExcelUtil.logBookingResult(offerName, sfids.toString(), bookingId, status);
            } catch (UnhandledAlertException e) {
                handlePopupAndCaptureScreenshot("Popup_Blocking_" + offerName);
                ExcelUtil.logBookingResult(offerName, "N/A", null, "Popup Blocked");
            } catch (Exception e) {
                softAssert.fail("Failed to create booking for offer: " + offerName + " - " + e.getMessage());
                captureScreenshot("Error_" + offerName);
                ExcelUtil.logBookingResult(offerName, "N/A", null, "Booking failed: " + e.getMessage());
                e.printStackTrace();
            }
        }
        softAssert.assertAll();        
        ExcelUtil.saveExcel();
    }
    private String createBookingWithSingleSFID(String offerName, String sfid) throws InterruptedException {
        loginToSalesforce();
        searchAndOpenOffer(offerName);
        AddApplicantName(sfid);
        CreateACtualBooking();
        System.out.println("Booking created successfully for: " + offerName);
		return sfid;
    }

    private String createBookingWithMultipleSFIDs(String offerName, List<String> sfids) throws InterruptedException {
        loginToSalesforce();
        searchAndOpenOffer(offerName);
        
        addApplicant(sfids.get(1));
        System.out.println("Booking created successfully for: " + offerName);
		return offerName;
    }
    private void loginToSalesforce() {
        driver.get("https://login.salesforce.com/?locale=in");
        driver.findElement(By.name("username")).sendKeys("appadmin@godrejproperties.com");
        driver.findElement(By.id("password")).sendKeys("d4Eg9Dk7@hS2x");
        driver.findElement(By.name("Login")).click();
    }
    private void searchAndOpenOffer(String offerName) throws InterruptedException {
        WebElement search = wait.until(ExpectedConditions.elementToBeClickable(By.name("str")));
        search.sendKeys(offerName);
        Thread.sleep(3000);
        driver.findElement(By.xpath("//input[@type='button']")).click();
    }   
    String mainWindow;
    
    private void AddApplicantName(String sfid) throws InterruptedException {
      WebElement id = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//a[contains(@data-seclke, '01I9')])[2]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", id);
        driver.findElement(By.xpath("//input[@title='Create Booking']")).click();
        Thread.sleep(2000);
        
        WebElement applicantName = driver.findElement(By.xpath("//input[@name=\"thepage:form:pb:j_id52:applicants:0:j_id64\"]"));
        applicantName.clear();
        
        WebElement lookupIcon = driver.findElement(By.xpath("(//img[@alt='Customer Name Lookup (New Window)'])"));
                        lookupIcon.click();
        Thread.sleep(500);
        try {
            String mainWindow = driver.getWindowHandle(); // Store main window handle
            Set<String> allWindows = driver.getWindowHandles();

            for (String window : allWindows) {
                if (!window.equals(mainWindow)) {
                    driver.switchTo().window(window);
                    break;
                }
            }          
            WebElement iframeElement = driver.findElement(By.id("resultsFrame"));  // Replace "iframe_id" with actual ID or locator
            driver.switchTo().frame(iframeElement);
            
            Thread.sleep(2000);
            
            WebElement name = driver.findElement(By.xpath("//*[@id=\"00N6F00000RVuChUALContact\"]"));
            name.sendKeys(sfid);
            Thread.sleep(1000);
            
            WebElement filter = driver.findElement(By.xpath(" //input[@value=\"Apply Filters\"]"));     
            filter.click();
            Thread.sleep(1000);
            
            WebElement Name = driver.findElement(By.xpath("//a[@class=\" dataCell \"]"));     
           String ApplicantName= Name.getText();
            
            System.out.println(ApplicantName);
            
            driver.switchTo().defaultContent();
            driver.switchTo().window(mainWindow);            
            applicantName.sendKeys(ApplicantName);
            // Switch back to the main window
        } catch (UnreachableBrowserException e) {
            System.out.println("Browser became unreachable. Restarting driver may be necessary.");
        } catch (Exception e) {
            System.out.println("Error during popup interaction: " + e.getMessage());
            return; 
        }                     
          Thread.sleep(500);
    }    
      private void CreateACtualBooking() throws InterruptedException {
        WebElement TdsDate = driver.findElement(By.xpath("(//span[@class='dateFormat'])[2]"));
        TdsDate.click();

        driver.findElement(By.xpath("//input[@type='submit']")).click();
        Thread.sleep(1000);
        driver.findElement(By.xpath("(//input[@type='submit'])[2]")).click();
        Thread.sleep(2000);

        Actions actions11 = new Actions(driver);
        WebElement dblClick = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//td[contains(text(),'Deal Status')]/following-sibling::td[contains(@class, 'dataCol')])[1]")));
        actions11.moveToElement(dblClick).doubleClick().perform();
        Thread.sleep(1000);

        WebElement approveDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//select)[1]")));
        Select select = new Select(approveDropdown);
        select.selectByVisibleText("Approved");
        Thread.sleep(1000);

        WebElement status = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//div[text()='Application Form Filled'])[1]")));
        actions11.moveToElement(status).doubleClick().perform();
        Thread.sleep(500);

        WebElement statusDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//select)[1]")));
        select = new Select(statusDropdown);
        select.selectByVisibleText("Deal Approved");
        Thread.sleep(1000);

        driver.findElement(By.xpath("(//input[@value=' Save '])[1]")).click();
        
        WebElement bookingIdElement = driver.findElement(By.xpath("//*[@class=\"pageDescription\"]"));  
        String bookingId = bookingIdElement.getText();
        System.out.println("Booking ID captured: " + bookingId);
    }   
// // Capture screenshot and get the text after focusing on the 'name' field
//    private void captureScreenshotAndGetTextFromNameField(String fileName) {
//        try {
//            // Assuming the field is in focus after pressing Tab 20 times
//            WebElement nameField = driver.findElement(By.cssSelector("input[name='name']"));  // Replace with appropriate locator
//            
//            // Capture screenshot
//            captureScreenshot(fileName);
//
//            // Get the text from the 'name' field
//            String nameText = nameField.getAttribute("value");  // Or use .getText() if it's a text element
//            System.out.println("Text from the 'name' field: " + nameText);
//        } catch (Exception e) {
//            System.out.println("Error capturing screenshot and retrieving text: " + e.getMessage());
//        }
//    }
        
    private void addApplicant(String sfid) throws InterruptedException {
        driver.findElement(By.xpath("//b[text()='Add more applicants']")).click();
        Thread.sleep(2000);
        Actions actions = new Actions(driver);
        actions.sendKeys(sfid).perform();
        Thread.sleep(1000);
    }
    private void handlePopupAndCaptureScreenshot(String fileName) {
        try {
            Alert alert = driver.switchTo().alert();
            System.out.println("Popup appeared: " + alert.getText());
            captureScreenshot(fileName);
            alert.accept();  // Close the popup
        } catch (NoAlertPresentException e) {
            System.out.println("No popup detected.");
        }
    }

    private void captureScreenshot(String fileName) {
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filePath = "screenshots/" + fileName + "_" + timestamp + ".png";
        try {
            Files.createDirectories(Paths.get("screenshots"));  // Ensure directory exists
            Files.copy(screenshot.toPath(), Paths.get(filePath));
            System.out.println("Screenshot saved at: " + filePath);
        } catch (IOException e) {
            System.err.println("Failed to save screenshot: " + e.getMessage());
        }
        

    }

//    @AfterClass
//    public void tearDown() {
//        if (driver != null) {
//            driver.quit();
//        }
//    }
}
