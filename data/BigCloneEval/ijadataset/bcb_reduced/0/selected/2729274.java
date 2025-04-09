package com.aide.simplification.popedom.popedom.base;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.directwebremoting.annotations.RemoteMethod;
import com.aide.simplification.global.Utils;
import com.aide.simplification.popedom.login.LoginUser;
import com.aide.simplification.popedom.login.PopSSO;

/**
 * 
 * 权限运算
 * 
 * @version 2.1
 * @author sorc
 * @date 2008-11-11
 */
public class PopedomOP {

    public static javax.servlet.http.HttpServletRequest request;

    public static javax.servlet.http.HttpServlet servlet;

    public static javax.servlet.http.HttpServletResponse response;

    public static javax.servlet.http.HttpSession session;

    /**
	 * 根据权限数组得到权限值
	 * 
	 * @param pops
	 * @return
	 */
    public BigInteger getPop(int[] pops) {
        BigInteger pop = BigInteger.ZERO;
        for (int i = 0; i < pops.length; i++) {
            int j = pops[i];
            pop = pop.add(BigInteger.valueOf(2).pow(j));
        }
        return pop;
    }

    /**
	 * 根据权限数组得到权限值
	 * 
	 * @param pops
	 * @return
	 */
    public BigInteger getPop(String[] pops) {
        BigInteger pop = BigInteger.ZERO;
        for (int i = 0; i < pops.length; i++) {
            int j = 0;
            try {
                j = Integer.parseInt(pops[i]);
            } catch (Exception e) {
            }
            pop = pop.add(BigInteger.valueOf(2).pow(j));
        }
        return pop;
    }

    /**
	 * 根据权限数组得到权限值 36进制
	 * 
	 * @param pops
	 * @return
	 */
    public String getPop36(int[] pops) {
        return getPop(pops).toString(36);
    }

    /**
	 * 根据权限数组得到权限值 36进制
	 * 
	 * @param pops
	 * @return
	 */
    public String getPop36(String[] pops) {
        return getPop(pops).toString(36);
    }

    /**
	 * 将2个权限值合并为一个权限值 36进制
	 * 
	 * @param pop1
	 *            36进制
	 * @param pop2
	 *            36进制
	 * @return
	 */
    public String unitePop36(String pop1, String pop2) {
        BigInteger pops1 = new BigInteger("".equals(pop1) ? "1" : pop1, 36);
        BigInteger pops2 = new BigInteger("".equals(pop2) ? "1" : pop2, 36);
        return pops1.or(pops2).toString(36);
    }

    /**
	 * 将一个权限值减去另一个权限值 36进制
	 * 
	 * @param pop1
	 *            36进制
	 * @param pop2
	 *            36进制
	 * @return
	 */
    public String reducePop36(String pop1, String pop2) {
        BigInteger pops1 = new BigInteger("".equals(pop1) ? "1" : pop1, 36);
        BigInteger pops2 = new BigInteger("".equals(pop2) ? "1" : pop2, 36);
        BigInteger pop = (pops1.or(pops2)).xor(pops2);
        return pop.toString(36);
    }

    /**
	 * 根据权限值得到权限值数组
	 * 
	 * @param pop
	 */
    public int[] getPops(BigInteger pop) {
        BigInteger temppop = pop;
        int[] pops = new int[temppop.bitCount()];
        for (int i = 0; i < pops.length; i++) {
            pops[i] = temppop.getLowestSetBit();
            temppop = temppop.subtract(BigInteger.valueOf(2).pow(pops[i]));
        }
        return pops;
    }

    /**
	 * 根据权限值得到权限值数组
	 * 
	 * @param pop
	 *            36进制
	 */
    public int[] getPops36(String pop) {
        return this.getPops(new BigInteger("".equals(pop) ? "1" : pop, 36));
    }

    /**
	 * 根据单个权限值获得权限id
	 * 
	 * @param popsign
	 * @return
	 */
    public int getPopid(int popsign) {
        return popsign - popsign % 4;
    }

    /**
	 * 根据单个权限值获得操作id
	 * 
	 * @param popsign
	 * @return
	 */
    public int getOpid(int popsign) {
        return popsign % 4;
    }

    /**
	 * 根据单个权限值获得操作名称
	 * 
	 * @param popsign
	 * @return
	 */
    public String getOpname(int popsign) {
        String opname = "";
        switch(getOpid(popsign)) {
            case 0:
                opname = "查(浏览)";
            case 1:
                opname = "改(修改)";
            case 2:
                opname = "增(添加)";
            case 3:
                opname = "删(删除)";
        }
        return opname;
    }

    /**
	 * 是否有所有权限
	 * 
	 * @param userpop
	 *            36进制
	 * @param pops
	 *            36进制
	 * @return
	 */
    @RemoteMethod
    public boolean checkPopAnd(String pagepop, String userpop) {
        BigInteger pagepopi = new BigInteger("".equals(pagepop) ? "1" : pagepop, 36);
        BigInteger userpopi = new BigInteger("".equals(userpop) ? "1" : userpop, 36);
        ;
        return this.checkPopAnd(pagepopi, userpopi);
    }

    public boolean checkPopAndByUser(String pagepop, HttpServletRequest request, HttpServletResponse response) {
        PopSSO sso = new PopSSO(request, response);
        LoginUser lu = sso.ssoSync();
        if (lu != null && Utils.getConfig("pop.admin").equals(lu.getUseruuid())) {
            return true;
        }
        return this.checkPopAnd(pagepop, lu.getPop());
    }

    public boolean checkPopAnd(BigInteger pagepopi, BigInteger userpopi) {
        return pagepopi.and(userpopi).equals(pagepopi);
    }

    /**
	 * 是否有所有权限
	 * 
	 * @param userpop
	 *            36进制
	 * @param pops
	 *            36进制
	 * @return
	 */
    @RemoteMethod
    public boolean checkPopOr(String pagepop, String userpop) {
        BigInteger pagepopi = new BigInteger("".equals(pagepop) ? "1" : pagepop, 36);
        BigInteger userpopi = new BigInteger("".equals(userpop) ? "1" : userpop, 36);
        return this.checkPopOr(pagepopi, userpopi);
    }

    public boolean checkPopOrByUser(String pagepop, HttpServletRequest request, HttpServletResponse response) {
        PopSSO sso = new PopSSO(request, response);
        LoginUser lu = sso.ssoSync();
        if (lu != null && Utils.getConfig("pop.admin").equals(lu.getUseruuid())) {
            return true;
        }
        return this.checkPopOr(pagepop, lu.getPop());
    }

    /**
	 * 是否有其中一个权限
	 * 
	 * @param userpop
	 * @param pops
	 * @return
	 */
    public boolean checkPopOr(BigInteger pagepopi, BigInteger userpopi) {
        return pagepopi.or(userpopi).equals(pagepopi);
    }

    public String toRadix(String str, int radix) {
        char[] baseStrs = new char[94];
        for (int i = 0; i < baseStrs.length; i++) {
            baseStrs[i] = (char) (i + 33);
        }
        String tempStr = "";
        String strtemp = str;
        BigInteger b_str = new BigInteger(strtemp, 10);
        BigInteger b_radix = new BigInteger("" + radix, 10);
        BigInteger b_temp = new BigInteger("0", 10);
        if (baseStrs.length >= radix && radix > 1) {
            while (!strtemp.equals("") && !strtemp.equals("0") && b_str.compareTo(b_radix) == 1) {
                b_str = new BigInteger(strtemp, 10);
                int temp = b_str.mod(b_radix).intValue();
                tempStr = baseStrs[temp] + tempStr;
                b_temp = new BigInteger("" + temp, 10);
                strtemp = (b_str.subtract(b_temp)).divide(b_radix).toString();
            }
            return tempStr;
        } else {
            return "";
        }
    }

    public String toStr(String str, int radix) {
        char[] baseStrs = new char[94];
        for (int i = 0; i < baseStrs.length; i++) {
            baseStrs[i] = (char) (i + 33);
        }
        String tempStr = "";
        if (baseStrs.length >= radix && radix > 1) {
            while (!str.equals("") && new BigInteger(str, 10).compareTo(new BigInteger("" + radix, 10)) == 1) {
                BigInteger bint = new BigInteger(str, 10);
                int temp = bint.mod(new BigInteger("" + radix, 10)).intValue();
                tempStr = baseStrs[temp] + tempStr;
                str = (bint.subtract(new BigInteger("" + temp, 10))).divide(new BigInteger("" + radix, 10)).toString();
            }
            return tempStr;
        } else {
            return "";
        }
    }

    public String zipbin(String bin) throws IOException {
        byte[] bytes = bin.getBytes();
        OutputStream bos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(bos);
        zos.setMethod(ZipOutputStream.DEFLATED);
        zos.putNextEntry(new ZipEntry("zip"));
        DataOutputStream os = new DataOutputStream(zos);
        os.write(bytes);
        os.close();
        System.out.println(bos.toString());
        return bos.toString();
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        PopedomOP op = new PopedomOP();
        String maxstring = "";
        for (int i = 0; i < 10000; i++) {
            maxstring += "1";
        }
        BigInteger bint = new BigInteger(maxstring, 2);
        System.out.println(bint.toString(2).length());
        System.out.println(bint.toString(36).length());
        String str36 = bint.toString(10);
        String strx = op.toRadix(str36, 94);
        System.out.println(strx);
        System.out.println(strx.length());
    }
}
