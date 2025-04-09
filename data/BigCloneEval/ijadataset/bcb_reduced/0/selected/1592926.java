package com.roiding.rdict.dict;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Vector;
import android.media.MediaPlayer;

public class Dict {

    public DictConst dictConstInstance = null;

    private appConstValue constValue = null;

    private AppUtility appUtility = null;

    private DictFile fc = null;

    private DictFile fcDict = null;

    private DictFile fcWord = null;

    private DictFile fcCont = null;

    private DictFile fcSund = null;

    private StringBuffer wordList[];

    private byte[] wordBytes = null;

    private int wordStep = 0;

    private Vector<indexInfo> vIndexInfo;

    private byte[] indexBuf = null;

    private long highIndexWord = 0;

    private long lowIndexWord = 0;

    private byte[] tmpPosBuf = null;

    private byte[] tmpWordBuf = null;

    private byte[] tmpContentBuf = null;

    private byte[] soundbuf = null;

    private long tmpWordStartPos = 0;

    public Dict() {
        dictConstInstance = DictConst.getInstance();
        constValue = appConstValue.getInstance();
        appUtility = AppUtility.getInstance();
        wordBytes = new byte[constValue.WORD_LENGTH_MAX * 2];
        vIndexInfo = new Vector<indexInfo>();
    }

    public boolean initDict(String dictName) {
        if (dictName == null) {
            return false;
        }
        dictConstInstance.fileInfo.dictName = dictName;
        if (fc != null) {
            fc.closeDict();
            fc = null;
        }
        fc = new DictFile();
        if (fc.openDiction(dictConstInstance.fileInfo.dictName) == false) {
            return false;
        }
        initDict(true);
        return true;
    }

    public StringBuffer getWordById(int index) {
        return wordList[index];
    }

    /**
	 * 
	 * @return
	 * @deprecated
	 */
    public int getDefaultLanguage() {
        return (int) dictConstInstance.fileInfo.defaultLanguage;
    }

    public StringBuffer[] getInitWordList() {
        dictConstInstance.DICT_WORDBuf = null;
        dictConstInstance.DICT_WORDBuf = new char[constValue.WORD_LENGTH_MAX];
        dictConstInstance.fstShowWordNO = 0;
        StringBuffer firstWord = new StringBuffer();
        int wordLen = 0;
        wordLen = SDGetWordByNO_A(0, true);
        appUtility.charCopyFrombyte(firstWord, wordBytes, wordLen);
        dictConstInstance.fstShowWordNO = -1;
        return getWordList(firstWord.toString());
    }

    public StringBuffer[] getWordListDown() {
        int i = 0;
        int wordLen = 0;
        if (dictConstInstance.fstShowWordNO >= dictConstInstance.fileInfo.wordCount - wordList.length - 1) {
            return null;
        }
        dictConstInstance.fstShowWordNO++;
        for (; i < wordList.length - 1; i++) {
            wordList[i] = wordList[i + 1];
        }
        wordList[i] = null;
        wordList[i] = new StringBuffer();
        wordLen = SDGetWordByNO_B(dictConstInstance.fstShowWordNO + i);
        if (wordLen != 0) {
            appUtility.charCopyFrombyte(wordList[i], wordBytes, wordLen);
        } else {
            wordLen = SDGetWordByNO_A(dictConstInstance.fstShowWordNO + i, true);
            appUtility.charCopyFrombyte(wordList[i], wordBytes, wordLen);
        }
        tmpPosBuf = null;
        tmpWordBuf = null;
        System.gc();
        return wordList;
    }

    public StringBuffer[] getWordListUp() {
        int i = 0;
        int wordLen = 0;
        if (dictConstInstance.fstShowWordNO <= 0) {
            return null;
        }
        dictConstInstance.fstShowWordNO--;
        for (i = wordList.length - 1; i > 0; i--) {
            wordList[i] = wordList[i - 1];
        }
        wordList[0] = null;
        wordList[0] = new StringBuffer();
        wordLen = SDGetWordByNO_B(dictConstInstance.fstShowWordNO);
        if (wordLen != 0) {
            appUtility.charCopyFrombyte(wordList[0], wordBytes, wordLen);
        } else {
            wordLen = SDGetWordByNO_A(dictConstInstance.fstShowWordNO, true);
            appUtility.charCopyFrombyte(wordList[0], wordBytes, wordLen);
        }
        tmpPosBuf = null;
        tmpWordBuf = null;
        System.gc();
        return wordList;
    }

    public StringBuffer[] getWordList(String word) {
        long nWordNo = -1;
        int i = 0;
        System.gc();
        dictConstInstance.DICT_WORDBuf = null;
        dictConstInstance.DICT_WORDBuf = new char[constValue.WORD_LENGTH_MAX];
        int wordLength = word.length() > 48 ? 48 : word.length();
        for (i = 0; i < wordLength; i++) {
            dictConstInstance.DICT_WORDBuf[i] = word.charAt(i);
        }
        long lastNo = dictConstInstance.fstShowWordNO;
        long retNo = searchWord();
        if (retNo == -2) {
            dictConstInstance.fstShowWordNO = 0;
        } else if (retNo == -3) {
            dictConstInstance.fstShowWordNO = dictConstInstance.fileInfo.wordCount - wordList.length - 1;
        } else {
            dictConstInstance.fstShowWordNO = retNo;
        }
        if (dictConstInstance.fstShowWordNO == lastNo) {
            tmpPosBuf = null;
            tmpWordBuf = null;
            dictConstInstance.DICT_WORDBuf = null;
            System.gc();
            return wordList;
        }
        if (wordList != null) {
            for (i = 0; i < wordList.length; i++) {
                wordList[i] = null;
            }
            wordList = null;
        }
        wordList = new StringBuffer[constValue.WORD_MAX_LIST];
        nWordNo = dictConstInstance.fstShowWordNO;
        int wordLen = 0;
        for (i = 0; i < constValue.WORD_MAX_LIST; i++) {
            wordLen = SDGetWordByNO_B(nWordNo + i);
            if (wordLen == 0) {
                break;
            }
            wordList[i] = new StringBuffer();
            appUtility.charCopyFrombyte(wordList[i], wordBytes, wordLen);
        }
        for (; i < constValue.WORD_MAX_LIST; i++) {
            if (nWordNo + i == dictConstInstance.fileInfo.wordCount) {
                break;
            }
            wordList[i] = new StringBuffer();
            wordLen = SDGetWordByNO_A(nWordNo + i, true);
            appUtility.charCopyFrombyte(wordList[i], wordBytes, wordLen);
        }
        tmpPosBuf = null;
        tmpWordBuf = null;
        return wordList;
    }

    public boolean getWordContentLast() {
        if (dictConstInstance.curWordNO > 0) dictConstInstance.curWordNO--; else return false;
        if (dictConstInstance.pDICT_ContentBuf != null) {
            dictConstInstance.pDICT_ContentBuf = null;
            System.gc();
        }
        dictConstInstance.pDICT_ContentBuf = new StringBuffer();
        DICT_GetContentToBuf(-1);
        soundbuf = null;
        System.gc();
        dictConstInstance.hSoundBuf = SDGetSoundByNO(dictConstInstance.curWordNO, dictConstInstance.CurTabNO);
        return true;
    }

    public boolean getWordContentNext() {
        if (dictConstInstance.curWordNO < dictConstInstance.fileInfo.wordCount - 1) dictConstInstance.curWordNO++; else return false;
        if (dictConstInstance.pDICT_ContentBuf != null) {
            dictConstInstance.pDICT_ContentBuf = null;
            System.gc();
        }
        dictConstInstance.pDICT_ContentBuf = new StringBuffer();
        DICT_GetContentToBuf(-1);
        soundbuf = null;
        System.gc();
        dictConstInstance.hSoundBuf = SDGetSoundByNO(dictConstInstance.curWordNO, dictConstInstance.CurTabNO);
        return true;
    }

    public String getWordContent(int index) {
        System.gc();
        if (dictConstInstance.pDICT_ContentBuf != null) {
            dictConstInstance.pDICT_ContentBuf = null;
            System.gc();
        }
        dictConstInstance.pDICT_ContentBuf = new StringBuffer();
        dictConstInstance.curWordNO = dictConstInstance.fstShowWordNO + index;
        DICT_GetContentToBuf(index);
        soundbuf = null;
        System.gc();
        dictConstInstance.hSoundBuf = SDGetSoundByNO(dictConstInstance.curWordNO, dictConstInstance.CurTabNO);
        return dictConstInstance.pDICT_ContentBuf.toString();
    }

    public boolean hasSound() {
        return (dictConstInstance.hSoundBuf != 0);
    }

    public byte[] getSound() {
        if (hasSound()) {
            if (soundbuf == null) {
                SDLoadSoundDataByIdx(dictConstInstance.hSoundBuf);
            }
            return soundbuf;
        } else {
            return null;
        }
    }

    public void playSound() {
        if (dictConstInstance.hSoundBuf != 0) {
            if (soundbuf == null) {
                SDLoadSoundDataByIdx(dictConstInstance.hSoundBuf);
            }
            try {
                File bufferedFile = File.createTempFile("playingMedia", ".dat");
                FileOutputStream out = new FileOutputStream(bufferedFile);
                out.write(soundbuf);
                out.flush();
                out.close();
                MediaPlayer player = new MediaPlayer();
                player.setDataSource(bufferedFile.getAbsolutePath());
                player.prepare();
            } catch (Exception e) {
                System.out.print(e.toString());
            }
        }
    }

    public void closeEngine() {
        if (fc != null) {
            fc.closeDict();
            fc = null;
        }
        if (fcDict != null) {
            fcDict.closeDict();
            fcDict = null;
        }
        if (fcWord != null) {
            fcWord.closeDict();
            fcWord = null;
        }
        if (fcCont != null) {
            fcCont.closeDict();
            fcCont = null;
        }
        if (fcSund != null) {
            fcSund.closeDict();
            fcSund = null;
        }
        wordBytes = null;
        vIndexInfo = null;
        tmpContentBuf = null;
        soundbuf = null;
        tmpPosBuf = null;
        tmpWordBuf = null;
        dictConstInstance.pDICT_ContentBuf = null;
        if (wordList != null) {
            for (int i = 0; i < wordList.length; i++) {
                if (wordList[i] != null) {
                    wordList[i] = null;
                }
            }
        }
        System.gc();
    }

    private void getIndexInfo(DictFileInfo dictInfo) {
        DictFile fcIndex = null;
        fcIndex = new DictFile();
        String indexPath = "dict/" + dictConstInstance.fileInfo.dictName + "/index.dat";
        if (fcIndex.openDict(indexPath) == false) {
            return;
        }
        InputStream is = null;
        byte bLen[] = new byte[4];
        byte bStep[] = new byte[4];
        try {
            is = fcIndex.getIn();
            is.read(bLen, 0, 4);
            is.read(bStep, 0, 4);
        } catch (Exception e) {
            System.out.println(e.toString());
            return;
        }
        int len = (int) appUtility.byte2long(bLen);
        wordStep = (int) appUtility.byte2long(bStep);
        bLen = null;
        bStep = null;
        indexBuf = new byte[len];
        try {
            is.read(indexBuf, 0, len);
            is.close();
            is = null;
        } catch (Exception e) {
            System.out.println(e.toString());
            return;
        }
        fcIndex.closeDict();
        fcIndex = null;
        int i = 0;
        long maxNum = dictInfo.wordCount;
        long wordNum = dictInfo.StartWordNum[i++];
        int start = 0;
        int j = 0;
        while (wordNum < maxNum) {
            indexInfo idx = new indexInfo();
            if (i < dictInfo.wordFiles) {
                if (wordNum > dictInfo.StartWordNum[i]) {
                    wordNum = dictInfo.StartWordNum[i];
                    i++;
                }
            }
            j = start;
            while (j < indexBuf.length && (!((indexBuf[j] == 0x3B) && (indexBuf[j + 1] == 0x00)))) {
                j += 2;
            }
            idx.startPos = start;
            idx.endPos = j;
            idx.wordNum = wordNum;
            vIndexInfo.addElement(idx);
            start = j + 2;
            wordNum += wordStep;
            if (wordNum >= maxNum - 1) {
                indexInfo idxLast = new indexInfo();
                j = start;
                while (j < indexBuf.length && (!((indexBuf[j] == 0x3B) && (indexBuf[j + 1] == 0x00)))) {
                    j += 2;
                }
                idxLast.startPos = start;
                idxLast.endPos = j;
                idxLast.wordNum = maxNum - 1;
                vIndexInfo.addElement(idxLast);
            }
        }
    }

    private long getWordPosByIndex() {
        int lowWord = 0;
        int highWord = vIndexInfo.size();
        int scanWordIndex = (highWord + lowWord) / 2;
        int intTmp = 0;
        while (lowWord < highWord - 1) {
            indexInfo idx = null;
            idx = (indexInfo) vIndexInfo.elementAt(scanWordIndex);
            int wordLen = idx.endPos - idx.startPos;
            appUtility.memsetBytes(wordBytes);
            appUtility.byteStrCpy(wordBytes, 0, indexBuf, idx.startPos, wordLen);
            intTmp = appUtility.compareWord(dictConstInstance.DICT_WORDBuf, wordBytes);
            if (constValue.DICT_WORDSAME == intTmp) {
                return idx.wordNum;
            } else if (constValue.DICT_WORDASMALL == intTmp) {
                highWord = scanWordIndex;
                scanWordIndex = (lowWord + highWord) / 2;
            } else {
                lowWord = scanWordIndex;
                scanWordIndex = (lowWord + highWord) / 2;
            }
            idx = null;
        }
        indexInfo idx1 = null;
        idx1 = (indexInfo) vIndexInfo.elementAt(scanWordIndex);
        int wordLen = idx1.endPos - idx1.startPos;
        appUtility.memsetBytes(wordBytes);
        appUtility.byteStrCpy(wordBytes, 0, indexBuf, idx1.startPos, wordLen);
        intTmp = appUtility.compareWord(dictConstInstance.DICT_WORDBuf, wordBytes);
        if (constValue.DICT_WORDSAME == intTmp) {
            lowIndexWord = 0;
            indexInfo idx2 = (indexInfo) vIndexInfo.elementAt(scanWordIndex + 1);
            highIndexWord = idx2.wordNum;
            idx2 = null;
            return idx1.wordNum;
        } else if (constValue.DICT_WORDASMALL == intTmp) {
            if (scanWordIndex > 0) {
                highIndexWord = idx1.wordNum;
                indexInfo idx2 = null;
                idx2 = (indexInfo) vIndexInfo.elementAt(scanWordIndex - 1);
                lowIndexWord = idx2.wordNum;
                idx2 = null;
            } else {
                return -2;
            }
            return -1;
        } else {
            if (scanWordIndex < vIndexInfo.size() - 1) {
                lowIndexWord = idx1.wordNum;
                indexInfo idx2 = null;
                idx2 = (indexInfo) vIndexInfo.elementAt(scanWordIndex + 1);
                highIndexWord = idx2.wordNum;
                idx2 = null;
            } else {
                return -3;
            }
            return -1;
        }
    }

    private long searchWord() {
        long searchNO = 0;
        searchNO = getWordPosByIndex();
        if (searchNO >= 0) {
            searchNO = findAWord_IdxInFileA(dictConstInstance.fileInfo, true);
        } else if (searchNO == -1) {
            searchNO = findAWord_IdxInFileA(dictConstInstance.fileInfo, false);
        }
        return searchNO;
    }

    private int SDGetWordByNO_B(long wordNO) {
        DictFileInfo dictFile = dictConstInstance.fileInfo;
        long tmpa = 0;
        long tmpb = 0;
        int skipSize = (int) (wordNO - lowIndexWord) * dictFile.IndexWidth;
        if ((tmpPosBuf == null) || (tmpWordBuf == null)) {
            return 0;
        }
        if (skipSize > (tmpPosBuf.length - dictFile.IndexWidth * 2)) {
            return 0;
        }
        tmpa = appUtility.byte2longB(tmpPosBuf, skipSize, dictFile.IndexWidth);
        tmpb = appUtility.byte2longB(tmpPosBuf, skipSize + dictFile.IndexWidth, dictFile.IndexWidth);
        tmpb -= tmpa;
        if (tmpb > constValue.WORD_LENGTH_MAX * 2) {
            tmpb = constValue.WORD_LENGTH_MAX * 2;
        }
        appUtility.memsetBytes(wordBytes);
        skipSize = (int) (tmpa - tmpWordStartPos);
        appUtility.byteStrCpy(wordBytes, 0, tmpWordBuf, skipSize, (int) tmpb);
        return (int) tmpb;
    }

    private void setWordTmpBuffer() {
        DictFileInfo dictFile = dictConstInstance.fileInfo;
        long tmpa = dictFile.StartWordNum[dictFile.CurWordFileNO];
        long tmpb = dictFile.StartWordNum[dictFile.CurWordFileNO + 1];
        long indexAreaSize = (tmpb - tmpa + 1) * dictFile.IndexWidth;
        tmpa = lowIndexWord - tmpa;
        int randomOffset = dictFile.randomOffset + (int) tmpa * dictFile.IndexWidth;
        int length = 0;
        if (tmpb > (highIndexWord + constValue.WORD_MAX_LIST)) {
            length = (int) (highIndexWord - lowIndexWord + constValue.WORD_MAX_LIST) * dictFile.IndexWidth;
        } else {
            length = (int) (tmpb - 1 - lowIndexWord) * dictFile.IndexWidth;
        }
        InputStream is = null;
        tmpPosBuf = null;
        tmpPosBuf = new byte[length];
        int skipSize = (int) tmpa * dictFile.IndexWidth;
        try {
            is = fcWord.getIn();
            is.skip(skipSize);
            is.read(tmpPosBuf, 0, length);
        } catch (Exception e) {
            System.out.println(e.toString());
            return;
        }
        skipSize += length;
        randomOffset = addRandomTable(tmpPosBuf, tmpPosBuf.length, randomOffset, dictFile);
        tmpa = 0;
        tmpb = 0;
        tmpa = appUtility.byte2longB(tmpPosBuf, 0, dictFile.IndexWidth);
        tmpb = appUtility.byte2longB(tmpPosBuf, (tmpPosBuf.length - dictFile.IndexWidth), dictFile.IndexWidth);
        tmpb -= tmpa;
        tmpWordStartPos = tmpa;
        tmpWordBuf = null;
        tmpWordBuf = new byte[(int) tmpb];
        skipSize = (int) (tmpa + indexAreaSize) - skipSize;
        try {
            is.skip(skipSize);
            is.read(tmpWordBuf, 0, (int) tmpb);
            is.close();
            is = null;
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        randomOffset = (int) (tmpa + indexAreaSize + dictFile.randomOffset);
        addRandomTable(tmpWordBuf, tmpWordBuf.length, randomOffset, dictFile);
        return;
    }

    private long findAWord_IdxInFileA(DictFileInfo dictFind, boolean bFind) {
        long lowWord;
        long highWord;
        long intTmp;
        long scanWordIndex;
        lowWord = lowIndexWord;
        highWord = highIndexWord;
        scanWordIndex = (lowWord + highWord) / 2;
        int FileNO = _FindFileNumByNO(dictFind, true, scanWordIndex, dictFind.wordFiles);
        if (FileNO != dictFind.CurWordFileNO) {
            dictFind.CurWordFileNO = FileNO;
            Integer i = new Integer(FileNO);
            String str = null;
            str = new String(dictFind.MainFileName + "w" + i.toString() + ".dat");
            dictFind.pfCurWord = str;
            if (fcWord != null) {
                fcWord.closeDict();
                fcWord = null;
            }
            fcWord = new DictFile();
            if (fcWord.openDict(str) == false) {
                return 0;
            }
        }
        setWordTmpBuffer();
        if (constValue.UNISTREND == dictConstInstance.DICT_WORDBuf[0]) {
            lowWord = 0;
            highWord = 1;
            scanWordIndex = 0;
        }
        if (!bFind) {
            SDGetWordByNO_B(scanWordIndex);
            intTmp = appUtility.compareWord(dictConstInstance.DICT_WORDBuf, wordBytes);
            while (lowWord < highWord - 1) {
                if (constValue.DICT_WORDSAME == intTmp) {
                    break;
                } else if (constValue.DICT_WORDASMALL == intTmp) {
                    highWord = scanWordIndex;
                    scanWordIndex = (lowWord + highWord) / 2;
                } else {
                    lowWord = scanWordIndex;
                    scanWordIndex = (lowWord + highWord) / 2;
                }
                SDGetWordByNO_B(scanWordIndex);
                intTmp = appUtility.compareWord(dictConstInstance.DICT_WORDBuf, wordBytes);
            }
        } else {
            intTmp = constValue.DICT_WORDSAME;
            scanWordIndex = lowIndexWord;
        }
        if (constValue.DICT_WORDSAME == intTmp) {
            highWord = scanWordIndex;
            while ((constValue.DICT_WORDSAME == intTmp) && (highWord != 0)) {
                highWord--;
                SDGetWordByNO_B(highWord);
                intTmp = appUtility.compareWord(dictConstInstance.DICT_WORDBuf, wordBytes);
                if (0 == highWord) {
                    break;
                }
            }
            intTmp = appUtility.compareWord(dictConstInstance.DICT_WORDBuf, wordBytes);
            if (constValue.DICT_WORDSAME != intTmp) {
                highWord++;
            }
            SDGetWordByNO_B(highWord);
            intTmp = appUtility.UNIStrByteCmp(dictConstInstance.DICT_WORDBuf, wordBytes);
            scanWordIndex = highWord;
            while (true) {
                lowWord = scanWordIndex;
                if (intTmp != 0) {
                    scanWordIndex++;
                } else {
                    break;
                }
                SDGetWordByNO_B(scanWordIndex);
                intTmp = appUtility.compareWord(dictConstInstance.DICT_WORDBuf, wordBytes);
                if (constValue.DICT_WORDSAME != intTmp || scanWordIndex == (dictFind.wordCount - 1)) {
                    scanWordIndex = highWord;
                    intTmp = constValue.DICT_WORDSAME;
                    break;
                }
                intTmp = appUtility.UNIStrByteCmp(dictConstInstance.DICT_WORDBuf, wordBytes);
            }
        }
        while ((scanWordIndex < (dictFind.wordCount - 1)) && (constValue.DICT_WORDABIG == intTmp)) {
            scanWordIndex++;
            SDGetWordByNO_B(scanWordIndex);
            intTmp = appUtility.compareWord(dictConstInstance.DICT_WORDBuf, wordBytes);
        }
        return scanWordIndex;
    }

    private void SDGetDictInfo() {
        String dictName = dictConstInstance.fileInfo.dictName;
        InputStream is = null;
        dictConstInstance.fileInfo.MainFileName = "dict/" + dictName + "/" + dictName;
        byte checkStr[] = new byte[8];
        byte version[] = new byte[8];
        try {
            is = fc.getIn();
            is.read(checkStr, 0, 8);
            is.read(version, 0, 8);
        } catch (Exception e) {
            System.out.println(e.toString());
            return;
        }
        if (false == appUtility.compareBytes(checkStr, constValue.DICT_TITLECHECK, constValue.DICT_TITLECHECK.length) || (checkStr[constValue.MACHINE_TYPE_POS] != 'A')) {
            dictConstInstance.fileInfo.wordCount = 0;
            return;
        }
        dictConstInstance.fileInfo.versionMain = (short) appUtility.byte2int(version);
        dictConstInstance.fileInfo.versionSub = version[2];
        dictConstInstance.fileInfo.versionReserve = version[3];
        dictConstInstance.fileInfo.intLanguage = appUtility.setlong4(version, 4);
        checkStr = null;
        version = null;
        dictConstInstance.fileInfo.EncodeData = new byte[constValue.ENCODE_SIZE];
        if (dictConstInstance.fileInfo.versionMain == constValue.CURRENT_MAIN_VERSION) {
            byte tmpBytes[] = new byte[56];
            byte randomOffset[] = new byte[1];
            try {
                is.skip(constValue.EMPTYDATA_BYTES);
                is.read(tmpBytes, 0, 56);
                is.read(dictConstInstance.fileInfo.EncodeData, 0, constValue.ENCODE_SIZE);
                is.skip(483);
                is.read(randomOffset, 0, 1);
            } catch (Exception e) {
                System.out.println(e.toString());
                return;
            }
            dictConstInstance.fileInfo.randomOffset = (short) appUtility.byte2short(randomOffset);
            int udTmp = (int) dictConstInstance.fileInfo.randomOffset;
            addRandomTable(tmpBytes, 56, udTmp, dictConstInstance.fileInfo);
            setDictInfo(tmpBytes, 56);
            tmpBytes = null;
            byte wordFile[] = new byte[((int) dictConstInstance.fileInfo.wordFiles + 1) * 8];
            byte conFile[] = new byte[((int) dictConstInstance.fileInfo.contentFiles + 1) * 8];
            try {
                is.read(wordFile, 0, wordFile.length);
            } catch (Exception e) {
                System.out.println(e.toString());
                return;
            }
            addRandomTable(wordFile, wordFile.length, udTmp, dictConstInstance.fileInfo);
            setWordFile(wordFile);
            try {
                is.skip(dictConstInstance.fileInfo.WordOffset[dictConstInstance.fileInfo.wordFiles]);
                is.read(conFile, 0, conFile.length);
                is.close();
                is = null;
            } catch (Exception e) {
                System.out.println(e.toString());
                return;
            }
            addRandomTable(conFile, conFile.length, udTmp, dictConstInstance.fileInfo);
            setConFile(conFile);
            dictConstInstance.fileInfo.pfLZDict = new String(dictConstInstance.fileInfo.MainFileName + "c0.dat");
            wordFile = null;
            conFile = null;
            if (fc != null) {
                fc.closeDict();
                fc = null;
            }
            if (fcDict != null) {
                fcDict.closeDict();
                fcDict = null;
            }
            fcDict = new DictFile();
            if (fcDict.openDict(dictConstInstance.fileInfo.pfLZDict) == false) {
                return;
            }
            for (int i = 0; i < dictConstInstance.fileInfo.DictNum; i++) {
                byte tmpByteV[] = new byte[4];
                try {
                    is = fcDict.getIn();
                    is.skip(12);
                    is.read(tmpByteV, 0, 4);
                    is.close();
                    is = null;
                } catch (Exception e) {
                    System.out.println(e.toString());
                    return;
                }
                udTmp = (int) dictConstInstance.fileInfo.randomOffset + 8;
                addRandomTable(tmpByteV, tmpByteV.length, udTmp, dictConstInstance.fileInfo);
                dictConstInstance.fileInfo.treeNodeBits[i] = tmpByteV[0];
                dictConstInstance.fileInfo.matchByteBits[i] = tmpByteV[1];
                dictConstInstance.fileInfo.BreakEvnBytes[i] = tmpByteV[2];
                dictConstInstance.fileInfo.sdOffset[i] += 16;
                tmpByteV = null;
            }
            dictConstInstance.fileInfo.ContentMaxBytes += dictConstInstance.fileInfo.wordMaxBytes + 8;
        }
        if (dictConstInstance.fileInfo.ContentMaxBytes < dictConstInstance.fileInfo.SoundMaxBytes) {
            dictConstInstance.fileInfo.ContentMaxBytes = dictConstInstance.fileInfo.SoundMaxBytes;
        }
        if (dictConstInstance.fileInfo.ContentMaxBytes < 1000) {
            dictConstInstance.fileInfo.ContentMaxBytes = 1000;
        }
        dictConstInstance.fileInfo.ContentMaxBytes += constValue.RESERVED_CACHEBYTES + 20;
        dictConstInstance.fileInfo.wordPerGroups = 1;
        dictConstInstance.fileInfo.CurWordFileNO = 0xFFFF;
        dictConstInstance.fileInfo.curContFileNO = 0xFFFF;
    }

    private int addRandomTable(byte[] psrc, int nums, int offset, DictFileInfo dictInfo) {
        int i = 0;
        int curoff = offset;
        curoff &= constValue.ENCODE_MASK;
        while (nums > 0) {
            if (curoff > constValue.ENCODE_MASK) {
                curoff = 0;
            }
            psrc[i] = (byte) (psrc[i] ^ dictInfo.EncodeData[curoff & constValue.ENCODE_MASK]);
            i++;
            nums--;
            curoff++;
        }
        return curoff;
    }

    private void initDict(boolean newApp) {
        long intTmp;
        if (newApp) {
            dictConstInstance.DispDictList = (byte) constValue._DICTLIST_DISPALL;
        } else {
            dictConstInstance.DispDictList = (byte) constValue._DICTLIST_DISPALL;
        }
        SDGetDictInfo();
        dictConstInstance.curWordNO = 0;
        dictConstInstance.fstShowWordNO = 0;
        dictConstInstance.fstBlurNO = 0;
        dictConstInstance.hDelayTimer = 0;
        dictConstInstance.CurTabNO = 0;
        if (newApp) {
            intTmp = dictConstInstance.fileInfo.ContentMaxBytes;
            dictConstInstance.hContentBuf = 0;
            dictConstInstance.hContentBuf = intTmp;
            dictConstInstance.hSoundBuf = 0;
            if (dictConstInstance.fileInfo.SoundMaxBytes != 0) {
                dictConstInstance.hSoundBuf = dictConstInstance.fileInfo.SoundMaxBytes;
            }
            dictConstInstance.imeTpye = 0;
        } else {
            intTmp = dictConstInstance.hContentBuf;
            if (intTmp < dictConstInstance.fileInfo.ContentMaxBytes) {
                intTmp = dictConstInstance.fileInfo.ContentMaxBytes;
                dictConstInstance.hContentBuf = 0;
                dictConstInstance.hContentBuf = intTmp;
            }
            intTmp = 0;
            if (dictConstInstance.hSoundBuf != 0) intTmp = dictConstInstance.hSoundBuf;
            if (intTmp < dictConstInstance.fileInfo.SoundMaxBytes) {
                dictConstInstance.hSoundBuf = 0;
                dictConstInstance.hSoundBuf = dictConstInstance.fileInfo.SoundMaxBytes;
            }
        }
        if (dictConstInstance.hContentBuf == 0) {
            return;
        }
        intTmp = dictConstInstance.fileInfo.wordCount / (constValue.WORDIDXINRAM_GROUPSIZE);
        if ((intTmp * constValue.WORDIDXINRAM_GROUPSIZE) != dictConstInstance.fileInfo.wordCount) {
            intTmp++;
        }
        getIndexInfo(dictConstInstance.fileInfo);
    }

    private void setConFile(byte wordFile[]) {
        DictFileInfo dictFile = dictConstInstance.fileInfo;
        int n = 0;
        int i = 0;
        while (n <= wordFile.length - 8) {
            dictFile.starContentNum[i] = appUtility.setlong4(wordFile, n);
            n += 4;
            dictFile.contentOffset[i] = appUtility.setlong4(wordFile, n);
            n += 4;
            i++;
        }
    }

    private void setWordFile(byte conFile[]) {
        DictFileInfo dictFile = dictConstInstance.fileInfo;
        int n = 0;
        int i = 0;
        while (n <= conFile.length - 8) {
            dictFile.StartWordNum[i] = appUtility.setlong4(conFile, n);
            n += 4;
            dictFile.WordOffset[i] = appUtility.setlong4(conFile, n);
            n += 4;
            i++;
        }
    }

    private void setDictInfo(byte defaultLanguage[], int len) {
        DictFileInfo dictFile = dictConstInstance.fileInfo;
        int n = 0;
        dictFile.defaultLanguage = appUtility.setlong4(defaultLanguage, n);
        n += 4;
        dictFile.Property = appUtility.setlong4(defaultLanguage, n);
        n += 4;
        dictFile.wordMaxBytes = appUtility.setlong4(defaultLanguage, n);
        n += 4;
        dictFile.ContentMaxBytes = appUtility.setlong4(defaultLanguage, n);
        n += 4;
        dictFile.SoundMaxBytes = appUtility.setlong4(defaultLanguage, n);
        n += 4;
        dictFile.wordCount = appUtility.setlong4(defaultLanguage, n);
        n += 4;
        dictFile.MaxFileSize = appUtility.setlong4(defaultLanguage, n);
        n += 4;
        dictFile.wordFiles = appUtility.setint2(defaultLanguage, n);
        n += 2;
        dictFile.contentFiles = appUtility.setint2(defaultLanguage, n);
        n += 2;
        dictFile.sndDatFiles = appUtility.setint2(defaultLanguage, n);
        n += 2;
        dictFile.sndIdxFiles = appUtility.setint2(defaultLanguage, n);
        n += 2;
        dictFile.ChrBeforeContent = (char) appUtility.setint2(defaultLanguage, n);
        n += 2;
        dictFile.DictNum = appUtility.setint2(defaultLanguage, n);
        n += 2;
        dictFile.flagCnt = appUtility.setshort1(defaultLanguage, n);
        n += 1;
        dictFile.IndexWidth = appUtility.setshort1(defaultLanguage, n);
        n += 1;
        dictFile.SoundType = appUtility.setshort1(defaultLanguage, n);
        n += 1;
        for (int j = 0; j < constValue.MAX_COMBINE_DICT_NUM; j++) {
            dictFile.sdOffset[j] = appUtility.setlong4(defaultLanguage, n);
            n += 4;
        }
    }

    private int _FindFileNumByNO(DictFileInfo dictFile, boolean bWordFile, long wordNO, int nums) {
        int low, high, cur;
        long val;
        low = 0;
        high = nums;
        cur = (low + high) / 2;
        while (cur != low && cur != high) {
            if (bWordFile) {
                val = dictFile.StartWordNum[cur];
            } else {
                val = dictFile.starContentNum[cur];
            }
            if (val == wordNO) return cur; else if (val > wordNO) high = cur; else low = cur;
            cur = (low + high) / 2;
        }
        return cur;
    }

    private int SDGetWordByNO_A(long wordNO, boolean removeFont) {
        DictFileInfo dictFile = dictConstInstance.fileInfo;
        int FileNO = _FindFileNumByNO(dictFile, true, wordNO, dictFile.wordFiles);
        if (FileNO != dictFile.CurWordFileNO) {
            dictFile.CurWordFileNO = FileNO;
            Integer i = new Integer(FileNO);
            String str = new String(dictFile.MainFileName + "w" + i.toString() + ".dat");
            dictFile.pfCurWord = str;
            if (fcWord != null) {
                fcWord.closeDict();
                fcWord = null;
            }
            fcWord = new DictFile();
            if (fcWord.openDict(str) == false) {
                return 0;
            }
        }
        long tmpa = dictFile.StartWordNum[dictFile.CurWordFileNO];
        long tmpb = dictFile.StartWordNum[dictFile.CurWordFileNO + 1];
        long indexAreaSize = (tmpb - tmpa + 1) * dictFile.IndexWidth;
        tmpa = wordNO - tmpa;
        int randomOffset = dictFile.randomOffset + (int) tmpa * dictFile.IndexWidth;
        InputStream is = null;
        int skipSize = (int) tmpa * dictFile.IndexWidth;
        byte tmpB[] = new byte[2 * dictFile.IndexWidth];
        try {
            is = fcWord.getIn();
            is.skip(skipSize);
            is.read(tmpB, 0, 2 * dictFile.IndexWidth);
        } catch (Exception e) {
            System.out.println(e.toString());
            return 0;
        }
        skipSize += 2 * dictFile.IndexWidth;
        randomOffset = addRandomTable(tmpB, tmpB.length, randomOffset, dictFile);
        tmpa = 0;
        tmpb = 0;
        tmpa = appUtility.byte2longB(tmpB, 0, dictFile.IndexWidth);
        tmpb = appUtility.byte2longB(tmpB, dictFile.IndexWidth, dictFile.IndexWidth);
        tmpb -= tmpa;
        if (tmpb > dictConstInstance.fileInfo.wordMaxBytes) {
            tmpb = dictConstInstance.fileInfo.wordMaxBytes;
        }
        appUtility.memsetBytes(wordBytes);
        skipSize = (int) (tmpa + indexAreaSize) - skipSize;
        try {
            is.skip(skipSize);
            is.read(wordBytes, 0, (int) tmpb);
            is.close();
            is = null;
        } catch (Exception e) {
            System.out.println(e.toString());
            return 0;
        }
        randomOffset = (int) (tmpa + indexAreaSize + dictFile.randomOffset);
        addRandomTable(wordBytes, (int) tmpb, randomOffset, dictFile);
        if (removeFont) {
            tmpb = _removeFontFlag(wordBytes);
        }
        return (int) tmpb;
    }

    private int _removeFontFlag(byte buf[]) {
        int i = 0;
        int j = 0;
        while ((i * 2 < buf.length) && ((buf[i * 2] != 0) || (buf[i * 2 + 1] != 0))) {
            if (buf[i * 2] == 0xF8) {
                i++;
            } else {
                buf[2 * j] = buf[i * 2];
                buf[2 * j + 1] = buf[i * 2 + 1];
                i++;
                j++;
            }
        }
        return 2 * j;
    }

    private int DICT_GetContentToBuf(int index) {
        int wordLen = 0, intTmp = 0;
        int refWordNO;
        int i = 0;
        if ((dictConstInstance.fileInfo.Property & constValue.DICT_FILE_ADDWORD) != 0 || (dictConstInstance.fileInfo.versionReserve == constValue.VER_RES_OOSNDIC)) {
            if (index == -1) {
                wordLen = SDGetWordByNO_A(dictConstInstance.curWordNO, false);
                appUtility.charCopyFrombyte(dictConstInstance.pDICT_ContentBuf, wordBytes, wordLen);
            } else {
                wordLen = wordList[index].length();
                dictConstInstance.pDICT_ContentBuf.append(wordList[index]);
            }
        }
        if ((dictConstInstance.fileInfo.Property & constValue.DICT_FILE_ADDCHRBFCONT) != 0) {
            wordLen++;
            dictConstInstance.pDICT_ContentBuf.append(dictConstInstance.fileInfo.ChrBeforeContent);
        } else if ((dictConstInstance.fileInfo.Property & constValue.DICT_FILE_ADDNULL) != 0) {
        } else {
            wordLen++;
            dictConstInstance.pDICT_ContentBuf.append("\n");
        }
        intTmp = SDGetContentByNO(dictConstInstance.curWordNO, (int) dictConstInstance.fileInfo.ContentMaxBytes - wordLen * 2, dictConstInstance.CurTabNO);
        int tmpLen = constValue.FLAG_REFWORD.length;
        StringBuffer tmpBuf = new StringBuffer(tmpLen);
        appUtility.stringBufCpy2(tmpBuf, wordLen, tmpContentBuf, 0, tmpLen * 2);
        for (i = 0; i < tmpLen; i++) {
            if (tmpBuf.charAt(i) != constValue.FLAG_REFWORD[i]) {
                break;
            }
        }
        if (i == tmpLen) {
            refWordNO = 0;
            for (intTmp = 0; intTmp < 8; intTmp++) {
                if (tmpBuf.charAt(intTmp) >= '0' && tmpBuf.charAt(intTmp) <= '9') refWordNO = (refWordNO << 4) + (tmpBuf.charAt(intTmp) - '0'); else if (tmpBuf.charAt(intTmp) >= 'a' && tmpBuf.charAt(intTmp) <= 'z') refWordNO = (refWordNO << 4) + (tmpBuf.charAt(intTmp) - 'a' + 10); else if (tmpBuf.charAt(intTmp) >= 'A' && tmpBuf.charAt(intTmp) <= 'Z') refWordNO = (refWordNO << 4) + (tmpBuf.charAt(intTmp) - 'A' + 10);
            }
            dictConstInstance.pDICT_ContentBuf.append((char) 0x21d2);
            wordLen++;
            dictConstInstance.pDICT_ContentBuf.append((char) 0x000a);
            wordLen++;
            _checkDicTab(refWordNO);
            if ((dictConstInstance.fileInfo.Property & constValue.DICT_FILE_ADDWORD) != 0) {
                intTmp = SDGetWordByNO_A(refWordNO, false);
                intTmp /= 2;
                appUtility.stringBufCpyBytes(dictConstInstance.pDICT_ContentBuf, wordLen, wordBytes, intTmp);
            }
            wordLen += intTmp;
            if ((dictConstInstance.fileInfo.Property & constValue.DICT_FILE_ADDCHRBFCONT) != 0) {
                dictConstInstance.pDICT_ContentBuf.append(dictConstInstance.fileInfo.ChrBeforeContent);
                wordLen++;
            } else if ((dictConstInstance.fileInfo.Property & constValue.DICT_FILE_ADDNULL) != 0) {
            } else {
                dictConstInstance.pDICT_ContentBuf.append("\n");
                wordLen++;
            }
            intTmp = SDGetContentByNO((long) refWordNO, (int) (dictConstInstance.fileInfo.ContentMaxBytes - wordLen * 2), dictConstInstance.CurTabNO);
            wordLen += appUtility.stringBufAppendFromByte(dictConstInstance.pDICT_ContentBuf, wordLen, tmpContentBuf);
        } else {
            wordLen += appUtility.stringBufAppendFromByte(dictConstInstance.pDICT_ContentBuf, wordLen, tmpContentBuf);
        }
        tmpBuf = null;
        tmpContentBuf = null;
        return wordLen;
    }

    private void _checkDicTab(int wordNO) {
        short tabNO = 0;
        if (dictConstInstance.fileInfo.DictNum <= 1) {
            dictConstInstance.CurTabNO = 0;
            return;
        }
        if (true && (dictConstInstance.CurTabNO != 0)) {
            tabNO = dictConstInstance.CurTabNO;
        } else {
            tabNO = 0;
            for (tabNO = 0; tabNO < dictConstInstance.fileInfo.DictNum; tabNO++) {
                if (tabNO != 0) {
                    break;
                }
            }
            if (tabNO >= dictConstInstance.fileInfo.DictNum) {
                tabNO = 0;
            }
        }
        dictConstInstance.CurTabNO = tabNO;
        return;
    }

    private int BinInputBits(byte[] buf, long startBit, int expBits) {
        byte tmp[] = new byte[4];
        if (expBits == 1) if ((buf[0] & (1 << startBit)) > 0) return 1; else return 0; else appUtility.byteStrCpy(tmp, 0, buf, 0, 3);
        int ret = (int) appUtility.byte2long(tmp);
        ret = (ret >> startBit) & ((1 << expBits) - 1);
        return ret;
    }

    private int SDGetContentByNO(long wordNO, int maxLength, short DictNO) {
        DictFileInfo dictFile = dictConstInstance.fileInfo;
        int FileNO = _FindFileNumByNO(dictFile, false, wordNO, dictFile.contentFiles);
        {
            dictFile.curContFileNO = FileNO;
            Integer i = new Integer(FileNO + 1);
            String str = new String(dictFile.MainFileName + "c" + i.toString() + ".dat");
            dictFile.pfCurContent = str;
            if (fcCont != null) {
                fcCont.closeDict();
                fcCont = null;
            }
            fcCont = new DictFile();
            if (fcCont.openDict(str) == false) {
                return 0;
            }
        }
        long indexAreaSize = (dictFile.starContentNum[dictFile.curContFileNO + 1] - dictFile.starContentNum[dictFile.curContFileNO] + 1) * dictFile.IndexWidth;
        long tmpa = wordNO - dictFile.starContentNum[dictFile.curContFileNO];
        int randomOffset = (int) (dictFile.randomOffset + tmpa * dictFile.IndexWidth);
        InputStream is = null;
        byte tmpBa[] = new byte[dictFile.IndexWidth];
        byte tmpBb[] = new byte[dictFile.IndexWidth];
        long skipSize = tmpa * dictFile.IndexWidth;
        try {
            is = fcCont.getIn();
            is.skip(skipSize);
            is.read(tmpBa, 0, dictFile.IndexWidth);
            is.read(tmpBb, 0, dictFile.IndexWidth);
        } catch (Exception e) {
            System.out.println(e.toString());
            return 0;
        }
        skipSize += 2 * dictFile.IndexWidth;
        randomOffset = addRandomTable(tmpBa, tmpBa.length, randomOffset, dictFile);
        randomOffset = addRandomTable(tmpBb, tmpBb.length, randomOffset, dictFile);
        tmpa = 0;
        tmpa = appUtility.byte2longB(tmpBa, 0, dictFile.IndexWidth);
        long tmpb = 0;
        tmpb = appUtility.byte2longB(tmpBb, 0, dictFile.IndexWidth);
        tmpBa = null;
        tmpBb = null;
        long startBit;
        long totalBits;
        long offset = 0;
        byte pStartBuf[];
        startBit = (int) (tmpa & 0x07);
        totalBits = tmpb - tmpa;
        if (totalBits == 0) {
            tmpContentBuf = null;
            tmpContentBuf = new byte[2];
            tmpContentBuf[0] = 0;
            tmpContentBuf[1] = 0;
            return 2;
        }
        tmpa >>= 3;
        tmpb >>= 3;
        tmpb = tmpb - tmpa + 1;
        tmpa = tmpa - dictFile.contentOffset[dictFile.curContFileNO];
        skipSize = tmpa + indexAreaSize - skipSize;
        try {
            is.skip(skipSize);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        if ((dictFile.versionMain == constValue.CURRENT_MAIN_VERSION) || (dictFile.versionMain == constValue.OPEN_DICT_MAIN_VERSION)) {
            byte tmpB1[] = new byte[1];
            try {
                is.read(tmpB1, 0, 1);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            if (BinInputBits(tmpB1, startBit, 1) == 0) {
                tmpa++;
                totalBits /= 8;
                tmpContentBuf = null;
                tmpContentBuf = new byte[(int) totalBits];
                try {
                    is.read(tmpContentBuf, 0, (int) totalBits);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
                tmpa += dictFile.contentOffset[dictFile.curContFileNO] + dictFile.randomOffset;
                addRandomTable(tmpContentBuf, (int) totalBits, (int) tmpa, dictFile);
                return (int) totalBits;
            }
            tmpContentBuf = null;
            tmpContentBuf = new byte[(int) totalBits];
            tmpContentBuf[0] = tmpB1[0];
            tmpB1 = null;
            startBit++;
            totalBits--;
            if (tmpContentBuf.length == tmpb) {
                offset = constValue.RESERVED_CACHEBYTES;
            } else {
                offset = tmpContentBuf.length - tmpb;
            }
            tmpContentBuf[(int) offset - 1] = tmpContentBuf[0];
            pStartBuf = new byte[(int) tmpb];
            pStartBuf[0] = tmpContentBuf[0];
            if (startBit >= 8) {
                startBit = 0;
                try {
                    is.read(pStartBuf, 0, (int) tmpb - 1);
                    is.close();
                    is = null;
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            } else {
                try {
                    is.read(pStartBuf, 1, (int) tmpb - 1);
                    is.close();
                    is = null;
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }
        } else {
            tmpContentBuf[0] = 0;
            tmpContentBuf[1] = 0;
            return 2;
        }
        tmpb = expendData(dictFile, pStartBuf, startBit, totalBits, tmpContentBuf, DictNO);
        pStartBuf = null;
        tmpContentBuf[(int) tmpb + 0] = 0;
        tmpContentBuf[(int) tmpb + 1] = 0;
        tmpContentBuf[(int) tmpb + 2] = 0;
        if ((dictFile.flagCnt != 0) && constValue.VER_RES_OOSNDIC != dictFile.versionReserve) {
            convertFlagInBuf(tmpContentBuf, (short) 65535);
        }
        return (int) tmpb;
    }

    private void convertFlagInBuf(byte[] ucOriStr, short dispFlag) {
        short scanPos;
        short bottomPos;
        scanPos = 0;
        bottomPos = 0;
        while ((ucOriStr[2 * scanPos] != 0x00) && (ucOriStr[2 * scanPos + 1] != 0x00)) {
            if ((ucOriStr[2 * scanPos + 1] >= 0x00) && (ucOriStr[2 * scanPos + 1] <= 0x7F) && (ucOriStr[2 * scanPos] == 0xF0)) {
                if (((1 << (int) (ucOriStr[2 * scanPos + 1])) & (int) (dispFlag)) > 0) {
                    scanPos++;
                } else {
                    scanPos++;
                    while ((ucOriStr[2 * scanPos] != 0x00) && (ucOriStr[2 * scanPos + 1] != 0x00)) {
                        if ((ucOriStr[2 * scanPos + 1] >= 0x00) && (ucOriStr[2 * scanPos + 1] <= 0x7F) && (ucOriStr[2 * scanPos] == 0xF0)) {
                            break;
                        } else {
                            scanPos++;
                        }
                    }
                }
            } else {
                ucOriStr[2 * bottomPos++] = ucOriStr[2 * scanPos++];
                ucOriStr[2 * bottomPos++ + 1] = ucOriStr[2 * scanPos++ + 1];
            }
        }
        ucOriStr[2 * bottomPos] = ucOriStr[2 * scanPos];
        ucOriStr[2 * bottomPos + 1] = ucOriStr[2 * scanPos + 1];
    }

    private int expendData(DictFileInfo dictFile, byte buf[], long startBit, long totalBits, byte tarBuf[], short DictNO) {
        int udTmp;
        int udMatchlength;
        int randomOffset;
        int i = 0;
        int j = 0;
        byte tmpByte[] = new byte[4];
        while (totalBits > 0) {
            appUtility.byteStrCpy(tmpByte, 0, buf, i, (buf.length - i) < 3 ? (buf.length - i) : 3);
            udTmp = BinInputBits(tmpByte, startBit, 1);
            startBit += 1;
            if (startBit >= 8) {
                startBit -= 8;
                i++;
            }
            totalBits -= 1;
            if (udTmp == 1) {
                appUtility.byteStrCpy(tmpByte, 0, buf, i, (buf.length - i) < 3 ? (buf.length - i) : 3);
                tarBuf[j++] = (byte) BinInputBits(tmpByte, startBit, 8);
                startBit += 8;
                totalBits -= 8;
            } else {
                appUtility.byteStrCpy(tmpByte, 0, buf, i, (buf.length - i) < 3 ? (buf.length - i) : 3);
                udTmp = BinInputBits(tmpByte, startBit, dictFile.treeNodeBits[DictNO]);
                startBit += dictFile.treeNodeBits[DictNO];
                totalBits -= dictFile.treeNodeBits[DictNO];
                while (startBit >= 8) {
                    startBit -= 8;
                    i++;
                }
                appUtility.byteStrCpy(tmpByte, 0, buf, i, (buf.length - i) < 3 ? (buf.length - i) : 3);
                udMatchlength = BinInputBits(tmpByte, startBit, dictFile.matchByteBits[DictNO]) + dictFile.BreakEvnBytes[DictNO];
                startBit += dictFile.matchByteBits[DictNO];
                totalBits -= dictFile.matchByteBits[DictNO];
                randomOffset = udTmp + dictFile.randomOffset;
                InputStream is = null;
                byte tmpB[] = new byte[udMatchlength];
                try {
                    is = fcDict.getIn();
                    is.skip(udTmp + dictFile.sdOffset[DictNO]);
                    is.read(tmpB, 0, udMatchlength);
                    is.close();
                    is = null;
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
                addRandomTable(tmpB, tmpB.length, randomOffset, dictFile);
                appUtility.byteStrCpy(tarBuf, j, tmpB, 0, udMatchlength);
                tmpB = null;
                j += udMatchlength;
            }
            while (startBit >= 8) {
                startBit -= 8;
                i++;
            }
        }
        tmpByte = null;
        return j;
    }

    private int SDGetSoundByNO(long wordNO, short DictNO) {
        DictFileInfo dictFile = dictConstInstance.fileInfo;
        int tmpa;
        long tmpb;
        if (dictFile.SoundMaxBytes == 0) {
            return 0;
        }
        tmpa = (int) (wordNO / (dictFile.MaxFileSize / dictFile.IndexWidth));
        wordNO = wordNO % (dictFile.MaxFileSize / dictFile.IndexWidth);
        while (wordNO >= (constValue.MAX_FILESIZE / dictFile.IndexWidth)) {
            wordNO -= constValue.MAX_FILESIZE / dictFile.IndexWidth;
            tmpa++;
        }
        String str = new String(dictFile.MainFileName + "s" + Integer.toString(tmpa) + ".dat");
        dictFile.pfCurWord = str;
        if (fcSund != null) {
            fcSund.closeDict();
            fcSund = null;
        }
        fcSund = new DictFile();
        if (fcSund.openDict(str) == false) {
            return 0;
        }
        tmpb = wordNO * dictFile.IndexWidth;
        InputStream is = null;
        byte tmpB[] = new byte[dictFile.IndexWidth];
        try {
            is = fcSund.getIn();
            is.skip(tmpb);
            is.read(tmpB, 0, dictFile.IndexWidth);
            is.close();
            is = null;
        } catch (Exception e) {
            System.out.println(e.toString());
            return 0;
        }
        tmpb += dictFile.randomOffset;
        addRandomTable(tmpB, tmpB.length, (int) tmpb, dictFile);
        tmpa = (int) appUtility.byte2longB(tmpB, 0, dictFile.IndexWidth);
        tmpB = null;
        if (fcSund != null) {
            fcSund.closeDict();
            fcSund = null;
        }
        return tmpa;
    }

    private int SDLoadSoundDataByIdx(long idxValue) {
        DictFileInfo dictFile = dictConstInstance.fileInfo;
        long tmpa;
        long tmpb;
        tmpb = (idxValue & 0xFF000000) >> 24;
        String str = new String(dictFile.MainFileName + "s" + Integer.toString((int) tmpb) + ".dat");
        dictFile.pfCurWord = str;
        if (fcSund != null) {
            fcSund.closeDict();
            fcSund = null;
        }
        fcSund = new DictFile();
        if (fcSund.openDict(str) == false) {
            return 0;
        }
        tmpa = idxValue & 0x00FFFFFF;
        tmpb = tmpa + dictFile.randomOffset;
        InputStream is = null;
        byte tmpB[] = new byte[constValue.PER_SOUNDSIZE_WIDTH];
        try {
            is = fcSund.getIn();
            is.skip(tmpa);
            is.read(tmpB, 0, constValue.PER_SOUNDSIZE_WIDTH);
        } catch (Exception e) {
            System.out.println(e.toString());
            return 0;
        }
        tmpb = addRandomTable(tmpB, tmpB.length, (int) tmpb, dictFile);
        tmpa = (int) appUtility.byte2longB(tmpB, 0, constValue.PER_SOUNDSIZE_WIDTH);
        tmpB = null;
        soundbuf = null;
        soundbuf = new byte[(int) tmpa];
        try {
            is.read(soundbuf, 0, (int) tmpa);
            is.close();
            is = null;
        } catch (Exception e) {
            System.out.println(e.toString());
            return 0;
        }
        addRandomTable(soundbuf, soundbuf.length, (int) tmpb, dictFile);
        if (fcSund != null) {
            fcSund.closeDict();
            fcSund = null;
        }
        return (int) tmpa;
    }
}

class indexInfo {

    int startPos;

    int endPos;

    long wordNum;
}
