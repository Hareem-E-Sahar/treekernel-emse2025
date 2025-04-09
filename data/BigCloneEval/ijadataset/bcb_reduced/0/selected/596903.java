package teachin.domain.randomlib.data.dataGeneration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import org.apache.commons.lang.ArrayUtils;
import teachin.domain.randomlib.data.CountryCodes;

/** 
 * A class providing methods for generating random data for basic data types.
 * 
 * @author robertam
 */
public class BasicDataGenerator {

    /**
     * Random Number Generator 
     */
    public static final Random generator = new Random();

    /**
     * ArrayList to ensure unique object return.
     */
    private static ArrayList<Object> set = new ArrayList<Object>();

    /**
     * Generates a random alphanumeric string.
     * 
     * @param len The length of the string to be generated.
     * @return A random alphanumeric string.
     */
    public static String generateRandomString(int len) {
        return generateRandomString(len, false);
    }

    /**
     * Generates a unique random alphanumeric string.
     * 
     * @param len The length of the string to be generated.
     * @param unique Overloaded method to generate unique Strings.  If true returns a unique value.
     * @return A unique random alphanumeric string.
     */
    public static String generateRandomString(int len, boolean unique) {
        int maxLen = Math.min(len, 1000);
        StringBuffer sb = null;
        if (unique) {
            do {
                sb = new StringBuffer(maxLen);
                for (int i = 0; i < maxLen; i++) {
                    if (generator.nextBoolean()) {
                        sb.append((char) (generator.nextInt(26) + 65));
                    } else {
                        sb.append(generator.nextInt(10));
                    }
                }
            } while (set.contains(sb.toString()));
            set.add(sb.toString());
        } else {
            sb = new StringBuffer(maxLen);
            for (int i = 0; i < maxLen; i++) {
                if (generator.nextBoolean()) {
                    sb.append((char) (generator.nextInt(26) + 65));
                } else {
                    sb.append(generator.nextInt(10));
                }
            }
        }
        return sb.toString();
    }

    /**
     * Generates a random string, made up of characters.
     * 
     * @param len The length of the string to be generated.
     * @return A random string, made up of characters.
     */
    public static String generateRandomStringChar(int len) {
        return generateRandomStringChar(len, true);
    }

    /**
     * Generates a unique random string, made up of characters.
     * 
     * @param len The length of the string to be generated.
     * @param unique Overloaded method to generate unique Strings.  If true returns a unique value.
     * @return A unique random string, made up of characters.
     */
    public static String generateRandomStringChar(int len, boolean unique) {
        int maxLen = Math.min(len, 1000);
        StringBuffer sb = null;
        if (unique) {
            do {
                sb = new StringBuffer(maxLen);
                for (int i = 0; i < maxLen; i++) {
                    sb.append((char) (generator.nextInt(26) + 65));
                }
            } while (set.contains(sb.toString()));
            set.add(sb.toString());
        } else {
            sb = new StringBuffer(maxLen);
            for (int i = 0; i < maxLen; i++) {
                sb.append((char) (generator.nextInt(26) + 65));
            }
        }
        return sb.toString();
    }

    /**
     * Generates a random string, made up of special characters.
     * 
     * @param len The length of the string to be generated.
     * @return A random string, made up of special characters.
     */
    public static String generateRandomSpecialChar(int len) {
        return generateRandomSpecialChar(len, true);
    }

    /**
     * Generates a unique random string, made up of special characters.
     * 
     * @param len The length of the string to be generated.
     * @param unique Overloaded method to generate unique Strings.  If true returns a unique value.
     * @return A unique random string, made up of special characters.
     */
    public static String generateRandomSpecialChar(int len, boolean unique) {
        int maxLen = Math.min(len, 1000);
        StringBuffer sb = null;
        if (unique) {
            do {
                sb = new StringBuffer(maxLen);
                for (int i = 0; i < maxLen; i++) {
                    sb.append((char) (generator.nextInt(14) + 33));
                }
            } while (set.contains(sb.toString()));
            set.add(sb.toString());
        } else {
            sb = new StringBuffer(maxLen);
            for (int i = 0; i < maxLen; i++) {
                sb.append((char) (generator.nextInt(14) + 33));
            }
        }
        return sb.toString();
    }

    /**
     * Generates a random string, made up of numbers.
     * 
     * @param len The length of the string to be generated.
     * @return A random string, made up of numbers.
     */
    public static String generateRandomNumericString(int len) {
        return generateRandomNumericString(len, true);
    }

    /**
     * Generates a unique random string, made up of numbers.
     * 
     * @param len The length of the string to be generated.
     * @param unique Overloaded method to generate unique Strings.  If true returns a unique value.
     * @return A unique random string, made up of numbers.
     */
    public static String generateRandomNumericString(int len, boolean unique) {
        int maxLen = Math.min(len, 1000);
        StringBuffer sb = null;
        if (unique) {
            do {
                sb = new StringBuffer(maxLen);
                for (int i = 0; i < maxLen; i++) {
                    sb.append(generator.nextInt(10));
                }
            } while (set.contains(sb.toString()));
            set.add(sb.toString());
        } else {
            sb = new StringBuffer(maxLen);
            for (int i = 0; i < maxLen; i++) {
                sb.append(generator.nextInt(10));
            }
        }
        return sb.toString();
    }

    /**
     * Generate a random integer within the specified range and
     * returns it as a Long object 
     * @param upperLimit Upper bound of range   
     * @return  A random integer within range casted to Long
     */
    public static Long generateRandomLong(int upperLimit) {
        return generateRandomLong(upperLimit, true);
    }

    /**
     * Generate a unique random integer within the specified range and
     * returns it as a Long object 
     * @param upperLimit Upper bound of range   
     * @param unique Overloaded method to generate unique Strings.  If true returns a unique value.
     * @return  A unique random integer within range casted to Long
     */
    public static Long generateRandomLong(int upperLimit, boolean unique) {
        Long randomLong = null;
        if (unique) {
            do {
                randomLong = (long) generator.nextInt(upperLimit);
            } while (set.contains(randomLong));
            set.add(randomLong);
        } else {
            randomLong = (long) generator.nextInt(upperLimit);
        }
        return randomLong;
    }

    /**
     * Generate a random Long 
     *
     * @return  A random Long
     */
    public static Long generateRandomLong() {
        return generateRandomLong(true);
    }

    /**
     * Generate a unique random Long 
     *
     * @param unique Overloaded method to generate unique Strings.  If true returns a unique value.
     * @return  A unique random Long
     */
    public static Long generateRandomLong(boolean unique) {
        Long randomLong = null;
        if (unique) {
            do {
                randomLong = (long) generator.nextInt(214748);
            } while (set.contains(randomLong));
            set.add(randomLong);
        } else {
            randomLong = (long) generator.nextInt(214748);
        }
        return randomLong;
    }

    /**
     * Generate a random Integer within the specified range 
     * @param upperLimit 
     *
     * @return  A random Integer with respect to the specified upper bound.
     */
    public static Integer generateRandomInt(int upperLimit) {
        return generateRandomInt(upperLimit, true);
    }

    /**
     * Generate a unique random Integer within the specified range 
     * @param upperLimit 
     * @param unique Overloaded method to generate unique Strings.  If true returns a unique value.
     * @return  A unique random Integer with respect to the specified upper bound.
     */
    public static Integer generateRandomInt(int upperLimit, boolean unique) {
        Integer randomInteger = null;
        if (unique) {
            do {
                randomInteger = generator.nextInt(upperLimit + 1);
            } while (set.contains(randomInteger));
            set.add(randomInteger);
        } else {
            randomInteger = generator.nextInt(upperLimit + 1);
        }
        return randomInteger;
    }

    /**
     * Generate a random Integer; Upper bound 2147483647 
     *
     * @return  A random Integer
     */
    public static Integer generateRandomInt() {
        return generateRandomInt(false);
    }

    /**
     * Generate a unique random Integer; Upper bound 2147483647 
     * @param unique Overloaded method to generate unique Strings.  If true returns a unique value.
     * @return  A unique random Integer
     */
    public static Integer generateRandomInt(boolean unique) {
        Integer randomInteger = null;
        if (unique) {
            do {
                randomInteger = generator.nextInt(21474);
            } while (set.contains(randomInteger));
            set.add(randomInteger);
        } else {
            randomInteger = generator.nextInt(21474);
        }
        return randomInteger;
    }

    /**
     * Generate a random Integer; Upper bound 65535 
     *
     * @return  A random Integer
     */
    public static Byte generateRandomTinyInt() {
        return generateRandomTinyInt(false);
    }

    /**
     * Generate a random tinyint; 0..127
     * @param unique Overloaded method to generate unique Strings.  If true returns a unique value.
     * @return  A random tinyint
     */
    public static Byte generateRandomTinyInt(boolean unique) {
        return (byte) (Math.abs(generateRandomSmallInt(unique) & 0x7F));
    }

    /**
     * Generate a random Integer; Upper bound 65535 
     *
     * @return  A random Integer
     */
    public static Integer generateRandomSmallInt() {
        return generateRandomSmallInt(false);
    }

    /**
     * Generate a unique random Integer; Upper bound 65535 
     * @param unique Overloaded method to generate unique Strings.  If true returns a unique value.
     * @return  A unique random Integer
     */
    public static Integer generateRandomSmallInt(boolean unique) {
        Integer randomInteger = null;
        if (unique) {
            do {
                randomInteger = generator.nextInt();
            } while (set.contains(randomInteger));
            set.add(randomInteger);
        } else {
            randomInteger = generator.nextInt();
        }
        return randomInteger;
    }

    /**
     * Generate a random Integer and casted to a Long
     * Upper bound 16777215 
     *
     * @return  A random Integer cased to a Long
     */
    public static Long generateRandomMediumInt() {
        return generateRandomMediumInt(false);
    }

    /**
     * Generate a unique random Integer and casted to a Long
     * Upper bound 16777215 
     * @param unique Overloaded method to generate unique Strings.  If true returns a unique value.
     * @return  A unique random Integer cased to a Long
     */
    public static Long generateRandomMediumInt(boolean unique) {
        Long randomLong = null;
        if (unique) {
            do {
                randomLong = Long.valueOf(generator.nextInt(16777215));
            } while (set.contains(randomLong));
            set.add(randomLong);
        } else {
            randomLong = Long.valueOf(generator.nextInt(16777215));
        }
        return randomLong;
    }

    /**
     * Generate a random Float number 
     * 
     * @return A random Float
     */
    public static Float generateRandomFloat() {
        return generateRandomFloat(false);
    }

    /**
     * Generate a unique random Float number 
     * @param unique Overloaded method to generate unique Strings.  If true returns a unique value.
     * @return A unique random Float
     */
    public static Float generateRandomFloat(boolean unique) {
        Float randomFloat = null;
        if (unique) {
            do {
                randomFloat = generator.nextFloat();
            } while (set.contains(randomFloat));
            set.add(randomFloat);
        } else {
            randomFloat = generator.nextFloat();
        }
        return randomFloat;
    }

    /**
     * Generate a random Byte
     * 
     * @return A random Byte
     */
    public static Byte generateRandomByte() {
        return generateRandomByte(false);
    }

    /**
     * Generate a unique random Byte 
     * @param unique Overloaded method to generate unique Strings.  If true returns a unique value.
     * @return A unique random Byte
     */
    public static Byte generateRandomByte(boolean unique) {
        Byte randomByte = null;
        if (unique) {
            do {
                randomByte = (byte) generator.nextInt(99);
            } while (set.contains(randomByte));
            set.add(randomByte);
        } else {
            randomByte = (byte) generator.nextInt(99);
        }
        return randomByte;
    }

    /**
     * Generate a random Double 
     *
     * @return  A random Double
     */
    public static Double generateRandomDouble() {
        return generateRandomDouble(false);
    }

    /**
     * Generate a unique random Double number 
     * @param unique Overloaded method to generate unique Strings.  If true returns a unique value.
     * @return A unique random Double
     */
    public static Double generateRandomDouble(boolean unique) {
        Double randomDouble = null;
        if (unique) {
            do {
                randomDouble = generator.nextDouble();
            } while (set.contains(randomDouble));
            set.add(randomDouble);
        } else {
            randomDouble = generator.nextDouble();
        }
        return randomDouble;
    }

    /**
     * Generate a random BigDecimal 
     * @param integral The number of integers present in the decimal number. (Example: integral being 10 would result in a decimal like 103667.5576 or 11.32547569)
     * @param fraction The number of integers present in the decimal, after the decimal point. (Example: fraction being 2 would result in a decimal like 3.55 or 77844.39)
     * @return  A random BigDecimal
     */
    public static BigDecimal generateRandomDecimal(int integral, int fraction) {
        return generateRandomDecimal(integral, fraction, false);
    }

    /**
     * Generate a unique random BigDecimal number 
     * @param integral The number of integers present in the decimal number. (Example: integral being 10 would result in a decimal like 103667.5576 or 11.32547569)
     * @param fraction The number of integers present in the decimal, after the decimal point. (Example: fraction being 2 would result in a decimal like 3.55 or 77844.39)
     * @param unique Overloaded method to generate unique Strings.  If true returns a unique value.
     * @return A unique random BigDecimal
     */
    public static BigDecimal generateRandomDecimal(int integral, int fraction, boolean unique) {
        String integralString = null;
        String fractionString = null;
        String decimalString = null;
        BigDecimal randomBigDecimal = null;
        do {
            integralString = generateRandomNumericString(integral - fraction);
            fractionString = generateRandomNumericString(fraction);
            decimalString = integralString + "." + fractionString;
            randomBigDecimal = new BigDecimal(decimalString);
        } while (unique && set.contains(randomBigDecimal));
        if (unique) {
            set.add(randomBigDecimal);
        }
        return randomBigDecimal;
    }

    /**
     * Returns a random country code in the form of 3 digits
     * 
     * @return  3-digit country code
     */
    public static Integer generateNumericCountryCode() {
        return generateNumericCountryCode(false);
    }

    /**
     * Returns a unique random country code in the form of 3 digits
     * @param unique Overloaded method to generate unique Strings.  If true returns a unique value.
     * @return Unique 3-digit country code
     */
    public static Integer generateNumericCountryCode(boolean unique) {
        Integer countryCode = null;
        do {
            int choice = generator.nextInt(CountryCodes.getCountryCodesNumLength());
            countryCode = Integer.parseInt(CountryCodes.getCountryCodesNum(choice));
        } while (unique && set.contains(countryCode));
        if (unique) {
            set.add(countryCode);
        }
        return countryCode;
    }

    /**
     * Returns a random country code in the form of 2 letters 
     * 
     * @return  2-letter country code
     */
    public static String generateCountryCode() {
        return generateCountryCode(false);
    }

    /**
     * Returns a random country code in the form of 2 letters 
     * @param unique Overloaded method to generate unique Strings.  If true returns a unique value.
     * @return Unique 2-letter country code
     */
    public static String generateCountryCode(boolean unique) {
        String countryCode = null;
        if (unique) {
            do {
                int choice = generator.nextInt(CountryCodes.getCountryCodesAcLength());
                countryCode = CountryCodes.getCountryCodesAc(choice);
            } while (set.contains(countryCode));
            set.add(countryCode);
        } else {
            int choice = generator.nextInt(CountryCodes.getCountryCodesAcLength());
            countryCode = CountryCodes.getCountryCodesAc(choice);
        }
        return countryCode;
    }

    /**
     * Generate random Binary data
     * 
     * @param count Number of bytes.
     * @return  A Byte array filled with Random data.  
     */
    public static Byte[] generateRandomBinary(int count) {
        return generateRandomBinary(count, false);
    }

    /**
     * Generate unique random Binary data
     * @param count Number of bytes.
     * @param unique Overloaded method to generate unique Strings.  If true returns a unique value.
     * @return A unique Byte array filled with Random data.  
     */
    public static Byte[] generateRandomBinary(int count, boolean unique) {
        byte[] data = new byte[Math.min(100000, count)];
        if (unique) {
            do {
                generator.nextBytes(data);
            } while (set.contains(data));
            set.add(data);
        } else {
            generator.nextBytes(data);
        }
        return ArrayUtils.toObject(data);
    }

    /**
     * Generate a random Boolean
     *
     * @return  A random Boolean
     */
    public static Boolean generateRandomBoolean() {
        return generator.nextBoolean();
    }

    /**
     * Given an enum a single element of the enum is returned.
     * @param currentEnum   An enum from which to select an element 
     * @return A single element of a given enum
     */
    public static Enum<?> generateRandomEnum(Enum<?>[] currentEnum) {
        int x = generator.nextInt(currentEnum.length);
        return currentEnum[x];
    }

    /**
     * Generate a date object.
     * 
     * @return Date
     */
    public static java.sql.Timestamp generateDate() {
        Date date = new Date();
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new java.sql.Timestamp(cal.getTime().getTime());
    }

    /**
     * Generate a future date object.
     * @param presentDate 
     * 
     * @return Future date
     */
    public static java.sql.Timestamp generateRandomFutureDate(Date presentDate) {
        Date date = new Date(presentDate.getTime() + generateRandomLong());
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new java.sql.Timestamp(cal.getTime().getTime());
    }

    /**
     * Generate a past date object.
     * @param presentDate 
     * 
     * @return Past date
     */
    public static java.sql.Timestamp generateRandomPastDate(Date presentDate) {
        Date date = new Date(presentDate.getTime() - generateRandomLong());
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new java.sql.Timestamp(cal.getTime().getTime());
    }

    /**
       * Given a path to the class where the enums are, a single random enum is returned.
       * @param <T> The class to be used, which extends the enum to be returned.
       * @param className The full path of the Class with enums
       * @return A random enum from the class
       */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<?>> T generateRandomEnumFromClass(String className) {
        Class<T> loadedClass = null;
        try {
            loadedClass = (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return (generateRandomEnumFromClass(loadedClass));
    }

    /**
     * Given a class with enums, a single random enum is returned.
     * @param <T> The class to be used, which extends the enum to be returned.
     * @param c The class containing the enums 
     * @return A random enum from the class
     */
    public static <T extends Enum<?>> T generateRandomEnumFromClass(Class<T> c) {
        T[] enums = c.getEnumConstants();
        ArrayList<T> enumList = new ArrayList<T>();
        for (T enumObject : enums) {
            enumList.add(enumObject);
        }
        int choice = generator.nextInt(enumList.size());
        T enumToReturn = enumList.get(choice);
        return enumToReturn;
    }
}
