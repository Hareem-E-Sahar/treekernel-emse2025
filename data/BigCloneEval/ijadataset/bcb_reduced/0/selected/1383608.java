package org.dmd.dms.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintStream;
import java.net.URL;
import org.dmd.dms.SchemaDefinition;
import org.dmd.dms.SchemaManager;
import org.dmd.util.exceptions.ResultException;
import org.dmd.util.parsing.ConfigLocation;

/**
 * The DmoGenerator class coordinates the code generation associated with
 * a Dark Matter Schema. In particular, the generator will create code
 * associated with enumerations, object reference types and Dark Matter Objects (DMOs).
 */
public class DmoGenerator {

    DmoFormatter dmoFormatter;

    DmoTypeFormatter typeFormatter;

    DmoEnumFormatter enumFormatter;

    DmoActionFormatter actionFormatter;

    DmoAttributeFactoryFormatter factoryFormatter;

    DmoCompactSchemaFormatter compactSchemaFormatter;

    String gendir;

    String dmodir;

    String auxdir;

    String typedir;

    String adapterdir;

    String enumdir;

    String fileHeader;

    PrintStream progress;

    public DmoGenerator() {
        initialize(null);
    }

    public DmoGenerator(PrintStream o) {
        initialize(o);
    }

    void initialize(PrintStream o) {
        dmoFormatter = new DmoFormatter(o);
        typeFormatter = new DmoTypeFormatter(o);
        enumFormatter = new DmoEnumFormatter(o);
        actionFormatter = new DmoActionFormatter(o);
        factoryFormatter = new DmoAttributeFactoryFormatter(o);
        compactSchemaFormatter = new DmoCompactSchemaFormatter(o);
        progress = o;
        fileHeader = null;
    }

    /**
	 * Generates base code for the specified schema.
	 * @param sd The schema.
	 * @throws IOException  
	 * @throws ResultException 
	 */
    public void generateCode(SchemaManager sm, SchemaDefinition sd, ConfigLocation sl) throws IOException, ResultException {
        gendir = sl.getConfigParentDirectory() + File.separator + "generated";
        dmodir = gendir + File.separator + "dmo";
        auxdir = gendir + File.separator + "dmo";
        typedir = gendir + File.separator + "types";
        adapterdir = typedir + File.separator + "adapters";
        enumdir = gendir + File.separator + "enums";
        fileHeader = null;
        createGenDirs();
        readFileHeader(sd, sl);
        dmoFormatter.setFileHeader(fileHeader);
        typeFormatter.setFileHeader(fileHeader);
        enumFormatter.setFileHeader(fileHeader);
        actionFormatter.setFileHeader(fileHeader);
        dmoFormatter.dumpDMOs(sm, sd, dmodir, auxdir);
        typeFormatter.dumpTypes(sd, typedir);
        ComplexTypeFormatter.dumpComplexTypes(fileHeader, sd, typedir);
        ExtendedReferenceTypeFormatter.dumpExtendedReferenceTypes(fileHeader, sd, typedir);
        enumFormatter.dumpEnums(sd, enumdir);
        actionFormatter.dumpActions(sd, dmodir);
        compactSchemaFormatter.dumpSchema(sm, sd, dmodir);
    }

    /**
	 * If the schema has a generatedFileHeader specified, we try to read the file.
	 * @param sd The schema definition.
	 * @param sl The schema location.
	 * @throws IOException
	 */
    void readFileHeader(SchemaDefinition sd, ConfigLocation sl) throws IOException {
        if (sd.getGeneratedFileHeader() != null) {
            StringBuffer sb = new StringBuffer();
            if (sl.getJarFilename() != null) {
                URL url = new URL("jar:file:" + sl.getJarFilename() + "!/" + sl.getJarDirectory() + "/" + sd.getGeneratedFileHeader());
                LineNumberReader in = new LineNumberReader(new InputStreamReader(url.openStream()));
                String str;
                while ((str = in.readLine()) != null) {
                    sb.append(str + "\n");
                }
                in.close();
            } else {
                LineNumberReader in = new LineNumberReader(new FileReader(sl.getDirectory() + File.separator + sd.getGeneratedFileHeader()));
                String str;
                while ((str = in.readLine()) != null) {
                    sb.append(str + "\n");
                }
                in.close();
            }
            fileHeader = sb.toString();
        }
    }

    public void readSchemaFile(URL schema) throws IOException {
        System.out.println(schema.getFile());
        LineNumberReader in = new LineNumberReader(new InputStreamReader(schema.openStream()));
        String str;
        while ((str = in.readLine()) != null) {
            System.out.println(str);
        }
        in.close();
    }

    /**
	 * Creates the output directory structure for our code.
	 * @param sl The schema location.
	 */
    void createGenDirs() {
        File gdf = new File(gendir);
        if (!gdf.exists()) gdf.mkdir();
        File ddf = new File(dmodir);
        if (!ddf.exists()) ddf.mkdir();
        File adf = new File(auxdir);
        if (!adf.exists()) adf.mkdir();
        File tdf = new File(typedir);
        if (!tdf.exists()) tdf.mkdir();
        File addf = new File(adapterdir);
        if (!addf.exists()) addf.mkdir();
        File edf = new File(enumdir);
        if (!edf.exists()) edf.mkdir();
    }
}
