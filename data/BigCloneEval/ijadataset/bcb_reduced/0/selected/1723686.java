package certforge.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import sun.security.pkcs.PKCS10;
import sun.security.pkcs.PKCS7;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AlgorithmId;
import sun.security.x509.BasicConstraintsExtension;
import sun.security.x509.CRLDistributionPointsExtension;
import sun.security.x509.CRLExtensions;
import sun.security.x509.CRLNumberExtension;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateExtensions;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.DistributionPoint;
import sun.security.x509.ExtendedKeyUsageExtension;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNames;
import sun.security.x509.KeyUsageExtension;
import sun.security.x509.RDN;
import sun.security.x509.URIName;
import sun.security.x509.X500Name;
import sun.security.x509.X509CRLImpl;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;
import certforge.db.CertForgeDB;
import certforge.db.EmbeddedDB;
import certforge.model.CertItem;
import certforge.model.CrlEntryItem;
import certforge.model.CrlItem;
import certforge.model.RdnEntry;
import certforge.util.Base64;
import certforge.util.Bytes;
import certforge.util.Integers;
import certforge.util.Time;
import certforge.util.cert.CertKey;
import certforge.util.cert.CertMap;
import certforge.util.cert.CertUtil;
import certforge.util.servlet.Servlets;

class MessageException extends Exception {
	private static final long serialVersionUID = 1L;

	private String prefix;

	public MessageException(String message) {
		this(message, "");
	}

	public MessageException(String message, String prefix) {
		super(message);
		this.prefix = prefix;
	}

	public String getPrefix() {
		return prefix;
	}
}

abstract class Order<T> {
	boolean descending = false;
	String column = null;

	Order() {/**/}

	Order(String column) {
		this.column = column;
	}

	public void setOrder(String column) {
		if (column == null) {
			return;
		}
		if (this.column != null && this.column.equals(column)) {
			descending = !descending;
		} else {
			this.column = column;
			descending = false;
		}
	}

	abstract void sort(ArrayList<T> list);
}

class CertOrder extends Order<CertItem> {
	private static final String SESSION_ATTRIBUTE = "CERT_ORDER";

	static CertOrder getFromSession() {
		HttpSession session = Servlets.getRequest().getSession();
		CertOrder order = (CertOrder) session.getAttribute(SESSION_ATTRIBUTE);
		if (order == null) {
			order = new CertOrder();
			session.setAttribute(SESSION_ATTRIBUTE, order);
		}
		return order;
	}

	private static Comparator<CertItem> compSubject = new Comparator<CertItem>() {
		@Override
		public int compare(CertItem cert1, CertItem cert2) {
			return cert1.getShortSubject().compareTo(cert2.getShortSubject());
		}
	};

	private static Comparator<CertItem> compIssuer = new Comparator<CertItem>() {
		@Override
		public int compare(CertItem cert1, CertItem cert2) {
			return cert1.getShortIssuer().compareTo(cert2.getShortIssuer());
		}
	};

	private static Comparator<CertItem> compNotBefore = new Comparator<CertItem>() {
		@Override
		public int compare(CertItem cert1, CertItem cert2) {
			return cert1.getNotBefore().compareTo(cert2.getNotBefore());
		}
	};

	private static Comparator<CertItem> compNotAfter = new Comparator<CertItem>() {
		@Override
		public int compare(CertItem cert1, CertItem cert2) {
			return cert1.getNotAfter().compareTo(cert2.getNotAfter());
		}
	};

	private static Comparator<CertItem> compType = new Comparator<CertItem>() {
		@Override
		public int compare(CertItem certItem1, CertItem certItem2) {
			return certItem1.getTypeString().compareTo(certItem2.getTypeString());
		}
	};

	@Override
	void sort(ArrayList<CertItem> certList) {
		Comparator<CertItem> comp = null;
		if (column == null) {
			return;
		}
		switch (column) {
		case "subject":
			comp = compSubject;
			break;
		case "issuer":
			comp = compIssuer;
			break;
		case "notbefore":
			comp = compNotBefore;
			break;
		case "notafter":
			comp = compNotAfter;
			break;
		case "type":
			comp = compType;
			break;
		default:
			return;
		}
		Collections.sort(certList, descending ?
				Collections.reverseOrder(comp) : comp);
	}
}

class CrlOrder extends Order<CrlItem> {
	private static final String SESSION_ATTRIBUTE = "CRL_ORDER";

	static CrlOrder getFromSession() {
		HttpSession session = Servlets.getRequest().getSession();
		CrlOrder order = (CrlOrder) session.getAttribute(SESSION_ATTRIBUTE);
		if (order == null) {
			order = new CrlOrder();
			session.setAttribute(SESSION_ATTRIBUTE, order);
		}
		return order;
	}

	private static Comparator<CrlItem> compIssuer = new Comparator<CrlItem>() {
		@Override
		public int compare(CrlItem crlItem1, CrlItem crlItem2) {
			return crlItem1.getShortIssuer().compareTo(crlItem2.getShortIssuer());
		}
	};

	private static Comparator<CrlItem> compThisUpdate = new Comparator<CrlItem>() {
		@Override
		public int compare(CrlItem crlItem1, CrlItem crlItem2) {
			return crlItem1.getThisUpdate().compareTo(crlItem2.getThisUpdate());
		}
	};

	private static Comparator<CrlItem> compNextUpdate = new Comparator<CrlItem>() {
		@Override
		public int compare(CrlItem crlItem1, CrlItem crlItem2) {
			return crlItem1.getNextUpdate().compareTo(crlItem2.getNextUpdate());
		}
	};

	@Override
	void sort(ArrayList<CrlItem> crlList) {
		Comparator<CrlItem> comp = null;
		if (column == null) {
			return;
		}
		switch (column) {
		case "issuer":
			comp = compIssuer;
			break;
		case "thisupdate":
			comp = compThisUpdate;
			break;
		case "nextupdate":
			comp = compNextUpdate;
			break;
		default:
			return;
		}
		Collections.sort(crlList, descending ?
				Collections.reverseOrder(comp) : comp);
	}
}

class CrlEntryOrder extends Order<CrlEntryItem> {
	private static final String SESSION_ATTRIBUTE = "CRLENTRY_ORDER";

	static CrlEntryOrder getFromSession() {
		HttpSession session = Servlets.getRequest().getSession();
		CrlEntryOrder order = (CrlEntryOrder) session.getAttribute(SESSION_ATTRIBUTE);
		if (order == null) {
			order = new CrlEntryOrder();
			session.setAttribute(SESSION_ATTRIBUTE, order);
		}
		return order;
	}

	private static Comparator<CrlEntryItem> compSerial = new Comparator<CrlEntryItem>() {
		@Override
		public int compare(CrlEntryItem crlEntryItem1, CrlEntryItem crlEntryItem2) {
			return crlEntryItem1.getSerial().compareTo(crlEntryItem2.getSerial());
		}
	};

	private static Comparator<CrlEntryItem> compRevocation = new Comparator<CrlEntryItem>() {
		@Override
		public int compare(CrlEntryItem crlEntryItem1, CrlEntryItem crlEntryItem2) {
			return crlEntryItem1.getRevocation().compareTo(crlEntryItem2.getRevocation());
		}
	};

	private static Comparator<CrlEntryItem> compReason = new Comparator<CrlEntryItem>() {
		@Override
		public int compare(CrlEntryItem crlEntryItem1, CrlEntryItem crlEntryItem2) {
			return crlEntryItem1.getReason() - crlEntryItem2.getReason();
		}
	};

	@Override
	void sort(ArrayList<CrlEntryItem> crlEntryList) {
		Comparator<CrlEntryItem> comp = null;
		if (column == null) {
			return;
		}
		switch (column) {
		case "serial":
			comp = compSerial;
			break;
		case "revocation":
			comp = compRevocation;
			break;
		case "reason":
			comp = compReason;
			break;
		default:
			return;
		}
		Collections.sort(crlEntryList, descending ?
				Collections.reverseOrder(comp) : comp);
	}
}

class CertExt {
	private static final boolean DEFAULT_ENTITY_KEY_USAGE[] = {
		true, true, true, true, true, false, false, false, false,
	};

	private static final boolean DEFAULT_CA_KEY_USAGE[] = {
		true, true, true, true, true, true, true, false, false,
	};

	private static final boolean DEFAULT_EXT_KEY_USAGE[] = {
		true, true, true, true, true, true, true, true, true, true,
	};

	static final String EXT_KEY_USAGE_OIDS[] = {
		"2.5.29.37.0",			// anyExtendedKeyUsage
		"1.3.6.1.5.5.7.3.1",	// serverAuth
		"1.3.6.1.5.5.7.3.2",	// clientAuth
		"1.3.6.1.5.5.7.3.3",	// codeSigning
		"1.3.6.1.5.5.7.3.4",	// emailProtection
		"1.3.6.1.5.5.7.3.5",	// ipsecEndSystem
		"1.3.6.1.5.5.7.3.6",	// ipsecTunnel
		"1.3.6.1.5.5.7.3.7",	// ipsecUser
		"1.3.6.1.5.5.7.3.8",	// timeStamping
		"1.3.6.1.5.5.7.3.9",	// OCSPSigning
	};

	int notBefore = Time.lastMidnight(Time.now());
	int notAfter = notBefore + Time.DAY * 365;
	boolean[] usages = DEFAULT_ENTITY_KEY_USAGE;
	boolean[] extUsages = DEFAULT_EXT_KEY_USAGE;
	boolean ca = false;
	String crl = "";

	CertExt() {/**/}

	CertExt(X509Certificate cert) {
		notBefore = (int) (cert.getNotBefore().getTime() / 1000);
		notAfter = (int) (cert.getNotAfter().getTime() / 1000);
		usages = cert.getKeyUsage();
		ca = CertUtil.isCa(cert);
		if (usages == null) {
			usages = ca ? DEFAULT_CA_KEY_USAGE : DEFAULT_ENTITY_KEY_USAGE;
		}
		List<String> lstOIDs = null;
		try {
			lstOIDs = cert.getExtendedKeyUsage();
		} catch (Exception e) {/**/}
		if (lstOIDs == null) {
			extUsages = DEFAULT_EXT_KEY_USAGE;
		} else {
			extUsages = new boolean[EXT_KEY_USAGE_OIDS.length];
			for (int i = 0; i < EXT_KEY_USAGE_OIDS.length; i ++) {
				extUsages[i] = lstOIDs.contains(EXT_KEY_USAGE_OIDS[i]);
			}
		}
		CRLDistributionPointsExtension crldpe = ((X509CertImpl) cert).
				getCRLDistributionPointsExtension();
		try {
			for (Object i : (List<?>) crldpe.get(CRLDistributionPointsExtension.POINTS)) {
				GeneralNames gn = ((DistributionPoint) i).getFullName();
				for (Object j : gn.names()) {
					try {
						crl = ((URIName) ((GeneralName) j).getName()).getName();
						return;
					} catch (Exception e) {
						// continue;
					}
				}
			}
		} catch (Exception e) {/**/}
	}
}

class CrlExt {
	int thisUpdate = Time.lastMidnight(Time.now());
	int nextUpdate = thisUpdate + Time.DAY * 30;
	BigInteger crlNumber = BigInteger.ZERO;
	ArrayList<CrlEntryItem> crlEntryList = new ArrayList<>();

	CrlExt() {/**/}

	CrlExt(X509CRL crl) {
		thisUpdate = (int) (crl.getThisUpdate().getTime() / 1000);
		nextUpdate = (int) (crl.getNextUpdate().getTime() / 1000);
		try {
			crlNumber = ((X509CRLImpl) crl).getCRLNumber();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (crlNumber == null) {
			crlNumber = BigInteger.ZERO;
		}
		if (crl.getRevokedCertificates() != null) {
			for (X509CRLEntry crlEntry : crl.getRevokedCertificates()) {
				crlEntryList.add(new CrlEntryItem(crlEntry));
			}
		}
	}
}

public class CertForgeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String EMPTY_KEY_NAMES[] = {
		"CN", "GIVENNAME", "EMAILADDRESS", "OU", "O", "L", "ST", "C",
	};

	private static final String KEY_USAGE_TEXT[] = {
		"Digital Signature",
		"Non Repudation",
		"Key Encipherment",
		"Data Encipherment",
		"Key Agreement",
		"Certificate Signing",
		"CRL Signing",
		"Encipher Only",
		"Decipher Only",
	};

	private static final String EXT_KEY_USAGE_TEXT[] = {
		"Any Extended Key Usage",
		"TLS Web Server Authentication",
		"TLS Web Client Authentication",
		"Code Signing",
		"E-mail Protection",
		"IP Security End System",
		"IP Security Tunnel Termination",
		"IP Security User",
		"Timestamping",
		"OCSP Signing",
	};

	private static final String KEY_DESCS[] = {
		"Common Name", "Given Name", "Email Address",
		"Organization Unit", "Organization",
		"Locality", "State", "Country",
		"Title", "IP Address", "Street Address",
		"Domain Component", "DN Qualifier", "Surname", "Initials",
		"Generation", "User ID", "Serial Number",
	};

	private static final String KEY_NAMES[] = {
		"CN", "GIVENNAME", "EMAILADDRESS", "OU", "O", "L", "ST", "C",
		"T", "IP", "STREET", "DC", "DNQ", "SURNAME", "INITIALS",
		"GENERATION", "UID", "SERIALNUMBER",
	};

	private static LinkedHashMap<String, String> name2DescMap = new LinkedHashMap<>();

	static {
		for (int i = 0; i < KEY_DESCS.length; i ++) {
			name2DescMap.put(KEY_NAMES[i], KEY_DESCS[i]);
		}
	}

	private int keySize;
	private String keyAlg;
	private HashMap<String, String> sigAlgMap = new HashMap<>();

	@Override
	public void init() throws ServletException {
		keySize = Integers.parse(getInitParameter("keySize"), 512, 4096);
		keyAlg = getInitParameter("keyAlg");
		if (keyAlg == null) {
			keyAlg = "RSA";
		}
		String rsaSigAlg = getInitParameter("rsaSigAlg");
		if (rsaSigAlg == null) {
			rsaSigAlg = "MD5withRSA";
		}
		String dsaSigAlg = getInitParameter("dsaSigAlg");
		if (dsaSigAlg == null) {
			dsaSigAlg = "SHA1withDSA";
		}
		sigAlgMap.put("RSA", rsaSigAlg);
		sigAlgMap.put("DSA", dsaSigAlg);
	}

	@Override
	public void destroy() {
		EmbeddedDB.shutdown();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		currentServlet.set(this);
		String cmd = req.getParameter("cmd");
		if (cmd == null) {
			doCert();
			currentServlet.set(null);
			return;
		}
		switch (cmd) {
		case "cert":
			doCert();
			break;
		case "certinfo":
			doCertInfo();
			break;
		case "exportcert":
			doExportCert();
			break;
		case "deletecert":
			doDeleteCert();
			break;
		case "importcert":
			doImportCert();
			break;
		case "importx509cert":
			try {
				doImportX509Cert();
			} catch (MessageException e) {
				redirect("?cmd=importcert", e.getMessage(), "x509_");
			}
			break;
		case "importstore":
			try {
				doImportStore();
			} catch (MessageException e) {
				redirect("?cmd=importcert", e.getMessage(), "store_");
			}
			break;
		case "importpkcs7":
			try {
				doImportPkcs7();
			} catch (MessageException e) {
				redirect("?cmd=importcert", e.getMessage(), "pkcs7_");
			}
			break;
		case "importjks":
			try {
				doImportJks();
			} catch (MessageException e) {
				redirect("?cmd=importcert", e.getMessage(), "jks_");
			}
			break;
		case "update":
			doUpdate();
			break;
		case "deletekey":
			doDeleteKey();
			break;
		case "signcert":
			doSignCert();
			break;
		case "signcert2":
			try {
				doSignCert2();
			} catch (MessageException e) {
				redirect("?cmd=signcert", e.getMessage(), e.getPrefix());
			}
			break;
		case "signcert3":
			try {
				doSignCert3();
			} catch (MessageException e) {
	            setMessage(e.getMessage());
	            forward("error");
			}
			break;
		case "crl":
			doCrl();
			break;
		case "crlinfo":
			doCrlInfo();
			break;
		case "exportcrl":
			doExportCrl();
			break;
		case "deletecrl":
			doDeleteCrl();
			break;
		case "importcrl":
			doImportCrl();
			break;
		case "importx509crl":
			try {
				doImportX509Crl();
			} catch (MessageException e) {
				redirect("?cmd=importcrl", e.getMessage(), "");
			}
			break;
		case "signcrl":
			doSignCrl();
			break;
		case "signcrl2":
			try {
				doSignCrl2();
			} catch (MessageException e) {
				redirect("?cmd=signcrl", e.getMessage(), e.getPrefix());
			}
			break;
		case "signcrl3":
			try {
				doSignCrl3();
			} catch (MessageException e) {
	            setMessage(e.getMessage());
	            forward("error");
			}
			break;
		case "downloadpkcs7":
			doDownloadPkcs7();
			break;
		case "downloadjks":
			doDownloadJks();
			break;
		case "downloadstore":
			doDownloadStore();
			break;
		case "downloadcert":
			doDownloadCert();
			break;
		case "downloadcrl":
			doDownloadCrl();
			break;
		case "downloadx509cert":
			doDownloadX509Cert();
			break;
		case "downloadx509crl":
			doDownloadX509Crl();
			break;
		case "downloadpkcs10":
			doDownloadPkcs10();
			break;
		}
		currentServlet.set(null);
	}

	private static ThreadLocal<CertForgeServlet> currentServlet = new ThreadLocal<>();

	private static CertForgeServlet get() {
		return currentServlet.get();
	}

	private static void forward(String jsp) {
		try {
			Servlets.getContext().getRequestDispatcher("/WEB-INF/classes/certforge/web/" +
					jsp + ".jsp").forward(Servlets.getRequest(), Servlets.getResponse());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void redirect(String url) {
		try {
			Servlets.getResponse().sendRedirect(url);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void redirect(String url, String message, String prefix) {
		try {
			redirect(url + (url.indexOf('?') < 0 ? '?' : '&') +
					prefix + "message=" + URLEncoder.encode(message, "UTF-8"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String getString(String key) {
		return Servlets.getRequest().getParameter(key);
	}

	private static int getInt(String key) {
		return Integers.parse(getString(key));
	}

	private static boolean getBoolean(String key) {
		String value = getString(key);
		return value != null && !value.equals("false");
	}

	private static byte[] getBytes(String key) {
		String value = getString(key);
		try {
			return value == null ? null : value.getBytes("ISO-8859-1");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void put(String key, Object value) {
		Servlets.getRequest().setAttribute(key, value);
	}

	private static void getAndPut(String... keys) {
		for (String key : keys) {
			String value = getString(key);
			put(key, value == null ? "" : value);
		}
	}

	private static void setMessage(String message) {
		setMessage(message, "");
	}

	private static void setMessage(String message, String prefix) {
		String message_ = getString(prefix + "message");
		put(prefix + "message", message_ == null ? message : message_);
	}

	private static void doCert() {
		ArrayList<CertItem> certList = CertForgeDB.getCertList();
		String cmd2 = getString("cmd2");
		if ("all".equals(cmd2)) {
			for (CertItem certItem : certList) {
				certItem.setSelected(true);
			}
		} else if ("none".equals(cmd2)) {
			for (CertItem certItem : certList) {
				certItem.setSelected(false);
			}
		}
		CertOrder order = CertOrder.getFromSession();
		order.setOrder(getString("order"));
		order.sort(certList);
		put("certList", certList);
		forward("cert/Cert");
	}

	private static String encodePem(byte[] encoded, String type) {
		String b64 = Base64.encode(encoded);
		StringBuilder sb = new StringBuilder();
		sb.append("-----BEGIN " + type + "-----\n");
		int b64Len = b64.length();
		for (int i = 0; i < b64Len; i += 64) {
			sb.append(b64.substring(i, Math.min(i + 64, b64Len)) + "\n");
		}
		sb.append("-----END " + type + "-----\n");
		return sb.toString();
	}

	private static void doCertInfo() {
		int id = getInt("id");
		CertItem certItem = CertForgeDB.getCert(id);
		if (certItem == null) {
			setMessage("No Such Certificate: [" + id + "]");
			forward("error");
			return;
		}
		put("BR", "\n");
		put("id", "" + id);
		X509Certificate cert = certItem.getCertificate();
		put("subject", CertUtil.fullDn(cert.
				getSubjectX500Principal().getEncoded()));
		put("issuer", CertUtil.fullDn(cert.
				getIssuerX500Principal().getEncoded()));
		put("sn", cert.getSerialNumber());
		CertExt certExt = new CertExt(cert);
		put("notBefore", Time.toString(certExt.notBefore));
		put("notAfter", Time.toString(certExt.notAfter));
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < KEY_USAGE_TEXT.length; i ++) {
			if (certExt.usages[i]) {
				sb.append(KEY_USAGE_TEXT[i] + "\n");
			}
		}
		put("usages", sb.length() == 0 ? "<Information Not Available>" :
				sb.substring(0, sb.length() - 1));
		sb = new StringBuilder();
		for (int i = 0; i < EXT_KEY_USAGE_TEXT.length; i ++) {
			if (certExt.extUsages[i]) {
				sb.append(EXT_KEY_USAGE_TEXT[i] + "\n");
			}
		}
		put("extUsages", sb.length() == 0 ? "<Information Not Available>" :
				sb.substring(0, sb.length() - 1));
		put("constraints", CertUtil.isCa(cert) ?
				"CA Certificate" : "End Entity Certificate");
		put("crl", certExt.crl.isEmpty() ?
				"<No CRL Distribution Points>" : certExt.crl);
		try {
			put("x509", Base64.encode(cert.getEncoded()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		PrivateKey key = certItem.getPrivateKey();
		if (key == null) {
			put("pkcs8", "");
			put("openSslKey", "");
		} else {
			put("pkcs8", Base64.encode(key.getEncoded()));
			put("openSslKey", encodePem(CertUtil.
					encodeOpenSSLKey(key), "RSA PRIVATE KEY"));
		}
		put("certSigner", Boolean.valueOf(certItem.isCertSigner()));
		put("crlSigner", Boolean.valueOf(certItem.isCrlSigner()));
		forward("cert/CertInfo");
	}

	private static void doExportCert() {
		Enumeration<?> en = Servlets.getRequest().getParameterNames();
		StringBuilder sbIds = new StringBuilder();
		StringBuilder sbSubjects = new StringBuilder();
		while (en.hasMoreElements()) {
			String key = (String) en.nextElement();
			if (key.startsWith("selected_")) {
				int id = getSuffixInt(key);
				CertItem certItem = CertForgeDB.getCert(id);
				if (certItem != null) {
					sbIds.append(id + ",");
					sbSubjects.append(certItem.getShortSubject() + "\n");
				}
			}
		}
		if (sbIds.length() == 0) {
			redirect("?cmd=cert");
			return;
		}
		put("BR", "\n");
		put("ids", sbIds.substring(0, sbIds.length() - 1));
		put("subjects", sbSubjects.substring(0, sbSubjects.length() - 1));
		if ("delete".equals(getString("cmd2"))) {
			forward("cert/DeleteCert");
		} else {
			forward("cert/ExportCert");
		}
	}

	private static void doDeleteCert() {
		String ids = getString("id");
		if (ids != null) {
			for (String id : ids.split(",")) {
				CertForgeDB.deleteCert(Integers.parse(id));
			}
		}
		redirect("?cmd=cert");
	}

	private static void doImportCert() {
		setMessage("Import from an X509 Certificate", "x509_");
		setMessage("Import from a Key-Store", "store_");
		setMessage("Import from a PKCS7 File", "pkcs7_");
		setMessage("Import from a JKS File", "jks_");
		forward("cert/ImportCert");
	}

	private static byte[] getUploadOrPaste(String suffix) throws MessageException {
		return getUploadOrPaste(suffix, "X509 Certificate");
	}

	private static byte[] getUploadOrPaste(String suffix,
			String type) throws MessageException {
		byte[] uploadCert = getBytes("upload" + suffix);
		String pasteCert = getString("paste" + suffix);
		if (uploadCert == null) {
			if (pasteCert == null || pasteCert.isEmpty()) {
				throw new MessageException(type + " Required");
			}
			uploadCert = pasteCert.getBytes();
		}
		return uploadCert;
	}

	private static X509Certificate parseCert(byte[] uploadCert)
			throws MessageException {
		X509Certificate cert = CertUtil.parseCert(uploadCert);
		if (cert == null) {
			cert = CertUtil.parseCert(Base64.decode(new String(uploadCert)));
		}
		if (cert == null) {
			throw new MessageException("Invalid X509 Certificate");
		}
		return cert;
	}

	private static void doImportX509Cert() throws MessageException {
		X509Certificate cert = parseCert(getUploadOrPaste("X509"));
		CertForgeDB.addCert(cert, null);
		redirect("?cmd=cert");
	}

	private static void doImportStore() throws MessageException {
		CertKey certKey = getKeyStore("");
		X509Certificate[] certChain = certKey.getCertificateChain();
		CertForgeDB.addCert(certChain[0], certKey.getKey());
		for (int i = 1; i < certChain.length; i ++) {
			CertForgeDB.addCert(certChain[i], null);
		}
		redirect("?cmd=cert");
	}

	private static void doImportPkcs7() throws MessageException {
		byte[] uploadPkcs7 = getBytes("uploadPkcs7");
		if (uploadPkcs7 == null) {
			throw new MessageException("PKCS7 File Required");
		}
		PKCS7 pkcs7;
		try {
			pkcs7 = new PKCS7(uploadPkcs7);
		} catch (Exception e) {
			throw new MessageException("Invalid PKCS7 File");
		}
		for (X509Certificate cert : pkcs7.getCertificates()) {
			CertForgeDB.addCert(cert, null);
		}
		redirect("?cmd=cert");
	}

	private static void doImportJks() throws MessageException {
		byte[] uploadJks = getBytes("uploadJks");
		if (uploadJks == null) {
			throw new MessageException("JKS File Required");
		}
		String password = getString("password");
		password = (password == null ? "" : password);
		CertMap certMap = new CertMap();
		try {
			KeyStore jks = KeyStore.getInstance("JKS");
			jks.load(new ByteArrayInputStream(uploadJks),
					password.toCharArray());
			certMap.importJks(jks);
		} catch (Exception e) {
			throw new MessageException("Invalid JKS File or Password");
		}
		for (X509Certificate cert : certMap.values()) {
			CertForgeDB.addCert(cert, null);
		}
		redirect("?cmd=cert");
	}

	private static void doUpdate() {
		int id = getInt("id");
		byte[] uploadCert;
		try {
			uploadCert = getUploadOrPaste("X509");
		} catch (MessageException e) {
			put("id", "" + id);
			setMessage("Import from X509 Certificate");
			forward("cert/UpdateCert");
			return;
		}
		X509Certificate cert;
		try {
			cert = parseCert(uploadCert);
		} catch (MessageException e) {
			redirect("?cmd=update&id=" + id, e.getMessage(), "");
			return;
		}
		CertItem certItem = CertForgeDB.getCert(id);
		if (certItem == null || certItem.getPrivateKey() == null) {
			setMessage("No Such Certificate or Private Key: [" + id + "]");
			forward("error");
			return;
		}
		if (CertUtil.isKeyPair(cert.getPublicKey(),
				certItem.getPrivateKey())) {
			CertForgeDB.updateCert(id, cert, certItem.getPrivateKey());
			redirect("?cmd=certinfo&id=" + id);
		} else {
			setMessage("Key-Pair not Match");
			put("id", "" + id);
			forward("cert/UpdateCert");
		}
	}

	private static void doDeleteKey() {
		int id = getInt("id");
		CertForgeDB.deleteKey(id);
		redirect("?cmd=certinfo&id=" + id);
	}

	private static void doSignCert() {
		setMessage("Select the Subject Certificate ...");
		setMessage("Select the Signer Certificate ...", "signer_");
		ArrayList<CertItem> certList = CertForgeDB.getCertList();
		ArrayList<CertItem> signerCertList = new ArrayList<>();
		for (CertItem certItem : certList) {
			if (certItem.isCertSigner()) {
				signerCertList.add(certItem);
			}
		}
		put("certList", certList);
		put("selfSigned", Boolean.TRUE);
		put("signerCertList", signerCertList);
		getAndPut("reissue", "signer");
		forward("cert/SignCert");
	}

	private static CertKey getKeyStore(String prefix) throws MessageException {
		byte[] uploadStore = getBytes(prefix + "uploadStore");
		if (uploadStore == null) {
			throw new MessageException("Key-Store File Required", prefix);
		}
		String password = getString(prefix + "password");
		String storeType = getString(prefix + "storeType");
		CertKey certKey;
		try {
			KeyStore keyStore = KeyStore.getInstance(storeType);
			keyStore.load(new ByteArrayInputStream(uploadStore),
					password.toCharArray());
			certKey = new CertKey(keyStore, password);
		} catch (Exception e) {
			throw new MessageException("Invalid Key-Store or Password", prefix);
		}
		if (certKey.getCertificateChain().length == 0) {
			throw new MessageException("No Certificate in Key-Store", prefix);
		}
		return certKey;
	}

	private static byte[] getEncodedSigner(PrivateKey[] privateKey)
			throws MessageException {
		String signerType = getString("signerType");

		if (signerType == null) {
			throw new MessageException("Invalid Signer Type", "signer_");
		}
		switch (signerType) {
		case "selfSigned":
			return null;
		case "store":
			CertKey certKey = getKeyStore("signer_");
			if (certKey.getCertificateChain().length == 0) {
				throw new MessageException("No Certificate in Key-Store", "signer_");
			}
			if (privateKey != null && privateKey.length > 0) {
				privateKey[0] = certKey.getKey();
			}
			return certKey.getCertificateChain()[0].
					getSubjectX500Principal().getEncoded();
		case "db":
			int signer = getInt("signer");
			if (signer == 0) {
				throw new MessageException("Please Choose an Signer Certificate", "signer_");
			}
			CertItem certItem = CertForgeDB.getCert(signer);
			if (certItem == null || certItem.getPrivateKey() == null) {
				throw new MessageException("No Such Signer Certificate", "signer_");
			}
			if (privateKey != null && privateKey.length > 0) {
				privateKey[0] = certItem.getPrivateKey();
			}
			certItem.isCertSigner();
			return certItem.getCertificate().
					getSubjectX500Principal().getEncoded();
		default:
			throw new MessageException("Invalid Signer Type", "signer_");
		}
	}

	private static X500Name getSubjectDn(X509Certificate cert) {
		try {
			return new X500Name(cert.getSubjectX500Principal().getEncoded());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void doSignCert2() throws MessageException {
		boolean save = getBoolean("save");
		boolean saveKey = getBoolean("saveKey");
		String subjectType = getString("subjectType");
		String pasteKey = getString("pasteKey");

		if (subjectType == null) {
			throw new MessageException("Invalid Subject Type");
		}
		PublicKey publicKey = null;
		PrivateKey privateKey = null;
		if (pasteKey != null && !pasteKey.isEmpty()) {
			byte[] keyData = Base64.decode(pasteKey);
			privateKey = CertUtil.parsePrivateKey(keyData);
			if (privateKey == null) {
				privateKey = CertUtil.parseOpenSSLKey(keyData);
			}
			// RSAPrivateCrtKey required
			publicKey = CertUtil.getPublicKey(privateKey);
		}

		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance(get().keyAlg);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		kpg.initialize(get().keySize);
		int id = save ? 0 : -1;
		CertExt certExt = new CertExt();
		X500Name subjectDn = null;
		switch (subjectType) {
		case "new":
			if (publicKey == null) {
				KeyPair keyPair = kpg.generateKeyPair();
				publicKey = keyPair.getPublic();
				privateKey = keyPair.getPrivate();
			}
			break;
		case "exist":
			byte[] uploadCert = getUploadOrPaste("Cert", "Request or Certificate");
			if ("PKCS10".equals(getString("fileType"))) {
				PKCS10 request;
				try {
					request = new PKCS10(uploadCert);
				} catch (Exception e) {
					try {
						request = new PKCS10(Base64.decode(new String(uploadCert)));
					} catch (Exception e2) {
						throw new MessageException("Invalid PKCS10 Request");
					}
				}
				subjectDn = request.getSubjectName();
				if (publicKey == null) {
					publicKey = request.getSubjectPublicKeyInfo();
				}
			} else {
				X509Certificate cert = parseCert(uploadCert);
				subjectDn = getSubjectDn(cert);
				certExt = new CertExt(cert);
				if (publicKey == null) {
					publicKey = cert.getPublicKey();
				}
			}
			if (getBoolean("genNewKey")) {
				KeyPair keyPair = kpg.generateKeyPair();
				publicKey = keyPair.getPublic();
				privateKey = keyPair.getPrivate();
			}
			break;
		case "store":
			CertKey certKey = getKeyStore("subject_");
			X509Certificate cert = certKey.getCertificateChain()[0];
			subjectDn = getSubjectDn(cert);
			certExt = new CertExt(cert);
			if (publicKey == null) {
				publicKey = cert.getPublicKey();
				privateKey = certKey.getKey();
			}
			break;
		case "db":
			int reissue = getInt("reissue");
			if (reissue == 0) {
				throw new MessageException("No Certificate Selected");
			}
			if (getBoolean("replace")) {
				id = reissue;
			}
			CertItem certItem = CertForgeDB.getCert(reissue);
			cert = certItem.getCertificate();
			subjectDn = getSubjectDn(cert);
			certExt = new CertExt(cert);
			if (publicKey == null) {
				if (getBoolean("retainKey")) {
					publicKey = cert.getPublicKey();
					privateKey = certItem.getPrivateKey();
				} else {
					KeyPair keyPair = kpg.generateKeyPair();
					publicKey = keyPair.getPublic();
					privateKey = keyPair.getPrivate();
				}
			}
			break;
		default:
			throw new MessageException("Invalid Subject Type");
		}

		ArrayList<RdnEntry> rdnList = new ArrayList<>();
		if (subjectDn == null) {
			for (int i = 0; i < EMPTY_KEY_NAMES.length; i ++) {
				rdnList.add(new RdnEntry(EMPTY_KEY_NAMES[i], ""));
			}
		} else {
			List<RDN> rdns = subjectDn.rdns();
			for (int i = rdns.size() - 1; i >= 0; i --) {
				String[] keyValue = rdns.get(i).toString().split("=", 2);
				rdnList.add(new RdnEntry(keyValue[0], keyValue[1]));
			}
		}
		byte[] snBytes = Bytes.random(16);
		snBytes[0] &= 0x7f;
		snBytes[0] |= 0x40;
		PrivateKey[] privateKey_ = new PrivateKey[1];
		byte[] encodedSigner = getEncodedSigner(privateKey_);

		put("id", "" + id);
		put("sn", new BigInteger(snBytes).toString());
		put("publicKey", publicKey == null ? "" :
				Base64.encode(publicKey.getEncoded()));
		put("privateKey", privateKey == null ? "" :
				Base64.encode(privateKey.getEncoded()));
		put("signerKey", encodedSigner == null ? "" :
				Base64.encode(privateKey_[0].getEncoded()));
		put("encodedSigner", encodedSigner == null ? "" :
				Base64.encode(encodedSigner));
		put("notBefore", Time.toDateString(certExt.notBefore));
		put("notAfter", Time.toDateString(certExt.notAfter));
		put("ca", "" + certExt.ca);
		put("crl", certExt.crl);
		put("saveKey", "" + saveKey);
		forwardSignCert2(rdnList, certExt.usages, certExt.extUsages);
	}

	private static int getSuffixInt(String key) {
		return getSuffixInt(key, 1024);
	}

	private static int getSuffixInt(String key, int length) {
		return Integers.parse(key.split("_", 2)[1], 0, length - 1);
	}

	private static ArrayList<RdnEntry> getEffectiveRdnList(
			ArrayList<RdnEntry> rdnList) {
		ArrayList<RdnEntry> effectiveRdnList = new ArrayList<>();
		for (RdnEntry entry : rdnList) {
			if (entry.getValue() != null && !entry.getValue().isEmpty()) {
				effectiveRdnList.add(entry);
			}
		}
		return effectiveRdnList;
	}

	private static void forwardSignCert2(ArrayList<RdnEntry> rdnList,
			boolean[] usages, boolean[] extUsages) {
		put("rdnList", rdnList);
		put("effectiveRdnList", getEffectiveRdnList(rdnList));
		put("name2Desc", name2DescMap);
		String encodedSigner = (String) Servlets.
				getRequest().getAttribute("encodedSigner");
		if (encodedSigner.isEmpty()) {
			put("shortIssuer", "<Same as Subject>");
		} else {
			put("shortIssuer", CertUtil.shortDn(Base64.decode(encodedSigner)));
		}
		put("usages", usages);
		put("extUsages", extUsages);
		put("KEY_USAGE_TEXT", KEY_USAGE_TEXT);
		put("EXT_KEY_USAGE_TEXT", EXT_KEY_USAGE_TEXT);
		forward("cert/SignCert2");
	}

	private static void doSignCert3() throws MessageException {
		// 1 Collect RDN Info
		// 1.1 Default Values
		int max = -1, insert = -1, delete = -1, up = -1, down = -1;
		HashMap<Integer, String> keyMap = new HashMap<>();
		HashMap<Integer, String> valueMap = new HashMap<>();
		boolean[] usages = new boolean[KEY_USAGE_TEXT.length];
		boolean[] extUsages = new boolean[EXT_KEY_USAGE_TEXT.length];
		Arrays.fill(usages, false);
		Arrays.fill(extUsages, false);

		// 1.2 RDN and related actions
		Enumeration<?> en = Servlets.getRequest().getParameterNames();
		while (en.hasMoreElements()) {
			String key = (String) en.nextElement();
			if (Boolean.FALSE.booleanValue()) { //
			} else if (key.startsWith("key_")) {
				int i = getSuffixInt(key);
				max = Math.max(max, i);
				keyMap.put(Integer.valueOf(i), getString(key));
			} else if (key.startsWith("value_")) {
				valueMap.put(Integer.valueOf(getSuffixInt(key)), getString(key));
			} else if (key.startsWith("insert_")) {
				insert = getSuffixInt(key);
			} else if (key.startsWith("delete_")) {
				delete = getSuffixInt(key);
			} else if (key.startsWith("up_")) {
				up = getSuffixInt(key);
			} else if (key.startsWith("down_")) {
				down = getSuffixInt(key);
			} else if (key.startsWith("usage_")) {
				usages[getSuffixInt(key, usages.length)] = true;
			} else if (key.startsWith("extUsage_")) {
				extUsages[getSuffixInt(key, extUsages.length)] = true;
			}
		}

		// 1.3 Generate RDNs
		ArrayList<RdnEntry> rdnList = new ArrayList<>();
		for (int i = 0; i <= max; i ++) {
			Integer ii = Integer.valueOf(i);
			rdnList.add(new RdnEntry(keyMap.get(ii), valueMap.get(ii)));
		}

		// 1.4 Insert/Delete/Move Up/Move Down
		if (insert >= 0 && insert <= rdnList.size()) {
			rdnList.add(insert, new RdnEntry("DC", ""));
		}
		if (delete >= 0 && delete < rdnList.size()) {
			rdnList.remove(delete);
		}
		if (up > 0 && up < rdnList.size()) {
			Collections.swap(rdnList, up - 1, up);
		}
		if (down >= 0 && down < rdnList.size() - 1) {
			Collections.swap(rdnList, down, down + 1);
		}

		// 1.5 Stay if not Submitted
		ArrayList<RdnEntry> effectiveRdnList = getEffectiveRdnList(rdnList);
		if (!getBoolean("next") || effectiveRdnList.isEmpty()) {
			getAndPut("id", "sn", "publicKey", "privateKey", "signerKey",
					"encodedSigner", "notBefore", "notAfter", "ca", "crl",
					"saveKey");
			forwardSignCert2(rdnList, usages, extUsages);
			return;
		}

		// 2 Parse Form
		int id = getInt("id");
		// 2.1 SN
		BigInteger sn;
		try {
			sn = new BigInteger(getString("sn"));
		} catch (Exception e) {
            throw new MessageException(e.getMessage());
		}
		// 2.2 Subject DN
		ArrayList<RDN> rdns = new ArrayList<>();
		try {
			for (RdnEntry entry : effectiveRdnList) {
				rdns.add(0, new RDN(entry.getKey() + "=" + entry.getValue()));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		X500Name subject;
		try {
			subject = new X500Name(rdns.toArray(new RDN[0]));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// 2.3 Subject Public Key
		String encoded = getString("publicKey");
		if (encoded == null || encoded.isEmpty()) {
			throw new MessageException("Invalid Subject Public Key");
		}
		PublicKey publicKey = CertUtil.parsePublicKey(Base64.decode(encoded));
		if (publicKey == null) {
            throw new MessageException("Invalid Subject Public Key");
		}
		// 2.4 Subject Private Key
		PrivateKey privateKey = null;
		encoded = getString("privateKey");
		if (encoded != null && !encoded.isEmpty()) {
			privateKey = CertUtil.parsePrivateKey(Base64.decode(encoded));
			if (privateKey == null) {
				throw new MessageException("Invalid Subject Private Key");
			}
		}
		// 2.5 Signer Private Key, Algorithm
		encoded = getString("signerKey");
		PrivateKey signerKey;
		if (privateKey != null && (encoded == null || encoded.isEmpty())) {
			signerKey = privateKey;
		} else {
			signerKey = CertUtil.parsePrivateKey(Base64.decode(encoded));
			if (signerKey == null) {
				throw new MessageException("Invalid Signer Private Key");
			}
		}
		String sigAlg = get().sigAlgMap.get(signerKey.getAlgorithm());
		// 2.6 Issuer DN
		X500Name issuer;
		encoded = getString("encodedSigner");
		if (encoded == null || encoded.isEmpty()) {
			issuer = subject;
		} else {
			try {
				issuer = new X500Name(Base64.decode(encoded));
			} catch (Exception e) {
				throw new MessageException(e.getMessage());
			}
		}
		// 2.7 Not Before, Not After
		encoded = getString("notBefore");
		if (encoded == null || encoded.isEmpty()) {
			throw new MessageException("Empty \"Not Before\" Date");
		}
		int notBefore = Time.parse(encoded, null);
		encoded = getString("notAfter");
		if (encoded == null || encoded.isEmpty()) {
			throw new MessageException("Empty \"Not After\" Date");
		}
		int notAfter = Time.parse(encoded, null);
		// 2.8 Extensions
		boolean ca = getBoolean("ca");
		String crl = getString("crl");

		// 3 Sign Certificate
		X509CertInfo certInfo = new X509CertInfo();
		try {
			// 3.1 Basic
			certInfo.set(X509CertInfo.VERSION, new CertificateVersion(2));
			certInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
			certInfo.set(X509CertInfo.ALGORITHM_ID,
					new CertificateAlgorithmId(AlgorithmId.get(sigAlg)));
			certInfo.set(X509CertInfo.ISSUER, new CertificateIssuerName(issuer));
			certInfo.set(X509CertInfo.VALIDITY, new CertificateValidity(
					new Date(1000L * notBefore), new Date(1000L * notAfter)));
			certInfo.set(X509CertInfo.SUBJECT, new CertificateSubjectName(subject));
			certInfo.set(X509CertInfo.KEY, new CertificateX509Key(publicKey));
			// 3.2 Extensions
			CertificateExtensions certExt = new CertificateExtensions();
			certExt.set(BasicConstraintsExtension.NAME,
					new BasicConstraintsExtension(ca, ca ? -1 : 0));
			certExt.set(KeyUsageExtension.NAME, new KeyUsageExtension(usages));
			Vector<ObjectIdentifier> lstOIDs = new Vector<>();
			for (int i = 0; i < extUsages.length; i ++) {
				if (extUsages[i]) {
					lstOIDs.add(new ObjectIdentifier(CertExt.EXT_KEY_USAGE_OIDS[i]));
				}
			}
			if (!lstOIDs.isEmpty()) {
				certExt.set(ExtendedKeyUsageExtension.NAME, new ExtendedKeyUsageExtension(lstOIDs));
			}
			if (crl != null && !crl.isEmpty()) {
				GeneralNames gn = new GeneralNames();
				URIName uriCrl = new URIName(crl);
				gn.add(new GeneralName(uriCrl));
				ArrayList<DistributionPoint> lstDistributionPoint = new ArrayList<>();
				lstDistributionPoint.add(new DistributionPoint(gn, null, null));
				certExt.set(CRLDistributionPointsExtension.NAME,
						new CRLDistributionPointsExtension(lstDistributionPoint));
			}
			certInfo.set(X509CertInfo.EXTENSIONS, certExt);
			// 3.3 Signature
			X509CertImpl cert = new X509CertImpl(certInfo);
			cert.sign(signerKey, sigAlg);

			// 4 Save data
			boolean saveKey = privateKey != null && getBoolean("saveKey");
			if (id == 0) {
				CertForgeDB.addCert(cert, saveKey ? privateKey : null);
			} else if (id > 0) {
				CertForgeDB.updateCert(id, cert, saveKey ? privateKey : null);
			}

			// 5 Forward to JSP
			put("x509", Base64.encode(cert.getEncoded()));
			if (privateKey == null) {
				forward("cert/SignCert3a");
			} else {
				Signature signature = Signature.getInstance(sigAlg);
				signature.initSign(signerKey);
				PKCS10 request = new PKCS10(publicKey);
				request.encodeAndSign(subject, signature);
				put("pkcs10", Base64.encode(request.getEncoded()));
				put("pkcs8", Base64.encode(privateKey.getEncoded()));
				put("openSslKey", encodePem(CertUtil.
						encodeOpenSSLKey(privateKey), "RSA PRIVATE KEY"));
				forward("cert/SignCert3b");
			}
		} catch (Exception e) {
			throw new MessageException(e.getMessage());
		}
	}

	private static void doCrl() {
		ArrayList<CrlItem> crlList = CertForgeDB.getCrlList();
		String cmd2 = getString("cmd2");
		if ("all".equals(cmd2)) {
			for (CrlItem crlItem : crlList) {
				crlItem.setSelected(true);
			}
		} else if ("none".equals(cmd2)) {
			for (CrlItem crlItem : crlList) {
				crlItem.setSelected(false);
			}
		}
		CrlOrder order = CrlOrder.getFromSession();
		order.setOrder(getString("order"));
		order.sort(crlList);
		put("crlList", crlList);
		forward("crl/Crl");
	}

	private static void doCrlInfo() {
		int id = getInt("id");
		CrlItem crlItem = CertForgeDB.getCrl(id);
		if (crlItem == null) {
			setMessage("No Such CRL: [" + id + "]");
			forward("error");
			return;
		}
		put("BR", "\n");
		put("id", "" + id);
		X509CRL crl = crlItem.getCrl();
		ArrayList<CrlEntryItem> crlEntryList = new ArrayList<>();
		Set<? extends X509CRLEntry> crlEntrySet = crl.getRevokedCertificates();
		if (crlEntrySet != null) {
			for (X509CRLEntry crlEntry : crlEntrySet) {
				crlEntryList.add(new CrlEntryItem(crlEntry));
			}
		}
		put("crlEntryList", crlEntryList);
		put("issuer", CertUtil.fullDn(crl.
				getIssuerX500Principal().getEncoded()));
		CrlExt crlExt = new CrlExt(crl);
		put("thisUpdate", Time.toString(crlExt.thisUpdate));
		put("nextUpdate", Time.toString(crlExt.nextUpdate));
		put("crlNumber", crlExt.crlNumber);
		try {
			put("x509", Base64.encode(crl.getEncoded()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		forward("crl/CrlInfo");
	}

	private static void doExportCrl() {
		Enumeration<?> en = Servlets.getRequest().getParameterNames();
		StringBuilder sbIds = new StringBuilder();
		StringBuilder sbIssuers = new StringBuilder();
		while (en.hasMoreElements()) {
			String key = (String) en.nextElement();
			if (key.startsWith("selected_")) {
				int id = getSuffixInt(key);
				CrlItem crlItem = CertForgeDB.getCrl(id);
				if (crlItem != null) {
					sbIds.append(id + ",");
					sbIssuers.append(crlItem.getShortIssuer() + "\n");
				}
			}
		}
		if (sbIds.length() == 0) {
			redirect("?cmd=cert");
			return;
		}
		put("BR", "\n");
		put("ids", sbIds.substring(0, sbIds.length() - 1));
		put("issuers", sbIssuers.substring(0, sbIssuers.length() - 1));
		forward("crl/DeleteCrl");
	}

	private static void doDeleteCrl() {
		String ids = getString("id");
		if (ids != null) {
			for (String id : ids.split(",")) {
				CertForgeDB.deleteCrl(Integers.parse(id));
			}
		}
		redirect("?cmd=crl");
	}

	private static void doImportCrl() {
		setMessage("Import from an X509 CRL");
		forward("crl/ImportCrl");
	}

	private static X509CRL parseCrl(String suffix) throws MessageException {
		byte[] uploadCrl = getUploadOrPaste(suffix);
		X509CRL crl = CertUtil.parseCrl(uploadCrl);
		if (crl == null) {
			crl = CertUtil.parseCrl(Base64.decode(new String(uploadCrl)));
		}
		if (crl == null) {
			throw new MessageException("Invalid X509 CRL");
		}
		return crl;
	}

	private static void doImportX509Crl() throws MessageException {
		CertForgeDB.addCrl(parseCrl("X509"));
		redirect("?cmd=crl");
	}

	private static void doSignCrl() {
		setMessage("Select the Subject CRL ...");
		setMessage("Select the Signer Certificate ...", "signer_");
		put("selfSigned", Boolean.FALSE);
		put("crlList", CertForgeDB.getCrlList());
		ArrayList<CertItem> certList = CertForgeDB.getCertList();
		ArrayList<CertItem> signerCertList = new ArrayList<>();
		for (CertItem certItem : certList) {
			if (certItem.isCrlSigner()) {
				signerCertList.add(certItem);
			}
		}
		put("signerCertList", signerCertList);
		getAndPut("reissue", "signer");
		forward("crl/SignCrl");
	}

	private static void doSignCrl2() throws MessageException {
		boolean save = getBoolean("save");
		String subjectType = getString("subjectType");

		if (subjectType == null) {
			throw new MessageException("Invalid Subject Type");
		}
		int id = save ? 0 : -1;
		CrlExt crlExt = new CrlExt();
		switch (subjectType) {
		case "new":
			break;
		case "exist":
			crlExt = new CrlExt(parseCrl("Crl"));
			break;
		case "db":
			int reissue = getInt("reissue");
			if (reissue == 0) {
				throw new MessageException("No Certificate Selected");
			}
			if (getBoolean("replace")) {
				id = reissue;
			}
			CrlItem certItem = CertForgeDB.getCrl(reissue);
			X509CRL crl = certItem.getCrl();
			crlExt = new CrlExt(crl);
			break;
		default:
			throw new MessageException("Invalid Subject Type");
		}

		PrivateKey[] privateKey_ = new PrivateKey[1];
		byte[] encodedSigner = getEncodedSigner(privateKey_);
		if (encodedSigner == null) {
			throw new MessageException("Invalid Signer", "signer_");
		}

		put("id", "" + id);
		put("signerKey", Base64.encode(privateKey_[0].getEncoded()));
		put("encodedSigner", Base64.encode(encodedSigner));
		put("thisUpdate", Time.toDateString(crlExt.thisUpdate));
		put("nextUpdate", Time.toDateString(crlExt.nextUpdate));
		put("crlNumber", crlExt.crlNumber);
		forwardSignCrl2(crlExt.crlEntryList, null);
	}

	private static void forwardSignCrl2(ArrayList<CrlEntryItem> crlEntryList,
			String message) {
		CrlEntryOrder order = CrlEntryOrder.getFromSession();
		order.setOrder(getString("order"));
		order.sort(crlEntryList);
		put("crlEntryList", crlEntryList);
		put("certList", CertForgeDB.getCertList());
		setMessage(message == null ?
				"Add a Certificate into Certificate Revocation List" : message);
		String encodedSigner = (String) Servlets.
				getRequest().getAttribute("encodedSigner");
		put("reason", "0");
		put("revocation", Time.toDateString(Time.now()));
		put("shortIssuer", CertUtil.shortDn(Base64.decode(encodedSigner)));
		put("REASON_STRING", CrlEntryItem.REASON_STRING);
		forward("crl/SignCrl2");
	}

	private static void doSignCrl3() throws MessageException {
		// 1 Collect CRL Entry List
		int max = -1;
		HashMap<Integer, BigInteger> serialMap = new HashMap<>();
		HashMap<Integer, String> revocationMap = new HashMap<>();
		HashMap<Integer, Integer> reasonMap = new HashMap<>();

		// 1.1 CRL Entries and related actions
		Enumeration<?> en = Servlets.getRequest().getParameterNames();
		while (en.hasMoreElements()) {
			String key = (String) en.nextElement();
			if (Boolean.FALSE.booleanValue()) { //
			} else if (key.startsWith("serial_")) {
				int i = getSuffixInt(key);
				max = Math.max(max, i);
				BigInteger sn;
				try {
					sn = new BigInteger(getString(key));
				} catch (Exception e) {
		            throw new MessageException(e.getMessage());
				}
				serialMap.put(Integer.valueOf(i), sn);
			} else if (key.startsWith("revocation_")) {
				int i = getSuffixInt(key);
				max = Math.max(max, i);
				revocationMap.put(Integer.valueOf(i), getString(key));
			} else if (key.startsWith("reason_")) {
				int i = getSuffixInt(key);
				max = Math.max(max, i);
				reasonMap.put(Integer.valueOf(i),
						Integer.valueOf(getInt(key)));
			}
		}

		// 1.2 Generate CRL Entries
		ArrayList<CrlEntryItem> crlEntryList = new ArrayList<>();
		for (int i = 0; i <= max; i ++) {
			Integer ii = Integer.valueOf(i);
			BigInteger serial = serialMap.get(ii);
			String revocation = revocationMap.get(ii);
			Integer reason = reasonMap.get(ii);
			crlEntryList.add(new CrlEntryItem(
					serial == null ? BigInteger.ZERO : serial,
					revocation, reason == null ? 0 : reason.intValue()));
		}

		// 1.3 Delete
		int delete = getInt("delete");
		if (delete >= 0 && delete < crlEntryList.size()) {
			crlEntryList.remove(delete);
		}

		// 1.4 Add
		String message = null;
		if (getBoolean("add")) {
			X509Certificate cert = null;
			if ("db".equals(getString("entryType"))) {
				int id = getInt("certList");
				CertItem certItem = CertForgeDB.getCert(id);
				if (certItem == null) {
					message = "No such Certificate [" + id + "]";
				} else {
					cert = certItem.getCertificate();
				}
			} else {
				try {
					cert = parseCert(getUploadOrPaste("Cert"));
				} catch (MessageException e) {
					message = e.getMessage();
				}
			}
			if (cert != null) {
				String revocation = getString("revocation");
				crlEntryList.add(new CrlEntryItem(cert.getSerialNumber(),
						revocation == null ? "" : revocation, getInt("reason")));
			}
		}

		// 1.4 Stay if not Submitted
		if (!getBoolean("next")) {
			getAndPut("id", "signerKey", "encodedSigner",
					"thisUpdate", "nextUpdate", "crlNumber");
			forwardSignCrl2(crlEntryList, message);
			return;
		}

		// 2 Parse Form
		int id = getInt("id");
		// 2.1 Signer Private Key, Algorithm
		String encoded = getString("signerKey");
		if (encoded == null || encoded.isEmpty()) {
			throw new MessageException("Invalid Signer Private Key");
		}
		PrivateKey signerKey;
		signerKey = CertUtil.parsePrivateKey(Base64.decode(encoded));
		if (signerKey == null) {
			throw new MessageException("Invalid Signer Private Key");
		}
		String sigAlg = get().sigAlgMap.get(signerKey.getAlgorithm());
		// 2.2 Issuer DN
		X500Name issuer;
		encoded = getString("encodedSigner");
		if (encoded == null || encoded.isEmpty()) {
			throw new MessageException("Invalid Signer DN");
		}
		try {
			issuer = new X500Name(Base64.decode(encoded));
		} catch (Exception e) {
			throw new MessageException(e.getMessage());
		}
		// 2.3 This Update, Next Update, CRL Number
		int thisUpdate = Time.parse(getString("thisUpdate"), null);
		int nextUpdate = Time.parse(getString("nextUpdate"), null);
		BigInteger crlNumber = BigInteger.ZERO;
		try {
			crlNumber = new BigInteger(getString("crlNumber"));
		} catch (Exception e) {/**/}

		// 3 Sign CRL
		// 3.1 CRL Entry List
		X509CRLEntry[] entries = new X509CRLEntry[crlEntryList.size()];
		for (int i = 0; i < entries.length; i ++) {
			entries[i] = crlEntryList.get(i).getX509CRLEntry();
		}
		// 3.2 CRL Extensions
		CRLExtensions ext = new CRLExtensions();
		try {
			ext.set(CRLNumberExtension.NAME, new CRLNumberExtension(crlNumber));
			// 3.3 Signature
			X509CRLImpl crl = new X509CRLImpl(issuer,
					new Date(1000L * thisUpdate), new Date(1000L * nextUpdate),
					entries, crlNumber.equals(BigInteger.ZERO) ? null : ext);
			crl.sign(signerKey, sigAlg);

			// 4 Save data
			if (id == 0) {
				CertForgeDB.addCrl(crl);
			} else if (id > 0) {
				CertForgeDB.updateCrl(id, crl);
			}

			// 5 Forward to JSP
			put("x509", Base64.encode(crl.getEncoded()));
			forward("crl/SignCrl3");
		} catch (Exception e) {
			throw new MessageException(e.getMessage());
		}
	}

	private static void download(String suffix, String contentType, byte[] data) {
		HttpServletResponse resp = Servlets.getResponse();
		resp.setHeader("Content-Disposition", "attachment; filename=\"" +
				Bytes.toHexUpper(Bytes.random(4)) + "." + suffix + "\"");
		resp.setContentType(contentType);
		try {
			resp.getOutputStream().write(data);
		} catch (Exception e) {/**/}
	}

	private static CertMap getCertMap() {
		CertMap certMap = new CertMap();
		String ids = getString("id");
		if (ids != null) {
			for (String id : ids.split(",")) {
				CertItem certItem = CertForgeDB.getCert(Integers.parse(id));
				if (certItem != null) {
					certMap.add(certItem.getCertificate());
				}
			}
		}
		return certMap;
	}

	private static void doDownloadPkcs7() {
		PKCS7 pkcs7 = getCertMap().exportPkcs7();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			pkcs7.encodeSignedData(baos);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		download("P7B", "application/x-pkcs7-certificates", baos.toByteArray());
	}

	private static void doDownloadJks() {
		String password = getString("password");
		if (password == null) {
			return;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			getCertMap().exportJks().store(baos, password.toCharArray());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		download("JKS", "application/octet-stream", baos.toByteArray());
	}

	private static void doDownloadStore() {
		doDownloadStore("");
	}

	private static void doDownloadStore(String prefix) {
		CertMap certMap = new CertMap();
		for (CertItem certItem : CertForgeDB.getCertList()) {
			certMap.add(certItem.getCertificate());
		}
		String encodedKey = getString(prefix + "pkcs8");
		String encodedCert = getString(prefix + "x509");
		if (encodedKey == null || encodedKey.isEmpty() ||
				encodedCert == null || encodedCert.isEmpty()) {
			return;
		}
		PrivateKey privateKey = CertUtil.parsePrivateKey(Base64.decode(encodedKey));
		X509Certificate cert = CertUtil.parseCert(Base64.decode(encodedCert));
		if (privateKey == null || cert == null) {
			return;
		}
		CertKey certKey = new CertKey(privateKey, certMap.getCertificateChain(cert));
		String password = getString(prefix + "password");
		if (password == null || password.isEmpty()) {
			return;
		}
		String storeType = getString(prefix + "storeType");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			KeyStore keyStore = certKey.toKeyStore(password, storeType);
			keyStore.store(baos, password.toCharArray());
		} catch (Exception e) {
			return;
		}
		if ("PKCS12".equals(storeType)) {
			download("PFX", "application/x-pkcs12", baos.toByteArray());
		} else {
			download("JKS", "application/octet-stream", baos.toByteArray());
		}
	}

	private static void doDownloadCert() {
		try {
			download("CER", "application/x-x509-ca-cert",
					CertForgeDB.getCert(getInt("id")).getCertificate().getEncoded());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void doDownloadCrl() {
		try {
			download("CRL", "application/pkix-crl",
					CertForgeDB.getCrl(getInt("id")).getCrl().getEncoded());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void doDownloadX509Cert() {
		String b64 = getString("x509");
		if (b64 != null) {
			download("CER", "application/x-x509-ca-cert", Base64.decode(b64));
		}
	}

	private static void doDownloadX509Crl() {
		String b64 = getString("x509");
		if (b64 != null) {
			download("CRL", "application/pkix-crl", Base64.decode(b64));
		}
	}

	private static void doDownloadPkcs10() {
		String b64 = getString("pkcs10");
		if (b64 != null) {
			download("P10", "application/pkcs10", Base64.decode(b64));
		}
	}
}