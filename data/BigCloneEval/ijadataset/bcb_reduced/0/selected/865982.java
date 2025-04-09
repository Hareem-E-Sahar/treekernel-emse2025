package org.metaphile.directory.exif;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.metaphile.TypeUtils;
import org.metaphile.directory.IParseableDirectory;
import org.metaphile.tag.TypedTag;
import org.metaphile.tag.exif.ricoh.RicohTag;

/**
 * 
 * @author stuart
 * @since 0.1.1
 */
public class RicohMakerNotesDirectory implements IParseableDirectory {

    private Boolean isBigEndian = false;

    private static Logger log = Logger.getLogger("ISegment");

    private Map<TypedTag, byte[]> values = new HashMap<TypedTag, byte[]>();

    public void setIsBigEndian(Boolean isBigEndian) {
        this.isBigEndian = isBigEndian;
    }

    public TypedTag getTagByIdentifier(String identifier) {
        return RicohTag.getTagByIdentifier(identifier);
    }

    public Map<TypedTag, byte[]> getValues() {
        return values;
    }

    public void parseDirectory(byte[] segmentData, int offset, int length) {
        if ("Rv".equals(new String(segmentData, offset, 2))) {
            String asciiText = new String(segmentData, 0, segmentData.length);
            Pattern pattern = Pattern.compile("(\\w{2})([\\w]+)[;|:]");
            Matcher matcher = pattern.matcher(asciiText);
            while (matcher.find()) {
                RicohTag tag = RicohTag.getTagByIdentifier(matcher.group(1));
                if (tag != null) {
                    values.put(tag, Arrays.copyOfRange(segmentData, matcher.start(2), matcher.end(2)));
                } else {
                    log.log(Level.WARNING, "Found unknown tag: " + matcher.group(1));
                }
            }
        }
    }

    public Integer getDirectoryIdentifier() {
        return 0x927C;
    }

    public String getDirectoryName() {
        return "Ricoh Maker Notes";
    }

    public String getRevision() {
        return TypeUtils.bytesToString(values.get(RicohTag.REVISION));
    }

    public String getUnknown_Sf() {
        return TypeUtils.bytesToString(values.get(RicohTag.UNKNOWN_SF));
    }

    public String getRedGain() {
        return TypeUtils.bytesToString(values.get(RicohTag.RED_GAIN));
    }

    public String getGreenGain() {
        return TypeUtils.bytesToString(values.get(RicohTag.GREEN_GAIN));
    }

    public String getBlueGain() {
        return TypeUtils.bytesToString(values.get(RicohTag.BLUE_GAIN));
    }

    public String getUnknown_Ll() {
        return TypeUtils.bytesToString(values.get(RicohTag.UNKNOWN_LL));
    }

    public String getUnknown_Ld() {
        return TypeUtils.bytesToString(values.get(RicohTag.UNKNOWN_LD));
    }

    public String getUnknown_Aj() {
        return TypeUtils.bytesToString(values.get(RicohTag.UNKNOWN_AJ));
    }

    public String getUnknown_Bn() {
        return TypeUtils.bytesToString(values.get(RicohTag.UNKNOWN_BN));
    }

    public String getUnknown_Fp() {
        return TypeUtils.bytesToString(values.get(RicohTag.UNKNOWN_FP));
    }

    public String getUnknown_Md() {
        return TypeUtils.bytesToString(values.get(RicohTag.UNKNOWN_MD));
    }

    public String getUnknown_Ln() {
        return TypeUtils.bytesToString(values.get(RicohTag.UNKNOWN_LN));
    }

    public String getUnknown_Sv() {
        return TypeUtils.bytesToString(values.get(RicohTag.UNKNOWN_SV));
    }
}
