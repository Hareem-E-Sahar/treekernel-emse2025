package org.mad.math;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MadMath {

    public static int ROUND_UP = BigDecimal.ROUND_UP;

    public static int ROUND_DOWN = BigDecimal.ROUND_DOWN;

    public static double INCH_CENTI_METER_CONVERSION = 2.54d;

    public static double FEET_METER_CONVERSION = 0.3048d;

    public static double SPEED_OF_LIGHT_METERS_PER_SECOND = 299792458;

    /**
	 * Add and returns the sum of all Integers in a List
	 * 
	 * @param List
	 *            of Integers
	 * @return Sum of all Integers in the List
	 */
    public static int getSumFromList(List<Integer> integers) {
        int total = 0;
        for (Integer i : integers) {
            total += i;
        }
        return total;
    }

    /**
	 * Add and returns the sum of all Integers in an array
	 * 
	 * @param Array
	 *            of int
	 * @return Sum of all Integers in the Array
	 */
    public static int getSumFromArray(int[] integers) {
        int total = 0;
        for (int i = 0; i < integers.length; i++) {
            total += integers[i];
        }
        return total;
    }

    /**
	 * Adds list of integers and returns average
	 * 
	 * @param List
	 *            of Integers
	 * @return Average of all Integers in List
	 */
    public static int getAverageFromList(List<Integer> integers) {
        int total = getSumFromList(integers);
        int average = total / integers.size();
        return average;
    }

    /**
	 * Returns average of all integers in array
	 * 
	 * @param Array
	 *            of int
	 * @return Average of all integers in Array
	 */
    public static int getAverageFromArray(int[] integers) {
        int total = getSumFromArray(integers);
        int average = total / integers.length;
        return average;
    }

    /**
	 * Sum of all doubles in List
	 * 
	 * @param List
	 *            of doubles
	 * @return Sum of all doubles in the List
	 */
    public static double getSumFromList(List<Double> doubles) {
        double total = 0.0;
        for (double d : doubles) {
            total += d;
        }
        return total;
    }

    /**
	 * Sum of all doubles in an array
	 * 
	 * @param Array
	 *            of doubles
	 * @return Sum of all doubles in an array
	 */
    public static double getSumFromArray(double[] doubles) {
        double total = 0.0;
        for (int i = 0; i < doubles.length; i++) {
            total += doubles[i];
        }
        return total;
    }

    /**
	 * Average of doubles in a List
	 * 
	 * @param List
	 *            of Double objects
	 * @return Average of all Doubles in a List
	 */
    public static double getAverageFromList(List<Double> d) {
        double total = getSumFromList(d);
        return total / d.size();
    }

    /**
	 * Average of all doubles in a array of doubles
	 * 
	 * @param d
	 * @return
	 */
    public static double getAverageFromArray(double[] d) {
        double total = getSumFromArray(d);
        return total / d.length;
    }

    /**
	 * Average of doubles rounded up or down
	 * 
	 * @param List
	 *            of doubles
	 * @param int round place, 2 would round to second decimal place
	 * @param ROUND_UP
	 *            or ROUND_DOWN
	 * @return average of all doubles in the List rounded up or down
	 */
    public static double getAverageFromListRounded(List<Double> d, int roundPlace, int roundUpOrDown) {
        double average = getAverageFromList(d);
        return roundDouble(average, roundPlace, roundUpOrDown);
    }

    /**
	 * Average of doubles rounded up or down
	 * 
	 * @param Array
	 *            of doubles
	 * @param int round place, 2 would round to second decimal place
	 * @param ROUND_UP
	 *            or ROUND_DOWN
	 * @return average of all doubles in the Array rounded up or down
	 */
    public static double getAverageFromArrayRounded(double[] d, int roundPlace, int roundUpOrDown) {
        double average = getAverageFromArray(d);
        return roundDouble(average, roundPlace, roundUpOrDown);
    }

    /**
	 * Rounds a double
	 * 
	 * @param double to round
	 * @param int round place, 2 would round to second decimal place
	 * @param ROUND_UP
	 *            or ROUND_DOWN
	 * @return
	 */
    public static double roundDouble(double d, int place, int roundWay) {
        BigDecimal bd = new BigDecimal(d);
        bd = bd.setScale(place, roundWay);
        return bd.doubleValue();
    }

    /**
	 * square a double
	 * 
	 * @param double
	 * @return squared value of the given double
	 */
    public static double doubleSquared(double d) {
        return Math.pow(d, 2.0);
    }

    /**
	 * square an int
	 * 
	 * @param int
	 * @return squared value of given int
	 */
    public static int intSquared(int i) {
        return i * i;
    }

    /**
	 * double cubed
	 * 
	 * @param double
	 * @return a double cubed
	 */
    public static double doubleCubed(double d) {
        return Math.pow(d, 3.0);
    }

    /**
	 * int cubed
	 * 
	 * @param int
	 * @return cubed value of an int
	 */
    public static int intCubed(int i) {
        return i * i * i;
    }

    /**
	 * find the area of a circle radius squared multiplied by PI
	 * 
	 * @param double radius
	 * @return double value of the area
	 */
    public static double getCircleArea(double radius) {
        return doubleSquared(radius) * Math.PI;
    }

    /**
	 * find the circumference of a circle 2 * PI * radius
	 * 
	 * @param double radius
	 * @return double circumference
	 */
    public static double getCircleCircumferenceUsingRadius(double radius) {
        return 2.0d * Math.PI * radius;
    }

    /**
	 * find the circumference of a circle diameter * PI
	 * 
	 * @param double diameter
	 * @return double circumference
	 */
    public static double getCircleCircumferenceUsingDiameter(double diameter) {
        return Math.PI * diameter;
    }

    /**
	 * converts inches to centi-meters. There are 2.54 centi-meters per inch.
	 * 
	 * @param double inches
	 * @return double centimeters
	 */
    public static double convertInchesToCentiMeter(double inches) {
        return inches * INCH_CENTI_METER_CONVERSION;
    }

    /**
	 * converts feet into meters. there are 0.3048 meters per foot.
	 * 
	 * @param double feet
	 * @return double meters
	 */
    public static double convertFeetToMeters(double feet) {
        return feet * FEET_METER_CONVERSION;
    }

    /**
	 * Returns the prime factors of the provided integer example: List would
	 * contain 2, 2, 2 for the number 8
	 * 
	 * @param Integer
	 *            to factor
	 * @return List containing the factors
	 */
    public static List<Integer> primeFactor(int i) {
        List<Integer> result = new ArrayList<Integer>();
        if (i < 2) return result;
        while (!isPrime(i)) {
            boolean stop = false;
            int g = 2;
            while (!stop) {
                if (i % g == 0) {
                    i = i / g;
                    result.add(g);
                    stop = true;
                } else g++;
            }
        }
        result.add(i);
        Collections.sort(result);
        return result;
    }

    /**
	 * Returns the prime factors of the provided integer example: Array would
	 * contain 2, 2, 2 for the number 8
	 * 
	 * @param Integer
	 *            to factor
	 * @return Array containing the factors
	 */
    public static Integer[] primeFactorReturnArray(int i) {
        List<Integer> list = primeFactor(i);
        Integer a[] = new Integer[list.size()];
        return list.toArray(a);
    }

    /**
	 * Returns the factors of the provided integer example: List would contain
	 * 1, 2, 4 and 8 for the number 8
	 * 
	 * @param number
	 * @return
	 */
    public static List<Integer> factor(int number) {
        List<Integer> list = new ArrayList<Integer>();
        if (number < 1) {
            list.add(1);
            return list;
        }
        list.add(1);
        list.add(number);
        for (int i = 2; i < number; i++) {
            int mod = number % i;
            if (mod == 0) list.add(i);
        }
        Collections.sort(list);
        return list;
    }

    /**
	 * finds the greatest common factor of any 2 numbers
	 * 
	 * @param int one
	 * @param int two
	 * @return the greatest common factor
	 */
    public static int greatestCommonFactor(int one, int two) {
        List<Integer> gcfList = new ArrayList<Integer>();
        gcfList.add(one);
        gcfList.add(two);
        return greatestCommonFactor(gcfList);
    }

    /**
	 * finds the greatest common factor of a list of numbers
	 * 
	 * @param List
	 *            Integer
	 * @return greatest common factor
	 */
    public static int greatestCommonFactor(List<Integer> numbers) {
        List<List<Integer>> factors = new ArrayList<List<Integer>>();
        int smallest = 0;
        List<Integer> biggySmalls = null;
        for (Integer numberToFactor : numbers) {
            List<Integer> temps = primeFactor(numberToFactor);
            Collections.sort(temps);
            if (smallest == 0) {
                smallest = temps.size();
                biggySmalls = temps;
            } else if (temps.size() < smallest) {
                smallest = temps.size();
                biggySmalls = temps;
            }
            factors.add(temps);
        }
        List<Integer> uniques = new ArrayList<Integer>();
        Map<String, Integer> taken = new HashMap<String, Integer>();
        for (int i = 0; i < biggySmalls.size(); i++) {
            int testInt = biggySmalls.get(i);
            boolean hasTestInt = true;
            Map<String, Integer> tempMap = new HashMap<String, Integer>();
            int count = 0;
            for (List<Integer> list : factors) {
                count++;
                boolean hasI = false;
                int countTwo = 0;
                for (Integer localFactor : list) {
                    countTwo++;
                    if (localFactor.intValue() == testInt && taken.get(count + "AND" + countTwo) == null) {
                        tempMap.put(count + "AND" + countTwo, testInt);
                        hasI = true;
                        break;
                    }
                }
                if (!hasI) {
                    hasTestInt = false;
                    break;
                }
            }
            if (hasTestInt) {
                taken.putAll(tempMap);
                uniques.add(testInt);
            }
        }
        if (uniques.size() > 0) {
            int x = 1;
            for (Integer xx : uniques) x = x * xx;
            return x;
        }
        return 1;
    }

    /**
	 * finds the least common multiple of two numbers
	 * 
	 * @param int one
	 * @param int two
	 * @return least common multiple of two numbers
	 */
    public static int leastCommonMultiple(int one, int two) {
        int greatestCommonFactor = greatestCommonFactor(one, two);
        int leastCommonMultiple = (one * two) / greatestCommonFactor;
        return leastCommonMultiple;
    }

    /**
	 * finds the least common multiple of a list of numbers
	 * 
	 * @param List
	 *            of Integers
	 * @return least common multiple of the List
	 */
    public static int leastCommonMultiple(List<Integer> numbers) {
        int greatestCommonFactor = greatestCommonFactor(numbers);
        int x = 1;
        for (Integer i : numbers) {
            x = x * i;
        }
        return x / greatestCommonFactor;
    }

    /**
	 * calculates the fibonacci sequence x times. x depending on the parameter
	 * 
	 * @param size
	 *            of array or how many numbers to get
	 * @return int array of the fibonacci sequence.
	 */
    public static int[] fibonacci(int size) {
        int[] fib = new int[size];
        if (size == 0) return fib;
        fib[0] = 1;
        if (size == 1) return fib;
        fib[1] = 1;
        if (size == 2) return fib;
        for (int i = 2; i <= size - 1; i++) {
            fib[i] = fib[i - 1] + fib[i - 2];
        }
        return fib;
    }

    /**
	 * calculates the distance between two points (x1, y1)(x2, y2) distance =
	 * Square root of (x1 - x2)squared + (y1 - y2)squared
	 * 
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @return distance between two points
	 */
    public static double getCoordinateDistance(double x1, double x2, double y1, double y2) {
        double xx = doubleSquared((x1 - x2));
        double yy = doubleSquared((y1 - y2));
        double add = xx + yy;
        return Math.sqrt(add);
    }

    /**
	 * calculates the midpoint between two points (x,y) = ( ((x1 + x2) / 2),
	 * ((y1 + y2) / 2) )
	 * 
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @return double array, where double[0] is x and double[1] is y. Midpoint
	 *         is (x,y)
	 */
    public static double[] getMidpoint(double x1, double x2, double y1, double y2) {
        double xx = (x1 + x2) / 2;
        double yy = (y1 + y2) / 2;
        double[] answer = new double[2];
        answer[0] = xx;
        answer[1] = yy;
        return answer;
    }

    /**
	 * finds the slope but doesn't round the double slope = (y1 - y2) / (x1 -
	 * x2)
	 * 
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @return slope double value
	 */
    public static double getSlopeNoRound(double x1, double x2, double y1, double y2) {
        double yy = y1 - y2;
        double xx = x1 - x2;
        return yy / xx;
    }

    /**
	 * finds the slope and rounds to second digit after period slope = (y1 - y2)
	 * / (x1 - x2)
	 * 
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @return slope double value
	 */
    public static double getSlopeRounded(double x1, double x2, double y1, double y2) {
        double yy = y1 - y2;
        double xx = x1 - x2;
        double aa = yy / xx;
        return roundDouble(aa, 2, ROUND_UP);
    }

    /**
	 * finds the slope and returns result in a double array slope = (y1 - y2) /
	 * (x1 - x2)
	 * 
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @return double array where double[0] represents the y and double[1]
	 *         represents the x. In case you need the slope in y/x
	 */
    public static double[] getSlopeAsArray(double x1, double x2, double y1, double y2) {
        double yy = y1 - y2;
        double xx = x1 - x2;
        double[] answer = new double[2];
        answer[0] = yy;
        answer[1] = xx;
        return answer;
    }

    /**
	 * return true if integer is prime, or false otherwise
	 * 
	 * @param integer
	 * @return boolean
	 */
    public static boolean isPrime(int x) {
        if (x == 2) return true;
        if (x < 2) {
            return false;
        }
        if (isEven(x)) {
            return false;
        }
        int max = (int) Math.sqrt(x);
        for (int i = 3; i <= max; i = i + 2) {
            if ((x % i) == 0) {
                return false;
            }
        }
        return true;
    }

    /**
	 * return true if integer is even
	 * 
	 * @param integer
	 * @return boolean
	 */
    public static boolean isEven(int x) {
        if (x % 2 == 0) return true;
        return false;
    }

    /**
	 * returns true if integer is odd
	 * 
	 * @param integer
	 * @return boolean
	 */
    public static boolean isOdd(int x) {
        if (x % 2 > 0) return true;
        return false;
    }

    /**
	 * The total area T of any right prism is equal to two times the area of the
	 * base plus the lateral area. Formula: T = 2B + Ph B = length * width P =
	 * (2 * length) + (2 * width)
	 * 
	 * @param double length
	 * @param double width
	 * @param double height
	 * @return Area of a prism
	 */
    public static double getPrismArea(double length, double width, double height) {
        double b = length * width;
        double p = (2 * length) + (2 * width);
        double total = (2 * b) + (p * height);
        return total;
    }

    /**
	 * The volume V of any right prism is the product of B, the area of the
	 * base, and the height h of the prism. Formula: V = Bh B = length * width
	 * 
	 * @param double length
	 * @param double width
	 * @param double height
	 * @return volume of a prism
	 */
    public static double getPrismVolume(double length, double width, double height) {
        double b = length * width;
        double volume = b * height;
        return volume;
    }

    /**
	 * Use to find the value of a or b using pythagorean theorem a(2) + b(2) =
	 * c(2)
	 * 
	 * @param double a or b
	 * @param c
	 * @return a or b double value
	 */
    public static double pythagoreanTheoremFindAorB(Double b, Double c) {
        double bSquared = doubleSquared(b);
        double cSquared = doubleSquared(c);
        double aSquared = cSquared - bSquared;
        return Math.sqrt(aSquared);
    }

    /**
	 * Use to find the value of c using pythagorean theorem a(2) + b(2) = c(2)
	 * 
	 * @param a
	 * @param b
	 * @return value of c
	 */
    public static double pythagoreanTheoremFindC(Double a, Double b) {
        double aSquared = doubleSquared(a);
        double bSquared = doubleSquared(b);
        double abTotal = aSquared + bSquared;
        return Math.sqrt(abTotal);
    }

    /**
	 * find the area of a triangle given the base and height
	 * 
	 * @param base
	 * @param height
	 * @return
	 */
    public static double getTriangleArea(double base, double height) {
        return 0.5 * base * height;
    }

    /**
	 * A quadratic equation with real or complex coefficients has two solutions, called roots. 
	 * These two solutions may or may not be distinct, and they may or may not be real. 
	 * The roots are given by the quadratic formula:
	 * (-b +- SquareRoot of bSquared - 4ac) / 2a
	 * @param int a
	 * @param int b
	 * @param int c
	 * @return Integer Array where Integer[0] == + result and Integer[1] == - result
	 */
    public static Integer[] quadraticEquation(int a, int b, int c) {
        Integer[] results = new Integer[2];
        double squareRootPart = Math.sqrt(new Double(intSquared(b) - (4 * (a * c))).doubleValue());
        Double resultPositive = (new Double(b * -1).doubleValue() + squareRootPart) / (2 * a);
        results[0] = resultPositive.intValue();
        Double resultNegative = (new Double(b * -1).doubleValue() - squareRootPart) / (2 * a);
        results[1] = resultNegative.intValue();
        return results;
    }

    /**
	 * A quadratic equation with real or complex coefficients has two solutions, called roots. 
	 * These two solutions may or may not be distinct, and they may or may not be real. 
	 * The roots are given by the quadratic formula:
	 * (-b +- SquareRoot of bSquared - 4ac) / 2a
	 * @param int a
	 * @param int b
	 * @param int c
	 * @return Double Array where Double[0] == + result and Double[1] == - result
	 */
    public static Double[] quadraticEquation(double a, double b, double c) {
        Double[] results = new Double[2];
        double squareRootPart = Math.sqrt(doubleSquared(b) - (4 * (a * c)));
        Double resultPositive = ((b * -1) + squareRootPart) / (2 * a);
        results[0] = resultPositive;
        Double resultNegative = ((b * -1) - squareRootPart) / (2 * a);
        results[1] = resultNegative;
        return results;
    }

    /**
	 * Einsteins theory of relativity E = mc2
	 * @param double Mass in kilograms
	 * @return amount of energy in Joules
	 */
    public static double eEqualsmc2(double kilogramsMass) {
        return kilogramsMass * doubleSquared(SPEED_OF_LIGHT_METERS_PER_SECOND);
    }

    /**
	 * returns the base2 or binary for any given int or base10
	 * equivalent to Integer.toBinaryString
	 * @param int baseTen value
	 * @return String representation of the binary equivalent of the base ten int
	 */
    public static String getBinaryString(int baseTen) {
        int baseTwo = 1;
        StringBuffer sb = new StringBuffer();
        if (baseTen == 0) return "0";
        if (baseTen == 1) return "1";
        int count = 1;
        int lowest = 0;
        while (baseTwo < baseTen) {
            count++;
            baseTwo = baseTwo * 2;
            lowest = baseTwo;
        }
        for (int i = 0; i < count; i++) {
            if (lowest < 1) sb.append("0"); else if (lowest < baseTen) {
                baseTen = baseTen - lowest;
                sb.append("1");
                lowest = lowest / 2;
            } else if (lowest == baseTen) {
                sb.append("1");
                lowest = 0;
            } else if (lowest > baseTen) {
                lowest = lowest / 2;
                sb.append("0");
            }
        }
        String result = sb.toString();
        while (result.startsWith("0")) {
            result = result.substring(1, result.length());
        }
        return result;
    }
}
