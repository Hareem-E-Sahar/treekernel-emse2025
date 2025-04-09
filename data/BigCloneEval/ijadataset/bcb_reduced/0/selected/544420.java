package ispyb.common.util;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author ricardo.leal@esrf.fr
 * 
 * Jun 3, 2005
 *
 */
public class StringUtils {

    private static final String image_pattern = "<IMG SRC=\"([^\"]*)\"";

    private static final String href_pattern = "<A HREF=.*image.*<IMG SRC=\"((.*)_small[.](.*).*)\"></A>";

    private static final String index_pattern = "<A HREF=.*index[.]html.*A>|<A HREF=.*log.*A>";

    /**
     * Returns User Office code from Proposal code
     * @param proposalCode
     * @return
     */
    public static String getUoCode(String proposalCode) {
        String uoCode = proposalCode.toUpperCase();
        if (proposalCode.toLowerCase().equals(Constants.PROPOSAL_CODE_BM14)) uoCode = "14-U";
        if (proposalCode.toLowerCase().equals(Constants.PROPOSAL_CODE_BM14xxxx)) uoCode = "14-U";
        if (proposalCode.toLowerCase().equals(Constants.PROPOSAL_CODE_BM161)) uoCode = "16-01";
        if (proposalCode.toLowerCase().equals(Constants.PROPOSAL_CODE_BM162)) uoCode = "16-02";
        return uoCode;
    }

    /**
     * Returns Proposal code from User Office code
     * @param uoCode
     * @return
     */
    public static String getProposalCode(String uoCode, int proposalNumber) {
        String proposalCode = uoCode;
        if ((uoCode.endsWith("14-U") || uoCode.endsWith("14-u")) && proposalNumber < 1000) proposalCode = "BM14U";
        if ((uoCode.endsWith("14-U") || uoCode.endsWith("14-u")) && proposalNumber >= 1000) proposalCode = "BM14";
        if (uoCode.endsWith("16-01")) proposalCode = "BM161";
        if (uoCode.endsWith("16-02")) proposalCode = "BM162";
        return proposalCode;
    }

    /**
     * Rewrite all image URL in DNA index page
     * @param orig - File in string format
     * @param pathImg - path of the image source to replace
     * @param pathHref - path of the <a> link to replace
     * @param fullDNAPath - path to folder where DNA images are stored
     * @return
     */
    public static String formatImageURL(String orig, String pathImg, String pathHref, String fullDNAPath) {
        String newOrig = orig.replaceAll("<TD>", "<TD><FONT COLOR=\"#003366\">");
        Pattern patternHref = Pattern.compile(href_pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcherHref = patternHref.matcher(newOrig);
        String href_pattern_subs = "<A HREF=\"" + pathHref + "&file=" + fullDNAPath + "$2" + "." + "$3" + "\"><IMG SRC=\"" + "$1" + "\"></A>";
        String tmpHref = matcherHref.replaceAll(href_pattern_subs);
        Pattern pattern = Pattern.compile(image_pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(tmpHref);
        String image_pattern_subs = "<img src=\"" + pathImg + "&" + Constants.IMG_DNA_PATH_PARAM + "=" + fullDNAPath + "$1\"";
        String tmp1 = matcher.replaceAll(image_pattern_subs);
        return tmp1;
    }

    public static ArrayList<String> getFormatImageURL_DNAPath(String orig, String pathImg, String pathHref, String fullDNAPath) {
        ArrayList<String> lstImages = new ArrayList<String>();
        Pattern pattern = Pattern.compile(image_pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(orig);
        String image_pattern_subs = fullDNAPath + "$1";
        String candidate = null;
        while (matcher.find()) {
            candidate = orig.substring(matcher.start(), matcher.end());
            Matcher matcher2 = pattern.matcher(candidate);
            String image_pattern_subs2 = fullDNAPath + "$1";
            String tmp1 = matcher2.replaceAll(image_pattern_subs);
            lstImages.add(tmp1);
        }
        return lstImages;
    }

    /**
     * Remove the html links : index.html + dpm_log.html
     * @param orig - File in string format
     * @return
     */
    public static String deleteIndexLinks(String orig) {
        Pattern pattern = Pattern.compile(index_pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(orig);
        String image_pattern_subs = "";
        String tmp1 = matcher.replaceAll(image_pattern_subs);
        return tmp1;
    }

    public static String formatImageURL_Denzo(String orig, String path, String fullDNAPath) {
        String newOrig = orig.replaceAll("<TD>", "<TD><FONT COLOR=\"#003366\">");
        String image_pattern_subs2 = "<img src=" + path + "&" + Constants.IMG_DNA_PATH_PARAM + "=" + fullDNAPath + "$1" + ">";
        Pattern pattern2 = Pattern.compile("<IMG SRC=([^>]*)>", Pattern.CASE_INSENSITIVE);
        Matcher matcher2 = pattern2.matcher(newOrig);
        String tmp2 = matcher2.replaceAll(image_pattern_subs2);
        return tmp2;
    }

    public static String[] setArrayElement(String element, int index, String[] array) {
        if (array == null) {
            array = new String[index + 1];
        } else if (index >= array.length) {
            String[] tmpArray = new String[index + 1];
            System.arraycopy(array, 0, tmpArray, 0, array.length);
            array = tmpArray;
        }
        array[index] = element;
        return array;
    }

    public static String getArrayElement(int index, String[] array) {
        if (array == null || index >= array.length) {
            return null;
        }
        return array[index];
    }

    public static Object[] setArrayElement(Object element, int index, Object[] array) {
        if (array == null) {
            array = (Object[]) Array.newInstance(element.getClass(), index + 1);
        } else if (index >= array.length) {
            Object[] tmpArray = (Object[]) Array.newInstance(element.getClass(), index + 1);
            System.arraycopy(array, 0, tmpArray, 0, array.length);
            array = tmpArray;
        }
        array[index] = element;
        return array;
    }

    public static Object getArrayElement(int index, Object[] array) {
        if (array == null || index >= array.length) {
            return null;
        }
        return array[index];
    }

    /**
	 * GetProposalNumberAndCode
	 * @param userPrincipal
	 * @param proposalCode
	 * @param prefix
	 * @param proposalNumber
	 */
    public static ArrayList GetProposalNumberAndCode(String userPrincipal, String proposalCode, String prefix, String proposalNumber) {
        ArrayList authenticationInfo = new ArrayList();
        int start = 0;
        int end = 0;
        proposalCode = userPrincipal.substring(0, 2);
        prefix = userPrincipal.substring(0, 3);
        proposalNumber = userPrincipal.substring(2);
        String strif = Constants.LOGIN_PREFIX_IFX;
        if (proposalCode.equals(strif)) {
            start = 1;
            end = 3;
        }
        String streh = Constants.LOGIN_PREFIX_EHTPX;
        if (proposalCode.equals(streh)) {
            start = 0;
            end = 5;
        }
        String opi = Constants.LOGIN_PREFIX_OPID;
        if (prefix.equals(opi)) {
            start = 0;
            end = 4;
        }
        String opd = Constants.LOGIN_PREFIX_OPD;
        if (prefix.equals(opd)) {
            start = 0;
            end = 3;
        }
        String mxi = Constants.LOGIN_PREFIX_MXIHR;
        if (prefix.equals(mxi)) {
            start = 0;
            end = 5;
        }
        String strbm = Constants.LOGIN_PREFIX_BM;
        if (proposalCode.equals(strbm)) {
            start = 0;
            end = 5;
        }
        if (end != 0) {
            proposalCode = userPrincipal.substring(start, end);
            proposalNumber = userPrincipal.substring(end);
        }
        if (proposalCode.toUpperCase().startsWith("BM14") && !proposalCode.toUpperCase().startsWith("BM14U")) {
            proposalCode = userPrincipal.substring(0, 4);
            proposalNumber = userPrincipal.substring(4);
        }
        authenticationInfo.add(0, proposalCode);
        authenticationInfo.add(1, prefix);
        authenticationInfo.add(2, proposalNumber);
        return authenticationInfo;
    }

    public static ArrayList<String> GetAllowedSpaceGroups() {
        ArrayList<String> spaceGroups = new ArrayList<String>();
        for (int s = 0; s < Constants.SPACE_GROUPS.length; s++) {
            spaceGroups.add(Constants.SPACE_GROUPS[s]);
        }
        return spaceGroups;
    }

    /**
	 * FitPathToOS
	 * @param fullFilePath
	 * @return
	 */
    public static String FitPathToOS(String fullFilePath) {
        String newPath = fullFilePath;
        boolean isWindows = (System.getProperty("os.name").indexOf("Win") != -1) ? true : false;
        if (isWindows && newPath != null) {
            newPath = newPath.replace(Constants.DATA_FILEPATH_START, Constants.DATA_FILEPATH_WINDOWS_MAPPING);
        }
        return newPath;
    }

    /**
	 * getShortFilename
	 * @param fullFilePath
	 * @return
	 */
    public static String getShortFilename(String fullFilePath) {
        String shortFilename = fullFilePath;
        try {
            File f = new File(fullFilePath);
            shortFilename = f.getName();
        } catch (Exception e) {
        }
        return shortFilename;
    }

    public static boolean isEmpty(String s) {
        if (s == null || s.length() < 1) {
            return true;
        }
        return false;
    }

    public static boolean isInteger(String s) {
        try {
            int number = Integer.parseInt(s);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }
}
