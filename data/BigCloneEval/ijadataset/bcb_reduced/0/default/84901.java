import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Serializes and deserializes G8R protocol responses from a server.
 */
public class G8RResponse extends G8RMessage {

    public static final String OK = "OK";

    public static final String ERROR = "ERROR";

    private String status;

    private String message;

    /**
     * Deserializes the G8RResponse and stores response and cookies in fields.
     * @param source - stream of bytes representing serialized message
     */
    public G8RResponse(InputStream source) throws G8RException, EOFException {
        G8RMessageReader reader = new G8RMessageReader(source);
        String buffer;
        Scanner firstLine = reader.readLine();
        if (firstLine == null) {
            throw new G8RException(G8RErrors.EMPTY);
        }
        G8RProtocolUtility.checkVersion(firstLine);
        buffer = firstLine.next();
        if (buffer.equals(OK)) {
            status = buffer;
        } else if (buffer.equals(ERROR)) {
            status = buffer;
        } else {
            throw new G8RException(G8RErrors.STATUS);
        }
        setFunction(firstLine.next());
        G8RProtocolUtility.validateToken(getFunction());
        try {
            firstLine.skip(" ");
        } catch (NoSuchElementException e) {
            throw new G8RException(G8RErrors.SPACE);
        }
        firstLine.useDelimiter("\r\n");
        if (firstLine.hasNext("\\p{Print}+")) {
            message = firstLine.next();
        } else {
            message = "";
        }
        firstLine.close();
        setCookies(new CookieList(reader));
    }

    /**
     * Constructor that allows every field to be set
     * @param theStatus
     * @param theFunction
     * @param theMessage
     * @param theCookies
     */
    public G8RResponse(String theStatus, String theFunction, String theMessage, CookieList theCookies) throws G8RException {
        status = theStatus;
        setFunction(theFunction);
        message = theMessage;
        setCookies(theCookies);
    }

    /**
     * Serializes the G8RResponse into a sequence of bytes.
     * @param destination - where we write the bytes to
     */
    public void encode(OutputStream destination) throws G8RException {
        try {
            G8RProtocolUtility.writeG8RVersion(destination);
            destination.write(G8RProtocolUtility.stringToASCII(" " + status + " " + getFunction() + " " + message + NEWLINE));
            getCookies().encode(destination);
            destination.write(G8RProtocolUtility.stringToASCII(NEWLINE));
        } catch (UnsupportedEncodingException e) {
            throw new G8RException(G8RErrors.ASCII, e);
        } catch (IOException e) {
            throw new G8RException(e);
        }
    }

    /**
     * Accessor method for status field
     * @return status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Accessor method for message field
     * @return message
     */
    public String getMessage() {
        return message;
    }
}
