package com.knowgate.jcifs.smb;

import java.util.Vector;
import java.util.Enumeration;
import java.net.InetAddress;
import java.net.UnknownHostException;
import com.knowgate.jcifs.Config;
import com.knowgate.jcifs.UniAddress;
import com.knowgate.debug.*;

/**
 * The class represents a user's session established with an SMB/CIFS
 * server. This class is used internally to the jCIFS library however
 * applications may wish to authenticate aribrary user credentials
 * with the <tt>logon</tt> method. It is noteworthy that jCIFS does not
 * support DCE/RPC at this time and therefore does not use the NETLOGON
 * procedure. Instead, it simply performs a "tree connect" to IPC$ using
 * the supplied credentials. This is only a subset of the NETLOGON procedure
 * but is achives the same effect.

Note that it is possible to change the resource against which clients
are authenticated to be something other than <tt>IPC$</tt> using the
<tt>jcifs.smb.client.logonShare</tt> property. This can be used to
provide simple group based access control. For example, one could setup
the NTLM HTTP Filter with the <tt>jcifs.smb.client.domainController</tt>
init parameter set to the name of the server used for authentication. On
that host, create a share called JCIFSAUTH and adjust the access control
list for that share to permit only the clients that should have access to
the target website. Finally, set the <tt>jcifs.smb.client.logonShare</tt>
to JCIFSAUTH. This should restrict access to only those clients that have
access to the JCIFSAUTH share. The access control on that share can be
changed without changing init parameters or reinitializing the webapp.
 */
public final class SmbSession {

    private static final String LOGON_SHARE = Config.getProperty("jcifs.smb.client.logonShare", "IPC$");

    public static byte[] getChallenge(UniAddress dc) throws SmbException, UnknownHostException {
        SmbTransport trans = SmbTransport.getSmbTransport(dc, 0);
        trans.negotiate();
        return trans.server.encryptionKey;
    }

    /**
 * Authenticate arbitrary credentials represented by the
 * <tt>NtlmPasswordAuthentication</tt> object against the domain controller
 * specified by the <tt>UniAddress</tt> parameter. If the credentials are
 * not accepted, an <tt>SmbAuthException</tt> will be thrown. If an error
 * occurs an <tt>SmbException</tt> will be thrown. If the credentials are
 * valid, the method will return without throwing an exception. See the
 * last <a href="../../../../FAQ.html">FAQ</a> question.
 *
 * See also the <tt>jcifs.smb.client.logonShare</tt> property.
 */
    public static void logon(UniAddress dc, NtlmPasswordAuthentication auth) throws SmbException {
        SmbTransport.getSmbTransport(dc, 0).getSmbSession(auth).getSmbTree(LOGON_SHARE, null).treeConnect(null, null);
    }

    private int uid;

    private Vector trees;

    private boolean sessionSetup;

    private UniAddress address;

    private int port, localPort;

    private InetAddress localAddr;

    SmbTransport transport = SmbTransport.NULL_TRANSPORT;

    NtlmPasswordAuthentication auth;

    SmbSession(UniAddress address, int port, InetAddress localAddr, int localPort, NtlmPasswordAuthentication auth) {
        this.address = address;
        this.port = port;
        this.localAddr = localAddr;
        this.localPort = localPort;
        this.auth = auth;
        trees = new Vector();
    }

    synchronized SmbTree getSmbTree(String share, String service) {
        SmbTree t;
        if (share == null) {
            share = "IPC$";
        }
        for (Enumeration e = trees.elements(); e.hasMoreElements(); ) {
            t = (SmbTree) e.nextElement();
            if (t.matches(share, service)) {
                return t;
            }
        }
        t = new SmbTree(this, share, service);
        trees.addElement(t);
        return t;
    }

    boolean matches(NtlmPasswordAuthentication auth) {
        return this.auth == auth || this.auth.equals(auth);
    }

    synchronized SmbTransport transport() throws SmbException {
        if (transport == SmbTransport.NULL_TRANSPORT) {
            transport = SmbTransport.getSmbTransport(address, port, localAddr, localPort);
        }
        return transport;
    }

    void sendTransaction(SmbComTransaction request, SmbComTransactionResponse response) throws SmbException {
        sessionSetup(null, null);
        request.uid = uid;
        request.auth = auth;
        transport().sendTransaction(request, response);
    }

    void send(ServerMessageBlock request, ServerMessageBlock response) throws SmbException {
        if (response != null) {
            response.received = false;
        }
        sessionSetup(request, response);
        if (response != null && response.received) {
            return;
        }
        request.uid = uid;
        request.auth = auth;
        transport().send(request, response);
    }

    void sessionSetup(ServerMessageBlock andx, ServerMessageBlock andxResponse) throws SmbException {
        synchronized (transport()) {
            if (sessionSetup) {
                return;
            }
            transport.negotiate();
            if (DebugFile.trace) DebugFile.writeln("sessionSetup: accountName=" + auth.username + ",primaryDomain=" + auth.domain);
            SmbComSessionSetupAndX request = new SmbComSessionSetupAndX(this, andx);
            SmbComSessionSetupAndXResponse response = new SmbComSessionSetupAndXResponse(andxResponse);
            if (transport.isSignatureSetupRequired(auth)) {
                if (auth.hashesExternal && NtlmPasswordAuthentication.DEFAULT_PASSWORD != null) {
                    transport.getSmbSession(NtlmPasswordAuthentication.DEFAULT).getSmbTree(LOGON_SHARE, null).treeConnect(null, null);
                }
                request.digest = new SigningDigest(transport, auth);
            }
            request.auth = auth;
            transport.send(request, response);
            if (response.isLoggedInAsGuest && "GUEST".equals(auth.username)) {
                throw new SmbAuthException(NtStatus.NT_STATUS_LOGON_FAILURE);
            }
            uid = response.uid;
            sessionSetup = true;
            if (request.digest != null) {
                transport.digest = request.digest;
            }
        }
    }

    void logoff(boolean inError) {
        synchronized (transport) {
            try {
                if (sessionSetup == false) {
                    return;
                }
                for (Enumeration e = trees.elements(); e.hasMoreElements(); ) {
                    SmbTree t = (SmbTree) e.nextElement();
                    t.treeDisconnect(inError);
                }
                if (transport.server.security == ServerMessageBlock.SECURITY_SHARE) {
                    return;
                }
                if (!inError) {
                    SmbComLogoffAndX request = new SmbComLogoffAndX(null);
                    request.uid = uid;
                    try {
                        transport.send(request, null);
                    } catch (SmbException se) {
                    }
                }
                sessionSetup = false;
            } finally {
                transport = SmbTransport.NULL_TRANSPORT;
            }
        }
    }

    public String toString() {
        return "SmbSession[accountName=" + auth.username + ",primaryDomain=" + auth.domain + ",uid=" + uid + ",sessionSetup=" + sessionSetup + "]";
    }
}
