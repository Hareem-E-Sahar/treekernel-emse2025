package org.ictclas4j.bean;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.ictclas4j.utility.GFCommon;
import org.ictclas4j.utility.GFString;
import org.ictclas4j.utility.Utility;

public class Dictionary {

    /**
	 * �ʵ��,��6768��,GB2312����
	 */
    public ArrayList<WordTable> wts;

    /**
	 * �ʵ��޸ı�
	 */
    public ArrayList<ModifyTable> mts;

    public Dictionary() {
        init();
    }

    public Dictionary(String filename) {
        init();
        load(filename);
    }

    public void init() {
        wts = new ArrayList<WordTable>();
        mts = new ArrayList<ModifyTable>();
        for (int i = 0; i < Utility.CC_NUM; i++) {
            wts.add(new WordTable());
            mts.add(new ModifyTable());
        }
    }

    public boolean load(String filename) {
        return load(filename, false);
    }

    /**
	 * �Ӵʵ���м��ش���.��6768�������ݿ�(����5���Ǻ����ַ�),ÿ������ݿ�������ɸ�С��ݿ�,
	 * ÿ��С��ݿ�Ϊһ������,����ݿ���ÿ���������ǹ�һ���ֿ�ͷ��.
	 * 
	 * @param filename
	 *            ���Ĵʵ��ļ���
	 * @param isReset
	 *            �Ƿ�Ҫ����
	 * @return
	 */
    public boolean load(String filename, boolean isReset) {
        File file;
        int[] nBuffer = new int[3];
        file = new File(filename);
        if (!file.canRead()) return false;
        try {
            delModified();
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            for (int i = 0; i < Utility.CC_NUM; i++) {
                int count = GFCommon.bytes2int(Utility.readBytes(in, 4), false);
                wts.get(i).setCount(count);
                if (count <= 0) continue;
                WordItem[] wis = new WordItem[count];
                for (int j = 0; j < count; j++) {
                    nBuffer[0] = GFCommon.bytes2int(Utility.readBytes(in, 4), false);
                    nBuffer[1] = GFCommon.bytes2int(Utility.readBytes(in, 4), false);
                    nBuffer[2] = GFCommon.bytes2int(Utility.readBytes(in, 4), false);
                    WordItem ti = new WordItem();
                    if (nBuffer[1] > 0) {
                        byte[] word = Utility.readBytes(in, nBuffer[1]);
                        ti.setWord(new String(word, "GBK"));
                    } else ti.setWord("");
                    if (isReset) ti.setFreq(0); else ti.setFreq(nBuffer[0]);
                    ti.setLen(nBuffer[1] / 2);
                    ti.setHandle(nBuffer[2]);
                    wis[j] = ti;
                }
                wts.get(i).setWords(wis);
            }
            in.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return true;
    }

    /**
	 * ����ʵ��.������޸ĵĴ���������Ҫ�Դʵ����и��²��ܰ�����д���ļ�
	 * 
	 * @param filename
	 * @return
	 */
    public boolean save(String filename) {
        File file;
        int j, k;
        int[] nBuffer = new int[3];
        file = new File(filename);
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
            for (int i = 0; i < Utility.CC_NUM; i++) {
                if (mts != null) {
                    int nCount = wts.get(i).getCount() + mts.get(i).getCount() - mts.get(i).getDelete();
                    out.write(GFCommon.int2bytes(nCount, false));
                    j = 0;
                    k = 0;
                    for (; j < mts.get(i).getCount() && k < wts.get(i).getCount(); ) {
                        WordItem mwi = mts.get(i).getWords().get(j);
                        WordItem wi = wts.get(i).getWords().get(k);
                        if (mwi.getLen() < wi.getLen() || (strEqual(mwi.getWord(), wi.getWord())) && mwi.getHandle() < wi.getHandle()) {
                            nBuffer[0] = mwi.getFreq();
                            nBuffer[1] = mwi.getLen();
                            nBuffer[2] = mwi.getHandle();
                            for (int n : nBuffer) out.write(GFCommon.int2bytes(n, false));
                            if (nBuffer[1] > 0) out.write(mwi.getWord().getBytes());
                            j++;
                        } else if (mwi.getFreq() == -1) {
                            k++;
                        } else if (mwi.getLen() > wi.getLen() || strEqual(mwi.getWord(), wi.getWord()) && mwi.getHandle() > wi.getHandle()) {
                            nBuffer[0] = wi.getFreq();
                            nBuffer[1] = wi.getLen();
                            nBuffer[2] = wi.getHandle();
                            for (int n : nBuffer) out.write(GFCommon.int2bytes(n, false));
                            if (nBuffer[1] > 0) out.write(wi.getWord().getBytes());
                            k++;
                        }
                    }
                    if (k < wts.get(i).getCount()) {
                        for (; k < wts.get(i).getCount(); ) {
                            WordItem wi = wts.get(i).getWords().get(k);
                            if (wi.getFreq() != -1) {
                                nBuffer[0] = wi.getFreq();
                                nBuffer[1] = wi.getLen();
                                nBuffer[2] = wi.getHandle();
                                for (int n : nBuffer) out.write(GFCommon.int2bytes(n, false));
                                if (nBuffer[1] > 0) out.write(wi.getWord().getBytes());
                            }
                            k++;
                        }
                    } else for (; j < mts.get(i).getCount(); ) {
                        WordItem wi = mts.get(i).getWords().get(j);
                        nBuffer[0] = wi.getFreq();
                        nBuffer[1] = wi.getLen();
                        nBuffer[2] = wi.getHandle();
                        for (int n : nBuffer) out.write(GFCommon.int2bytes(n, false));
                        if (nBuffer[1] > 0) out.write(wi.getWord().getBytes());
                    }
                } else {
                    out.writeInt(wts.get(i).getCount());
                    for (j = 0; j < wts.get(i).getCount(); j++) {
                        WordItem wi = wts.get(i).getWords().get(j);
                        nBuffer[0] = wi.getFreq();
                        nBuffer[1] = wi.getLen();
                        nBuffer[2] = wi.getHandle();
                        for (int n : nBuffer) out.write(GFCommon.int2bytes(n, false));
                        if (nBuffer[1] > 0) out.write(wi.getWord().getBytes());
                    }
                }
            }
            out.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return true;
    }

    /**
	 * ��ʵ������Ӵ���.���ʱֻ���ȰѴ����ŵ��޸ı��У�����ʱ���������ӵĴ���д��ʵ����
	 * 
	 * @param word
	 *            ��
	 * @param handle
	 *            ���
	 * @param frequency
	 *            Ƶ��
	 * @return
	 */
    public boolean addItem(String word, int handle, int frequency) {
        Preword pw = preProcessing(word);
        if (pw != null & pw.getWord() != null) {
            int found = findInOriginalTable(pw.getIndex(), pw.getRes(), handle);
            if (found >= 0) {
                WordItem wi = wts.get(pw.getIndex()).getWords().get(found);
                if (wi.getFreq() != -1) {
                    wi.setFreq(frequency);
                    if (mts == null) mts = new ArrayList<ModifyTable>(Utility.CC_NUM);
                    mts.get(pw.getIndex()).setDelete(mts.get(pw.getIndex()).getDelete() - 1);
                } else wi.setFreq(wi.getFreq() + frequency);
                return true;
            }
            if (mts == null) mts = new ArrayList<ModifyTable>(Utility.CC_NUM);
            int found2 = findInModifyTable(pw.getIndex(), pw.getRes(), handle);
            if (found2 >= 0) {
                WordItem wi = mts.get(pw.getIndex()).getWords().get(found2);
                wi.setFreq(wi.getFreq() + frequency);
                return true;
            }
            WordItem wi = new WordItem();
            wi.setFreq(frequency);
            wi.setHandle(handle);
            wi.setLen(pw.getRes().length());
            wi.setWord(pw.getRes());
            ModifyTable mt = mts.get(pw.getIndex());
            mt.getWords().add(found2, wi);
            mt.setCount(mt.getCount() + 1);
            return true;
        }
        return false;
    }

    public boolean delItem(String word, int handle) {
        Preword pw = preProcessing(word);
        if (pw != null & pw.getWord() != null) {
            int found = findInOriginalTable(pw.getIndex(), pw.getRes(), handle);
            if (found >= 0) {
                if (mts == null) mts = new ArrayList<ModifyTable>(Utility.CC_NUM);
                ModifyTable mt = mts.get(pw.getIndex());
                WordItem wi = mt.getWords().get(found);
                wi.setFreq(-1);
                mt.setCount(mt.getDelete() + 1);
                if (handle == -1) {
                    for (int i = found; i < mt.getCount() && strEqual(mt.getWords().get(i).getWord(), pw.getRes()); i++) {
                        WordItem wi2 = mt.getWords().get(i);
                        wi2.setFreq(-1);
                        mt.setDelete(mt.getDelete() + 1);
                    }
                }
                return true;
            }
            int found2 = findInModifyTable(pw.getIndex(), pw.getRes(), handle);
            if (found2 >= 0) {
                ModifyTable mt = mts.get(pw.getIndex());
                ArrayList<WordItem> wis = mt.getWords();
                for (int i = found2; i < wis.size(); i++) {
                    WordItem wi = wis.get(i);
                    if (strEqual(wi.getWord(), pw.getRes()) && (wi.getHandle() == handle || handle < 0)) {
                        wis.remove(wi);
                        mt.setCount(mt.getCount() - 1);
                        i--;
                    }
                }
                return true;
            }
        }
        return false;
    }

    protected boolean delModified() {
        mts = null;
        return true;
    }

    public boolean isExist(String word, int handle) {
        if (word != null) {
            Preword pw = preProcessing(word);
            if (pw != null) {
                if (findInOriginalTable(pw.getIndex(), pw.getRes(), handle) >= 0 || findInModifyTable(pw.getIndex(), pw.getRes(), handle) >= 0) return true;
            }
        }
        return false;
    }

    public ArrayList<WordItem> getHandle(String word) {
        ArrayList<WordItem> result = null;
        if (word != null) {
            result = new ArrayList<WordItem>();
            Preword pw = preProcessing(word);
            if (pw != null && pw.getWord() != null) {
                int found = findInOriginalTable(pw.getIndex(), pw.getRes(), -1);
                if (found >= 0) {
                    WordItem wi = new WordItem();
                    WordItem wi2 = wts.get(pw.getIndex()).getWords().get(found);
                    wi.setHandle(wi2.getHandle());
                    wi.setFreq(wi2.getFreq());
                    result.add(wi);
                    int temp = found + 1;
                    WordTable wt = wts.get(pw.getIndex());
                    while (temp < wt.getCount() && strEqual(wt.getWords().get(temp).getWord(), pw.getRes())) {
                        wi = new WordItem();
                        wi.setHandle(wt.getWords().get(temp).getHandle());
                        wi.setFreq(wt.getWords().get(temp).getFreq());
                        wi.setWord(word);
                        result.add(wi);
                        temp++;
                    }
                    return result;
                }
                int found2 = findInModifyTable(pw.getIndex(), pw.getRes(), -1);
                if (found2 >= 0) {
                    ModifyTable mt = mts.get(pw.getIndex());
                    ArrayList<WordItem> wis = mt.getWords();
                    for (int i = found2; i < wis.size(); i++) {
                        WordItem wi0 = wis.get(i);
                        if (strEqual(wi0.getWord(), pw.getRes())) {
                            WordItem wi = new WordItem();
                            wi.setHandle(wi0.getHandle());
                            wi.setFreq(wi0.getFreq());
                            wi.setWord(word);
                            result.add(wi);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
	 * ��2�ַ���ѯԴ�ʵ��,���Ƿ��Ѵ���
	 * 
	 * @param index
	 *            ����ݿ��Ӧ���±꣨����ͬһ���ֿ�ͷ�Ĵ���Ϊһ������ݿ飩
	 * @param res
	 *            ȥ��ͷһ���ֺ�ʣ��Ĳ���
	 * @param handle
	 * @return
	 */
    public int findInOriginalTable(int index, String res, int handle) {
        int result = -1;
        if (res != null && wts != null && index > -1) {
            WordTable wt = wts.get(index);
            if (wt != null && wt.getCount() > 0) {
                int start = 0;
                int end = wt.getCount() - 1;
                int mid = (end + start) / 2;
                ArrayList<WordItem> wis = wt.getWords();
                while (start <= end) {
                    WordItem wi = wis.get(mid);
                    int cmpValue = GFString.compareTo(wi.getWord(), res);
                    if (cmpValue == 0 && (wi.getHandle() == handle || handle == -1)) {
                        if (handle == -1) {
                            while (mid >= 0 && res.compareTo(wis.get(mid).getWord()) == 0) {
                                mid--;
                            }
                            if (mid < 0 || res.compareTo(wis.get(mid).getWord()) != 0) mid++;
                        }
                        result = mid;
                        return result;
                    } else if (cmpValue < 0 || cmpValue == 0 && wi.getHandle() < handle && handle != -1) start = mid + 1; else if (cmpValue > 0 || cmpValue == 0 && wi.getHandle() > handle && handle != -1) end = mid - 1;
                    mid = (start + end) / 2;
                }
            }
        }
        return result;
    }

    /**
	 * ���޸ı��в�ѯ�Ƿ����,���������λ�����
	 * 
	 * @param index
	 * @param res
	 * @param handle
	 * @return λ�����
	 */
    protected int findInModifyTable(int index, String res, int handle) {
        int result = -1;
        if (mts != null && mts.size() > index) {
            ArrayList<WordItem> wis = mts.get(index).getWords();
            if (res != null && wis != null) {
                int i = 0;
                for (; i < wis.size(); i++) {
                    WordItem wi = wis.get(i);
                    if (wi.getWord().length() < res.length() || (wi.getWord().length() == res.length() && wi.getHandle() < handle)) continue;
                }
                if (i < wis.size() && strEqual(wis.get(i).getWord(), res) && (wis.get(i).getHandle() == handle || handle < 0)) result = i;
            }
        }
        return result;
    }

    public boolean strEqual(String b1, String b2) {
        if (b1 == null && b2 == null) return true; else if (b1 != null && b2 != null) {
            return b1.equals(b2);
        }
        return false;
    }

    public int getWordType(String word) {
        if (word != null) {
            int type = Utility.charType(word);
            int len = word.length();
            if (len > 0 && type == Utility.CT_CHINESE && GFString.isAllChinese(word)) return Utility.WT_CHINESE; else if (len > 0 && type == Utility.CT_DELIMITER) return Utility.WT_DELIMITER;
        }
        return Utility.WT_OTHER;
    }

    /**
	 * Ԥ����,�ȰѴ�ǰ��Ŀո�ȥ��
	 * 
	 * @param word
	 * @param wordRet
	 * @param isAdd
	 * @return
	 */
    public Preword preProcessing(String word) {
        Preword result = null;
        if (word != null && word.length() > 0) {
            int type = Utility.charType(word);
            word = GFString.removeSpace(word);
            int len = word.length();
            int end = len - 1, begin = 0;
            if (begin > end) return null;
            result = new Preword();
            result.setWord(word);
            if (type == Utility.CT_CHINESE) {
                result.setIndex(Utility.CC_ID(word));
                if (word != null) result.setRes(word.length() > 1 ? word.substring(1) : "");
            } else if (type == Utility.CT_DELIMITER) {
                result.setIndex(3755);
                result.setRes(word);
            } else result.setIndex(-1);
        }
        return result;
    }

    public boolean mergePOS(int handle) {
        mts = new ArrayList<ModifyTable>();
        for (int i = 0; i < Utility.CC_NUM; i++) {
        }
        return false;
    }

    /**
	 * �Ӵʵ�����ҳ���ƥ���һ��
	 * 
	 * @param word
	 * @return
	 */
    public WordItem getMaxMatch(String word) {
        if (word != null) {
            Preword pw = preProcessing(word);
            if (pw != null & pw.getWord() != null && pw.getIndex() >= 0) {
                String firstChar = pw.getWord().substring(0, 1);
                int found = findInOriginalTable(pw.getIndex(), pw.getRes(), -1);
                if (found == -1) {
                    ArrayList<WordItem> wis = wts.get(pw.getIndex()).getWords();
                    if (wis != null) {
                        for (int j = 0; j < wis.size(); j++) {
                            int compValue = GFString.compareTo(wis.get(j).getWord(), pw.getRes());
                            if (compValue == 1) {
                                found = j;
                                break;
                            }
                        }
                    }
                }
                if (found >= 0 && wts != null && wts.get(pw.getIndex()) != null) {
                    ArrayList<WordItem> wis = wts.get(pw.getIndex()).getWords();
                    if (wis == null) return null;
                    WordItem wi = wis.get(found);
                    String wordRet = firstChar + wi.getWord();
                    return new WordItem(wordRet, wi.getLen(), wi.getHandle(), wi.getFreq());
                }
                ArrayList<WordItem> wis = null;
                if (mts != null && mts.get(pw.getIndex()) != null) {
                    wis = mts.get(pw.getIndex()).getWords();
                    if (wis != null) for (WordItem wi : wis) {
                        if (pw.getRes() != null && pw.getRes().equals(wi.getWord())) {
                            String wordRet = firstChar + wi.getWord();
                            return new WordItem(wordRet, wi.getLen(), wi.getHandle(), wi.getFreq());
                        }
                    }
                }
            }
        }
        return null;
    }

    public int getFreq(String word, int handle) {
        if (word != null && word.length() > 0) {
            Preword pw = preProcessing(word);
            if (pw != null) {
                int found = findInOriginalTable(pw.getIndex(), pw.getRes(), handle);
                if (found >= 0 && wts != null) {
                    WordTable wt = wts.get(pw.getIndex());
                    WordItem wi = wt.getWords().get(found);
                    return wi.getFreq();
                }
                int found2 = findInModifyTable(pw.getIndex(), pw.getRes(), handle);
                if (found2 >= 0 && mts != null) {
                    ModifyTable mt = mts.get(pw.getIndex());
                    WordItem wi = mt.getWords().get(found);
                    return wi.getFreq();
                }
            }
        }
        return 0;
    }

    public boolean optimum() {
        return false;
    }

    public boolean merge(Dictionary dict2, int nRatio) {
        return false;
    }

    public boolean outputChars(String sFilename) {
        return false;
    }

    public boolean output(String sFilename) {
        return false;
    }

    public boolean getPOSString(int nPOS, String sPOSRet) {
        return false;
    }

    public int getPOSValue(byte[] sPOS) {
        return 0;
    }
}
