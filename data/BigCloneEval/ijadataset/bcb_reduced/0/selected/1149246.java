package wjhk.jupload2.upload.helper;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.crypto.BadPaddingException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import wjhk.jupload2.policies.UploadPolicy;

/**
 * An implementation of {@link javax.net.ssl.X509TrustManager} which can operate
 * in different modes. If mode is {@link #NONE}, then any server certificate is
 * accepted and no certificate-based client authentication is performed. If mode
 * is SERVER, then server certificates are verified and if verification is
 * unsuccessful, a dialog is presented to the user, which allows accepting a
 * certificate temporarily or permanently. If mode is CLIENT, then
 * certificate-based client authentication is performed. Finally, there is a
 * mode STRICT, which combines both SERVER and CLIENT modes.
 * 
 * @author felfert
 */
public class InteractiveTrustManager implements X509TrustManager, CallbackHandler {

    /**
     * Mode for accepting any certificate.
     */
    public static final int NONE = 0;

    /**
     * Mode for verifying server certificate chains.
     */
    public static final int SERVER = 1;

    /**
     * Mode for using client certificates.
     */
    public static final int CLIENT = 2;

    /**
     * Mode for performing both client authentication and server cert
     * verification.
     */
    public static final int STRICT = SERVER + CLIENT;

    private UploadPolicy uploadPolicy;

    private int mode = STRICT;

    private String hostname;

    private static final String TS = ".truststore";

    private static final String TSKEY = "javax.net.ssl.trustStore";

    private static final String USERTS = System.getProperty("user.home") + File.separator + TS;

    /**
     * Absolute path of the truststore to use.
     */
    private String tsname = null;

    private String tspasswd = null;

    private TrustManagerFactory tmf = null;

    private KeyManagerFactory kmf = null;

    /**
     * The truststore for validation of server certificates
     */
    private KeyStore ts = null;

    /**
     * The keystore for client certificates.
     */
    private KeyStore ks = null;

    private String getPassword(String storename) {
        JPasswordField pwf = new JPasswordField(16);
        JLabel l = new JLabel(this.uploadPolicy.getLocalizedString("itm_prompt_pass", storename));
        l.setLabelFor(pwf);
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        p.add(l, BorderLayout.LINE_START);
        p.add(pwf, BorderLayout.LINE_END);
        int res = JOptionPane.showConfirmDialog(null, p, this.uploadPolicy.getLocalizedString("itm_title_pass", storename), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) return new String(pwf.getPassword());
        return null;
    }

    /**
     * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
     */
    public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof PasswordCallback) {
                PasswordCallback pc = (PasswordCallback) callbacks[i];
                String pw = getPassword(pc.getPrompt());
                pc.setPassword((pw == null) ? null : pw.toCharArray());
                pw = null;
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
            }
        }
    }

    /**
     * Create a new instance.
     * 
     * @param p The UploadPolicy to use for this instance.
     * @param hostname
     * @param passwd An optional password for the truststore.
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws IllegalArgumentException
     * @throws UnrecoverableKeyException
     */
    public InteractiveTrustManager(UploadPolicy p, String hostname, String passwd) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IllegalArgumentException, UnrecoverableKeyException {
        this.mode = p.getSslVerifyCert();
        this.uploadPolicy = p;
        if ((this.mode & SERVER) != 0) {
            if (null == passwd) passwd = "changeit";
            this.tsname = System.getProperty(TSKEY);
            if (null == this.tsname) {
                this.tsname = System.getProperty("java.home") + File.separator + "lib" + File.separator + "security" + File.separator + "cacerts";
                if (new File(USERTS).exists()) this.tsname = USERTS;
            }
            if (null == hostname || hostname.length() == 0) throw new IllegalArgumentException("hostname may not be null or empty.");
            this.hostname = hostname;
            if (null == this.ts) {
                this.ts = KeyStore.getInstance(KeyStore.getDefaultType());
                while (true) {
                    try {
                        FileInputStream is = new FileInputStream(this.tsname);
                        this.ts.load(is, passwd.toCharArray());
                        is.close();
                        this.tspasswd = passwd;
                        break;
                    } catch (IOException e) {
                        if (e.getMessage().equals("Keystore was tampered with, or password was incorrect")) {
                            passwd = getPassword(this.uploadPolicy.getLocalizedString("itm_tstore"));
                            if (null != passwd) continue;
                        }
                        throw new KeyStoreException("Could not load truststore");
                    }
                }
            }
            this.tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            this.tmf.init(this.ts);
        }
        if ((this.mode & CLIENT) != 0) {
            String ksname = System.getProperty("javax.net.ssl.keyStore");
            if (null == ksname) ksname = System.getProperty("user.home") + File.separator + ".keystore";
            String cpass = "changeit";
            File f = new File(ksname);
            if (!(f.exists() && f.isFile())) throw new KeyStoreException("Keystore " + ksname + " does not exist.");
            if (null == this.kmf) {
                String kstype = ksname.toLowerCase().endsWith(".p12") ? "PKCS12" : KeyStore.getDefaultType();
                this.ks = KeyStore.getInstance(kstype);
                while (true) {
                    try {
                        FileInputStream is = new FileInputStream(ksname);
                        this.ks.load(is, cpass.toCharArray());
                        is.close();
                        break;
                    } catch (IOException e) {
                        if ((e.getCause() instanceof BadPaddingException) || (e.getMessage().equals("Keystore was tampered with, or password was incorrect"))) {
                            cpass = getPassword("Keystore");
                            if (null != cpass) continue;
                        }
                        throw new KeyStoreException("Could not load keystore: " + e.getMessage());
                    }
                }
                this.kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                this.kmf.init(this.ks, cpass.toCharArray());
            }
        }
    }

    /**
     * Retrieve key managers.
     * 
     * @return The current array of key managers.
     */
    public KeyManager[] getKeyManagers() {
        return ((this.mode & CLIENT) == 0) ? null : this.kmf.getKeyManagers();
    }

    /**
     * Retrieve trust managers.
     * 
     * @return The current array of trust managers
     */
    public X509TrustManager[] getTrustManagers() {
        return new X509TrustManager[] { this };
    }

    /**
     * As this class is used on the client side only, The implementation of this
     * method does nothing.
     * 
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[],
     *      java.lang.String)
     */
    public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
    }

    /**
     * Format a DN. This method formats a DN (Distinguished Name) string as
     * returned from {@link javax.security.auth.x500.X500Principal#getName()} to
     * HTML table columns.
     * 
     * @param dn The DN to format.
     * @param cn An optional CN (Common Name) to match against the CN in the DN.
     *            If this parameter is non null and the CN, encoded in the DN
     *            does not match the CN specified, it is considered an error and
     *            the CN is printed accordingly (red).
     * @param reason A vector of error-strings. If the CN-comparison fails, an
     *            explanation is added to this vector.
     * @return A string, containing the HTML code rendering the given DN in a
     *         table.
     */
    private String formatDN(String dn, String cn, Vector<String> reason) {
        StringBuffer ret = new StringBuffer();
        StringTokenizer t = new StringTokenizer(dn, ",");
        while (t.hasMoreTokens()) {
            String tok = t.nextToken();
            while (tok.endsWith("\\")) tok += t.nextToken();
            String kv[] = tok.split("=", 2);
            if (kv.length == 2) {
                if (kv[0].equals("C")) ret.append("<tr><td>").append(this.uploadPolicy.getLocalizedString("itm_cert_C")).append("</td><td>").append(kv[1]).append("</td></tr>\n");
                if (kv[0].equals("CN")) {
                    boolean ok = true;
                    if (null != cn) ok = cn.equals(kv[1]);
                    ret.append("<tr><td>").append(this.uploadPolicy.getLocalizedString("itm_cert_CN")).append("</td><td");
                    ret.append(ok ? ">" : " class=\"err\">").append(kv[1]).append("</td></tr>\n");
                    if (!ok) reason.add(this.uploadPolicy.getLocalizedString("itm_reason_cnmatch", cn));
                }
                if (kv[0].equals("L")) ret.append("<tr><td>").append(this.uploadPolicy.getLocalizedString("itm_cert_L")).append("</td><td>").append(kv[1]).append("</td></tr>\n");
                if (kv[0].equals("ST")) ret.append("<tr><td>").append(this.uploadPolicy.getLocalizedString("itm_cert_ST")).append("</td><td>").append(kv[1]).append("</td></tr>\n");
                if (kv[0].equals("O")) ret.append("<tr><td>").append(this.uploadPolicy.getLocalizedString("itm_cert_O")).append("</td><td>").append(kv[1]).append("</td></tr>\n");
                if (kv[0].equals("OU")) ret.append("<tr><td>").append(this.uploadPolicy.getLocalizedString("itm_cert_OU")).append("</td><td>").append(kv[1]).append("</td></tr>\n");
            }
        }
        return ret.toString();
    }

    private void CertDialog(X509Certificate c) throws CertificateException {
        int i;
        boolean expired = false;
        boolean notyet = false;
        Vector<String> reason = new Vector<String>();
        reason.add(this.uploadPolicy.getLocalizedString("itm_reason_itrust"));
        try {
            c.checkValidity();
        } catch (CertificateExpiredException e1) {
            expired = true;
            reason.add(this.uploadPolicy.getLocalizedString("itm_reason_expired"));
        } catch (CertificateNotYetValidException e2) {
            notyet = true;
            reason.add(this.uploadPolicy.getLocalizedString("itm_reason_notyet"));
        }
        StringBuffer msg = new StringBuffer();
        msg.append("<html><head>");
        msg.append("<style type=\"text/css\">\n");
        msg.append("td, th, p, body { ");
        msg.append("font-family: Arial, Helvetica, sans-serif; ");
        msg.append("font-size: 12pt; ");
        Integer ii = Integer.valueOf(new JButton(".").getForeground().getRGB() & 0x00ffffff);
        msg.append("color: ").append(String.format("#%06x", ii)).append(" }\n");
        msg.append("th { text-align: left; }\n");
        msg.append("td { margin-left: 20; }\n");
        msg.append(".err { color: red; }\n");
        msg.append("</style>\n");
        msg.append("</head><body>");
        msg.append("<h3>").append(this.uploadPolicy.getLocalizedString("itm_fail_verify")).append("</h3>");
        msg.append("<h4>").append(this.uploadPolicy.getLocalizedString("itm_cert_details")).append("</h4>");
        msg.append("<table>");
        msg.append("<tr><th colspan=2>").append(this.uploadPolicy.getLocalizedString("itm_cert_subject")).append("</th></tr>");
        msg.append(formatDN(c.getSubjectX500Principal().getName(), this.hostname, reason));
        msg.append("<tr><td>").append(this.uploadPolicy.getLocalizedString("itm_cert_nbefore")).append("</td>");
        msg.append(notyet ? "<td class=\"err\">" : "<td>").append(c.getNotBefore()).append("</td></tr>\n");
        msg.append("<tr><td>").append(this.uploadPolicy.getLocalizedString("itm_cert_nafter")).append("</td>");
        msg.append(expired ? "<td class=\"err\">" : "<td>").append(c.getNotAfter()).append("</td></tr>\n");
        msg.append("<tr><td>").append(this.uploadPolicy.getLocalizedString("itm_cert_serial")).append("</td><td>");
        msg.append(c.getSerialNumber());
        msg.append("</td></tr>\n");
        msg.append("<tr><td>").append(this.uploadPolicy.getLocalizedString("itm_cert_fprint", "SHA1")).append("</td><td>");
        MessageDigest d;
        StringBuffer fp = new StringBuffer();
        try {
            d = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException("Unable to calculate certificate SHA1 fingerprint: " + e.getMessage());
        }
        byte[] sha1sum = d.digest(c.getEncoded());
        for (i = 0; i < sha1sum.length; i++) {
            if (i > 0) fp.append(":");
            fp.append(Integer.toHexString((sha1sum[i] >> 4) & 0x0f));
            fp.append(Integer.toHexString(sha1sum[i] & 0x0f));
        }
        msg.append(fp).append("</td></tr>\n");
        fp.setLength(0);
        msg.append("<tr><td>").append(this.uploadPolicy.getLocalizedString("itm_cert_fprint", "MD5")).append("</td><td>");
        try {
            d = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException("Unable to calculate certificate MD5 fingerprint: " + e.getMessage());
        }
        byte[] md5sum = d.digest(c.getEncoded());
        for (i = 0; i < md5sum.length; i++) {
            if (i > 0) fp.append(":");
            fp.append(Integer.toHexString((md5sum[i] >> 4) & 0x0f));
            fp.append(Integer.toHexString(md5sum[i] & 0x0f));
        }
        msg.append(fp).append("</td></tr>\n");
        msg.append("</table><table>");
        msg.append("<tr><th colspan=2>").append(this.uploadPolicy.getLocalizedString("itm_cert_issuer")).append("</th></tr>");
        msg.append(formatDN(c.getIssuerX500Principal().getName(), null, reason));
        msg.append("</table>");
        msg.append("<p><b>").append(this.uploadPolicy.getLocalizedString("itm_reasons")).append("</b><br><ul>");
        Iterator<String> it = reason.iterator();
        while (it.hasNext()) {
            msg.append("<li>" + it.next() + "</li>\n");
        }
        msg.append("</ul></p>");
        msg.append("<p><b>").append(this.uploadPolicy.getLocalizedString("itm_accept_prompt")).append("</b></p>");
        msg.append("</body></html>");
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        JEditorPane ep = new JEditorPane("text/html", msg.toString());
        ep.setEditable(false);
        ep.setBackground(p.getBackground());
        p.add(ep, BorderLayout.CENTER);
        String no = this.uploadPolicy.getLocalizedString("itm_accept_no");
        int ans = JOptionPane.showOptionDialog(null, p, "SSL Certificate Alert", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[] { this.uploadPolicy.getLocalizedString("itm_accept_always"), this.uploadPolicy.getLocalizedString("itm_accept_now"), no }, no);
        switch(ans) {
            case JOptionPane.CANCEL_OPTION:
            case JOptionPane.CLOSED_OPTION:
                throw new CertificateException("Server certificate rejected.");
            case JOptionPane.NO_OPTION:
            case JOptionPane.YES_OPTION:
                try {
                    this.ts.setCertificateEntry(fp.toString(), c);
                } catch (KeyStoreException e) {
                    throw new CertificateException("Unable to add certificate: " + e.getMessage());
                }
                if (ans == JOptionPane.YES_OPTION) {
                    if (null == System.getProperty(TSKEY)) this.tsname = USERTS;
                    while (true) {
                        try {
                            File f = new File(this.tsname);
                            boolean old = false;
                            if (f.exists()) {
                                if (!f.renameTo(new File(this.tsname + ".old"))) throw new IOException("Could not rename truststore");
                                old = true;
                            } else {
                                this.tspasswd = this.getPassword(this.uploadPolicy.getLocalizedString("itm_new_tstore"));
                                if (null == this.tspasswd) this.tspasswd = "changeit";
                            }
                            FileOutputStream os = new FileOutputStream(this.tsname);
                            this.ts.store(os, this.tspasswd.toCharArray());
                            os.close();
                            if (old && (!f.delete())) throw new IOException("Could not delete old truststore");
                            this.tmf.init(this.ts);
                            System.out.println("Saved cert to " + this.tsname);
                            break;
                        } catch (Exception e) {
                            if (this.tsname.equals(USERTS)) throw new CertificateException(e);
                            this.tsname = USERTS;
                        }
                    }
                }
        }
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[],
     *      java.lang.String)
     */
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if ((this.mode & SERVER) != 0) {
            if (null == chain || chain.length == 0) throw new IllegalArgumentException("Certificate chain is null or empty");
            int i;
            TrustManager[] mgrs = this.tmf.getTrustManagers();
            for (i = 0; i < mgrs.length; i++) {
                if (mgrs[i] instanceof X509TrustManager) {
                    X509TrustManager m = (X509TrustManager) (mgrs[i]);
                    try {
                        m.checkServerTrusted(chain, authType);
                        return;
                    } catch (Exception e) {
                    }
                }
            }
            CertDialog(chain[0]);
        }
    }

    /**
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    public X509Certificate[] getAcceptedIssuers() {
        System.out.println("getAcceptedIssuers");
        return new X509Certificate[0];
    }
}
