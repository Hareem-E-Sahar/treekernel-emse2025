package com.oroad.stxx.xform;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import com.oroad.stxx.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *  Encodes and decodes information to and from base64-encoded Strings using zip
 *  compression
 *
 *@author    Don Brown
 */
public class ZipEncoder implements Encoder {

    private static final Log log = LogFactory.getLog(ZipEncoder.class);

    /**
     *  Encodes XMLForm's xml and phase information into a string
     *
     *@param  form  The xmlform
     *@return       A base64-encoded string
     */
    public String encodeXMLForm(XMLForm form) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ZipOutputStream zout = new ZipOutputStream(bout);
            zout.setLevel(9);
            if (form.getPhase() != null) {
                zout.putNextEntry(new ZipEntry("phase"));
                byte[] phase = form.getPhase().getBytes();
                zout.write(phase, 0, phase.length);
            }
            zout.putNextEntry(new ZipEntry("xml"));
            form.outputXML(zout);
            zout.close();
            byte[] enc = Base64.encode(bout.toByteArray());
            return new String(enc);
        } catch (IOException ex) {
            throw new XMLException("Unable to encode xml viewstate:" + ex);
        }
    }

    /**
     *  Decodes XMLForm's xml and phase from a base64-encoded string
     *
     *@param  vs     The base64-encoded string
     *@param  xform  The form to populate
     */
    public void decodeXMLForm(String vs, XMLForm xform) {
        if (log.isDebugEnabled()) {
            log.debug("processing viewstate size:" + vs.length());
        }
        try {
            byte[] zipped = Base64.decode(vs.getBytes());
            ByteArrayInputStream bin = new ByteArrayInputStream(zipped);
            ZipInputStream zin = new ZipInputStream(bin);
            byte[] buffer = new byte[128];
            int len = 0;
            ZipEntry entry = zin.getNextEntry();
            if ("phase".equals(entry.getName())) {
                len = zin.read(buffer, 0, buffer.length);
                xform.setPhase(new String(buffer, 0, len));
                entry = zin.getNextEntry();
            }
            if ("xml".equals(entry.getName())) {
                xform.loadState(zin);
            } else {
                log.warn("Cannot find xml zip entry");
            }
        } catch (IOException ex) {
            throw new XMLException("Unable to load viewstate: " + ex);
        }
    }
}
