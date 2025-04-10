package org.drftpd.slave.socket;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Iterator;
import java.security.MessageDigest;
import net.sf.drftpd.FatalException;
import net.sf.drftpd.master.ConnectionManager;
import net.sf.drftpd.master.RemoteSlave;
import org.apache.log4j.Logger;
import org.apache.oro.text.GlobCompiler;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Matcher;
import socks.server.Ident;

/**
 *
 * @author  jbarrett
 */
public class SocketSlaveListener extends Thread {

    private static final Logger logger = Logger.getLogger(SocketSlaveListener.class.getName());

    private int _port;

    private ServerSocket sock;

    static ConnectionManager _conman;

    static String _pass = "";

    static void invalidSlave(String msg, Socket sock) throws IOException {
        BufferedReader _sinp = null;
        PrintWriter _sout = null;
        try {
            _sout = new PrintWriter(sock.getOutputStream(), true);
            _sinp = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            _sout.println(msg);
            logger.info("NEW< " + msg);
            String txt = SocketSlaveListener.readLine(_sinp, 30);
            String sname = "";
            String spass = "";
            String shash = "";
            try {
                String[] items = txt.split(" ");
                sname = items[1].trim();
                spass = items[2].trim();
                shash = items[3].trim();
            } catch (Exception e) {
                throw new IOException("Slave Inititalization Faailed");
            }
            String pass = sname + spass + _pass;
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.reset();
            md5.update(pass.getBytes());
            String hash = SocketSlaveListener.hash2hex(md5.digest()).toLowerCase();
            if (!hash.equals(shash)) {
                throw new IOException("Slave Inititalization Faailed");
            }
        } catch (Exception e) {
        }
        throw new IOException("Slave Inititalization Faailed");
    }

    static String hash2hex(byte[] bytes) {
        String res = "";
        for (int i = 0; i < 16; i++) {
            String hex = Integer.toHexString((int) bytes[i]);
            if (hex.length() < 2) hex = "0" + hex;
            res += hex.substring(hex.length() - 2);
        }
        return res;
    }

    static String readLine(BufferedReader _sinp, int secs) {
        int cnt = secs * 10;
        try {
            while (true) {
                while (!_sinp.ready()) {
                    if (cnt < 1) return null;
                    sleep(100);
                    cnt--;
                    if (cnt == 0) return null;
                }
                String txt = _sinp.readLine();
                logger.info("NEW> " + txt);
                return txt;
            }
        } catch (Exception e) {
            return null;
        }
    }

    /** Creates a new instance of SocketSlaveListener */
    public SocketSlaveListener(ConnectionManager conman, int port, String pass) {
        _conman = conman;
        _port = port;
        _pass = pass;
        start();
    }

    public void run() {
        try {
            sock = new ServerSocket(_port);
        } catch (Exception e) {
            throw new FatalException(e);
        }
        Socket slave;
        while (true) {
            try {
                slave = sock.accept();
            } catch (Exception e) {
                throw new FatalException(e);
            }
            InetAddress addr = slave.getInetAddress();
            logger.info("SockSlaveListener: accepting " + addr);
            Ident identObj = new Ident(slave);
            String ident;
            if (identObj.successful) {
                ident = identObj.userName;
            } else {
                ident = "";
            }
            Perl5Matcher m = new Perl5Matcher();
            String ipmask = ident + "@" + addr.getHostAddress();
            String hostmask = ident + "@" + addr.getHostName();
            logger.info("SockSlaveListener: ipmask " + ipmask);
            logger.info("SockSlaveListener: hostmask " + hostmask);
            Collection slaves = _conman.getSlaveManager().getSlaves();
            boolean match = false;
            RemoteSlave thisone = null;
            for (Iterator i = slaves.iterator(); i.hasNext(); ) {
                RemoteSlave rslave = (RemoteSlave) i.next();
                if (rslave.isAvailable()) {
                    logger.info("SockSlaveListener: online> " + rslave.getName());
                    continue;
                }
                String saddr = (String) rslave.getConfig().get("addr");
                if (saddr == null) {
                    logger.info("SockSlaveListener: noaddr> " + rslave.getName());
                    continue;
                }
                if (!saddr.equals("Dynamic")) {
                    logger.info("SockSlaveListener: static> " + rslave.getName());
                    continue;
                }
                logger.info("SockSlaveListener: testing " + rslave.getName());
                for (Iterator i2 = rslave.getMasks().iterator(); i2.hasNext(); ) {
                    String mask = (String) i2.next();
                    logger.info("SockSlaveListener: mask = " + mask);
                    Pattern p;
                    try {
                        p = new GlobCompiler().compile(mask);
                    } catch (MalformedPatternException ex) {
                        throw new RuntimeException("Invalid glob pattern: " + mask, ex);
                    }
                    if (m.matches(ipmask, p) || m.matches(hostmask, p)) {
                        match = true;
                        thisone = rslave;
                        break;
                    }
                }
                if (match) break;
            }
            if (match) {
                try {
                    SocketSlaveImpl tmp = new SocketSlaveImpl(_conman, thisone.getConfig(), slave);
                } catch (Exception e) {
                }
            } else {
                try {
                    SocketSlaveListener.invalidSlave("INITFAIL Unregistered", slave);
                } catch (Exception e) {
                }
            }
        }
    }
}
