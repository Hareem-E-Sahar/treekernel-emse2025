package com.google.code.p.keytooliui.ktl.util.jarsigner;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.PropertyResourceBundle;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import com.google.code.p.keytooliui.shared.Shared;
import com.google.code.p.keytooliui.shared.lang.*;
import com.google.code.p.keytooliui.shared.swing.optionpane.*;
import com.google.code.p.keytooliui.shared.util.jarsigner.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.io.*;
import java.awt.*;

public abstract class KTLAbs extends Object {

    private static final String _f_s_strClass = "com.google.code.p.keytooliui.ktl.util.jarsigner.KTLAbs.";

    private static final int _f_s_intSizeMinKprDsa = 512;

    private static final int _f_s_intSizeMinKprRsa = 512;

    private static final int _f_s_intSizeMinKprEc = 192;

    private static final int _f_s_intSizeMaxKprDsa = 1024;

    private static final int _f_s_intSizeMaxKprRsa = 65536;

    private static final int _f_s_intSizeMaxKprEc = 256;

    private static final int _f_s_intSizeStepKprDsa = 64;

    private static final int _f_s_intSizeStepKprRsa = 64;

    private static final int _f_s_intSizeMidKprEc = 239;

    public static final String f_s_strFormatFileShkDer = "DER";

    public static final String f_s_strFormatFileShkPem = "PEM";

    public static final String f_s_strFormatFileKprDer = "DER";

    public static final String f_s_strFormatFileKprPem = "PEM";

    public static final String f_s_strFormatFileCrtDer = "DER";

    public static final String f_s_strFormatFileCrtPkcs7 = "PKCS#7";

    public static final String f_s_strFormatFileCrtPem = "PEM";

    public static final String f_s_strFormatFileCsrPkcs10 = "PKCS#10";

    public static final String f_s_strFormatFileCrtPkcs12 = "PKCS#12";

    public static final String f_s_strFormatFileCrtOther = "Other";

    public static final String f_s_strFormatFileCrtCms = "P7C";

    public static final String f_s_strFormatFileXmlXml = "XML";

    public static final String f_s_strFormatFileSigDer = KTLAbs.f_s_strFormatFileCrtDer;

    public static final String f_s_strFormatFileSigPkcs7 = KTLAbs.f_s_strFormatFileCrtPkcs7;

    public static final String f_s_strFormatFileSigPem = KTLAbs.f_s_strFormatFileCrtPem;

    public static final String f_s_strFormatFileSCmsP7m = "P7M";

    public static final String f_s_strFormatFileSCmsP7s = "P7S";

    public static final String f_s_strProviderKstJks = "SUN";

    public static final String f_s_strProviderKstJceks = "SunJCE";

    public static final String f_s_strProviderKstBks = "BC";

    public static final String f_s_strProviderKstUber = "BC";

    public static final String f_s_strProviderKstPkcs12 = "BC";

    public static final String f_s_strProviderKstBC = "BC";

    public static final String f_s_strSecurityProviderSunRsaSign = "SunRsaSign";

    public static final String f_s_strSecurityProviderBC = "BC";

    /**
        provider(s) below just can read
    **/
    public static final String[] f_s_strsProviderKstPkcs12R = { KTLAbs.f_s_strSecurityProviderSunRsaSign, KTLAbs.f_s_strProviderKstBC };

    /**
        provider(s) below can write
    **/
    public static final String[] f_s_strsProviderKstPkcs12RW = { KTLAbs.f_s_strProviderKstBC };

    /**
        provider(s) below can write
    **/
    public static final String[] f_s_strsProviderKstBksRW = { KTLAbs.f_s_strProviderKstBC };

    /**
        provider(s) below can write
    **/
    public static final String[] f_s_strsProviderKstUberRW = { KTLAbs.f_s_strProviderKstBC };

    public static final String[] f_s_strsProviderKpgDsa = { KTLAbs.f_s_strProviderKstJks, KTLAbs.f_s_strProviderKstBC };

    public static final String[] f_s_strsProviderKpgEc = { KTLAbs.f_s_strProviderKstBC };

    public static final String[] f_s_strsProviderKpgRsa = { KTLAbs.f_s_strProviderKstBC, KTLAbs.f_s_strSecurityProviderSunRsaSign };

    public static final String[] f_s_strsProviderSigRsa = { KTLAbs.f_s_strProviderKstBC };

    public static final String f_s_strTypeKeypairDsa = "DSA";

    public static final String f_s_strTypeKeypairRsa = "RSA";

    public static final String f_s_strTypeKeypairEc = "EC";

    public static final String f_s_strTypeCrtDsa = KTLAbs.f_s_strTypeKeypairDsa;

    public static final String f_s_strTypeCrtRsa = KTLAbs.f_s_strTypeKeypairRsa;

    public static final String[] f_s_strsFormatFileCertReqBc = { KTLAbs.f_s_strFormatFileCsrPkcs10 };

    public static final String[] f_s_strsFormatFileCertOutBc = { KTLAbs.f_s_strFormatFileCrtDer, KTLAbs.f_s_strFormatFileCrtPem, KTLAbs.f_s_strFormatFileCrtPkcs7 };

    public static final String[] f_s_strsFormatFileCertImportBc = { KTLAbs.f_s_strFormatFileCrtPkcs7, KTLAbs.f_s_strFormatFileCrtOther };

    public static final String[] f_s_strsCertSigAlgoRsaBc2Csr = { UtilCrtX509.f_s_strDigestAlgoMD5 + "withRSA", UtilCrtX509.f_s_strDigestAlgoMD2 + "withRSA", UtilCrtX509.f_s_strDigestAlgoSHA1 + "withRSA", UtilCrtX509.f_s_strDigestAlgoMD5 + "withRSA" + "Encryption", UtilCrtX509.f_s_strDigestAlgoMD2 + "withRSA" + "Encryption", UtilCrtX509.f_s_strDigestAlgoSHA1 + "withRSA" + "Encryption" };

    public static final String[] f_s_strsCertSigAlgoKprBc2Crt = { UtilCrtX509.f_s_strDigestAlgoSHA1 + "withDSA", UtilCrtX509.f_s_strDigestAlgoMD5 + "withRSA", UtilCrtX509.f_s_strDigestAlgoMD2 + "withRSA", UtilCrtX509.f_s_strDigestAlgoSHA1 + "withRSA", UtilCrtX509.f_s_strDigestAlgoMD5 + "withRSA" + "Encryption", UtilCrtX509.f_s_strDigestAlgoMD2 + "withRSA" + "Encryption", UtilCrtX509.f_s_strDigestAlgoSHA1 + "withRSA" + "Encryption" };

    public static final String[] f_s_strsCertSigAlgoTcrBc2Crt = KTLAbs.f_s_strsCertSigAlgoKprBc2Crt;

    public static final String[] f_s_strsCertSigAlgoDsaAny = { UtilCrtX509.f_s_strDigestAlgoSHA1 + "withDSA" };

    public static final String[] f_s_strsSigAlgoPKBC = { "DSA", "RSA", "EC" };

    public static final String[] f_s_strsSigAlgoSKJceks = { "AES", "ARCFOUR", "Blowfish", "DES", "DESede", "HmacMD5", "HmacSHA1", "HmacSHA256", "HmacSHA384", "HmacSHA512", "RC2" };

    public static final String[] f_s_strsCipherRsaAlgoJceks = { "RSA/ECB/PKCS1Padding", "RSA/NONE/PKCS1Padding", "RSA/NONE/OAEPWithSHA1AndMGF1Padding" };

    public static final String[] f_s_strsCertSigAlgoRsaBc = { UtilCrtX509.f_s_strDigestAlgoSHA1 + "withRSA", UtilCrtX509.f_s_strDigestAlgoMD2 + "withRSA", UtilCrtX509.f_s_strDigestAlgoMD5 + "withRSA", UtilCrtX509.f_s_strDigestAlgoSHA256 + "withRSA", UtilCrtX509.f_s_strDigestAlgoSHA384 + "withRSA", UtilCrtX509.f_s_strDigestAlgoSHA512 + "withRSA", "RIPEMD128withRSA", "RIPEMD160withRSA", "RIPEMD256withRSA" };

    public static final String[] f_s_strsCertSigAlgoEcBc = { UtilCrtX509.f_s_strDigestAlgoSHA1 + "withECDSA", "SHA224" + "withECDSA", UtilCrtX509.f_s_strDigestAlgoSHA256 + "withECDSA", UtilCrtX509.f_s_strDigestAlgoSHA384 + "withECDSA", UtilCrtX509.f_s_strDigestAlgoSHA512 + "withECDSA" };

    public static final String f_s_strCertSigAlgoUnknown = "Unknown!";

    public static final Integer[] f_s_itgsCertVersion = { new Integer(1), new Integer(3) };

    public static final int f_s_intCertValidityMin = 180;

    public static final int f_s_intCertValidityMax = 14600;

    public static final int f_s_intCertValidityDefault = 9125;

    /**
        * Encode bytes array to BASE64 string
        * @param bytes
        * @return Encoded string
        */
    private static String encodeBASE64(byte[] bytes) {
        sun.misc.BASE64Encoder b64 = new sun.misc.BASE64Encoder();
        return b64.encode(bytes);
    }

    /**
        * Decode BASE64 encoded string to bytes array
        * @param text The string
        * @return Bytes array
        * @throws IOException
        */
    private static byte[] decodeBASE64(String text) throws IOException {
        sun.misc.BASE64Decoder b64 = new sun.misc.BASE64Decoder();
        return b64.decodeBuffer(text);
    }

    protected static boolean _s_can_encryptRsa_(Frame frmOwner, X509Certificate crtX509, int intSizeFileInput) {
        int intSizeKey = UtilCrtX509.s_getSizeKey(crtX509);
        int intSizeDataMax = intSizeKey;
        intSizeDataMax /= 8;
        intSizeDataMax -= 11;
        if (intSizeDataMax < intSizeFileInput) {
            String strBody = "Data must not be longer than " + intSizeDataMax + " bytes";
            strBody += "\n" + "Data size: " + intSizeFileInput + " bytes";
            strBody += "\n" + "Key size: " + intSizeKey + " bytes";
            strBody += "\n\n" + "Workaround: increase key size";
            strBody += "\n\n" + "MEMO: Data size should not exceed the following in bytes";
            strBody += "\n" + "  ([key-size] / 8) - 11";
            OPAbstract.s_showDialogError(frmOwner, strBody);
            return false;
        }
        return true;
    }

    protected static void _s_encryptRsa_(PublicKey pky, InputStream ism, OutputStream osm, int intSizeFileInput, String strInstanceCipherAlgo) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException, IllegalBlockSizeException, BadPaddingException, java.security.NoSuchProviderException {
        Cipher cip = Cipher.getInstance(strInstanceCipherAlgo, "BC");
        cip.init(Cipher.ENCRYPT_MODE, pky);
        byte[] bytsBuffer = new byte[intSizeFileInput];
        int intBytesRead;
        while ((intBytesRead = ism.read(bytsBuffer)) != -1) {
        }
        byte[] bytsTarget = cip.doFinal(bytsBuffer);
        osm.write(bytsTarget);
        osm.close();
    }

    protected static void _s_decryptRsa_(PrivateKey pky, InputStream ism, OutputStream osm, int intSizeFileInput, String strInstanceCipherAlgo) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException, IllegalBlockSizeException, BadPaddingException, java.security.NoSuchProviderException {
        Cipher cip = Cipher.getInstance(strInstanceCipherAlgo, "BC");
        cip.init(Cipher.DECRYPT_MODE, pky);
        byte[] bytsBuffer = new byte[intSizeFileInput];
        int intBytesRead;
        while ((intBytesRead = ism.read(bytsBuffer)) != -1) {
        }
        byte[] bytsTarget = cip.doFinal(bytsBuffer);
        osm.write(bytsTarget);
        osm.close();
    }

    public static Integer s_getItgDefaultKprDsa() {
        int intVal = 1024;
        intVal -= KTLAbs._f_s_intSizeMinKprDsa;
        intVal /= KTLAbs._f_s_intSizeStepKprDsa;
        return new Integer(intVal);
    }

    public static Integer s_getItgDefaultKprRsa() {
        int intVal = 2048;
        intVal -= KTLAbs._f_s_intSizeMinKprRsa;
        intVal /= KTLAbs._f_s_intSizeStepKprRsa;
        return new Integer(intVal);
    }

    public static Integer s_getItgDefaultKprEc() {
        Integer[] itgs = KTLAbs.s_getItgsListSizeKprEc();
        return new Integer(itgs.length - 1);
    }

    public static Integer[] s_getItgsListSizeKprDsa() {
        return _s_getItgsListSizeKpr(KTLAbs._f_s_intSizeMinKprDsa, KTLAbs._f_s_intSizeMaxKprDsa, KTLAbs._f_s_intSizeStepKprDsa);
    }

    public static Integer[] s_getItgsListSizeKprRsa() {
        return _s_getItgsListSizeKpr(KTLAbs._f_s_intSizeMinKprRsa, KTLAbs._f_s_intSizeMaxKprRsa, KTLAbs._f_s_intSizeStepKprRsa);
    }

    public static Integer[] s_getItgsListSizeKprEc() {
        Integer[] itgs = new Integer[3];
        itgs[0] = new Integer(KTLAbs._f_s_intSizeMinKprEc);
        itgs[1] = new Integer(KTLAbs._f_s_intSizeMidKprEc);
        itgs[2] = new Integer(KTLAbs._f_s_intSizeMaxKprEc);
        return itgs;
    }

    private static Integer[] _s_getItgsListSizeKpr(int intSizeMin, int intSizeMax, int intStep) {
        int intNbChoice = intSizeMax - intSizeMin;
        intNbChoice /= intStep;
        intNbChoice++;
        Integer[] itgs = new Integer[intNbChoice];
        for (int i = 0; i < intNbChoice; i++) {
            int intCur = intSizeMin + (i * intStep);
            itgs[i] = new Integer(intCur);
        }
        return itgs;
    }

    public abstract boolean doJob();

    protected Frame _frmOwner_ = null;

    protected String _strPathAbsKst_ = null;

    protected char[] _chrsPasswdKst_ = null;

    protected String _strProviderKst_ = null;

    protected void _setEnabledCursorWait_(boolean bln) {
        if (this._frmOwner_ == null) return;
        if (bln) this._frmOwner_.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); else this._frmOwner_.setCursor(Cursor.getDefaultCursor());
    }

    protected KTLAbs(Frame frmOwner, String strPathAbsKst, char[] chrsPasswdKst, String strProviderKst) {
        this._frmOwner_ = frmOwner;
        this._strPathAbsKst_ = strPathAbsKst;
        this._chrsPasswdKst_ = chrsPasswdKst;
        this._strProviderKst_ = strProviderKst;
    }

    /**
        if any error in code exiting,
        else if any other errors, show error-warning dialogs, then return false
        else return true
    **/
    protected boolean _saveKeyStore_(KeyStore kst, File fleSave, char[] chrsPasswordKst) {
        String strMethod = "_saveKeyStore_(...)";
        if (kst == null || fleSave == null) MySystem.s_printOutExit(this, strMethod, "nil arg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fleSave);
        } catch (FileNotFoundException excFileNotFound) {
            excFileNotFound.printStackTrace();
            MySystem.s_printOutError(this, strMethod, "excFileNotFound caught");
            String strBody = excFileNotFound.getMessage();
            strBody += "\n\n" + "Got FileNotFound exception,\nsounds like you selected a directory.";
            OPAbstract.s_showDialogError(this._frmOwner_, strBody);
            return false;
        }
        if (chrsPasswordKst == null) chrsPasswordKst = "".toCharArray();
        try {
            kst.store(fos, chrsPasswordKst);
        } catch (KeyStoreException excKeyStore) {
            excKeyStore.printStackTrace();
            MySystem.s_printOutError(this, strMethod, "excKeyStore caught");
            String strBody = "Got keystore exception.";
            strBody += "\n" + excKeyStore.getMessage();
            OPAbstract.s_showDialogError(this._frmOwner_, strBody);
            return false;
        } catch (IOException excIO) {
            excIO.printStackTrace();
            MySystem.s_printOutError(this, strMethod, "excIO caught");
            String strBody = "Got IO exception.";
            strBody += "\n" + excIO.getMessage();
            OPAbstract.s_showDialogError(this._frmOwner_, strBody);
            return false;
        } catch (NoSuchAlgorithmException excNoSuchAlgorithm) {
            excNoSuchAlgorithm.printStackTrace();
            MySystem.s_printOutError(this, strMethod, "excNoSuchAlgorithm caught");
            String strBody = "Got NoSuchAlgorithm exception.";
            strBody += "\n" + excNoSuchAlgorithm.getMessage();
            OPAbstract.s_showDialogError(this._frmOwner_, strBody);
            return false;
        } catch (CertificateException excCertificate) {
            excCertificate.printStackTrace();
            MySystem.s_printOutError(this, strMethod, "excCertificate caught");
            String strBody = "Got Certificate exception.";
            strBody += "\n" + excCertificate.getMessage();
            OPAbstract.s_showDialogError(this._frmOwner_, strBody);
            return false;
        } catch (ExceptionInInitializerError errExceptionInInitializer) {
            errExceptionInInitializer.printStackTrace();
            MySystem.s_printOutError(this, strMethod, "errExceptionInInitializer caught");
            String strBody = "Got error  exception in initializer.";
            strBody += "\n" + errExceptionInInitializer.getMessage();
            OPAbstract.s_showDialogError(this._frmOwner_, strBody);
            return false;
        } catch (NoClassDefFoundError errNoClassDefFound) {
            errNoClassDefFound.printStackTrace();
            MySystem.s_printOutError(this, strMethod, "errNoClassDefFound caught");
            String strBody = "Got NoClassDefFoundError exception.";
            strBody += "\n" + errNoClassDefFound.getMessage();
            OPAbstract.s_showDialogError(this._frmOwner_, strBody);
            return false;
        } catch (Exception exc) {
            exc.printStackTrace();
            MySystem.s_printOutError(this, strMethod, "exc caught");
            String strBody = "Got exception.";
            strBody += "\n" + exc.getMessage();
            OPAbstract.s_showDialogError(this._frmOwner_, strBody);
            return false;
        }
        try {
            fos.close();
            fos = null;
        } catch (IOException excIO) {
            excIO.printStackTrace();
            MySystem.s_printOutError(this, strMethod, "excIO caught");
            String strBody = "Got IO exception.";
            strBody += "\n" + excIO.getMessage();
            OPAbstract.s_showDialogError(this._frmOwner_, strBody);
            return false;
        }
        return true;
    }
}
