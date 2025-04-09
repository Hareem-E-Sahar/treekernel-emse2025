package com.rbnb.plugins;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.PlugInChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipFilePlugIn implements SimplePlugIn.PlugInCallback {

    public void setOptions(Hashtable options) {
        myOptions = options;
    }

    public void processRequest(PlugInChannelMap picm) throws SAPIException {
        boolean registrationRequest = "registration".equals(picm.GetRequestReference());
        if (!(registrationRequest && picm.NumberOfChannels() == 0 || picm.NumberOfChannels() == 1 && picm.GetName(0).equals("..."))) {
            verifySink();
            sinkMap.Clear();
            for (int ii = 0; ii < picm.NumberOfChannels(); ++ii) sinkMap.Add(picm.GetName(ii));
            sink.Request(sinkMap, picm.GetRequestStart(), picm.GetRequestDuration(), picm.GetRequestReference());
            sink.Fetch(60000, sinkMap);
            if (sinkMap.GetIfFetchTimedOut()) System.err.println("++ ZipFilePlugIn Time out on" + picm.GetName(0)); else if (registrationRequest) {
                for (int ii = 0; ii < sinkMap.NumberOfChannels(); ++ii) {
                    int rIndex = picm.GetIndex(sinkMap.GetName(ii));
                    if (rIndex == -1) rIndex = picm.Add(sinkMap.GetName(ii));
                    picm.PutTimeRef(sinkMap, ii);
                    if ("text/xml".equals(sinkMap.GetMime(ii))) {
                        String result = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + "<!DOCTYPE rbnb>\n" + "<rbnb>\n" + "\t\t<size>" + 1 + "</size>\n" + "\t\t<mime>" + ZIP_MIME + "</mime>\n" + "</rbnb>\n";
                        picm.PutDataAsString(rIndex, result);
                    } else picm.PutData(rIndex, sinkMap.GetData(ii), sinkMap.GetType(ii));
                    picm.PutMime(rIndex, sinkMap.GetMime(ii));
                }
            } else {
                picm.Clear();
                int index = picm.Add("output.zip");
                picm.PutMime(index, ZIP_MIME);
                picm.PutDataAsByteArray(index, zip());
            }
        } else picm.Clear();
    }

    public boolean recycle() {
        sink.CloseRBNBConnection();
        return true;
    }

    /**
	  * The main only prints instructions on how to use the service with
	  * the XMLRPCPlugIn.  It then exits.
	  */
    public static void main(String[] args) {
        if (args.length == 0) showUsage(); else {
            String[] args2 = new String[args.length + 2];
            System.arraycopy(args, 0, args2, 0, args.length);
            args2[args.length] = "-c";
            args2[args.length + 1] = ZipFilePlugIn.class.getName();
            SimplePlugIn.main(args2);
        }
    }

    private static void showUsage() {
        System.err.println(ZipFilePlugIn.class.getName() + ": Data compressor.\nCopyright Creare, Inc. 2003" + "\nOptions:" + "\n\t-a host:port [localhost:3333]\t- RBNB server" + " to connect to" + "\n\t-n name [" + ZipFilePlugIn.class.getName() + "]\t- client name for plugin");
    }

    private static final String ZIP_MIME = "application/x-zip-compressed";

    /**
	  * Verifies that a good sink connection exists; if not, reconnects once.
	  */
    private void verifySink() throws SAPIException {
        String hostname = myOptions.get("RBNB").toString(), sinkname = "ZipFilePlugIn.sink", user = myOptions.get("user").toString(), pword = myOptions.get("password").toString();
        boolean reconnect = true;
        try {
            if (hostname.equals(sink.GetServerName())) reconnect = false;
        } catch (Throwable t) {
        }
        if (reconnect) sink.OpenRBNBConnection(hostname, sinkname, user, pword);
    }

    /**
	  * Performs zip function.
	  */
    private byte[] zip() {
        try {
            baos.reset();
            ZipOutputStream zos = new ZipOutputStream(baos);
            for (int ii = 0; ii < sinkMap.NumberOfChannels(); ++ii) {
                byte[] data = sinkMap.GetData(ii);
                ZipEntry ze = new ZipEntry(sinkMap.GetName(ii));
                zos.putNextEntry(ze);
                zos.write(data);
            }
            zos.finish();
            return baos.toByteArray();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        return null;
    }

    private final Sink sink = new Sink();

    private final ChannelMap sinkMap = new ChannelMap();

    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    private Hashtable myOptions;
}
