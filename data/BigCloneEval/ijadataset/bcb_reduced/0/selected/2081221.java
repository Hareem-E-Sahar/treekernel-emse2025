package file2xliff4j;

import f2xutils.*;
import java.io.*;
import java.util.*;
import java.nio.charset.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.regex.*;
import java.util.zip.*;

/**
 * Class to export XLIFF to an OpenOffice.org OpenDocument Text (odt), 
 * Spreadsheet (ods) or Presentation (odp) document.
 * @author Weldon Whipple &lt;weldon@lingotek.com&gt;
 */
public class OOoTextExporter extends OdfExporter implements Converter {

    protected static final int BLKSIZE = 8192;

    private FileType myDataType = null;

    /** Creates a new instance of OdfExporter */
    public OOoTextExporter() {
    }

    /** 
     * Convert one set of targets (in the translation units of an XLIFF file) back
     * to a document in OpenOffice.org OpenDocument Text (odt) format. Use 
     * (besides the XLIFF file) the skeleton and format files that were 
     * generated when the XLIFF file was created.
     * <p>Note: This conversion actually uses the convert method of its superclass 
     * (OdfExporter) to generate a content.&lt;language&gt;.xml file--which is
     * a special target-language-specific version of the "standard" content.xml 
     * file that is found in ZIP-formated OpenOffice.org odt files. After the 
     * superclass creates the content.&lt;language&gt;.xml file, this converter
     * copies the original (used for import) odt file to one that inserts the
     * target-language between the "stem" and the odt extension of the file
     * name. It replaces the original content.xml (in the new odt copy) with
     * the contents of the new content.&lt;language&gt;.xml file.
     * @param mode The mode of conversion (FROM_XLIFF in this case).
     * @param language The language of the XLIFF target to use in constructing
     *        the ODF document. The language is used in constructing a unique
     *        name for the output file. For example, if ja_JP is specified and
     *        the original input file was named myfile.odt, the output file name
     *        is myfile.ja_jp.odt. (Note that the Java Locale's toString
     *        method lowercases the characters of language codes.)
     * @param phaseName The name of the phase to export. If this parameter's
     *        value is not null, it is matched against the value of the 
     *        optional phase-name attribute of the target elements of the
     *        XLIFF document. If null, no check is made against a phase-name
     *        attribute.
     *        <p>If the phase name string consists entirely of numeric digit(s) 
     *        equivalent to an integer with value greater than 1 but less than 
     *        or equal to maxPhase (see next parameter) search for targets with 
     *        lower numbered phase names.
     * @param maxPhase The maximum phase number. If phaseName is specified as 
     *        "0" and maxPhase is a non-negative integer, search for the highest 
     *        "numbered" phase, starting at maxPhase, and searching down to phase
     *        "1".
     * @param nativeEncoding The encoding of the native document. This parameter
     *        is ignored for OpenDocument Format, since the content.xml file
     *        (which we will place in the new odt file) will always be encoded in
     *        UTF-8.
     * @param nativeFileType This parameter is ignored. For export, the original
     *        native file type is stored in the XLIFF, and is retrieved from that
     *        location.
     * @param nativeFileName The name of the original native document that was
     *        first converted to XLIFF. If it was an OpenOffice.org OpenDocument
     *        Text file, it ends in ".odt". (It might also end with something other
     *        than ".odt")
     * @param baseDir The directory (in the file system) from which input files
     *        (XLIFF, skeleton and format files) will be read, and to which the
     *        output file will be written.
     * @param notifier Instance of a class that implements the Notifier
     *        interface (to send notifications in case of conversion error).
     * @param boundary (Ignored. The boundary on which to segment translation 
     *        units (e.g., on paragraph or sentence boundaries) is meaningful
     *        only for importers--converters that generate XLIFF from documents.)
     * @param generatedFileName If non-null, the converter will write the name
     *        of the file (without parent directories) to which the generated
     *        output file was written.
     * @return Indicator of the status of the conversion.
     * @throws file2xliff4j.ConversionException
     *         If a conversion exception is encountered.
     */
    public ConversionStatus convert(ConversionMode mode, Locale language, String phaseName, int maxPhase, Charset nativeEncoding, FileType nativeFileType, String nativeFileName, String baseDir, Notifier notifier, SegmentBoundary boundary, StringWriter generatedFileName) throws ConversionException {
        ConversionStatus status = ConversionStatus.CONVERSION_SUCCEEDED;
        if (!mode.equals(ConversionMode.FROM_XLIFF)) {
            throw new ConversionException("The OpenOffice.org Text Exporter supports" + " only conversions from XLIFF to OpenDocument Text format.");
        }
        if ((language == null) || (nativeFileName == null) || (nativeFileName.length() == 0) || (baseDir == null) || (baseDir.length() == 0)) {
            throw new ConversionException("Required parameter(s)" + " omitted, incomplete or incorrect.");
        }
        if (xliffOriginalFileName == null || xliffOriginalFileName.length() == 0) {
            xliffOriginalFileName = nativeFileName;
        }
        status = super.convert(mode, language, phaseName, maxPhase, nativeEncoding, nativeFileType, xliffOriginalFileName, baseDir, notifier, boundary, null);
        String oldOdtFileName = "";
        String newOdtFileName = "";
        if (xliffOriginalFileName.toLowerCase().endsWith(".odt")) {
            oldOdtFileName = baseDir + File.separator + xliffOriginalFileName;
            int extPos = xliffOriginalFileName.toLowerCase().lastIndexOf(".odt");
            newOdtFileName = baseDir + File.separator + xliffOriginalFileName.substring(0, extPos) + "." + language.toString() + ".odt";
            if (generatedFileName != null) {
                generatedFileName.write(xliffOriginalFileName.substring(0, extPos) + "." + language.toString() + ".odt");
            }
        } else if (xliffOriginalFileName.toLowerCase().endsWith(".ods")) {
            oldOdtFileName = baseDir + File.separator + xliffOriginalFileName;
            int extPos = xliffOriginalFileName.toLowerCase().lastIndexOf(".ods");
            newOdtFileName = baseDir + File.separator + xliffOriginalFileName.substring(0, extPos) + "." + language.toString() + ".ods";
            if (generatedFileName != null) {
                generatedFileName.write(xliffOriginalFileName.substring(0, extPos) + "." + language.toString() + ".ods");
            }
        } else if (xliffOriginalFileName.toLowerCase().endsWith(".odp")) {
            oldOdtFileName = baseDir + File.separator + xliffOriginalFileName;
            int extPos = xliffOriginalFileName.toLowerCase().lastIndexOf(".odp");
            newOdtFileName = baseDir + File.separator + xliffOriginalFileName.substring(0, extPos) + "." + language.toString() + ".odp";
            if (generatedFileName != null) {
                generatedFileName.write(xliffOriginalFileName.substring(0, extPos) + "." + language.toString() + ".odp");
            }
        } else if (xliffOriginalFileName.toLowerCase().endsWith(".ppt")) {
            oldOdtFileName = baseDir + File.separator + xliffOriginalFileName + ".odp";
            newOdtFileName = baseDir + File.separator + xliffOriginalFileName + "." + language.toString() + ".odp";
            if (generatedFileName != null) {
                generatedFileName.write(xliffOriginalFileName + "." + language.toString() + ".odp");
            }
        } else if (xliffOriginalFileName.toLowerCase().endsWith(".xls")) {
            oldOdtFileName = baseDir + File.separator + xliffOriginalFileName + ".ods";
            newOdtFileName = baseDir + File.separator + xliffOriginalFileName + "." + language.toString() + ".ods";
            if (generatedFileName != null) {
                generatedFileName.write(xliffOriginalFileName + "." + language.toString() + ".ods");
            }
        } else if (xliffOriginalFileName.toLowerCase().endsWith(".doc")) {
            oldOdtFileName = baseDir + File.separator + xliffOriginalFileName + ".odt";
            newOdtFileName = baseDir + File.separator + xliffOriginalFileName + "." + language.toString() + ".odt";
            if (generatedFileName != null) {
                generatedFileName.write(xliffOriginalFileName + "." + language.toString() + ".odt");
            }
        } else if (xliffOriginalFileName.toLowerCase().endsWith(".rtf")) {
            oldOdtFileName = baseDir + File.separator + xliffOriginalFileName + ".odt";
            newOdtFileName = baseDir + File.separator + xliffOriginalFileName + "." + language.toString() + ".odt";
            if (generatedFileName != null) {
                generatedFileName.write(xliffOriginalFileName + "." + language.toString() + ".odt");
            }
        } else {
            oldOdtFileName = baseDir + File.separator + xliffOriginalFileName;
            newOdtFileName = baseDir + File.separator + xliffOriginalFileName + "." + language.toString();
            if (generatedFileName != null) {
                generatedFileName.write(xliffOriginalFileName + "." + language.toString());
            }
        }
        try {
            byte[] byteBuf = new byte[BLKSIZE];
            int numRead;
            ZipFile odfZipFile = new ZipFile(oldOdtFileName);
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(newOdtFileName));
            Enumeration all = odfZipFile.entries();
            while (all.hasMoreElements()) {
                ZipEntry nextFile = (ZipEntry) all.nextElement();
                if (nextFile.getName().equals("content.xml")) {
                    zipOut.putNextEntry(new ZipEntry("content.xml"));
                    InputStream inOdt = new FileInputStream(baseDir + File.separator + "content." + language.toString() + ".xml");
                    while ((numRead = inOdt.read(byteBuf)) != -1) {
                        zipOut.write(byteBuf, 0, numRead);
                    }
                    inOdt.close();
                    zipOut.flush();
                } else if (nextFile.getName().equals("styles.xml")) {
                    zipOut.putNextEntry(new ZipEntry("styles.xml"));
                    StringBuilder stylesBuf = new StringBuilder();
                    File stylesLl = new File(baseDir + File.separator + "styles." + language.toString() + ".xml");
                    InputStreamReader inStyles = null;
                    if (stylesLl.exists()) {
                        inStyles = new InputStreamReader(new FileInputStream(baseDir + File.separator + "styles." + language.toString() + ".xml"), Charset.forName("UTF-8"));
                    } else {
                        inStyles = new InputStreamReader(odfZipFile.getInputStream(nextFile), Charset.forName("UTF-8"));
                    }
                    int b;
                    char[] charBuf = new char[BLKSIZE];
                    while ((numRead = inStyles.read(charBuf)) != -1) {
                        stylesBuf.append(charBuf, 0, numRead);
                    }
                    String stylesContent = stylesBuf.toString().replaceAll("(?s)(fo:language|number:language|style:language-complex|style:language-asian)=(['\"])[^'\"]*\\2", "$1=$2" + language.getLanguage() + "$2");
                    stylesContent = stylesContent.replaceAll("(?s)(fo:country|number:country|style:country-complex|style:country-asian)=(['\"])[^'\"]*\\2", "$1=$2" + language.getCountry() + "$2");
                    CharsetEncoder c2b = Charset.forName("UTF-8").newEncoder();
                    ByteBuffer stylesInBytes = c2b.encode(CharBuffer.wrap(stylesContent.subSequence(0, stylesContent.length()), 0, stylesContent.length()));
                    int numBytes = stylesInBytes.limit();
                    zipOut.write(stylesInBytes.array(), 0, numBytes);
                    inStyles.close();
                    zipOut.flush();
                } else if (nextFile.getName().equals("mimetype")) {
                    ZipEntry nextOutFile = new ZipEntry(nextFile.getName());
                    nextOutFile.setMethod(ZipEntry.STORED);
                    nextOutFile.setSize(nextFile.getSize());
                    nextOutFile.setCrc(nextFile.getCrc());
                    zipOut.putNextEntry(nextOutFile);
                    InputStream zin = odfZipFile.getInputStream(nextFile);
                    while ((numRead = zin.read(byteBuf)) != -1) {
                        zipOut.write(byteBuf, 0, numRead);
                    }
                    zipOut.flush();
                    zin.close();
                } else if (nextFile.getName().equals("meta.xml")) {
                    zipOut.putNextEntry(new ZipEntry("meta.xml"));
                    StringBuilder metaBuf = new StringBuilder();
                    InputStreamReader zin = new InputStreamReader(odfZipFile.getInputStream(nextFile));
                    int b;
                    char[] charBuf = new char[BLKSIZE];
                    while ((numRead = zin.read(charBuf)) != -1) {
                        metaBuf.append(charBuf, 0, numRead);
                    }
                    String metaContent = metaBuf.toString().replaceFirst("(?s)^(.*?<dc:language>).*?(</dc:language>.*)$", "$1" + language.toString() + "$2");
                    for (int i = 0; i < metaContent.length(); i++) {
                        zipOut.write(metaContent.charAt(i));
                    }
                    zipOut.flush();
                    zin.close();
                } else {
                    ZipEntry nextOutFile = new ZipEntry(nextFile.getName());
                    zipOut.putNextEntry(nextOutFile);
                    InputStream zin = odfZipFile.getInputStream(nextFile);
                    while ((numRead = zin.read(byteBuf)) != -1) {
                        zipOut.write(byteBuf, 0, numRead);
                    }
                    zipOut.flush();
                    zin.close();
                }
                zipOut.closeEntry();
            }
            odfZipFile.close();
            zipOut.close();
        } catch (IOException e) {
            System.err.println("Cannot create (ZIP format) OpenOffice Text file " + newOdtFileName + ": " + e.getMessage());
            throw new ConversionException("Cannot create (ZIP format) OpenOffice Text file " + newOdtFileName + ": " + e.getMessage());
        }
        return status;
    }

    /** 
     * Convert one set of targets (in the translation units of an XLIFF file) back
     * to a document in OpenOffice.org OpenDocument Text (odt) format. Use 
     * (besides the XLIFF file) the skeleton and format files that were 
     * generated when the XLIFF file was created.
     * <p>Note: This conversion actually uses the convert method of its superclass 
     * (OdfExporter) to generate a content.&lt;language&gt;.xml file--which is
     * a special target-language-specific version of the "standard" content.xml 
     * file that is found in ZIP-formated OpenOffice.org odt files. After the 
     * superclass creates the content.&lt;language&gt;.xml file, this converter
     * copies the original (used for import) odt file to one that inserts the
     * target-language between the "stem" and the odt extension of the file
     * name. It replaces the original content.xml (in the new odt copy) with
     * the contents of the new content.&lt;language&gt;.xml file.
     * @param mode The mode of conversion (FROM_XLIFF in this case).
     * @param language The language of the XLIFF targets to use in constructing
     *        the ODF document. The language is used in constructing a unique
     *        name for the output file. For example, if ja_JP is specified and
     *        the original input file was named myfile.odt, the output file name
     *        is myfile.ja_jp.odt. (Note that the Java Locale's toString
     *        method lowercases the characters of language codes.)
     * @param phaseName The name of the phase to export. If this parameter's
     *        value is not null, it is matched against the value of the 
     *        optional phase-name attribute of the target elements of the
     *        XLIFF document. If null, no check is made against a phase-name
     *        attribute.
     *        <p>If the phase name string consists entirely of numeric digit(s) 
     *        equivalent to an integer with value greater than 1 but less than 
     *        or equal to maxPhase (see next parameter) search for targets with 
     *        lower numbered phase names.
     * @param maxPhase The maximum phase number. If phaseName is specified as 
     *        "0" and maxPhase is a non-negative integer, search for the highest 
     *        "numbered" phase, starting at maxPhase, and searching down to phase
     *        "1".
     * @param nativeEncoding The encoding of the native document. This parameter
     *        is ignored for OpenDocument Format, since the content.xml file
     *        (which we will place in the new odt file) will always be encoded in
     *        UTF-8.
     * @param nativeFileType This parameter is ignored. For export, the original
     *        native file type is stored in the XLIFF, and is retrieved from that
     *        location.
     * @param nativeFileName The name of the original native document that was
     *        first converted to XLIFF. If it was an OpenOffice.org OpenDocument
     *        Text file, it ends in ".odt". (It might also end with something other
     *        than ".odt")
     * @param baseDir The directory (in the file system) from which input files
     *        (XLIFF, skeleton and format files) will be read, and to which the
     *        output file will be written.
     * @param notifier Instance of a class that implements the Notifier
     *        interface (to send notifications in case of conversion error).
     * @param boundary (Ignored. The boundary on which to segment translation 
     *        units (e.g., on paragraph or sentence boundaries) is meaningful
     *        only for importers--converters that generate XLIFF from documents.)
     * @param generatedFileName If non-null, the converter will write the name
     *        of the file (without parent directories) to which the generated
     *        output file was written.
     * @param skipList (Not used by this converter.)
     * @return Indicator of the status of the conversion.
     * @throws file2xliff4j.ConversionException
     *         If a conversion exception is encountered.
     */
    public ConversionStatus convert(ConversionMode mode, Locale language, String phaseName, int maxPhase, Charset nativeEncoding, FileType nativeFileType, String nativeFileName, String baseDir, Notifier notifier, SegmentBoundary boundary, StringWriter generatedFileName, Set<XMLTuXPath> skipList) throws ConversionException {
        return this.convert(mode, language, phaseName, maxPhase, nativeEncoding, nativeFileType, nativeFileName, baseDir, notifier, boundary, generatedFileName);
    }

    /** 
     * Convert one set of targets (in the translation units of an XLIFF file) back
     * to a document in OpenOffice.org OpenDocument Text (odt) format. Use 
     * (besides the XLIFF file) the skeleton and format files that were 
     * generated when the XLIFF file was created.
     * <p>Note: This conversion actually uses the convert method of its superclass 
     * (OdfExporter) to generate a content.&lt;language&gt;.xml file--which is
     * a special target-language-specific version of the "standard" content.xml 
     * file that is found in ZIP-formated OpenOffice.org odt files. After the 
     * superclass creates the content.&lt;language&gt;.xml file, this converter
     * copies the original (used for import) odt file to one that inserts the
     * target-language between the "stem" and the odt extension of the file
     * name. It replaces the original content.xml (in the new odt copy) with
     * the contents of the new content.&lt;language&gt;.xml file.
     * @param mode The mode of conversion (FROM_XLIFF in this case).
     * @param language The language of the XLIFF targets to use in constructing
     *        the ODF document. The language is used in constructing a unique
     *        name for the output file. For example, if ja_JP is specified and
     *        the original input file was named myfile.odt, the output file name
     *        is myfile.ja_jp.odt. (Note that the Java Locale's toString
     *        method lowercases the characters of language codes.)
     * @param phaseName The name of the phase to export. If this parameter's
     *        value is not null, it is matched against the value of the 
     *        optional phase-name attribute of the target elements of the
     *        XLIFF document. If null, no check is made against a phase-name
     *        attribute.
     *        <p>If the phase name string consists entirely of numeric digit(s) 
     *        equivalent to an integer with value greater than 1 but less than 
     *        or equal to maxPhase (see next parameter) search for targets with 
     *        lower numbered phase names.
     * @param maxPhase The maximum phase number. If phaseName is specified as 
     *        "0" and maxPhase is a non-negative integer, search for the highest 
     *        "numbered" phase, starting at maxPhase, and searching down to phase
     *        "1".
     * @param nativeEncoding The encoding of the native document. This parameter
     *        is ignored for OpenDocument Format, since the content.xml file
     *        (which we will place in the new odt file) will always be encoded in
     *        UTF-8.
     * @param nativeFileType This parameter is ignored. For export, the original
     *        native file type is stored in the XLIFF, and is retrieved from that
     *        location.
     * @param nativeFileName The name of the original native document that was
     *        first converted to XLIFF. If it was an OpenOffice.org OpenDocument
     *        Text file, it ends in ".odt". (It might also end with something other
     *        than ".odt")
     * @param baseDir The directory (in the file system) from which input files
     *        (XLIFF, skeleton and format files) will be read, and to which the
     *        output file will be written.
     * @param notifier Instance of a class that implements the Notifier
     *        interface (to send notifications in case of conversion error).
     * @return Indicator of the status of the conversion.
     * @throws file2xliff4j.ConversionException
     *         If a conversion exception is encountered.
     */
    @Deprecated
    public ConversionStatus convert(ConversionMode mode, Locale language, String phaseName, int maxPhase, Charset nativeEncoding, FileType nativeFileType, String nativeFileName, String baseDir, Notifier notifier) throws ConversionException {
        return this.convert(mode, language, phaseName, maxPhase, nativeEncoding, nativeFileType, nativeFileName, baseDir, notifier, null, null);
    }

    /** 
     * Return an object representing a format-specific (and converter-specific) 
     * property.
     * @param property The name of the property to return.
     * @return An Object that represents the property's value.
     */
    public Object getConversionProperty(String property) {
        return null;
    }

    /** 
     * Return the file type that this converter handles. (For importers, this
     * means the file type that it imports <i>to</i> XLIFF; for exporters, it
     * is the file type that ie exports to (from XLIFF).
     * @return the ODT file type.
     */
    public FileType getFileType() {
        if (this.myDataType != null) {
            return this.myDataType;
        } else {
            return FileType.ODT;
        }
    }

    /**
     * Set a format-specific property that might affect the way that the
     * conversion occurs.
     * <p><i>Note:</i> This converter needs no format-specific properties.
     * If any are passed, they will be silently ignored.
     * @param property The name of the property
     * @param value The value of the property
     * @throws file2xliff4j.ConversionException
     *         If the property isn't recognized (and if it matters).
     */
    public void setConversionProperty(String property, Object value) throws ConversionException {
        if (property.equals("http://www.lingotek.com/converters/properties/datatype")) {
            if (value != null) {
                this.myDataType = (FileType) value;
            }
        }
        return;
    }
}
