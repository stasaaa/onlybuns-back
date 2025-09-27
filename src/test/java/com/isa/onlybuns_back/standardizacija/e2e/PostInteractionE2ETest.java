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
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostInteractionE2ETest {

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
    public void testAddCommentFlow() {
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

            // skroluj malo nadole da bi se ucitali postovi
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 500);");
            Thread.sleep(2000);

            // DODAJ: nadji dugme za komentare i span unutar njega
            List<WebElement> interactionButtons = driver.findElements(By.cssSelector("button.interaction-btn"));
            WebElement commentButton = interactionButtons.get(1); // drugi button (0=like, 1=comment, 2=share)
            WebElement commentCountElement = commentButton.findElement(By.tagName("span"));

            String initialCountText = commentCountElement.getText();
            int initialCount = Integer.parseInt(initialCountText);
            System.out.println("Pocetni broj komentara: " + initialCount);

            // nadji input za komentar
            WebElement commentInput = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@placeholder='Add a comment...']"))
            );
            commentInput.sendKeys("Ovo je E2E test komentar");

            // nadji dugme "Post"
            WebElement postButton = driver.findElement(By.xpath("//button[contains(text(),'Post')]"));

            // cekaj da postane klikabilno
            wait.until(ExpectedConditions.elementToBeClickable(postButton));

            // skroluj do njega
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", postButton);

            // probaj normalan klik
            try {
                postButton.click();
            } catch (ElementClickInterceptedException e) {
                // ako nesto prekriva dugme, koristi JS klik
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", postButton);
            }

            Thread.sleep(2000); // malo pricekaj da se komentar posalje

            // DODAJ: proveri da li se brojač povećao
            List<WebElement> updatedInteractionButtons = driver.findElements(By.cssSelector("button.interaction-btn"));
            WebElement updatedCommentButton = updatedInteractionButtons.get(1);
            WebElement updatedCountElement = updatedCommentButton.findElement(By.tagName("span"));

            String newCountText = updatedCountElement.getText();
            int newCount = Integer.parseInt(newCountText);

            assertEquals("Brojac komentara se nije povećao", initialCount + 1, newCount);
            System.out.println("Brojač komentara se povećao sa " + initialCount + " na " + newCount);

            // proveri da li se komentar pojavio u listi
            List<WebElement> comments = driver.findElements(By.xpath("//*[contains(text(),'Ovo je E2E test komentar')]"));
            assertFalse(comments.isEmpty(), "Komentar nije dodat");
            System.out.println("Test za dodavanje komentara je prosao.");

        } catch (Exception e) {
            System.out.println("! Test za dodavanje komentara je pukao: " + e.getMessage());
            assertTrue("Test pukao", false);
        }
    }

    @Test
    public void testLikePostFlow() {
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

            // skroluj malo nadole da se ucitaju postovi
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 500);");

            // nadji dugme za lajkovanje
            WebElement likeButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button.like-btn")));

            // nadji span unutar dugmeta (broj lajkova)
            WebElement likeCountElement = likeButton.findElement(By.tagName("span"));

            // uzmi pocetni broj
            int initialCount = Integer.parseInt(likeCountElement.getText().trim());

            // proveri boju dugmeta (da li je vec lajkovano)
            String color = likeButton.getCssValue("color"); // npr. "rgba(236, 93, 67, 1)" za narandzasto
            boolean alreadyLiked = color.contains("236, 93, 67"); // narandzasta boja

            if (!alreadyLiked) {
                // nije lajkovano → klikni jednom, broj mora da se poveća
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", likeButton);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", likeButton);

                // cekaj da se broj promeni
                wait.until(ExpectedConditions.not(
                        ExpectedConditions.textToBePresentInElement(likeCountElement, String.valueOf(initialCount))
                ));

                int newCount = Integer.parseInt(likeCountElement.getText().trim());
                assertTrue("Broj lajkova se nije povecao", newCount > initialCount);
                System.out.println("Post nije bio lajkovan → broj lajkova se povecao.");
            } else {
                // vec lajkovano → klikni dva puta, broj mora ostati isti
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", likeButton);

                // odlajkuj
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", likeButton);
                wait.until(ExpectedConditions.not(
                        ExpectedConditions.textToBePresentInElement(likeCountElement, String.valueOf(initialCount))
                ));

                // ponovo lajkuj
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", likeButton);
                wait.until(ExpectedConditions.textToBePresentInElement(likeCountElement, String.valueOf(initialCount)));

                int finalCount = Integer.parseInt(likeCountElement.getText().trim());
                assertEquals("Ako je post bio lajkovan, broj mora ostati isti.", initialCount, finalCount);
                System.out.println("Post je vec bio lajkovan → broj lajkova ostaje isti posle odlajka i ponovnog lajka.");
            }

            // provera boje ikone na kraju
            String finalColor = likeButton.getCssValue("color");
            assertTrue("Dugme mora biti narandzasto nakon lajka", finalColor.contains("236, 93, 67"));
            System.out.println("Test za lajkovanje objave je prosao.");

        } catch (Exception e) {
            System.out.println("! Test za lajkovanje objave je pukao: " + e.getMessage());
            assertTrue("Test pukao", false);
        }
    }
}