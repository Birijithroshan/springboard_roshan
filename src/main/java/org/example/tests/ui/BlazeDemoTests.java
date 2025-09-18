package org.example.tests.ui;

import org.example.engine.BaseUITest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

/**
 * Real-world UI tests for BlazeDemo flight booking website
 * Tests the complete flight booking workflow
 */
public class BlazeDemoTests extends BaseUITest {

    private static final String BLAZEDEMO_URL = "https://blazedemo.com/";

    @Test(description = "Verify homepage loads successfully")
    public void testHomepageLoad() {
        driver.get(BLAZEDEMO_URL);

        // Verify page title
        String pageTitle = driver.getTitle();
        Assert.assertTrue(pageTitle.contains("BlazeDemo"),
            "Page title should contain 'BlazeDemo', but was: " + pageTitle);

        // Verify departure city dropdown is present
        WebElement departureDropdown = driver.findElement(By.name("fromPort"));
        Assert.assertTrue(departureDropdown.isDisplayed(), "Departure city dropdown should be visible");

        // Verify destination city dropdown is present
        WebElement destinationDropdown = driver.findElement(By.name("toPort"));
        Assert.assertTrue(destinationDropdown.isDisplayed(), "Destination city dropdown should be visible");

        // Verify find flights button is present
        WebElement findFlightsBtn = driver.findElement(By.cssSelector("input[type='submit']"));
        Assert.assertTrue(findFlightsBtn.isDisplayed(), "Find Flights button should be visible");

        testLogger.info("Homepage loaded successfully with all required elements");
    }

    @Test(description = "Search for flights from Boston to London")
    public void testFlightSearch() {
        driver.get(BLAZEDEMO_URL);

        // Select departure city
        Select departureSelect = new Select(driver.findElement(By.name("fromPort")));
        departureSelect.selectByVisibleText("Boston");

        // Select destination city
        Select destinationSelect = new Select(driver.findElement(By.name("toPort")));
        destinationSelect.selectByVisibleText("London");

        // Click find flights
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        // Wait for results page to load
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));

        // Verify we're on the reserve page
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("reserve.php"),
            "Should be on reserve page after flight search");

        // Verify flight results table is present
        WebElement flightTable = driver.findElement(By.tagName("table"));
        Assert.assertTrue(flightTable.isDisplayed(), "Flight results table should be displayed");

        // Verify there are flight options
        List<WebElement> flightRows = driver.findElements(By.xpath("//table/tbody/tr"));
        Assert.assertTrue(flightRows.size() > 0, "Should have at least one flight option");

        testLogger.info("Flight search completed successfully with " + flightRows.size() + " flight options");
    }

    @Test(description = "Complete flight booking workflow")
    public void testCompleteFlightBooking() {
        driver.get(BLAZEDEMO_URL);

        // Step 1: Search for flights
        Select departureSelect = new Select(driver.findElement(By.name("fromPort")));
        departureSelect.selectByVisibleText("Portland");

        Select destinationSelect = new Select(driver.findElement(By.name("toPort")));
        destinationSelect.selectByVisibleText("Rome");

        driver.findElement(By.cssSelector("input[type='submit']")).click();

        // Step 2: Select a flight
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));

        // Click on the first "Choose This Flight" button
        driver.findElement(By.cssSelector("input[value='Choose This Flight']")).click();

        // Step 3: Fill purchase form
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("inputName")));

        driver.findElement(By.id("inputName")).sendKeys("John Doe");
        driver.findElement(By.id("address")).sendKeys("123 Test Street");
        driver.findElement(By.id("city")).sendKeys("Test City");
        driver.findElement(By.id("state")).sendKeys("Test State");
        driver.findElement(By.id("zipCode")).sendKeys("12345");

        // Select card type
        Select cardTypeSelect = new Select(driver.findElement(By.id("cardType")));
        cardTypeSelect.selectByVisibleText("Visa");

        driver.findElement(By.id("creditCardNumber")).sendKeys("4111111111111111");
        driver.findElement(By.id("creditCardMonth")).sendKeys("12");
        driver.findElement(By.id("creditCardYear")).sendKeys("2025");
        driver.findElement(By.id("nameOnCard")).sendKeys("John Doe");

        // Submit purchase
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        // Step 4: Verify confirmation page
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

        String confirmationTitle = driver.findElement(By.tagName("h1")).getText();
        Assert.assertTrue(confirmationTitle.contains("Thank you"),
            "Confirmation page should display thank you message");

        // Verify confirmation details are present
        WebElement confirmationTable = driver.findElement(By.tagName("table"));
        Assert.assertTrue(confirmationTable.isDisplayed(), "Confirmation details table should be present");

        testLogger.info("Flight booking completed successfully");
    }

    @Test(description = "Verify all destination cities are available")
    public void testDestinationCities() {
        driver.get(BLAZEDEMO_URL);

        Select destinationSelect = new Select(driver.findElement(By.name("toPort")));
        List<WebElement> destinations = destinationSelect.getOptions();

        // Expected destinations based on BlazeDemo
        String[] expectedDestinations = {
            "Choose a Destination City:",
            "Buenos Aires",
            "Rome",
            "London",
            "Berlin",
            "New York",
            "Dublin",
            "Cairo"
        };

        Assert.assertEquals(destinations.size(), expectedDestinations.length,
            "Should have " + expectedDestinations.length + " destination options");

        for (int i = 0; i < expectedDestinations.length; i++) {
            Assert.assertEquals(destinations.get(i).getText(), expectedDestinations[i],
                "Destination at index " + i + " should match expected value");
        }

        testLogger.info("All destination cities verified successfully");
    }

    @Test(description = "Verify form validation on purchase page")
    public void testFormValidation() {
        // Navigate directly to purchase page (this will show validation behavior)
        driver.get("https://blazedemo.com/purchase.php");

        // Try to submit empty form
        driver.findElement(By.cssSelector("input[type='submit']")).click();

        // Verify we're still on purchase page (form validation should prevent submission)
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.contains("purchase.php"),
            "Should remain on purchase page when form is invalid");

        testLogger.info("Form validation test completed");
    }
}
