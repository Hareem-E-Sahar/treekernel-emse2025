package org.jfunfx;

import junit.framework.TestCase;
import org.jfunfx.jsconstruction.JFunFXContainer;
import org.jfunfx.jsconstruction.ObjectLocator;
import org.jfunfx.jsconstruction.components.TextInput;
import org.openqa.selenium.ie.InternetExplorerDriver;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * date 16.07.2009
 *
 * @author dvponomarev
 * @version 1.0
 */
public class LoginTest extends TestCase {

    private JFunFXComponentFactory factory;

    private InternetExplorerDriver driver;

    private String testName;

    private static int screenshotCounter = 1;

    @Override
    protected void setUp() throws Exception {
        driver = new InternetExplorerDriver();
        driver.get("file:///C:/Java/projects/my/homepage-mirror/education/docs/Flex/login/bin-debug/login.html");
        WebDriverJSExecutor jsExecutor = new WebDriverJSExecutor(driver);
        factory = new JFunFXComponentFactory(new JFunFXContainer(jsExecutor, "login"));
    }

    public void testLogin() throws IOException, AWTException {
        testName = "login";
        grabScreenShot();
        TextInput nick = factory.createTextInput("nick2");
        nick.inputText("bob");
        assertEquals("bob", nick.getText());
        grabScreenShot();
        TextInput password = factory.createTextInput("password22");
        password.inputText("jimmy");
        assertEquals("jimmy", password.getText());
        grabScreenShot();
        factory.createButton("enter").click();
        waitLoading();
        grabScreenShot();
        assertFalse(factory.createLabel("error_message_id").isVisible());
        assertTrue(factory.createLabel("welcome_page_id").isVisible());
    }

    public void testFail() {
        TextInput nick = factory.createTextInput("nick2");
        nick.inputText("bob");
        assertEquals("bob", nick.getText());
        TextInput password = factory.createTextInput("password22");
        password.inputText("jimmy2");
        assertEquals("jimmy2", password.getText());
        factory.createButton("enter").click();
        waitLoading();
        assertTrue(factory.createLabel("error_message_id").isVisible());
        assertFalse(factory.createLabel("welcome_page_id").isVisible());
    }

    @Override
    protected void tearDown() throws Exception {
        driver.close();
    }

    private void waitLoading() {
        TestUtils.waitLoading(factory, new ObjectLocator("login_hbox_id"), "data");
    }

    private void grabScreenShot() throws AWTException, IOException {
        grabScreenShot(testName);
    }

    private void grabScreenShot(String testName) throws AWTException, IOException {
        String dir = "c:/screenshots";
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        BufferedImage image = new Robot().createScreenCapture(new Rectangle(screenSize));
        File file = new File(dir + "/" + testName + (screenshotCounter++) + ".jpeg");
        new File(dir).mkdirs();
        file.createNewFile();
        ImageIO.write(image, "jpeg", file);
        System.out.println("Screenshot saved: " + file.getName() + ". Directory: " + file.getParentFile().getAbsolutePath() + ".");
    }
}
