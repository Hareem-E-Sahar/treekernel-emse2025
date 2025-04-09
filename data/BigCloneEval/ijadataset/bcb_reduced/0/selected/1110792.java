package watij;

import com.tapsterrock.jiffie.IHTMLDocument2;
import org.w3c.dom.Element;
import watij.runtime.MissingWayOfFindingObjectException;
import watij.runtime.NotImplementedYetException;
import watij.runtime.ie.IEController;
import watij.runtime.ie.IESupportsSubElements;
import watij.symbols.Symbol;
import watij.symbols.TitleSymbol;
import watij.symbols.UrlSymbol;
import watij.utilities.StringUtils;
import watij.elements.HtmlElement;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User:
 * Date: Apr 13, 2006
 * Time: 7:58:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class IE extends IESupportsSubElements implements WatijBrowser {

    IEController ieController;

    public IE() {
    }

    private IE(IEController ieController) {
        this.ieController = ieController;
    }

    protected IHTMLDocument2 ihtmlDocument2() throws Exception {
        return ieController.ihtmlDocument2();
    }

    protected Element element() throws Exception {
        return document().getDocumentElement();
    }

    public IE childBrowser() throws Exception {
        return childBrowser(0);
    }

    public IE childBrowser(int index) throws Exception {
        return new IE(ieController.getChildBrowser(index));
    }

    public int childBrowserCount() throws Exception {
        return ieController.getChildBrowserCount();
    }

    public void executeScript(String script) throws Exception {
        ieController.executeScript(script);
    }

    public void show() throws Exception {
        htmlElements().show();
    }

    public HtmlElement active() throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void navigate(String url) throws Exception {
        ieController.navigate(url);
    }

    public void attach(Symbol how, String what) throws Exception {
        if (how instanceof UrlSymbol) {
            ieController = IEController.attachByUrl(what);
        } else if (how instanceof TitleSymbol) {
            ieController = IEController.attachByTitle(what);
        } else {
            throw new MissingWayOfFindingObjectException();
        }
    }

    public void start(String url) throws Exception {
        start();
        goTo(url);
    }

    public void start() throws Exception {
        ieController = new IEController();
        goTo("about:blank");
    }

    public void addChecker(Object checker) throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void attachInit(Symbol how, String what) throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void back() throws Exception {
        ieController.goBack();
    }

    public void bringToFront() throws Exception {
        ieController.bringToFront();
    }

    public void checkForHttpError(WatijBrowser watijBrowser) throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void clearUrlList() throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void close() throws Exception {
        ieController.closeBrowser();
    }

    public boolean containsText(String textOrRegex) throws Exception {
        return StringUtils.matchesOrContains(textOrRegex, ieController.getInnerText());
    }

    public Object dir() throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void disableChecker(Object checker) throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void focus() throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void forward() throws Exception {
        ieController.goForward();
    }

    public boolean isFront() throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void goTo(String url) throws Exception {
        navigate(url);
    }

    public void log(String what) throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void maximize() throws Exception {
        ieController.maximize();
    }

    public void minimize() throws Exception {
        ieController.minimize();
    }

    public void refresh() throws Exception {
        ieController.refresh();
    }

    public void restore() throws Exception {
        ieController.restore();
    }

    public void screenCapture(String fullyQualifiedFileName_PngFormat) throws Exception {
        screenCapture(fullyQualifiedFileName_PngFormat, "png");
    }

    public void screenCapture(String fullyQualifiedFileName, String format) throws Exception {
        maximize();
        Robot robot = new Robot();
        Rectangle captureSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage bufferedImage = robot.createScreenCapture(captureSize);
        ImageIO.write(bufferedImage, format, new File(fullyQualifiedFileName));
        restore();
    }

    public void width(int width) throws Exception {
        ieController.setWidth(width);
    }

    public void height(int height) throws Exception {
        ieController.setHeight(height);
    }

    public int width() throws Exception {
        return ieController.getWidth();
    }

    public int height() throws Exception {
        return ieController.getHeight();
    }

    public void runErrorChecks() throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void sendKeys(String keyString) throws Exception {
        ieController.sendKeys(keyString);
    }

    public void sendKeys(String title, String keyString) throws Exception {
        ieController.sendKeys(title, keyString);
    }

    public void setFastSpeed() throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public void setSlowSpeed() throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public String status() throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }

    public String text() throws Exception {
        return ieController.text();
    }

    public String title() throws Exception {
        return ieController.getTitle();
    }

    public String url() throws Exception {
        return ieController.getUrl();
    }

    public void waitUntil() throws NotImplementedYetException {
        throw new NotImplementedYetException();
    }
}
