package d4u.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.Set;

public class SalesforcePage {
    private WebDriver driver;
    private WebDriverWait wait;

    public SalesforcePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(40));
    }

    public void login(String username, String password) {
        driver.get("https://login.salesforce.com/?locale=in");
        driver.findElement(By.name("username")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.name("Login")).click();
    }

    public void searchAndOpenOffer(String offerNumber) throws InterruptedException {
        WebElement search = wait.until(ExpectedConditions.elementToBeClickable(By.name("str")));
        search.sendKeys(offerNumber);
        Thread.sleep(3000);
        driver.findElement(By.xpath("//input[@type='button']")).click();

        WebElement id = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("(//a[contains(@data-seclke, '01I9')])[2]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", id);
    }

    public void createBooking() {
        driver.findElement(By.xpath("//input[@title='Create Booking']")).click();
    }

    public void handlePopup(String customerId) throws InterruptedException {
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

        Actions actions = new Actions(driver);
        for (int i = 0; i < 4; i++) {
            actions.sendKeys(Keys.TAB);
        }
        actions.sendKeys(customerId).sendKeys(Keys.ENTER).perform();

        driver.switchTo().window(mainWindow);
    }

    public void submitBookingDetails() throws InterruptedException {
        WebElement sharingRatio = driver.findElement(By.id("thepage:form:pb:j_id52:applicants:1:j_id70"));
        sharingRatio.sendKeys("0.00");
        Thread.sleep(1000);

        driver.findElement(By.xpath("(//span[@class='dateFormat'])[2]")).click();
        driver.findElement(By.xpath("//input[@type='submit']")).click();
        Thread.sleep(1000);

        WebElement status = driver.findElement(By.xpath("(//input[@type='submit'])[2]"));
        Select select = new Select(status);
        select.selectByVisibleText("Deal Approved");
    }
}
