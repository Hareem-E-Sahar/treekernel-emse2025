package com.rbnb.web;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

/**
  * Utility to monitor a list of URLs for changes.  The changed files are 
  *  copied to a destination URL using WebDAV PUT, or streamed to TCP or
  *   UDP sockets.
  *<p>
  * @author William Finger, Creare Inc.
  * @version 1.3
  */
public abstract class HttpMonitor {

    /**
	  * Utility class which manages each resource, including URL, last 
	  *   modification time, etc.
	  */
    protected static class Resource implements Comparable {

        Resource(URL source, URL dest) {
            this.source = source;
            this.dest = dest;
            minInterval = HttpMonitor.minimumInterval;
            maxInterval = HttpMonitor.maximumInterval;
        }

        /**
		  * Comparison function for priority queue sorting.
		  */
        public int compareTo(Object o) {
            Resource r = (Resource) o;
            if (nextRequestTime < r.nextRequestTime) return -1;
            if (nextRequestTime == r.nextRequestTime) return 0;
            return 1;
        }

        /** 
		  * Comparison function for set operations on queues.
		  */
        public boolean equals(Object o) {
            Resource r = (Resource) o;
            return (source.equals(r.source) && dest.equals(r.dest));
        }

        /** 
		  * Returns the String used in the Authorization field.
		  */
        public String getAuthorization() {
            return auth;
        }

        /**
		  * Sets the username and password to use for BASIC authentication.
		  */
        public void setAuthorization(String name, String pwd) {
            String authentication = com.rbnb.utility.Base64Encode.encode((name + ":" + pwd).getBytes());
            auth = "Basic " + authentication;
        }

        /** 
		  * Sets the String used in the Authorization field.
		  */
        public void setAuthorization(String value) {
            auth = value;
        }

        public URL getSource() {
            return source;
        }

        /**
		  * Can be overridden by subclasses to modify the destination on a
		  *  per-PUT basis.
		  */
        public URL getDestination() {
            return dest;
        }

        /**
		  * In milliseconds.
		  */
        public long getInitialSleep() {
            return initialSleep;
        }

        /**
		  * Sets the time to wait before downloading a resource for the 
		  *  first time.  The defualt is zero.
		  */
        public void setInitialSleep(long is) {
            initialSleep = is;
            nextRequestTime = System.currentTimeMillis() + is;
        }

        public long getMinimumInterval() {
            return minInterval;
        }

        /**
		  * In milliseconds.
		  */
        public void setMinimumInterval(long min) {
            minInterval = min;
        }

        public long getMaximumInterval() {
            return maxInterval;
        }

        public void setMaximumInterval(long max) {
            maxInterval = max;
        }

        /**
		  * Sets the resource's time calculations to their initial states.
		  */
        public void reset() {
            prevLocalDate = prevLastMod = nextRequestTime = delta = 0;
            successCount = failCount = 0;
            lastModifiedString = null;
        }

        byte[] getLastRead() {
            return lastRead;
        }

        /**
		  * Calls <a href="#get(boolean)">get(true)</a>.
		  */
        byte[] get() throws IOException {
            return get(true);
        }

        /**
		  * Download the resource data from its URL.
		  *
		  * @param doWrite  if true, the file is written to the destination.
		  * @return the bytes of the resource, or null.
		  */
        byte[] get(boolean doWrite) throws IOException {
            long expires = 0, lastMod = 0, theirDate = 0, requestTime = System.currentTimeMillis();
            HttpURLConnection srcCon = (HttpURLConnection) source.openConnection();
            boolean success = false;
            byte[] ba = null;
            if (urlConnectionReadTimeoutMethod != null) {
                try {
                    Object[] args = { new Integer(resourceReadTimeout) };
                    urlConnectionReadTimeoutMethod.invoke(srcCon, args);
                } catch (Throwable t) {
                }
            }
            if (lastModifiedString != null) {
                srcCon.setRequestProperty("If-Modified-Since", lastModifiedString);
            }
            if (auth != null) {
                srcCon.setRequestProperty("Authorization", auth);
            }
            int getResponse = 0, putResponse = 0;
            try {
                getResponse = srcCon.getResponseCode();
                if (getResponse == HttpURLConnection.HTTP_OK) {
                    lastModifiedString = srcCon.getHeaderField("Last-Modified");
                    expires = srcCon.getExpiration();
                    lastMod = srcCon.getLastModified();
                    theirDate = srcCon.getDate();
                    ba = read(srcCon);
                    if (lastMod == 0 || lastMod != prevLastMod) {
                        if (doWrite && ba.length > 0) putResponse = write(ba);
                        success = true;
                    }
                }
            } catch (Exception e) {
                System.out.println("Error copying:\n\t" + source + "\nto:\n\t" + dest + ":");
                e.printStackTrace();
            }
            nextRequestTime = guessNextTime(requestTime, success, theirDate, lastMod, expires);
            if (debug) printLog(requestTime, getResponse, putResponse, theirDate, lastMod, expires, nextRequestTime);
            return ba;
        }

        byte[] read(HttpURLConnection srcCon) throws IOException {
            InputStream input = srcCon.getInputStream();
            byte[] ba = lastRead = null;
            try {
                ba = new byte[srcCon.getContentLength()];
                int bytesRead = 0;
                do {
                    int wasRead = input.read(ba, bytesRead, ba.length - bytesRead);
                    if (wasRead < 1) {
                        Thread.dumpStack();
                        System.err.println("Error in resource read.");
                        ba = null;
                        break;
                    }
                    bytesRead += wasRead;
                } while (bytesRead != ba.length);
            } finally {
                input.close();
            }
            return lastRead = ba;
        }

        /**
		  * @return the PUT response code.
		  */
        final int write(byte[] ba) throws IOException {
            try {
                writeToSockets(ba);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            URL dest = getDestination();
            if (dest == null) return 0;
            HttpURLConnection destCon = (HttpURLConnection) dest.openConnection();
            destCon.setDoOutput(true);
            destCon.setRequestMethod("PUT");
            if (putAuth != null) {
                destCon.setRequestProperty("Authorization", putAuth);
            }
            OutputStream output = destCon.getOutputStream();
            try {
                output.write(ba);
                output.flush();
            } finally {
                output.close();
            }
            int response = destCon.getResponseCode();
            if (response >= 200 && response < 300) ; else if (response == 409) makeDestination(); else throw new IOException("Error in PUT to " + dest + ": " + response + ' ' + destCon.getResponseMessage());
            return response;
        }

        /**
		  * Writes to any connected sockets.
		  */
        private void writeToSockets(byte[] ba) throws IOException {
            synchronized (sockets) {
                for (int ii = 0; ii < sockets.size(); ++ii) {
                    Socket sock = (Socket) sockets.get(ii);
                    try {
                        sock.getOutputStream().write(ba);
                    } catch (SocketException se) {
                        sockets.remove(ii--);
                    }
                }
            }
            if (udpPacket != null) {
                try {
                    udpPacket.setData(ba);
                    udpSocket.send(udpPacket);
                } catch (PortUnreachableException pue) {
                    System.err.println("WARNING: UDP destination " + udpPacket.getAddress() + ":" + udpPacket.getPort() + " is unreachable.");
                } catch (SocketException se) {
                    se.printStackTrace();
                }
            }
        }

        /**
		  * Determines when to make the next resource download, based on the
		  *  local clock.
		  * <p>This algorithm attempts to synchronize downloads of a resource
		  *  with the server's updates of that resource.  In general it
		  *  averages the difference between the server's claimed last update
		  *  time and the current update time, with the difference between our
		  *  last download time and the current download time.  This result is
		  *  added to the current time to compute the next download time.
		  * <p>Because it is impossible to know if a resource has been updated
		  *  more than once between intervals, this algorithm seeks to guess a 
		  *  time slightly smaller ({@link #deltaFraction} of the time)
		  *  than the real update interval.  A {@link
		  *  HttpURLConnection#HTTP_NOT_MODIFIED} result from the server serves
		  *  to confirm that we are not missing server updates in the interim.
		  * <p>Once a failure occurs, the minimum interval is used until the 
		  *  file is updated.
		  * <p>
		  * @param ourNow    Current system time.
		  * @param success   Whether last GET was successful.
		  * @param dateField The value of the server's Date header field; should
		  *      be the server's local time when the response was made.
		  * @param lastModField   The value of the Last-Modified field.
		  * @param expiresField   The value of the Expires field.  Most  
		  *      servers do not set this field.
		  * @return the absolute time the next request should be made.
		  */
        private long guessNextTime(long ourNow, boolean success, long dateField, long lastModField, long expiresField) {
            long updateDelta;
            if (success) {
                long thisDelta;
                failCount = 0;
                if (++successCount == successCountToMin) {
                    successCount = 0;
                    thisDelta = delta = getMinimumInterval();
                } else if (prevLocalDate != 0) thisDelta = ourNow - prevLocalDate; else thisDelta = getMinimumInterval();
                if (lastModField != 0 && prevLastMod != 0) {
                    long serverDelta = lastModField - prevLastMod;
                    thisDelta = (thisDelta + serverDelta) / 2;
                }
                prevLastMod = lastModField;
                thisDelta = boundInterval(thisDelta);
                if (delta == 0) delta = thisDelta; else if (thisDelta > delta) delta = (3 * delta + thisDelta) / 4; else delta = (delta + 3 * thisDelta) / 4;
                updateDelta = delta * deltaFraction / 100;
            } else {
                successCount = 0;
                if (failCount + 1 == failCountToMax) {
                    updateDelta = getMaximumInterval();
                } else {
                    ++failCount;
                    updateDelta = getMinimumInterval();
                }
            }
            if (dateField != 0 && expiresField > dateField) {
                updateDelta = expiresField - dateField;
            }
            if (success) prevLocalDate = ourNow;
            updateDelta = boundInterval(updateDelta);
            return ourNow + updateDelta;
        }

        /**
		  * Bounds a value between the minimum and maximum intervals.
		  * @return the bounded interval.
		  */
        protected long boundInterval(long toBound) {
            if (toBound > getMaximumInterval()) toBound = getMaximumInterval(); else if (toBound < getMinimumInterval()) toBound = getMinimumInterval();
            return toBound;
        }

        /**
		  * Keep consistent with printLog().
		  */
        static void printLogHeader() {
            System.out.println("Local Date,Source URL,Dest URL,GET Code," + "PUT Code,Server Date,Server LastMod,Server Expires," + "Next Copy Date");
        }

        private void printLog(long localTime, int getCode, int putCode, long serverDate, long serverLastMod, long serverExpires, long nextRequestTime) {
            PrintStream ps = System.out;
            String notSet = "---";
            logDate.setTime(localTime);
            ps.print(logDate);
            ps.print(',');
            ps.print(source);
            ps.print(',');
            ps.print(dest);
            ps.print(',');
            if (getCode > 0) ps.print(getCode); else ps.print(notSet);
            ps.print(',');
            if (putCode > 0) ps.print(putCode); else ps.print(notSet);
            ps.print(',');
            if (serverDate > 0) {
                logDate.setTime(serverDate);
                ps.print(logDate);
            } else ps.print(notSet);
            ps.print(',');
            if (serverLastMod > 0) {
                logDate.setTime(serverLastMod);
                ps.print(logDate);
            } else ps.print(notSet);
            ps.print(',');
            if (serverExpires > 0) {
                logDate.setTime(serverExpires);
                ps.print(logDate);
            } else ps.print(notSet);
            ps.print(',');
            logDate.setTime(nextRequestTime);
            ps.println(logDate);
        }

        public String toString() {
            return source.toString() + " -> " + dest.toString() + "(" + (nextRequestTime - System.currentTimeMillis()) + ")";
        }

        private final URL source, dest;

        /**
		  * The exact string specified in the server response.  We use this
		  *  for 'If-Modified-Since', based on a recommendation from the RFC,
		  *  that indicates that some servers may use a string comparison on 
		  *  that header rather than an actual date comparison.
		  */
        private String lastModifiedString;

        /**
		  * Authorization header field used for BASIC authentication.
		  */
        private String auth;

        private long prevLocalDate, prevLastMod, nextRequestTime, delta, minInterval, maxInterval, initialSleep;

        private final java.util.Date logDate = new java.util.Date();

        private int successCount, failCount;

        /**
		  * The bytes from the last read of the resource, or null if the
		  *  get failed.
		  */
        private byte[] lastRead;
    }

    /**
	  *  Root utility class for XML SAX parsing.
	  */
    protected static class RootParser extends org.xml.sax.helpers.DefaultHandler {

        protected void parse(org.xml.sax.InputSource input) throws java.io.IOException, SAXException {
            if (xmlReader == null) {
                try {
                    xmlReader = XMLReaderFactory.createXMLReader();
                } catch (Throwable t) {
                    try {
                        xmlReader = XMLReaderFactory.createXMLReader("com.bluecast.xml.Piccolo");
                    } catch (Throwable t2) {
                        xmlReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
                    }
                }
                System.err.println("Using SAX parser: " + xmlReader.getClass().getName());
            }
            xmlReader.setContentHandler(this);
            xmlReader.setErrorHandler(this);
            xmlReader.parse(input);
        }

        private static XMLReader xmlReader = null;
    }

    /**
	  * A SAX handler to process the configuration file.
	  */
    protected static class ConfigParser extends RootParser {

        ConfigParser(String fname) throws IOException, SAXException {
            parse(fname);
        }

        ConfigParser(byte[] xmlArray) throws IOException, SAXException {
            parse(new org.xml.sax.InputSource(new ByteArrayInputStream(xmlArray)));
        }

        /**
		  * Parse the XML in the provided filename.
		  */
        private void parse(String fname) throws java.io.IOException, SAXException {
            InputStream stream;
            File file = new File(fname);
            if (!file.exists()) {
                URL url = new URL(fname);
                stream = url.openStream();
            } else stream = new java.io.FileInputStream(file);
            parse(new org.xml.sax.InputSource(stream));
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            sbuffer.setLength(0);
            if ("monitor".equals(qName)) {
                String temp;
                if ((temp = attributes.getValue("minimumInterval")) != null) {
                    try {
                        minimumInterval = Long.parseLong(temp);
                    } catch (NumberFormatException nfe) {
                        System.err.println("WARNING: minimumInterval attribute incorrect.");
                    }
                }
                if ((temp = attributes.getValue("maximumInterval")) != null) {
                    try {
                        maximumInterval = Long.parseLong(temp);
                    } catch (NumberFormatException nfe) {
                        System.err.println("WARNING: maximumInterval attribute incorrect.");
                    }
                }
                if ((temp = attributes.getValue("deltaFraction")) != null) {
                    try {
                        double df = Double.parseDouble(temp);
                        deltaFraction = (int) (100 * df);
                        if (deltaFraction <= 0 || deltaFraction >= 100) {
                            System.err.println("WARNING: deltaFraction must be" + " greater than zero and less than one.");
                            deltaFraction = 90;
                        }
                    } catch (NumberFormatException nfe) {
                        System.err.println("WARNING: deltaFraction attribute incorrect.");
                    }
                }
                if ((temp = attributes.getValue("successCountToMin")) != null) {
                    try {
                        successCountToMin = Integer.parseInt(temp);
                    } catch (NumberFormatException nfe) {
                        System.err.println("WARNING: successCountToMin attribute incorrect.");
                    }
                }
                if ((temp = attributes.getValue("failCountToMax")) != null) {
                    try {
                        failCountToMax = Integer.parseInt(temp);
                    } catch (NumberFormatException nfe) {
                        System.err.println("WARNING: failCountToMax attribute incorrect.");
                    }
                }
                if ((destURLPath = attributes.getValue("destURLPath")) != null) {
                    if (!destURLPath.endsWith("/")) destURLPath += '/';
                    mkcolQuery = attributes.getValue("mkcolQuery");
                    if (mkcolQuery == null) mkcolQuery = "";
                    destPrefix = attributes.getValue("destPrefix");
                }
                if ((temp = attributes.getValue("staggerStartup")) != null) {
                    stagger = Long.parseLong(temp);
                }
                if ((temp = attributes.getValue("readTimeout")) != null) {
                    resourceReadTimeout = Integer.parseInt(temp);
                }
                if ((temp = attributes.getValue("debug")) != null) {
                    debug = Boolean.valueOf(temp).booleanValue();
                }
                if ((temp = attributes.getValue("tcpListenPort")) != null) {
                    tcpListenPort = Integer.parseInt(temp);
                }
                boolean udpError = false;
                String addr, port;
                if ((addr = attributes.getValue("udpDestAddr")) != null) {
                    if ((port = attributes.getValue("udpDestPort")) == null) udpError = true; else try {
                        udpPacket = new DatagramPacket(new byte[0], 0, InetAddress.getByName(addr), Integer.parseInt(port));
                    } catch (UnknownHostException uhe) {
                        uhe.printStackTrace();
                    }
                } else if (attributes.getValue("udpDestPort") != null) udpError = true;
                if (udpError) throw new SAXException("Both udpDestAddr and" + " udpDestPort must be specified, if either.");
            } else if ("resource".equals(qName)) {
                clear();
                inResource = true;
            } else if ("configMonitor".equals(qName)) {
                clear();
                inResource = true;
                if (configQueue.size() > 0) {
                    throw new SAXException("Only one configuration node allowed.");
                }
            } else if ("gate".equals(qName)) {
                if (inResource) throw new SAXException("<gate> cannot be a child of <resource>.");
                clear();
                inGate = true;
            } else if ("url".equals(qName) && !inResource && !inGate) {
                clear();
            }
        }

        public void characters(char[] ch, int start, int length) {
            sbuffer.append(ch, start, length);
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("url".equals(qName)) {
                try {
                    srcUrl = new URL(sbuffer.toString());
                } catch (MalformedURLException mue) {
                    System.err.println("WARNING: " + mue);
                }
                if (!inResource && !inGate) makeResource(resourceQueue);
            } else if ("destFile".equals(qName)) {
                destFile = sbuffer.toString();
            } else if ("resource".equals(qName)) {
                inResource = false;
                if (srcUrl == null) {
                    throw new SAXException("ERROR: resource requires url subtag.");
                }
                makeResource(resourceQueue);
            } else if ("configMonitor".equals(qName)) {
                inResource = false;
                if (srcUrl == null) {
                    throw new SAXException("ERROR: configMonitor requires url subtag.");
                }
                makeResource(configQueue);
            } else if ("user".equals(qName)) user = sbuffer.toString(); else if ("password".equals(qName)) password = sbuffer.toString(); else if ("initialSleep".equals(qName) && (inResource || inGate)) iSleep = Long.parseLong(sbuffer.toString()); else if ("minimumInterval".equals(qName) && (inResource || inGate)) minInterval = Long.parseLong(sbuffer.toString()); else if ("maximumInterval".equals(qName) && (inResource || inGate)) maxInterval = Long.parseLong(sbuffer.toString()); else if ("gate".equals(qName)) {
                inGate = false;
                if (srcUrl == null) {
                    throw new SAXException("ERROR: gate requires url subtag.");
                }
                makeResource(gateQueue);
            } else if ("auth".equals(qName) && !(inResource || inGate)) {
                defaultUser = user;
                defaultPassword = password;
                if (putAuth == null) setPutAuth(defaultUser, defaultPassword);
            } else if ("putAuth".equals(qName) && !(inResource || inGate)) {
                setPutAuth(user, password);
            }
        }

        public void endDocument() throws SAXException {
            Collections.sort(resourceQueue);
            Collections.sort(gateQueue);
            Collections.sort(configQueue);
        }

        public void error(SAXParseException e) {
            System.err.println("Parse error: ");
            e.printStackTrace();
        }

        public void warning(SAXParseException e) {
            System.err.println("Parse warning: ");
            e.printStackTrace();
        }

        private void clear() {
            srcUrl = null;
            destFile = user = password = null;
            minInterval = maxInterval = iSleep = 0L;
        }

        protected final StringBuffer getCharacters() {
            return sbuffer;
        }

        private void makeResource(List queue) {
            Resource res = HttpMonitor.makeResource(srcUrl, destFile, queue);
            if (user != null) res.setAuthorization(user, password); else if (defaultUser != null) res.setAuthorization(defaultUser, defaultPassword);
            if (minInterval != 0L) res.setMinimumInterval(minInterval);
            if (maxInterval != 0L) res.setMaximumInterval(maxInterval);
            if (iSleep != 0L) {
                res.setInitialSleep(iSleep);
                nextSleep = iSleep + stagger;
            } else if (stagger != 0L) {
                res.setInitialSleep(nextSleep);
                nextSleep += stagger;
            }
        }

        private final StringBuffer sbuffer = new StringBuffer();

        private URL srcUrl;

        private String destFile, user, password;

        private long minInterval, maxInterval, iSleep, nextSleep = 0;

        /**
		  * An interval to add between consecutive resources loading for the
		  *  first time during startup.
		  */
        private long stagger = 0L;

        private boolean inResource = false, inGate = false;
    }

    /**
	  * Calls <a href="#makeResource(URL,String,List)">
	  *   makeResource(srcUrl, destFile, resourceQueue)</a>.
	  */
    protected static Resource makeResource(URL srcUrl, String destFile) {
        return makeResource(srcUrl, destFile, resourceQueue);
    }

    /**
	  * Creates a new resource to monitor.  If the resource is already present,
	  *  it is NOT replaced.
	  *
	  * @param srcUrl URL of the resource.  Required.
	  * @param destFile  The file name to use at the destination if WebDAV
	  *   has been selected.  If null, one is built from the srcUrl.
	  * @param queue  The list to add the resource to.
	  */
    protected static Resource makeResource(URL srcUrl, String destFile, List queue) {
        URL destUrl = null;
        if (destURLPath != null) {
            String dest;
            if (destFile != null) dest = destURLPath + destFile; else if (destPrefix == null) {
                String srcFname = srcUrl.getPath().substring(srcUrl.getPath().lastIndexOf('/') + 1);
                dest = destURLPath + srcFname;
            } else dest = destURLPath + destPrefix + resourceQueue.size() + ".jpg";
            try {
                destUrl = new URL(dest);
            } catch (MalformedURLException mue) {
                System.err.println("WARNING: " + mue);
            }
        }
        Resource res = createResource(srcUrl, destUrl);
        int resIndex = queue.indexOf(res);
        if (resIndex == -1) queue.add(0, res); else res = (Resource) queue.get(resIndex);
        return res;
    }

    /**
	  * Does actual resource creation.  Uses property resourceClass, 
	  *   if available.
	  */
    private static Resource createResource(URL src, URL dest) {
        try {
            if (resourceClass != null) {
                Object[] args = { src, dest };
                return (Resource) resourceClass.getDeclaredConstructors()[0].newInstance(args);
            }
        } catch (Exception e) {
            System.err.println("Could not form resource object:");
            e.printStackTrace();
        }
        return new Resource(src, dest);
    }

    private static void makeWebDavDestination() {
        if (webDavHandlerFactory == null) {
            try {
                webDavHandlerFactory = new com.ibm.webdav.protocol.URLStreamHandlerFactory();
            } catch (Throwable e) {
                e.printStackTrace();
                System.err.println("WARNING: WebDAV extensions not available.");
            }
        }
        try {
            int res;
            while (true) {
                URL parseUrl = new URL(destURLPath + mkcolQuery), mkcolUrl;
                if (webDavHandlerFactory != null) {
                    mkcolUrl = new URL(parseUrl.getProtocol(), parseUrl.getHost(), parseUrl.getPort(), parseUrl.getFile(), webDavHandlerFactory.createURLStreamHandler(parseUrl.getProtocol()));
                } else mkcolUrl = parseUrl;
                HttpURLConnection huc = (HttpURLConnection) mkcolUrl.openConnection();
                huc.setRequestMethod("MKCOL");
                if (putAuth != null) {
                    huc.setRequestProperty("Authorization", putAuth);
                }
                res = huc.getResponseCode();
                if (debug) System.err.println("MKCOL response = " + res);
                if (res >= 200 && res < 300 || res == HttpURLConnection.HTTP_BAD_METHOD) break;
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void reconfigure(byte[] newConf) throws IOException, SAXException {
        if (serverSocketThread != null) {
            stopSignal = true;
            try {
                serverSocket.close();
                serverSocketThread.join();
            } catch (Throwable t) {
            }
            serverSocket = null;
            serverSocketThread = null;
            tcpListenPort = 0;
            for (Iterator iter = sockets.iterator(); iter.hasNext(); ) {
                ((Socket) iter.next()).close();
            }
            sockets.clear();
        }
        if (udpSocket != null) {
            udpSocket.close();
            udpSocket = null;
            udpPacket = null;
        }
        resourceQueue.clear();
        gateQueue.clear();
        Resource oldConf = (Resource) configQueue.get(0);
        configQueue.clear();
        new ConfigParser(newConf);
        if (!configQueue.isEmpty()) {
            Resource currConf = (Resource) configQueue.get(0);
            if (currConf.getSource().equals(oldConf.getSource())) {
                currConf.nextRequestTime = oldConf.nextRequestTime;
                currConf.lastModifiedString = oldConf.lastModifiedString;
            }
        }
        makeDestination();
    }

    /** 
	  * Creates the destination collection URL using MKCOL. 
	  */
    protected static void makeDestination() {
        if (destURLPath != null) makeWebDavDestination();
        try {
            if (tcpListenPort != 0) {
                serverSocket = new ServerSocket(tcpListenPort);
                serverSocketThread = new Thread(new Runnable() {

                    public void run() {
                        stopSignal = false;
                        int errorCount = 0, ERROR_MAX = 5;
                        while (!stopSignal && errorCount < ERROR_MAX) {
                            try {
                                Socket s = serverSocket.accept();
                                synchronized (sockets) {
                                    sockets.add(s);
                                }
                            } catch (IOException ioe) {
                                if (!stopSignal) {
                                    ioe.printStackTrace();
                                    ++errorCount;
                                }
                            }
                        }
                    }
                }, "ServerSocket Listener");
                serverSocketThread.setDaemon(true);
                serverSocketThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (udpPacket != null) {
                udpSocket = new DatagramSocket();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	  * Check registered gate URLs for freshness.
	  */
    protected static boolean checkGates() {
        long now = System.currentTimeMillis();
        if (gateQueue.size() == 0 || gateExpiration > now) return true;
        for (int ii = 0; ii < gateQueue.size(); ++ii) {
            Resource gate = (Resource) gateQueue.get(ii);
            try {
                gate.get(false);
                if (now - gate.prevLastMod < gate.getMinimumInterval()) {
                    gateExpiration = now + gate.getMinimumInterval();
                    return true;
                }
            } catch (IOException ioe) {
            }
        }
        resetResources = true;
        return false;
    }

    /**
	  * Loops infinitely, loading resources in the queue.
	  */
    protected static void loadResources() throws InterruptedException {
        Resource.printLogHeader();
        while (loadNextResource(resourceQueue, true) != null) {
            if (!configQueue.isEmpty()) {
                Resource conf = (Resource) configQueue.get(0);
                if (conf.nextRequestTime <= System.currentTimeMillis()) {
                    try {
                        byte[] newConf = conf.get(false);
                        if (newConf != null) reconfigure(newConf);
                    } catch (Exception e) {
                        System.err.println("WARNING: Could not load configuration from " + conf.getSource() + ":");
                        e.printStackTrace();
                        conf.nextRequestTime = System.currentTimeMillis() + conf.getMinimumInterval();
                    }
                }
            }
        }
    }

    /**
	  * Loads the next resource in the queue.
	  * @param queue  The queue to scan.
	  * @param doWrite The parameter passed to Resource.get().
	  * @return the resource loaded, or null if loading should be aborted.
	  */
    protected static Resource loadNextResource(List queue, boolean doWrite) throws InterruptedException {
        while (!checkGates()) {
            if (debug) System.err.println("No gates satisfied.");
            Thread.sleep(minimumInterval);
        }
        if (resetResources) {
            resetResources = false;
            for (Iterator iter = queue.iterator(); iter.hasNext(); ) {
                ((Resource) iter.next()).reset();
            }
        }
        Resource r = (Resource) queue.remove(0);
        long now = System.currentTimeMillis();
        if (r.nextRequestTime > now) Thread.sleep(r.nextRequestTime - now);
        try {
            r.get(doWrite);
            int index = Collections.binarySearch(queue, r);
            if (index >= 0) queue.add(index, r); else queue.add(-index - 1, r);
        } catch (IOException ioe) {
            System.err.println("Error resolving resource(" + r + "): " + ioe);
            System.err.println("Object discarded.");
            if (queue.size() == 0) return null;
        }
        return r;
    }

    protected static java.util.List getResourceQueue() {
        return resourceQueue;
    }

    protected static void setPutAuth(String user, String pword) {
        Resource r = new Resource(null, null);
        r.setAuthorization(user, pword);
        putAuth = r.getAuthorization();
    }

    /**
	  * HttpMonitor main.  The main function takes one argument, the 
	  *   XML file to use for configuration.
	  */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Insufficient arguments.");
            System.err.println("HttpMonitor config-file-or-url\n");
            return;
        }
        try {
            new ConfigParser(args[0]);
            if (resourceQueue.size() == 0) {
                System.err.println("ERROR: No source files specified.");
                return;
            }
            makeDestination();
            loadResources();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	  * Sorted queue of URLs to monitor, with the next resource at element 0.
	  */
    private static final ArrayList resourceQueue = new ArrayList(), configQueue = new ArrayList(), gateQueue = new ArrayList(), sockets = new ArrayList();

    /**
	  * In millis.
	  */
    private static long minimumInterval = 10000, maximumInterval = 60 * 1000, gateExpiration;

    /**
	  * Port to listen for incoming connections.
	  */
    private static int tcpListenPort = 0;

    /**
	  * Socket which accepts incoming connections, if tcpListenPort is non-zero.
	  */
    private static ServerSocket serverSocket;

    /**
	  * The thread which monitors the server socket, waiting for connections.
	  */
    private static Thread serverSocketThread;

    /**
	  * When raised, stops the serverSocketThread after the serverSocket is
	  *  closed.
	  */
    private static volatile boolean stopSignal;

    /**
	  * Destination address for UDP mode.
	  */
    private static DatagramPacket udpPacket;

    /**
	  * Local bound socket for UDP.
	  */
    private static DatagramSocket udpSocket;

    /**
	  * The amount of time allowed for a resource download.  Functional
	  *  only on Java 1.5 or later.  The default is 60000 (one minute).
	  */
    private static int resourceReadTimeout = 60000;

    private static final java.lang.reflect.Method urlConnectionReadTimeoutMethod;

    static {
        java.lang.reflect.Method temp = null;
        try {
            Class[] args = { int.class };
            temp = URLConnection.class.getDeclaredMethod("setReadTimeout", args);
        } catch (Throwable t) {
            System.err.println("Read Timeout not available.");
        }
        urlConnectionReadTimeoutMethod = temp;
    }

    /**
	  * Percentage of the calculated interval to use until the next update.
	  *  Values smaller than 100 cause more updates than necessary but 
	  *  guarantee that resource updates are not missed.
	  */
    static int deltaFraction = 90;

    /**
	  * The number of consecutive successful GETs before the algorithm gives up
	  *  trying to find the bounds and goes to the minimum value.
	  */
    static int successCountToMin = 5;

    /**
	  * The number of consecutive failed GETs (not modified or otherwise) before
	  *  the algorithm gives up and goes to the maximum value.  This saves on
	  *  network bandwidth in cases where a resource is down.
	  */
    static int failCountToMax = 5;

    static String destURLPath, destPrefix, mkcolQuery, defaultUser, defaultPassword, putAuth;

    static boolean debug = false, resetResources = false;

    static java.net.URLStreamHandlerFactory webDavHandlerFactory;

    /**
	  * Set to a class object that extends Resource, if desired.
	  */
    static Class resourceClass;
}
