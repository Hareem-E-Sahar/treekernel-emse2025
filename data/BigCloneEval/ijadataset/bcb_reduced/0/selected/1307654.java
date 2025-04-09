package org.powerfolder.utils.misc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.powerfolder.PFRuntimeException;
import org.powerfolder.ValueAndClass;
import org.powerfolder.ValueAndClassFactory;
import gnu.regexp.RE;
import gnu.regexp.REException;

public final class MiscHelper {

    public static final String REG_EX_POSITIVE_INTEGER = "[0-9]*[1-9][0-9]*";

    public static final String REG_EX_NON_NEGATIVE_INTEGER = "[0-9]+";

    public static final String REG_EX_INTEGER = "[0-9]+";

    public static final String REG_EX_JAVA_NAME = "[a-zA-Z][a-zA-Z0-9]*";

    public static final String REG_EX_DECIMAL = "[-+]?\\d+(\\.\\d+)?";

    public static final String REG_EX_SINGLE_CHARACTER = ".";

    public static final String REG_EX_ANY_STRING = ".*";

    private MiscHelper() {
    }

    private static final String GT_XML = "&gt;";

    private static final String LT_XML = "&lt;";

    private static final String AMP_XML = "&amp;";

    private static final String APOST_XML = "&apos;";

    private static final String QUOTE_XML = "&quot;";

    public static final String fixString(String inSource, int inLength) {
        String outValue = null;
        StringBuffer sb = new StringBuffer(shortenString(inSource, inLength));
        while (sb.length() < inLength) {
            sb.append(" ");
        }
        outValue = sb.toString();
        return outValue;
    }

    public static final String shortenString(String inSource) {
        return shortenString(inSource, 20);
    }

    public static final String shortenString(String inSource, int inLength) {
        String outValue = null;
        if (inSource.length() <= inLength) {
            outValue = inSource;
        } else {
            int fadLength = Math.min(3, inLength);
            outValue = inSource.substring(0, inLength - fadLength);
            for (int i = 0; i < fadLength; i++) {
                outValue = outValue + ".";
            }
        }
        return outValue;
    }

    public static final boolean isRegularExpressionMatch(String inInput, String inPattern, boolean inMultiline) {
        boolean outValue = false;
        try {
            RE re = null;
            if (inMultiline) {
                re = new RE(inPattern, RE.REG_DOT_NEWLINE);
            } else {
                re = new RE(inPattern);
            }
            outValue = re.isMatch(inInput);
        } catch (REException ree) {
            outValue = false;
        }
        return outValue;
    }

    public static final Class getClassByName(String inName) {
        return getClassByName(inName, true);
    }

    public static final Class getClassByName(String name, boolean inThrow) {
        Class retClass = null;
        if (name.equals(Boolean.TYPE.getName())) {
            retClass = Boolean.TYPE;
        } else if (name.equals(Byte.TYPE.getName())) {
            retClass = Byte.TYPE;
        } else if (name.equals(Short.TYPE.getName())) {
            retClass = Short.TYPE;
        } else if (name.equals(Character.TYPE.getName())) {
            retClass = Character.TYPE;
        } else if (name.equals(Integer.TYPE.getName())) {
            retClass = Integer.TYPE;
        } else if (name.equals(Long.TYPE.getName())) {
            retClass = Long.TYPE;
        } else if (name.equals(Float.TYPE.getName())) {
            retClass = Float.TYPE;
        } else if (name.equals(Double.TYPE.getName())) {
            retClass = Double.TYPE;
        } else if (name.equals(Void.TYPE.getName())) {
            retClass = Void.TYPE;
        } else {
            try {
                retClass = Class.forName(name);
            } catch (ClassNotFoundException cnfe) {
                if (inThrow) {
                    throw new PFRuntimeException(cnfe);
                } else {
                    retClass = null;
                }
            }
        }
        return retClass;
    }

    public static final String convertStringToJSCharacterData(String inInput) {
        StringBuffer outValue = new StringBuffer();
        for (int i = 0; i < inInput.length(); i++) {
            char nextChar = inInput.charAt(i);
            outValue.append("\\u");
            int baseLength = outValue.length();
            for (int j = 0; j < 4; j++) {
                int nextDigit = nextChar % 16;
                nextChar = (char) (nextChar / 16);
                outValue.insert(baseLength, Character.forDigit(nextDigit, 16));
            }
        }
        return outValue.toString();
    }

    public static final String convertStringToXMLCharacterData(String input) {
        StringTokenizer st = new StringTokenizer(input, "&<>'\"", true);
        StringBuffer sb = new StringBuffer();
        while (st.hasMoreTokens()) {
            String nextToken = st.nextToken();
            char testChar = nextToken.charAt(0);
            if (testChar == '&' || testChar == '<' || testChar == '>' || testChar == '\'' || testChar == '"') {
                for (int i = 0; i < nextToken.length(); i++) {
                    char nextChar = nextToken.charAt(i);
                    if (nextChar == '&') {
                        sb.append(AMP_XML);
                    } else if (nextChar == '<') {
                        sb.append(LT_XML);
                    } else if (nextChar == '>') {
                        sb.append(GT_XML);
                    } else if (nextChar == '\'') {
                        sb.append(APOST_XML);
                    } else if (nextChar == '"') {
                        sb.append(QUOTE_XML);
                    } else {
                    }
                }
            } else {
                sb.append(nextToken);
            }
        }
        return sb.toString();
    }

    public static final ValueAndClass castTo(Class castTo, ValueAndClass vac) {
        String destinationName = castTo.getName();
        String sourceName = vac.getValueClass().getName();
        if (destinationName.equals(Boolean.TYPE.getName())) {
            if (sourceName.equals(Boolean.TYPE.getName())) {
                return ValueAndClassFactory.newValueAndClass(vac.getValue(), Boolean.TYPE);
            }
        } else if (destinationName.equals(Void.TYPE.getName())) {
            if (sourceName.equals(Void.TYPE.getName())) {
                return ValueAndClassFactory.newValueAndClass(vac.getValue(), Void.TYPE);
            }
        } else if (isNamePrimitive(destinationName) && isNamePrimitive(sourceName)) {
            Double assignValue = null;
            if (vac.getValue() instanceof Number) {
                assignValue = new Double(((Number) vac.getValue()).doubleValue());
            } else if (vac.getValue() instanceof Character) {
                assignValue = new Double(((Character) vac.getValue()).charValue());
            } else {
                throw new PFRuntimeException("Value " + sourceName + " cannot be converted.");
            }
            if (destinationName.equals(Byte.TYPE.getName())) {
                return ValueAndClassFactory.newValueAndClass(new Byte(assignValue.byteValue()), Byte.TYPE);
            } else if (destinationName.equals(Short.TYPE.getName())) {
                return ValueAndClassFactory.newValueAndClass(new Short(assignValue.shortValue()), Short.TYPE);
            } else if (destinationName.equals(Character.TYPE.getName())) {
                return ValueAndClassFactory.newValueAndClass(new Character((char) assignValue.shortValue()), Character.TYPE);
            } else if (destinationName.equals(Integer.TYPE.getName())) {
                return ValueAndClassFactory.newValueAndClass(new Integer(assignValue.intValue()), Integer.TYPE);
            } else if (destinationName.equals(Long.TYPE.getName())) {
                return ValueAndClassFactory.newValueAndClass(new Long(assignValue.longValue()), Long.TYPE);
            } else if (destinationName.equals(Float.TYPE.getName())) {
                return ValueAndClassFactory.newValueAndClass(new Float(assignValue.floatValue()), Float.TYPE);
            } else if (destinationName.equals(Double.TYPE.getName())) {
                return ValueAndClassFactory.newValueAndClass(new Double(assignValue.doubleValue()), Double.TYPE);
            } else if (destinationName.equals(Void.TYPE.getName())) {
                return ValueAndClassFactory.newValueAndClass(new Double(assignValue.doubleValue()), Void.TYPE);
            } else {
                throw new PFRuntimeException("Value " + destinationName + " is an unknown primitive type.");
            }
        } else {
            if (castTo.isAssignableFrom(vac.getValueClass())) {
                return ValueAndClassFactory.newValueAndClass(vac.getValue(), castTo);
            }
        }
        throw new PFRuntimeException("Could not cast " + sourceName + " to " + destinationName + ".");
    }

    protected static final boolean isNamePrimitive(String name) {
        if (name == null) {
            return false;
        } else if (name.equals(Boolean.TYPE.getName()) || name.equals(Byte.TYPE.getName()) || name.equals(Short.TYPE.getName()) || name.equals(Character.TYPE.getName()) || name.equals(Integer.TYPE.getName()) || name.equals(Long.TYPE.getName()) || name.equals(Float.TYPE.getName()) || name.equals(Double.TYPE.getName())) {
            return true;
        } else {
            return false;
        }
    }

    public static final String convertSerializedDataToXMLCharacterData(byte ba[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ba.length; i++) {
            int baseDec = ba[i] & 0x00ff;
            String baseHex = Integer.toHexString(baseDec);
            if (baseHex.length() == 1) {
                sb.append("0");
                sb.append(baseHex);
            } else {
                sb.append(baseHex);
            }
        }
        return sb.toString();
    }

    public static final byte[] convertXMLCharacterDataToSerializedData(String cd) {
        if ((cd.length() % 2) != 0) {
            throw new PFRuntimeException(cd + " does not have an even length.");
        }
        byte ba[] = new byte[cd.length() / 2];
        for (int i = 0; i < ba.length; i++) {
            int offset = i * 2;
            String hexNum = cd.substring(offset, offset + 2);
            byte nextByte = (byte) (Integer.parseInt(hexNum, 16) & 0x00ff);
            ba[i] = nextByte;
        }
        return ba;
    }

    public static final boolean isClassNumber(Class c) {
        String cName = c.getName();
        if (cName.equals(Byte.class.getName()) || cName.equals(Character.class.getName()) || cName.equals(Short.class.getName()) || cName.equals(Integer.class.getName()) || cName.equals(Long.class.getName()) || cName.equals(Float.class.getName()) || cName.equals(Double.class.getName()) || cName.equals(Byte.TYPE.getName()) || cName.equals(Character.TYPE.getName()) || cName.equals(Short.TYPE.getName()) || cName.equals(Integer.TYPE.getName()) || cName.equals(Long.TYPE.getName()) || cName.equals(Float.TYPE.getName()) || cName.equals(Double.TYPE.getName()) || cName.equals(BigInteger.class.getName()) || cName.equals(BigDecimal.class.getName())) {
            return true;
        } else {
            return false;
        }
    }

    public static final ValueAndClass convertBigDecimalToNumber(BigDecimal b, Class inClass) {
        String cName = inClass.getName();
        if (cName.equals(Byte.class.getName()) || cName.equals(Byte.TYPE.getName())) {
            return ValueAndClassFactory.newValueAndClass(new Byte(b.byteValue()), inClass);
        } else if (cName.equals(Character.class.getName()) || cName.equals(Character.TYPE.getName())) {
            return ValueAndClassFactory.newValueAndClass(new Character((char) b.intValue()), inClass);
        } else if (cName.equals(Short.class.getName()) || cName.equals(Short.TYPE.getName())) {
            return ValueAndClassFactory.newValueAndClass(new Short(b.shortValue()), inClass);
        } else if (cName.equals(Integer.class.getName()) || cName.equals(Integer.TYPE.getName())) {
            return ValueAndClassFactory.newValueAndClass(new Integer(b.intValue()), inClass);
        } else if (cName.equals(Long.class.getName()) || cName.equals(Long.TYPE.getName())) {
            return ValueAndClassFactory.newValueAndClass(new Long(b.longValue()), inClass);
        } else if (cName.equals(Float.class.getName()) || cName.equals(Float.TYPE.getName())) {
            return ValueAndClassFactory.newValueAndClass(new Float(b.floatValue()), inClass);
        } else if (cName.equals(Double.class.getName()) || cName.equals(Double.TYPE.getName())) {
            return ValueAndClassFactory.newValueAndClass(new Double(b.doubleValue()), inClass);
        } else if (cName.equals(BigInteger.class.getName())) {
            return ValueAndClassFactory.newValueAndClass(b.toBigInteger(), inClass);
        } else if (cName.equals(BigDecimal.class.getName())) {
            return ValueAndClassFactory.newValueAndClass(b, inClass);
        } else {
            return null;
        }
    }

    public static final BigDecimal convertNumberToBigDecimal(Object n) {
        BigDecimal rbd = null;
        String cName = n.getClass().getName();
        if (cName.equals(Byte.class.getName()) || cName.equals(Short.class.getName()) || cName.equals(Integer.class.getName()) || cName.equals(Long.class.getName()) || cName.equals(Float.class.getName()) || cName.equals(Double.class.getName()) || cName.equals(Byte.TYPE.getName()) || cName.equals(Short.TYPE.getName()) || cName.equals(Integer.TYPE.getName()) || cName.equals(Long.TYPE.getName()) || cName.equals(Float.TYPE.getName()) || cName.equals(Double.TYPE.getName())) {
            rbd = new BigDecimal(((Number) n).doubleValue());
        } else if (cName.equals(Character.class.getName()) || cName.equals(Character.TYPE.getName())) {
            rbd = new BigDecimal(((Character) n).charValue());
        } else if (cName.equals(BigInteger.class.getName())) {
            rbd = new BigDecimal((BigInteger) n);
        } else if (cName.equals(BigDecimal.class.getName())) {
            rbd = (BigDecimal) n;
        } else {
            return null;
        }
        return rbd;
    }

    public static final Class[] getDecimalClasses() {
        Class outClasses[] = new Class[16];
        outClasses[0] = Byte.class;
        outClasses[1] = Short.class;
        outClasses[2] = Integer.class;
        outClasses[3] = Long.class;
        outClasses[4] = Float.class;
        outClasses[5] = Double.class;
        outClasses[6] = Byte.TYPE;
        outClasses[7] = Short.TYPE;
        outClasses[8] = Integer.TYPE;
        outClasses[9] = Long.TYPE;
        outClasses[10] = Float.TYPE;
        outClasses[11] = Double.TYPE;
        outClasses[12] = Character.class;
        outClasses[13] = Character.TYPE;
        outClasses[14] = BigInteger.class;
        outClasses[15] = BigDecimal.class;
        return outClasses;
    }

    public static final boolean isStringNonNegativeNumber(String testString) {
        boolean retBool = true;
        if (testString != null) {
            for (int i = 0; i < testString.length(); i++) {
                char nextChar = testString.charAt(i);
                if (nextChar == '0' || nextChar == '1' || nextChar == '2' || nextChar == '3' || nextChar == '4' || nextChar == '5' || nextChar == '6' || nextChar == '7' || nextChar == '8' || nextChar == '9') {
                } else {
                    retBool = false;
                }
            }
            if (testString.length() == 0) {
                retBool = false;
            }
        } else {
            retBool = false;
        }
        return retBool;
    }

    public static final String[] convertClassArrayToStringArray(Class inClasses[]) {
        String outValue[] = null;
        if (inClasses != null) {
            outValue = new String[inClasses.length];
            for (int i = 0; i < inClasses.length; i++) {
                outValue[i] = (inClasses[i]).getName();
            }
        }
        return outValue;
    }

    public static final Class[] convertStringArrayToClassArray(String inStrings[]) {
        Class outValue[] = null;
        if (inStrings != null) {
            outValue = new Class[inStrings.length];
            for (int i = 0; i < inStrings.length; i++) {
                try {
                    outValue[i] = Class.forName(inStrings[i]);
                } catch (ClassNotFoundException cnfe) {
                    outValue[i] = null;
                }
            }
        }
        return outValue;
    }

    public static final String formatTime(long inMillis) {
        String dateString = "";
        Date modifiedDate = new Date(inMillis);
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(modifiedDate);
        int year = gc.get(Calendar.YEAR);
        int month = gc.get(Calendar.MONTH);
        int day = gc.get(Calendar.DAY_OF_MONTH);
        int hour = gc.get(Calendar.HOUR);
        int minute = gc.get(Calendar.MINUTE);
        int second = gc.get(Calendar.SECOND);
        int amPm = gc.get(Calendar.AM_PM);
        if (hour == 0) {
            hour = 12;
        }
        if (month == Calendar.JANUARY) {
            dateString = dateString + "Jan ";
        } else if (month == Calendar.FEBRUARY) {
            dateString = dateString + "Feb ";
        } else if (month == Calendar.MARCH) {
            dateString = dateString + "Mar ";
        } else if (month == Calendar.APRIL) {
            dateString = dateString + "Apr ";
        } else if (month == Calendar.MAY) {
            dateString = dateString + "May ";
        } else if (month == Calendar.JUNE) {
            dateString = dateString + "Jun ";
        } else if (month == Calendar.JULY) {
            dateString = dateString + "Jul ";
        } else if (month == Calendar.AUGUST) {
            dateString = dateString + "Aug ";
        } else if (month == Calendar.SEPTEMBER) {
            dateString = dateString + "Sep ";
        } else if (month == Calendar.OCTOBER) {
            dateString = dateString + "Oct ";
        } else if (month == Calendar.NOVEMBER) {
            dateString = dateString + "Nov ";
        } else if (month == Calendar.DECEMBER) {
            dateString = dateString + "Dec ";
        }
        dateString = dateString + day + ", " + year;
        if (minute < 10) {
            dateString = dateString + " " + hour + ":0" + minute;
        } else {
            dateString = dateString + " " + hour + ":" + minute;
        }
        if (amPm == Calendar.AM) {
            dateString = dateString + " AM";
        } else if (amPm == Calendar.PM) {
            dateString = dateString + " PM";
        }
        return dateString;
    }

    private static final String representStringAsNumberCode(String inString) {
        StringBuffer outValue = new StringBuffer();
        for (int i = 0; i < inString.length(); i++) {
            char nextChar = inString.charAt(i);
            int divisor = 10000;
            while (divisor != 0) {
                outValue.append((int) (nextChar / divisor));
                nextChar = (char) (nextChar % divisor);
                divisor = divisor / 10;
            }
        }
        return outValue.toString();
    }

    private static final String representNumberCodeAsString(String inCode) {
        StringBuffer outValue = new StringBuffer();
        for (int i = 0; i < inCode.length(); i = i + 5) {
            String nextCodeString = inCode.substring(i, i + 5);
            int nextCode = Integer.parseInt(nextCodeString);
            outValue.append((char) nextCode);
        }
        return outValue.toString();
    }

    public static final boolean isJavaIdentifierName(String inName) {
        boolean outValue = false;
        if (inName != null && inName.length() > 0) {
            outValue = true;
            for (int i = 0; i < inName.length(); i++) {
                if (i == 0) {
                    outValue &= Character.isJavaIdentifierStart(inName.charAt(i));
                } else {
                    outValue &= Character.isJavaIdentifierPart(inName.charAt(i));
                }
            }
        }
        return outValue;
    }

    public static final void deleteFileOrDirectory(File inFile) {
        if (inFile.isFile()) {
            inFile.delete();
        } else {
            File subFiles[] = inFile.listFiles();
            for (int i = 0; i < subFiles.length; i++) {
                deleteFileOrDirectory(subFiles[i]);
            }
            inFile.delete();
        }
    }

    public static final String literalReplace(String inSource, String inMatch, String inReplace) {
        StringBuffer outValue = new StringBuffer();
        int startPoint = 0;
        int endPoint = 0;
        int result = 0;
        while (endPoint != -1) {
            endPoint = inSource.indexOf(inMatch, startPoint);
            if (endPoint != -1) {
                outValue.append(inSource.substring(startPoint, endPoint));
                outValue.append(inReplace);
                startPoint = endPoint + inMatch.length();
            } else {
                outValue.append(inSource.substring(startPoint));
            }
        }
        return outValue.toString();
    }

    public static final String readTextFile(File inFile) {
        try {
            StringBuffer outValue = new StringBuffer();
            if (inFile.exists()) {
                FileReader fr = new FileReader(inFile);
                int nextChar = 0;
                while ((nextChar = fr.read()) != -1) {
                    outValue.append((char) nextChar);
                }
                fr.close();
            }
            return outValue.toString();
        } catch (IOException ioe) {
            throw new PFRuntimeException(ioe);
        }
    }

    public static final void writeTextFile(File inFile, String inContent) {
        try {
            FileWriter fw = new FileWriter(inFile);
            fw.write(inContent);
            fw.flush();
            fw.close();
        } catch (IOException ioe) {
            throw new PFRuntimeException(ioe);
        }
    }

    public static final void writeBinaryFile(File inFile, byte inContent[]) {
        try {
            FileOutputStream fos = new FileOutputStream(inFile);
            fos.write(inContent);
            fos.flush();
            fos.close();
        } catch (IOException ioe) {
            throw new PFRuntimeException(ioe);
        }
    }

    public static final byte[] readBinaryFile(File inFile) {
        try {
            ByteArrayOutputStream outValue = new ByteArrayOutputStream();
            byte buffer[] = new byte[16 * 1024];
            FileInputStream fis = new FileInputStream(inFile);
            int size = 0;
            while ((size = fis.read(buffer)) != -1) {
                outValue.write(buffer, 0, size);
            }
            fis.close();
            return outValue.toByteArray();
        } catch (IOException ioe) {
            throw new PFRuntimeException(ioe);
        }
    }

    public static final String removeFileExtension(String fileName) {
        int periodPoint = fileName.indexOf('.');
        return fileName.substring(0, periodPoint);
    }

    public static final void zipDirectory(File inDirName, OutputStream inOs) throws IOException {
        ZipOutputStream zos = new ZipOutputStream(inOs);
        zipDirectory(inDirName, inDirName, zos);
        zos.flush();
        zos.close();
    }

    private static final void zipDirectory(File inBaseDir, File inCurrentDir, ZipOutputStream inZos) throws IOException {
        File dirContents[] = inCurrentDir.listFiles();
        byte buffer[] = new byte[1024];
        for (int i = 0; i < dirContents.length; i++) {
            File nextFile = dirContents[i];
            if (nextFile.isFile()) {
                FileInputStream fis = new FileInputStream(nextFile);
                String entryName = nextFile.getAbsolutePath().substring(inBaseDir.getAbsolutePath().length() + 1);
                entryName = entryName.replace(File.separatorChar, '/');
                inZos.putNextEntry(new ZipEntry(entryName));
                int len = 0;
                while ((len = fis.read(buffer, 0, buffer.length)) != -1) {
                    inZos.write(buffer, 0, len);
                }
                inZos.closeEntry();
                fis.close();
            } else {
                zipDirectory(inBaseDir, nextFile, inZos);
            }
        }
    }

    private static final void createDir(File inDir) {
        if (!inDir.exists()) {
            createDir(inDir.getParentFile());
            inDir.mkdir();
        }
    }

    public static final void parseZipInfo(InputStream inIs, File inBaseDir) throws IOException {
        ZipInputStream zis = new ZipInputStream(inIs);
        ZipEntry nextEntry = null;
        while ((nextEntry = zis.getNextEntry()) != null) {
            String nextEntryName = nextEntry.getName();
            nextEntryName = nextEntryName.replace('/', File.separatorChar);
            File nextEntryFile = new File(inBaseDir, nextEntryName);
            if (nextEntry.isDirectory()) {
                createDir(nextEntryFile);
            } else {
                createDir(nextEntryFile.getParentFile());
                FileOutputStream fos = new FileOutputStream(nextEntryFile);
                byte buffer[] = new byte[1024];
                int len = 0;
                while ((len = zis.read(buffer, 0, buffer.length)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.flush();
                fos.close();
            }
            zis.closeEntry();
        }
        zis.close();
    }

    public static final void justIndent(int inIndent[]) {
        inIndent[0] = inIndent[0] + 4;
    }

    public static final void justRevert(int inIndent[]) {
        inIndent[0] = inIndent[0] - 4;
    }

    public static final void indentAndPrint(StringBuffer inSb, int inIndent[], String inValue) {
        justIndent(inIndent);
        simpleAndPrint(inSb, inIndent, inValue);
    }

    public static final void revertAndPrint(StringBuffer inSb, int inIndent[], String inValue) {
        justRevert(inIndent);
        simpleAndPrint(inSb, inIndent, inValue);
    }

    public static final void simpleAndPrint(StringBuffer inSb, int inIndent[], String inValue) {
        int spaceCount = inIndent[0];
        for (int i = 0; i < spaceCount; i++) {
            inSb.append(" ");
        }
        inSb.append(inValue);
        inSb.append("\n");
    }

    public static final String prependSpaces(int inSpaceCount[]) {
        final String TWO_SPACES = "&nbsp;&nbsp;";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < inSpaceCount[0]; i = i + 2) {
            sb.append(TWO_SPACES);
        }
        return sb.toString();
    }
}
