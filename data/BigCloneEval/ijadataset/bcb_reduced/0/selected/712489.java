package calendar;

class Gran implements CalOp {

    private String name;

    private int op;

    private long l_n, k, m;

    private Gran operand1 = null, operand2 = null;

    private Gran definedOnlyOn = null;

    private Gran layer1Gran = null;

    private int layer;

    private Calendar calendar = null;

    private int initial_step = 100;

    private int period;

    Gran(Calendar c, String n) {
        calendar = c;
        name = n;
        op = NO_OPERATION;
        layer = 1;
    }

    Gran(Calendar c, String theName, int theOp, Gran op1, Gran op2, long theL_n, long theK, long theM) throws CalException {
        calendar = c;
        name = theName;
        op = theOp;
        operand1 = op1;
        operand2 = op2;
        l_n = theL_n;
        k = theK;
        m = theM;
        determineDefinedOnlyOn();
        determineLayer();
        determineLayer1Gran();
    }

    Gran getDefinedOnlyOn() {
        return (definedOnlyOn);
    }

    String getName() {
        return (name);
    }

    Calendar getCalendar() {
        return (calendar);
    }

    int getOperator() {
        return (op);
    }

    Gran getOperand(int i) {
        if (i == 0) return (operand1); else return (operand2);
    }

    boolean isPrimitive() {
        if (op == NO_OPERATION) return (true); else return (false);
    }

    boolean isBasic() {
        return (layer == 1);
    }

    int getLayer() {
        return (layer);
    }

    boolean isDefinedOnlyOn(String g) {
        if (op == NO_OPERATION) return (false); else if (g.equals(definedOnlyOn.name)) return (true); else return (definedOnlyOn.isDefinedOnlyOn(g));
    }

    Gran getLayer1Gran() {
        return (layer1Gran);
    }

    IntSet convertDownTo(String target, long inTick) throws UndefinedException, CalException {
        IntSet result;
        if (target.compareTo(name) == 0) {
            if (layer != 1) {
                convertDownTo(definedOnlyOn.getName(), inTick);
            }
            result = new IntSet();
            result.add(inTick);
            return (result);
        }
        switch(op) {
            case SHIFT:
                result = downShifting(target, inTick);
                break;
            case GROUP:
            case ALTER:
                long l = first(target, inTick);
                long u = last(target, inTick);
                result = new IntSet();
                result.add(l, u);
                break;
            case SUBSET:
                result = downSubset(target, inTick);
                break;
            case SELECT_DOWN:
                result = downSelectDown(target, inTick);
                break;
            case SELECT_UP:
                result = downSelectUp(target, inTick);
                break;
            case SELECT_BY_OVERLAP:
                result = downSelectByOverlap(target, inTick);
                break;
            case UNION:
                result = downUnion(target, inTick);
                break;
            case INTERSECT:
                result = downIntersection(target, inTick);
                break;
            case DIFFERENCE:
                result = downDifference(target, inTick);
                break;
            case COMBINE:
                result = downCombining(target, inTick);
                break;
            case ANCHORED_GROUP:
                result = downAnchoredGroup(target, inTick);
                break;
            default:
                throw new CalException("Encountered in ConvertDown in " + name);
        }
        return (result);
    }

    long convertUpFrom(String target, long inTick) throws UndefinedException, CalException {
        if (target.compareTo(name) == 0) {
            if (layer != 1) {
                convertDownTo(definedOnlyOn.getName(), inTick);
            }
            return (inTick);
        }
        long result;
        switch(op) {
            case GROUP:
                result = upGrouping(target, inTick);
                break;
            case ALTER:
                result = upAlteringTick(target, inTick);
                break;
            case SHIFT:
                result = upShifting(target, inTick);
                break;
            case SUBSET:
                result = upSubset(target, inTick);
                break;
            case SELECT_DOWN:
            case SELECT_UP:
            case SELECT_BY_OVERLAP:
                result = upSelecting(target, inTick);
                break;
            case UNION:
                result = upUnion(target, inTick);
                break;
            case INTERSECT:
                result = upIntersection(target, inTick);
                break;
            case DIFFERENCE:
                result = upDifference(target, inTick);
                break;
            case COMBINE:
                result = upCombining(target, inTick);
                break;
            case ANCHORED_GROUP:
                result = upAnchoredGroup(target, inTick);
                break;
            default:
                throw new CalException("Encountered in ConvertDown in " + name);
        }
        return (result);
    }

    private Gran[] path = null, tempPath = null;

    private int pathLen, tempPathLen;

    private void determineDefinedOnlyOn() throws CalException {
        int grans = calendar.getSize() + 1;
        tempPath = new Gran[grans];
        path = new Gran[grans];
        tempPath[0] = this;
        tempPathLen = 1;
        pathLen = 0;
        travel(this);
        if (pathLen == 1) definedOnlyOn = null; else definedOnlyOn = path[1];
        path = null;
        tempPath = null;
    }

    private void travel(Gran node) throws CalException {
        if (node.getOperator() == NO_OPERATION) {
            if (pathLen == 0) {
                for (int i = 0; i < tempPathLen; ++i) path[i] = tempPath[i];
                pathLen = tempPathLen;
            } else {
                pathIntersect();
            }
            return;
        }
        Gran t = node.getOperand(0);
        if (t != null) {
            tempPath[tempPathLen++] = t;
            travel(t);
            tempPathLen--;
        } else throw new CalException(CalException.NO_SUCH_OPERATOR, "operand1 is null in " + name + ".travel for nonprimitive gran.");
        t = node.getOperand(1);
        if (t != null) {
            tempPath[tempPathLen++] = t;
            travel(t);
            tempPathLen--;
        }
    }

    private void pathIntersect() {
        int k = 0;
        for (int i = 0; i < pathLen; ++i) for (int j = 0; j < tempPathLen; ++j) if (tempPath[j] == path[i]) path[k++] = path[i];
        pathLen = k;
    }

    private void determineLayer() {
        switch(op) {
            case GROUP:
            case ALTER:
            case SHIFT:
                layer = 1;
                break;
            case SUBSET:
                layer = operand1.getLayer();
                break;
            case SELECT_DOWN:
            case SELECT_UP:
            case SELECT_BY_OVERLAP:
            case UNION:
            case INTERSECT:
            case DIFFERENCE:
                layer = operand1.getLayer();
                layer = layer > 2 ? 3 : 2;
                break;
            case COMBINE:
            case ANCHORED_GROUP:
                layer = 3;
                break;
            default:
                layer = 3;
                break;
        }
    }

    private void determineLayer1Gran() {
        if (isBasic()) {
            layer1Gran = this;
            return;
        }
        layer1Gran = operand1.getLayer1Gran();
    }

    private long first(String n, long i) throws CalException {
        long h, j;
        if (n.compareTo(name) == 0) return (i);
        switch(op) {
            case SHIFT:
                return (operand1.first(n, i - m));
            case GROUP:
                return (operand1.first(n, (i - 1) * m + 1));
            case ALTER:
                j = operand2.first(operand1.getName(), i);
                if (i >= l_n) h = (i - l_n) / m + 1; else h = (i - l_n - m + 1) / m + 1;
                if (i == (h - 1) * m + l_n) return (operand1.first(n, j + (h - 1) * k)); else return (operand1.first(n, j + h * k));
            default:
                throw new CalException(CalException.INVALID_OPERATION, "Invalid operation");
        }
    }

    private long last(String n, long i) throws CalException {
        long h, j;
        if (n.compareTo(name) == 0) return (i);
        switch(op) {
            case SHIFT:
                return (operand1.last(n, i - m));
            case GROUP:
                return (operand1.last(n, i * m));
            case ALTER:
                j = operand2.last(operand1.getName(), i);
                if (i >= l_n) h = (i - l_n) / m + 1; else h = (i - l_n - m + 1) / m + 1;
                return (operand1.last(n, j + h * k));
            default:
                throw new CalException(CalException.INVALID_OPERATION, "Invalid operation");
        }
    }

    private void firstlast(String n, long[] intv) throws CalException {
        long h;
        if (n.compareTo(name) == 0) return;
        switch(op) {
            case SHIFT:
                intv[0] -= m;
                intv[1] -= m;
                operand1.firstlast(n, intv);
                break;
            case GROUP:
                intv[0] = (intv[0] - 1) * m + 1;
                intv[1] = intv[1] * m;
                operand1.firstlast(n, intv);
                break;
            case ALTER:
                long[] j = new long[2];
                j[0] = intv[0];
                j[1] = intv[1];
                operand2.firstlast(operand1.getName(), j);
                if (intv[0] >= l_n) h = (intv[0] - l_n) / m + 1; else h = (intv[0] - l_n - m + 1) / m + 1;
                if (intv[0] == (h - 1) * m + l_n) intv[0] = j[0] + (h - 1) * k; else intv[0] = j[0] + h * k;
                if (intv[1] >= l_n) h = (intv[1] - l_n) / m + 1; else h = (intv[1] - l_n - m + 1) / m + 1;
                intv[1] = j[1] + h * k;
                operand1.firstlast(n, intv);
                break;
            default:
                throw new CalException(CalException.INVALID_OPERATION, "Invalid operation");
        }
    }

    private IntSet downShifting(String n, long inTick) throws CalException {
        IntSet result;
        try {
            result = operand1.convertDownTo(n, inTick - m);
        } catch (UndefinedException e) {
            throw new CalException(CalException.NO_SUCH_OPERATOR, "operand of shifting operation is not basic granularity in " + name + ".downGrouping.");
        }
        return (result);
    }

    private IntSet downSubset(String n, long inTick) throws UndefinedException, CalException {
        if (inTick < m || inTick > l_n) throw new UndefinedException(name + "(" + inTick + ")");
        return (operand1.convertDownTo(n, inTick));
    }

    private IntSet downSelectSub(String n, long inTick, IntSet s) throws UndefinedException, CalException {
        int j, tn = s.getTotal();
        if ((j = s.getIndex(inTick)) < tn) {
            ++j;
            if (k >= 0 && j <= min(k + l_n - 1, tn) && j > min(k - 1, tn)) return (operand1.convertDownTo(n, inTick)); else if (k < 0 && j >= max(tn + k + l_n, 1) && j < max(tn + k + 2, 1)) return (operand1.convertDownTo(n, inTick));
        }
        throw new UndefinedException("down conversion is undefined in " + name + ".downSelectSub.");
    }

    private IntSet downSelectDown(String n, long inTick) throws UndefinedException, CalException {
        String op1Name = operand1.getName(), op2Name = operand2.getName();
        String glb = calendar.getGlb(operand1, operand2).getName();
        IntSet s = operand1.convertDownTo(glb, inTick);
        long j;
        try {
            j = s.getByIndex(0);
        } catch (OutOfBoundException e) {
            throw new CalException("Index out of bound in downSelectDown--" + name);
        }
        long i = operand2.convertUpFrom(glb, j);
        s = calendar.convert(op2Name, i, Calendar.COVERING, op1Name);
        return (downSelectSub(n, inTick, s));
    }

    private IntSet downSelectUp(String n, long inTick) throws UndefinedException, CalException {
        String op1Name = operand1.getName(), op2Name = operand2.getName();
        IntSet s = calendar.convert(op1Name, inTick, Calendar.COVERING, op2Name);
        if (s.getTotal() == 0) throw new UndefinedException(); else return (operand1.convertDownTo(n, inTick));
    }

    private IntSet downSelectByOverlap(String n, long inTick) throws UndefinedException, CalException {
        String op1Name = operand1.getName(), op2Name = operand2.getName();
        IntSet t = calendar.convert(op1Name, inTick, Calendar.OVERLAP, op2Name);
        IntSet s;
        for (int i = 0; i < t.getTotal(); ++i) {
            try {
                long j = t.getByIndex(i);
                s = calendar.convert(op2Name, j, Calendar.OVERLAP, op1Name);
                return (downSelectSub(n, inTick, s));
            } catch (OutOfBoundException e) {
                throw new CalException("Index out of bound in downSelectByOverlap--" + name);
            } catch (UndefinedException e) {
            }
        }
        throw new UndefinedException();
    }

    private IntSet downUnion(String n, long inTick) throws UndefinedException, CalException {
        IntSet result = null;
        try {
            result = operand1.convertDownTo(n, inTick);
        } catch (UndefinedException e) {
        }
        if (result == null) result = operand2.convertDownTo(n, inTick);
        return (result);
    }

    private IntSet downIntersection(String n, long inTick) throws UndefinedException, CalException {
        IntSet result, temp;
        result = operand1.convertDownTo(n, inTick);
        temp = operand2.convertDownTo(n, inTick);
        if (!result.equal(temp)) throw new CalException(CalException.NO_SUCH_OPERATOR, "the same index don't have same time elements in " + name + ".downIntersection.");
        return (result);
    }

    private IntSet downDifference(String n, long inTick) throws UndefinedException, CalException {
        IntSet result, temp;
        result = operand1.convertDownTo(n, inTick);
        try {
            temp = operand2.convertDownTo(n, inTick);
        } catch (UndefinedException e) {
            return (result);
        }
        if (!result.equal(temp)) throw new CalException(CalException.NO_SUCH_OPERATOR, "the same index don't have same time elements in " + name + ".downIntersection.");
        throw new UndefinedException();
    }

    private IntSet downCombining(String n, long inTick) throws UndefinedException, CalException {
        String op1Name = operand1.getName(), op2Name = operand2.getName();
        IntSet s = calendar.convert(op1Name, inTick, Calendar.COVERING, op2Name);
        return (calendar.convert(op2Name, s, Calendar.COVERING, n));
    }

    private IntSet downAnchoredGroup(String n, long inTick) throws UndefinedException, CalException {
        IntSet result = new IntSet();
        return (result);
    }

    private long upGrouping(String n, long inTick) throws CalException {
        try {
            long j = operand1.convertUpFrom(n, inTick) - 1;
            if (j > 0) j = j / m + 1; else j = (j - m + 1) / m + 1;
            return (j);
        } catch (UndefinedException e) {
            throw new CalException(CalException.NO_SUCH_OPERATOR, e.getMessage() + "\n changed from UndefinedException in " + name + ".upGrouping");
        }
    }

    private void estimateBound(String n, long[] period, double[] delta) throws CalException {
        if (n.compareTo(name) == 0) {
            period[0] = 1;
            period[1] = 1;
            delta[0] = 0;
            delta[1] = 0;
            return;
        }
        switch(op) {
            case SHIFT:
                operand1.estimateBound(n, period, delta);
                delta[0] -= delta[1];
                delta[1] = -delta[0];
                break;
            case GROUP:
                operand1.estimateBound(n, period, delta);
                delta[0] -= (m - 1) * period[1] / period[0];
                long t = gcd(m, period[0]);
                period[0] /= t;
                period[1] *= m / t;
                break;
            case ALTER:
                long f = 1;
                if (n.compareTo(operand1.getName()) != 0) {
                    f = tryTranslate(n);
                }
                if (f > 0) {
                    f *= k;
                    operand2.estimateBound(n, period, delta);
                    t = gcd(period[0], m);
                    period[1] = (period[0] * f + m * period[1]) / t;
                    period[0] *= m / t;
                    if (k > 0) {
                        delta[0] -= f * (long) (l_n + 1) / m;
                        delta[1] += f * (1 - (long) l_n / m);
                    } else {
                        delta[0] += f * (1 - (long) (l_n + 1) / m);
                        delta[1] -= f * (long) l_n / m;
                    }
                } else {
                    period[1] = operand2.last(n, period[1]) - operand2.last(n, 0);
                    delta[0] = -period[1];
                    delta[1] = period[1];
                }
                break;
            default:
                throw new CalException(CalException.INVALID_OPERATION, "Invalid operation");
        }
    }

    private long gcd(long a, long b) {
        long r, aa = a, bb = b;
        while (true) {
            r = aa % bb;
            if (r == 0) break;
            aa = bb;
            bb = r;
        }
        return (bb);
    }

    private long tryTranslate(String n) {
        if (n.compareTo(name) == 0) return (1);
        if (op == ALTER) return (-1);
        long t = operand1.tryTranslate(n);
        if (op == GROUP) t *= m;
        return (t);
    }

    public static long statistics = 0;

    private long upAlteringTick(String n, long inTick) throws CalException {
        long[] period = new long[2];
        long finerTick, t0, lowerBound, upperBound, estimatedResult;
        double[] delta = new double[2];
        double e_y;
        String finerName;
        try {
            finerTick = operand1.convertUpFrom(n, inTick);
            finerName = operand1.getName();
            t0 = last(finerName, 0);
            estimateBound(finerName, period, delta);
            e_y = (double) period[1] / period[0];
            estimatedResult = (finerTick - t0 > 0) ? (long) ((finerTick - t0) / e_y + 0.5) : (long) ((finerTick - t0) / e_y - 0.5);
            delta[0] /= e_y;
            delta[1] /= e_y;
            lowerBound = (delta[0] < -period[0]) ? -period[0] : (long) delta[0] - 1;
            upperBound = (delta[1] > period[0]) ? period[0] : (long) delta[1] + 1;
            lowerBound += estimatedResult;
            upperBound += estimatedResult;
            if (statistics < upperBound - lowerBound) statistics = upperBound - lowerBound;
            long l, u;
            while (lowerBound < upperBound - 1) {
                l = first(finerName, estimatedResult);
                if (l > finerTick) {
                    upperBound = estimatedResult;
                    estimatedResult = (lowerBound + upperBound) / 2;
                    continue;
                }
                u = last(finerName, estimatedResult);
                if (u < finerTick) {
                    lowerBound = estimatedResult;
                    estimatedResult = (lowerBound + upperBound) / 2;
                    continue;
                }
                break;
            }
            if (last(finerName, estimatedResult) < finerTick) ++estimatedResult;
        } catch (UndefinedException e) {
            throw new CalException();
        }
        return (estimatedResult);
    }

    private long upShifting(String n, long inTick) throws CalException {
        long t;
        try {
            t = operand1.convertUpFrom(n, inTick) + m;
        } catch (UndefinedException e) {
            throw new CalException(CalException.NO_SUCH_OPERATOR, "operand is not basic granularity for shifting operation in " + name + ".downGrouping.");
        }
        return (t);
    }

    private long upSubset(String n, long inTick) throws UndefinedException, CalException {
        long t = operand1.convertUpFrom(n, inTick);
        if (t < m || t > l_n) throw new UndefinedException("index out of bound in subset operation in " + name + ".upSubset.");
        return (t);
    }

    private long upSelecting(String n, long inTick) throws UndefinedException, CalException {
        long t = operand1.convertUpFrom(n, inTick);
        String glb = calendar.getGlb(operand1, operand2).getName();
        convertDownTo(glb, t);
        return (t);
    }

    private long upUnion(String n, long inTick) throws UndefinedException, CalException {
        long t;
        try {
            t = operand1.convertUpFrom(n, inTick);
            return (t);
        } catch (UndefinedException e) {
        }
        return (operand2.convertUpFrom(n, inTick));
    }

    private long upIntersection(String n, long inTick) throws UndefinedException, CalException {
        long t1, t2;
        t1 = operand1.convertUpFrom(n, inTick);
        t2 = operand2.convertUpFrom(n, inTick);
        if (t1 != t2) throw new CalException(CalException.NO_SUCH_OPERATOR, "same subtick not in same index for intersected granularities in " + name + ".upInterSection.");
        return (t1);
    }

    private long upDifference(String n, long inTick) throws UndefinedException, CalException {
        long t1, t2;
        t1 = operand1.convertUpFrom(n, inTick);
        try {
            t2 = operand2.convertUpFrom(n, inTick);
        } catch (UndefinedException e) {
            return (t1);
        }
        if (t1 != t2) throw new CalException(CalException.NO_SUCH_OPERATOR, "same subtick not in same index for intersected granularities in " + name + ".upDifference.");
        throw new UndefinedException();
    }

    private long upCombining(String n, long inTick) throws UndefinedException, CalException {
        long j1 = operand1.convertUpFrom(n, inTick);
        long j2 = operand2.convertUpFrom(n, inTick);
        String glb = calendar.getGlb(operand1, operand2).getName();
        IntSet s1 = operand1.convertDownTo(glb, j1);
        IntSet s2 = operand2.convertDownTo(glb, j2);
        if (s1.supersetOf(s2)) return (j1); else throw new UndefinedException();
    }

    private long upAnchoredGroup(String n, long inTick) throws UndefinedException, CalException {
        return (0);
    }

    protected void finalize() throws Throwable {
        definedOnlyOn = null;
        calendar = null;
        operand1 = null;
        operand2 = null;
    }

    private long min(long a, long b) {
        return (a > b ? b : a);
    }

    private long max(long a, long b) {
        return (a > b ? a : b);
    }
}
