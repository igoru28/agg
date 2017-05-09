package org.test.ag.site;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.test.ag.goods.Product;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


/**
 * Created by igor on 14.03.17.
 */
public class AmazonClient {
    private final WebDriver driver;
    private final String baseUrl = "https://sellercentral.amazon.com/";
    private static final Logger logger = LogManager.getLogger(AmazonClient.class);
    private static final int waitIntervalSeconds = 30;

    public AmazonClient() {
        logger.debug("initializing driver...");
        File profileDir = new File("profile");
        if (!profileDir.exists()) {
            logger.debug(String.format("Making %s directory for profile", profileDir.getAbsolutePath()));
            profileDir.mkdir();
        }
        this.driver = new FirefoxDriver(new FirefoxProfile());
        logger.debug(String.format("Setting element find interval %d seconds...", waitIntervalSeconds));
        driver.manage().timeouts().implicitlyWait(waitIntervalSeconds, TimeUnit.SECONDS);
        logger.info("driver {} has been initialized", driver);
    }

    public boolean needAuth() throws Exception {
        logger.info("checking if authentication is required?");
        try {
            logger.debug("Navigating base URL {}", baseUrl);
            driver.get(baseUrl);
            int currentWaitInterval = 1;
            logger.debug("Setting element find interval to {} seconds", 1);
            driver.manage().timeouts().implicitlyWait(currentWaitInterval, TimeUnit.SECONDS);
            WebElement settingsMenu = findSettingsMenu();
            boolean result = !(settingsMenu.isDisplayed() && settingsMenu.isEnabled());
            logger.info("authentication is required: " + result);
            return result;
        } catch (NoSuchElementException ex) {
            logger.info("cannot find settings menu. Authentication is required");
            return true;
        } catch (Exception ex) {
            logger.error("auth check error", ex);
            throw ex;
        } finally {
            logger.debug("Restoring element find interval to {} seconds", waitIntervalSeconds);
            driver.manage().timeouts().implicitlyWait(waitIntervalSeconds, TimeUnit.SECONDS);
        }
    }

    public void auth(String email, String password) throws Exception {
        logger.info("authenticating using email {} and password XXXXXXX", email);
        try {
            driver.get(baseUrl + "/ap/signin?openid.pape.max_auth_age=18000&openid.return_to=https%3A%2F%2Fsellercentral.amazon.com%2Fgp%2Fhomepage.html&openid.identity=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select&openid.assoc_handle=sc_na_amazon_v2&_encoding=UTF8&openid.mode=checkid_setup&openid.ns.pape=http%3A%2F%2Fspecs.openid.net%2Fextensions%2Fpape%2F1.0&language=en_US&openid.claimed_id=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0%2Fidentifier_select&pageId=sc_na_amazon&openid.ns=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0&ssoResponse=eyJ6aXAiOiJERUYiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiQTI1NktXIn0.AXcevbtscxcSMhTltXc0zMtn8MHR03keS4YOSFVBXci54mt9fbXjTQ.tkv465PKqfhsuvt_.pc4wvwyPlg5F7UCIsNr3jF0DRAll4JELItTCwpeNEUItbbFGdbQZ7Qhdsua28Bc89ChfCbRFXwEQapziXJIjQryefJy0n12rSyl8v63ZeO4jQLVARiVC95wMbeYb060pp6qFEvZTFe7ku6Oy3XncZrJqylZPB1gXC1FsWN3nBr0KwlWK51yafGPqgzxPk8KRH5jsGSjX8MpBNGS8DCxJ5pXuQCbxicV7ru1zYTftszezrgu7zTRb4hqt7h0E-5yLHw.xe2m03QgaXxzl3Pz_kn0HQ");
            WebElement ap_email = driver.findElement(By.cssSelector("input#ap_email"));
            logger.debug("Found email input element {}", ap_email.toString());
            ap_email.clear();
            ap_email.sendKeys(email);
            WebElement ap_password = driver.findElement(By.cssSelector("input#ap_password"));
            logger.debug("Found password input element {}", ap_password);
            ap_password.clear();
            ap_password.sendKeys(password);
            driver.findElement(By.id("signInSubmit")).click();
            logger.debug("Checking whether if authentication succeeded...");
            WebElement settingsMenu = findSettingsMenu();
            if (settingsMenu.isDisplayed() && settingsMenu.isEnabled()) {
                logger.info("successfully authenticated");
            } else {
                throw new IllegalStateException();
            }
        } catch (Exception ex) {
            logger.error("Authentication failed", ex);
            throw ex;
        }
    }

    private WebElement findSettingsMenu() {
        return driver.findElement(By.tagName("span").className("sc-quick-txt"));
    }

    public String addProduct(Product product) throws Exception {
        logger.info("Adding product {} ...", product);
        try {
            String mainWindow = driver.getWindowHandle();
            String productSearchPageUrl = String.format("%s/productsearch?q=%s&ref_=xx_prodsrch_cont_prodsrch", baseUrl, product.getId());
            logger.debug("Navigating product search page {} ...", productSearchPageUrl);
            driver.get(productSearchPageUrl);

            logger.debug("Clicking product search button...");
            driver.findElement(By.cssSelector("#product-search-button > span.a-button-inner > input.a-button-input")).click();
//            driver.findElement(By.id("product-search-button")).click();

            logger.debug("Waiting for product adding button gets enabled...");
            new WebDriverWait(driver, waitIntervalSeconds).until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver webDriver) {
                    WebElement button = driver.findElement(By.id("a-autoid-2-announce"));
                    if (button.isDisplayed() && button.isEnabled()) {
                        logger.debug("Adding button is loaded and enabled");
                        return true;
                    }
                    return false;
                }
            });
            logger.debug("Opening product adding page");
            driver.findElement(By.id("a-autoid-2-announce")).click();
            new WebDriverWait(driver, waitIntervalSeconds).until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver webDriver) {
                    return webDriver.getWindowHandles().size() > 1;
                }
            });
            logger.debug("Switching to product add page");
            ArrayList<String> windowsList = new ArrayList<String>(driver.getWindowHandles());
            for (String w: windowsList) {
                if (!mainWindow.equals(w)) {
                    driver.switchTo().window(w);
                    break;
                }
            }
            WebElement advancedViewSwitch = driver.findElement(By.id("advanced-view-switch-top"));//.click();
//            WebElement advancedViewSwitch = driver.findElement(By.className("aui-switch"));
            logger.debug("Switching to advanced view...");
            while (!advancedViewSwitch.isSelected()) {
//                advancedViewSwitch.click();
                driver.findElement(By.cssSelector("label[for='advanced-view-switch-top']")).click();
//                driver.findElement(By.cssSelector("div.view-toggle-tab-bar")).click();
            }
            logger.debug("Setting conditionNote to {}", product.getConditionNote());
            WebElement conditionNote = driver.findElement(By.id("condition_note"));
            conditionNote.click();
            conditionNote.clear();
            conditionNote.sendKeys(product.getConditionNote());
            logger.debug("Setting quantity to {}", product.getQuantity());
            WebElement quantity = driver.findElement(By.id("quantity"));
            quantity.click();
            quantity.clear();
            quantity.sendKeys(String.valueOf(product.getQuantity()));

            logger.debug("Setting condition type to New");
            WebElement conditionType = driver.findElement(By.id("condition_type"));
            conditionType.click();
            new Select(conditionType).selectByVisibleText("New");
//            driver.findElement(By.cssSelector("option[value='new, new']")).click();

            logger.debug("Setting standard price to {}", product.getPrice());
            WebElement standardPrice = driver.findElement(By.id("standard_price"));
            standardPrice.click();
            standardPrice.clear();
            standardPrice.sendKeys(String.valueOf(product.getPrice()));

            logger.debug("Setting fulfillment latency to {}", product.getLatency());
            WebElement fulfillmentLatency = driver.findElement(By.id("fulfillment_latency"));
            fulfillmentLatency.click();
            fulfillmentLatency.clear();
            fulfillmentLatency.sendKeys(String.valueOf(product.getLatency()));

            logger.debug("Waiting until submit button is enabled...");
            final WebElement mainSubmitButton = driver.findElement(By.id("main_submit_button"));//.click();
            new WebDriverWait(driver, waitIntervalSeconds).until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver webDriver) {
                    return mainSubmitButton.isEnabled();
                }
            });
            logger.debug("Submitting product info...");
            mainSubmitButton.click();
            logger.debug("Checking if there a success alert...");
            driver.findElement(By.cssSelector("div.a-box.a-alert.a-alert-success.newItemAlert"));
//            String title = driver.findElement(By.cssSelector("table > tbody > tr > td[data-column=title] > div.mt-combination.mt-layout-block > div[data-column=\"title\"]")).getText();
//            String id = driver.findElement(By.cssSelector("table > tbody > tr > td[data-column=title] > div.mt-combination.mt-layout-block > div[data-column=\"asin\"]")).getText();
            logger.debug("Getting product additional info from success alert");
            String productDetails = driver.findElement(By.cssSelector("div.a-alert-content > table.a-normal > tbody > tr > td.newItemDetails")).getText();
            logger.info("successfully added product {}", productDetails);
            logger.debug("Closing product add window");
            driver.close();
            logger.debug("Switching to main window");
            driver.switchTo().window(mainWindow);
            return productDetails;
        } catch (Exception ex) {
            logger.error("error adding product " + product, ex);
            throw ex;
        }
    }

    public void close() {
        driver.close();
    }
    public void quit() { driver.quit(); }
}
