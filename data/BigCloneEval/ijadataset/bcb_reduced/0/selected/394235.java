package net.sf.ahtutils.controller.factory.xml.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import net.sf.ahtutils.xml.mail.Attachment;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlAttachmentFactory {

    static final Logger logger = LoggerFactory.getLogger(EmailAddressXmlFactory.class);

    public static Attachment create(File f) throws IOException, MagicParseException, MagicMatchNotFoundException, MagicException {
        FileInputStream fis = new FileInputStream(f);
        byte[] data = IOUtils.toByteArray(fis);
        fis.close();
        String mimeType = Magic.getMagicMatch(f, false).getMimeType();
        return create(f.getName(), mimeType, data);
    }

    public static Attachment create(String fileName, String mime, byte[] data) {
        net.sf.exlp.xml.io.File file = new net.sf.exlp.xml.io.File();
        file.setName(fileName);
        file.setMime(mime);
        Attachment attachment = new Attachment();
        attachment.setData(data);
        attachment.setFile(file);
        return attachment;
    }
}
