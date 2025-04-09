package org.acid3lib.spec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import org.acid3lib.Bytes;
import org.acid3lib.FrameDefinition;
import org.acid3lib.FrameProperty;
import org.acid3lib.FramePropertyDefinition;
import org.acid3lib.ID3Constants;
import org.acid3lib.ID3v2Frame;
import org.acid3lib.ID3v2ParseException;
import org.acid3lib.ID3v2Restrictions;
import org.acid3lib.ID3v2Tag;

/**
 * The spec for the infomal standard 2.4.0.
 * @author Jascha Ulrich
 */
public class Spec240 extends Spec230 implements Serializable {

    public Spec240() {
        super();
        changeFrameDefinitionID(ID3v2Frame.YEAR, "TDRC");
        registerFrameDefinition(ID3v2Frame.MOOD, new FrameDefinition(propertiesTextFrame, "TMOO"));
        registerFrameDefinition(ID3v2Frame.SET_SUBTITLE, new FrameDefinition(propertiesTextFrame, "TSST"));
        registerFrameDefinition(ID3v2Frame.TAGGING_TIME, new FrameDefinition(propertiesTextFrame, "TDTG"));
        registerFrameDefinition(ID3v2Frame.SEEK, new FrameDefinition(propertiesUndefinedFrame, "SEEK"));
        registerFrameDefinition(ID3v2Frame.SIGNATURE, new FrameDefinition(propertiesSigFrame, "SIGN"));
    }

    public int getVersion() {
        return 4;
    }

    public int getRevision() {
        return 0;
    }

    public ID3v2Tag readTag(InputStream in) throws ID3v2ParseException, IOException {
        ID3v2Tag tag = new ID3v2Tag(this);
        CRC32 crc32 = new CRC32();
        ID3v2Header header = new ID3v2Header();
        int position = read(header, in);
        if (header.useCRC) {
            logger.info("Spec240::readTag(InputStream): " + "A CRC32 (" + header.crc + ") was found");
            in = new CheckedInputStream(in, crc32);
        }
        tag.setUseFooter(header.useFooter);
        tag.setUseUnsynchronisation(header.useUnsynchronisation);
        tag.setUseCRC(header.useCRC);
        tag.setExperimental(header.isExperimental);
        tag.setUpdate(header.isUpdate);
        tag.setRestrictions(header.restrictions);
        logger.debug("Spec240::readTag(InputStream): " + "Reading frames.");
        FrameReader frameReader = new FrameReader(tag, in);
        while (position < header.tagSize) {
            int frameSize;
            ID3v2Frame currentFrame = frameReader.readFrame(true);
            if (currentFrame == null) break;
            if (currentFrame.getType() == ID3v2Frame.UNDEFINED_FRAME_TYPE) logger.info("Spec240::readTag(InputStream): " + "Frame('" + currentFrame.getID() + "').getType() == ID3v2Frame.UNDEFINED_FRAME_TYPE.");
            position += frameReader.getFrameSize();
            tag.add(currentFrame);
        }
        logger.debug("Spec240::readTag(InputStream): " + "Read " + tag.getFrameCount() + " frames.");
        int paddingSize = header.tagSize - position;
        if (paddingSize >= 4) paddingSize = paddingSize - 4;
        in.skip(paddingSize);
        logger.debug("Spec240::readTag(InputStream): " + "Read " + paddingSize + " bytes of padding.");
        if (header.useCRC) {
            if (header.crc != crc32.getValue()) {
                String msg = "Wrong CRC32 (computed value: " + crc32.getValue() + "; value found: " + header.crc;
                logger.error("Spec240::readTag(InputStream): " + msg);
                throw new ID3v2ParseException(msg);
            } else {
                logger.debug("Spec240::readTag(InputStream): CRC is correct.");
            }
        }
        if (header.useFooter) {
            logger.debug("Spec240::readTag(InputStream): Footer is present");
            if (paddingSize != 0) throw new ID3v2ParseException("Tag must not have padding if a footer is present");
            ID3v2Header footer = toFooter(header);
            read(footer, in);
            position += getSize(footer);
        }
        setTagSize(tag, position);
        tag.setPreferredSize(header.tagSize, ID3Constants.ABSOLUTE);
        return tag;
    }

    public int read(ID3v2Header header, InputStream in) throws ID3v2ParseException, IOException {
        String methodName = "Spec240::read(ID3v2Header,InputStream)";
        int bytesRead;
        byte headerData[] = new byte[10];
        boolean extended;
        bytesRead = in.read(headerData, 0, 10);
        Bytes.checkRead(bytesRead, headerData.length, methodName);
        int tagSizeWithoutHeader = (int) Bytes.convertLong(headerData, 7, 6, 4);
        int tagSize = (header.useFooter ? tagSizeWithoutHeader + 20 : tagSizeWithoutHeader + 10);
        logger.debug(methodName + ": According to the size descriptor, " + "the tag should be " + tagSize + " bytes big.");
        byte flags = headerData[5];
        header.identifier = new String(headerData, 0, 3);
        header.useUnsynchronisation = Bytes.testBit(flags, 7);
        header.isExperimental = Bytes.testBit(flags, 5);
        header.useFooter = Bytes.testBit(flags, 4);
        header.tagSize = tagSize;
        extended = Bytes.testBit(flags, 6);
        if (extended) {
            boolean restricted;
            boolean update;
            boolean crcPresent;
            byte sizeDescriptor[] = new byte[4];
            bytesRead = in.read(sizeDescriptor, 0, 4);
            Bytes.checkRead(bytesRead, sizeDescriptor.length, methodName);
            int size = (int) Bytes.convertLong(sizeDescriptor, 7, 0, 4);
            byte data[] = new byte[size - 4];
            in.read(data);
            if (data[0] != 0x01) throw new ID3v2ParseException("In extended header: flag count descriptor != 0x01");
            byte xflags = data[1];
            update = Bytes.testBit(xflags, 6);
            crcPresent = Bytes.testBit(xflags, 5);
            restricted = Bytes.testBit(xflags, 4);
            int index = 2;
            if (update) {
                ++index;
                header.isUpdate = true;
            }
            if (crcPresent) {
                ++index;
                header.useCRC = true;
                header.crc = Bytes.convertLong(data, 7, index, 5);
                index += 5;
            }
            if (restricted) {
                ++index;
                header.restrictions = readRestrictions(data[index]);
            }
        }
        return getSize(header);
    }

    public int write(ID3v2Tag tag, OutputStream out) throws IOException {
        ID3v2Header header = constructHeaderInformation(tag);
        int nbytes = 0;
        if (header.useCRC) {
            logger.info("Spec240::write(ID3v2Tag,OutputStream): " + "Writing tag to ByteArrayOutputStream for CRC32 computation.");
            CRC32 crc32 = new CRC32();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            CheckedOutputStream cout = new CheckedOutputStream(bout, crc32);
            nbytes += writeTagContent(tag, cout);
            header.crc = crc32.getValue();
            header.tagSize = nbytes + getSize(header);
            logger.info("Spec240::write(ID3v2Tag,OutputStream): " + "Written CRC32: " + crc32.getValue() + "; " + "Written tag size: " + header.tagSize);
            nbytes += write(header, out);
            out.write(bout.toByteArray());
        } else {
            header.tagSize = tag.getSize();
            logger.debug("Spec240::write(ID3v2Tag,OutputStream): " + "Calculated tag size: " + header.tagSize);
            nbytes += write(header, out);
            nbytes += writeTagContent(tag, out);
        }
        if (header.useFooter) {
            ID3v2Header footer = toFooter(header);
            nbytes += write(footer, out);
        }
        return nbytes;
    }

    public int write(ID3v2Frame frame, OutputStream out) throws IOException {
        byte[] flags = new byte[] { 0, 0 };
        int size;
        FrameContent content;
        size = getSize(frame);
        content = getContent(frame);
        if (frame.getID() == null) logger.info("Spec240::write(ID3v2Frame,OutputStream): " + "frame.getID() == null");
        byte[] sizeDescriptor = Bytes.toByteArray(size - 10, 7, 4);
        byte[] byteId = frame.getID().getBytes("8859_1");
        flags[0] = (byte) Bytes.setBit(flags[0], 6, frame.getTagAlterDiscard());
        flags[0] = (byte) Bytes.setBit(flags[0], 5, frame.getFileAlterDiscard());
        flags[0] = (byte) Bytes.setBit(flags[0], 4, frame.isReadOnly());
        flags[1] = (byte) Bytes.setBit(flags[1], 6, frame.getUseGroupID());
        flags[1] = (byte) Bytes.setBit(flags[1], 3, content.isCompressed);
        flags[1] = (byte) Bytes.setBit(flags[1], 2, frame.getUseEncryption());
        flags[1] = (byte) Bytes.setBit(flags[1], 1, frame.getUseUnsynchronisation());
        flags[1] = (byte) Bytes.setBit(flags[1], 0, useDataLength(frame));
        out.write(byteId, 0, 4);
        out.write(sizeDescriptor, 0, 4);
        out.write(flags, 0, 2);
        if (frame.getUseGroupID()) out.write(frame.getGroupID());
        if (frame.getUseEncryption()) out.write(frame.getEncryptionMethod());
        if (useDataLength(frame)) out.write(Bytes.toByteArray(content.dataLength, 7, 4), 0, 4);
        out.write(content.data, 0, content.data.length);
        return size;
    }

    private int write(ID3v2Header header, OutputStream out) throws IOException {
        byte[] id = header.identifier.getBytes();
        byte flags = 0x00;
        flags = (byte) Bytes.setBit(flags, 7, header.useUnsynchronisation);
        flags = (byte) Bytes.setBit(flags, 6, isExtended(header));
        flags = (byte) Bytes.setBit(flags, 5, header.isExperimental);
        flags = (byte) Bytes.setBit(flags, 4, header.useFooter);
        out.write(id, 0, id.length);
        out.write(0x04);
        out.write(0x00);
        out.write(flags);
        long size = header.useFooter ? header.tagSize - 20 : header.tagSize - 10;
        byte[] sizeDescriptor = Bytes.toByteArray(size, 7, 4);
        out.write(sizeDescriptor);
        if (isExtended(header)) {
            int xsize = getSize(header) - 10;
            byte xflags = 0x00;
            xflags = (byte) Bytes.setBit(xflags, 6, header.isUpdate);
            xflags = (byte) Bytes.setBit(xflags, 5, header.useCRC);
            xflags = (byte) Bytes.setBit(xflags, 4, header.restrictions != null);
            out.write(Bytes.toByteArray(xsize, 7, 4));
            out.write(0x01);
            out.write(xflags);
            if (header.isUpdate) out.write(0x00);
            if (header.useCRC) {
                out.write(0x05);
                long crc = header.crc;
                out.write(Bytes.toByteArray(crc, 7, 5));
            }
            if (header.restrictions != null) {
                out.write(0x01);
                out.write(toByteValue(header.restrictions));
            }
        }
        return getSize(header);
    }

    private ID3v2Header constructHeaderInformation(ID3v2Tag tag) {
        ID3v2Header header = new ID3v2Header();
        header.isExperimental = tag.isExperimental();
        header.isUpdate = tag.isUpdate();
        header.useCRC = tag.getUseCRC();
        header.useUnsynchronisation = tag.getUseUnsynchronisation();
        header.restrictions = tag.getRestrictions();
        return header;
    }

    private boolean getFileAlterDiscard(ID3v2Frame f) {
        int type = f.getType();
        String id = f.getID();
        if (id.equals("ASPI") || id.equals("AENC") || id.equals("ETCO") || id.equals("EQU2") || id.equals("MLLT") || id.equals("POSS") || type == ID3v2Frame.SEEK || type == ID3v2Frame.SYNCHRONISED_LYRICS || id.equals("SYTC") || id.equals("RVA2") || type == ID3v2Frame.ENCODED_BY || type == ID3v2Frame.LENGTH) {
            return true;
        }
        return false;
    }

    private boolean getTagAlterDiscard(ID3v2Frame f) {
        return false;
    }

    private boolean useDataLength(ID3v2Frame f) {
        if (f.getUseCompression()) return true;
        return false;
    }

    protected FrameContent getContent(ID3v2Frame frame) {
        FrameContent frameContent = super.getContent(frame);
        if (frame.getUseUnsynchronisation()) {
            frameContent.data = Bytes.unsynchronise(frameContent.data);
        }
        return frameContent;
    }

    protected boolean getCalculateContentSize(ID3v2Frame frame) {
        if (frame.getUseCompression() || frame.getUseEncryption() || frame.getUseUnsynchronisation()) return false;
        return true;
    }

    private int writeTagContent(ID3v2Tag tag, OutputStream out) throws IOException {
        ID3v2Header header = constructHeaderInformation(tag);
        logger.debug("Spec240::writeTagContent(ID3v2Tag,OutputStream): " + "Writing frames.");
        int nbytes = 0;
        for (Iterator i = tag.frames(); i.hasNext(); ) {
            ID3v2Frame frame = (ID3v2Frame) i.next();
            nbytes += write(frame, out);
        }
        logger.debug("Spec240::writeTagContent(ID3v2Tag,OutputStream): " + "Wrote " + tag.getFrameCount() + " frames (" + nbytes + " bytes).");
        int paddingSize = tag.getPreferredSizeMode() == ID3Constants.ABSOLUTE ? tag.getPreferredSize() - nbytes - getSize(header) : tag.getPreferredSize();
        if (paddingSize > 0) {
            for (int i = 0; i < paddingSize; ++i) out.write(0x00);
            nbytes += paddingSize;
            logger.debug("Spec240::writeTagContent(ID3v2Tag,OutputStream): " + "Wrote " + paddingSize + " bytes of padding");
        }
        return nbytes;
    }

    private ID3v2Restrictions readRestrictions(byte code) throws ID3v2ParseException {
        ID3v2Restrictions restrictions = new ID3v2Restrictions();
        restrictions.setImageEncodingRestriction(Bytes.testBit(code, 2));
        restrictions.setTextEncodingRestriction(Bytes.testBit(code, 5));
        int maximumFrameCount;
        int maximumStringLength;
        long maximumTagSize;
        int maximumImageSize;
        switch(Bytes.getBits(code, 6, 2)) {
            case 0:
                maximumFrameCount = 128;
                break;
            case 1:
                maximumFrameCount = 64;
                break;
            case 2:
            case 3:
                maximumFrameCount = 32;
                break;
            default:
                throw new ID3v2ParseException();
        }
        switch(Bytes.getBits(code, 6, 2)) {
            case 0:
                maximumTagSize = 1024 * 1000000;
                break;
            case 1:
                maximumTagSize = 1024 * 128;
                break;
            case 2:
                maximumTagSize = 1024 * 40;
                break;
            case 3:
                maximumTagSize = 1024 * 4;
                break;
            default:
                throw new ID3v2ParseException();
        }
        switch(Bytes.getBits(code, 0, 2)) {
            case 0:
                maximumImageSize = ID3v2Restrictions.NONE;
                break;
            case 1:
                maximumImageSize = 256;
                break;
            case 2:
                maximumImageSize = 64;
                break;
            case 3:
                maximumImageSize = 32;
                break;
            default:
                throw new ID3v2ParseException();
        }
        switch(Bytes.getBits(code, 3, 2)) {
            case 0:
                maximumStringLength = ID3v2Restrictions.NONE;
                break;
            case 1:
                maximumStringLength = 1024;
                break;
            case 2:
                maximumStringLength = 128;
                break;
            case 3:
                maximumStringLength = 30;
                break;
            default:
                throw new ID3v2ParseException();
        }
        restrictions.setMaximumImageSize(maximumImageSize);
        restrictions.setMaximumStringLength(maximumStringLength);
        return restrictions;
    }

    private byte toByteValue(ID3v2Restrictions restrictions) {
        byte code = 0;
        int value = 0;
        switch(restrictions.getMaximumFrameCount()) {
            case 128:
                value = 0;
                break;
            case 64:
                value = 1;
                break;
            case 32:
                value = (restrictions.getMaximumTagSize() == 1024 * 40 ? 2 : 3);
                break;
        }
        code = (byte) Bytes.setBitsToValue(code, 6, value);
        switch(restrictions.getMaximumImageSize()) {
            case ID3v2Restrictions.NONE:
                value = 0;
                break;
            case 256:
                value = 1;
                break;
            case 64:
                value = 2;
                break;
            case 32:
                value = 3;
                break;
        }
        code = (byte) Bytes.setBitsToValue(code, 0, value);
        switch(restrictions.getMaximumStringLength()) {
            case ID3v2Restrictions.NONE:
                value = 0;
                break;
            case 1024:
                value = 1;
                break;
            case 128:
                value = 2;
                break;
            case 30:
                value = 3;
                break;
        }
        code = (byte) Bytes.setBitsToValue(code, 3, value);
        return code;
    }

    private boolean isExtended(ID3v2Header header) {
        if (header.isUpdate || header.useCRC || header.restrictions != null) return true;
        return false;
    }

    private ID3v2Header toFooter(ID3v2Header header) {
        ID3v2Header footer = (ID3v2Header) header.clone();
        footer.useCRC = false;
        footer.isUpdate = false;
        footer.restrictions = null;
        footer.identifier = "3DI";
        return footer;
    }

    public int getSize(ID3v2Tag tag) {
        logger.debug("Spec240::getSize(ID3v2Tag): Calculating tag size.");
        ID3v2Header header = constructHeaderInformation(tag);
        int size = getSize(header);
        for (Iterator i = tag.frames(); i.hasNext(); ) {
            ID3v2Frame f = (ID3v2Frame) i.next();
            size += getSize(f);
        }
        if (header.useFooter) size += 10;
        return size;
    }

    public int getSize(ID3v2Frame frame) {
        int size = 10 + getContentSize(frame);
        if (frame.getUseGroupID()) ++size;
        if (frame.getUseEncryption()) ++size;
        if (useDataLength(frame)) size += 4;
        return size;
    }

    private int getSize(ID3v2Header header) {
        int size = 10;
        if (isExtended(header)) {
            size += 4 + 1 + 1;
            if (header.isUpdate) ++size;
            if (header.useCRC) size += 1 + 5;
            if (header.restrictions != null) size += 1 + 1;
        }
        return size;
    }

    public boolean supports(int propertyType, String value) {
        if (propertyType == FrameProperty.ENCODING) {
            byte encoding = getByteValue(value);
            if (encoding != 0x00 && encoding != 0x01 && encoding != 0x02 && encoding != 0x03) return false;
        }
        return super.supports(propertyType, value);
    }

    public boolean supportsFrameUnsynchronisation() {
        return true;
    }

    public boolean supportsTagIsUpdateIndicator() {
        return true;
    }

    public boolean supportsTagRestrictions() {
        return true;
    }

    public boolean supportsTagFooter() {
        return true;
    }

    public org.acid3lib.FrameReader getFrameReader(InputStream in) throws ID3v2ParseException, IOException {
        return new FrameReader(in);
    }

    protected static FramePropertyDefinition propertyGroupSymbol = new FramePropertyDefinition(FrameProperty.GROUP_SYMBOL, FramePropertyDefinition.DT_BYTE, 1);

    protected static FramePropertyDefinition propertyMethodSymbol = new FramePropertyDefinition(FrameProperty.METHOD_SYMBOL, FramePropertyDefinition.DT_BYTE, 1);

    protected static FramePropertyDefinition[] propertiesSigFrame = new FramePropertyDefinition[] { propertyGroupSymbol, new FramePropertyDefinition(FrameProperty.MAIN, FramePropertyDefinition.DT_BINARY) };

    protected static FramePropertyDefinition[] propertiesEncryptionMethodFrame = new FramePropertyDefinition[] { propertyOwnerID, propertyMethodSymbol, new FramePropertyDefinition(FrameProperty.MAIN, FramePropertyDefinition.DT_BINARY) };

    class FrameReader extends org.acid3lib.FrameReader {

        private ID3v2Tag tag;

        private int dataLength;

        private int contentSize;

        private int totalSize;

        public FrameReader(InputStream in) throws ID3v2ParseException, IOException {
            super(in);
            tag = new ID3v2Tag(Spec240.this);
            contentSize = -1;
            dataLength = -1;
            ID3v2Header header = new ID3v2Header();
            int position = read(header, in);
            tag.setUseFooter(header.useFooter);
            tag.setUseUnsynchronisation(header.useUnsynchronisation);
            tag.setUseCRC(header.useCRC);
            tag.setExperimental(header.isExperimental);
            tag.setUpdate(header.isUpdate);
            tag.setRestrictions(header.restrictions);
        }

        public FrameReader(ID3v2Tag tag, InputStream in) throws ID3v2ParseException, IOException {
            super(in);
            this.tag = tag;
            this.totalSize = -1;
            this.contentSize = -1;
            this.dataLength = -1;
        }

        public ID3v2Frame readFrameImpl() throws ID3v2ParseException, IOException {
            String methodName = "Spec240.FrameReader::readFrameImpl()";
            int bytesRead;
            ID3v2Frame frame;
            byte[] byteID = new byte[4];
            byte[] sizeDescriptor = new byte[4];
            byte[] flags = new byte[2];
            byte groupID = 0;
            byte encryptionMethod = 0;
            boolean fileAlterDiscard;
            boolean tagAlterDiscard;
            boolean hasGroupID;
            boolean hasDataLength;
            boolean isReadOnly;
            boolean isCompressed;
            boolean isUnsynchronised;
            boolean isEncrypted;
            bytesRead = in.read(byteID, 0, 4);
            if (bytesRead != -1) Bytes.checkRead(bytesRead, byteID.length, methodName); else return null;
            String id = new String(byteID, 0, 4);
            if (!isLegalFrameID(id)) {
                logger.info(methodName + ": Found illegal frame ID '" + id + "");
                return null;
            }
            in.read(sizeDescriptor, 0, 4);
            in.read(flags, 0, 2);
            contentSize = (int) Bytes.convertLong(sizeDescriptor, 7);
            totalSize = contentSize + 10;
            tagAlterDiscard = Bytes.testBit(flags[0], 6);
            fileAlterDiscard = Bytes.testBit(flags[0], 5);
            isReadOnly = Bytes.testBit(flags[0], 4);
            hasGroupID = Bytes.testBit(flags[1], 6);
            isCompressed = Bytes.testBit(flags[1], 3);
            isEncrypted = Bytes.testBit(flags[1], 2);
            isUnsynchronised = Bytes.testBit(flags[1], 1) || tag.getUseUnsynchronisation();
            hasDataLength = Bytes.testBit(flags[1], 0);
            if (hasGroupID) {
                groupID = (byte) in.read();
                --contentSize;
            }
            if (isEncrypted) {
                encryptionMethod = (byte) in.read();
                --contentSize;
            }
            dataLength = -1;
            if (hasDataLength) {
                byte[] dataLengthDescriptor = new byte[4];
                bytesRead = in.read(dataLengthDescriptor, 0, 4);
                Bytes.checkRead(bytesRead, dataLengthDescriptor.length, methodName);
                dataLength = (int) Bytes.convertLong(dataLengthDescriptor, 7, 0, 4);
                contentSize -= dataLengthDescriptor.length;
            }
            frame = new ID3v2Frame(Spec240.this, id, null);
            frame.setTag(tag);
            frame.setUseUnsynchronisation(isUnsynchronised);
            frame.setUseCompression(isCompressed);
            frame.setUseEncryption(isEncrypted, encryptionMethod);
            frame.setUseGroupID(hasGroupID, groupID);
            frame.setFileAlterDiscard(fileAlterDiscard);
            frame.setTagAlterDiscard(tagAlterDiscard);
            frame.setReadOnly(isReadOnly);
            return frame;
        }

        public void readContentImpl() throws ID3v2ParseException, IOException {
            String methodName = "Spec240.FrameReader::readContentImpl()";
            int bytesRead;
            byte[] content = new byte[] {};
            if (USE_INPUTSTREAMS && dataLength != -1) {
                logger.info(methodName + ": " + "Found data length value " + dataLength + " for frame " + lastFrameRead.getID() + "\n");
                InputStream is = new CountedInputStream(in, contentSize);
                content = new byte[dataLength];
                SynchronisationInputStream synchronisationIS = null;
                if (lastFrameRead.getUseUnsynchronisation()) {
                    synchronisationIS = new SynchronisationInputStream(is);
                    is = synchronisationIS;
                }
                Inflater inflater = null;
                if (lastFrameRead.getUseCompression()) {
                    inflater = new Inflater();
                    is = new InflaterInputStream(is, inflater);
                }
                bytesRead = 0;
                while (bytesRead < content.length) {
                    int i = is.read(content, bytesRead, content.length - bytesRead);
                    if (i == -1) break;
                    bytesRead += i;
                }
                if (bytesRead != content.length) {
                    logger.warn("Spec240.FrameReader::readContentImpl(): " + "The given data length " + dataLength + " does not correspond " + "to the amount of bytes actually read " + bytesRead + "." + "Skipping data rest to assure that following frames are read correctly." + "Compressed size: " + contentSize + "; inflater.getTotalIn(): " + inflater.getTotalIn());
                    long skipDesired = contentSize - inflater.getTotalIn() - synchronisationIS.getDiscardedBytesCount();
                    long skippedBytes = 0;
                    while (skippedBytes < skipDesired) {
                        long l = in.skip(skipDesired - skippedBytes);
                        if (l == -1) break;
                        skippedBytes += l;
                    }
                }
            } else {
                byte[] rawContent = new byte[contentSize];
                bytesRead = in.read(rawContent, 0, rawContent.length);
                Bytes.checkRead(bytesRead, rawContent.length, methodName);
                content = rawContent;
                if (lastFrameRead.getUseUnsynchronisation()) {
                    content = Bytes.synchronise(content);
                }
                if (lastFrameRead.getUseCompression()) {
                    byte[] uncompressed = new byte[dataLength];
                    Inflater inflater = new Inflater();
                    inflater.setInput(content);
                    try {
                        inflater.inflate(uncompressed, 0, uncompressed.length);
                    } catch (DataFormatException e) {
                        throw new ID3v2ParseException(e.getMessage());
                    }
                    content = uncompressed;
                }
                if (lastFrameRead.getUseEncryption()) {
                }
            }
            frameContentParser.parse(lastFrameRead.getProperties(), content);
        }

        public void skipContentImpl() throws IOException {
            in.skip(contentSize);
        }

        public int getFrameSize() {
            return totalSize;
        }
    }

    /**
	 * ID3v2.4 header "struct".
	 */
    static class ID3v2Header {

        public String identifier;

        public int tagSize;

        public boolean isExperimental;

        public boolean useUnsynchronisation;

        public boolean useFooter;

        public boolean isUpdate;

        public boolean useCRC;

        public long crc;

        public ID3v2Restrictions restrictions;

        public ID3v2Header() {
            this("ID3");
        }

        public ID3v2Header(String identifier) {
            this.identifier = identifier;
            this.useUnsynchronisation = false;
            this.isExperimental = false;
            this.useFooter = false;
            this.useCRC = false;
            this.restrictions = null;
            this.crc = 0;
            this.tagSize = 0;
        }

        public Object clone() {
            ID3v2Header clone = new ID3v2Header(identifier);
            clone.useUnsynchronisation = useUnsynchronisation;
            clone.isExperimental = isExperimental;
            clone.useFooter = useFooter;
            clone.useCRC = useCRC;
            clone.crc = crc;
            clone.restrictions = restrictions;
            return clone;
        }
    }
}
