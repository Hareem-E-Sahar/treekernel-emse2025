package joelib2.io.types;

import joelib2.io.BasicIOType;
import joelib2.io.BasicIOTypeHolder;
import joelib2.io.BasicMoleculeWriter;
import joelib2.io.BasicReader;
import joelib2.io.MoleculeFileIO;
import joelib2.io.MoleculeIOException;
import joelib2.molecule.Molecule;
import wsi.ra.tool.BasicPropertyHolder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Category;

/**
 * Reader/Writer for compressed ZIP files.
 *
 * @.author     wegnerj
 * @.wikipedia  File format
 * @.license GPL
 * @.cvsversion    $Revision: 1.9 $, $Date: 2005/02/17 16:48:34 $
 */
public class ZIP implements MoleculeFileIO {

    /**
     * Obtain a suitable logger.
     */
    private static Category logger = Category.getInstance(ZIP.class.getName());

    private static final String description = "Compressed ZIP file format";

    private static final String[] extensions = new String[] { "ZIP" };

    private boolean isCMLFile = false;

    private BasicReader reader;

    private BasicMoleculeWriter writer;

    private ZipInputStream zipInputFileSream;

    private ZipOutputStream zipOutputFileSream;

    /**
     *  Constructor for the ZIP object
     */
    public ZIP() {
        if (logger.isDebugEnabled()) {
            logger.debug("Initialize " + this.getClass().getName());
        }
    }

    public void closeReader() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }

    public void closeWriter() throws IOException {
        writer.close();
    }

    public boolean hasNextFileEntry(Molecule mol) throws IOException, MoleculeIOException {
        if (isCMLFile) {
            String value = BasicPropertyHolder.instance().getProperties().getProperty("joelib2.io.types.ChemicalMarkupLanguage.useSlowerMemorySavingPreparser");
            if (((value != null) && !value.equalsIgnoreCase("true"))) {
                logger.warn("Only one CML file in a compressed ZIP file can be loaded.");
                logger.warn("Other CML files are skipped, because SAX parser forces close.");
                logger.warn("Please activate: joelib2.io.types.ChemicalMarkupLanguage.useSlowerMemorySavingPreparser");
            }
            return false;
        }
        ZipEntry actualZipEntry;
        while ((actualZipEntry = zipInputFileSream.getNextEntry()) != null) {
            if (actualZipEntry.isDirectory()) {
                continue;
            } else {
                String inputFile = actualZipEntry.getName();
                BasicIOType inType;
                inType = BasicReader.checkGetInputType(inputFile);
                if (inType.equals(BasicIOTypeHolder.instance().getIOType("CML"))) {
                    isCMLFile = true;
                }
                logger.info(inputFile + " (" + actualZipEntry.getSize() + " bytes) forces setting input type to " + inType.toString() + ".");
                reader = new BasicReader(zipInputFileSream, inType);
                mol.setInputType(inType);
                return reader.readNext(mol);
            }
        }
        return false;
    }

    /**
     *  Description of the Method
     *
     * @param  is               Description of the Parameter
     * @exception  IOException  Description of the Exception
     */
    public void initReader(InputStream is) throws IOException {
        zipInputFileSream = new ZipInputStream(is);
    }

    /**
     *  Description of the Method
     *
     * @param  os               Description of the Parameter
     * @exception  IOException  Description of the Exception
     */
    public void initWriter(OutputStream os) throws IOException {
        zipOutputFileSream = new ZipOutputStream(os);
        String outputFile = "zipped.sdf";
        ZipEntry zipEntry = new ZipEntry(outputFile);
        BasicIOType outType = BasicMoleculeWriter.checkGetOutputType(outputFile);
        zipOutputFileSream.putNextEntry(zipEntry);
        writer = new BasicMoleculeWriter(zipOutputFileSream, outType);
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String inputDescription() {
        return description;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String[] inputFileExtensions() {
        return extensions;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String outputDescription() {
        return description;
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public String[] outputFileExtensions() {
        return extensions;
    }

    /**
     *  Reads an molecule entry as (unparsed) <tt>String</tt> representation.
     *
     * @return                  <tt>null</tt> if the reader contains no more
     *      relevant data. Otherwise the <tt>String</tt> representation of the
     *      whole molecule entry is returned.
     * @exception  IOException  typical IOException
     */
    public String read() throws IOException {
        logger.error("Reading ZIP data as String representation is not implemented yet !!!");
        return null;
    }

    /**
     *  Description of the Method
     *
     * @param  mol              Description of the Parameter
     * @return                  Description of the Return Value
     * @exception  IOException  Description of the Exception
     */
    public boolean read(Molecule mol) throws IOException, MoleculeIOException {
        return read(mol, null);
    }

    /**
     *  Description of the Method
     *
     * @param  mol              Description of the Parameter
     * @param  title            Description of the Parameter
     * @return                  Description of the Return Value
     * @exception  IOException  Description of the Exception
     */
    public boolean read(Molecule mol, String title) throws IOException, MoleculeIOException {
        if (reader == null) {
            return hasNextFileEntry(mol);
        }
        if (reader.readNext(mol)) {
            return true;
        } else {
            if (!hasNextFileEntry(mol)) {
                return false;
            }
            return true;
        }
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public boolean readable() {
        return true;
    }

    public boolean skipReaderEntry() throws IOException {
        return true;
    }

    /**
     *  Description of the Method
     *
     * @param  mol              Description of the Parameter
     * @return                  Description of the Return Value
     * @exception  IOException  Description of the Exception
     */
    public boolean write(Molecule mol) throws IOException, MoleculeIOException {
        return write(mol, null);
    }

    /**
     *  Description of the Method
     *
     * @param  mol              Description of the Parameter
     * @param  title            Description of the Parameter
     * @return                  Description of the Return Value
     * @exception  IOException  Description of the Exception
     */
    public boolean write(Molecule mol, String title) throws IOException, MoleculeIOException {
        return writer.writeNext(mol);
    }

    /**
     *  Description of the Method
     *
     * @return    Description of the Return Value
     */
    public boolean writeable() {
        return true;
    }
}
