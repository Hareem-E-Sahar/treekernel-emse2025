package fi.tkk.ics.hadoop.bam.custom.samtools;

import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMValidationError;
import net.sf.samtools.SAMFileReader.ValidationStringency;
import net.sf.samtools.util.DateParser;
import net.sf.samtools.util.LineReader;
import net.sf.samtools.util.RuntimeIOException;
import net.sf.samtools.util.StringUtil;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Parser for a SAM text header, and a generator of SAM text header.
 */
public class SAMTextHeaderCodec {

    private static final String HEADER_LINE_START = "@";

    private SAMFileHeader mFileHeader;

    private final TextTagCodec mTagCodec = new TextTagCodec();

    private String mCurrentLine;

    private LineReader mReader;

    private String mSource;

    private List<SAMSequenceRecord> sequences;

    private List<SAMReadGroupRecord> readGroups;

    private final StringBuilder textHeader = new StringBuilder();

    private ValidationStringency validationStringency = ValidationStringency.SILENT;

    private BufferedWriter writer;

    private static final String TAG_KEY_VALUE_SEPARATOR = ":";

    private static final String FIELD_SEPARATOR = "\t";

    public static final String COMMENT_PREFIX = HEADER_LINE_START + HeaderRecordType.CO.name() + FIELD_SEPARATOR;

    /**
     * Reads text SAM header and converts to a SAMFileHeader object.
     * @param reader Where to get header text from.
     * @param source Name of the input file, for error messages.  May be null.
     * @return complete header object.
     */
    public SAMFileHeader decode(final LineReader reader, final String source) {
        mFileHeader = new SAMFileHeader();
        mReader = reader;
        mSource = source;
        sequences = new ArrayList<SAMSequenceRecord>();
        readGroups = new ArrayList<SAMReadGroupRecord>();
        while (advanceLine() != null) {
            final ParsedHeaderLine parsedHeaderLine = new ParsedHeaderLine(mCurrentLine);
            if (!parsedHeaderLine.isLineValid()) {
                continue;
            }
            switch(parsedHeaderLine.getHeaderRecordType()) {
                case HD:
                    parseHDLine(parsedHeaderLine);
                    break;
                case PG:
                    parsePGLine(parsedHeaderLine);
                    break;
                case RG:
                    parseRGLine(parsedHeaderLine);
                    break;
                case SQ:
                    parseSQLine(parsedHeaderLine);
                    break;
                case CO:
                    mFileHeader.addComment(mCurrentLine);
                    break;
                default:
                    throw new IllegalStateException("Unrecognized header record type: " + parsedHeaderLine.getHeaderRecordType());
            }
        }
        mFileHeader.setSequenceDictionary(new SAMSequenceDictionary(sequences));
        mFileHeader.setReadGroups(readGroups);
        if (!mFileHeader.getValidationErrors().isEmpty() || textHeader.length() < (1024 * 1024)) {
            mFileHeader.setTextHeader(textHeader.toString());
        }
        SAMUtils.processValidationErrors(mFileHeader.getValidationErrors(), -1, validationStringency);
        return mFileHeader;
    }

    private String advanceLine() {
        final int nextChar = mReader.peek();
        if (nextChar != '@') {
            return null;
        }
        mCurrentLine = mReader.readLine();
        textHeader.append(mCurrentLine).append("\n");
        return mCurrentLine;
    }

    /**
     * Transfer standard and non-standard tags from text representation to in-memory representation.
     * All values are now stored as Strings.
     * @param record attributes get set into this object.
     * @param textAttributes Map of tag type to value.  Some values may be removed by this method.
     */
    private void transferAttributes(final AbstractSAMHeaderRecord record, final Map<String, String> textAttributes) {
        for (final Map.Entry<String, String> entry : textAttributes.entrySet()) {
            record.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    private void parsePGLine(final ParsedHeaderLine parsedHeaderLine) {
        assert (HeaderRecordType.PG.equals(parsedHeaderLine.getHeaderRecordType()));
        if (!parsedHeaderLine.requireTag(SAMProgramRecord.PROGRAM_GROUP_ID_TAG)) {
            return;
        }
        final SAMProgramRecord programRecord = new SAMProgramRecord(parsedHeaderLine.removeValue(SAMProgramRecord.PROGRAM_GROUP_ID_TAG));
        transferAttributes(programRecord, parsedHeaderLine.mKeyValuePairs);
        mFileHeader.addProgramRecord(programRecord);
    }

    private void parseRGLine(final ParsedHeaderLine parsedHeaderLine) {
        assert (HeaderRecordType.RG.equals(parsedHeaderLine.getHeaderRecordType()));
        if (!parsedHeaderLine.requireTag(SAMReadGroupRecord.READ_GROUP_ID_TAG) || !parsedHeaderLine.requireTag(SAMReadGroupRecord.READ_GROUP_SAMPLE_TAG)) {
            return;
        }
        final SAMReadGroupRecord samReadGroupRecord = new SAMReadGroupRecord(parsedHeaderLine.removeValue(SAMReadGroupRecord.READ_GROUP_ID_TAG));
        transferAttributes(samReadGroupRecord, parsedHeaderLine.mKeyValuePairs);
        final String predictedMedianInsertSize = samReadGroupRecord.getAttribute(SAMReadGroupRecord.PREDICTED_MEDIAN_INSERT_SIZE_TAG);
        if (predictedMedianInsertSize != null) {
            try {
                Integer.parseInt(predictedMedianInsertSize);
                samReadGroupRecord.setAttribute(SAMReadGroupRecord.PREDICTED_MEDIAN_INSERT_SIZE_TAG, predictedMedianInsertSize);
            } catch (NumberFormatException e) {
                reportErrorParsingLine(SAMReadGroupRecord.PREDICTED_MEDIAN_INSERT_SIZE_TAG + " is not numeric: " + predictedMedianInsertSize, SAMValidationError.Type.INVALID_PREDICTED_MEDIAN_INSERT_SIZE, e);
            }
        }
        final String dateRunProduced = samReadGroupRecord.getAttribute(SAMReadGroupRecord.DATE_RUN_PRODUCED_TAG);
        if (dateRunProduced != null) {
            Object date;
            try {
                date = mTagCodec.decodeDate(dateRunProduced);
            } catch (DateParser.InvalidDateException e) {
                date = dateRunProduced;
                reportErrorParsingLine(SAMReadGroupRecord.DATE_RUN_PRODUCED_TAG + " tag value '" + dateRunProduced + "' is not parseable as a date", SAMValidationError.Type.INVALID_DATE_STRING, e);
            }
            samReadGroupRecord.setAttribute(SAMReadGroupRecord.DATE_RUN_PRODUCED_TAG, date.toString());
        }
        readGroups.add(samReadGroupRecord);
    }

    private void parseSQLine(final ParsedHeaderLine parsedHeaderLine) {
        assert (HeaderRecordType.SQ.equals(parsedHeaderLine.getHeaderRecordType()));
        if (!parsedHeaderLine.requireTag(SAMSequenceRecord.SEQUENCE_NAME_TAG) || !parsedHeaderLine.requireTag(SAMSequenceRecord.SEQUENCE_LENGTH_TAG)) {
            return;
        }
        String sequenceName = parsedHeaderLine.removeValue(SAMSequenceRecord.SEQUENCE_NAME_TAG);
        sequenceName = SAMSequenceRecord.truncateSequenceName(sequenceName);
        final SAMSequenceRecord samSequenceRecord = new SAMSequenceRecord(sequenceName, Integer.parseInt(parsedHeaderLine.removeValue(SAMSequenceRecord.SEQUENCE_LENGTH_TAG)));
        transferAttributes(samSequenceRecord, parsedHeaderLine.mKeyValuePairs);
        sequences.add(samSequenceRecord);
    }

    private void parseHDLine(final ParsedHeaderLine parsedHeaderLine) {
        assert (HeaderRecordType.HD.equals(parsedHeaderLine.getHeaderRecordType()));
        if (!parsedHeaderLine.requireTag(SAMFileHeader.VERSION_TAG)) {
            return;
        }
        transferAttributes(mFileHeader, parsedHeaderLine.mKeyValuePairs);
    }

    private void reportErrorParsingLine(String reason, final SAMValidationError.Type type, final Throwable nestedException) {
        reason = "Error parsing SAM header. " + reason + ". Line:\n" + mCurrentLine;
        if (validationStringency != ValidationStringency.STRICT) {
            final SAMValidationError error = new SAMValidationError(type, reason, null, mReader.getLineNumber());
            error.setSource(mSource);
            mFileHeader.addValidationError(error);
        } else {
            String fileMessage = "";
            if (mSource != null) {
                fileMessage = "File " + mSource;
            }
            throw new SAMFormatException(reason + "; " + fileMessage + "; Line number " + mReader.getLineNumber(), nestedException);
        }
    }

    private enum HeaderRecordType {

        HD, SQ, RG, PG, CO
    }

    /**
     * Takes a header line as a String and converts it into a HeaderRecordType, and a map of key:value strings.
     * If the line does not contain a recognized HeaderRecordType, then the line is considered invalid, and will
     * not have any key:value pairs.
     */
    private class ParsedHeaderLine {

        private HeaderRecordType mHeaderRecordType;

        private final Map<String, String> mKeyValuePairs = new HashMap<String, String>();

        private boolean lineValid = false;

        ParsedHeaderLine(final String line) {
            assert (line.startsWith(HEADER_LINE_START));
            final String[] fields = line.split(FIELD_SEPARATOR);
            try {
                mHeaderRecordType = HeaderRecordType.valueOf(fields[0].substring(1));
            } catch (IllegalArgumentException e) {
                reportErrorParsingLine("Unrecognized header record type", SAMValidationError.Type.UNRECOGNIZED_HEADER_TYPE, null);
                mHeaderRecordType = null;
                return;
            }
            if (mHeaderRecordType == HeaderRecordType.CO) {
                lineValid = true;
                return;
            }
            for (int i = 1; i < fields.length; ++i) {
                final String[] keyAndValue = fields[i].split(TAG_KEY_VALUE_SEPARATOR, 2);
                if (keyAndValue.length != 2) {
                    reportErrorParsingLine("Problem parsing " + HEADER_LINE_START + mHeaderRecordType + " key:value pair", SAMValidationError.Type.POORLY_FORMATTED_HEADER_TAG, null);
                    continue;
                }
                if (mKeyValuePairs.containsKey(keyAndValue[0]) && !mKeyValuePairs.get(keyAndValue[0]).equals(keyAndValue[1])) {
                    reportErrorParsingLine("Problem parsing " + HEADER_LINE_START + mHeaderRecordType + " key:value pair " + keyAndValue[0] + ":" + keyAndValue[1] + " clashes with " + keyAndValue[0] + ":" + mKeyValuePairs.get(keyAndValue[0]), SAMValidationError.Type.HEADER_TAG_MULTIPLY_DEFINED, null);
                    continue;
                }
                mKeyValuePairs.put(keyAndValue[0], keyAndValue[1]);
            }
            lineValid = true;
        }

        /**
         * True if the line is recognized as one of the valid HeaderRecordTypes.
         */
        public boolean isLineValid() {
            return lineValid;
        }

        /**
         * Handling depends on the validation stringency.  If the tag is not present, and stringency is strict,
         * an exception is thrown.  If stringency is not strict, false is returned.
         * @param tag Must be present for the line to be considered value.
         * @return True if tag is present.
         */
        boolean requireTag(final String tag) {
            if (!mKeyValuePairs.containsKey(tag)) {
                reportErrorParsingLine(HEADER_LINE_START + mHeaderRecordType + " line missing " + tag + " tag", SAMValidationError.Type.HEADER_RECORD_MISSING_REQUIRED_TAG, null);
                return false;
            }
            return true;
        }

        /**
         * @return null if line is invalid, otherwise the parsed HeaderRecordType
         */
        public HeaderRecordType getHeaderRecordType() {
            return mHeaderRecordType;
        }

        boolean containsKey(final String key) {
            return mKeyValuePairs.containsKey(key);
        }

        String getValue(final String key) {
            return mKeyValuePairs.get(key);
        }

        String removeValue(final String key) {
            final String ret = mKeyValuePairs.get(key);
            mKeyValuePairs.remove(key);
            return ret;
        }
    }

    /**
     * Convert SAMFileHeader from in-memory representation to text representation.
     * @param writer where to write the header text.
     * @param header object to be converted to text.
     */
    public void encode(final Writer writer, final SAMFileHeader header) {
        mFileHeader = header;
        this.writer = new BufferedWriter(writer);
        writeHDLine();
        for (final SAMSequenceRecord sequenceRecord : header.getSequenceDictionary().getSequences()) {
            writeSQLine(sequenceRecord);
        }
        for (final SAMReadGroupRecord readGroup : header.getReadGroups()) {
            writeRGLine(readGroup);
        }
        for (final SAMProgramRecord programRecord : header.getProgramRecords()) {
            writePGLine(programRecord);
        }
        for (final String comment : header.getComments()) {
            println(comment);
        }
        try {
            this.writer.flush();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    private void println(final String s) {
        try {
            writer.append(s);
            writer.append("\n");
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    private void writePGLine(final SAMProgramRecord programRecord) {
        if (programRecord == null) {
            return;
        }
        final String[] fields = new String[2 + programRecord.getAttributes().size()];
        fields[0] = HEADER_LINE_START + HeaderRecordType.PG;
        fields[1] = SAMProgramRecord.PROGRAM_GROUP_ID_TAG + TAG_KEY_VALUE_SEPARATOR + programRecord.getProgramGroupId();
        encodeTags(programRecord, fields, 2);
        println(StringUtil.join(FIELD_SEPARATOR, fields));
    }

    private void writeRGLine(final SAMReadGroupRecord readGroup) {
        final String[] fields = new String[2 + readGroup.getAttributes().size()];
        fields[0] = HEADER_LINE_START + HeaderRecordType.RG;
        fields[1] = SAMReadGroupRecord.READ_GROUP_ID_TAG + TAG_KEY_VALUE_SEPARATOR + readGroup.getReadGroupId();
        encodeTags(readGroup, fields, 2);
        println(StringUtil.join(FIELD_SEPARATOR, fields));
    }

    private void writeHDLine() {
        final SAMFileHeader newHeader = new SAMFileHeader();
        for (final Map.Entry<String, String> entry : mFileHeader.getAttributes()) {
            if (!entry.getKey().equals(SAMFileHeader.VERSION_TAG)) {
                newHeader.setAttribute(entry.getKey(), entry.getValue());
            }
        }
        final String[] fields = new String[1 + newHeader.getAttributes().size()];
        fields[0] = HEADER_LINE_START + HeaderRecordType.HD;
        encodeTags(newHeader, fields, 1);
        println(StringUtil.join(FIELD_SEPARATOR, fields));
    }

    private void writeSQLine(final SAMSequenceRecord sequenceRecord) {
        final int numAttributes = sequenceRecord.getAttributes() != null ? sequenceRecord.getAttributes().size() : 0;
        final String[] fields = new String[3 + numAttributes];
        fields[0] = HEADER_LINE_START + HeaderRecordType.SQ;
        fields[1] = SAMSequenceRecord.SEQUENCE_NAME_TAG + TAG_KEY_VALUE_SEPARATOR + sequenceRecord.getSequenceName();
        fields[2] = SAMSequenceRecord.SEQUENCE_LENGTH_TAG + TAG_KEY_VALUE_SEPARATOR + Integer.toString(sequenceRecord.getSequenceLength());
        encodeTags(sequenceRecord, fields, 3);
        println(StringUtil.join(FIELD_SEPARATOR, fields));
    }

    /**
     * Encode all the attributes in the given object as text
     * @param rec object containing attributes, and knowledge of which are standard tags
     * @param fields where to put the text representation of the tags.  Must be big enough to hold all tags.
     * @param offset where to start putting text tag representations.
     */
    private void encodeTags(final AbstractSAMHeaderRecord rec, final String[] fields, int offset) {
        for (final Map.Entry<String, String> entry : rec.getAttributes()) {
            fields[offset++] = mTagCodec.encodeUntypedTag(entry.getKey(), entry.getValue());
        }
    }

    public void setValidationStringency(final ValidationStringency validationStringency) {
        if (validationStringency == null) {
            throw new IllegalArgumentException("null validationStringency not allowed");
        }
        this.validationStringency = validationStringency;
    }
}
