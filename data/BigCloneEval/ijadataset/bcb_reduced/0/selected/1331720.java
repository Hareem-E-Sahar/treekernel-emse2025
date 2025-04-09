package ro.cuzma.larry.persistance.xml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import ro.cuzma.larry.persistance.xml.exception.XMLException;

public class XMLUtil {

    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

    public static String getStartTag(XMLObject xmlObj, String indent, boolean hasValue) {
        String ret = "";
        String tag = xmlObj.getTag();
        if (tag == null) {
            ret = "";
        } else {
            if (indent != null && !indent.equals("")) {
                ret = "\n";
            }
            ret += indent + "<" + tag;
            for (String key : xmlObj.getAtributes().keySet()) {
                ret += " " + key + "=\"" + convertXMLChars(xmlObj.getAtribute(key)) + "\"";
            }
            if (hasValue) {
                ret += ">";
            } else {
                ret += "/>";
            }
        }
        return ret;
    }

    public static String getEndTagNewLine(String tag, String indent) {
        if (tag == null) {
            return "";
        } else {
            return "\n" + indent + "</" + tag + ">";
        }
    }

    public static String getEndTag(String tag) {
        if (tag == null) {
            return "";
        } else {
            return "</" + tag + ">";
        }
    }

    public static String convertGregorianCalendar(GregorianCalendar cal, SimpleDateFormat format) {
        if (format == null) {
            format = simpleDateFormat;
        }
        return format.format(cal.getTime());
    }

    public static GregorianCalendar convertGregorianCalendar(String value, SimpleDateFormat format) {
        if (format == null) {
            format = simpleDateFormat;
        }
        Date date;
        try {
            date = format.parse(value);
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            return calendar;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File saveXMLToFile(String fileName, XMLObject xmlObject) throws XMLException {
        File xmlFile;
        try {
            xmlFile = new File(fileName);
            BufferedWriter out = new BufferedWriter(new FileWriter(xmlFile));
            out.write("<?xml version='1.0' encoding='utf-8'?>\r\n");
            out.write(xmlObject.toXml(""));
            out.close();
            return xmlFile;
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    public static void fileToZIP(File fileName, String zipFileName) {
        try {
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFileName)));
            byte[] data = new byte[1000];
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileName));
            int count;
            out.putNextEntry(new ZipEntry(fileName.getName()));
            while ((count = in.read(data, 0, 1000)) != -1) {
                out.write(data, 0, count);
            }
            in.close();
            out.flush();
            out.close();
            System.out.println("Your file is zipped");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String convertXMLChars(String input) {
        String ret = "";
        if (input != null) {
            ret = input.replace("\"", "&quot;");
        }
        return ret;
    }
}
