package org.openxml4j.opc.internal.marshallers;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.StreamHelper;
import org.openxml4j.opc.internal.ZipHelper;

/**
 * Package core properties marshaller specialized for zipped package.
 * 
 * @author Julien Chable
 * @version 1.0
 */
public class ZipPackagePropertiesMarshaller extends PackagePropertiesMarshaller {

    @Override
    public boolean marshall(PackagePart part, OutputStream out) throws OpenXML4JException {
        if (!(out instanceof ZipOutputStream)) {
            throw new IllegalArgumentException("ZipOutputStream expected!");
        }
        ZipOutputStream zos = (ZipOutputStream) out;
        ZipEntry ctEntry = new ZipEntry(ZipHelper.getZipItemNameFromOPCName(part.getPartName().getURI().toString()));
        try {
            zos.putNextEntry(ctEntry);
            super.marshall(part, out);
            if (!StreamHelper.saveXmlInStream(xmlDoc, out)) {
                return false;
            }
            zos.closeEntry();
        } catch (IOException e) {
            throw new OpenXML4JException(e.getLocalizedMessage());
        }
        return true;
    }
}
