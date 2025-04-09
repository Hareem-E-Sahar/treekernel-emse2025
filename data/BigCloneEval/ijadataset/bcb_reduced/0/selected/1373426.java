package com.ahm.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;
import com.ahm.db.entity.EmailConfig;

/**
 * @author JP
 *
 */
public class Utils {

    public Utils() {
        super();
    }

    public static String longArrayToCommaSeparatedString(long[] longArray) {
        StringBuffer str = new StringBuffer("");
        try {
            for (long val : longArray) {
                str.append(",").append(val);
            }
            if (str.length() > 0) str = str.delete(0, 1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return str.toString();
    }

    public static int[] convertStringToIntegerArray(String str) {
        if (str == null) return null;
        String[] idStr = str.split(",");
        int[] ids = new int[idStr.length];
        for (int i = 0; i < idStr.length; i++) {
            ids[i] = Integer.parseInt(idStr[i]);
        }
        return ids;
    }

    public static long[] convertStringToLongArray(String str) {
        if (str == null) return null;
        String[] idStr = str.split(",");
        ArrayList<Long> ids = new ArrayList<Long>();
        for (int i = 0; i < idStr.length; i++) {
            if (idStr[i].trim().length() > 0) {
                ids.add(new Long(Long.parseLong(idStr[i].trim())));
            }
        }
        Long ia[] = new Long[ids.size()];
        ia = ids.toArray(ia);
        return ArrayUtils.toPrimitive(ia);
    }

    public static Integer[] convertStringToIntegerObjectArray(String str) {
        if (str == null) return null;
        String[] idStr = str.split(",");
        Integer[] ids = new Integer[idStr.length];
        for (int i = 0; i < idStr.length; i++) {
            ids[i] = new Integer(idStr[i]);
        }
        return ids;
    }

    public static Date convertStringToUtilDate(String str, String delimiter) {
        if (str == null) return null;
        String[] idStr = str.split(delimiter);
        if (idStr.length == 3) {
            return new Date((Integer.parseInt(idStr[2]) - 1900), Integer.parseInt(idStr[0]) - 1, Integer.parseInt(idStr[1]));
        }
        return null;
    }

    public static int noOfDaysbetweenDates(Date d1, Date d2) {
        int noOfDays = 0;
        try {
            return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return noOfDays;
    }

    public static int noOfDaysbetweenGivenDateAndCurrentDate(Date d1) {
        int noOfDays = 0;
        try {
            Calendar cal = new GregorianCalendar();
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            Date d2 = new Date(year - 1900, month, day);
            Date givenDae = new Date(d1.getYear(), d1.getMonth(), d1.getDate());
            return (int) ((d2.getTime() - givenDae.getTime()) / (1000 * 60 * 60 * 24));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return noOfDays;
    }

    public static void main(String args[]) throws Exception {
        System.out.println("PTR1:" + noOfDaysbetweenDates(new Date(2010, 1, 11), new Date()));
        System.out.println("PTR2:" + noOfDaysbetweenGivenDateAndCurrentDate(new Date("12/21/2010")));
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        System.out.println(date);
        System.out.println("----------------------------------------------------------");
        System.out.println(encrypt("licenseCode") + "$#@!" + encrypt("3aaa 1dddd e3332 aaa2 999833 sss12#$"));
        System.out.println(encrypt("startDate") + "$#@!" + encrypt("12/16/2010"));
        System.out.println(encrypt("endDate") + "$#@!" + encrypt("12/30/2010"));
        System.out.println(encrypt("noOfUsers") + "$#@!" + encrypt("5"));
        System.out.println(encrypt("noOfDays") + "$#@!" + encrypt("14"));
        System.out.println(encrypt("macAddress") + "$#@!" + encrypt("00-24-7E-E0-28-B2"));
        System.out.println(encrypt("validateOrNot") + "$#@!" + encrypt("no"));
        System.out.println("----------------------------------------------------------");
        FileWriter fstream = new FileWriter(new File("D://out.txt"));
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(encrypt("licenseCode") + "CLEAREQ" + encrypt("3aaa 1dddd e3332 aaa2 999833 sss12#$") + "\n");
        out.write(encrypt("startDate") + "CLEAREQ" + encrypt("12/16/2010") + "\n");
        out.write(encrypt("endDate") + "CLEAREQ" + encrypt("12/30/2010") + "\n");
        out.write(encrypt("noOfUsers") + "CLEAREQ" + encrypt("5") + "\n");
        out.write(encrypt("noOfDays") + "CLEAREQ" + encrypt("14") + "\n");
        out.write(encrypt("macAddress") + "CLEAREQ" + encrypt("00-24-7E-E0-28-B2") + "\n");
        out.write(encrypt("validateOrNot") + "CLEAREQ" + encrypt("no") + "\n");
        out.close();
        File f = new File("D://out.txt");
        FileInputStream fis = new FileInputStream(f);
        BufferedReader br = new BufferedReader(new FileReader(f));
        String thisLine = null;
        while ((thisLine = br.readLine()) != null) {
            System.out.println(thisLine);
            String[] argss = thisLine.split("CLEAREQ");
            System.out.println(argss.length);
            System.out.println(decrypt(argss[0]) + "=" + decrypt(argss[1]));
        }
    }

    public static Map<Long, Object> convertListToMap(Collection<?> colObj, String methodName) {
        Map<Long, Object> mapObj = new HashMap<Long, Object>();
        try {
            for (Object obj : colObj) {
                Method m = obj.getClass().getMethod(methodName);
                Long i = (Long) m.invoke(obj);
                mapObj.put(i, obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapObj;
    }

    public static LinkedHashMap<Long, Object> convertListToLinkedMap(Collection<?> colObj, String methodName) {
        LinkedHashMap<Long, Object> mapObj = new LinkedHashMap<Long, Object>();
        try {
            for (Object obj : colObj) {
                Method m = obj.getClass().getMethod(methodName);
                Long i = (Long) m.invoke(obj);
                mapObj.put(i, obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapObj;
    }

    public static Map<Integer, Object> convertListToMapOfInt(Collection<?> colObj, String methodName) {
        Map<Integer, Object> mapObj = new HashMap<Integer, Object>();
        try {
            for (Object obj : colObj) {
                Method m = obj.getClass().getMethod(methodName);
                Integer i = (Integer) m.invoke(obj);
                mapObj.put(i, obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapObj;
    }

    public static LinkedHashMap<Integer, Object> convertListToLinkedMapOfInt(Collection<?> colObj, String methodName) {
        LinkedHashMap<Integer, Object> mapObj = new LinkedHashMap<Integer, Object>();
        try {
            for (Object obj : colObj) {
                Method m = obj.getClass().getMethod(methodName);
                Integer i = (Integer) m.invoke(obj);
                mapObj.put(i, obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapObj;
    }

    public static Map<String, Object> convertListToMapWithGivenFieldName(Collection<?> colObj, String methodName) {
        Map<String, Object> mapObj = new HashMap<String, Object>();
        try {
            for (Object obj : colObj) {
                Method m = obj.getClass().getMethod(methodName);
                String i = (String) m.invoke(obj);
                mapObj.put(i, obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapObj;
    }

    public static String listToString(List<?> list) {
        String result = null;
        try {
            if (list != null) {
                Iterator<?> iterator = list.iterator();
                StringBuffer sb = new StringBuffer();
                while (iterator.hasNext()) {
                    sb.append(iterator.next() + ", ");
                }
                result = sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "0";
                System.out.println(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static List getCustomListWithParameters(String className, String methodName, String page, List baseList, String rows, String sord) {
        if (sord.equals("asc")) {
            GenericComparatorNaturalOrder genComp = new GenericComparatorNaturalOrder();
            genComp.setClassName(className);
            genComp.setMethodName(methodName);
            Collections.sort(baseList, genComp);
        } else {
            GenericComparatorReverseOrder genComp = new GenericComparatorReverseOrder();
            genComp.setClassName(className);
            genComp.setMethodName(methodName);
            Collections.sort(baseList, genComp);
        }
        List finalList = new ArrayList();
        int count = Integer.parseInt(rows);
        int pageNo = (page == null ? 1 : Integer.parseInt(page));
        int start = count * (pageNo - 1);
        int end = start + count;
        for (int i = start; i < end; i++) {
            if (baseList.size() <= i) break;
            finalList.add(baseList.get(i));
        }
        return finalList;
    }

    public static String convertXMLDataTODatainTAGPerLine(String str, String tag) {
        str = str.replaceAll("</" + tag + ">", "");
        str = str.replaceAll("<" + tag + ">", "<br/>");
        return str;
    }

    public static List getSortedListInAscending(List baseList, String className, String methodName) {
        if (baseList != null && baseList.size() > 0) {
            GenericComparatorNaturalOrder genComp = new GenericComparatorNaturalOrder();
            genComp.setClassName(className);
            genComp.setMethodName(methodName);
            Collections.sort(baseList, genComp);
            return baseList;
        }
        return baseList;
    }

    public static List getSortedListInDescending(List baseList, String className, String methodName) {
        if (baseList != null && baseList.size() > 0) {
            GenericComparatorReverseOrder genComp = new GenericComparatorReverseOrder();
            genComp.setClassName(className);
            genComp.setMethodName(methodName);
            Collections.sort(baseList, genComp);
            return baseList;
        }
        return baseList;
    }

    public static String convertListOfLongValuesToString(List<Long> baseList) {
        String ids = "";
        if (baseList.size() > 0) {
            Iterator<Long> iterator = baseList.iterator();
            while (iterator.hasNext()) {
                ids = ids + "," + iterator.next();
            }
            ids = ids.substring(1);
        }
        return ids;
    }

    public static String integerListToCommaSeparatedString(List<Integer> intList) {
        String resultString = null;
        StringBuilder sb = new StringBuilder();
        try {
            if (intList != null) {
                Iterator<Integer> iterator = intList.iterator();
                Integer id = null;
                while (iterator.hasNext()) {
                    id = iterator.next();
                    sb.append(id);
                    sb.append(",");
                }
            }
            if (sb.length() > 0) {
                resultString = sb.substring(0, sb.length() - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("RESULT STRING:" + resultString);
        return resultString;
    }

    /**
	 * @return
	 */
    public static String getHostSystemMacAddress() {
        String macAddress = null;
        try {
            InetAddress thisIp = InetAddress.getLocalHost();
            String ipAddress = thisIp.getHostAddress();
            System.out.println("-- Ip address --:" + ipAddress);
            InetAddress address = InetAddress.getByName(ipAddress);
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            StringBuffer sb = new StringBuffer();
            if (ni != null) {
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    System.out.println("--mac addr--:" + sb);
                } else {
                    System.out.println("Address doesn't exist or is not accessible.");
                }
            } else {
                System.out.println("Network Interface for the specified address is not found.");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return macAddress;
    }

    public static synchronized String encrypt(String plaintext) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String encryptedString = null;
        try {
            byte[] encodedBytes = Base64.encodeBase64(plaintext.getBytes());
            encryptedString = new String(encodedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedString;
    }

    public static synchronized String decrypt(String encodedString) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String decryptedString = null;
        try {
            decryptedString = new String(Base64.decodeBase64(encodedString.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedString;
    }

    public static String cutAndDecrypt(String str) {
        try {
            str = str.substring(str.indexOf("=started") + 8, str.indexOf("ended"));
            str = decrypt(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static void sendEmail(Map<String, String> lablesMap, EmailConfig emailConfig) {
        try {
            List attachments = new ArrayList<String>();
            MailClient mailClient = new MailClient();
            String[] to = null;
            if (emailConfig.getRecipent() != null && emailConfig.getRecipent().length() > 0) to = emailConfig.getRecipent().split(",");
            String server = emailConfig.getSender().getSmtpHost();
            int port = emailConfig.getSender().getSmtpPort();
            String from = AhmConstants.FROM_ID;
            String authUser = emailConfig.getSender().getSenderEmail();
            String authPasswd = emailConfig.getSender().getSenderPasswd();
            String subject = emailConfig.getSubject();
            String message = bodyReplacebyLabels(emailConfig.getBody(), lablesMap);
            mailClient.sendMail(server, port, authUser, authPasswd, from, to, subject, message, attachments, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String bodyReplacebyLabels(String str, Map<String, String> map) {
        String orignString = str;
        while (str.indexOf("<") > -1) {
            int startTag = str.indexOf("<") + 1;
            int endTag = str.indexOf(">");
            String variable = str.substring(startTag, endTag);
            orignString = orignString.replace("<" + variable + ">", map.get("<" + variable + ">"));
            str = str.substring(endTag + 1);
        }
        return orignString;
    }
}
