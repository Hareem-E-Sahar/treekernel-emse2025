package com.chinacache.snp.train.common;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import net.sf.json.JSONObject;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FileUtils;

/**
 * @author huanran.chen
 */
public class CommonsDemo {

    public static void main(String[] args) {
        decimalFormatTest();
    }

    /**
     * 数字基础应用
     */
    public static void numberBasicTest() {
        String str = "123.456";
        double d = Double.parseDouble(str);
        BigDecimal bd = new BigDecimal(d);
        int scale = 2;
        bd = bd.setScale(scale, BigDecimal.ROUND_HALF_UP);
        System.out.println(d + " scale to " + bd.doubleValue());
    }

    /**
     * 数字格式化
     */
    public static void decimalFormatTest() {
        DecimalFormat df1 = new DecimalFormat("000.000");
        DecimalFormat df2 = new DecimalFormat("###.###");
        DecimalFormat df3 = new DecimalFormat("0.000%");
        DecimalFormat df4 = new DecimalFormat(",###.###");
        System.out.println(df1.format(12.345678));
        System.out.println(df2.format(12.345578));
        System.out.println(df3.format(.0012));
        System.out.println(df4.format(12345678.56789));
    }

    /**
     * 随机数
     */
    public static void randTest() {
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            System.out.println(rand.nextInt(100));
        }
    }

    /**
     * Map 基础
     */
    public static void mapTest() {
        Map<String, House> map = new HashMap<String, House>();
        House house = new House(HouseType.SALE, "天河", 100);
        map.put("a", house);
        map.put("b", null);
        map.put(null, house);
        House nullHouse = map.get(null);
        System.out.println("null house: " + JSONObject.fromObject(nullHouse));
        nullHouse.setArea(200);
        House aHouse = map.get("a");
        System.out.println("a house: " + JSONObject.fromObject(aHouse));
    }

    /**
     * Map 跟 Bean 的转换
     */
    @SuppressWarnings("unchecked")
    public static void mapToBeanTest() {
        try {
            House house = new House(HouseType.RENT, "海珠", 120);
            Map<String, Object> propertyMap = PropertyUtils.describe(house);
            System.out.println("\n\npropertyMap: ");
            for (Entry<String, Object> entry : propertyMap.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue() + ", " + entry.getValue().getClass());
            }
            Map<String, String> beanMap = BeanUtils.describe(house);
            System.out.println("\n\nbeanMap: ");
            for (Entry<String, String> entry : beanMap.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue() + ", " + entry.getValue().getClass());
            }
            House newHouse = new House();
            BeanUtils.populate(newHouse, propertyMap);
            System.out.println(JSONObject.fromObject(newHouse));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件基本操作
     */
    public static void fileTest() {
        String path = "/home/test/abcd.txt";
        File file = new File(path);
        System.out.println("isfile:" + file.isFile());
        if (file.exists()) {
            if (file.isFile()) {
                System.out.println("status: file");
                try {
                    String str = FileUtils.readFileToString(file);
                    System.out.println("content: \n" + str);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("status: dir");
                File[] children = file.listFiles();
                if (children != null) {
                    for (File f : children) {
                        if (f.isFile()) {
                            System.out.print("file:");
                        } else if (f.isDirectory()) {
                            System.out.print("dir:");
                        }
                        System.out.println(f.getAbsolutePath());
                    }
                } else {
                    System.out.println("no children~");
                }
            }
        } else {
            System.out.println("status: lost");
            System.out.println("delete on lost: " + file.delete());
        }
    }

    /**
     * 日期、字符串转换
     */
    public static void dateFormatTest() {
        String formatLong = "yyyy-MM-dd";
        String formatCn = "yyyy年MM月dd日";
        SimpleDateFormat sdfLong = new SimpleDateFormat(formatLong);
        SimpleDateFormat sdfCn = new SimpleDateFormat(formatCn);
        try {
            String dateStr = "2011-02-14";
            Date date = sdfLong.parse(dateStr);
            System.out.println(sdfCn.format(date));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Calendar 字段含义
     */
    public static void calFieldTest() {
        System.out.println(String.format("%3d%3d%3d", Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.SUNDAY));
        Calendar cal = Calendar.getInstance();
        cal.set(2010, 7, 21);
        System.out.println(cal.getTime());
        System.out.println(cal.get(Calendar.DAY_OF_WEEK));
        System.out.println(cal.get(Calendar.DAY_OF_MONTH));
        System.out.println(cal.get(Calendar.DAY_OF_YEAR));
        System.out.println(cal.get(Calendar.WEEK_OF_MONTH));
        System.out.println(cal.get(Calendar.WEEK_OF_YEAR));
        System.out.println(cal.get(Calendar.DAY_OF_WEEK_IN_MONTH));
        cal.set(Calendar.DATE, 0);
        System.out.println(cal.getTime());
        cal.set(2010, 7, 31);
        cal.add(Calendar.MONTH, -2);
        System.out.println(cal.getTime());
    }

    /**
     * 日历操作
     */
    public static void calOperTest() {
        Calendar cal = Calendar.getInstance();
        int rollValue = 0;
        switch(cal.get(Calendar.DAY_OF_WEEK)) {
            case 1:
                rollValue = -2;
                break;
            case 7:
                rollValue = -1;
                break;
        }
        cal.roll(Calendar.DATE, rollValue);
        System.out.println(cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DATE));
    }

    /**
     * 枚举
     */
    public static void enumTest() {
        String shortName = "r";
        HouseType houseType = HouseType.fromShortName(shortName);
        switch(houseType) {
            case SALE:
                System.out.println("it's for sale.");
                break;
            case RENT:
                System.out.println("it's for rent.");
                break;
        }
    }
}
