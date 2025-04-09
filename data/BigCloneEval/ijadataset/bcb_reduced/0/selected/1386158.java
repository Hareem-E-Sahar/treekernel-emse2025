package org.hardtokenmgmt.keyceremony.tolima;

import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Properties;
import org.ejbca.util.passgen.PasswordGeneratorFactory;
import org.hardtokenmgmt.core.util.CertUtils;

/**
 * Base class used by implementation performing key ceremony
 * initializations
 * 
 * 
 * @author Philip Vendil 26 jan 2008
 *
 * @version $Id$
 */
public abstract class BaseKeyCeremony {

    private Properties props = new Properties();

    protected PrintStream out = System.out;

    protected Console console = System.console();

    protected ArrayList<String> confirmStrings = new ArrayList<String>();

    static {
        CertUtils.installBCProvider();
    }

    protected void initProps() throws Exception {
        props.load(this.getClass().getResourceAsStream("/tolima_keyceremony.properties"));
    }

    protected void initProps(String fileName) throws Exception {
        props.load(this.getClass().getResourceAsStream(fileName));
    }

    /**
	 * 
	 * @return Method returning a line from standard input.
	 */
    protected String readLine() {
        return console.readLine();
    }

    /**
	 * Help method printing a string at the specified level.
	 * @param level is the number of double spaces inserted before the text.
	 */
    protected PrintStream out(int level) {
        for (int i = 0; i < level; i++) {
            out.print("  ");
        }
        return out;
    }

    /**
	 * Help Method for printing the result data to file and standard output.
	 * @param ps print stream connected to file.
	 * @param level is the number of double spaces inserted before the text.

	 */
    protected void resultout(PrintStream ps, int level, String message) {
        for (int i = 0; i < level; i++) {
            ps.print("  ");
        }
        ps.println(message);
        out(level).println(message);
    }

    /**
	 * 
	 * @return Method expecting a 'Y' or 'N'.
	 */
    protected boolean getConfirmation(int level, String message) {
        boolean correct = false;
        String value = "";
        boolean retval = false;
        while (!correct) {
            out(level).print(message);
            out.flush();
            value = console.readLine().trim();
            if (value.equalsIgnoreCase("Y")) {
                correct = true;
                retval = true;
            }
            if (value.equalsIgnoreCase("N")) {
                correct = true;
                retval = false;
            }
        }
        return retval;
    }

    /**
	 * 
	 * @param customPropertiesPath path specified in main(args), use null to prompt for custom property file
	 */
    protected void loadCustomProperties(String customPropertiesPath) {
        if (customPropertiesPath != null) {
            File f = new File(customPropertiesPath);
            if (f.exists() && f.isFile() && f.canRead()) {
                try {
                    Properties customProps = new Properties();
                    customProps.load(new FileInputStream(f));
                    for (String key : customProps.stringPropertyNames()) {
                        props.setProperty(key, customProps.getProperty(key));
                    }
                } catch (Exception e) {
                    out(1).println("Error couldn't parse specified file. Is it correct? Enter path again.");
                }
            } else {
                out(1).println("Error config file " + customPropertiesPath + " don't exist or can be read.");
            }
        } else {
            if (getConfirmation(1, "Do you want to load a custom property file ('Y' or 'N') : ")) {
                boolean pathFound = false;
                while (!pathFound) {
                    out(1).print("Enter the path to the property file :");
                    out.flush();
                    String path = readLine();
                    File f = new File(path);
                    if (f.exists() && f.isFile() && f.canRead()) {
                        try {
                            Properties customProps = new Properties();
                            customProps.load(new FileInputStream(f));
                            for (String key : customProps.stringPropertyNames()) {
                                props.setProperty(key, customProps.getProperty(key));
                            }
                            pathFound = true;
                        } catch (Exception e) {
                            out(1).println("Error couldn't parse specified file. Is it correct? Enter path again.");
                        }
                    } else {
                        out(1).println("Error couldn't read specified file, enter path again.");
                    }
                }
            }
        }
    }

    /**
	 * Method displaying the manual configured properties and let's the user
	 * confirm and continue or re-enter the data.
	 */
    protected boolean displayCurrentConfiguration(int startLevel) {
        boolean retval = false;
        if (confirmStrings.size() > 0) {
            out(startLevel).println("\n");
            out(startLevel).println("You have entered the following data:");
            for (String confirmString : confirmStrings) {
                out(startLevel + 1).println(confirmString);
            }
            retval = getConfirmation(2, "Is this configuration correct ('Y' or 'N') : ");
        } else {
            retval = getConfirmation(1, "Are you sure you want to continue ('Y' or 'N') : ");
            if (!retval) {
                System.exit(-1);
            }
        }
        return retval;
    }

    protected String getProperty(String setting, boolean required) throws IOException {
        String value = props.getProperty(setting);
        if (value != null) {
            return value.trim();
        } else {
            if (required) {
                throw new IOException("Error in configuration, setting: '" + setting + "' is required.");
            }
        }
        return value;
    }

    protected String getProperty(String setting, String defaultValue) throws IOException {
        return props.getProperty(setting, defaultValue);
    }

    protected int getPropertyAsInt(String setting, boolean required) throws IOException {
        String value = props.getProperty(setting);
        int retval = 0;
        if (value != null) {
            try {
                retval = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IOException("Error in configuration, setting: '" + setting + "' should only contain numbers.");
            }
        } else {
            if (required) {
                throw new IOException("Error in configuration, setting: '" + setting + "' is required.");
            }
        }
        return retval;
    }

    protected int getPropertyAsInt(String setting, int defaultValue) throws IOException {
        String value = props.getProperty(setting);
        int retval = 0;
        if (value != null) {
            try {
                retval = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new IOException("Error in configuration, setting: '" + setting + "' should only contain numbers.");
            }
        } else {
            retval = defaultValue;
        }
        return retval;
    }

    protected long getPropertyAsLong(String setting, boolean required) throws IOException {
        String value = props.getProperty(setting);
        long retval = 0;
        if (value != null) {
            try {
                retval = Long.parseLong(value);
            } catch (NumberFormatException e) {
                throw new IOException("Error in configuration, setting: '" + setting + "' should only contain numbers.");
            }
        } else {
            if (required) {
                throw new IOException("Error in configuration, setting: '" + setting + "' is required.");
            }
        }
        return retval;
    }

    protected long getPropertyAsLong(String setting, long defaultValue) throws IOException {
        String value = props.getProperty(setting);
        long retval = 0;
        if (value != null) {
            try {
                retval = Long.parseLong(value);
            } catch (NumberFormatException e) {
                throw new IOException("Error in configuration, setting: '" + setting + "' should only contain numbers.");
            }
        } else {
            retval = defaultValue;
        }
        return retval;
    }

    protected boolean getPropertyAsBoolean(String setting) throws IOException {
        String value = props.getProperty(setting);
        if (value != null) {
            value = value.trim().toLowerCase();
            if (value.equals("true") || value.equals("false")) {
                return Boolean.parseBoolean(value);
            } else {
                throw new IOException("Error in configuration, setting: '" + setting + "' should be either true or false.");
            }
        } else {
            throw new IOException("Error in configuration, setting: '" + setting + "' is required.");
        }
    }

    protected boolean getPropertyAsBoolean(String setting, boolean defaultValue) throws IOException {
        String value = props.getProperty(setting);
        if (value != null) {
            value = value.trim().toLowerCase();
            if (value.equals("true") || value.equals("false")) {
                return Boolean.parseBoolean(value);
            } else {
                throw new IOException("Error in configuration, setting: '" + setting + "' should be either true or false.");
            }
        } else {
            return defaultValue;
        }
    }

    protected String getPropertyWithUI(String setting, int printLevel, String[] promptHeader, String confirmString, boolean confirm, String[] confirmHeader) throws IOException {
        String value = getProperty(setting, false);
        if (value == null) {
            while (value == null) {
                for (String promptString : promptHeader) {
                    out(printLevel).println(promptString);
                }
                String input = readLine().trim();
                if (input.equals("")) {
                    out(printLevel).println("Error, you must enter a value.");
                } else {
                    value = input;
                }
            }
            if (confirm) {
                for (String h : confirmHeader) {
                    confirmStrings.add(h);
                }
                confirmStrings.add(confirmString + value);
            }
        }
        return value;
    }

    protected String getPropertyWithUI(String setting, int printLevel, String[] promptHeader, String confirmString, boolean confirm) throws IOException {
        return getPropertyWithUI(setting, printLevel, promptHeader, confirmString, confirm, new String[] {});
    }

    protected boolean getPropertyWithUIAsBoolean(String setting, int printLevel, String[] promptHeader, String confirmString, boolean confirm, String[] confirmHeader) throws IOException {
        String value = getProperty(setting, false);
        if (value == null) {
            while (value == null) {
                for (String promptString : promptHeader) {
                    out(printLevel).println(promptString);
                }
                String input = readLine().trim();
                if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false")) {
                    value = input;
                } else {
                    out(printLevel).println("Error, entered value must be either 'true' or 'false'.");
                }
            }
            if (confirm) {
                for (String h : confirmHeader) {
                    confirmStrings.add(h);
                }
                confirmStrings.add(confirmString + value);
            }
        }
        return Boolean.parseBoolean(value);
    }

    protected boolean getPropertyWithUIAsBoolean(String setting, int printLevel, String[] promptHeader, String confirmString, boolean confirm) throws IOException {
        return getPropertyWithUIAsBoolean(setting, printLevel, promptHeader, confirmString, confirm, new String[] {});
    }

    protected long getPropertyWithUIAsLong(String setting, int printLevel, String[] promptHeader, String confirmString, boolean confirm, String[] confirmHeader) throws IOException {
        String value = getProperty(setting, false);
        if (value == null) {
            while (value == null) {
                for (String promptString : promptHeader) {
                    out(printLevel).println(promptString);
                }
                String input = readLine().trim();
                try {
                    Long.parseLong(input);
                    value = input;
                } catch (NumberFormatException e) {
                    out(printLevel).println("Error, entered value can only contain numbers.");
                }
            }
            if (confirm) {
                for (String h : confirmHeader) {
                    confirmStrings.add(h);
                }
                confirmStrings.add(confirmString + value);
            }
        }
        return Long.parseLong(value);
    }

    protected long getPropertyWithUIAsLong(String setting, int printLevel, String[] promptHeader, String confirmString, boolean confirm) throws IOException {
        return getPropertyWithUIAsLong(setting, printLevel, promptHeader, confirmString, confirm, new String[] {});
    }

    protected int getPropertyWithUIAsInt(String setting, int printLevel, String[] promptHeader, String confirmString, boolean confirm, String[] confirmHeader) throws IOException {
        String value = getProperty(setting, false);
        if (value == null) {
            while (value == null) {
                for (String promptString : promptHeader) {
                    out(printLevel).println(promptString);
                }
                out.flush();
                String input = readLine().trim();
                try {
                    Integer.parseInt(input);
                    value = input;
                } catch (NumberFormatException e) {
                    out(printLevel).println("Error, entered value can only contain numbers.");
                }
            }
            if (confirm) {
                for (String h : confirmHeader) {
                    confirmStrings.add(h);
                }
                confirmStrings.add(confirmString + value);
            }
        }
        return Integer.parseInt(value);
    }

    protected int getPropertyWithUIAsInt(String setting, int printLevel, String[] promptHeader, String confirmString, boolean confirm) throws IOException {
        return getPropertyWithUIAsInt(setting, printLevel, promptHeader, confirmString, confirm, new String[] {});
    }

    protected Properties getProps() {
        return props;
    }

    protected byte[] readFile(File f) throws IOException {
        InputStream is = new FileInputStream(f);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int len = 0;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return baos.toByteArray();
    }

    protected String genRandomPasswordAllPrintable() {
        return PasswordGeneratorFactory.getInstance(PasswordGeneratorFactory.PASSWORDTYPE_ALLPRINTABLE).getNewPassword(8, 8);
    }

    protected String genRandomPasswordNumbers() {
        return PasswordGeneratorFactory.getInstance(PasswordGeneratorFactory.PASSWORDTYPE_DIGITS).getNewPassword(12, 12);
    }

    protected static String getCustomPropertyFilePathFromArgs(String[] args) {
        String retval = null;
        if (args.length > 1) {
            if (args[1] != null && !args[1].trim().equals("")) {
                retval = args[1].trim();
            }
        }
        return retval;
    }
}
