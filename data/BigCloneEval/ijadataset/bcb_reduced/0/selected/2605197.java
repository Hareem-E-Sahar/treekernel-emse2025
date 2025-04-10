package com.gargoylesoftware.htmlunit.libraries;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mortbay.jetty.Server;
import com.gargoylesoftware.htmlunit.BrowserRunner;
import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.HttpWebConnectionTest;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebTestCase;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTableDataCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.gargoylesoftware.htmlunit.html.HtmlUnknownElement;

/**
 * Tests for 1.5 version of <a href="http://code.google.com/webtoolkit">Google Web Toolkit</a>.
 *
 * @version $Revision: 3421 $
 * @author Ahmed Ashour
 */
@RunWith(BrowserRunner.class)
public class GWT15Test extends WebTestCase {

    private Server server_;

    /**
     * @throws Exception if an error occurs
     */
    @Test
    public void hello() throws Exception {
        final List<String> collectedAlerts = new ArrayList<String>();
        final HtmlPage page = loadGWTPage("Hello", collectedAlerts);
        final HtmlButton button = (HtmlButton) page.getFirstByXPath("//button");
        final DomText buttonLabel = (DomText) button.getChildren().iterator().next();
        assertEquals("Click me", buttonLabel.getData());
        button.click();
        final String[] expectedAlerts = { "Hello, AJAX" };
        assertEquals(expectedAlerts, collectedAlerts);
    }

    /**
     * Tests I18N default language.
     * @throws Exception if an error occurs
     */
    @Test
    public void i18n() throws Exception {
        final Locale locale = Locale.getDefault();
        Locale.setDefault(Locale.US);
        final HtmlPage page = loadGWTPage("I18N", null);
        i18n(page, "numberFormatOutputText", "31,415,926,535.898");
        String timeZone = new SimpleDateFormat("Z").format(new SimpleDateFormat("d MMMMMMMM yyyy").parse("13 September 1999"));
        timeZone = timeZone.substring(0, 3) + ':' + timeZone.substring(3);
        i18n(page, "dateTimeFormatOutputText", "Monday, September 13, 1999 12:00:00 AM GMT" + timeZone);
        i18n(page, "messagesFormattedOutputText", "User 'amelie' has security clearance 'guest' and cannot access '/secure/blueprints.xml'");
        i18n(page, "constantsFirstNameText", "Amelie");
        i18n(page, "constantsLastNameText", "Crutcher");
        i18n(page, "constantsFavoriteColorList", new String[] { "Red", "White", "Yellow", "Black", "Blue", "Green", "Grey", "Light Grey" });
        i18n(page, "constantsWithLookupResultsText", "Red");
        final Map<String, String> map = new HashMap<String, String>();
        map.put("name", "Amelie Crutcher");
        map.put("timeZone", "EST");
        map.put("userID", "123");
        map.put("lastLogOn", "2/2/2006");
        i18nDictionary(page, map);
        Locale.setDefault(locale);
    }

    /**
     * Test I18N French language.
     * @throws Exception if an error occurs
     */
    @Test
    public void i18n_fr() throws Exception {
        final Locale locale = Locale.getDefault();
        Locale.setDefault(Locale.US);
        server_ = HttpWebConnectionTest.startWebServer("src/test/resources/gwt/" + getDirectory() + "/I18N");
        final WebClient client = getWebClient();
        final String url = "http://localhost:" + HttpWebConnectionTest.PORT + "/I18N.html?locale=fr";
        final HtmlPage page = client.getPage(url);
        page.getEnclosingWindow().getThreadManager().joinAll(10000);
        i18n(page, "numberFormatOutputText", "31 415 926 535,898");
        String timeZone = new SimpleDateFormat("Z").format(new SimpleDateFormat("d MMMMMMMM yyyy").parse("13 September 1999"));
        timeZone = timeZone.substring(0, 3) + ':' + timeZone.substring(3);
        i18n(page, "dateTimeFormatOutputText", "lundi 13 septembre 1999 00:00:00 GMT" + timeZone);
        i18n(page, "messagesFormattedOutputText", "L'utilisateur 'amelie' a un niveau de securité 'guest', " + "et ne peut accéder à '/secure/blueprints.xml'");
        i18n(page, "constantsFirstNameText", "Amelie");
        i18n(page, "constantsLastNameText", "Crutcher");
        i18n(page, "constantsFavoriteColorList", new String[] { "Rouge", "Blanc", "Jaune", "Noir", "Bleu", "Vert", "Gris", "Gris clair" });
        i18n(page, "constantsWithLookupResultsText", "Rouge");
        final Map<String, String> map = new HashMap<String, String>();
        map.put("name", "Amelie Crutcher");
        map.put("timeZone", "EST");
        map.put("userID", "123");
        map.put("lastLogOn", "2/2/2006");
        i18nDictionary(page, map);
        Locale.setDefault(locale);
    }

    /**
     * Test value inside {@link HtmlDivision} or {@link HtmlInput}
     * @param page the page to load
     * @param id id of the element to search for
     * @param expectedValue expected value of the value inside the element
     * @throws Exception if the test fails
     */
    private void i18n(final HtmlPage page, final String id, final String expectedValue) {
        final HtmlTableDataCell cell = (HtmlTableDataCell) page.getHtmlElementById(id);
        tableDataCell(cell, expectedValue);
    }

    /**
     * Test value of {@link HtmlSelect}
     *
     * @param page the page to load
     * @param id id of the element to search for
     * @param expectedValues expected value of the value inside the select
     * @throws Exception if the test fails
     */
    private void i18n(final HtmlPage page, final String id, final String[] expectedValues) {
        final HtmlTableDataCell cell = (HtmlTableDataCell) page.getHtmlElementById(id);
        final Object child = cell.getFirstChild();
        if (child instanceof HtmlSelect) {
            final HtmlSelect select = (HtmlSelect) child;
            assertEquals(expectedValues.length, select.getOptionSize());
            for (int i = 0; i < expectedValues.length; i++) {
                assertEquals(expectedValues[i], select.getOption(i).getValueAttribute());
            }
        } else {
            fail("Could not find '" + expectedValues + "'");
        }
    }

    private void i18nDictionary(final HtmlPage page, final Map<String, String> expectedMap) throws Exception {
        final HtmlTableRow headerRow = (HtmlTableRow) page.getFirstByXPath("//*[@class='i18n-dictionary-header-row']");
        final HtmlTableRow valueRow = (HtmlTableRow) headerRow.getNextSibling();
        DomNode headerNode = headerRow.getFirstChild();
        DomNode valueNode = valueRow.getFirstChild();
        final Set<String> foundHeaders = new HashSet<String>();
        for (int i = 0; i < expectedMap.size(); i++) {
            final String header = headerNode.getFirstChild().getNodeValue();
            final String value = valueNode.getFirstChild().getNodeValue();
            assertNotNull(expectedMap.get(header));
            assertEquals(expectedMap.get(header), value);
            foundHeaders.add(header);
            valueNode = valueNode.getNextSibling();
            headerNode = headerNode.getNextSibling();
        }
        assertEquals(expectedMap.size(), foundHeaders.size());
    }

    /**
     * Test value inside {@link HtmlDivision}, {@link HtmlInput} or {@link DomText}
     *
     * @param cell the cells to search in
     * @param expectedValue expected value of the value inside the cell
     * @throws Exception if the test fails
     */
    private void tableDataCell(final HtmlTableDataCell cell, final String expectedValue) {
        final Object child = cell.getFirstChild();
        if (child instanceof HtmlDivision) {
            final HtmlDivision div = (HtmlDivision) child;
            DomNode firstChild = div.getFirstChild();
            if (firstChild instanceof HtmlUnknownElement && (firstChild.getNodeName().equals("b") || firstChild.getNodeName().equals("i"))) {
                firstChild = firstChild.getFirstChild();
            }
            if (firstChild instanceof DomText) {
                final DomText text = (DomText) firstChild;
                assertEquals(expectedValue, text.getData());
            } else {
                fail("Could not find '" + expectedValue + "'");
            }
        } else if (child instanceof HtmlInput) {
            final HtmlInput input = (HtmlInput) child;
            assertEquals(expectedValue, input.getValueAttribute());
        } else if (child instanceof DomText) {
            final DomText text = (DomText) child;
            assertEquals(expectedValue, text.getData());
        } else {
            fail("Could not find '" + expectedValue + "'");
        }
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    public void simpleXML() throws Exception {
        final HtmlPage page = loadGWTPage("SimpleXML", null);
        final String[] pendingOrders = { "123-2", "3 45122 34566", "2/2/2004", "43 Butcher lane", "Atlanta", "Georgia", "30366" };
        for (int i = 0; i < 20; i++) {
            if (page.getByXPath("//table[@class='userTable'][1]//tr[2]/td").size() == pendingOrders.length) {
                break;
            }
            synchronized (page) {
                page.wait(500);
            }
        }
        final List<?> cells = page.getByXPath("//table[@class='userTable'][1]//tr[2]/td");
        assertEquals(pendingOrders.length, cells.size());
        for (int i = 0; i < pendingOrders.length; i++) {
            final HtmlTableDataCell cell = (HtmlTableDataCell) cells.get(i);
            tableDataCell(cell, pendingOrders[i]);
        }
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    public void mail() throws Exception {
        final HtmlPage page = loadGWTPage("Mail", null);
        final HtmlTableDataCell cell = (HtmlTableDataCell) page.getFirstByXPath("//table[@class='mail-TopPanel']//div[@class='gwt-HTML']//..");
        tableDataCell(cell, "Welcome back, foo@example.com");
        final String[] selectedRow = { "markboland05", "mark@example.com", "URGENT -[Mon, 24 Apr 2006 02:17:27 +0000]" };
        final List<?> selectedRowCells = page.getByXPath("//tr[@class='mail-SelectedRow']/td");
        assertEquals(selectedRow.length, selectedRowCells.size());
        for (int i = 0; i < selectedRow.length; i++) {
            final HtmlTableDataCell selectedRowCell = (HtmlTableDataCell) selectedRowCells.get(i);
            tableDataCell(selectedRowCell, selectedRow[i]);
        }
        final List<?> detailsCells = page.getByXPath("//div[@class='mail-DetailBody']/text()");
        final String[] details = { "Dear Friend,", "I am Mr. Mark Boland the Bank Manager of ABN AMRO BANK 101 Moorgate, London, EC2M 6SB." };
        for (int i = 0; i < details.length; i++) {
            final DomText text = (DomText) detailsCells.get(i);
            assertEquals(details[i], text.getData());
        }
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    public void json() throws Exception {
        final HtmlPage page = loadGWTPage("JSON", null);
        final HtmlButton button = (HtmlButton) page.getFirstByXPath("//button");
        button.click();
        for (int i = 0; i < 20; i++) {
            if (page.getFirstByXPath("//div[@class='JSON-JSONResponseObject']") != null) {
                break;
            }
            synchronized (page) {
                page.wait(500);
            }
        }
        final HtmlSpan span = (HtmlSpan) page.getFirstByXPath("//div[@class='JSON-JSONResponseObject']/div/div/table//td[2]/span/span");
        assertEquals("ResultSet", span.getFirstChild().getNodeValue());
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    public void dynaTable_1_5() throws Exception {
        server_ = HttpWebConnectionTest.startWebServer("src/test/resources/gwt/" + getDirectory() + "/DynaTable", new String[] { "src/test/resources/gwt/" + getDirectory() + "/gwt-servlet.jar" });
        final WebClient client = getWebClient();
        final String url = "http://localhost:" + HttpWebConnectionTest.PORT + "/DynaTable.html";
        final HtmlPage page = client.getPage(url);
        final String[] firstRow = { "Inman Mendez", "Majoring in Phrenology", "Mon 9:45-10:35, Tues 2:15-3:05, Fri 8:45-9:35, Fri 9:45-10:35" };
        for (int i = 0; i < 40; i++) {
            final List<?> detailsCells = page.getByXPath("//table[@class='table']//tr[2]/td");
            if (detailsCells.size() == firstRow.length) {
                final HtmlTableDataCell firstCell = (HtmlTableDataCell) detailsCells.get(0);
                if (firstCell.getFirstChild().getNodeValue().equals(firstRow[0])) {
                    break;
                }
            }
            synchronized (page) {
                page.wait(500);
            }
        }
        final List<?> detailsCells = page.getByXPath("//table[@class='table']//tr[2]/td");
        assertEquals(firstRow.length, detailsCells.size());
        for (int i = 0; i < firstRow.length; i++) {
            final HtmlTableDataCell cell = (HtmlTableDataCell) detailsCells.get(i);
            tableDataCell(cell, firstRow[i]);
        }
    }

    /**
     * Returns the GWT directory being tested.
     * @return the GWT directory being tested
     */
    protected String getDirectory() {
        return "1.5.2";
    }

    /**
     * Loads the GWT unit test index page using the specified test name.
     *
     * @param testName the test name
     * @param collectedAlerts the List to collect alerts into
     * @throws Exception if an error occurs
     * @return the loaded page
     */
    protected HtmlPage loadGWTPage(final String testName, final List<String> collectedAlerts) throws Exception {
        final String resource = "gwt/" + getDirectory() + "/" + testName + "/" + testName + ".html";
        final URL url = getClass().getClassLoader().getResource(resource);
        assertNotNull(url);
        final WebClient client = getWebClient();
        if (collectedAlerts != null) {
            client.setAlertHandler(new CollectingAlertHandler(collectedAlerts));
        }
        final HtmlPage page = (HtmlPage) client.getPage(url);
        page.getEnclosingWindow().getThreadManager().joinAll(10000);
        return page;
    }

    /**
     * Performs post-test deconstruction.
     * @throws Exception if an error occurs
     */
    @After
    public void after() throws Exception {
        HttpWebConnectionTest.stopWebServer(server_);
        server_ = null;
    }

    /**
     * @throws Exception if an error occurs
     */
    @Test
    public void showcase() throws Exception {
        final List<String> collectedAlerts = new ArrayList<String>();
        final HtmlPage page = loadGWTPage("Showcase", collectedAlerts);
        assertEquals("Monday", page.<HtmlElement>getHtmlElementById("gwt-debug-cwCheckBox-Monday-label").getFirstChild().getNodeValue());
        assertEquals("Tuesday", page.<HtmlElement>getHtmlElementById("gwt-debug-cwCheckBox-Tuesday-label").getFirstChild().getNodeValue());
    }
}
