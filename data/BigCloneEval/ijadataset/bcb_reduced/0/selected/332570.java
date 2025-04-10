package org.uilabs.server.soap;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author Bill
 */
public class SOAPServer {

    public static String service = "http://datadirect.webservices.zap2it.com/tvlistings/xtvdService";

    /** Creates a new instance of SOAPServer */
    public SOAPServer() {
    }

    public static void main(String args[]) throws Exception {
        Authenticator.setDefault(new SOAPAuthenticator());
        URL u = new URL(service);
        URLConnection uc = u.openConnection();
        HttpURLConnection connection = (HttpURLConnection) uc;
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        OutputStream out = connection.getOutputStream();
        Writer wout = new OutputStreamWriter(out);
        wout.write("<?xml version='1.0'?>\r\n");
        wout.write("<SOAP-ENV:Envelope ");
        wout.write("xmlns:SOAP-ENV=");
        wout.write("'http://schemas.xmlsoap.org/soap/envelope/' ");
        wout.write("xmlns:xsi=");
        wout.write("'http://www.w3.org/2001/XMLSchema-instance' xmlns:urn=\"urn:TMSWebServices\">\r\n");
        wout.write("<SOAP-ENV:Body>\r\n");
        wout.write("<urn:download soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\r\n");
        wout.write("<startTime xsi:type=\"urn:dateTime\">?</startTime>\r\n");
        wout.write("<endTime xsi:type=\"urn:dateTime\">?</endTime>\r\n");
        wout.write("</urn:download>\r\n");
        wout.write("</SOAP-ENV:Body>\r\n");
        wout.write("</SOAP-ENV:Envelope>\r\n");
        wout.flush();
        wout.close();
        InputStream in = connection.getInputStream();
        int c;
        while ((c = in.read()) != -1) System.out.write(c);
        in.close();
    }

    static class SOAPAuthenticator extends Authenticator {

        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication("wsnyder6", "lisas1".toCharArray());
        }
    }
}
