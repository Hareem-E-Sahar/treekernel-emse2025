package org.melati.poem.prepro;

import java.util.Vector;
import java.io.Writer;
import java.io.IOException;
import org.melati.poem.StandardIntegrityFix;

/**
 * A definition of a <tt>ReferencePoemType</tt> from the DSD.
 * 
 * Its member variables are populated from the DSD or defaults.
 * Its methods are used to generate the java code.
 */
public class ReferenceFieldDef extends FieldDef {

    StandardIntegrityFix integrityfix;

    /**
  * Constructor.
  *
  * @param table        the {@link TableDef} that this <code>Field</code> is 
  *                     part of 
  * @param name         the name of this field
  * @param type         the type of this field
  * @param displayOrder where to place this field in a list
  * @param qualifiers   all the qualifiers of this field
  * 
  * @throws IllegalityException if a semantic inconsistency is detected
  */
    public ReferenceFieldDef(TableDef table, String name, int displayOrder, String type, Vector qualifiers) throws IllegalityException {
        super(table, name, type, "Integer", displayOrder, qualifiers);
        table.addImport("org.melati.poem.ReferencePoemType", "table");
        table.addImport("org.melati.poem.NoSuchRowPoemException", "persistent");
        if (integrityfix != null) {
            table.addImport("org.melati.poem.StandardIntegrityFix", "table");
        }
        table.addImport(type, "table");
        table.addImport(type, "persistent");
    }

    /**
  * @param w The base table java file.
  * @throws IOException 
  *           if something goes wrong with the file system
  */
    protected void generateColRawAccessors(Writer w) throws IOException {
        super.generateColRawAccessors(w);
        w.write("\n" + "          public Object getRaw(Persistent g)\n" + "              throws AccessPoemException {\n" + "            return ((" + mainClass + ")g).get" + suffix + "Troid();\n" + "          }\n" + "\n");
        w.write("          public void setRaw(Persistent g, Object raw)\n" + "              throws AccessPoemException {\n" + "            ((" + mainClass + ")g).set" + suffix + "Troid((" + rawType + ")raw);\n" + "          }\n");
        if (integrityfix != null) {
            w.write("\n" + "          public StandardIntegrityFix defaultIntegrityFix() {\n" + "            return StandardIntegrityFix." + integrityfix.name + ";\n" + "          }\n");
        }
    }

    private String targetCast() {
        TableNamingInfo targetTable = (TableNamingInfo) table.dsd.nameStore.tablesByShortName.get(type);
        return targetTable == null || targetTable.superclass == null ? "" : "(" + type + ")";
    }

    /**
  * @param w The base persistent java file.
  * @throws IOException 
  *           if something goes wrong with the file system
  */
    public void generateBaseMethods(Writer w) throws IOException {
        super.generateBaseMethods(w);
        String targetTableAccessorMethod = "get" + type + "Table";
        String targetSuffix = type;
        String db = "get" + table.dsd.databaseTablesClass + "()";
        w.write("\n /**\n" + "  * Retrieves the Table Row Object ID. \n" + "  *\n" + "  * @generator " + "org.melati.poem.prepro.ReferenceFieldDef" + "#generateBaseMethods \n" + "  * @throws AccessPoemException  \n" + "  *         if the current <code>AccessToken</code> \n" + "  *         does not confer read access rights \n" + "  * @return the TROID as an <code>Integer</code> \n" + "  */\n");
        w.write("\n" + "  public Integer get" + suffix + "Troid()\n" + "      throws AccessPoemException {\n" + "    readLock();\n" + "    return get" + suffix + "_unsafe();\n" + "  }\n" + "\n");
        w.write("\n /**\n" + "  * Sets the Table Row Object ID. \n" + "  * \n" + "  * @generator " + "org.melati.poem.prepro.ReferenceFieldDef" + "#generateBaseMethods \n" + "  * @param raw  a Table Row Object Id \n" + "  * @throws AccessPoemException  \n" + "  *         if the current <code>AccessToken</code> \n" + "  *         does not confer write access rights\n" + "  */\n");
        w.write("  public void set" + suffix + "Troid(Integer raw)\n" + "      throws AccessPoemException {\n" + "    set" + suffix + "(" + "raw == null ? null : \n" + "        " + targetCast() + db + "." + targetTableAccessorMethod + "()." + "get" + targetSuffix + "Object(raw));\n" + "  }\n" + "\n");
        w.write("\n /**\n" + "  * Retrieves the <code>" + suffix + "</code> object reffered to.\n" + "  *  \n" + "  * @generator " + "org.melati.poem.prepro.ReferenceFieldDef" + "#generateBaseMethods \n" + "  * @throws AccessPoemException  \n" + "  *         if the current <code>AccessToken</code> \n" + "  *         does not confer read access rights \n" + "  * @throws NoSuchRowPoemException  \n" + "  *         if the <Persistent</code> has yet " + "to be allocated a TROID \n" + "  * @return the <code>" + suffix + "</code> as a <code>" + type + "</code> \n" + "  */\n");
        w.write("  public " + type + " get" + suffix + "()\n" + "      throws AccessPoemException, NoSuchRowPoemException {\n" + "    Integer troid = get" + suffix + "Troid();\n" + "    return troid == null ? null :\n" + "        " + targetCast() + db + "." + targetTableAccessorMethod + "()." + "get" + targetSuffix + "Object(troid);\n" + "  }\n" + "\n");
        w.write("\n /**\n" + "  * Set the " + suffix + ".\n" + "  * \n" + "  * @generator " + "org.melati.poem.prepro.ReferenceFieldDef" + "#generateBaseMethods \n" + "  * @param cooked  a validated <code>" + type + "</code>\n" + "  * @throws AccessPoemException  \n" + "  *         if the current <code>AccessToken</code> \n" + "  *         does not confer write access rights \n" + "  */\n");
        w.write("  public void set" + suffix + "(" + type + " cooked)\n" + "      throws AccessPoemException {\n" + "    _" + tableAccessorMethod + "().\n" + "      get" + suffix + "Column().\n" + "        getType().assertValidCooked(cooked);\n" + "    writeLock();\n" + "    if (cooked == null)\n" + "      set" + suffix + "_unsafe(null);\n" + "    else {\n" + "      cooked.existenceLock();\n" + "      set" + suffix + "_unsafe(cooked.troid());\n" + "    }\n" + "  }\n");
    }

    /**
  * Write out this <code>Field</code>'s java declaration string.
  *
  * @param w The base persistent java file.
  * @throws IOException 
  *           if something goes wrong with the file system
  */
    public void generateJavaDeclaration(Writer w) throws IOException {
        w.write("Integer " + name);
    }

    /** @return the Java string for this <code>PoemType</code>. */
    public String poemTypeJava() {
        String targetTableAccessorMethod = "get" + type + "Table";
        String db = "get" + table.dsd.databaseTablesClass + "()";
        return "new ReferencePoemType(" + db + ".\n" + "                                             " + targetTableAccessorMethod + "(), " + isNullable + ")";
    }
}
