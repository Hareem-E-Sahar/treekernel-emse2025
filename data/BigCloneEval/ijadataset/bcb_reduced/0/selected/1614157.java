package com.fangmou.ing.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 比特维数据结构.
 * User: liming
 * Date: 2011-3-21
 * Time: 15:11:21
 * 用一个比特序列的来表示多维数据，支持互斥数据，按原始值<code>ByteData#toOriginalString</code>或存储值<code>ByteData#toInt</code>输出，可以测试原始值逻辑表达式<code>ByteData#test</code>。
 * 通过定义维度可以无限扩展本数据结构，代码示例：
 * <pre>
 *     String[] names = new String[]{"Manager","Man/Women"};
 *     ByteData bd = new ByteData(names);
 *     //单维
 *     bd.set("Manager");
 *     bd.is("Manager");
 *     //互斥维度
 *     bd.set("Man/Women", "Man");
 *     bd.is("Man/Women", "Man");
 *     //表达式运算
 *     bd.test("Manager&&Man");
 * </pre>
 */
public class ByteData {

    private String[] names;

    private byte[] values;

    private int value;

    /**
     * 初始化数据结构
     *
     * @param names
     */
    public ByteData(String[] names) {
        this.names = names;
        int index = 0;
        for (int i = 0; i < this.names.length; i++) {
            String[] meta = names[i].split("/");
            index += meta.length;
        }
        this.values = new byte[index];
    }

    /**
     * 返回最终数据
     *
     * @return
     */
    public int toInt() {
        return value;
    }

    /**
     * 按最终数据格式化数据结构
     *
     * @param value
     */
    public void parseInt(int value) {
        this.value = value;
        String valuesS = Integer.toBinaryString(value);
        for (int i = valuesS.length() - 1, m = values.length - 1; i >= 0; i--, m--) {
            values[m] = Byte.parseByte(valuesS.charAt(i) + "");
        }
    }

    /**
     * 根据原始值字符串格式化数据结构
     *
     * @param value
     * @param sequenceIgnore 忽略顺序，为false时严格按照定义的数据结构初始化，为true时，则忽视顺序，但必须保证key没有重复
     */
    public void parseOriginalString(String value, boolean sequenceIgnore) {
        if (sequenceIgnore) {
            String[] keys = value.split(",");
            for (int k = 0; k < keys.length; k++) {
                String key = keys[k];
                for (int i = 0; i < names.length; i++) {
                    if (key.equals(names[i])) {
                        this.set(key, true);
                    } else if (names[i].contains(key)) {
                        this.set(names[i], key);
                    }
                }
            }
        }
    }

    /**
     * 设置互斥维度的值
     * 该维度只能设置一个值，所以当设置value时，其他非value全部都为否
     *
     * @param name
     * @param value
     */
    public void set(String name, String value) {
        if (name != null && name.indexOf(value) != -1) {
            int index = 0;
            for (int i = 0; i < this.names.length; i++) {
                String[] meta = names[i].split("/");
                if (name.equals(this.names[i])) {
                    boolean setted = false;
                    for (int j = 0; j < meta.length; j++) {
                        if (!setted && value.equals(meta[j])) {
                            this.values[index] = 1;
                            setted = true;
                        } else {
                            this.values[index] = 0;
                        }
                        index++;
                    }
                    reformValue();
                    break;
                } else {
                    index += meta.length;
                }
            }
        } else {
            throw new IllegalArgumentException("设置的值[" + value + "]不在互斥维度[" + name + "]里");
        }
    }

    /**
     * 重新计算值
     */
    private void reformValue() {
        String valueString = "";
        for (int m = 0; m < this.values.length; m++) {
            valueString += this.values[m];
        }
        this.value = Integer.parseInt(valueString, 2);
    }

    /**
     * 判断互斥维度的值
     *
     * @param name
     * @param value
     * @return
     */
    public boolean is(String name, String value) {
        if (name != null && name.indexOf(value) != -1) {
            int index = 0;
            for (int i = 0; i < this.names.length; i++) {
                String[] meta = names[i].split("/");
                if (name.equals(this.names[i])) {
                    for (int j = 0; j < meta.length; j++) {
                        if (value.equals(meta[j])) {
                            return this.values[index] == 1;
                        }
                        index++;
                    }
                } else {
                    index += meta.length;
                }
            }
        }
        throw new IllegalArgumentException("传入参数非法，互斥维度[" + name + "]和值[" + value + "]不符");
    }

    /**
     * 设置单维非互斥维度值
     *
     * @param value
     */
    public void set(String name, boolean value) {
        int index = 0;
        for (int i = 0; i < names.length; i++) {
            if (names[i].indexOf("/") > 0) {
                String[] meta = names[i].split("/");
                index += meta.length;
                continue;
            }
            if (name.equals(names[i])) {
                if (value) this.values[index] = 1; else this.values[index] = 0;
                reformValue();
                return;
            }
            index++;
        }
        throw new IllegalArgumentException("传入参数非法，无单维非互斥维度[" + name + "]");
    }

    /**
     * 判断单值非互斥维度判断
     *
     * @param value
     * @return
     */
    public boolean is(String value) {
        int index = 0;
        for (int i = 0; i < names.length; i++) {
            if (names[i].indexOf("/") > 0) {
                String[] meta = names[i].split("/");
                index += meta.length;
                continue;
            }
            if (value.equals(names[i])) {
                return this.values[index] == 1;
            }
            index++;
        }
        throw new IllegalArgumentException("传入参数非法，无单维非互斥维度[" + value + "]");
    }

    /**
     * 判断表达式是否成立
     *
     * @param expression 表达式 格式：&&与操作；||或操作;!非操作。优先级：!>&&>||。
     * @return
     */
    public boolean test(String expression) {
        if (expression != null) {
            String innerVar = "byteData";
            String regex = "\\w+";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(expression);
            StringBuffer javaExpression = new StringBuffer("");
            int index = 0;
            while (matcher.find()) {
                if (index == 0 && matcher.start() > 0) javaExpression.append(expression.substring(0, matcher.start())); else if (index > 0) javaExpression.append(expression.substring(index, matcher.start()));
                javaExpression.append(innerVar).append(".is(\"").append(matcher.group()).append("\")");
                index = matcher.end();
            }
            if (index < expression.length()) javaExpression.append(expression.substring(index));
            ExpressionCompute ep = new FreemarkerExpressionCompute();
            return ep.logicCompute(this, innerVar, javaExpression.toString());
        } else {
            return false;
        }
    }

    /**
     * 将数据结构按照元定义的意义展示
     *
     * @return
     */
    public String toOriginalString() {
        String r = "";
        int index = 0;
        for (int i = 0; i < this.names.length; i++) {
            String[] meta = names[i].split("/");
            for (int j = 0; j < meta.length; j++) {
                if (this.values[index] == 1) {
                    r += "," + meta[j];
                }
                index++;
            }
        }
        if (r.length() > 0) r = r.substring(1);
        return r;
    }
}
