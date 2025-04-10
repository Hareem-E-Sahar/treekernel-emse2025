package org.ezim.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.FileLock;
import java.util.Locale;
import java.util.Properties;
import org.ezim.core.Ezim;
import org.ezim.core.EzimLogger;

public class EzimConf {

    public static final String ezimThPoolSizeCore = "ezim.thpoolsize.core";

    public static final String ezimThPoolSizeMax = "ezim.thpoolsize.max";

    public static final String ezimThPoolKeepAlive = "ezim.thpool.KeepAlive";

    public static final String ezimMcGroup = "ezim.multicast.group";

    public static final String ezimMcPort = "ezim.multicast.port";

    public static final String ezimDtxPort = "ezim.dtx.port";

    public static final String ezimLocaladdress = "ezim.localaddress";

    public static final String ezimLocalname = "ezim.localname";

    public static final String ezimColorSelf = "ezim.color.self";

    public static final String ezimUserLocale = "ezim.user.locale";

    public static final String ezimStateiconSize = "ezim.stateicon.size";

    public static final String ezimmainAlwaysontop = "ezimmain.alwaysontop";

    public static final String ezimmainVisible = "ezimmain.visible";

    public static final String ezimmainLocationX = "ezimmain.location.x";

    public static final String ezimmainLocationY = "ezimmain.location.y";

    public static final String ezimmainSizeH = "ezimmain.size.h";

    public static final String ezimmainSizeW = "ezimmain.size.w";

    public static final String ezimplazaLocationX = "ezimplaza.location.x";

    public static final String ezimplazaLocationY = "ezimplaza.location.y";

    public static final String ezimplazaSizeH = "ezimplaza.size.h";

    public static final String ezimplazaSizeW = "ezimplaza.size.w";

    public static final String ezimmsgoutLocationX = "ezimmsgout.location.x";

    public static final String ezimmsgoutLocationY = "ezimmsgout.location.y";

    public static final String ezimmsgoutSizeH = "ezimmsgout.size.h";

    public static final String ezimmsgoutSizeW = "ezimmsgout.size.w";

    public static final String ezimmsginAutoopen = "ezimmsgin.autoopen";

    public static final String ezimmsginLocationX = "ezimmsgin.location.x";

    public static final String ezimmsginLocationY = "ezimmsgin.location.y";

    public static final String ezimmsginSizeH = "ezimmsgin.size.h";

    public static final String ezimmsginSizeW = "ezimmsgin.size.w";

    public static final String ezimfileoutLocationX = "ezimfileout.location.x";

    public static final String ezimfileoutLocationY = "ezimfileout.location.y";

    public static final String ezimfileoutSizeH = "ezimfileout.size.h";

    public static final String ezimfileoutSizeW = "ezimfileout.size.w";

    public static final String ezimfileoutDirectory = "ezimfileout.directory";

    public static final String ezimfileinLocationX = "ezimfilein.location.x";

    public static final String ezimfileinLocationY = "ezimfilein.location.y";

    public static final String ezimfileinSizeH = "ezimfilein.size.h";

    public static final String ezimfileinSizeW = "ezimfilein.size.w";

    public static final String ezimfileinDirectory = "ezimfilein.directory";

    public static final String ezimsoundEnabled = "ezimsound.enabled";

    private FileLock flock = null;

    public Properties settings;

    private static EzimConf ezconf = null;

    /**
	 * construct an instance of the configuration class
	 */
    private EzimConf() {
        this.settings = new Properties();
        this.read();
        this.validate();
        this.prepareFile();
    }

    /**
	 * read configuration items from file indicated by the given filename
	 */
    private void read() {
        FileInputStream fisTmp = null;
        try {
            fisTmp = new FileInputStream(EzimConf.getConfFilename());
            this.settings.load(fisTmp);
        } catch (FileNotFoundException fnfe) {
            EzimLogger.getInstance().warning(fnfe.getMessage(), fnfe);
        } catch (Exception e) {
            EzimLogger.getInstance().severe(e.getMessage(), e);
        } finally {
            try {
                if (fisTmp != null) fisTmp.close();
            } catch (Exception e) {
                EzimLogger.getInstance().severe(e.getMessage(), e);
            }
        }
    }

    /**
	 * validate settings and set default values where necessary
	 */
    private void validate() {
        boolean blnTmp = false;
        int iTmp = 0;
        String strTmp = null;
        strTmp = this.settings.getProperty(EzimConf.ezimThPoolSizeCore);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) {
            this.settings.setProperty(EzimConf.ezimThPoolSizeCore, Integer.toString(Ezim.thPoolSizeCore));
        }
        strTmp = this.settings.getProperty(EzimConf.ezimThPoolSizeMax);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) {
            this.settings.setProperty(EzimConf.ezimThPoolSizeMax, Integer.toString(Ezim.thPoolSizeMax));
        }
        strTmp = this.settings.getProperty(EzimConf.ezimThPoolKeepAlive);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) {
            this.settings.setProperty(EzimConf.ezimThPoolKeepAlive, Integer.toString(Ezim.thPoolKeepAlive));
        }
        iTmp = -1;
        strTmp = this.settings.getProperty(EzimConf.ezimMcPort);
        if (strTmp != null && strTmp.matches(Ezim.regexpInt)) {
            iTmp = Integer.parseInt(this.settings.getProperty(EzimConf.ezimMcPort));
        }
        if (iTmp < 0 || iTmp > 65535) {
            this.settings.setProperty(EzimConf.ezimMcPort, Integer.toString(Ezim.mcPort));
        }
        iTmp = -1;
        strTmp = this.settings.getProperty(EzimConf.ezimDtxPort);
        if (strTmp != null && strTmp.matches(Ezim.regexpInt)) {
            iTmp = Integer.parseInt(this.settings.getProperty(EzimConf.ezimDtxPort));
        }
        if (iTmp < 0 || iTmp > 65535) {
            this.settings.setProperty(EzimConf.ezimDtxPort, Integer.toString(Ezim.dtxPort));
        }
        strTmp = this.settings.getProperty(EzimConf.ezimColorSelf);
        if (strTmp == null || !strTmp.matches(Ezim.regexpRgb)) {
            this.settings.setProperty(EzimConf.ezimColorSelf, Integer.toString(Ezim.colorSelf, 16));
        }
        strTmp = this.settings.getProperty(EzimConf.ezimUserLocale);
        if (strTmp == null) strTmp = Locale.getDefault().toString();
        boolean blnInvalidLocale = true;
        for (int iCnt = 0; iCnt < Ezim.locales.length; iCnt++) {
            if (strTmp.equals(Ezim.locales[iCnt].toString())) {
                blnInvalidLocale = false;
                break;
            }
        }
        if (blnInvalidLocale) strTmp = Ezim.locales[0].toString();
        this.settings.setProperty(EzimConf.ezimUserLocale, strTmp);
        iTmp = -1;
        strTmp = this.settings.getProperty(EzimConf.ezimStateiconSize);
        if (strTmp != null && strTmp.matches(Ezim.regexpInt)) {
            iTmp = Integer.parseInt(this.settings.getProperty(EzimConf.ezimStateiconSize));
        }
        blnTmp = true;
        for (int iCnt = 0; iCnt < Ezim.stateiconSizes.length; iCnt++) {
            if (Ezim.stateiconSizes[iCnt].intValue() == iTmp) {
                blnTmp = false;
                break;
            }
        }
        if (blnTmp) {
            this.settings.setProperty(EzimConf.ezimStateiconSize, Ezim.stateiconSizes[0].toString());
        }
        strTmp = this.settings.getProperty(EzimConf.ezimmainAlwaysontop);
        if (strTmp == null || !strTmp.matches(Ezim.regexpBool)) this.settings.setProperty(EzimConf.ezimmainAlwaysontop, "false");
        strTmp = this.settings.getProperty(EzimConf.ezimmainVisible);
        if (strTmp == null || !strTmp.matches(Ezim.regexpBool)) this.settings.setProperty(EzimConf.ezimmainVisible, "true");
        strTmp = this.settings.getProperty(EzimConf.ezimmainLocationX);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimmainLocationX, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimmainLocationY);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimmainLocationY, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimmainSizeH);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimmainSizeH, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimmainSizeW);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimmainSizeW, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimplazaLocationX);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimplazaLocationX, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimplazaLocationY);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimplazaLocationY, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimplazaSizeH);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimplazaSizeH, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimplazaSizeW);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimplazaSizeW, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimmsgoutLocationX);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimmsgoutLocationX, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimmsgoutLocationY);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimmsgoutLocationY, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimmsgoutSizeH);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimmsgoutSizeH, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimmsgoutSizeW);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimmsgoutSizeW, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimmsginAutoopen);
        if (strTmp == null || !strTmp.matches(Ezim.regexpBool)) this.settings.setProperty(EzimConf.ezimmsginAutoopen, "false");
        strTmp = this.settings.getProperty(EzimConf.ezimmsginLocationX);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimmsginLocationX, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimmsginLocationY);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimmsginLocationY, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimmsginSizeH);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimmsginSizeH, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimmsginSizeW);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimmsginSizeW, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimfileoutLocationX);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimfileoutLocationX, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimfileoutLocationY);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimfileoutLocationY, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimfileoutSizeH);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimfileoutSizeH, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimfileoutSizeW);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimfileoutSizeW, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimfileinLocationX);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimfileinLocationX, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimfileinLocationY);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimfileinLocationY, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimfileinSizeH);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimfileinSizeH, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimfileinSizeW);
        if (strTmp == null || !strTmp.matches(Ezim.regexpInt)) this.settings.setProperty(EzimConf.ezimfileinSizeW, "0");
        strTmp = this.settings.getProperty(EzimConf.ezimsoundEnabled);
        if (strTmp == null || !strTmp.matches(Ezim.regexpBool)) this.settings.setProperty(EzimConf.ezimsoundEnabled, "true");
    }

    /**
	 * prepare and lock the physical file
	 */
    private void prepareFile() {
        try {
            File fTmp = new File(EzimConf.getConfDir());
            if (!fTmp.exists() || !fTmp.isDirectory()) fTmp.mkdirs();
            this.flock = new FileOutputStream(EzimConf.getLockFilename()).getChannel().tryLock();
            if (this.flock == null) {
                throw new Exception("Only one program instance can be run at a time.");
            }
        } catch (Exception e) {
            EzimLogger.getInstance().severe(e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
	 * return an EzimConf object
	 */
    public static EzimConf getInstance() {
        if (EzimConf.ezconf == null) EzimConf.ezconf = new EzimConf();
        return EzimConf.ezconf;
    }

    /**
	 * determine the appropriate configuration directory name
	 */
    public static String getConfDir() {
        String strSep = System.getProperty("file.separator");
        String strHome = System.getProperty("user.home");
        StringBuffer sbFName = new StringBuffer();
        sbFName.append(strHome);
        sbFName.append(strSep);
        sbFName.append(".");
        sbFName.append(Ezim.appAbbrev);
        return sbFName.toString();
    }

    /**
	 * determine the appropriate configuration filename
	 */
    public static String getConfFilename() {
        String strSep = System.getProperty("file.separator");
        StringBuffer sbFName = new StringBuffer();
        sbFName.append(EzimConf.getConfDir());
        sbFName.append(strSep);
        sbFName.append(Ezim.appAbbrev);
        sbFName.append(".conf");
        return sbFName.toString();
    }

    /**
	 * determine the appropriate lock filename
	 */
    public static String getLockFilename() {
        String strSep = System.getProperty("file.separator");
        StringBuffer sbFName = new StringBuffer();
        sbFName.append(EzimConf.getConfDir());
        sbFName.append(strSep);
        sbFName.append(Ezim.appAbbrev);
        sbFName.append(".lock");
        return sbFName.toString();
    }

    /**
	 * write configuration items to the file indicated by the given filename
	 */
    public synchronized void write() {
        FileOutputStream fosTmp = null;
        try {
            fosTmp = new FileOutputStream(EzimConf.getConfFilename());
            this.settings.store(fosTmp, "--- ezim Configuration File ---");
        } catch (Exception e) {
            EzimLogger.getInstance().severe(e.getMessage(), e);
        } finally {
            try {
                if (fosTmp != null) {
                    fosTmp.flush();
                    fosTmp.close();
                }
            } catch (Exception e) {
                EzimLogger.getInstance().severe(e.getMessage(), e);
            }
        }
    }
}
