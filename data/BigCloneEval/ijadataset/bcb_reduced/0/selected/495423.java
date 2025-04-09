package org.pubcurator.core.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.TypeSystemUtil;
import org.apache.uima.util.XMLInputSource;
import org.xml.sax.SAXException;

/**
 * @author Kai Schlamp (schlamp@gmx.de)
 *
 */
public class FullCasSerializer {

    public static final String TYPE_SYSTEM_ENTRY = "TypeSystem";

    public static final String CAS_ENTRY = "CAS";

    public static void serialize(CAS cas, TypeSystem typeSystem, OutputStream out) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(out) {

            @Override
            public void close() throws IOException {
            }
        };
        zipOut.setLevel(9);
        ZipEntry typeSystemEntry = new ZipEntry(TYPE_SYSTEM_ENTRY);
        zipOut.putNextEntry(typeSystemEntry);
        TypeSystemDescription typeSystemDescription = TypeSystemUtil.typeSystem2TypeSystemDescription(typeSystem);
        try {
            typeSystemDescription.toXML(zipOut);
        } catch (SAXException e) {
            throw new IOException("Error while serializing type system.", e);
        }
        ZipEntry casEntry = new ZipEntry(CAS_ENTRY);
        zipOut.putNextEntry(casEntry);
        try {
            XmiCasSerializer.serialize(cas, zipOut);
        } catch (SAXException e) {
            throw new IOException("Error while serializing CAS.", e);
        }
        zipOut.finish();
        out.close();
    }

    public static void serialize(CAS cas, URL typeSystemDescriptionUrl, OutputStream out) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(out) {

            @Override
            public void close() throws IOException {
            }
        };
        zipOut.setLevel(9);
        ZipEntry typeSystemEntry = new ZipEntry(TYPE_SYSTEM_ENTRY);
        zipOut.putNextEntry(typeSystemEntry);
        try {
            TypeSystemDescription typeSystemDescription = UIMAFramework.getXMLParser().parseTypeSystemDescription(new XMLInputSource(typeSystemDescriptionUrl));
            typeSystemDescription.toXML(zipOut);
        } catch (SAXException e) {
            throw new IOException("Error while serializing type system.", e);
        } catch (InvalidXMLException e) {
            throw new IOException("Error while serializing type system.", e);
        }
        ZipEntry casEntry = new ZipEntry(CAS_ENTRY);
        zipOut.putNextEntry(casEntry);
        try {
            XmiCasSerializer.serialize(cas, zipOut);
        } catch (SAXException e) {
            throw new IOException("Error while serializing CAS.", e);
        }
        zipOut.finish();
        out.close();
    }
}
