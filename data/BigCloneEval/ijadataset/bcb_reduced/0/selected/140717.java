package uk.ac.ebi.intact.view.webapp.it;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.test.context.ContextConfiguration;
import uk.ac.ebi.intact.core.unit.IntactBasicTestCase;
import uk.ac.ebi.intact.view.webapp.it.util.ScreenShotOnFailureRule;
import java.io.File;
import java.io.IOException;
import static uk.ac.ebi.intact.view.webapp.Constants.BASE_URL;

/**
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 *
 *
 */
@ContextConfiguration(locations = { "classpath*:/META-INF/intact.spring.xml", "classpath*:/META-INF/intact-view-test.spring.xml", "classpath*:/META-INF/intact-view.jpa-test.spring.xml" }, inheritLocations = false)
public abstract class IntactViewIT extends IntactBasicTestCase {

    protected WebDriver driver;

    protected WebDriverWait wait;

    @Rule
    public ScreenShotOnFailureRule screenshotOnFailureRule = new ScreenShotOnFailureRule();

    protected IntactViewIT() {
    }

    @Before
    public void setUp() throws Exception {
        this.driver = new FirefoxDriver();
        wait = new WebDriverWait(driver, 30, 500);
        screenshotOnFailureRule.setDriver(driver);
    }

    @After
    public void tearDown() throws Exception {
        driver.quit();
    }

    protected void waitUntilElementIsVisible(final By by) {
        wait.until(new ExpectedCondition<Boolean>() {

            public Boolean apply(WebDriver webDriver) {
                return driver.findElement(by) != null;
            }
        });
    }

    protected void waitUntilElementIsNotVisible(final By by) {
        wait.until(new ExpectedCondition<Boolean>() {

            public Boolean apply(WebDriver webDriver) {
                return driver.findElement(by) == null;
            }
        });
    }

    protected void waitUntilLoadingIsComplete() {
        wait.until(new ExpectedCondition<Boolean>() {

            public Boolean apply(WebDriver webDriver) {
                return "status-normal".equals(webDriver.findElement(By.id("statusIndicator")).getAttribute("class"));
            }
        });
    }

    protected void waitUntilElementHasValue(final By by, final String value) {
        wait.until(new ExpectedCondition<Boolean>() {

            public Boolean apply(WebDriver webDriver) {
                final WebElement element = driver.findElement(by);
                return element != null && (element.getAttribute("value")).equals(value);
            }
        });
    }

    protected void takeScreenshot(String filename) throws IOException {
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile, new File(filename));
    }

    protected void goToInteractionsTab() {
        driver.findElement(By.linkText("Interactions (101)")).click();
        waitUntilLoadingIsComplete();
    }

    protected void search(String query) {
        driver.findElement(By.id("queryTxt")).clear();
        driver.findElement(By.id("queryTxt")).sendKeys(query);
        driver.findElement(By.id("quickSearchBtn")).click();
    }

    protected int numberOfResultsDisplayed() {
        return Integer.parseInt(driver.findElement(By.id("mainPanels:totalResultsOut")).getText());
    }

    protected void goToTheStartPage() {
        driver.get(BASE_URL);
    }

    protected void goToTheQueryPage(String query) {
        driver.get(BASE_URL + "/query/" + query);
    }

    protected void goToInteractionDetailsPage(String interactionAc) {
        driver.get(BASE_URL + "/interaction/" + interactionAc);
    }

    protected int rowCountForDataTableWithId(String id) {
        return driver.findElements(By.xpath("//div[@id=\"" + id + "\"]/table/tbody/tr")).size();
    }

    protected String searchQuery() {
        return valueForElement(By.id("queryTxt"));
    }

    protected String valueForElement(By id) {
        return driver.findElement(id).getAttribute("value");
    }

    protected void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
