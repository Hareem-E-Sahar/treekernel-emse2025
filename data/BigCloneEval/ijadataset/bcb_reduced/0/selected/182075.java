package com.huaren.model;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;

public class UtilBean {

    String[] chackMoney = new String[2];

    public UtilBean() {
    }

    /**
	 * @author ף�Σ���
	 * �ṩ�Լ۸�ķָ��
	 * @param arg �۸��ַ��м���","Ϊ�ָ��
	 * @return ���ش����ļ۸�����
	 */
    public String[] getChackMoney(String arg, String tmp) {
        StringTokenizer parser = new StringTokenizer(arg, tmp);
        int i = 0;
        while (parser.hasMoreTokens()) {
            chackMoney[i] = parser.nextToken();
            i++;
        }
        return chackMoney;
    }

    /**
	 * @author ף�Σ���
	 * �ṩ���ַ�ķָ��
	 * @param arg �ַ�
	 * @param tmp �ָ���
	 * @return ���ش������ַ�arrayList����
	 */
    public ArrayList getChackString(String arg, String tmp) {
        ArrayList lst = new ArrayList();
        StringTokenizer parser = new StringTokenizer(arg, tmp);
        int i = 0;
        while (parser.hasMoreTokens()) {
            lst.add(i, parser.nextToken());
            i++;
        }
        return lst;
    }

    /**
	 * @author ף�Σ���
	 * �ṩ����Ա�����ݡ����ʡ����������ַ�ķָ�
	 * @param arg ��Ա�����ݡ����ʡ����������ַ����ҷָ����зָ
	 * @return ���ش�����ArrayList����
	 */
    public ArrayList getCheckChar(String arg) {
        ArrayList lst = new ArrayList();
        String arg_tmp = "";
        if (arg.indexOf("��") > 0) {
            arg_tmp = "��";
        } else if (arg.indexOf(" ") > 0) {
            arg_tmp = " ";
        } else if (arg.indexOf("��") > 0) {
            arg_tmp = "��";
        } else if (arg.indexOf("��") > 0) {
            arg_tmp = "��";
        } else if (arg.indexOf("��") > 0) {
            arg_tmp = "��";
        } else if (arg.indexOf("��") > 0) {
            arg_tmp = "��";
        } else if (arg.indexOf("��") > 0) {
            arg_tmp = "��";
        } else if (arg.indexOf("��") > 0) {
            arg_tmp = "��";
        } else if (arg.indexOf("��") > 0) {
            arg_tmp = "��";
        } else if (arg.indexOf("��") > 0) {
            arg_tmp = "��";
        } else if (arg.indexOf("&&&&") > 0) {
            arg_tmp = "&&&&";
        } else {
            arg_tmp = " ";
        }
        StringTokenizer parser = new StringTokenizer(arg, arg_tmp);
        int i = 0;
        while (parser.hasMoreTokens()) {
            lst.add(i, parser.nextToken());
            i++;
        }
        return lst;
    }

    Calendar cal = Calendar.getInstance();

    SimpleDateFormat dateFm = new SimpleDateFormat("yyyyMMdd");

    String mDateTime = dateFm.format(cal.getTime());

    /**
	 * @author ף�Σ���
	 * ȡ�õ�ǰ�����
	 * @return ����int�����
	 */
    public int getYear() {
        int year = Integer.parseInt(mDateTime.substring(0, 4));
        return year;
    }

    /**
	 * @author ף�Σ���
	 * ȡ�õ�ǰ���·�
	 * @return ����int���·�
	 */
    public int getMounth() {
        int mounth = Integer.parseInt(mDateTime.substring(4, 6));
        return mounth;
    }

    /**
	 * @author ף�Σ���
	 * ȡ�õ�ǰ������
	 * @return ����int������
	 */
    public int getDay() {
        int day = Integer.parseInt(mDateTime.substring(6, 8));
        return day;
    }

    /**
	 * @author ף�Σ���
	 * ȡ�õ�ǰ���������ں�ʱ��
	 * @return ����String�����ڣ���ʽΪ��yyyy-MM-dd hh:mm:ss
	 */
    public String getTime() {
        SimpleDateFormat dateFm1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = dateFm1.format(cal.getTime());
        return time;
    }

    /**
	 * @author ף�Σ���
	 * ȡ��tmp�������ں�ʱ��
	 * @return ����String�����ڣ���ʽΪ��yyyy-MM-dd hh:mm:ss
	 * @throws ParseException 
	 */
    public String getTimeOneYear(String tmp, String date) throws ParseException {
        SimpleDateFormat dateFm1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date myDate = dateFm1.parse(date);
        int tmp1 = Integer.parseInt(tmp);
        long myTime = (myDate.getTime() / 1000) + 365 * 60 * 60 * 24 * tmp1;
        myDate.setTime(myTime * 1000);
        String mDate = dateFm1.format(myDate);
        return mDate;
    }

    /**
	 * @author ף�Σ���
	 * ȡ��һ�������ں�ʱ��
	 * @return ����String�����ڣ���ʽΪ��yyyy-MM-dd-hh-mm-ss
	 * @throws ParseException 
	 */
    public String getTimeOneSecond(String date) throws ParseException {
        SimpleDateFormat dateFm1 = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        java.util.Date myDate = dateFm1.parse(date);
        long myTime = (myDate.getTime() / 1000) + 1;
        myDate.setTime(myTime * 1000);
        String mDate = dateFm1.format(myDate);
        return mDate;
    }

    /**
	 * @author ף�Σ���
	 * ȡ�õ�ǰ���������ں�ʱ��
	 * @return ����String�����ڣ���ʽΪ��yyyy-MM-dd-hh-mm-ss
	 */
    public String getTime4() {
        SimpleDateFormat dateFm1 = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String time = dateFm1.format(cal.getTime());
        return time;
    }

    /**
	 * @author ף�Σ���
	 * @param date ʱ������ʽΪ��yyyy-MM-dd-hh-mm-ss
	 * ȡ�õ�ǰ���������ں�ʱ��
	 * @return ����String�����ڣ���ʽΪ��yyyy-MM-dd hh:mm:ss
	 */
    public String getTime5(String date) {
        String time = "";
        time = date.substring(0, 4) + "-" + date.substring(5, 7) + "-" + date.substring(8, 10) + " " + date.substring(11, 13) + ":" + date.substring(14, 16) + ":" + date.substring(17, 19);
        return time;
    }

    /**
	 * @author ף�Σ���
	 * ȡ�õ�ǰ���������ں�ʱ��
	 * @return ����String�����ڣ���ʽΪ��yyyy-MM-dd
	 */
    public String getTime1() {
        SimpleDateFormat dateFm1 = new SimpleDateFormat("yyyy-MM-dd");
        String time = dateFm1.format(cal.getTime());
        return time;
    }

    /**
	 * @author ף�Σ���
	 * ȡ�õ�ǰ���������ں�ʱ��
	 * @return ����String�����ڣ���ʽΪ��yyyyMMdd
	 */
    public String getTime2() {
        SimpleDateFormat dateFm1 = new SimpleDateFormat("yyyyMMdd");
        String time = dateFm1.format(cal.getTime());
        return time;
    }

    /**
	 * @author ף�Σ���
	 * ȡ�õ�ǰ���������ں�ʱ��
	 * @return ����String�����ڣ���ʽΪ��yyMMdd
	 */
    public String getTime3() {
        SimpleDateFormat dateFm1 = new SimpleDateFormat("yyMMdd");
        String time = dateFm1.format(cal.getTime());
        return time;
    }

    /**
	 * @author ף�Σ���
	 * Ĭ�ϵĳ����㾫�ȡ�
	 */
    private static final int DEF_DIV_SCALE = 2;

    /**
	 * @author ף�Σ���
	 * �ṩ��ȷ�ļӷ����㡣
	 * @param v1 ������
	 * @param v2 ����
	 * @return ��������ĺ�
	 */
    public double add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    /**
	 * @author ף�Σ���
	 * �ṩ��ȷ�ļ������㡣
	 * @param v1 ������
	 * @param v2 ����
	 * @return ��������Ĳ�
	 */
    public double sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    /**
	 * @author ף�Σ���
	 * �ṩ��ȷ�ĳ˷����㡣
	 * @param v1 ������
	 * @param v2 ����
	 * @return ��������Ļ�
	 */
    public double mul(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }

    /**
	 * @author ף�Σ���
	 * �ṩ����ԣ���ȷ�ĳ����㣬��������ʱ��
	 * ��ȷ��С����Ժ�10λԪ���Ժ�������������롣
	 * @param v1 ������
	 * @param v2 ����
	 * @return �����������
	 */
    public double div(double v1, double v2) {
        return div(v1, v2, DEF_DIV_SCALE);
    }

    /**
	 * @author ף�Σ���
	 * �ṩ����ԣ���ȷ�ĳ����㣬��������ʱ�򣬣���scale����ָ
	 * �����ȣ��Ժ�������������롣
	 * @param v1 ������
	 * @param v2 ����
	 * @param scale ��ʾ��Ҫ��ȷ��С����Ժ�λ��
	 * @return �����������
	 */
    public double div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
	 * @author ף�Σ���
	 * �ṩ��ȷ��С��λ�������봦�?
	 * @param v ��Ҫ���������λ��
	 * @param scale С��������λ
	 * @return ��������Ľ��
	 */
    public double round(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
	 * @author ף�Σ���
	 * �ṩ�ַ��滻
	 * @param sourceString��ԭ�ַ�
	 * @param toReplaceString�����滻���ַ�
	 * @param replaceString���滻���ַ�
	 * @return returnString �滻����ַ�
	 */
    public String stringReplace(String sourceString, String toReplaceString, String replaceString) {
        String returnString = sourceString;
        int stringLength = 0;
        if (toReplaceString != null) {
            stringLength = toReplaceString.length();
        }
        if (returnString != null && returnString.length() > stringLength) {
            int max = 0;
            String S4 = "";
            for (int i = 0; i < sourceString.length(); i++) {
                max = i + toReplaceString.length() > sourceString.length() ? sourceString.length() : i + stringLength;
                String S3 = sourceString.substring(i, max);
                if (!S3.equals(toReplaceString)) {
                    S4 += S3.substring(0, 1);
                } else {
                    S4 += replaceString;
                    i += stringLength - 1;
                }
            }
            returnString = S4;
        }
        return returnString;
    }

    /**
	 * @author ף�Σ���
	 * �ṩ�ַ��滻��������ʱ��Ӣ���ַ�ת���������ַ�
	 * @param sourceString��ԭ�ַ�
	 * @return returnChar �滻����ַ�
	 */
    public String stringReplace(String sourceString) {
        String returnChar = "";
        if ("".equals(sourceString) || null == sourceString) {
            returnChar = "";
        } else {
            if (sourceString.indexOf("'") > 0) {
                returnChar = sourceString.replaceAll("'", "��");
            } else {
                returnChar = sourceString;
            }
        }
        return returnChar;
    }

    private String str;

    private int counterOfDoubleByte;

    private byte b[];

    /**
	 * @author ף�Σ���
	 * ������Ҫ�����Ƴ��ȵ��ַ�
	 * @param str ��Ҫ�����Ƴ��ȵ��ַ�
	 */
    public void setLimitLengthString(String str) {
        this.str = str;
    }

    /**
	 * @author ף�Σ���
	 * @param len ��Ҫ��ʾ�ĳ���(ע�⣺��������byteΪ��λ�ģ�һ��������2��byte)
	 * @param symbol ���ڱ�ʾʡ�Ե���Ϣ���ַ��硰...��,��>>>���ȡ�
	 * @param arg��ԭ�ַ�
	 * @return ���ش������ַ�
	 */
    public String getLimitLengthString(int len, String symbol, String arg) throws UnsupportedEncodingException {
        counterOfDoubleByte = 0;
        str = arg;
        b = str.getBytes("GBK");
        if (b.length <= len) return str;
        for (int i = 0; i < len; i++) {
            if (b[i] < 0) counterOfDoubleByte++;
        }
        if (counterOfDoubleByte % 2 == 0) return new String(b, 0, len, "GBK") + symbol; else return new String(b, 0, len - 1, "GBK") + symbol;
    }
}
