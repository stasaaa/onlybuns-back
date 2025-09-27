package com.isa.onlybuns_back.standardizacija.e2e;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.*;
import org.junit.runner.RunWith;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthenticationE2ETest {

    private WebDriver driver;
    private WebDriverWait wait;

    @Before
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // bez otvaranja prozora
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testLoginFlow() {
        driver.get("http://localhost:3000/login");

        // pronalazi polja i dugme
        WebElement emailInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("email"))
        );
        WebElement passwordInput = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Login')]"));

        // unos podataka
        emailInput.sendKeys("milavujkov@gmail.com");
        passwordInput.sendKeys("12345678");
        loginButton.click();

        // proveri da li je URL posle logina /
        wait.until(ExpectedConditions.urlToBe("http://localhost:3000/"));
        String currentUrl = driver.getCurrentUrl();

        assertTrue("Korisnik treba da bude na home stranici nakon logina",
                currentUrl.equals("http://localhost:3000/"));
    }

    @Test
    public void testLogoutFlow() {
        try {
            // idi na login stranicu
            driver.get("http://localhost:3000/login");

            // pronadji inpute za email i password
            WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("email")));
            WebElement passwordField = driver.findElement(By.id("password"));

            // unesi kredencijale
            emailField.sendKeys("milavujkov@gmail.com");
            passwordField.sendKeys("12345678");

            // klikni login
            WebElement loginButton = driver.findElement(By.xpath("//button[contains(text(),'Login')]"));
            loginButton.click();

            // sacekaj da se prebaci na /
            wait.until(ExpectedConditions.urlToBe("http://localhost:3000/"));

            // pronadji logout dugme po klasi
            By logoutSelector = By.xpath("//button[@type='button' and contains(@class,'logout-btn')]");
            WebElement logoutButton = wait.until(ExpectedConditions.presenceOfElementLocated(logoutSelector));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", logoutButton);

            // proveri da li su se pojavili Login i Register linkovi
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(text(),'Login')]")));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(text(),'Register')]")));

            System.out.println("Test za logout je prosao.");
        } catch (Exception e) {
            System.out.println("! Test za logout je pukao: " + e.getMessage());
            assertTrue("Test pukao", false);
        }
    }
}