package uk.ac.ed.ph.snuggletex;

import uk.ac.ed.ph.snuggletex.definitions.Globals;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;
import java.util.Map.Entry;

/**
 * Some handy utility methods for managing SnuggleTeX CSS.
 *
 * @author  David McKain
 * @version $Revision: 179 $
 */
public class CSSUtilities {

    /**
     * Writes out the CSS stylesheet specified via the given {@link DOMOutputOptions} Object,
     * using the default if nothing has been specified, saving the results to the given
     * {@link OutputStream}.
     * 
     * @param options
     * @param cssOutputStream
     */
    public static void writeStylesheet(DOMOutputOptions options, OutputStream cssOutputStream) {
        Properties cssProperties = options.getInlineCSSProperties();
        if (cssProperties == null) {
            cssProperties = readBuiltinInlineCSSProperties();
        }
        writeStylesheet(cssProperties, cssOutputStream);
    }

    /**
     * Writes out the "default" CSS stylesheet specified via {@link #readBuiltinInlineCSSProperties()},
     * returning the result as a String.
     */
    public static String writeDefaultStylesheet() {
        return writeStylesheet(readBuiltinInlineCSSProperties());
    }

    /**
     * Writes out the CSS stylesheet made from the given <strong>cssProperties</strong>, returning 
     * the result as a String.
     */
    public static String writeStylesheet(Properties cssProperties) {
        StringWriter resultWriter = new StringWriter();
        writeStylesheet(cssProperties, resultWriter);
        return resultWriter.toString();
    }

    /**
     * Writes out the "default" CSS stylesheet specified via {@link #readBuiltinInlineCSSProperties()},
     * saving the results to the given {@link OutputStream}.
     */
    public static void writeDefaultStylesheet(OutputStream cssOutputStream) {
        writeStylesheet(readBuiltinInlineCSSProperties(), cssOutputStream);
    }

    /**
     * Writes out the CSS stylesheet made from the given <strong>cssProperties</strong>,
     * saving the results to the given {@link OutputStream}.
     */
    public static void writeStylesheet(Properties cssProperties, OutputStream cssOutputStream) {
        writeStylesheet(cssProperties, new OutputStreamWriter(cssOutputStream));
    }

    /**
     * Writes out the CSS stylesheet made from the given <strong>cssProperties</strong>,
     * saving the results to the given {@link Writer}.
     */
    public static void writeStylesheet(Properties cssProperties, Writer cssOutputWriter) {
        PrintWriter writer = new PrintWriter(new BufferedWriter(cssOutputWriter));
        writer.println("/* SnuggleTeX CSS File (autogenerated) */");
        String value;
        for (Entry<Object, Object> entry : cssProperties.entrySet()) {
            writer.print("\n");
            writer.print(entry.getKey());
            writer.print(" {\n");
            value = ((String) entry.getValue()).trim();
            if (value.length() != 0) {
                writer.print("  ");
                writer.print(value.replaceFirst("(?!;)$", ";").replaceAll(";\\s*(?=.)", ";\n  "));
                writer.print("\n");
            }
            writer.print("}\n");
        }
        writer.close();
    }

    /**
     * Reads in the default CSS Properties file, which is packaged within SnuggleTeX itself.
     */
    public static Properties readBuiltinInlineCSSProperties() {
        Properties builtinInlineCSSProperties = new Properties();
        try {
            builtinInlineCSSProperties.load(CSSUtilities.class.getClassLoader().getResourceAsStream(Globals.CSS_PROPERTIES_NAME));
        } catch (IOException e) {
            throw new SnuggleRuntimeException("Could not load CSS properties file via ClassLoader " + Globals.CSS_PROPERTIES_NAME);
        }
        return builtinInlineCSSProperties;
    }

    /**
     * Reads in the custom CSS {@link Properties} Object specified by the given
     * {@link DOMOutputOptions}. If nothing has been supplied, then it reads in the
     * default CSS Properties via {@link #readBuiltinInlineCSSProperties()} as a fallback.
     */
    public static Properties readInlineCSSProperties(DOMOutputOptions options) {
        Properties properties = options.getInlineCSSProperties();
        if (properties == null) {
            properties = readBuiltinInlineCSSProperties();
        }
        return properties;
    }

    /**
     * Main method is used by the build process to write out the default CSS stylesheet.
     * 
     * @param args Array containing 1 argument: the File that the default CSS should be written to.
     * 
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: specify a File that the default SnuggleTeX CSS will be written to");
            System.exit(1);
        }
        writeDefaultStylesheet(new FileOutputStream(new File(args[0])));
    }
}
