package net.sf.chineseutils.mapping;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Traditional Chinese to Simplified Chinese glossary mapping.
 * 
 * @author <a href="mailto:luhuiguo@gmail.com">Lu,Huiguo</a>
 * @version $Id: TSLexemicMapping.java 50 2006-08-31 15:02:13Z fantasy4u $
 */
public class TSLexemicMapping {

    private static final String MAPING_FILE = "TSLexemicMapping.dat";

    private static Log logger = LogFactory.getLog(TSLexemicMapping.class);

    private static final char SEPARATOR_CHAR = '=';

    /**
	 * ȥ���ظ��������Ժ�Ĵ���?
	 */
    public static String[] TC_TO_SC_LEXEME;

    /**
	 * ��Ӧ�ڴ�����ת������?
	 */
    public static String[] TC_TO_SC_LEXEME_MAP;

    /**
	 * �������?
	 */
    public static char[] TC_TO_SC_LEXEME_BLOCK;

    /**
	 * ÿ������ڴ���������ʼλ�á�
	 */
    public static int[] TC_TO_SC_LEXEME_BLOCK_START;

    /**
	 * <p>
	 * ȱʡ�Ĵ���������
	 * </p>
	 */
    public TSLexemicMapping() {
    }

    /**
	 * <p>
	 * ��ʼ������->�������ӳ��?
	 * </p>
	 * 
	 * @return <code>true</code>��ʼ���ɹ���<code>false</code>��ʼ��ʧ��
	 */
    public boolean init() {
        if (logger.isDebugEnabled()) {
            logger.debug("Initializing TC to SC Orthographic and Lexemic Mapping Table...");
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
            TC_TO_SC_LEXEME = new String[sz];
            TC_TO_SC_LEXEME_MAP = new String[sz];
            char[] bufBlocks = new char[sz];
            int[] bufBlockStarts = new int[sz];
            Iterator it = map.entrySet().iterator();
            int i = 0;
            int j = 0;
            char tmp = SEPARATOR_CHAR;
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry) it.next();
                TC_TO_SC_LEXEME[i] = (String) e.getKey();
                TC_TO_SC_LEXEME_MAP[i] = (String) e.getValue();
                char ch = ((String) e.getKey()).charAt(0);
                if (ch != tmp) {
                    bufBlocks[j] = ch;
                    bufBlockStarts[j] = i;
                    tmp = ch;
                    j++;
                }
                i++;
            }
            TC_TO_SC_LEXEME_BLOCK = new char[j];
            TC_TO_SC_LEXEME_BLOCK_START = new int[j];
            System.arraycopy(bufBlocks, 0, TC_TO_SC_LEXEME_BLOCK, 0, j);
            System.arraycopy(bufBlockStarts, 0, TC_TO_SC_LEXEME_BLOCK_START, 0, j);
        } catch (Exception ex) {
            if (logger.isErrorEnabled()) {
                logger.error("An error has occurred when initializing TC to SC Orthographic and Lexemic Mapping Table: " + ex.getMessage());
            }
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
            return false;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("TC to SC Orthographic and Lexemic Mapping Table Initialized.");
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
        if (TC_TO_SC_LEXEME_BLOCK == null) {
            return -1;
        }
        int l = TC_TO_SC_LEXEME_BLOCK.length - 1;
        for (int i = 0; i <= l; ) {
            int p = (l + i) / 2;
            int t = ch - TC_TO_SC_LEXEME_BLOCK[p];
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
        if (TC_TO_SC_LEXEME_BLOCK_START == null || idx < 0 || idx >= TC_TO_SC_LEXEME_BLOCK_START.length) {
            return -1;
        }
        return TC_TO_SC_LEXEME_BLOCK_START[idx];
    }

    /**
	 * �õ�ĳ�����Ľ���λ�á�
	 * 
	 * @param idx
	 *            ����š�
	 * @return �����Ŀ�ʼλ������һ�������ڸ���Ĵ����λ��
	 */
    public static int blockEnd(int idx) {
        if (TC_TO_SC_LEXEME_BLOCK_START == null || idx < 0 || idx >= TC_TO_SC_LEXEME_BLOCK_START.length) {
            return -1;
        }
        if (idx == TC_TO_SC_LEXEME_BLOCK_START.length - 1) {
            return TC_TO_SC_LEXEME.length;
        }
        return TC_TO_SC_LEXEME_BLOCK_START[idx + 1];
    }
}
