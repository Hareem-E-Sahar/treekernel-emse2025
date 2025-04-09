import java.math.*;

public final class expression {

    private static final BigInteger BigInt1 = BigInteger.valueOf(1L);

    private static final BigInteger BigInt2 = BigInteger.valueOf(2L);

    private static final BigInteger BigInt3 = BigInteger.valueOf(3L);

    public static int ComputeExpression(String expr, int type, BigInteger ExpressionResult[]) {
        BigInteger BigInt1 = BigInteger.valueOf(1L);
        int stackIndex = 0;
        int exprIndex = 0;
        int exprLength = expr.length();
        int i, j;
        char charValue;
        boolean leftNumberFlag = false;
        int exprIndexAux;
        int SubExprResult, len;
        BigInteger factorial;
        BigInteger stackValues[] = new BigInteger[400];
        int stackOperators[] = new int[400];
        while (exprIndex < exprLength) {
            charValue = expr.charAt(exprIndex);
            if (charValue == '!') {
                if (leftNumberFlag == false) {
                    return -6;
                }
                len = stackValues[stackIndex].bitLength() - 1;
                if (len > 16) {
                    return -3;
                }
                len = stackValues[stackIndex].intValue();
                if (len < 0 || len > 5984) {
                    return -3;
                }
                factorial = BigInt1;
                for (i = 2; i <= len; i++) {
                    factorial = factorial.multiply(BigInteger.valueOf(i));
                }
                stackValues[stackIndex] = factorial;
            }
            if (charValue == '#') {
                if (leftNumberFlag == false) {
                    return -6;
                }
                len = stackValues[stackIndex].bitLength() - 1;
                if (len > 16) {
                    return -3;
                }
                len = stackValues[stackIndex].intValue();
                if (len < 0 || len > 46049) {
                    return -3;
                }
                factorial = BigInt1;
                for (i = 2; i * i <= len; i++) {
                    if (len / i * i == len) {
                        return -8;
                    }
                }
                factorial = BigInt1;
                compute_primorial_loop: for (i = 2; i <= len; i++) {
                    for (j = 2; j * j <= i; j++) {
                        if (i / j * j == i) {
                            continue compute_primorial_loop;
                        }
                    }
                    factorial = factorial.multiply(BigInteger.valueOf(i));
                }
                stackValues[stackIndex] = factorial;
            }
            if (charValue == 'B' || charValue == 'b' || charValue == 'N' || charValue == 'n' || charValue == 'F' || charValue == 'f' || charValue == 'P' || charValue == 'p' || charValue == 'L' || charValue == 'l') {
                if (leftNumberFlag || exprIndex == exprLength - 1) {
                    return -6;
                }
                exprIndex++;
                if (expr.charAt(exprIndex) != '(') {
                    return -6;
                }
                if (stackIndex > 395) {
                    return -7;
                }
                stackOperators[stackIndex++] = charValue & 0xDF;
                charValue = '(';
            }
            if (charValue == '+' || charValue == '-') {
                if (leftNumberFlag == false) {
                    exprIndex++;
                    if (charValue == '+') {
                        continue;
                    } else {
                        if (stackIndex > 0 && stackOperators[stackIndex - 1] == '_') {
                            stackIndex--;
                            continue;
                        }
                        if (stackIndex > 395) {
                            return -7;
                        }
                        stackOperators[stackIndex++] = '_';
                        continue;
                    }
                }
                if (stackIndex > 0 && stackOperators[stackIndex - 1] != '(') {
                    if ((SubExprResult = ComputeSubExpr(--stackIndex, stackValues, stackOperators)) != 0) {
                        return SubExprResult;
                    }
                    if (stackIndex > 0 && stackOperators[stackIndex - 1] != '(') {
                        if ((SubExprResult = ComputeSubExpr(--stackIndex, stackValues, stackOperators)) != 0) {
                            return SubExprResult;
                        }
                        if (stackIndex > 0 && stackOperators[stackIndex - 1] != '(') {
                            if ((SubExprResult = ComputeSubExpr(--stackIndex, stackValues, stackOperators)) != 0) {
                                return SubExprResult;
                            }
                        }
                    }
                }
                stackOperators[stackIndex++] = charValue;
                leftNumberFlag = false;
            } else {
                if (charValue == '*' || charValue == '/' || charValue == '%') {
                    if (leftNumberFlag == false) {
                        return -6;
                    }
                    if (stackIndex > 0 && (stackOperators[stackIndex - 1] == '^' || stackOperators[stackIndex - 1] == '*' || stackOperators[stackIndex - 1] == '/' || stackOperators[stackIndex - 1] == '%' || stackOperators[stackIndex - 1] == 'B' || stackOperators[stackIndex - 1] == 'N' || stackOperators[stackIndex - 1] == 'F' || stackOperators[stackIndex - 1] == 'L' || stackOperators[stackIndex - 1] == 'P')) {
                        if ((SubExprResult = ComputeSubExpr(--stackIndex, stackValues, stackOperators)) != 0) {
                            return SubExprResult;
                        }
                        if (stackIndex > 0 && (stackOperators[stackIndex - 1] == '^' || stackOperators[stackIndex - 1] == '*' || stackOperators[stackIndex - 1] == '/' || stackOperators[stackIndex - 1] == '%' || stackOperators[stackIndex - 1] == 'B' || stackOperators[stackIndex - 1] == 'N' || stackOperators[stackIndex - 1] == 'F' || stackOperators[stackIndex - 1] == 'L' || stackOperators[stackIndex - 1] == 'P')) {
                            if ((SubExprResult = ComputeSubExpr(--stackIndex, stackValues, stackOperators)) != 0) {
                                return SubExprResult;
                            }
                        }
                    }
                    stackOperators[stackIndex++] = charValue;
                    leftNumberFlag = false;
                } else {
                    if (charValue == '^') {
                        if (leftNumberFlag == false) {
                            return -6;
                        }
                        if (stackIndex > 0 && (stackOperators[stackIndex - 1] == '^' || stackOperators[stackIndex - 1] == 'B' || stackOperators[stackIndex - 1] == 'N' || stackOperators[stackIndex - 1] == 'F' || stackOperators[stackIndex - 1] == 'L' || stackOperators[stackIndex - 1] == 'P')) {
                            if ((SubExprResult = ComputeSubExpr(--stackIndex, stackValues, stackOperators)) != 0) {
                                return SubExprResult;
                            }
                        }
                        stackOperators[stackIndex++] = charValue;
                        leftNumberFlag = false;
                    } else {
                        if (charValue == '(') {
                            if (leftNumberFlag == true) {
                                return -6;
                            }
                            if (stackIndex > 395) {
                                return -7;
                            }
                            stackOperators[stackIndex++] = charValue;
                        } else {
                            if (charValue == ')') {
                                if (leftNumberFlag == false) {
                                    return -6;
                                }
                                if (stackIndex > 0 && stackOperators[stackIndex - 1] != '(') {
                                    if ((SubExprResult = ComputeSubExpr(--stackIndex, stackValues, stackOperators)) != 0) {
                                        return SubExprResult;
                                    }
                                    if (stackIndex > 0 && stackOperators[stackIndex - 1] != '(') {
                                        if ((SubExprResult = ComputeSubExpr(--stackIndex, stackValues, stackOperators)) != 0) {
                                            return SubExprResult;
                                        }
                                        if (stackIndex > 0 && stackOperators[stackIndex - 1] != '(') {
                                            if ((SubExprResult = ComputeSubExpr(--stackIndex, stackValues, stackOperators)) != 0) {
                                                return SubExprResult;
                                            }
                                        }
                                    }
                                }
                                if (stackIndex == 0) {
                                    return -5;
                                }
                                stackIndex--;
                                stackValues[stackIndex] = stackValues[stackIndex + 1];
                                leftNumberFlag = true;
                            } else {
                                if (charValue >= '0' && charValue <= '9') {
                                    exprIndexAux = exprIndex;
                                    while (exprIndexAux < exprLength - 1) {
                                        charValue = expr.charAt(exprIndexAux + 1);
                                        if (charValue >= '0' && charValue <= '9') {
                                            exprIndexAux++;
                                        } else {
                                            break;
                                        }
                                    }
                                    stackValues[stackIndex] = new BigInteger(expr.substring(exprIndex, exprIndexAux + 1));
                                    leftNumberFlag = true;
                                    exprIndex = exprIndexAux;
                                }
                            }
                        }
                    }
                }
            }
            exprIndex++;
        }
        if (leftNumberFlag == false) {
            return -6;
        }
        if (stackIndex > 0 && stackOperators[stackIndex - 1] != '(') {
            if ((SubExprResult = ComputeSubExpr(--stackIndex, stackValues, stackOperators)) != 0) {
                return SubExprResult;
            }
            if (stackIndex > 0 && stackOperators[stackIndex - 1] != '(') {
                if ((SubExprResult = ComputeSubExpr(--stackIndex, stackValues, stackOperators)) != 0) {
                    return SubExprResult;
                }
                if (stackIndex > 0 && stackOperators[stackIndex - 1] != '(') {
                    if ((SubExprResult = ComputeSubExpr(--stackIndex, stackValues, stackOperators)) != 0) {
                        return SubExprResult;
                    }
                }
            }
        }
        if (stackIndex != 0) {
            return -5;
        }
        if (stackValues[0].compareTo(BigInt1) <= 0 && type == 0) if (stackValues[0].bitLength() > 33219) {
            return -2;
        }
        ExpressionResult[0] = stackValues[0];
        return 0;
    }

    private static int modInv(int NbrMod, int currentPrime) {
        int QQ, T1, T3;
        int V1 = 1;
        int V3 = NbrMod;
        int U1 = 0;
        int U3 = currentPrime;
        while (V3 != 0) {
            if (U3 < V3 + V3) {
                T1 = U1 - V1;
                T3 = U3 - V3;
            } else {
                QQ = U3 / V3;
                T1 = U1 - V1 * QQ;
                T3 = U3 - V3 * QQ;
            }
            U1 = V1;
            U3 = V3;
            V1 = T1;
            V3 = T3;
        }
        return U1 + (currentPrime & (U1 >> 31));
    }

    static BigInteger partition(int val) {
        int[] partArray;
        int index, currentPrime, Q, k, n, sum, idx;
        long numerator, prodmod;
        int limbs = (int) (0.12 * Math.sqrt(val) + 1);
        partArray = new int[limbs * 2 + val];
        currentPrime = 0x7FFFFFFF;
        for (index = limbs + val - 1; index >= val; index--) {
            partArray[index] = currentPrime;
            calculate_previous_prime_loop: for (; ; ) {
                currentPrime -= 2;
                if (currentPrime % 3 == 0) {
                    continue calculate_previous_prime_loop;
                }
                for (Q = 5; Q <= 46341; Q += 6) {
                    if (currentPrime % Q == 0) {
                        continue calculate_previous_prime_loop;
                    }
                    if (currentPrime % (Q + 2) == 0) {
                        continue calculate_previous_prime_loop;
                    }
                }
                break;
            }
        }
        for (index = val; index < val + limbs; index++) {
            currentPrime = partArray[index];
            sum = 1;
            for (k = 1; k <= val; k++) {
                idx = k;
                partArray[k - 1] = sum;
                sum = 0;
                n = 1;
                for (; ; ) {
                    idx -= n + n - 1;
                    if (idx < 0) {
                        break;
                    }
                    sum -= currentPrime - partArray[idx];
                    sum += currentPrime & (sum >> 31);
                    idx -= n;
                    if (idx < 0) {
                        break;
                    }
                    sum -= currentPrime - partArray[idx];
                    sum += currentPrime & (sum >> 31);
                    n++;
                    idx -= n + n - 1;
                    if (idx < 0) {
                        break;
                    }
                    sum -= partArray[idx];
                    sum += currentPrime & (sum >> 31);
                    idx -= n;
                    if (idx < 0) {
                        break;
                    }
                    sum -= partArray[idx];
                    sum += currentPrime & (sum >> 31);
                    n++;
                }
            }
            partArray[index + limbs] = sum;
        }
        partArray[0] = partArray[val + limbs];
        for (index = 1; index < limbs; index++) {
            currentPrime = partArray[val + index];
            prodmod = 1;
            for (k = index - 1; k >= 0; k--) {
                prodmod = prodmod * partArray[val + k] % currentPrime;
            }
            prodmod = modInv((int) prodmod, currentPrime);
            numerator = partArray[index - 1];
            for (k = index - 2; k >= 0; k--) {
                numerator = (numerator * partArray[val + k] + partArray[k]) % currentPrime;
            }
            sum = partArray[val + limbs + index] - (int) numerator;
            if (sum < 0) {
                sum += currentPrime;
            }
            partArray[index] = (int) (sum * prodmod % currentPrime);
        }
        BigInteger result = BigInteger.valueOf(partArray[0]);
        BigInteger prodModulus = BigInteger.valueOf(1);
        for (index = 1; index < limbs; index++) {
            prodModulus = prodModulus.multiply(BigInteger.valueOf(partArray[val + index - 1]));
            result = result.add(prodModulus.multiply(BigInteger.valueOf(partArray[index])));
        }
        return result;
    }

    private static int ComputeSubExpr(int stackIndex, BigInteger[] stackValues, int[] stackOperators) {
        int i, j, len, val;
        double logarithm;
        BigInteger FibonPrev, FibonAct, FibonNext;
        int stackOper;
        stackOper = stackOperators[stackIndex];
        switch(stackOper) {
            case '+':
                stackValues[stackIndex] = stackValues[stackIndex].add(stackValues[stackIndex + 1]);
                return 0;
            case '-':
                stackValues[stackIndex] = stackValues[stackIndex].subtract(stackValues[stackIndex + 1]);
                return 0;
            case '_':
                stackValues[stackIndex] = stackValues[stackIndex + 1].negate();
                return 0;
            case '/':
                if (stackValues[stackIndex + 1].signum() == 0) {
                    return -3;
                }
                if (stackValues[stackIndex].remainder(stackValues[stackIndex + 1]).signum() != 0) {
                    return -4;
                }
                stackValues[stackIndex] = stackValues[stackIndex].divide(stackValues[stackIndex + 1]);
                return 0;
            case '%':
                if (stackValues[stackIndex + 1].signum() != 0) {
                    stackValues[stackIndex] = stackValues[stackIndex].remainder(stackValues[stackIndex + 1]);
                }
                return 0;
            case '*':
                if (stackValues[stackIndex].bitLength() + stackValues[stackIndex + 1].bitLength() > 66438) {
                    return -3;
                }
                stackValues[stackIndex] = stackValues[stackIndex].multiply(stackValues[stackIndex + 1]);
                return 0;
            case '^':
                len = stackValues[stackIndex].bitLength() - 1;
                if (len > 32) {
                    logarithm = (double) (len - 32) + Math.log(stackValues[stackIndex].shiftRight(len - 32).doubleValue()) / Math.log(2);
                } else {
                    logarithm = Math.log(stackValues[stackIndex].doubleValue()) / Math.log(2);
                }
                if (logarithm * stackValues[stackIndex + 1].doubleValue() > 66438) {
                    return -3;
                }
                stackValues[stackIndex] = stackValues[stackIndex].pow(stackValues[stackIndex + 1].intValue());
                return 0;
            case 'F':
            case 'L':
                len = stackValues[stackIndex + 1].bitLength() - 1;
                if (len > 17) {
                    return -3;
                }
                len = stackValues[stackIndex + 1].intValue();
                if (len > 95662) {
                    return -3;
                }
                if (len < 0) {
                    return -8;
                }
                FibonPrev = BigInteger.valueOf(stackOper == 'L' ? -1 : 1);
                FibonAct = BigInteger.valueOf(stackOper == 'L' ? 2 : 0);
                for (i = 1; i <= len; i++) {
                    FibonNext = FibonPrev.add(FibonAct);
                    FibonPrev = FibonAct;
                    FibonAct = FibonNext;
                }
                stackValues[stackIndex] = FibonAct;
                return 0;
            case 'P':
                len = stackValues[stackIndex + 1].bitLength() - 1;
                if (len > 24) {
                    return -3;
                }
                len = stackValues[stackIndex + 1].intValue();
                if (len > 3520000) {
                    return -3;
                }
                if (len < 0) {
                    return -8;
                }
                val = stackValues[stackIndex + 1].intValue();
                stackValues[stackIndex] = partition(val);
                break;
            case 'B':
            case 'N':
                int Base, Q, baseNbr;
                BigInteger value;
                if (stackOper == 'B') {
                    j = stackValues[stackIndex + 1].compareTo(BigInt3);
                    if (j < 0) {
                        return -8;
                    }
                    if (j == 0) {
                        stackValues[stackIndex] = BigInt2;
                        return 0;
                    }
                    value = stackValues[stackIndex + 1].subtract(BigInt2).or(BigInt1);
                } else {
                    if (stackValues[stackIndex + 1].compareTo(BigInt2) < 0) {
                        return -8;
                    }
                    value = stackValues[stackIndex + 1].add(BigInt1).or(BigInt1);
                }
                outer_calculate_SPRP: while (true) {
                    calculate_SPRP: do {
                        if (value.bitLength() < 16) {
                            j = value.intValue();
                            if (j >= 9) {
                                for (Q = 3; Q * Q <= j; Q += 2) {
                                    if (j % Q == 0) {
                                        break calculate_SPRP;
                                    }
                                }
                            }
                            break outer_calculate_SPRP;
                        }
                        Base = 3;
                        for (baseNbr = 100; baseNbr > 0; baseNbr--) {
                            if (value.mod(BigInteger.valueOf(Base)).signum() == 0) {
                                break calculate_SPRP;
                            }
                            calculate_new_prime3: do {
                                Base += 2;
                                for (Q = 3; Q * Q <= Base; Q += 2) {
                                    if (Base % Q == 0) {
                                        continue calculate_new_prime3;
                                    }
                                }
                                break;
                            } while (true);
                            if (value.mod(BigInteger.valueOf(Base)).signum() == 0) {
                                break calculate_SPRP;
                            }
                        }
                        BigInteger valuem1 = value.subtract(BigInt1);
                        int exp = valuem1.getLowestSetBit();
                        compute_SPRP_loop: for (baseNbr = 20; baseNbr > 0; baseNbr--) {
                            Base = 3;
                            calculate_new_prime4: do {
                                Base += 2;
                                for (Q = 3; Q * Q <= Base; Q += 2) {
                                    if (Base % Q == 0) {
                                        continue calculate_new_prime4;
                                    }
                                }
                                break;
                            } while (true);
                            BigInteger bBase = BigInteger.valueOf(Base);
                            BigInteger pow = bBase.modPow(valuem1.shiftRight(exp), value);
                            if (pow.equals(BigInt1) || pow.equals(valuem1)) {
                                continue;
                            }
                            for (j = 1; j < exp; j++) {
                                pow = pow.multiply(pow).mod(value);
                                if (pow.equals(valuem1)) {
                                    continue compute_SPRP_loop;
                                }
                                if (pow.equals(BigInt1)) {
                                    break compute_SPRP_loop;
                                }
                            }
                            break;
                        }
                        if (baseNbr == 0) {
                            break outer_calculate_SPRP;
                        }
                    } while (false);
                    if (stackOper == 'B') {
                        value = value.subtract(BigInt2);
                    } else {
                        value = value.add(BigInt2);
                    }
                }
                stackValues[stackIndex] = value;
        }
        return 0;
    }
}
