package egovframework.com.utl.fcc.service;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EgovNumberUtil {

    /**
     * 특정숫자 집합에서 랜덤 숫자를 구하는 기능 시작숫자와 종료숫자 사이에서 구한 랜덤 숫자를 반환한다
     *
     * @param startNum - 시작숫자
     * @param endNum - 종료숫자
     * @return 랜덤숫자
     * @exception MyException
     * @see
     */
    public static int getRandomNum(int startNum, int endNum) {
        int randomNum = 0;
        try {
            SecureRandom rnd = new SecureRandom();
            do {
                randomNum = rnd.nextInt(endNum + 1);
            } while (randomNum < startNum);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return randomNum;
    }

    /**
     * 특정 숫자 집합에서 특정 숫자가 있는지 체크하는 기능 12345678에서 7이 있는지 없는지 체크하는 기능을 제공함
     *
     * @param sourceInt - 특정숫자집합
     * @param searchInt - 검색숫자
     * @return 존재여부
     * @exception MyException
     * @see
     */
    public static Boolean getNumSearchCheck(int sourceInt, int searchInt) {
        String sourceStr = String.valueOf(sourceInt);
        String searchStr = String.valueOf(searchInt);
        if (sourceStr.indexOf(searchStr) == -1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 숫자타입을 문자열로 변환하는 기능 숫자 20081212를 문자열 '20081212'로 변환하는 기능
     *
     * @param srcNumber
     *            - 숫자
     * @return 문자열
     * @exception MyException
     * @see
     */
    public static String getNumToStrCnvr(int srcNumber) {
        String rtnStr = null;
        try {
            rtnStr = String.valueOf(srcNumber);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return rtnStr;
    }

    /**
     * 숫자타입을 데이트 타입으로 변환하는 기능
     * 숫자 20081212를 데이트타입  '2008-12-12'로 변환하는 기능
     * @param srcNumber - 숫자
     * @return String
     * @exception MyException
     * @see
     */
    public static String getNumToDateCnvr(int srcNumber) {
        String pattern = null;
        String cnvrStr = null;
        String srcStr = String.valueOf(srcNumber);
        if (srcStr.length() != 8 && srcStr.length() != 14) {
            throw new IllegalArgumentException("Invalid Number: " + srcStr + " Length=" + srcStr.trim().length());
        }
        if (srcStr.length() == 8) {
            pattern = "yyyyMMdd";
        } else if (srcStr.length() == 14) {
            pattern = "yyyyMMddhhmmss";
        }
        SimpleDateFormat dateFormatter = new SimpleDateFormat(pattern, Locale.KOREA);
        Date cnvrDate = null;
        try {
            cnvrDate = dateFormatter.parse(srcStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        cnvrStr = String.format("%1$tY-%1$tm-%1$td", cnvrDate);
        return cnvrStr;
    }

    /**
     * 체크할 숫자 중에서 숫자인지 아닌지 체크하는 기능
     * 숫자이면 True, 아니면 False를 반환한다
     * @param checkStr - 체크문자열
     * @return 숫자여부
     * @exception MyException
     * @see
     */
    public static Boolean getNumberValidCheck(String checkStr) {
        int i;
        int checkStrLt = checkStr.length();
        try {
            for (i = 0; i < checkStrLt; i++) {
                if (checkStr.charAt(i) > 47 && checkStr.charAt(i) < 58) {
                    continue;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * 특정숫자를 다른 숫자로 치환하는 기능 숫자 12345678에서 123를 999로 변환하는 기능을 제공(99945678)
     *
     * @param srcNumber - 숫자집합
     * @param cnvrSrcNumber - 원래숫자
     * @param cnvrTrgtNumber - 치환숫자
     * @return 치환숫자
     * @exception MyException
     * @see
     */
    public static int getNumberCnvr(int srcNumber, int cnvrSrcNumber, int cnvrTrgtNumber) {
        String source = String.valueOf(srcNumber);
        String subject = String.valueOf(cnvrSrcNumber);
        String object = String.valueOf(cnvrTrgtNumber);
        StringBuffer rtnStr = new StringBuffer();
        String preStr = "";
        String nextStr = source;
        try {
            while (source.indexOf(subject) >= 0) {
                preStr = source.substring(0, source.indexOf(subject));
                nextStr = source.substring(source.indexOf(subject) + subject.length(), source.length());
                source = nextStr;
                rtnStr.append(preStr).append(object);
            }
            rtnStr.append(nextStr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Integer.parseInt(rtnStr.toString());
    }

    /**
     * 특정숫자가 실수인지, 정수인지, 음수인지 체크하는 기능 123이 실수인지, 정수인지, 음수인지 체크하는 기능을 제공함
     *
     * @param srcNumber - 숫자집합
     * @return -1(음수), 0(정수), 1(실수)
     * @exception MyException
     * @see
     */
    public static int checkRlnoInteger(double srcNumber) {
        String cnvrString = null;
        if (srcNumber < 0) {
            return -1;
        } else {
            cnvrString = String.valueOf(srcNumber);
            if (cnvrString.indexOf(".") == -1) {
                return 0;
            } else {
                return 1;
            }
        }
    }
}
