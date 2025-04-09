package org.qinghailake.birdmigration.mail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.qinghailake.birdmigration.util.SimpleLogger;

/**
 * �ļ���Ԥ���?��,�������в�����ĸ�ʽͳһת��ΪString����,
 * ת����ɵ�ÿ��Ԫ��Ϊһ����¼,����������ȱʧʹ��"."�����.
 * @author Haiting Zhang
 *
 */
public class MailPreProcessor {

    private static Logger logger = Logger.getLogger("Mail");

    public static void main(String[] args) throws Exception {
        MailPreProcessor mailPreProcessor = new MailPreProcessor();
        String[] lines = mailPreProcessor.process("mail/synopsis_qinghai08a.txt", MailType.ARGOS, 1);
    }

    public String[] process(String fileName, int TYPE, int trial) throws Exception {
        if (TYPE == MailType.BH) {
            return BHProcess(fileName, trial);
        } else if (TYPE == MailType.GPS) {
            return GPSProcess(fileName, trial);
        } else if (TYPE == MailType.ARGOS) {
            return ArgosProcess(fileName, trial);
        }
        return null;
    }

    /**
	 * ������"BH"���͵��ı�����ļ�,���ı������е�����ݳ�ȡ����,ͳһ��ʽ����.
	 * 
	 * @param fileName	�ı�����ļ��ļ���,����·��
	 * @return	
	 * @throws IOException
	 */
    private String[] BHProcess(String fileName, int trial) throws Exception {
        int[] width_total = null;
        if (trial == 1) {
            width_total = new int[] { 10, 5, 17, 10, 8, 3, 8, 7, 8, 2, 1, 3, 2, 2, 2, 2, 6, 2, 3, 2, 5, 4, 1, 2, 2, 1 };
        } else if (trial == 2) {
            width_total = new int[] { 10, 5, 17, 10, 8, 3, 8, 7, 8, 2, 2, 3, 2, 2, 2, 2, 6, 2, 3, 2, 5, 4, 1, 2, 2, 1 };
        }
        int[] width_1 = { 10, 5, 17, 10, 8, 6, 9, 7, 9, 3, 4, 6, 3 };
        int[] width_2 = { 2, 10, 10, 13, 4, 6, 5, 11, 9, 3, 6, 8, 7 };
        return BHProcessLines(fileName, width_total, width_1, width_2);
    }

    public String[] BHProcessLines(String fileName, int[] width_total, int[] width_1, int[] width_2) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        ArrayList<String> lines = new ArrayList<String>();
        int blankAmount = 1;
        try {
            while ((line = reader.readLine()) != null) {
                int ID = -1;
                if ((ID = getID(line)) != -1) {
                    StringBuilder sb = new StringBuilder(line);
                    if (ID <= 985) {
                        blankAmount = 1;
                    } else {
                        blankAmount = 3;
                    }
                    removeID(sb, blankAmount);
                    if (ID <= 985) {
                        ReplaceBlanks(sb, width_total, blankAmount);
                        lines.add(sb.toString());
                    } else if (ID > lines.size()) {
                        ReplaceBlanks(sb, width_1, blankAmount);
                        lines.add(sb.toString());
                    } else {
                        ReplaceBlanks(sb, width_2, blankAmount);
                        lines.set(ID - 1, lines.get(ID - 1) + sb.toString());
                    }
                } else {
                }
            }
        } finally {
        }
        String[] ret = new String[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            ret[i] = lines.get(i);
        }
        return ret;
    }

    /**
	 * ������"GPS"���͵��ı�����ļ�.
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
    private String[] GPSProcess(String fileName, int trial) throws Exception {
        int[] width_total = null;
        if (trial == 1) {
            width_total = new int[] { 10, 5, 17, 10, 8, 3, 8, 7, 8, 2, 1, 3, 2, 2, 2, 2, 6, 2, 3, 2, 5, 4, 1, 2, 2, 1 };
        } else if (trial == 2) {
            width_total = new int[] { 10, 5, 17, 10, 8, 3, 8, 7, 8, 2, 1, 3, 2, 2, 2, 2, 6, 2, 3, 2, 4, 4, 1, 2, 2, 1 };
        } else if (trial == 3) {
            width_total = new int[] { 10, 5, 17, 10, 8, 3, 8, 7, 8, 2, 2, 3, 2, 2, 2, 2, 6, 2, 3, 2, 5, 4, 1, 2, 2, 1 };
        }
        int[] width_1 = { 10, 5, 17, 10, 8, 6, 9, 7, 9, 3, 4, 6, 3 };
        int[] width_2 = { 2, 10, 10, 13, 4, 6, 5, 11, 9, 3, 6, 8, 7 };
        return BHProcessLines(fileName, width_total, width_1, width_2);
    }

    /**
	 * ������"ARGOS"���͵��ı�����ļ�.
	 * @param fileName
	 * @return
	 */
    private String[] ArgosProcess(String fileName, int trial) throws Exception {
        int[] widths = { 10, 6, 17, 10, 8, 3, 8, 7, 8, 2, 1, 3, 3, 10, 9, 10 };
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line;
        ArrayList<String> lines = new ArrayList<String>();
        int blankAmount = 1;
        try {
            while ((line = reader.readLine()) != null) {
                int ID = -1;
                if ((ID = getID(line)) != -1) {
                    StringBuilder sb = new StringBuilder(line);
                    blankAmount = 1;
                    removeID(sb, blankAmount);
                    if (ID <= 985) {
                        ReplaceBlanks(sb, widths, blankAmount);
                        lines.add(sb.toString());
                    } else {
                        logger.error("The amount of the lines of the file " + fileName + " exceeds 985.");
                        throw new IOException("OVER FLOW Exception 'exceed 985'.");
                    }
                } else {
                }
            }
        } finally {
        }
        String[] ret = new String[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            ret[i] = lines.get(i);
        }
        return ret;
    }

    /**
	 * ɾ���ÿһ�п�ͷ��ID..
	 * 
	 * @param sb
	 * @param blankAmount
	 * 
	 */
    public void removeID(StringBuilder sb, int blankAmount) {
        Integer id = getID(sb.toString());
        String ss = sb.toString().trim();
        int index = sb.indexOf(id + " ");
        sb.delete(0, index + id.toString().length() + blankAmount);
    }

    public int getID(String line) {
        Pattern pattern = Pattern.compile("^\\s*\\d+\\s+.*");
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
            int index = -1;
            String ss = line.trim();
            index = ss.indexOf(" ");
            String strNum = ss.substring(0, index);
            return Integer.parseInt(strNum);
        }
        return -1;
    }

    /**
	 * ȥ�����еĿո�,���ָ��λ�õ����Ϊ������"."���.
	 * 
	 * @param sb
	 * @param width
	 * @param blankAmount
	 * 
	 */
    public void ReplaceBlanks(StringBuilder sb, int[] width, int blankAmount) throws Exception {
        int index = 0;
        int nextIndex = 0;
        for (int i = 0; i < width.length; i++) {
            nextIndex = index + width[i];
            if (sb.substring(index, nextIndex).trim().equals("")) {
                int mid = (index + nextIndex) / 2;
                sb.replace(mid, mid + 1, ".");
            }
            index = nextIndex + blankAmount;
        }
    }

    /**
	 * ��ȡ�ո���Ŀ.
	 * @deprecated δ���.
	 * @return
	 */
    private int getBlankAmount() {
        return -1;
    }
}
