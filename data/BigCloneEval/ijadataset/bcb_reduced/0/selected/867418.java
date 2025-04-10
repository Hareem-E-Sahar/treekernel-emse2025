package com.wwwc.util.web;

import java.io.*;
import java.net.*;
import java.util.*;
import java.security.Security;
import java.security.Provider;
import javax.net.ssl.*;

public class MyHttpConnection {

    private StringBuffer message;

    private StringBuffer fullStringBuffer;

    private Vector fullLineVector;

    private static Vector vInput = new Vector(10);

    private static Vector vOut = new Vector(10);

    public StringBuffer getMessage() {
        return message;
    }

    public StringBuffer getFullReturnStringBuffer() {
        return fullStringBuffer;
    }

    public Vector getReturnLineVector() {
        fullLineVector = new Vector();
        String temp = fullStringBuffer.toString();
        temp = temp.replaceAll("\n", "");
        temp = temp.replaceAll("\t", "");
        temp = temp.replaceAll("&nbsp;", " ");
        temp = temp.replaceAll(">", ">\n");
        temp = temp.replaceAll("</", "\n</");
        String element = null;
        StringTokenizer tokens = new StringTokenizer(temp, "\n");
        while (tokens.hasMoreTokens()) {
            element = (tokens.nextToken()).trim();
            if (element != null && element.length() > 0) {
                fullLineVector.addElement(element);
            }
        }
        return fullLineVector;
    }

    public boolean connectToUrl(String url_address) {
        message = new StringBuffer("");
        try {
            URL url = new URL(url_address);
            try {
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                httpConnection.setDoOutput(false);
                httpConnection.connect();
                message.append("<BR>\n Connection Code:[" + httpConnection.getResponseCode() + "]");
                message.append("<BR>\n Response Message:[" + httpConnection.getResponseMessage() + "]");
                InputStreamReader insr = new InputStreamReader(httpConnection.getInputStream());
                BufferedReader in = new BufferedReader(insr);
                fullStringBuffer = new StringBuffer("");
                String temp = in.readLine();
                while (temp != null) {
                    fullStringBuffer.append(temp + "\n");
                    temp = in.readLine();
                }
                in.close();
            } catch (IOException e) {
                message.append("<BR>\n [Error][IOException][" + e.getMessage() + "]");
                return false;
            }
        } catch (MalformedURLException e) {
            message.append("<BR>\n [Error][MalformedURLException][" + e.getMessage() + "]");
            return false;
        } catch (Exception e) {
            message.append("<BR>\n [Error][Exception][" + e.getMessage() + "]");
            return false;
        }
        return true;
    }
}
