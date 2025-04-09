package net.sf.chineseutils.mapping;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Traditional Chinese to Simplified Chinese glossary mapping.
 * 
 * @author <a href="mailto:luhuiguo@gmail.com">Lu,Huiguo</a>
 * @version $Id: TradToSimpGlossaryMapping.java 4 2006-08-03 10:29:26Z fantasy4u $
 */
public class TradToSimpGlossaryMapping {

    private static final String MAPING_FILE = "TradToSimpGlossaryMapping.txt";

    private static Logger logger = LoggerFactory.getLogger(TradToSimpGlossaryMapping.class);

    private static final char SEPARATOR_CHAR = '=';

    /**
	 * ȥ���ظ��������Ժ�Ĵ���?
	 */
    public static String[] glossarys;

    /**
	 * ��Ӧ�ڴ�����ת������?
	 */
    public static String[] glossaryMappings;

    /**
	 * ��������
	 * <p>
	 * ���ַ���ͬ�Ĵ���Ϊһ����飬�Ը����ַ�Ϊ�� ��
	 * 
	 * <pre>
	 * <code>
	 * ��������
	 * </code>
	 * <code>
	 * �˹�����
	 * </code>
	 *  ����[
	 * <code>
	 * ��</code>
	 * ]��Ĵ���
	 * </pre>
	 * 
	 * </p>
	 */
    public static char[] blocks;

    /**
	 * ÿ������ڴ���������ʼλ�á�
	 */
    public static int[] blockStarts;

    /**
	 * <p>
	 * ȱʡ�Ĵ���������
	 * </p>
	 */
    public TradToSimpGlossaryMapping() {
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
            logger.debug("Initializing TC to SC glossary mapping table...");
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(MAPING_FILE), "GBK"));
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
            glossarys = new String[sz];
            glossaryMappings = new String[sz];
            char[] bufBlocks = new char[sz];
            int[] bufBlockStarts = new int[sz];
            Iterator it = map.entrySet().iterator();
            int i = 0;
            int j = 0;
            char tmp = SEPARATOR_CHAR;
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry) it.next();
                glossarys[i] = (String) e.getKey();
                glossaryMappings[i] = (String) e.getValue();
                char ch = ((String) e.getKey()).charAt(0);
                if (ch != tmp) {
                    bufBlocks[j] = ch;
                    bufBlockStarts[j] = i;
                    tmp = ch;
                    j++;
                }
                i++;
            }
            blocks = new char[j];
            blockStarts = new int[j];
            System.arraycopy(bufBlocks, 0, blocks, 0, j);
            System.arraycopy(bufBlockStarts, 0, blockStarts, 0, j);
        } catch (Exception ex) {
            if (logger.isErrorEnabled()) {
                logger.error("An error has occurred when initializing TC to SC glossary mapping table: " + ex.getMessage());
            }
            if (logger.isDebugEnabled()) {
                ex.printStackTrace();
            }
            return false;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("TC to SC glossary mapping table Initialized.");
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
        if (blocks == null) {
            return -1;
        }
        int l = blocks.length - 1;
        for (int i = 0; i <= l; ) {
            int p = (l + i) / 2;
            int t = ch - blocks[p];
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
        if (blockStarts == null || idx < 0 || idx >= blockStarts.length) {
            return -1;
        }
        return blockStarts[idx];
    }

    /**
	 * �õ�ĳ�����Ľ���λ�á�
	 * 
	 * @param idx
	 *            ����š�
	 * @return �����Ŀ�ʼλ������һ�������ڸ���Ĵ����λ��
	 */
    public static int blockEnd(int idx) {
        if (blockStarts == null || idx < 0 || idx >= blockStarts.length) {
            return -1;
        }
        if (idx == blockStarts.length - 1) {
            return glossarys.length;
        }
        return blockStarts[idx + 1];
    }
}
