public class IntegerNumber {

    public IntegerNumber() {
        _val = 0;
    }

    public IntegerNumber(int val) {
        _val = val;
    }

    public int absoluteValue() {
        int temp = _val;
        if (temp < 0) temp = -1 * _val;
        return temp;
    }

    public int getValue() {
        return _val;
    }

    public boolean isPrime() {
        int maximumPrime = (int) java.lang.Math.sqrt(_val);
        if (_val <= 0) return false; else if (_val <= 3) return true; else if (_val % 2 == 0 || _val % 3 == 0 || _val % 5 == 0) return false; else for (int i = 7; i <= maximumPrime; i = i + 2) if (_val % i == 0) return false;
        return true;
    }

    public int minimumCommonMultiplier(int number) {
        IntegerNumber rtn = new IntegerNumber(_val * number / maximumCommonDenominator(number));
        return rtn.absoluteValue();
    }

    public int maximumCommonDenominator(int number) {
        int copyA = absoluteValue();
        int copyB = (new IntegerNumber(number)).absoluteValue();
        int temp = 0;
        while (copyB != 0) {
            temp = copyA % copyB;
            copyA = copyB;
            copyB = temp;
        }
        return copyA;
    }

    public int minimumCommonDenominator(int number) {
        int denominator = (new IntegerNumber(number)).absoluteValue();
        int residue = absoluteValue();
        while (residue != 0) {
            int dividend = denominator;
            denominator = residue;
            residue = dividend % denominator;
        }
        return denominator;
    }

    private int _val;
}
