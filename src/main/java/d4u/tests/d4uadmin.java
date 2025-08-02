package d4u.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class d4uadmin {
    public static void main(String[] args) throws InterruptedException {
        // Set up the ChromeDriver
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\LENOVO\\Downloads\\chromedriver-win64\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();

        try {
            driver.get("https://d4u.godrejproperties.com/mumbai/d4uadlogin");
            driver.manage().window().maximize();

            // Wait for the username field to be present and input credentials
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='username']")));
            username.sendKeys("naveen.moningi@godrejproperties.com");

            WebElement password = driver.findElement(By.xpath("//input[@name='password']"));
            password.sendKeys("dhccwdc");

            // Wait for login button and click
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@id='loginbtn']")));
            button.click();

            // Wait for the project dropdown to be visible
            WebElement projectDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//select[@id='projectSelected']")));

            // Create a Select object for the project dropdown and select the option by visible text
            Select projectSelect = new Select(projectDropdown);
            projectSelect.selectByVisibleText("Godrej North Estate");
            
            // Add a slight delay to ensure page loads fully
            Thread.sleep(2000);

            // Wait for the role dropdown to be visible and then create another Select object for it
            WebElement roleDropdown = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//select[@id='roleSelected']")));
            Select roleSelect = new Select(roleDropdown);

            // Select the role by visible text
            roleSelect.selectByVisibleText("KYC Approval");

        } catch (Exception e) {
            e.printStackTrace();
       
        }
    }
}
