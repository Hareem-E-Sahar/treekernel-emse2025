package com.fangr.servers.email.smtp;

import java.util.Vector;
import nfc.utils.Log;
import nfc.plugin.*;
import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.zip.CRC32;

public class SMTPWorker extends Thread implements SMTPConstants {

    PluginHolder plugins;

    BufferedReader in;

    PrintStream out;

    int state;

    String from;

    Vector to;

    String helohost;

    Socket sock;

    Date timestamp;

    String msgid;

    /**
	 * Construct a worker thread
	 * @param holder The plugin holder
	 * @param in The socket input stream
	 * @param out The socket output stream
	 * @param parent The parent object (an SMTPServer in this case)
	 * @param sock The socket object for this thing
	**/
    public SMTPWorker(PluginHolder holder, BufferedReader in, PrintStream out, Object parent, Socket sock) {
        Log.clone(this, parent);
        plugins = holder;
        this.in = in;
        this.out = out;
        this.sock = sock;
        Log.Debug("SMTPWorker created", this);
        to = new Vector();
        from = "";
        timestamp = new Date();
        state = STATESTART;
    }

    /**
	 * The main loop for a worker thread.  We continue reading data until
	 * either the state is STATEQUIT or the socket has died.  We let the
	 * PluginHolder decide what plugin is invoked (by the name of the command).
	**/
    public void run() {
        send(GREETING);
        String line;
        loop: while (state != STATEQUIT) {
            try {
                line = in.readLine();
                Log.Debug(line, this);
            } catch (Exception e) {
                break loop;
            }
            if (line == null) {
                break;
            }
            StringTokenizer tokenizer = new StringTokenizer(line);
            if (tokenizer.hasMoreTokens()) {
                String cmd = tokenizer.nextToken();
                if (plugins.invokePlugin(cmd, new SMTPPluginData(this, line))) {
                } else {
                    send(BAD + " bad request; Command not implemented/recognized");
                }
            }
        }
        send(QUIT);
        try {
            sock.close();
        } catch (Exception e) {
        }
    }

    /**
	 * Send a line of data back to the client
	 * @param data The data to send.
	**/
    public void send(String data) {
        out.println(data);
        Log.Debug(data, this);
    }

    /**
	 * Used to move from state to state (called by plugins)
	 * @param state The state we should go to
	**/
    public void setState(int state) {
        this.state = state;
    }

    /**
	 * Used by plugins to determine if we are in a valid state
	 * for that command to be called
	 * @return The current state
	**/
    public int getState() {
        return state;
    }

    /**
	 * Get the data stream so that if more than one line is needed,
	 * the plugins can read from the stream.
	 * @return The BufferedReader that is laid over the socket stream
	**/
    public BufferedReader getStream() {
        return in;
    }

    /**
	 * Reset the current session to helo state unless we're in
	 * start state in which case we wait.
	**/
    public void reset() {
        from = null;
        to = new Vector();
        helohost = null;
        if (state != STATESTART) {
            state = STATEHELO;
        }
        send(OK + " reset ok");
    }

    /**
	 * Get the next line from the input stream.
	 * @return the next line from the input stream
	**/
    public String readLine() {
        try {
            return in.readLine();
        } catch (Exception e) {
            state = STATEQUIT;
            return ".";
        }
    }

    /**
	 * Set the from address
	 * @param address The address to report in the "from" field.
	**/
    public void setAddr(String address) {
        from = address;
    }

    /**
	 * Add a e-mail to deliver to
	 * @param address The address to add to the "to" field.
	**/
    public void addTo(String address) {
        to.add(address);
    }

    /**
	 * The host reported in the HELO command.
	 * @param helo The host reported
	**/
    public void setHelo(String helo) {
        helohost = helo;
    }

    /**
	 * Sends the message as soon as the data is received.  Sends to each
	 * address specified by parsing the domain (ie fangr@fangr.com sends to
	 * fangr.com) and sending to that user and hoping it gets there.  If we
	 * own this domain write it out to our e-mail format (mailbox/id.msg = txt file)
	 * Log some information.
	 * FIXME: Check for multiple people in same domain and concatenate them
	 * to save bandwidth.
	 * @param data The message data
	**/
    public void setData(String data) {
        for (int i = 0; i < to.size(); i++) {
            String header;
            try {
                header = "Received: from " + helohost + " " + sock.getInetAddress().getHostAddress() + " " + from + "\n";
                header = header + " by " + InetAddress.getLocalHost().getHostAddress() + "\n";
            } catch (Exception e) {
                header = "Received: from " + helohost + " <error resolving host address> " + from + "\n";
            }
            header = header + " for " + ((String) (to.elementAt(i))) + " " + timestamp.toString() + "\n";
            generateID(data);
            header = header + "Message-ID: " + msgid;
            Log.Debug(header, this);
            data = header + data;
            String address;
            if (((String) (to.elementAt(i))).indexOf("<") != -1) {
                address = ((String) (to.elementAt(i))).substring(((String) (to.elementAt(i))).indexOf("<") + 1, ((String) (to.elementAt(i))).indexOf(">"));
            } else {
                address = (String) (to.elementAt(i));
            }
            Log.Debug(address, this);
            StringTokenizer tokenizer = new StringTokenizer(address);
            String username = tokenizer.nextToken("@");
            String domain = tokenizer.nextToken();
            if (SMTPServer.isMyDomain(domain)) {
                Log.Debug("Is my domain", this);
                try {
                    File f = new File(username);
                    if (!f.exists()) {
                        f.mkdir();
                    }
                    f = new File(username + File.separator + msgid + ".msg");
                    PrintStream fileout = new PrintStream(new FileOutputStream(f));
                    fileout.print(fixcrlf(data));
                    fileout.close();
                } catch (Exception e) {
                    Log.Debug("Couldn't write msg file:\n" + data, this);
                }
            } else {
                Log.Debug("Forwarding to " + domain, this);
                (new SMTPSender((String) to.elementAt(i), from, data, domain)).start();
            }
        }
    }

    /**
	 * generate a unique message id number, should be unique based on message data.
	 * according to RFC same id is ok if message is identical.
	**/
    void generateID(String data) {
        CRC32 checksum = new CRC32();
        int i = 0;
        checksum.reset();
        while ((i < data.length()) && (i < 5000)) {
            checksum.update((int) data.charAt(i));
            i++;
        }
        msgid = Long.toHexString(checksum.getValue()) + Integer.toHexString(data.hashCode());
    }

    /**
	 * Change \n to \r\n so that we don't get garbled newlines
	**/
    String fixcrlf(String data) {
        StringBuffer result = new StringBuffer();
        int in = 0;
        while (in < data.length()) {
            if (data.charAt(in) == '\n') {
                result.append("\r\n");
            } else {
                if (data.charAt(in) != '\r') {
                    result.append(data.charAt(in));
                }
            }
            in++;
        }
        return result.toString();
    }
}
