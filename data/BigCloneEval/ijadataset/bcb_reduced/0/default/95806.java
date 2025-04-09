import java.io.File;
import java.util.StringTokenizer;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

/**
 * An exception thrown when invalid syntax is encountered.
 * 
 * @author Hadi Abu Rislan
 * @author Kevin Azzam
 * @author Ramsey Nasser
 */
class HTTPParserException extends Exception {

    public HTTPParserException() {
        super();
    }

    public HTTPParserException(String message) {
        super(message);
    }
}

/**
 * The URL parser class. Uses parse() to parse the URL to check whether it is a
 * valid URL or not.
 * 
 * Reference on the Java XML API from
 * http://www.cafeconleche.org/books/xmljava/chapters/
 * 
 * @author Hadi Abu Rislan
 * @author Kevin Azzam
 * @author Ramsey Nasser
 */
public class HTTPParser {

    private String url;

    private int cursor;

    private Document xmlDoc;

    private Element element;

    /**
	 * Create a new HTTPParser with a URL to parse.
	 * 
	 * @param url
	 *            The URL to parse.
	 */
    public HTTPParser(String url) {
        try {
            this.url = url;
            this.xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            this.element = xmlDoc.createElement("root");
            this.element.setAttribute("LBL", "url");
            this.element.setAttribute("valid", "no");
            this.element.setAttribute("LNK", this.url);
            this.xmlDoc.appendChild(this.element);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
	 * Parses the URL.
	 */
    public void parse() throws HTTPParserException {
        protocol();
        this.xmlDoc.getDocumentElement().setAttribute("valid", "yes");
    }

    /**
	 * Appends a non-terminal node to the xml.
	 */
    private void syntaxTreeAppendNonterm(String label) {
        xmlElementAppend(label, "node");
    }

    /**
	 * Appends a terminal symbol to the xml.
	 */
    private void syntaxTreeAppendTerm(String label) {
        if (this.element.hasChildNodes() && ((Element) this.element.getLastChild()).getAttribute("TYPE").equals("lit")) {
            String oldLabel = ((Element) this.element.getLastChild()).getAttribute("LBL");
            ((Element) this.element.getLastChild()).setAttribute("LBL", oldLabel + label);
        } else {
            xmlElementAppend(label, "lit");
            xmlElementBackup();
        }
    }

    /**
	 * Backs up the xml document.
	 */
    private void syntaxTreeBackup() {
        xmlElementBackup();
    }

    /**
	 * Appends an xml element.
	 * 
	 * @param label
	 *            The text that will appear on the node or the literal.
	 * @param type
	 *            The type of the element: a node or a literal.
	 */
    private void xmlElementAppend(String label, String type) {
        Element newNode = this.xmlDoc.createElement("token");
        newNode.setAttribute("LBL", label);
        newNode.setAttribute("TYPE", type);
        this.element.appendChild(newNode);
        this.element = newNode;
    }

    /**
	 * Backs up specific elemtes in the xml document.
	 */
    private void xmlElementBackup() {
        this.element = (Element) this.element.getParentNode();
    }

    /**
	 * This gives the current xml data.
	 * 
	 * @return The current xml document.
	 */
    public Document getSyntaxTreeAsXML() {
        return this.xmlDoc;
    }

    /**
	 * Saves the xml data in an xml file.
	 * 
	 * @param filename
	 *            The name of the xml file to be written.
	 * @return The current xml document.
	 */
    public void dumpSyntaxTreeToFile(String filename) {
        DOMSource domSource = new DOMSource(xmlDoc);
        StreamResult streamResult = new StreamResult(new File(filename));
        try {
            Transformer serializer = TransformerFactory.newInstance().newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.transform(domSource, streamResult);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    /**
	 * Checks the protocol to see if it is http or ftp or invalid.
	 */
    private void protocol() throws HTTPParserException {
        if (url.length() - cursor < 7) {
            quit("URL is too small and/or incomplete!");
        }
        if (url.substring(cursor, cursor + 7).equalsIgnoreCase("http://")) {
            cursor += 7;
            syntaxTreeAppendTerm("http://");
            http_url();
        } else if (url.substring(cursor, cursor + 6).equalsIgnoreCase("ftp://")) {
            cursor += 6;
            syntaxTreeAppendTerm("ftp://");
            ftp_url();
        } else {
            quit("Unrecognized protocol. Allowed protocols are http:// or ftp://");
        }
    }

    /**
	 * Continues execution for the http rules
	 */
    private void http_url() throws HTTPParserException {
        syntaxTreeAppendNonterm("http-url");
        hostport();
        if (url.indexOf('/', cursor) != -1) {
            syntaxTreeAppendTerm("/");
            cursor += url.substring(cursor).indexOf('/') + 1;
            int QuestionMark = url.indexOf('?');
            if (QuestionMark != -1) {
                hpath(url.substring(cursor, QuestionMark));
                syntaxTreeAppendTerm("?");
                search(url.substring(QuestionMark + 1));
            } else {
                if (url.substring(cursor).endsWith("/")) {
                    hpath(url.substring(cursor, url.length() - 1));
                } else {
                    hpath(url.substring(cursor));
                }
            }
        }
        syntaxTreeBackup();
    }

    /**
	 * Continues execution for the http rules.
	 */
    private void hpath(String hpath) throws HTTPParserException {
        syntaxTreeAppendNonterm("hpath");
        int hpathCursor = 0;
        while (true) {
            int nextSlash;
            nextSlash = hpath.substring(hpathCursor).indexOf('/');
            if (nextSlash != -1) {
                hsegment(hpath.substring(hpathCursor, hpathCursor + nextSlash));
                syntaxTreeAppendTerm("/");
            } else {
                hsegment(hpath.substring(hpathCursor));
                break;
            }
            hpathCursor += hpath.substring(hpathCursor).indexOf('/') + 1;
        }
        syntaxTreeBackup();
    }

    /**
	 * Current element is an hsegment.
	 * 
	 * @param hsegment
	 *            the current segment.
	 */
    private void hsegment(String hsegment) throws HTTPParserException {
        syntaxTreeAppendNonterm("hsegment");
        String buffer = "";
        for (int i = 0; i < hsegment.length(); i++) {
            switch(hsegment.charAt(i)) {
                case ';':
                case ':':
                case '@':
                case '&':
                case '=':
                    uchar(buffer);
                    syntaxTreeAppendTerm(String.valueOf(hsegment.charAt(i)));
                    buffer = "";
                    break;
                default:
                    buffer += hsegment.charAt(i);
            }
        }
        if (!buffer.isEmpty()) {
            uchar(buffer);
        }
        syntaxTreeBackup();
    }

    /**
	 * Current element is a queryString.
	 * 
	 * @param hsegment
	 *            the current queryString.
	 */
    private void search(String queryString) throws HTTPParserException {
        syntaxTreeAppendNonterm("search");
        String buffer = "";
        for (int i = 0; i < queryString.length(); i++) {
            switch(queryString.charAt(i)) {
                case ';':
                case ':':
                case '@':
                case '&':
                case '=':
                    uchar(buffer);
                    syntaxTreeAppendTerm(String.valueOf(queryString.charAt(i)));
                    buffer = "";
                    break;
                default:
                    buffer += queryString.charAt(i);
            }
        }
        if (!buffer.isEmpty()) {
            uchar(buffer);
        }
        syntaxTreeBackup();
    }

    /**
	 * Current element is a hostport.
	 */
    private void hostport() throws HTTPParserException {
        syntaxTreeAppendNonterm("hostport");
        if (url.indexOf(':', cursor) != -1) {
            host(cursor, url.indexOf(':', cursor));
            syntaxTreeAppendTerm(":");
            port(url.indexOf(':', cursor) + 1, url.indexOf('/', cursor));
        } else {
            host(cursor, url.indexOf('/', cursor));
        }
        syntaxTreeBackup();
    }

    /**
	 * Current element is a host. Checks whether it is an IP or hostname
	 * 
	 * @param start
	 *            the start position of the substring host.
	 * @param end
	 *            the end position of the substring host.
	 */
    private void host(int start, int end) throws HTTPParserException {
        syntaxTreeAppendNonterm("host");
        String host;
        if (end < 0) {
            host = url.substring(start);
        } else {
            host = url.substring(start, end);
        }
        if (host.isEmpty()) {
            quit("Invalid hostname. Cannot be empty. ");
        }
        if (host.startsWith(".") || host.endsWith(".")) {
            quit("Invalid hostname. Cannot start or end with \".\"");
        }
        if (host.indexOf("..") != -1) {
            quit("Invalid hostname. Cannot contain \"..\"");
        }
        boolean isIP = true;
        int sectionCounter = 0;
        StringTokenizer IPSection = new StringTokenizer(host, ".");
        String token;
        while (IPSection.hasMoreTokens()) {
            sectionCounter++;
            token = IPSection.nextToken();
            if (token.length() > 3) {
                isIP = false;
                break;
            }
            if (!isDigit(token)) {
                isIP = false;
                break;
            }
            if (sectionCounter > 4) {
                isIP = false;
                break;
            }
        }
        if (sectionCounter != 4) {
            isIP = false;
        }
        if (isIP) {
            hostnumber(host);
        } else {
            hostname(host);
        }
        syntaxTreeBackup();
    }

    /**
	 * Host is a domain name.
	 * 
	 * @param s
	 *            the hostname.
	 */
    private void hostname(String s) throws HTTPParserException {
        syntaxTreeAppendNonterm("hostname");
        String token;
        StringTokenizer hostnameTokenizer = new StringTokenizer(s, ".");
        while (hostnameTokenizer.hasMoreTokens()) {
            token = hostnameTokenizer.nextToken();
            if (hostnameTokenizer.hasMoreTokens()) {
                domainlabel(token);
                syntaxTreeAppendTerm(".");
            } else {
                if (token.indexOf(';') != -1) {
                    toplabel(token.substring(0, token.indexOf(';')));
                } else {
                    toplabel(token);
                }
            }
        }
        syntaxTreeBackup();
    }

    /**
	 * Host is an IP.
	 * 
	 * @param s
	 *            the ip.
	 */
    private void hostnumber(String s) throws HTTPParserException {
        syntaxTreeAppendNonterm("hostnumber");
        int hostNumSize = 0;
        String token;
        StringTokenizer hostnumTokenizer = new StringTokenizer(s, ".");
        while (hostnumTokenizer.hasMoreTokens()) {
            hostNumSize++;
            token = hostnumTokenizer.nextToken();
            digits(token);
            if (hostnumTokenizer.hasMoreTokens()) {
                syntaxTreeAppendTerm(".");
            }
        }
        if (hostNumSize != 4) {
            quit("Invalid host number. Expecting 4 sets of integers, got " + hostNumSize);
        }
        syntaxTreeBackup();
    }

    /**
	 * Host is an IP. This parses the port number
	 * 
	 * @param start
	 *            The start of the substring.
	 * @param end
	 *            The end of the substring.
	 */
    private void port(int start, int end) throws HTTPParserException {
        syntaxTreeAppendNonterm("port");
        String port;
        if (end == -1) {
            port = url.substring(start);
        } else {
            port = url.substring(start, end);
        }
        digits(port);
        syntaxTreeBackup();
    }

    /**
	 * This parses the domain label of the domainname.
	 * 
	 * @param s
	 *            The domain.
	 */
    private void domainlabel(String s) throws HTTPParserException {
        syntaxTreeAppendNonterm("domainlabel");
        if (s.charAt(0) == '-') {
            quit("Invalid domain label. Cannot start with '-'");
        }
        if (s.charAt(s.length() - 1) == '-') {
            quit("Invalid domain label. Cannot end with '-'");
        }
        String token;
        StringTokenizer alphadigits = new StringTokenizer(s, "-");
        while (alphadigits.hasMoreTokens()) {
            token = alphadigits.nextToken();
            alphadigit(token);
            if (alphadigits.hasMoreElements()) {
                syntaxTreeAppendTerm("-");
            }
        }
        syntaxTreeBackup();
    }

    /**
	 * This parses the top label of the domainname.
	 * 
	 * @param s
	 *            The domain.
	 */
    private void toplabel(String s) throws HTTPParserException {
        syntaxTreeAppendNonterm("toplabel");
        if (!isAlpha(s.charAt(0))) {
            quit("Invalid toplabel. Must start with a letter, not " + s.charAt(0));
        }
        if (s.charAt(s.length() - 1) == '-') {
            quit("Invalid toplabel. Cannot end with '-'");
        }
        String token;
        StringTokenizer alphadigits = new StringTokenizer(s, "-");
        while (alphadigits.hasMoreTokens()) {
            token = alphadigits.nextToken();
            alphadigit(token);
            if (alphadigits.hasMoreElements()) {
                syntaxTreeAppendTerm("-");
            }
        }
        syntaxTreeBackup();
    }

    /**
	 * Checks if current element is composed of legal alphadigit(s).
	 * 
	 * @param s
	 *            The string to check
	 */
    private void alphadigit(String s) throws HTTPParserException {
        if (isAlphaNumeric(s)) {
            syntaxTreeAppendTerm(s);
        } else {
            quit("Expecting string of letters or numbers. Got " + s);
        }
    }

    /**
	 * Continues execution as an ftp URL.
	 */
    private void ftp_url() throws HTTPParserException {
        syntaxTreeAppendNonterm("ftp-url");
        login();
        if (url.substring(cursor).indexOf('/') != -1) {
            cursor += url.substring(cursor).indexOf('/') + 1;
            int typeLocation = url.indexOf(";type=");
            if (typeLocation != -1) {
                fpath(url.substring(cursor, typeLocation));
                syntaxTreeAppendTerm(";type=");
                ftptype(url.substring(typeLocation + 6));
            } else {
                if (url.substring(cursor).endsWith("/")) {
                    fpath(url.substring(cursor, url.length() - 1));
                } else {
                    fpath(url.substring(cursor));
                }
            }
        }
        syntaxTreeBackup();
    }

    /**
	 * Checks the filepath for validity.
	 * 
	 * @param fpath
	 *            The ftp path.
	 */
    private void fpath(String fpath) throws HTTPParserException {
        syntaxTreeAppendNonterm("fpath");
        int fpathCursor = 0;
        while (true) {
            int nextSlash;
            nextSlash = fpath.substring(fpathCursor).indexOf('/');
            if (nextSlash != -1) {
                fsegment(fpath.substring(fpathCursor, fpathCursor + nextSlash));
                syntaxTreeAppendTerm("/");
            } else {
                fsegment(fpath.substring(fpathCursor));
                break;
            }
            fpathCursor += fpath.substring(fpathCursor).indexOf('/') + 1;
        }
        syntaxTreeBackup();
    }

    /**
	 * Current element is an fsegment.
	 * 
	 * @param fsegment
	 *            The segment being checked.
	 */
    private void fsegment(String fsegment) throws HTTPParserException {
        syntaxTreeAppendNonterm("fsegment");
        for (int i = 0; i < fsegment.length(); i++) {
            if (!isUChar(fsegment.charAt(i))) {
                switch(fsegment.charAt(i)) {
                    case '?':
                    case ':':
                    case '@':
                    case '&':
                    case '=':
                        syntaxTreeAppendTerm(String.valueOf(fsegment.charAt(i)));
                        break;
                    default:
                        quit("Expecting '?', ':', '@', '&', '=' or unreserved character, got '" + fsegment.charAt(i));
                }
            } else {
                syntaxTreeAppendTerm(String.valueOf(fsegment.charAt(i)));
            }
        }
        syntaxTreeBackup();
    }

    /**
	 * Checks the ftp type.
	 * 
	 * @param typeString
	 *            Ftp input.
	 */
    private void ftptype(String typeString) throws HTTPParserException {
        syntaxTreeAppendNonterm("ftptype");
        for (int i = 0; i < typeString.length(); i++) {
            switch(typeString.charAt(i)) {
                case 'A':
                case 'a':
                case 'I':
                case 'i':
                case 'D':
                case 'd':
                    syntaxTreeAppendTerm(String.valueOf(typeString.charAt(i)));
                    break;
                default:
                    quit("Expecting 'A', 'a', 'I', 'i', 'D' or 'd', got '" + typeString.charAt(i));
            }
        }
        syntaxTreeBackup();
    }

    /**
	 * Current element is a login.
	 */
    private void login() throws HTTPParserException {
        syntaxTreeAppendNonterm("login");
        if (url.indexOf('@') != -1) {
            user();
            if (url.indexOf(':', cursor) > 0) {
                syntaxTreeAppendTerm(":");
                password();
            }
            syntaxTreeAppendTerm("@");
            cursor = url.indexOf('@') + 1;
        }
        hostport();
        syntaxTreeBackup();
    }

    /**
	 * Current element is a user.
	 */
    private void user() throws HTTPParserException {
        syntaxTreeAppendNonterm("user");
        String user;
        if (url.indexOf(':', cursor) > 0) {
            user = url.substring(cursor, url.indexOf(':', cursor));
        } else {
            user = url.substring(cursor, url.indexOf('@'));
        }
        String buffer = "";
        for (int i = 0; i < user.length(); i++) {
            switch(user.charAt(i)) {
                case ';':
                case '?':
                case '&':
                case '=':
                    uchar(buffer);
                    syntaxTreeAppendTerm(String.valueOf(user.charAt(i)));
                    buffer = "";
                    break;
                default:
                    buffer += user.charAt(i);
            }
        }
        if (!buffer.isEmpty()) {
            uchar(buffer);
        }
        syntaxTreeBackup();
    }

    /**
	 * Current element is a password.
	 */
    private void password() throws HTTPParserException {
        syntaxTreeAppendNonterm("password");
        String password = url.substring(url.indexOf(':', cursor) + 1, url.indexOf('@'));
        String buffer = "";
        for (int i = 0; i < password.length(); i++) {
            switch(password.charAt(i)) {
                case ';':
                case '?':
                case '&':
                case '=':
                    uchar(buffer);
                    syntaxTreeAppendTerm(String.valueOf(password.charAt(i)));
                    buffer = "";
                    break;
                default:
                    buffer += password.charAt(i);
            }
        }
        if (!buffer.isEmpty()) {
            uchar(buffer);
        }
        syntaxTreeBackup();
    }

    /**
	 * Bail out of parsing invalid syntax.
	 */
    private boolean quit() throws HTTPParserException {
        String fullReason = "In token (" + element.getAttribute("LBL") + ") : Reason Unknown";
        xmlDoc.getDocumentElement().setAttribute("failed", fullReason);
        throw new HTTPParserException();
    }

    /**
	 * Bail out of parsing invalid syntax for a reason.
	 */
    private boolean quit(String reason) throws HTTPParserException {
        String fullReason = "In token (" + element.getAttribute("LBL") + ") : " + reason;
        xmlDoc.getDocumentElement().setAttribute("failed", fullReason);
        throw new HTTPParserException(fullReason);
    }

    /**
	 * Checks if the string is composed of digits.
	 * 
	 * @param s
	 *            the sting to be checked.
	 */
    private void digits(String s) throws HTTPParserException {
        if (isDigit(s)) {
            syntaxTreeAppendTerm(s);
        } else {
            quit("Expecting digits, got '" + s + "'");
        }
    }

    /**
	 * Checks if the string is composed of characters.
	 * 
	 * @param s
	 *            the sting to be checked.
	 */
    private void uchar(String s) throws HTTPParserException {
        StringTokenizer ucharTokenizer = new StringTokenizer(s, "%");
        String token = ucharTokenizer.nextToken();
        if (isEscape(token)) {
            escape(token);
        } else {
            unreserved(token);
        }
        while (ucharTokenizer.hasMoreTokens()) {
            token = ucharTokenizer.nextToken();
            escape("%" + token.substring(0, 2));
            unreserved(token.substring(2));
        }
    }

    /**
	 * Checks if the string is a reserved word.
	 * 
	 * @param s
	 *            the sting to be checked.
	 */
    private void unreserved(String s) throws HTTPParserException {
        if (isUnreserved(s)) {
            syntaxTreeAppendTerm(s);
        } else {
            quit("String contains reserved characters '" + s + "'");
        }
    }

    /**
	 * Checks if the string is an escape character.
	 * 
	 * @param s
	 *            the sting to be checked.
	 */
    private void escape(String s) throws HTTPParserException {
        if (isEscape(s)) {
            syntaxTreeAppendTerm("%");
            hex(s.charAt(1) + "" + s.charAt(2));
        } else {
            quit("Invalid character in escape code '" + s + "'");
        }
    }

    /**
	 * Checks if the string is a hex code.
	 * 
	 * @param s
	 *            the sting to be checked.
	 */
    private void hex(String s) throws HTTPParserException {
        if (isHex(s)) {
            syntaxTreeAppendTerm(s);
        } else {
            quit("Invalid hex code");
        }
    }

    /**
	 * Checks if the character is composed of digits or numbers
	 * 
	 * @param toTest
	 *            the character to be checked.
	 * @return passes the character to two functions to test it.
	 */
    private boolean isAlphaNumeric(char toTest) {
        return isAlpha(toTest) || isDigit(toTest);
    }

    /**
	 * Checks if the string is composed of digits or numbers
	 * 
	 * @param toTest
	 *            the string to be checked.
	 * @return true if it is valid.
	 * @return false if it is invalid.
	 */
    private boolean isAlphaNumeric(String toTest) {
        for (int i = 0; i < toTest.length(); i++) {
            if (!isAlphaNumeric(toTest.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
	 * Checks if the string is composed of digits
	 * 
	 * @param toTest
	 *            the string to be checked.
	 * @return true if it is valid.
	 * @return false if it is invalid.
	 */
    private boolean isDigit(String toTest) {
        for (int i = 0; i < toTest.length(); i++) {
            if (!isDigit(toTest.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
	 * Checks if the string is composed unreserved
	 * 
	 * @param toTest
	 *            the string to be checked.
	 * @return true if it is valid.
	 * @return false if it is invalid.
	 */
    private boolean isUnreserved(String toTest) {
        for (int i = 0; i < toTest.length(); i++) {
            if (!isUnreserved(toTest.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
	 * Checks if the string is a hex code
	 * 
	 * @param toTest
	 *            the string to be checked.
	 * @return true if it is valid.
	 * @return false if it is invalid.
	 */
    private boolean isHex(String toTest) {
        for (int i = 0; i < toTest.length(); i++) {
            if (!isHex(toTest.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
	 * Checks if the caracter is a digit
	 * 
	 * @param toTest
	 *            the caracter to be checked.
	 * @return true if it is valid.
	 * @return false if it is invalid.
	 */
    private boolean isDigit(char toTest) {
        return (toTest >= 48 && toTest < 58);
    }

    /**
	 * Checks if the caracter is a letter
	 * 
	 * @param toTest
	 *            the caracter to be checked.
	 * @return passes it to two other functions to check.
	 */
    private boolean isAlpha(char toTest) {
        return (isLowAlpha(toTest) || isHighAlpha(toTest));
    }

    /**
	 * Checks if the caracter is a lowercase letter
	 * 
	 * @param toTest
	 *            the caracter to be checked.
	 * @return true if it is valid.
	 * @return false if it is invalid.
	 */
    private boolean isLowAlpha(char toTest) {
        return (toTest >= 97 && toTest < 123);
    }

    /**
	 * Checks if the caracter is an uppercase letter
	 * 
	 * @param toTest
	 *            the caracter to be checked.
	 * @return true if it is valid.
	 * @return false if it is invalid.
	 */
    private boolean isHighAlpha(char toTest) {
        return (toTest >= 65 && toTest < 91);
    }

    /**
	 * Checks if the caracter is unreserved
	 * 
	 * @param toTest
	 *            the caracter to be checked.
	 * @return passes character to another function to check
	 */
    private boolean isUChar(char toTest) {
        return isUnreserved(toTest);
    }

    /**
	 * Checks if the caracter is reserved
	 * 
	 * @param toTest
	 *            the caracter to be checked.
	 * @return passes character to other functions to check
	 */
    private boolean isUnreserved(char toTest) {
        return isAlpha(toTest) || isDigit(toTest) || isSafe(toTest);
    }

    /**
	 * Checks if the caracter is safe
	 * 
	 * @param toTest
	 *            the caracter to be checked.
	 * @return passes character to other functions to check
	 */
    private boolean isSafe(char toTest) {
        return toTest == '$' || toTest == '-' || toTest == '_' || toTest == '.' || toTest == '+';
    }

    /**
	 * Checks if the caracter is extra
	 * 
	 * @param toTest
	 *            the caracter to be checked.
	 * @return passes character to other functions to check
	 */
    private boolean isExtra(char toTest) {
        return toTest == '!' || toTest == '*' || toTest == '\'' || toTest == '(' || toTest == ')' || toTest == ',';
    }

    /**
	 * Checks if the caracter is an escape char
	 * 
	 * @param toTest
	 *            the caracter to be checked.
	 * @return passes character to other functions to check
	 */
    private boolean isEscape(String toTest) {
        return toTest.charAt(0) == '%' && isHex(toTest.charAt(1)) && isHex(toTest.charAt(2));
    }

    /**
	 * Checks if the caracter is a hex
	 * 
	 * @param toTest
	 *            the caracter to be checked.
	 * @return passes character to other functions to check
	 */
    private boolean isHex(char toTest) {
        return isDigit(toTest) || toTest == 'A' || toTest == 'B' || toTest == 'C' || toTest == 'D' || toTest == 'E' || toTest == 'F' || toTest == 'a' || toTest == 'b' || toTest == 'c' || toTest == 'd' || toTest == 'e' || toTest == 'f';
    }
}
