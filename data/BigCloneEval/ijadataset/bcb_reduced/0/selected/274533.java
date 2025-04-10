package com.sun.org.apache.xml.internal.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import com.sun.org.apache.xml.internal.res.XMLErrorResources;
import com.sun.org.apache.xml.internal.res.XMLMessages;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Sample implementation of similar SAX ErrorHandler and JAXP ErrorListener.  
 *
 * <p>This implementation is suitable for various use cases, and 
 * provides some basic configuration API's as well to control 
 * when we re-throw errors, etc.</p>
 *
 * @author shane_curcuru@us.ibm.com
 * @version $Id: ListingErrorHandler.java,v 1.2.4.1 2005/09/15 08:15:46 suresh_emailid Exp $
 * @xsl.usage general
 */
public class ListingErrorHandler implements ErrorHandler, ErrorListener {

    protected PrintWriter m_pw = null;

    /**
     * Constructor ListingErrorHandler; user-supplied PrintWriter.  
     */
    public ListingErrorHandler(PrintWriter pw) {
        if (null == pw) throw new NullPointerException(XMLMessages.createXMLMessage(XMLErrorResources.ER_ERRORHANDLER_CREATED_WITH_NULL_PRINTWRITER, null));
        m_pw = pw;
    }

    /**
     * Constructor ListingErrorHandler; uses System.err.  
     */
    public ListingErrorHandler() {
        m_pw = new PrintWriter(System.err, true);
    }

    /**
     * Receive notification of a warning.
     *
     * <p>SAX parsers will use this method to report conditions that
     * are not errors or fatal errors as defined by the XML 1.0
     * recommendation.  The default behaviour is to take no action.</p>
     *
     * <p>The SAX parser must continue to provide normal parsing events
     * after invoking this method: it should still be possible for the
     * application to process the document through to the end.</p>
     *
     * <p>Filters may use this method to report other, non-XML warnings
     * as well.</p>
     *
     * @param exception The warning information encapsulated in a
     *                  SAX parse exception.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     * wrapping another exception; only if setThrowOnWarning is true.
     * @see org.xml.sax.SAXParseException 
     */
    public void warning(SAXParseException exception) throws SAXException {
        logExceptionLocation(m_pw, exception);
        m_pw.println("warning: " + exception.getMessage());
        m_pw.flush();
        if (getThrowOnWarning()) throw exception;
    }

    /**
     * Receive notification of a recoverable error.
     *
     * <p>This corresponds to the definition of "error" in section 1.2
     * of the W3C XML 1.0 Recommendation.  For example, a validating
     * parser would use this callback to report the violation of a
     * validity constraint.  The default behaviour is to take no
     * action.</p>
     *
     * <p>The SAX parser must continue to provide normal parsing events
     * after invoking this method: it should still be possible for the
     * application to process the document through to the end.  If the
     * application cannot do so, then the parser should report a fatal
     * error even if the XML 1.0 recommendation does not require it to
     * do so.</p>
     *
     * <p>Filters may use this method to report other, non-XML errors
     * as well.</p>
     *
     * @param exception The error information encapsulated in a
     *                  SAX parse exception.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     * wrapping another exception; only if setThrowOnErroris true.
     * @see org.xml.sax.SAXParseException 
     */
    public void error(SAXParseException exception) throws SAXException {
        logExceptionLocation(m_pw, exception);
        m_pw.println("error: " + exception.getMessage());
        m_pw.flush();
        if (getThrowOnError()) throw exception;
    }

    /**
     * Receive notification of a non-recoverable error.
     *
     * <p>This corresponds to the definition of "fatal error" in
     * section 1.2 of the W3C XML 1.0 Recommendation.  For example, a
     * parser would use this callback to report the violation of a
     * well-formedness constraint.</p>
     *
     * <p>The application must assume that the document is unusable
     * after the parser has invoked this method, and should continue
     * (if at all) only for the sake of collecting addition error
     * messages: in fact, SAX parsers are free to stop reporting any
     * other events once this method has been invoked.</p>
     *
     * @param exception The error information encapsulated in a
     *                  SAX parse exception.  
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     * wrapping another exception; only if setThrowOnFatalError is true.
     * @see org.xml.sax.SAXParseException
     */
    public void fatalError(SAXParseException exception) throws SAXException {
        logExceptionLocation(m_pw, exception);
        m_pw.println("fatalError: " + exception.getMessage());
        m_pw.flush();
        if (getThrowOnFatalError()) throw exception;
    }

    /**
     * Receive notification of a warning.
     *
     * <p>{@link javax.xml.transform.Transformer} can use this method to report
     * conditions that are not errors or fatal errors.  The default behaviour
     * is to take no action.</p>
     *
     * <p>After invoking this method, the Transformer must continue with
     * the transformation. It should still be possible for the
     * application to process the document through to the end.</p>
     *
     * @param exception The warning information encapsulated in a
     *                  transformer exception.
     *
     * @throws javax.xml.transform.TransformerException  only if 
     * setThrowOnWarning is true.
     *
     * @see javax.xml.transform.TransformerException
     */
    public void warning(TransformerException exception) throws TransformerException {
        logExceptionLocation(m_pw, exception);
        m_pw.println("warning: " + exception.getMessage());
        m_pw.flush();
        if (getThrowOnWarning()) throw exception;
    }

    /**
     * Receive notification of a recoverable error.
     *
     * <p>The transformer must continue to try and provide normal transformation
     * after invoking this method.  It should still be possible for the
     * application to process the document through to the end if no other errors
     * are encountered.</p>
     *
     * @param exception The error information encapsulated in a
     *                  transformer exception.
     *
     * @throws javax.xml.transform.TransformerException  only if 
     * setThrowOnError is true.
     *
     * @see javax.xml.transform.TransformerException
     */
    public void error(TransformerException exception) throws TransformerException {
        logExceptionLocation(m_pw, exception);
        m_pw.println("error: " + exception.getMessage());
        m_pw.flush();
        if (getThrowOnError()) throw exception;
    }

    /**
     * Receive notification of a non-recoverable error.
     *
     * <p>The transformer must continue to try and provide normal transformation
     * after invoking this method.  It should still be possible for the
     * application to process the document through to the end if no other errors
     * are encountered, but there is no guarantee that the output will be
     * useable.</p>
     *
     * @param exception The error information encapsulated in a
     *                  transformer exception.
     *
     * @throws javax.xml.transform.TransformerException  only if 
     * setThrowOnError is true.
     *
     * @see javax.xml.transform.TransformerException
     */
    public void fatalError(TransformerException exception) throws TransformerException {
        logExceptionLocation(m_pw, exception);
        m_pw.println("error: " + exception.getMessage());
        m_pw.flush();
        if (getThrowOnError()) throw exception;
    }

    /**
     * Print out location information about the exception.  
     *
     * Cribbed from DefaultErrorHandler.printLocation() 
     * @param pw PrintWriter to send output to
     * @param exception TransformerException or SAXParseException
     * to log information about
     */
    public static void logExceptionLocation(PrintWriter pw, Throwable exception) {
        if (null == pw) pw = new PrintWriter(System.err, true);
        SourceLocator locator = null;
        Throwable cause = exception;
        do {
            if (cause instanceof SAXParseException) {
                locator = new SAXSourceLocator((SAXParseException) cause);
            } else if (cause instanceof TransformerException) {
                SourceLocator causeLocator = ((TransformerException) cause).getLocator();
                if (null != causeLocator) {
                    locator = causeLocator;
                }
            }
            if (cause instanceof TransformerException) cause = ((TransformerException) cause).getCause(); else if (cause instanceof WrappedRuntimeException) cause = ((WrappedRuntimeException) cause).getException(); else if (cause instanceof SAXException) cause = ((SAXException) cause).getException(); else cause = null;
        } while (null != cause);
        if (null != locator) {
            String id = (locator.getPublicId() != locator.getPublicId()) ? locator.getPublicId() : (null != locator.getSystemId()) ? locator.getSystemId() : "SystemId-Unknown";
            pw.print(id + ":Line=" + locator.getLineNumber() + ";Column=" + locator.getColumnNumber() + ": ");
            pw.println("exception:" + exception.getMessage());
            pw.println("root-cause:" + ((null != cause) ? cause.getMessage() : "null"));
            logSourceLine(pw, locator);
        } else {
            pw.print("SystemId-Unknown:locator-unavailable: ");
            pw.println("exception:" + exception.getMessage());
            pw.println("root-cause:" + ((null != cause) ? cause.getMessage() : "null"));
        }
    }

    /**
     * Print out the specific source line that caused the exception, 
     * if possible to load it.  
     *
     * @param pw PrintWriter to send output to
     * @param locator Xalan wrapper for either a JAXP or a SAX 
     * source location object
     */
    public static void logSourceLine(PrintWriter pw, SourceLocator locator) {
        if (null == locator) return;
        if (null == pw) pw = new PrintWriter(System.err, true);
        String url = locator.getSystemId();
        if (null == url) {
            pw.println("line: (No systemId; cannot read file)");
            pw.println();
            return;
        }
        try {
            int line = locator.getLineNumber();
            int column = locator.getColumnNumber();
            pw.println("line: " + getSourceLine(url, line));
            StringBuffer buf = new StringBuffer("line: ");
            for (int i = 1; i < column; i++) {
                buf.append(' ');
            }
            buf.append('^');
            pw.println(buf.toString());
        } catch (Exception e) {
            pw.println("line: logSourceLine unavailable due to: " + e.getMessage());
            pw.println();
        }
    }

    /**
     * Return the specific source line that caused the exception, 
     * if possible to load it; allow exceptions to be thrown.  
     *
     * @author shane_curcuru@us.ibm.com
     */
    protected static String getSourceLine(String sourceUrl, int lineNum) throws Exception {
        URL url = null;
        try {
            url = new URL(sourceUrl);
        } catch (java.net.MalformedURLException mue) {
            int indexOfColon = sourceUrl.indexOf(':');
            int indexOfSlash = sourceUrl.indexOf('/');
            if ((indexOfColon != -1) && (indexOfSlash != -1) && (indexOfColon < indexOfSlash)) {
                throw mue;
            } else {
                url = new URL(SystemIDResolver.getAbsoluteURI(sourceUrl));
            }
        }
        String line = null;
        InputStream is = null;
        BufferedReader br = null;
        try {
            URLConnection uc = url.openConnection();
            is = uc.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            for (int i = 1; i <= lineNum; i++) {
                line = br.readLine();
            }
        } finally {
            br.close();
            is.close();
        }
        return line;
    }

    /**
     * User-settable behavior: when to re-throw exceptions.  
     *
     * <p>This allows per-instance configuration of 
     * ListingErrorHandlers.  You can ask us to either throw 
     * an exception when we're called for various warning / 
     * error / fatalErrors, or simply log them and continue.</p>
     *
     * @param b if we should throw an exception on warnings
     */
    public void setThrowOnWarning(boolean b) {
        throwOnWarning = b;
    }

    /**
     * User-settable behavior: when to re-throw exceptions.  
     *
     * @return if we throw an exception on warnings
     */
    public boolean getThrowOnWarning() {
        return throwOnWarning;
    }

    /** If we should throw exception on warnings; default:false.  */
    protected boolean throwOnWarning = false;

    /**
     * User-settable behavior: when to re-throw exceptions.  
     *
     * <p>This allows per-instance configuration of 
     * ListingErrorHandlers.  You can ask us to either throw 
     * an exception when we're called for various warning / 
     * error / fatalErrors, or simply log them and continue.</p>
     *
     * <p>Note that the behavior of many parsers/transformers 
     * after an error is not necessarily defined!</p>
     *
     * @param b if we should throw an exception on errors
     */
    public void setThrowOnError(boolean b) {
        throwOnError = b;
    }

    /**
     * User-settable behavior: when to re-throw exceptions.  
     *
     * @return if we throw an exception on errors
     */
    public boolean getThrowOnError() {
        return throwOnError;
    }

    /** If we should throw exception on errors; default:true.  */
    protected boolean throwOnError = true;

    /**
     * User-settable behavior: when to re-throw exceptions.  
     *
     * <p>This allows per-instance configuration of 
     * ListingErrorHandlers.  You can ask us to either throw 
     * an exception when we're called for various warning / 
     * error / fatalErrors, or simply log them and continue.</p>
     *
     * <p>Note that the behavior of many parsers/transformers 
     * after a fatalError is not necessarily defined, most 
     * products will probably barf if you continue.</p>
     *
     * @param b if we should throw an exception on fatalErrors
     */
    public void setThrowOnFatalError(boolean b) {
        throwOnFatalError = b;
    }

    /**
     * User-settable behavior: when to re-throw exceptions.  
     *
     * @return if we throw an exception on fatalErrors
     */
    public boolean getThrowOnFatalError() {
        return throwOnFatalError;
    }

    /** If we should throw exception on fatalErrors; default:true.  */
    protected boolean throwOnFatalError = true;
}
