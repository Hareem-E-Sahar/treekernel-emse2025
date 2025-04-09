import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class OutputHandler {

    private Socket sock;

    private BufferedOutputStream out;

    private Writer outStr;

    private Map httpHeaders;

    private boolean sendHeaders = true;

    private boolean sendBody = true;

    public OutputHandler(Socket o_sock) {
        try {
            this.sock = o_sock;
            this.out = new BufferedOutputStream(this.sock.getOutputStream());
            this.outStr = new OutputStreamWriter(this.out);
        } catch (IOException e) {
            Misc.putSysMessage(1, "IO Exception caught: " + e);
        }
    }

    public void setHttpHeaders(Map httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public void sendHeaders(boolean value) {
        this.sendHeaders = value;
    }

    public void sendBody(boolean value) {
        this.sendBody = value;
    }

    public void close() {
        try {
            this.out.close();
            this.outStr.close();
        } catch (IOException e) {
            Misc.putSysMessage(1, "InputHandler: " + e);
        }
    }

    private void outputStatusLine(int code) {
        String statusMessage;
        switch(code) {
            case 100:
                statusMessage = "Continue";
                break;
            case 101:
                statusMessage = "Switching Protocols";
                break;
            case 200:
                statusMessage = "OK";
                break;
            case 201:
                statusMessage = "Created";
                break;
            case 202:
                statusMessage = "Accepted";
                break;
            case 203:
                statusMessage = "Non-Authoritative Information";
                break;
            case 204:
                statusMessage = "No Content";
                break;
            case 205:
                statusMessage = "Reset Content";
                break;
            case 206:
                statusMessage = "Partial Content";
                break;
            case 300:
                statusMessage = "Multiple Choices";
                break;
            case 301:
                statusMessage = "Moved Permanently";
                break;
            case 302:
                statusMessage = "Moved Temporarily";
                break;
            case 303:
                statusMessage = "See Other";
                break;
            case 304:
                statusMessage = "Not Modified";
                break;
            case 305:
                statusMessage = "Use Proxy";
                break;
            case 307:
                statusMessage = "Temporary Redirect";
                break;
            case 400:
                statusMessage = "Bad Request";
                break;
            case 401:
                statusMessage = "Unauthorized";
                break;
            case 402:
                statusMessage = "Payment Required";
                break;
            case 403:
                statusMessage = "Forbidden";
                break;
            case 404:
                statusMessage = "Not Found";
                break;
            case 405:
                statusMessage = "Method Not Allowed";
                break;
            case 406:
                statusMessage = "Not Acceptable";
                break;
            case 407:
                statusMessage = "Proxy Authentication Required";
                break;
            case 408:
                statusMessage = "Request Time-out";
                break;
            case 409:
                statusMessage = "Conflict";
                break;
            case 410:
                statusMessage = "Gone";
                break;
            case 411:
                statusMessage = "Length Required";
                break;
            case 412:
                statusMessage = "Precondition Failed";
                break;
            case 413:
                statusMessage = "Request Entity Too Large";
                break;
            case 414:
                statusMessage = "Request-URI Too Large";
                break;
            case 415:
                statusMessage = "Unsupported Media Type";
                break;
            case 416:
                statusMessage = "Requested range not satisfiable";
                break;
            case 417:
                statusMessage = "Expectation Failed";
                break;
            case 500:
                statusMessage = "Internal Server Error";
                break;
            case 501:
                statusMessage = "Not Implemented";
                break;
            case 502:
                statusMessage = "Bad Gateway";
                break;
            case 503:
                statusMessage = "Service Unavailable";
                break;
            case 504:
                statusMessage = "Gateway Time-out";
                break;
            case 505:
                statusMessage = "HTTP Version not supported";
                break;
            default:
                statusMessage = "An unknown/undefined HTTP code!";
                break;
        }
        this.outputHeader("HTTP/1.1 " + code + " " + statusMessage);
    }

    private void outputConnectionHeader() {
        if (this.httpHeaders.get("connection").toString().startsWith("keep-alive")) this.outputHeader("Connection: keep-alive"); else this.outputHeader("Connection: close");
    }

    private void outputSomething(String someText) {
        try {
            this.outStr.write(someText + pws.crlf());
        } catch (IOException e) {
            Misc.putSysMessage(1, "Error while outputting: " + someText + " (" + e + ")");
        }
    }

    private void outputHeader(String aHeader) {
        if (this.sendHeaders == true) this.outputSomething(aHeader);
    }

    private void outputStdHeaders() {
        SimpleDateFormat temp = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        temp.setTimeZone(new SimpleTimeZone(0, "GMT"));
        String dateStr = temp.format(new Date());
        this.outputHeader("Date: " + dateStr);
        this.outputHeader("Server: " + pws.serverName() + "/" + pws.serverVersion());
    }

    public void outputError(int code, String value) {
        String statusExplanation;
        statusExplanation = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\"><HTML><HEAD><TITLE>Error " + code + "</TITLE></HEAD><BODY>";
        switch(code) {
            case 404:
                statusExplanation += "<H1>Not Found</H1>The requested URL " + value + " was not found on this server.";
                break;
            case 400:
                statusExplanation += "<H1>Bad Request</H1>Your browser sent a request that this server could not understand.<P>Client sent HTTP/1.1 request without hostname (see RFC2068 section 9, and 14.23)";
                break;
            case 501:
                statusExplanation += "<H1>Method Not Implemented</H1>" + value + " not supported.<P>Invalid method in request " + value + ".";
                break;
            case 505:
                statusExplanation += "<H1>HTTP Version Not Supported</H1>Your browser uses an HTTP version this server cannot serve. Please upgrade your browser to a version that uses at least HTTP/1.0.";
                break;
            case 601:
                statusExplanation += "<H1>Protocol Not Implemented</H1>Your browser sent a request using a protocol that is not implemented. <P>Client used protocol " + value + ".";
                break;
        }
        statusExplanation += giveOutputFooter() + "</BODY></HTML>";
        this.outputStatusLine(code);
        this.outputStdHeaders();
        SimpleDateFormat temp = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        temp.setTimeZone(new SimpleTimeZone(0, "GMT"));
        String dateStr = temp.format(new Date());
        this.outputHeader("Last-Modified: " + dateStr);
        this.outputHeader("Content-type: " + Misc.getContentType("error.htm"));
        this.outputHeader("Content-length: " + statusExplanation.length());
        this.outputConnectionHeader();
        this.outputHeader("");
        if (this.sendBody == true) this.outputSomething(statusExplanation);
        this.outputFlush();
    }

    private void outputFlush() {
        try {
            this.out.flush();
            this.outStr.flush();
        } catch (IOException e) {
            Misc.putSysMessage(1, "Error while flushing output buffer: " + e);
        }
    }

    public void ProcessAndOutputFile(File path, String httpMethod, String httpURIVars, InetAddress remoteIP) {
        File fileToRead = path;
        DataInputStream fileIn = null;
        String actionHandler = Misc.getActionHandler(fileToRead.getPath());
        try {
            if (actionHandler != null) {
                Misc.putSysMessage(0, "Preprocessing...");
                String command = "";
                if (actionHandler.indexOf(' ') != -1) command += "\"" + actionHandler + "\""; else command += actionHandler;
                command += " " + fileToRead.getPath().replace('\\', '/');
                Misc.putSysMessage(0, "Executing: " + command);
                Process Handler = Runtime.getRuntime().exec(command);
                if (Handler != null) fileIn = new DataInputStream(Handler.getInputStream());
                this.outputStatusLine(200);
                this.outputStdHeaders();
                this.outputConnectionHeader();
                this.outputFlush();
            } else {
                fileIn = new DataInputStream(new BufferedInputStream(new FileInputStream(fileToRead)));
                this.outputStatusLine(200);
                this.outputStdHeaders();
                SimpleDateFormat temp = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
                temp.setTimeZone(new SimpleTimeZone(0, "GMT"));
                String dateStr = temp.format(new Date(fileToRead.lastModified()));
                this.outputHeader("Last-Modified: " + dateStr);
                this.outputHeader("Content-type: " + Misc.getContentType(fileToRead.getPath()));
                this.outputHeader("Content-length: " + fileToRead.length());
                this.outputConnectionHeader();
                this.outputHeader("");
                this.outputFlush();
            }
            if (this.sendBody == true) {
                if (actionHandler == null) {
                    byte[] buffer = new byte[(int) fileToRead.length()];
                    fileIn.readFully(buffer);
                    this.out.write(buffer);
                    this.outputFlush();
                } else {
                    byte[] buffer = new byte[2];
                    int status = 0;
                    while (status != -1) {
                        status = fileIn.read(buffer);
                        this.out.write(buffer);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Misc.putSysMessage(1, "FileRead: " + e);
        } catch (IOException e) {
            Misc.putSysMessage(1, "FileRead: " + e);
        } finally {
            try {
                if (fileIn != null) fileIn.close();
            } catch (IOException e) {
                Misc.putSysMessage(1, "FileRead: " + e);
            }
            this.outputFlush();
        }
    }

    private String giveOutputFooter() {
        boolean doOutput = false;
        String startOfFooter = "<P><HR><ADDRESS>" + pws.serverName() + " v" + pws.serverVersion() + " at ";
        String endOfFooter = " Port " + sock.getLocalPort() + "</ADDRESS>";
        String midOfFooter = "";
        if (pws.getSetting("serversignature").equalsIgnoreCase("on") == true) {
            doOutput = true;
            midOfFooter = pws.getSetting("servername");
        } else {
            if (pws.getSetting("serversignature").equalsIgnoreCase("email") == true) {
                doOutput = true;
                midOfFooter = " <a href=\"mailto:" + pws.getSetting("serveradmin") + "\">" + pws.getSetting("servername") + "</a>";
            }
        }
        if (doOutput == true) return startOfFooter + midOfFooter + endOfFooter; else return "";
    }

    public void outputDirectoryListing(String httpURIPath) {
        String dirListing = this.generateDirectoryListing(httpURIPath);
        this.outputStatusLine(200);
        this.outputStdHeaders();
        SimpleDateFormat temp = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        temp.setTimeZone(new SimpleTimeZone(0, "GMT"));
        String dateStr = temp.format(new Date());
        this.outputHeader("Last-Modified: " + dateStr);
        this.outputHeader("Content-type: " + Misc.getContentType("dirlist.htm"));
        this.outputHeader("Content-length: " + dirListing.length());
        this.outputConnectionHeader();
        this.outputHeader("");
        if (this.sendBody == true) this.outputSomething(dirListing);
        this.outputFlush();
    }

    private String generateDirectoryListing(String httpURIPath) {
        String dirListing = "";
        File path = new File(pws.getSetting("documentroot") + httpURIPath);
        File[] Listing = path.listFiles();
        if (Listing != null) {
            dirListing = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">";
            dirListing += "<HTML><HEAD><TITLE>Index of " + httpURIPath + "</TITLE></HEAD><BODY><H1>Index of " + httpURIPath + "</H1><P><TABLE BORDER=\"0\" CELLPADDING=\"4\" WIDTH=\"100%\">";
            dirListing += "<TR><TD>Type</TD><TD>File Name</TD><TD>File Size</TD><TD>Last Modified</TD><TD></TD></TR>";
            dirListing += "<TR><TD COLSPAN=\"5\"><HR></TD>";
            if (path.getPath().replace('\\', '/').equals(pws.getSetting("documentroot")) == false) {
                String filePath = path.getPath().substring(pws.getSetting("documentroot").length(), path.getPath().length() - path.getName().length()).replace('\\', '/');
                StringTokenizer filePart = new StringTokenizer(filePath, "/");
                String piece = "";
                String EncodedfilePath = "";
                while (filePart.hasMoreTokens() == true) {
                    EncodedfilePath += "/";
                    piece = filePart.nextToken();
                    try {
                        EncodedfilePath += URLEncoder.encode(piece, pws.getEncoding());
                    } catch (UnsupportedEncodingException e) {
                        Misc.putSysMessage(1, "Unsupported encoding detected: " + e);
                    }
                }
                dirListing += "<TR><TD COLSPAN=\"4\"><a href=\"http://" + pws.getSetting("servername") + ":" + pws.getSetting("port");
                dirListing += EncodedfilePath + "\">Parent Directory</a></TD></TR>" + pws.crlf();
            }
            for (int i = 0; i < Listing.length; i++) {
                dirListing += "<TR><TD>";
                if (Listing[i].isDirectory() == true) dirListing += "[DIR]"; else dirListing += "[FILE]";
                SimpleDateFormat temp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss zzz");
                temp.setTimeZone(new SimpleTimeZone(0, "GMT"));
                String S_lastModified = temp.format(new Date(Listing[i].lastModified()));
                String filePath = Listing[i].getPath().substring(pws.getSetting("documentroot").length() + 1).replace('\\', '/');
                StringTokenizer filePart = new StringTokenizer(filePath, "/");
                String piece = "";
                String EncodedfilePath = "";
                while (filePart.hasMoreTokens() == true) {
                    EncodedfilePath += "/";
                    piece = filePart.nextToken();
                    try {
                        EncodedfilePath += URLEncoder.encode(piece, pws.getEncoding());
                    } catch (UnsupportedEncodingException e) {
                        Misc.putSysMessage(1, "Unsupported encoding detected: " + e);
                    }
                }
                dirListing += "</TD><TD><a href=\"http://" + pws.getSetting("servername") + ":" + pws.getSetting("port");
                dirListing += EncodedfilePath + "\">" + Listing[i].getName() + "</a></TD><TD>" + Listing[i].length() + " Bytes</TD><TD>" + S_lastModified + "</TD></TR>" + pws.crlf();
            }
            dirListing += "</TABLE>" + this.giveOutputFooter() + "</BODY></HTML>";
        }
        return dirListing;
    }

    public void outputTrace(String httpURI) {
        this.outputStatusLine(200);
        this.outputStdHeaders();
        SimpleDateFormat temp = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        temp.setTimeZone(new SimpleTimeZone(0, "GMT"));
        String dateStr = temp.format(new Date());
        this.outputHeader("Last-Modified: " + dateStr);
        this.outputHeader("Content-type: " + Misc.getContentType("trace.htm"));
        this.outputHeader("");
        Iterator mapIterator = this.httpHeaders.keySet().iterator();
        String aHeader, aValue;
        while (mapIterator.hasNext()) {
            aHeader = (String) mapIterator.next();
            aValue = (String) httpHeaders.get(aHeader);
            this.outputSomething(aHeader + ": " + aValue);
        }
        this.outputFlush();
    }
}
