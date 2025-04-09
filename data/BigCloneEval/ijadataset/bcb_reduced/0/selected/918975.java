package org.ictclas4j.utility;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import org.ictclas4j.bean.Dictionary;
import org.ictclas4j.bean.PersonName;
import org.ictclas4j.segment.PosTagger;

public class Utility {

    public static final int CC_NUM = 6768;

    public static final int WORD_MAXLENGTH = 100;

    public static final int WT_DELIMITER = 0;

    public static final int WT_CHINESE = 1;

    public static final int WT_OTHER = 2;

    public static final int CT_SENTENCE_BEGIN = 1;

    public static final int CT_SENTENCE_END = 4;

    public static final int CT_SINGLE = 5;

    public static final int CT_DELIMITER = CT_SINGLE + 1;

    public static final int CT_CHINESE = CT_SINGLE + 2;

    public static final int CT_LETTER = CT_SINGLE + 3;

    public static final int CT_NUM = CT_SINGLE + 4;

    public static final int CT_INDEX = CT_SINGLE + 5;

    public static final int CT_OTHER = CT_SINGLE + 12;

    public static final int MAX_WORDS = 650;

    public static final int MAX_SEGMENT_NUM = 10;

    public static final String POSTFIX_SINGLE = "�Ӱ���ǳش嵥�����̵궴�ɶӷ��帮�Ը۸󹬹���źӺ������������ǽ־����ӿڿ�����¥·������Ū����������������Ȫ��ɽʡ��ˮ����̨̲̳����ͤ��������ϪϿ������������ҤӪ����԰ԷԺբկվ����ׯ�������";

    public static final String[] POSTFIX_MUTIPLE = { "�뵺", "��ԭ", "����", "���", "�󹫹�", "����", "����", "�۹�", "�ɲ�", "�ۿ�", "���ٹ�·", "��ԭ", "��·", "��԰", "���͹�", "�ȵ�", "�㳡", "���", "��Ͽ", "��ͬ", "��", "����", "����", "�ֵ�", "�ڰ�", "��ͷ", "ú��", "����", "ũ��", "���", "ƽԭ", "����", "Ⱥ��", "ɳĮ", "ɳ��", "ɽ��", "ɽ��", "ˮ��", "���", "����", "��·", "�´�", "ѩ��", "�γ�", "�κ�", "�泡", "ֱϽ��", "������", "������", "������", "" };

    public static final String TRANS_ENGLISH = "�������������������°İʰŰͰװݰ������������ȱϱ˱𲨲��������������Ųɲֲ��񳹴��Ĵȴδ������������µõĵǵϵҵٵ۶����Ŷض������������Ʒҷѷ�򸣸������ǸɸԸ��������ŹϹ��������������ϺӺպ���������Ӽּ��ܽ𾩾þӾ���������¿ƿɿ˿Ͽ����������������������������������������������������������¡¬²³·��������������������éï÷����������������ĦĪīĬķľ������������������������ŦŬŵŷ��������������Ƥƽ��������ǡǿ��������Ȫ��������������������ɣɪɭɯɳɽ������ʥʩʫʯʲʷʿ��˹˾˿��������̩̹����������͡ͼ������������������Τάκ��������������������ϣϲ������Ъл������������ҢҶ��������������ӢӺ����Լ������ղ������������׿������٤�������üν�����������Ľ����������������ɺ����ѷ��������ܽ���������������";

    public static final String TRANS_RUSSIAN = "�������°ͱȱ˲�����Ĵ�µö��Ŷ���Ǹ����Ӽ�ݽ𿨿ƿɿ˿���������������������¬³������÷����ķ������ŵ������������������ɫɽ��ʲ˹����̹������ά������ϣл��ҮҶ�������������ǵٸ��ջ������������������������������ɣɳ��̩ͼ������׿��";

    public static final String TRANS_JAPANESE = "���°˰װٰ�������ȱ��������ʲ˲ֲ�سന�����δ������µص�ɶ��縣�Ը߹����Źȹع���úƺͺϺӺں���󻧻Ļ漪�ͼѼӼ�����������þƾտ����ɿ˿�����������������������������¡¹������������ľ��������������Ƭƽ����ǧǰǳ����������������Ȫ������������ɭɴɼɽ��������ʥʯʵʸ������ˮ˳˾��̩��������������βδ����������ϸ������СТ����������������������ңҰҲҶһ����������ӣ��������������ԨԪԫԭԶ����������������լ����������ֲ֦֪֮��������������׵��������ܥݶ��޹������";

    public static final int TT_ENGLISH = 0;

    public static final int TT_RUSSIAN = 1;

    public static final int TT_JAPANESE = 2;

    public static final String SEPERATOR_C_SENTENCE = "������������";

    public static final String SEPERATOR_C_SUB_SENTENCE = "����������������";

    public static final String SEPERATOR_E_SENTENCE = "!?:;";

    public static final String SEPERATOR_E_SUB_SENTENCE = ",()\"'";

    public static final String SEPERATOR_LINK = "\n\r ��";

    public static final String SENTENCE_BEGIN = "ʼ##ʼ";

    public static final String SENTENCE_END = "ĩ##ĩ";

    public static final String WORD_SEGMENTER = "@";

    public static final int MAX_WORDS_PER_SENTENCE = 120;

    public static final int MAX_UNKNOWN_PER_SENTENCE = 200;

    public static final int MAX_POS_PER_WORD = 20;

    public static final int LITTLE_FREQUENCY = 6;

    public enum TAG_TYPE {

        TT_NORMAL, TT_PERSON, TT_PLACE, TT_TRANS_PERSON
    }

    ;

    public static final int MAX_FREQUENCE = 2079997;

    public static final int MAX_SENTENCE_LEN = 2000;

    public static final double INFINITE_VALUE = 10000.00;

    public static final double SMOOTH_PARAM = 0.1;

    public static final String UNKNOWN_PERSON = "δ##��";

    public static final String UNKNOWN_SPACE = "δ##��";

    public static final String UNKNOWN_NUM = "δ##��";

    public static final String UNKNOWN_TIME = "δ##ʱ";

    public static final String UNKNOWN_LETTER = "δ##��";

    public static boolean gbGenerate(String fileName) {
        File file;
        int i, j;
        file = new File(fileName);
        try {
            PrintWriter out = new PrintWriter(new FileOutputStream(file));
            if (!file.canWrite()) return false;
            for (i = 161; i < 255; i++) for (j = 161; j < 255; j++) out.println("" + i + j + "," + i + "," + j);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    /***************************************************************************
	 * 
	 * Func Name : CC_Generate
	 * 
	 * Description: Generate the Chinese Char List file
	 * 
	 * 
	 * Parameters : sFilename: the file name for the output CC List
	 * 
	 * Returns : public static boolean Author : Kevin Zhang History : 1.create
	 * 2002-1-8
	 **************************************************************************/
    public static boolean CC_Generate(String fileName) {
        File file;
        int i, j;
        file = new File(fileName);
        try {
            PrintWriter out = new PrintWriter(new FileOutputStream(file));
            for (i = 176; i < 255; i++) for (j = 161; j < 255; j++) out.println("" + i + j + "," + i + "," + j);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    /***************************************************************************
	 * 
	 * Func Name : CC_Find
	 * 
	 * Description: Find a Chinese sub-string in the Chinese String
	 * 
	 * 
	 * Parameters : string:Null-terminated string to search
	 * 
	 * strCharSet:Null-terminated string to search for
	 * 
	 * Returns : String Author : Kevin Zhang History : 1.create 2002-1-8
	 **************************************************************************/
    public static boolean CC_Find(final byte[] string, final byte[] strCharSet) {
        if (string != null && strCharSet != null) {
            int index = strstr(string, strCharSet);
            if (index != -1 && (index % 2 == 1)) {
                return false;
            }
        }
        return true;
    }

    /***************************************************************************
	 * 
	 * Func Name : charType
	 * 
	 * Description: Judge the type of sChar or (sChar,sChar+1)
	 * 
	 * 
	 * Parameters : sFilename: the file name for the output CC List
	 * 
	 * Returns : int : the type of char Author : Kevin Zhang History : 1.create
	 * 2002-1-8
	 **************************************************************************/
    public static int charType(String str) {
        if (str != null && str.length() > 0) {
            byte[] b = str.getBytes();
            byte b1 = b[0];
            byte b2 = b.length > 1 ? b[1] : 0;
            if (getUnsigned(b1) < 128) {
                if ("\"!,.?()[]{}+=".indexOf((char) b1) != -1) return CT_DELIMITER;
                return CT_SINGLE;
            } else if (getUnsigned(b1) == 162) return CT_INDEX; else if (getUnsigned(b1) == 163 && getUnsigned(b2) > 175 && getUnsigned(b2) < 186) return CT_NUM; else if (getUnsigned(b1) == 163 && (getUnsigned(b2) >= 193 && getUnsigned(b2) <= 218 || getUnsigned(b2) >= 225 && getUnsigned(b2) <= 250)) return CT_LETTER; else if (getUnsigned(b1) == 161 || getUnsigned(b1) == 163) return CT_DELIMITER; else if (getUnsigned(b1) >= 176 && getUnsigned(b1) <= 247) return CT_CHINESE;
        }
        return CT_OTHER;
    }

    /***************************************************************************
	 * 
	 * Func Name : GetCCPrefix
	 * 
	 * Description: Get the max Prefix string made up of Chinese Char
	 * 
	 * 
	 * Parameters : sSentence: the original sentence which includes Chinese or
	 * Non-Chinese char
	 * 
	 * Returns : the end of the sub-sentence Author : Kevin Zhang History :
	 * 1.create 2002-1-8
	 **************************************************************************/
    public static int getCCPrefix(byte[] sSentence) {
        int nLen = sSentence.length;
        int nCurPos = 0;
        while (nCurPos < nLen && getUnsigned(sSentence[nCurPos]) > 175 && getUnsigned(sSentence[nCurPos]) < 248) {
            nCurPos += 2;
        }
        return nCurPos;
    }

    /***************************************************************************
	 * 
	 * Func Name : IsAllSingleByte
	 * 
	 * Description: Judge the string is all made up of Single Byte Char
	 * 
	 * 
	 * Parameters : sSentence: the original sentence which includes Chinese or
	 * Non-Chinese char
	 * 
	 * Returns : the end of the sub-sentence Author : Kevin Zhang History :
	 * 1.create 2002-1-24
	 **************************************************************************/
    public static boolean isAllChinese(String str) {
        if (str != null) {
            String temp = str + " ";
            for (int i = 0; i < str.length(); i++) {
                byte[] b = temp.substring(i, i + 1).getBytes();
                if (b.length == 2) {
                    if (!(getUnsigned(b[0]) < 248 && getUnsigned(b[0]) > 175) || !(getUnsigned(b[0]) < 253 && getUnsigned(b[0]) > 160)) return false;
                }
            }
            return true;
        }
        return false;
    }

    /***************************************************************************
	 * 
	 * Func Name : IsAllNonChinese
	 * 
	 * Description: Judge the string is all made up of Single Byte Char
	 * 
	 * 
	 * Parameters : sSentence: the original sentence which includes Chinese or
	 * Non-Chinese char
	 * 
	 * Returns : the end of the sub-sentence Author : Kevin Zhang History :
	 * 1.create 2002-1-24
	 **************************************************************************/
    public static boolean isAllNonChinese(byte[] sString) {
        int nLen = sString.length;
        int i = 0;
        while (i < nLen) {
            if (getUnsigned(sString[i]) < 248 && getUnsigned(sString[i]) > 175) return false;
            if (sString[i] < 0) i += 2; else i += 1;
        }
        return true;
    }

    /***************************************************************************
	 * 
	 * Func Name : IsAllSingleByte
	 * 
	 * Description: Judge the string is all made up of Single Byte Char
	 * 
	 * 
	 * Parameters : sSentence: the original sentence which includes Chinese or
	 * Non-Chinese char
	 * 
	 * Returns : the end of the sub-sentence Author : Kevin Zhang History :
	 * 1.create 2002-1-24
	 **************************************************************************/
    public static boolean isAllSingleByte(String str) {
        if (str != null) {
            int len = str.length();
            int i = 0;
            byte[] b = str.getBytes();
            while (i < len && b[i] < 128) {
                i++;
            }
            if (i < len) return false;
            return true;
        }
        return false;
    }

    /***************************************************************************
	 * 
	 * Func Name : IsAllNum
	 * 
	 * Description: Judge the string is all made up of Num Char
	 * 
	 * 
	 * Parameters : sSentence: the original sentence which includes Chinese or
	 * Non-Chinese char
	 * 
	 * Returns : the end of the sub-sentence Author : Kevin Zhang History :
	 * 1.create 2002-1-24
	 **************************************************************************/
    public static boolean isAllNum(String str) {
        if (str != null) {
            int i = 0;
            String temp = str + " ";
            if ("��+��-��".indexOf(temp.substring(0, 1)) != -1) i++;
            while (i < str.length() && "��������������������".indexOf(str.substring(i, i + 1)) != -1) i++;
            if (i < str.length()) {
                String s = str.substring(i, i + 1);
                if ("�á�����".indexOf(s) != -1 || ".".equals(s) || "/".equals(s)) {
                    i++;
                    while (i + 1 < str.length() && "��������������������".indexOf(str.substring(i + 1, i + 2)) != -1) i++;
                }
            }
            if (i >= str.length()) return true;
            while (i < str.length() && GFString.cint(str.substring(i, i + 1)) >= 0 && GFString.cint(str.substring(i, i + 1)) <= 9) i++;
            if (i < str.length()) {
                String s = str.substring(i, i + 1);
                if ("�á�����".indexOf(s) != -1 || ".".equals(s) || "/".equals(s)) {
                    i++;
                    while (i + 1 < str.length() && "0123456789".indexOf(str.substring(i + 1, i + 2)) != -1) i++;
                }
            }
            if (i < str.length()) {
                if ("��ǧ���ڰ�Ǫ����".indexOf(str.substring(i, i + 1)) == -1 && !"%".equals(str.substring(i, i + 1))) i--;
            }
            if (i >= str.length()) return true;
        }
        return false;
    }

    /***************************************************************************
	 * 
	 * Func Name : IsAllIndex
	 * 
	 * Description: Judge the string is all made up of Index Num Char
	 * 
	 * 
	 * Parameters : sSentence: the original sentence which includes Chinese or
	 * Non-Chinese char
	 * 
	 * Returns : the end of the sub-sentence Author : Kevin Zhang History :
	 * 1.create 2002-1-24
	 **************************************************************************/
    public static boolean isAllIndex(byte[] sString) {
        int nLen = sString.length;
        int i = 0;
        while (i < nLen - 1 && getUnsigned(sString[i]) == 162) {
            i += 2;
        }
        if (i >= nLen) return true;
        while (i < nLen && (sString[i] > 'A' - 1 && sString[i] < 'Z' + 1) || (sString[i] > 'a' - 1 && sString[i] < 'z' + 1)) {
            i += 1;
        }
        if (i < nLen) return false;
        return true;
    }

    /***************************************************************************
	 * 
	 * Func Name : IsAllLetter
	 * 
	 * Description: Judge the string is all made up of Letter Char
	 * 
	 * 
	 * Parameters : sSentence: the original sentence which includes Chinese or
	 * Non-Chinese char
	 * 
	 * Returns : the end of the sub-sentence Author : Kevin Zhang History :
	 * 1.create 2002-1-24
	 **************************************************************************/
    public static boolean isAllLetter(String str) {
        int i = 0;
        if (str != null) {
            int nLen = str.length();
            byte[] b = str.getBytes();
            while (i < nLen - 1 && getUnsigned(b[i]) == 163 && ((getUnsigned(b[i + 1]) >= 193 && getUnsigned(b[i + 1]) <= 218) || (getUnsigned(b[i + 1]) >= 225 && getUnsigned(b[i + 1]) <= 250))) {
                i += 2;
            }
            if (i < nLen) return false;
            return true;
        }
        return false;
    }

    /***************************************************************************
	 * 
	 * Func Name : IsAllDelimiter
	 * 
	 * Description: Judge the string is all made up of Delimiter
	 * 
	 * 
	 * Parameters : sSentence: the original sentence which includes Chinese or
	 * Non-Chinese char
	 * 
	 * Returns : the end of the sub-sentence Author : Kevin Zhang History :
	 * 1.create 2002-1-24
	 **************************************************************************/
    public static boolean isAllDelimiter(byte[] sString) {
        int nLen = sString.length;
        int i = 0;
        while (i < nLen - 1 && (getUnsigned(sString[i]) == 161 || getUnsigned(sString[i]) == 163)) {
            i += 2;
        }
        if (i < nLen) return false;
        return true;
    }

    /***************************************************************************
	 * 
	 * Func Name : BinarySearch
	 * 
	 * Description: Lookup the index of nVal in the table nTable which length is
	 * nTableLen
	 * 
	 * Parameters : nPOS: the POS value
	 * 
	 * Returns : the index value Author : Kevin Zhang History : 1.create
	 * 2002-1-25
	 **************************************************************************/
    public static int binarySearch(int val, int[] table) {
        if (table != null) {
            int len = table.length;
            int start = 0, end = len - 1, mid = (start + end) / 2;
            while (start <= end) {
                if (table[mid] == val) {
                    return mid;
                } else if (table[mid] < val) {
                    start = mid + 1;
                } else {
                    end = mid - 1;
                }
                mid = (start + end) / 2;
            }
        }
        return -1;
    }

    /***************************************************************************
	 * 
	 * Func Name : IsForeign
	 * 
	 * Description: Decide whether the word is not a Non-fereign word
	 * 
	 * Parameters : sWord: the word
	 * 
	 * Returns : the index value Author : Kevin Zhang History : 1.create
	 * 2002-1-26
	 **************************************************************************/
    public static boolean isForeign(String word) {
        if (word != null) {
            int foreignCount = getForeignCharCount(word);
            int charCount = word.length();
            if (charCount > 2 || foreignCount >= 1 * charCount / 2) return true;
        }
        return false;
    }

    /***************************************************************************
	 * 
	 * Func Name : IsAllForeign
	 * 
	 * Description: Decide whether the word is not a Non-fereign word
	 * 
	 * Parameters : sWord: the word
	 * 
	 * Returns : the index value Author : Kevin Zhang History : 1.create
	 * 2002-3-25
	 **************************************************************************/
    public static boolean isAllForeign(String sWord) {
        int nForeignCount = getForeignCharCount(sWord);
        if (2 * nForeignCount == sWord.length()) return true;
        return false;
    }

    /***************************************************************************
	 * 
	 * Func Name : IsForeign
	 * 
	 * Description: Decide whether the word is Chinese Num word
	 * 
	 * Parameters : sWord: the word
	 * 
	 * Returns : the index value Author : Kevin Zhang History : 1.create
	 * 2002-1-26
	 **************************************************************************/
    public static boolean isAllChineseNum(String word) {
        String chineseNum = "���һ�������������߰˾�ʮإ��ǧ����Ҽ��������½��ƾ�ʰ��Ǫ�á�������";
        String prefix = "������ϳ�";
        if (word != null) {
            String temp = word + " ";
            for (int i = 0; i < word.length(); i++) {
                if (temp.indexOf("��֮", i) != -1) {
                    i += 2;
                    continue;
                }
                String tchar = temp.substring(i, i + 1);
                if (chineseNum.indexOf(tchar) == -1 && (i != 0 || prefix.indexOf(tchar) == -1)) return false;
            }
            return true;
        }
        return false;
    }

    /***************************************************************************
	 * 
	 * Func Name : GetForeignCharCount
	 * 
	 * Description:
	 * 
	 * Parameters : sWord: the word
	 * 
	 * Returns : the index value Author : Kevin Zhang History : 1.create
	 * 2002-4-4 2.Modify 2002-5-21
	 **************************************************************************/
    public static int getForeignCharCount(String sWord) {
        int nForeignCount, nCount;
        nForeignCount = getCharCount(TRANS_ENGLISH, sWord);
        nCount = getCharCount(TRANS_JAPANESE, sWord);
        if (nForeignCount <= nCount) nForeignCount = nCount;
        nCount = getCharCount(TRANS_RUSSIAN, sWord);
        if (nForeignCount <= nCount) nForeignCount = nCount;
        return nForeignCount;
    }

    /**
	 * �õ��ַ���ַ����ַ��г��ֵĴ���
	 * 
	 * @param charSet
	 * @param word
	 * @return
	 */
    public static int getCharCount(String charSet, String word) {
        int nCount = 0;
        if (word != null) {
            String temp = word + " ";
            for (int i = 0; i < word.length(); i++) {
                String s = temp.substring(i, i + 1);
                if (charSet.indexOf(s) != -1) nCount++;
            }
        }
        return nCount;
    }

    /***************************************************************************
	 * 
	 * Func Name : GetForeignCharCount
	 * 
	 * Description: Return the foreign type
	 * 
	 * Parameters : sWord: the word
	 * 
	 * Returns : the index value Author : Kevin Zhang History : 1.create
	 * 2002-4-4 2.Modify 2002-5-21
	 **************************************************************************/
    public int GetForeignType(String sWord) {
        int nForeignCount, nCount, nType = TT_ENGLISH;
        nForeignCount = getCharCount(TRANS_ENGLISH, sWord);
        nCount = getCharCount(TRANS_RUSSIAN, sWord);
        if (nForeignCount < nCount) {
            nForeignCount = nCount;
            nType = TT_RUSSIAN;
        }
        nCount = getCharCount(TRANS_JAPANESE, sWord);
        if (nForeignCount < nCount) {
            nForeignCount = nCount;
            nType = TT_JAPANESE;
        }
        return nType;
    }

    public static byte[] readBytes(DataInputStream in, int len) {
        if (in != null && len > 0) {
            byte[] b = new byte[len];
            try {
                for (int i = 0; i < len; i++) b[i] = in.readByte();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return b;
        }
        return null;
    }

    public static boolean PostfixSplit(byte[] sWord, byte[] sWordRet, byte[] sPostfix) {
        byte[] sSinglePostfix = POSTFIX_SINGLE.getBytes();
        byte[][] sMultiPostfix = new byte[POSTFIX_MUTIPLE.length][9];
        for (int i = 0; i < sMultiPostfix.length; i++) sMultiPostfix[i] = POSTFIX_MUTIPLE[i].getBytes();
        int nPostfixLen = 0, nWordLen = sWord.length;
        int i = 0;
        while (sMultiPostfix[i][0] != 0 && strncmp(GFCommon.bytesCopy(sWord, nWordLen - sMultiPostfix[i].length, sWord.length - nWordLen + sMultiPostfix[i].length), 0, sMultiPostfix[i], sMultiPostfix[i].length) == false) {
            i++;
        }
        GFCommon.bytesCopy(sPostfix, sMultiPostfix[i], 0, sMultiPostfix.length);
        nPostfixLen = sMultiPostfix[i].length;
        if (nPostfixLen == 0) {
            sPostfix[2] = 0;
            strncpy(sPostfix, GFCommon.bytesCopy(sWord, nWordLen - 2, 2), 2);
            if (CC_Find(sSinglePostfix, sPostfix)) nPostfixLen = 2;
        }
        strncpy(sWordRet, sWord, nWordLen - nPostfixLen);
        sWordRet[nWordLen - nPostfixLen] = 0;
        sPostfix[nPostfixLen] = 0;
        return true;
    }

    /**
	 * �Ƚϵڶ����ֽ������Ƿ��ڵ�һ���г���
	 * 
	 * @param b1
	 * @param b2
	 * @return ���ص�һ�γ�����λ�á����û�г��֣��򷵻أ�1
	 */
    public static int strstr(byte[] b1, byte[] b2) {
        boolean flag = true;
        if (b1 != null && b2 != null) {
            for (int i = 0; i < b1.length; i++) {
                if (b1[i] != b2[0]) continue; else {
                    if (b1.length - i >= b2.length) {
                        for (int j = 0; j < b2.length; j++) {
                            if (b2[j] != b1[i + j]) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            return i;
                        }
                    }
                }
            }
        }
        return -1;
    }

    public static int strchr(byte[] bs, byte b) {
        if (bs != null) {
            for (int i = 0; i < bs.length; i++) {
                if (bs[i] == b) return i;
            }
        }
        return -1;
    }

    /**
	 * �Ƚ������ֽ�����ǰlen���ֽ��Ƿ����
	 * 
	 * @param b1
	 * @param b2
	 * @param len
	 * @return
	 */
    public static boolean strncmp(byte[] b1, int startIndex, byte[] b2, int len) {
        if (b1 != null && b2 != null && len > 0) {
            if (b1.length >= len && b2.length >= len) {
                for (int i = startIndex; i < len; i++) {
                    if (b1[i] != b2[i]) return true;
                }
            }
        }
        return false;
    }

    public static int getUnsigned(byte b) {
        if (b > 0) return (int) b; else return (b & 0x7F + 128);
    }

    public static void strncpy(byte[] dest, byte[] src, int len) {
        if (dest != null && src != null) {
            if (dest.length >= len && len <= src.length) {
                for (int i = 0; i < len; i++) dest[i] = src[i];
            }
        }
    }

    /**
	 * ������6768��λ���ж�Ӧ��ID��
	 */
    public static int CC_ID(String str) {
        int result = -1;
        if (str != null && str.length() > 0) {
            byte[] b = str.getBytes();
            result = (getUnsigned(b[0]) - 176) * 94 + (getUnsigned(b[1]) - 161);
        }
        return result;
    }

    /**
	 * The first char computed by the Chinese Char ID
	 * 
	 * @param id
	 * @return
	 */
    public static int CC_CHAR1(int id) {
        return (id) / 94 + 176;
    }

    /**
	 * The second char computed by the Chinese Char ID
	 * 
	 * @param id
	 * @return
	 */
    public static int CC_CHAR2(int id) {
        return (id) % 94 + 161;
    }

    public static int strcat(byte[] dest, byte[] src, int len) {
        if (dest != null && src != null && len > 0) {
            for (int i = 0; i < dest.length; i++) {
                if (dest[i] == 0) {
                    for (int j = 0; j < len; j++) dest[i] = src[j];
                    return i;
                }
            }
        }
        return -1;
    }

    public static int strcpy(byte[] dest, byte[] src) {
        return strcpy(dest, src, src.length);
    }

    public static int strcpy(byte[] dest, byte[] src, int len) {
        if (dest != null && src != null && len > 0) {
            int i = 0;
            for (i = 0; i < len; i++) {
                dest[i] = src[i];
            }
            return i;
        }
        return -1;
    }

    /**
	 * ���ID�ŵõ���Ӧ��GB����
	 * 
	 * @param id
	 *            0--6767
	 * @return
	 */
    public static String getGB(int id) {
        String result = null;
        if (id >= 0 && id < 6768) {
            byte[] b = new byte[2];
            b[0] = (byte) CC_CHAR1(id);
            b[1] = (byte) CC_CHAR2(id);
            try {
                result = new String(b, "GBK");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static boolean isSingle(String s) {
        if (s != null && s.getBytes().length == 1) return true; else return false;
    }

    public static int[] removeInvalid(int[] src) {
        int[] result = null;
        int count = 0;
        if (src != null && src.length > 0) {
            for (int i = 0; i < src.length; i++) {
                if (i != 0 && src[i] == 0) break; else count++;
            }
            result = new int[count];
            for (int i = 0; i < count; i++) result[i] = src[i];
        }
        return result;
    }

    /**
	 * �ж��ַ��Ƿ������
	 * 
	 * @param str
	 * @return
	 */
    public static boolean isYearTime(String snum) {
        if (snum != null) {
            int len = snum.length();
            String first = snum.substring(0, 1);
            if (isAllSingleByte(snum) && (len == 4 || len == 2 && (GFString.cint(first) > 4 || GFString.cint(first) == 0))) return true;
            if (isAllNum(snum) && (len >= 6 || len == 4 && "������������".indexOf(first) != -1)) return true;
            if (getCharCount("���һ�����������߰˾�Ҽ��������½��ƾ�", snum) == len && len >= 2) return true;
            if (len == 4 && getCharCount("ǧǪ���", snum) == 2) return true;
            if (len == 1 && getCharCount("ǧǪ", snum) == 1) return true;
            if (len == 2 && getCharCount("���ұ��켺�����ɹ�", snum) == 1 && getCharCount("�ӳ���î������δ�����纥", snum.substring(1)) == 1) return true;
        }
        return false;
    }

    /**
	 * �ж�һ���ַ�������ַ��Ƿ�����һ���ַ�����
	 * 
	 * @param aggr
	 *            �ַ���
	 * @param str
	 *            ��Ҫ�жϵ��ַ�
	 * @return
	 */
    public static boolean isInAggregate(String aggr, String str) {
        if (aggr != null && str != null) {
            str += "1";
            for (int i = 0; i < str.length(); i++) {
                String s = str.substring(i, i + 1);
                if (aggr.indexOf(s) == -1) return false;
            }
            return true;
        }
        return false;
    }

    /**
	 * �жϸ��ַ��Ƿ��ǰ���ַ�
	 * 
	 * @param str
	 * @return
	 */
    public static boolean isDBCCase(String str) {
        if (str != null) {
            str += " ";
            for (int i = 0; i < str.length(); i++) {
                String s = str.substring(i, i + 1);
                if (s.getBytes().length != 1) return false;
            }
            return true;
        }
        return false;
    }

    /**
	 * �жϸ��ַ��Ƿ���ȫ���ַ�
	 * 
	 * @param str
	 * @return
	 */
    public static boolean isSBCCase(String str) {
        if (str != null) {
            str += " ";
            for (int i = 0; i < str.length(); i++) {
                String s = str.substring(i, i + 1);
                if (s.getBytes().length != 2) return false;
            }
            return true;
        }
        return false;
    }

    /**
	 * �ж��Ƿ���һ�����ַ�ָ���
	 * 
	 * @param str
	 * @return
	 */
    public static boolean isDelimiter(String str) {
        if (str != null && ("-".equals(str) || "��".equals(str))) return true; else return false;
    }

    public static boolean isUnknownWord(String word) {
        if (word != null && word.indexOf("δ##") == 0) return true; else return false;
    }

    public static PersonName chineseNameSplit(String word, PosTagger personTagger) {
        PersonName result = null;
        if (word != null && personTagger != null) {
            Dictionary personDict = personTagger.getUnknownDict();
            int len = word.length();
            if (len < 2 || len > 4) return null;
            String[] atoms = GFString.atomSplit(word);
            for (String s : atoms) {
                if (Utility.charType(s) != Utility.CT_CHINESE && Utility.charType(s) != Utility.CT_OTHER) return null;
            }
            String surName = null;
            int surNameLen = 2;
            if (len > 2) surName = word.substring(0, surNameLen); else if (len == 2) surName = word;
            if (!personDict.isExist(surName, 1)) {
                surNameLen = 1;
                if (len > 1) surName = word.substring(0, surNameLen); else if (len == 1) surName = word;
                if (!personDict.isExist(surName, 1)) {
                    surName = null;
                    surNameLen = 0;
                }
            }
            String giveName = word.substring(surNameLen);
            if (len > 3) {
                String temp = word.substring(surNameLen, surNameLen + 1);
                if (personDict.isExist(temp, 1)) {
                    giveName = word.substring(surNameLen + 1);
                }
            }
            double freq = personDict.getFreq(surName, 1);
            String temp = giveName.substring(0, 1);
            double freq2 = personDict.getFreq(temp, 2);
            if (surNameLen != 2 && ((surNameLen == 0 && len > 2) || giveName.length() > 2 || getForeignCharCount(word) >= 3 && freq < personDict.getFreq("��", 1) / 40 && freq2 < personDict.getFreq("��", 2) / 20 || (freq < 10 && getForeignCharCount(giveName) == (len - surNameLen) / 2))) return null;
            if (len == 2 && personTagger.isGivenName(word)) return null;
            result = new PersonName();
            result.setFirstName(surName);
            result.setLastName(giveName);
        }
        return result;
    }
}
