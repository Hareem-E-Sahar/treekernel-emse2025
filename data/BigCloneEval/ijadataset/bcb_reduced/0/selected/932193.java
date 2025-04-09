package org.metaphile.segment;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.metaphile.TypeUtils;
import org.metaphile.tag.app12.App12Tag;
import org.metaphile.util.HexDump;

/**
 * The JPEG APP12 "Picture Info" segment was used by some older cameras, and
 * contains ASCII-based meta information.
 * 
 * @author stuart
 * @since 0.1.1
 */
public class App12Segment implements ISegment {

    private static Logger log = Logger.getLogger("ISegment");

    private Boolean isBigEndian = true;

    private Map<App12Tag, byte[]> values = new EnumMap<App12Tag, byte[]>(App12Tag.class);

    public int getMarker() {
        return 0xEC;
    }

    public String getName() {
        return "PictureInfo (App12)";
    }

    /**
     * Parses the segment data and extracts the sets the values for all matching 
     * tags.
     * 
     * @param segmentData the segment bytes to be parsed
     */
    public void parse(byte[] segmentData) {
        String asciiText = new String(segmentData, 0, segmentData.length);
        Pattern pattern = Pattern.compile("\r\n([\\w#]+)=([\\w-#,\\. ]+)");
        Matcher matcher = pattern.matcher(asciiText);
        while (matcher.find()) {
            App12Tag tag = App12Tag.getTagByIdentifier(matcher.group(1));
            if (tag != null) {
                values.put(tag, Arrays.copyOfRange(segmentData, matcher.start(2), matcher.end(2)));
            } else {
                log.log(Level.WARNING, "Found unknown tag: " + matcher.group(1));
            }
        }
    }

    public String getTimeDate() {
        return TypeUtils.bytesToString(values.get(App12Tag.TIMEDATE));
    }

    public String getFlash() {
        return TypeUtils.bytesToString(values.get(App12Tag.FLASH));
    }

    public String getShutter() {
        return TypeUtils.bytesToString(values.get(App12Tag.SHUTTER));
    }

    public String getResolution() {
        return TypeUtils.bytesToString(values.get(App12Tag.RESOLUTION));
    }

    public String getProtect() {
        return TypeUtils.bytesToString(values.get(App12Tag.PROTECT));
    }

    public String getContTake() {
        return TypeUtils.bytesToString(values.get(App12Tag.CONT_TAKE));
    }

    public String getImageSize() {
        return TypeUtils.bytesToString(values.get(App12Tag.IMAGE_SIZE));
    }

    public String getColorMode() {
        return TypeUtils.bytesToString(values.get(App12Tag.COLOR_MODE));
    }

    public String getFNumber() {
        return TypeUtils.bytesToString(values.get(App12Tag.F_NUMBER));
    }

    public String getZoom() {
        return TypeUtils.bytesToString(values.get(App12Tag.ZOOM));
    }

    public String getMacro() {
        return TypeUtils.bytesToString(values.get(App12Tag.MACRO));
    }

    public String getType() {
        return TypeUtils.bytesToString(values.get(App12Tag.TYPE));
    }

    public String getVersion() {
        return TypeUtils.bytesToString(values.get(App12Tag.VERSION));
    }

    public String getId() {
        return TypeUtils.bytesToString(values.get(App12Tag.ID));
    }

    public String getPicLen() {
        return TypeUtils.bytesToString(values.get(App12Tag.PIC_LEN));
    }

    public String getThmLen() {
        return TypeUtils.bytesToString(values.get(App12Tag.THM_LEN));
    }

    public String getSerialNumber() {
        return TypeUtils.bytesToString(values.get(App12Tag.SERIAL_NUMBER));
    }

    public String getQuality() {
        return TypeUtils.bytesToString(values.get(App12Tag.QUALITY));
    }

    public String getUnknownB() {
        return TypeUtils.bytesToString(values.get(App12Tag.UNKNOWN_B));
    }

    public String getUnknownR() {
        return TypeUtils.bytesToString(values.get(App12Tag.UNKNOWN_R));
    }

    public String getUnknownS0() {
        return TypeUtils.bytesToString(values.get(App12Tag.UNKNOWN_S0));
    }

    public String getUnknownT0() {
        return TypeUtils.bytesToString(values.get(App12Tag.UNKNOWN_T0));
    }
}
