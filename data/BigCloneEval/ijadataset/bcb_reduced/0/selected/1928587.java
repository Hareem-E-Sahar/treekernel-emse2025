package org.jdamico.ircivelaclient.util;

import java.awt.Color;
import java.awt.Point;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import org.jdamico.ircivelaclient.config.Constants;

public class IRCIvelaClientStringUtils {

    private static final IRCIvelaClientStringUtils getInstance = new IRCIvelaClientStringUtils();

    public static IRCIvelaClientStringUtils singleton() {
        return getInstance;
    }

    public String showVersion() {
        String formatedVersion = null;
        formatedVersion = "<font color='green'><b>" + Constants.APPNAME + " " + Constants.APPVERSION + "</b></font>";
        return formatedVersion;
    }

    public String setMessage(String unformatedMessage, int row, String color, boolean isTeacher, boolean isPvt) {
        String formatedMessage = null;
        String fontColor = "'" + color + "'";
        String bgColor = "'#FFFFFF'";
        if (isTeacher) {
            String msgTB_a = "<table width=\"100%\" bgcolor='" + "black" + "' border ='0' cellpadding='4'><tr valign='top'><td width='90'>";
            String msgTB_b = "</td><td>";
            String msgTB_c = "</td></tr></table>";
            formatedMessage = msgTB_a + getTimeTeacher() + msgTB_b + "<font color=" + "white" + " face='Arial' ><b>" + unformatedMessage.replaceAll("|", "") + "</b></font>" + msgTB_c;
            return formatedMessage;
        }
        if (isPvt) {
            bgColor = "#FFFFFF";
        } else {
            if (row % 2 == 0) bgColor = "#EFEFEF"; else bgColor = "#FFFFFF";
        }
        String msgTB_a = "<table width=\"100%\" bgcolor='" + bgColor + "' border ='0' cellpadding='4'><tr valign='top'><td width='90'>";
        String msgTB_b = "</td><td>";
        String msgTB_c = "</td></tr></table>";
        formatedMessage = msgTB_a + getTime() + msgTB_b + "<font color=" + fontColor + ">" + unformatedMessage.replaceAll("|", "") + "</font>" + msgTB_c;
        return formatedMessage;
    }

    public String setMyMessage(String unformatedMessage) {
        String formatedMessage = null;
        formatedMessage = "<font color'#666666'>Me: " + unformatedMessage + "</font>";
        return formatedMessage;
    }

    public String getTime() {
        Format formatter;
        Date date = new Date();
        formatter = new SimpleDateFormat("HH:mm:ss");
        String stime = formatter.format(date);
        return "<font color'#efefef'>[" + stime + "]&nbsp</font>";
    }

    public String getTimeTeacher() {
        Format formatter;
        Date date = new Date();
        formatter = new SimpleDateFormat("HH:mm:ss");
        String stime = formatter.format(date);
        return "<font color='white'>[" + stime + "]&nbsp</font>";
    }

    public Color getColorParameter(String strColor) {
        int hexConverted = 0;
        hexConverted = Integer.parseInt(strColor, 16);
        Color returnColor = Color.decode(String.valueOf(hexConverted));
        return returnColor;
    }

    public static int generateRandomNumber(int maxSize) {
        Random rand = new Random();
        int randnum = rand.nextInt();
        randnum = rand.nextInt(maxSize + 1);
        return randnum;
    }

    public static String generateInfoString(List<P2P> list) {
        String s = "";
        for (P2P p2p : list) {
            s += p2p.p1.x + "-";
            s += p2p.p1.y + "-";
            s += p2p.p2.x + "-";
            s += p2p.p2.y + "-";
            s += "#";
        }
        return s;
    }

    public static List<P2P> degenerateInfoString(String msg) {
        msg = msg.trim();
        ArrayList<P2P> list = new ArrayList<P2P>();
        StringTokenizer tokens = new StringTokenizer(msg, "#");
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            String numbers[] = token.split("-");
            P2P p2p = new P2P();
            Point p1 = new Point(Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1]));
            Point p2 = new Point(Integer.parseInt(numbers[2]), Integer.parseInt(numbers[3]));
            p2p.p1 = p1;
            p2p.p2 = p2;
            list.add(p2p);
        }
        return list;
    }

    public static void main(String[] args) {
        System.out.println(generateRandomNumber(5));
    }
}
