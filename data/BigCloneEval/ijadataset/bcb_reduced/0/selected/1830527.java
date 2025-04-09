package lib;

import java.text.NumberFormat;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import subtypes.*;

/**
 * Various static methods.
 *
 * @author A. Ballatore
 */
public class Utils {

    /**
	* Parse a known type subtitle file into a SubtitleFile object.
	* @param fileName file name;
	* @return parsed SubtitleFile.
	*/
    public static SubtitleFile parseSubtitleFile(String fileName) throws Exception {
        SubtitleParser sp = null;
        String input = FileIO.file2string(fileName);
        if (Utils.fileType(fileName) == 0) {
            sp = new SrtParser();
            return sp.parse(input);
        }
        if (Utils.fileType(fileName) == 1) {
            sp = new SubParser();
            return sp.parse(input);
        }
        if (Utils.fileType(fileName) == 2) {
            throw new Exception("Invalid File Extension.");
        }
        return null;
    }

    /**
	* Print a subtitle file into a string in a known format.
	*
	* @param sf a SubtitleFile;
	* @param fileName name of the target File, only use to detect the format;
	* @return SubtitleFile printed into a string.
	*/
    public static String printSubtitleFile(SubtitleFile sf, String fileName) throws Exception {
        SubtitlePrinter sp = null;
        int type = Utils.fileType(fileName);
        if (type == 0) {
            sp = new SrtPrinter();
            return sp.print(sf);
        }
        if (type == 1) {
            sp = new SubPrinter();
            return sp.print(sf);
        }
        if (type == 2) {
            throw new Exception("Invalid File Extension.");
        }
        return null;
    }

    /**
	* @return the file type analyzing only the extension. 0=SRT, 1=SUB, 2=unknown
	*/
    public static int fileType(String file) {
        if (file.length() > 3) {
            String ext = file.substring(file.length() - 4, file.length());
            ext = ext.toLowerCase();
            if (ext.equals(".srt")) return 0;
            if (ext.equals(".sub")) return 1;
        }
        return 2;
    }

    /**
	* @return the file extension, null if file doesn't have extension.
	*/
    public static String fileExt(String file) {
        if (file.length() > 3) {
            String ext = file.substring(file.length() - 4, file.length());
            ext = ext.toLowerCase();
            return ext;
        }
        return null;
    }

    /**
	 * Simple integer formatter.
	 * 
	 * @param n integer to format;
	 * @param chars number of char on which represent n;
	 * @return n on chars characters.
	 */
    public static String format(int n, int chars) {
        NumberFormat numberFormatter;
        String amountOut;
        numberFormatter = NumberFormat.getNumberInstance();
        numberFormatter.setMinimumIntegerDigits(chars);
        numberFormatter.setMaximumIntegerDigits(chars);
        return numberFormatter.format(n);
    }

    /**
	 * Parse shift time.
	 * 
	 * @param shift String that contains a representation of the shift in seconds
	 * 	(e.g. "12","13.5");
	 * @return milliseconds
	 */
    public static int parseShiftTime(String shift) throws NumberFormatException {
        String tmp[] = shift.split("\\.");
        int value = 0;
        if (tmp.length == 1) {
            return Integer.parseInt(tmp[0]) * 1000;
        }
        if (tmp.length == 2) {
            int dec = Integer.parseInt(tmp[1]);
            if ((dec > 999) || (dec < 0)) {
                throw new NumberFormatException("Unpermitted shift value.");
            }
            if ((dec > 0) && (dec <= 9)) {
                dec = dec * 100;
            } else {
                if ((dec >= 10) && (dec <= 99)) {
                    dec = dec * 10;
                }
            }
            if (Integer.parseInt(tmp[0]) < 0) {
                return Integer.parseInt(tmp[0]) * 1000 - dec;
            } else {
                return Integer.parseInt(tmp[0]) * 1000 + dec;
            }
        }
        throw new NumberFormatException("Unpermitted shift value.");
    }

    /**
	 * Remove hearing impaired subtitles.
	 * 
	 * @param text with hearing impaired subtitles'
	 * @param start char (e.g. '[');
	 * @param end char (e.g. ']');
	 * @return text without Hearing Impaired Subtitles
	 */
    public static String removeHearImp(String text, String start, String end) {
        String res = text;
        Pattern p = Pattern.compile("\\" + start + ".*?" + "\\" + end);
        Matcher m = p.matcher(res);
        while (m.find()) {
            res = res.substring(0, m.start()) + res.substring(m.end(), res.length());
            m = p.matcher(res);
        }
        return res;
    }

    /**
	 * Frame/MilliSec Converter.
	 *
	 * @param frames n of frames
	 * @param framerate framerate (frames per sec)
	 * @return milliseconds
	 */
    public static int frame2mil(int frames, float framerate) {
        if (framerate <= 0) return 0;
        float fr = frames;
        Float fl = new Float(frames / framerate * 1000);
        return fl.intValue();
    }

    /**
	 * MilliSec/Frame Converter.
	 *
	 * @param millisec n of millisec
	 * @param framerate framerate (frames per sec)
	 * @return frames
	 */
    public static int mil2frame(int millisec, float framerate) {
        if (framerate <= 0) return 0;
        float fr = millisec;
        millisec = millisec / 1000;
        Float fl = new Float(millisec * framerate);
        return fl.intValue();
    }

    public static SubtitleFile fillValues(SubtitleFile sf, float framerate) {
        SubtitleLine sl;
        SubtitleTime st;
        for (SubtitleLine x : sf) {
            x.getBegin().setAllValues(framerate);
            x.getEnd().setAllValues(framerate);
        }
        return sf;
    }
}
