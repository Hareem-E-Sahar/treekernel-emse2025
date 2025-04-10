package com.handcoded.framework;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The <CODE>Application</CODE> class extends the basic <CODE>Process</CODE>
 * framework to provide support for command line options and perferences.
 * Derived classes extend its functionality and specialise it to a particular
 * task.
 *
 * @author	BitWise
 * @version	$Id: Application.java 416 2010-02-09 01:11:33Z andrew_jacobs $
 * @since	TFP 1.0
 */
public abstract class Application extends Process {

    /**
	 * Returns the current <CODE>Application</CODE> instance.
	 *
	 * @return	The <CODE>Application</CODE> instance.
	 * @since	TFP 1.0
	 */
    public static Application getApplication() {
        return (application);
    }

    /**
	 * Causes the <CODE>Application</CODE> to process it's command line arguments
	 * and begin the execution cycle.
	 *
	 * @param	arguments		The array of command line arguments.
	 * @since	TFP 1.0
	 */
    public void run(String arguments[]) {
        this.arguments = Option.processArguments(arguments);
        super.run();
    }

    /**
	 * Opens a stream to read the contents of an application resource that is
	 * with packaged in the application JAR or as a local file.
	 * 
	 * @param 	name			The name of the resource to be opened
	 * @return	An <CODE>InputStream</CODE> attached to the resource.
	 * @since	TFP 1.4
	 */
    public static InputStream openStream(String name) {
        try {
            URL url = new URL(name);
            return (url.openStream());
        } catch (Exception error) {
            ;
        }
        try {
            if (true) {
                URL url = Application.class.getResource("/" + name);
                if (url != null) {
                    return (url.openStream());
                }
            }
            return (new FileInputStream(name));
        } catch (Exception error) {
            logger.log(Level.SEVERE, "Error while opening stream", error);
            return (null);
        }
    }

    /**
	 * Converts the instance data members to a <CODE>String</CODE> representation
	 * that can be displayed for debugging purposes.
	 *
	 * @return 	The object's <CODE>String</CODE> representation.
	 * @since	TFP 1.0
	 */
    public String toString() {
        return (getClass().getName() + "[" + toDebug() + "]");
    }

    /**
	 * Constructs an <CODE>Application</CODE> instance and records it.
	 * @since	TFP 1.0
	 */
    protected Application() {
        application = this;
    }

    /**
	 * Provides an <CODE>Application</CODE> with a chance to perform any
	 * initialisation. This implementation checks for the -help option.
	 * Derived classes may extend the functionality.
	 * @since	TFP 1.0
	 */
    protected void startUp() {
        if (helpOption.isPresent()) {
            System.err.println("Usage:\n    java " + this.getClass().getName() + Option.listOptions() + describeArguments());
            System.err.println();
            System.err.println("Options:");
            Option.describeOptions();
            System.exit(1);
        }
        try {
            settings = PropertyResourceBundle.getBundle(getClass().getName());
        } catch (MissingResourceException error) {
            settings = null;
        }
    }

    /**
	 * Provides a text description of the arguments expected after the options
	 * (if any), for example "file ...". This method should be overridden in a
	 * derived class requiring a non-empty argument list.
	 * 
	 * @return	A description of the expected application arguments.
	 * @since	TFP 1.0
	 */
    protected String describeArguments() {
        return ("");
    }

    /**
	 * Provides access to the command line arguments after any option processing
	 * has been applied.
	 *
	 * @return	The command line arguments.
	 * @since	TFP 1.0
	 */
    protected final String[] getArguments() {
        return (arguments);
    }

    /**
	 * Returns the <CODE>ResourceBundle</CODE> containing this applications
	 * (localised) settings or <CODE>null</CODE> if it doesn't have any.
	 * 
	 * @return	The settings <CODE>ResourceBundle</CODE> or <CODE>null</CODE>.
	 * @since	TFP 1.1
	 */
    protected final ResourceBundle getSettings() {
        return (settings);
    }

    /**
	 * Converts the instance's member values to <CODE>String</CODE> representations
	 * and concatenates them all together. This function is used by toString and
	 * may be overriden in derived classes.
	 *
	 * @return	The object's <CODE>String</CODE> representation.
	 * @since	TFP 1.0
	 */
    protected String toDebug() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("arguments=");
        if (arguments != null) {
            buffer.append('[');
            for (int index = 0; index != arguments.length; ++index) {
                if (index != 0) buffer.append(',');
                if (arguments[index] != null) buffer.append("\"" + arguments[index] + "\""); else buffer.append("null");
            }
            buffer.append(']');
        } else buffer.append("null");
        return (buffer.toString());
    }

    /**
	 * A <CODE>Logger</CODE> instance used to report serious errors.
	 * @since	TFP 1.4
	 */
    private static Logger logger = Logger.getLogger("com.handcoded.framework.Application");

    /**
	 * The one and only <CODE>Application</CODE> instance.
	 * @since	TFP 1.0
	 */
    private static Application application = null;

    /**
	 * The <CODE>Option</CODE> instance used to detect <CODE>-help</CODE>
	 * @since	TFP 1.0
	 */
    private Option helpOption = new Option("-help", "Displays help information");

    /**
	 * The command line arguments after processing.
	 * @since	TFP 1.0
	 */
    private String arguments[] = null;

    /**
	 * The <CODE>ResourceBundle</CODE> used to application settings.
	 * @since	TFP 1.0
	 */
    protected ResourceBundle settings = null;
}
