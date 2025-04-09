package net.sf.chineseutils.mapping;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simplified Chinese to Traditional Chinese Orthographic and Lexemic Mapping.
 * 
 * @author <a href="mailto:luhuiguo@gmail.com">Lu,Huiguo</a>
 * @version $Id: STLexemicMapping.java 50 2006-08-31 15:02:13Z fantasy4u $
 */
public class STLexemicMapping {

    private static Log logger = LogFactory.getLog(STLexemicMapping.class);

    private static final String MAPING_FILE = "STLexemicMapping.dat";

    private static final char SEPARATOR_CHAR = '=';

    /**
	 * ȥ���ظ��������Ժ�Ĵ���?
	 */
    public static String[] SC_TO_TC_LEXEME;

    /**
	 * ��Ӧ�ڴ�����ת������?
	 */
    public static String[] SC_TO_TC_LEXEME_MAP;

    /**
	 * �������?���ַ���ͬ�Ĵ���Ϊһ����飬�Ը����ַ�Ϊ�� �磺���������ơ������˹����ܡ����ǡ��ˡ���Ĵ��顣
	 */
    public static char[] SC_TO_TC_LEXEME_BLOCK;

    /**
	 * ÿ������ڴ���������ʼλ�á�
	 */
    public static int[] SC_TO_TC_LEXEME_BLOCK_START;

    /**
	 * <p>
	 * ȱʡ�Ĵ���������
	 * </p>
	 */
    public STLexemicMapping() {
    }

    /**
	 * <p>
	 * ��ʼ������->�������ӳ��?
	 * </p>
	 * 
	 * @return <code>true</code>��ʼ���ɹ���<code>false</code>��ʼ��ʧ�ܡ�
	 */
    public boolean init() {
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing SC to TC Orthographic and Lexemic Mapping Table...");
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(MAPING_FILE), "UTF-16"));
            TreeMap<String, String> map = new TreeMap<String, String>();
            String line = br.readLine();
            do {
                String[] ss = line.split(String.valueOf(SEPARATOR_CHAR));
                if (2 == ss.length) {
                    map.put(ss[0], ss[1]);
                }
                line = br.readLine();
            } while (line != null);
            br.close();
            int sz = map.size();
            SC_TO_TC_LEXEME = new String[sz];
            SC_TO_TC_LEXEME_MAP = new String[sz];
            char[] bufBlocks = new char[sz];
            int[] bufBlockStarts = new int[sz];
            Iterator it = map.entrySet().iterator();
            int i = 0;
            int j = 0;
            char tmp = SEPARATOR_CHAR;
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry) it.next();
                SC_TO_TC_LEXEME[i] = (String) e.getKey();
                SC_TO_TC_LEXEME_MAP[i] = (String) e.getValue();
                char ch = ((String) e.getKey()).charAt(0);
                if (ch != tmp) {
                    bufBlocks[j] = ch;
                    bufBlockStarts[j] = i;
                    tmp = ch;
                    j++;
                }
                i++;
            }
            SC_TO_TC_LEXEME_BLOCK = new char[j];
            SC_TO_TC_LEXEME_BLOCK_START = new int[j];
            System.arraycopy(bufBlocks, 0, SC_TO_TC_LEXEME_BLOCK, 0, j);
            System.arraycopy(bufBlockStarts, 0, SC_TO_TC_LEXEME_BLOCK_START, 0, j);
        } catch (Exception ex) {
            if (logger.isErrorEnabled()) {
                logger.error("An error has occurred when initializing SC to TC Orthographic and Lexemic Mapping Table: " + ex.getMessage());
            }
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
            return false;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("SC to TC Orthographic and Lexemic Mapping Table Initialized.");
        }
        return true;
    }

    /**
	 * <p>
	 * ������ĳ�ַ�ͷ�Ĵ������λ�á�
	 * </p>
	 * <p>
	 * ���������������,�����۰���ҡ�
	 * </p>
	 * 
	 * @param ch
	 *            Ҫ���ҵ��ַ�
	 * @return �ڴ��������λ�ã�<code>-1</code>��ʾû�ҵ���
	 */
    public static int findBlock(char ch) {
        if (SC_TO_TC_LEXEME_BLOCK == null) {
            return -1;
        }
        int l = SC_TO_TC_LEXEME_BLOCK.length - 1;
        for (int i = 0; i <= l; ) {
            int p = (l + i) / 2;
            int t = ch - SC_TO_TC_LEXEME_BLOCK[p];
            if (t == 0) {
                return p;
            }
            if (t < 0) {
                l = --p;
            } else {
                i = ++p;
            }
        }
        return -1;
    }

    /**
	 * �õ�ĳ�����Ŀ�ʼλ�á�
	 * 
	 * @param idx
	 *            ����š�
	 * @return ���ڸ���Ĵ����ڴ����Ŀ�ʼλ��
	 */
    public static int blockStart(int idx) {
        if (SC_TO_TC_LEXEME_BLOCK_START == null || idx < 0 || idx >= SC_TO_TC_LEXEME_BLOCK_START.length) {
            return -1;
        }
        return SC_TO_TC_LEXEME_BLOCK_START[idx];
    }

    /**
	 * �õ�ĳ�����Ľ���λ�á�
	 * 
	 * @param idx
	 *            ����š�
	 * @return �����Ŀ�ʼλ������һ�������ڸ���Ĵ����λ��
	 */
    public static int blockEnd(int idx) {
        if (SC_TO_TC_LEXEME_BLOCK_START == null || idx < 0 || idx >= SC_TO_TC_LEXEME_BLOCK_START.length) {
            return -1;
        }
        if (idx == SC_TO_TC_LEXEME_BLOCK_START.length - 1) {
            return SC_TO_TC_LEXEME.length;
        }
        return SC_TO_TC_LEXEME_BLOCK_START[idx + 1];
    }
}
