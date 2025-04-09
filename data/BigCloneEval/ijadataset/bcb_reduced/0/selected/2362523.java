package com.rbnb.api;

import com.rbnb.utility.ToString;

public final class TimeRange extends com.rbnb.api.Serializable {

    /**
     * Duration value is inherited.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/06/2001
     */
    public static final double INHERIT_DURATION = Double.NaN;

    /**
     * Time values are inherited.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/23/2001
     */
    public static final double[] INHERIT_TIMES = null;

    /**
     * server time of day.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 10/23/2002
     */
    public static final TimeRange SERVER_TOD = new TimeRange(0., -Double.MAX_VALUE / 10.);

    /**
     * Decreasing times.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/18/2001
     */
    static final byte DECREASING = 3;

    /**
     * Increasing times.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/18/2001
     */
    static final byte INCREASING = 2;

    /**
     * Random time ordering.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/18/2001
     */
    static final byte RANDOM = 1;

    /**
     * Unknown time ordering.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/18/2001
     */
    static final byte UNKNOWN = 0;

    private static final byte EQUAL = 0, SUBSET = 1, SUPERSET = 2, OVERLAP = 3, PAR_DUR = 0, PAR_PTM = 1, PAR_STA = 2, PAR_INC = 3;

    private static final byte[][] MATCHES = { { Rmap.MATCH_BEFORE, Rmap.MATCH_AFTER }, { Rmap.MATCH_BEFORENAME, Rmap.MATCH_AFTERNAME }, { Rmap.MATCH_NOINTERSECTION, Rmap.MATCH_NOINTERSECTION }, { Rmap.MATCH_EQUAL, Rmap.MATCH_EQUAL }, { Rmap.MATCH_SUBSET, Rmap.MATCH_SUPERSET }, { Rmap.MATCH_SUPERSET, Rmap.MATCH_SUBSET }, { Rmap.MATCH_INTERSECTION, Rmap.MATCH_INTERSECTION }, { Rmap.MATCH_AFTERNAME, Rmap.MATCH_BEFORENAME }, { Rmap.MATCH_AFTER, Rmap.MATCH_BEFORE } };

    private static final double TOLERANCE = (((1 << 4) - 1) / ((double) (1L << 52))), LOW_TOLERANCE = (1. - TOLERANCE), HI_TOLERANCE = (1. + TOLERANCE);

    private static final String[] PARAMETERS = { "DUR", "PTM", "STA", "INC" };

    /**
     * how does time change across the range?
     * <p>
     * The values that this field can take are:
     * <p><ul>
     * <li>INCREASING - monotonically increasing,</li>
     * <li>DECREASING - monotonically descreasing,</li>
     * <li>RANDOM - time can go in any direction,<li>
     * <li>UNKNOWN - haven't determined direction yet.<li>
     * </ul><p>
     * <it>Note: at this point, <code>TimeRanges</code> must monotonically
     *     increase.</it>
     *
     * @author Ian Brown
     * @since V2.0
     * @version 09/20/2001
     */
    private byte changing = INCREASING;

    /**
     * the comparison direction.
     * <p>
     * This field is used by requests for <code>NEWEST</code> to indicate that
     * the comparison is to reverse its direction. It is somewhat of a kludge
     * due to the fact that I needed to fix things quickly.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 05/21/2002
     */
    private boolean direction = false;

    /**
     * duration (applies to all of the times).
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/30/2000
     */
    private double duration = INHERIT_DURATION;

    /**
     * inclusive of both ends?
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/06/2002
     */
    private boolean inclusive = false;

    /**
     * time values.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/30/2000
     */
    private double[] ptimes = INHERIT_TIMES;

    public TimeRange() {
        super();
    }

    TimeRange(InputStream isI, DataInputStream disI) throws com.rbnb.api.SerializeException, java.io.EOFException, java.io.IOException {
        this(null, isI, disI);
    }

    TimeRange(TimeRange otherI, InputStream isI, DataInputStream disI) throws com.rbnb.api.SerializeException, java.io.EOFException, java.io.IOException {
        this();
        read(otherI, isI, disI);
    }

    public TimeRange(double timeI) {
        this();
        set(timeI);
        setInclusive(true);
    }

    public TimeRange(double startI, double durationI) {
        this();
        set(startI, durationI);
        setInclusive((durationI == 0.) || (durationI == INHERIT_DURATION));
    }

    public TimeRange(double[] ptimesI) {
        this();
        set(ptimesI);
        setInclusive(true);
    }

    public TimeRange(double[] ptimesI, double durationI) {
        this();
        set(ptimesI, durationI);
        setInclusive((durationI == 0.) || (durationI == INHERIT_DURATION));
    }

    final TimeRange add(TimeRange otherI) {
        if (otherI == null) {
            return (this);
        }
        if ((otherI.getPtimes() == INHERIT_TIMES) || (otherI.getDuration() == INHERIT_DURATION)) {
            throw new java.lang.IllegalArgumentException("Cannot add " + otherI + " to " + this);
        }
        TimeRange sumR = new TimeRange();
        double mEnd = (getDuration() == INHERIT_DURATION) ? otherI.getDuration() : getDuration(), iEnd = otherI.getTime() + otherI.getDuration();
        if (getPtimes() == INHERIT_TIMES) {
            sumR.setPtimes(otherI.getPtimes());
            mEnd += sumR.getTime();
        } else {
            double[] sumTimes = new double[getNptimes() * otherI.getNptimes()];
            mEnd += getTime() + otherI.getTime();
            for (int idx = 0, offset = 0; idx < otherI.getNptimes(); ++idx) {
                double base = otherI.getPtimes()[idx];
                for (int idx1 = 0; idx1 < getNptimes(); ++idx1, ++offset) {
                    sumTimes[offset] = base + getPtimes()[idx1];
                }
            }
            sumR.setPtimes(sumTimes);
        }
        sumR.setDuration(Math.min(mEnd, iEnd) - sumR.getTime());
        sumR.setInclusive(getInclusive() || otherI.getInclusive());
        return (sumR);
    }

    final void addLimits(double[] limitsI) {
        double minimum = Math.min(getPtimes()[0], limitsI[0]), maximum = Math.max(getPtimes()[0] + getDuration(), limitsI[1]);
        set(minimum, maximum - minimum);
    }

    static final TimeRange addLimits(TimeRange limitI, TimeRange tRangeI) {
        TimeRange limitR = limitI;
        double[] limits = tRangeI.getLimits();
        if (limitR == null) {
            limitR = new TimeRange(limits[0], limits[1] - limits[0]);
            limitR.setInclusive(tRangeI.getInclusive());
        } else {
            limitR.addLimits(limits);
            if (limitR.getLimits()[1] == limits[1]) {
                limitR.setInclusive(tRangeI.getInclusive());
            }
        }
        return (limitR);
    }

    private final TimeRange addOffset(TimeRange iRangeI, boolean needAllI) {
        double rDuration = getDuration();
        if (rDuration == INHERIT_DURATION) {
            rDuration = iRangeI.getDuration();
        }
        TimeRange tRangeR = null;
        if (!needAllI) {
            tRangeR = new TimeRange(getTime() + iRangeI.getTime(), rDuration);
        } else {
            double[] ltimes = (double[]) getPtimes().clone();
            for (int idx = 0; idx < ltimes.length; ++idx) {
                ltimes[idx] += iRangeI.getTime();
            }
            tRangeR = new TimeRange(ltimes, rDuration);
        }
        tRangeR.setInclusive(getInclusive() || iRangeI.getInclusive());
        return (tRangeR);
    }

    final void addToTimes(double incrementI) {
        for (int idx = 0; idx < getNptimes(); ++idx) {
            getPtimes()[idx] += incrementI;
        }
    }

    TimeRelativeResponse afterTimeRelative(TimeRelativeRequest requestI, RequestOptions roI, int nPointsI) throws com.rbnb.utility.SortException, com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.EOFException, java.io.IOException, java.lang.InterruptedException {
        TimeRelativeResponse responseR = new TimeRelativeResponse();
        responseR.setStatus(0);
        responseR.setTime(getTime());
        responseR.setInvert(false);
        return (responseR);
    }

    TimeRelativeResponse beforeTimeRelative(TimeRelativeRequest requestI, RequestOptions roI, int nPointsI) throws com.rbnb.utility.SortException, com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.EOFException, java.io.IOException, java.lang.InterruptedException {
        TimeRelativeResponse responseR = new TimeRelativeResponse();
        responseR.setStatus(0);
        if ((roI != null) && roI.getExtendStart()) {
            responseR.setTime(getPointTime(nPointsI - 1, nPointsI));
            responseR.setInvert(false);
        } else {
            responseR.setTime(getPtimes()[getNptimes() - 1] + getDuration());
            responseR.setInvert(true);
        }
        return (responseR);
    }

    private final TimeRange buildRange(boolean needAllI) {
        TimeRange tRangeR = null;
        if (needAllI) {
            tRangeR = (TimeRange) clone();
        } else {
            tRangeR = new TimeRange(getTime(), getDuration());
            tRangeR.setInclusive(getInclusive());
        }
        return (tRangeR);
    }

    public final Object clone() {
        TimeRange clonedR = new TimeRange();
        clonedR.changing = changing;
        clonedR.direction = direction;
        clonedR.duration = duration;
        clonedR.inclusive = inclusive;
        if (clonedR != null) {
            if (getPtimes() != null) {
                double[] temp = new double[getPtimes().length];
                System.arraycopy(getPtimes(), 0, temp, 0, getPtimes().length);
                clonedR.setPtimes(temp);
            }
        }
        return (clonedR);
    }

    private static final void compareTimeIntervals(int oneIdxI, double oneLowI, double oneHighI, boolean allowOneHighI, boolean[] oneIntersectedIO, int twoIdxI, double twoLowI, double twoHighI, boolean allowTwoHighI, boolean[] twoIntersectedIO, int[] numMatchesIO) {
        if (oneLowI < twoLowI) {
            if (oneHighI >= twoHighI) {
                ++numMatchesIO[SUPERSET];
                oneIntersectedIO[oneIdxI] = twoIntersectedIO[twoIdxI] = true;
            } else if ((oneHighI > twoLowI) || (allowOneHighI && (oneHighI == twoLowI))) {
                ++numMatchesIO[OVERLAP];
                oneIntersectedIO[oneIdxI] = twoIntersectedIO[twoIdxI] = true;
            }
        } else if (oneHighI > twoHighI) {
            if ((oneLowI < twoHighI) || (allowTwoHighI && (oneLowI == twoHighI))) {
                if (oneLowI == twoLowI) {
                    ++numMatchesIO[SUPERSET];
                    oneIntersectedIO[oneIdxI] = twoIntersectedIO[twoIdxI] = true;
                } else {
                    ++numMatchesIO[OVERLAP];
                    oneIntersectedIO[oneIdxI] = twoIntersectedIO[twoIdxI] = true;
                }
            }
        } else if (oneLowI == twoLowI) {
            if (oneHighI == twoHighI) {
                ++numMatchesIO[EQUAL];
                oneIntersectedIO[oneIdxI] = twoIntersectedIO[twoIdxI] = true;
            } else {
                ++numMatchesIO[SUBSET];
                oneIntersectedIO[oneIdxI] = twoIntersectedIO[twoIdxI] = true;
            }
        } else {
            ++numMatchesIO[SUBSET];
            oneIntersectedIO[oneIdxI] = twoIntersectedIO[twoIdxI] = true;
        }
    }

    public final int compareTo(TimeRange otherI) {
        int compareR = 0;
        for (int idx = 0; (compareR == 0); ++idx) {
            if (idx == getNptimes()) {
                if (idx != otherI.getNptimes()) {
                    compareR = -1;
                }
                break;
            } else if (idx == otherI.getNptimes()) {
                compareR = 1;
                break;
            }
            double difference = getPtimes()[idx] - otherI.getPtimes()[idx];
            if (difference < 0.) {
                compareR = -1;
            } else if (difference > 0.) {
                compareR = 1;
            }
        }
        if (compareR == 0) {
            double difference = getDuration() - otherI.getDuration();
            if (difference < 0.) {
                compareR = -1;
            } else if (difference > 0.) {
                compareR = 1;
            }
        }
        return (compareR);
    }

    final TimeRange duplicate() {
        return ((TimeRange) clone());
    }

    public final int copyTimes(int nPointsI, double[] timeI, int startAtI) {
        if (getNptimes() == 0) {
            throw new IllegalStateException("Inherited time values are not available.");
        } else if (nPointsI < 0) {
            throw new IllegalArgumentException("Negative input number of points.");
        } else if ((getNptimes() != 1) && (nPointsI != 0) && (nPointsI != getNptimes())) {
            throw new IllegalArgumentException("The number of input points does not match the number of " + "times.");
        }
        int nPointsR = Math.max(nPointsI, getNptimes());
        if (nPointsR == getNptimes()) {
            System.arraycopy(getPtimes(), 0, timeI, startAtI, nPointsR);
        } else {
            for (int idx = 0; idx < nPointsR; ++idx) {
                timeI[startAtI + idx] = getPointTime(idx, nPointsR);
            }
        }
        return (nPointsR);
    }

    private final TimeRange dataOffsets(TimeRange iRangeI, DataBlock iBlockI, DataBlock dBlockI) {
        TimeRange tRangeR = null;
        if ((getDuration() == INHERIT_DURATION) && (dBlockI.getNpts() != 1)) {
            throw new java.lang.IllegalStateException("Need a duration to be able to extract the times " + "for more than one point from a parent data pool.");
        }
        try {
            double ltimes[] = new double[iBlockI.getNpts() * dBlockI.getNpts()];
            double rDuration = 0.;
            if (getPtimes() == INHERIT_TIMES) {
                int dNpts = dBlockI.getNpts();
                rDuration = getDuration() / dNpts;
                for (int idx = 0, idx2 = 0, iNpts = iBlockI.getNpts(); idx < iNpts; ++idx) {
                    double itime = iRangeI.getPointTime(idx, iNpts);
                    for (int idx1 = 0; idx1 < dNpts; ++idx1, ++idx2, itime += rDuration) {
                        ltimes[idx2] = itime;
                    }
                }
            } else {
                for (int idx = 0, idx2 = 0, iNpts = iBlockI.getNpts(), dNpts = dBlockI.getNpts(); idx < iNpts; ++idx) {
                    double itime = iRangeI.getPointTime(idx, iNpts);
                    for (int idx1 = 0; idx1 < dNpts; ++idx1, ++idx2) {
                        double ltime = getPointTime(idx1, dNpts);
                        ltimes[idx2] = itime + ltime;
                    }
                }
            }
            tRangeR = new TimeRange(ltimes, rDuration);
            tRangeR.setInclusive(getInclusive());
        } catch (IllegalStateException e) {
            throw new java.lang.IllegalStateException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new java.lang.IllegalStateException(e.getMessage());
        }
        return (tRangeR);
    }

    private static final byte determineResults(int modeI, int oneIntervalsI, double oneLowestI, double oneHighestI, boolean allowOneHighI, boolean[] oneIntersectedI, int twoIntervalsI, double twoLowestI, double twoHighestI, boolean allowTwoHighI, boolean[] twoIntersectedI, int[] numMatchesI) {
        boolean canBeEqual = ((numMatchesI[EQUAL] == oneIntervalsI) && (numMatchesI[EQUAL] == twoIntervalsI)), canSubset = ((numMatchesI[SUBSET] > 0) && (numMatchesI[SUPERSET] == 0)), canSuperset = (!canSubset && (numMatchesI[SUPERSET] > 0));
        for (int idx = 0; (canBeEqual || canSubset) && (idx < oneIntervalsI); ++idx) {
            if (!oneIntersectedI[idx]) {
                canBeEqual = canSubset = false;
            }
        }
        for (int idx = 0; (canBeEqual || canSuperset) && (idx < twoIntervalsI); ++idx) {
            if (!twoIntersectedI[idx]) {
                canBeEqual = canSuperset = false;
            }
        }
        byte matchesR = Rmap.MATCH_NOINTERSECTION;
        if (canBeEqual) {
            matchesR = Rmap.MATCH_EQUAL;
        } else if (canSubset) {
            matchesR = Rmap.MATCH_SUBSET;
        } else if (canSuperset) {
            matchesR = Rmap.MATCH_SUPERSET;
        } else if ((numMatchesI[EQUAL] > 0) || (numMatchesI[SUBSET] > 0) || (numMatchesI[SUPERSET] > 0) || (numMatchesI[OVERLAP] > 0)) {
            matchesR = Rmap.MATCH_INTERSECTION;
        } else if ((oneHighestI < twoLowestI) || (!allowOneHighI && (oneHighestI == twoLowestI))) {
            matchesR = Rmap.MATCH_BEFORE;
        } else if ((oneLowestI > twoHighestI) || (!allowTwoHighI && (oneLowestI == twoHighestI))) {
            matchesR = Rmap.MATCH_AFTER;
        }
        matchesR = MATCHES[matchesR - Rmap.MATCH_BEFORE][modeI];
        return (matchesR);
    }

    public final boolean extend(int myPointsI, TimeRange otherI, int oPointsI) {
        boolean compatibleR = false;
        int myPtimes = getNptimes(), oPtimes = otherI.getNptimes();
        double myD = getDuration(), oD = otherI.getDuration();
        if ((myPtimes == myPointsI) && (oPtimes == oPointsI) && ((myPtimes != 1) || (myD == 0.)) && ((oPtimes != 1) || (oD == 0.))) {
            if (compatibleR = ((oD >= myD * LOW_TOLERANCE) && (oD <= myD * HI_TOLERANCE))) {
                double[] newTimes = new double[myPtimes + oPtimes];
                System.arraycopy(getPtimes(), 0, newTimes, 0, myPtimes);
                System.arraycopy(otherI.getPtimes(), 0, newTimes, myPtimes, oPtimes);
                setPtimes(newTimes);
                setInclusive(getInclusive() || otherI.getInclusive());
                return (true);
            }
        } else if ((myPtimes == 1) && (oPtimes == 1)) {
            double myS = getTime(), myE = myS + myD, oS = otherI.getTime();
            if ((oS >= myE * LOW_TOLERANCE) && (oS <= myE * HI_TOLERANCE)) {
                double myI = myD / myPointsI, oI = oD / oPointsI;
                if (compatibleR = ((oI >= myI * LOW_TOLERANCE) && (oI <= myI * HI_TOLERANCE))) {
                    setDuration(myD + oD);
                    setInclusive(getInclusive() || otherI.getInclusive());
                }
            }
        }
        return (compatibleR);
    }

    final TimeRange extractInheritance(boolean needAllI, TimeRange iRangeI, DataBlock iBlockI, DataBlock dBlockI) {
        TimeRange tRangeR = null;
        if (iRangeI == null) {
            tRangeR = buildRange(needAllI);
        } else if (iBlockI == null) {
            tRangeR = addOffset(iRangeI, needAllI);
        } else {
            tRangeR = extractOffsets(iRangeI, iBlockI, dBlockI);
        }
        return (tRangeR);
    }

    private final TimeRange extractOffsets(TimeRange iRangeI, DataBlock iBlockI, DataBlock dBlockI) {
        TimeRange tRangeR = null;
        if (dBlockI == null) {
            tRangeR = mergeOffsets(iRangeI, iBlockI);
        } else {
            tRangeR = dataOffsets(iRangeI, iBlockI, dBlockI);
        }
        return (tRangeR);
    }

    final boolean extractRequest(TimeRange requestI, TimeRange otherI, TimeRange rangeO, TimeRange otherO) {
        return (extractRequestWithData(requestI, otherI, null, rangeO, otherO, null));
    }

    final boolean extractRequestWithData(TimeRange requestI, TimeRange otherI, DataBlock dBlockI, TimeRange rangeO, TimeRange otherO, DataBlock dBlockO) {
        if (requestI == null) {
            throw new java.lang.IllegalStateException("Cannot perform extraction without a request.");
        } else if (requestI.getNptimes() != 1) {
            throw new java.lang.IllegalStateException("Cannot perform extraction of multiple time values " + "at this time.");
        } else if (getDuration() == INHERIT_DURATION) {
            throw new java.lang.IllegalStateException("Cannot perform extraction without a duration at this time.");
        }
        boolean reversed = (getDirection() != requestI.getDirection());
        DataBlock dRef = null;
        int nPoints = (dBlockI == null) ? 1 : dBlockI.getNpts();
        if (getNptimes() == 1) {
            int sp = 0, ep = 0;
            double myDuration = getDuration();
            if (myDuration == 0.) {
                sp = 0;
                ep = nPoints - 1;
            } else {
                int myNpoints;
                double myStart, requestStart, requestDuration = requestI.getDuration();
                if (reversed) {
                    myStart = -(getPtimes()[0] + myDuration);
                    myNpoints = -nPoints;
                    requestStart = -(requestI.getPtimes()[0] + requestDuration);
                } else {
                    myStart = getPtimes()[0];
                    myNpoints = nPoints;
                    requestStart = requestI.getPtimes()[0];
                }
                double tOffset = requestStart - myStart, pIndex = (myNpoints * tOffset) / myDuration;
                if (reversed) {
                    ep = (int) Math.min(nPoints - 1, Math.max(0, Math.ceil(nPoints - 1 + pIndex)));
                } else if (requestI.getDuration() != 0.) {
                    sp = (int) Math.ceil(pIndex);
                } else {
                    sp = (int) Math.floor(pIndex);
                }
                tOffset += requestDuration;
                pIndex = (myNpoints * tOffset) / myDuration;
                if (reversed) {
                    if (!requestI.isInclusive() && (pIndex == Math.ceil(pIndex))) {
                        ++pIndex;
                    }
                    sp = (int) Math.ceil(nPoints - 1 + pIndex);
                } else {
                    if (!requestI.isInclusive() && (pIndex == Math.ceil(pIndex))) {
                        --pIndex;
                    }
                    ep = (int) Math.floor(pIndex);
                }
                if (sp < 0) {
                    sp = 0;
                } else if (sp >= nPoints) {
                    return (false);
                }
                if (ep < 0) {
                    return (false);
                } else if (ep >= nPoints) {
                    ep = nPoints - 1;
                }
            }
            if (ep < sp) {
                return (false);
            }
            rangeO.set(getPtimes()[0] + (sp * myDuration) / nPoints, ((ep - sp + 1) * myDuration) / nPoints);
            if (dBlockI != null) {
                if ((sp == 0) && (ep == nPoints - 1) && (ep == dBlockI.getNpts())) {
                    dBlockO.set(dBlockI.getData(), dBlockI.getNpts(), dBlockI.getPtsize(), dBlockI.getDtype(), dBlockI.getMIMEType(), dBlockI.getWorder(), dBlockI.getIndivFlg(), dBlockI.getOffset(), dBlockI.getStride());
                } else {
                    DataBlock ndBlock;
                    ndBlock = new DataBlock(null, (ep - sp) + 1, dBlockI.getPtsize(), dBlockI.getDtype(), dBlockI.getMIMEType(), dBlockI.getWorder(), dBlockI.getIndivFlg(), (dBlockI.getOffset() + sp * dBlockI.getStride()), dBlockI.getStride());
                    ndBlock.setData(dBlockI.getData());
                    dRef = new DataBlock(null, 1, dBlockI.getPtsize(), dBlockI.getDtype(), dBlockI.getMIMEType(), dBlockI.getWorder(), dBlockI.getIndivFlg(), 0, dBlockI.getPtsize());
                    dBlockO.set(ndBlock.extractData(dRef), ndBlock.getNpts(), dBlockI.getPtsize(), dBlockI.getDtype(), dBlockI.getMIMEType(), dBlockI.getWorder(), dBlockI.getIndivFlg(), 0, dBlockI.getPtsize());
                }
            }
            if (otherO != null) {
                if (otherI.getNptimes() == 1) {
                    otherO.set((otherI.getTime() + sp * otherI.getDuration() / nPoints), (((ep - sp) + 1) * otherI.getDuration() / nPoints));
                } else {
                    double[] otherTimes = new double[(ep - sp) + 1];
                    for (int idx = sp; idx <= ep; ++idx) {
                        otherTimes[idx - sp] = otherI.getPointTime(idx, nPoints);
                    }
                    otherO.set(otherTimes, otherI.getDuration());
                }
            }
        } else {
            java.util.Vector values = new java.util.Vector(), ovalues = ((otherO == null) ? null : new java.util.Vector());
            double rMin = requestI.getPtimes()[0], rMax = rMin + requestI.getDuration();
            for (int idx = 0; idx < getNptimes(); ++idx) {
                double pMin = getPtimes()[idx], pMax = pMin + getDuration();
                if ((pMin <= rMax) && (pMax >= rMin)) {
                    values.addElement(new Double(pMin));
                    values.addElement(new Integer(idx));
                    if (ovalues != null) {
                        ovalues.addElement(new Double(otherI.getPointTime(idx, getNptimes())));
                    }
                }
            }
            if (values.size() == 0) {
                throw new java.lang.IllegalStateException("No points matched?");
            }
            double[] nPtimes = new double[values.size() / 2], oPtimes = ((ovalues == null) ? null : new double[nPtimes.length]);
            for (int idx = 0, idx1 = 0; idx < values.size(); idx += 2, ++idx1) {
                nPtimes[idx1] = ((Double) values.elementAt(idx)).doubleValue();
                if (oPtimes != null) {
                    oPtimes[idx1] = ((Double) ovalues.elementAt(idx1)).doubleValue();
                }
            }
            rangeO.set(nPtimes, getDuration());
            if (otherO != null) {
                otherO.set(oPtimes, ((otherI.getNptimes() == 1) ? otherI.getDuration() / nPoints : otherI.getDuration()));
            }
            if (dBlockI != null) {
                if ((nPtimes.length == getNptimes()) && (getNptimes() == dBlockI.getNpts())) {
                    dBlockO.set(dBlockI.getData(), dBlockI.getNpts(), dBlockI.getPtsize(), dBlockI.getDtype(), dBlockI.getMIMEType(), dBlockI.getWorder(), dBlockI.getIndivFlg(), dBlockI.getOffset(), dBlockI.getStride());
                } else {
                    java.util.Vector points = new java.util.Vector();
                    DataBlock ndBlock;
                    ndBlock = new DataBlock(null, 1, dBlockI.getPtsize(), dBlockI.getDtype(), dBlockI.getMIMEType(), dBlockI.getWorder(), dBlockI.getIndivFlg(), 0, dBlockI.getPtsize());
                    ndBlock.setData(dBlockI.getData());
                    dRef = new DataBlock(null, 1, dBlockI.getPtsize(), dBlockI.getDtype(), dBlockI.getMIMEType(), dBlockI.getWorder(), dBlockI.getIndivFlg(), 0, dBlockI.getPtsize());
                    for (int idx = 1; idx < values.size(); idx += 2) {
                        int vIdx = ((Integer) values.elementAt(idx)).intValue();
                        ndBlock.setOffset(dBlockI.getOffset() + vIdx * dBlockI.getStride());
                        Object element = ndBlock.extractData(dRef);
                        points.addElement(element);
                    }
                    dBlockO.set(points, values.size() / 2, dBlockI.getPtsize(), dBlockI.getDtype(), dBlockI.getMIMEType(), dBlockI.getWorder(), dBlockI.getIndivFlg(), 0, dBlockI.getPtsize());
                }
            }
        }
        return (true);
    }

    final byte getChanging() {
        if ((changing == UNKNOWN) && (getNptimes() > 0)) {
            if (getNptimes() == 1) {
                changing = INCREASING;
            } else {
                changing = (getPtimes()[1] >= getPtimes()[0]) ? INCREASING : DECREASING;
            }
            for (int idx = 2; (changing != RANDOM) && (idx < getNptimes()); ++idx) {
                byte pChanging = (getPtimes()[idx] >= getPtimes()[idx - 1]) ? INCREASING : DECREASING;
                if (pChanging != changing) {
                    changing = RANDOM;
                }
            }
        }
        return (changing);
    }

    final boolean getDirection() {
        return (direction);
    }

    public final double getDuration() {
        return (duration);
    }

    final boolean getInclusive() {
        return (inclusive);
    }

    public final double[] getLimits() {
        if (getNptimes() == 0) {
            return (null);
        }
        double addDuration = getDuration();
        if (addDuration == INHERIT_DURATION) {
            addDuration = 0.;
        }
        double[] valuesR = new double[2];
        if (changing == INCREASING) {
            valuesR[0] = getPtimes()[0];
            valuesR[1] = (getPtimes()[getNptimes() - 1] + addDuration);
        } else if (changing == DECREASING) {
            valuesR[1] = (valuesR[0] = getPtimes()[0]) + addDuration;
        } else {
            valuesR[1] = (valuesR[0] = getPtimes()[0]) + addDuration;
            for (int idx = 1; idx < getNptimes(); ++idx) {
                double min = getPtimes()[idx], max = min + addDuration;
                valuesR[0] = Math.min(valuesR[0], min);
                valuesR[1] = Math.max(valuesR[1], max);
            }
        }
        return (valuesR);
    }

    public final int getNptimes() {
        return ((ptimes == null) ? 0 : ptimes.length);
    }

    public final double getPointTime(int pointI, int nPointsI) {
        if (getNptimes() == 0) {
            throw new IllegalStateException("Inherited time values are not available to compute time.");
        } else if (nPointsI < 0) {
            throw new IllegalArgumentException("Negative input number of points.");
        } else if ((getNptimes() != 1) && (nPointsI != 0) && (nPointsI != getNptimes())) {
            throw new IllegalArgumentException("The number of input points does not match the number of " + "times.");
        }
        int nPoints = Math.max(nPointsI, getNptimes());
        if ((pointI < 0) || (pointI >= nPoints)) {
            throw new IllegalArgumentException("Point is not in range: 0 <= " + pointI + " < " + nPoints + ".");
        }
        double valueR;
        if (getNptimes() == 1) {
            valueR = getTime() + getDuration() * pointI / nPoints;
        } else {
            valueR = getPtimes()[pointI];
        }
        return (valueR);
    }

    public final double[] getPtimes() {
        return (ptimes);
    }

    public final double getTime() {
        return (ptimes[0]);
    }

    public boolean isInclusive() {
        return (getInclusive() || (getDuration() == 0.) || (getDuration() == INHERIT_DURATION));
    }

    final byte matches(TimeRange requestI) {
        byte matchesR = Rmap.MATCH_EQUAL;
        boolean reversed = getDirection() || requestI.getDirection();
        if ((requestI.getPtimes() == INHERIT_TIMES) || (getPtimes() == INHERIT_TIMES)) {
            matchesR = Rmap.MATCH_INTERSECTION;
        }
        double myDuration = getDuration(), requestDuration = requestI.getDuration();
        boolean allowMyHigh = isInclusive(), allowRequestHigh = requestI.isInclusive() || allowMyHigh;
        if (myDuration == INHERIT_DURATION) {
            myDuration = 0.;
        }
        if (requestDuration == INHERIT_DURATION) {
            requestDuration = 0.;
        }
        byte positive = reversed ? DECREASING : INCREASING, negative = reversed ? INCREASING : DECREASING;
        if (getChanging() == positive) {
            matchesR = matchMonotonic(reversed, 0, 0, 1, myDuration, allowMyHigh, requestI, requestDuration, allowRequestHigh);
        } else if (getChanging() == negative) {
            matchesR = matchMonotonic(reversed, 0, getNptimes() - 1, -1, myDuration, allowMyHigh, requestI, requestDuration, allowRequestHigh);
        } else if (requestI.getChanging() == positive) {
            matchesR = requestI.matchMonotonic(reversed, 1, 0, 1, requestDuration, allowRequestHigh, this, myDuration, allowMyHigh);
        } else if (requestI.getChanging() == negative) {
            matchesR = requestI.matchMonotonic(reversed, 1, requestI.getNptimes() - 1, -1, requestDuration, allowRequestHigh, this, myDuration, allowMyHigh);
        } else {
            matchesR = matchRandom(reversed, myDuration, allowMyHigh, requestI, requestDuration, allowRequestHigh);
        }
        if (reversed) {
            matchesR = MATCHES[matchesR - Rmap.MATCH_BEFORE][1];
        }
        return (matchesR);
    }

    private final byte matchMonotonic(boolean reversedI, int modeI, int startI, int incrementI, double myDurationI, boolean allowMyHighI, TimeRange otherI, double otherDurationI, boolean allowOtherHighI) {
        byte matchesR = Rmap.MATCH_EQUAL, positive = reversedI ? DECREASING : INCREASING, negative = reversedI ? INCREASING : DECREASING;
        if (otherI.getChanging() == positive) {
            matchesR = matchMonotonic2(reversedI, modeI, startI, incrementI, myDurationI, allowMyHighI, 0, 1, otherI, otherDurationI, allowOtherHighI);
        } else if (otherI.getChanging() == negative) {
            matchesR = matchMonotonic2(reversedI, modeI, startI, incrementI, myDurationI, allowMyHighI, otherI.getNptimes() - 1, -1, otherI, otherDurationI, allowOtherHighI);
        } else {
            matchesR = matchMonotonicRandom(reversedI, modeI, startI, incrementI, myDurationI, allowMyHighI, otherI, otherDurationI, allowOtherHighI);
        }
        return (matchesR);
    }

    private final byte matchMonotonic2(boolean reversedI, int modeI, int startI, int incrementI, double myDurationI, boolean allowMyHighI, int otherStartI, int otherIncrementI, TimeRange otherI, double otherDurationI, boolean allowOtherHighI) {
        int currentLow = 0;
        int[] numMatches = new int[4];
        boolean[] mineIntersected = new boolean[getNptimes()], otherIntersected = new boolean[otherI.getNptimes()];
        double myLowest = (reversedI ? -(getPtimes()[getNptimes() - 1 - startI] + myDurationI) : getPtimes()[startI]), myHighest = (reversedI ? -(getPtimes()[getNptimes() - 1 - startI] + myDurationI) : getPtimes()[startI]), otherLowest = (reversedI ? -(otherI.getPtimes()[otherI.getNptimes() - 1 - otherStartI] + otherDurationI) : otherI.getPtimes()[otherStartI]), otherHighest = (reversedI ? -otherI.getPtimes()[otherStartI] : (otherI.getPtimes()[otherI.getNptimes() - 1 - otherStartI] + otherDurationI));
        for (int tIdx = 0, idx = otherStartI; (tIdx < otherI.getNptimes()); ++tIdx, idx += otherIncrementI) {
            double otherLow = (reversedI ? -(otherI.getPtimes()[idx] + otherDurationI) : otherI.getPtimes()[idx]), otherHigh = otherLow + otherDurationI;
            for (int low = currentLow, high = getNptimes() - 1, idx1 = (low + high) / 2; (low <= high); idx1 = (low + high) / 2) {
                double myLow = (reversedI ? -(getPtimes()[startI + idx1 * incrementI] + myDurationI) : getPtimes()[startI + idx1 * incrementI]), myHigh = myLow + myDurationI;
                if ((otherLow > myHigh) || (!allowMyHighI && (otherLow == myHigh))) {
                    currentLow = low = idx1 + 1;
                } else if ((otherHigh < myLow) || (!allowOtherHighI && (otherHigh == myLow))) {
                    high = idx1 - 1;
                } else {
                    compareTimeIntervals(idx1, myLow, myHigh, allowMyHighI, mineIntersected, idx, otherLow, otherHigh, allowOtherHighI, otherIntersected, numMatches);
                    int idx2;
                    for (idx2 = idx1 - 1; idx2 >= low; --idx2) {
                        myLow = (reversedI ? -(getPtimes()[startI + idx2 * incrementI] + myDurationI) : getPtimes()[startI + idx2 * incrementI]);
                        myHigh = myLow + myDurationI;
                        if ((myHigh < otherLow) || (!allowMyHighI && (myHigh == otherLow))) {
                            break;
                        }
                        compareTimeIntervals(idx2, myLow, myHigh, allowMyHighI, mineIntersected, idx, otherLow, otherHigh, allowOtherHighI, otherIntersected, numMatches);
                    }
                    currentLow = idx2 + 1;
                    for (idx2 = idx1 + 1; idx2 <= high; ++idx2) {
                        myLow = (reversedI ? -(getPtimes()[startI + idx2 * incrementI] + myDurationI) : getPtimes()[startI + idx2 * incrementI]);
                        myHigh = myLow + myDurationI;
                        if ((myLow > otherHigh) || (!allowOtherHighI && (myLow == otherHigh))) {
                            break;
                        }
                        compareTimeIntervals(idx2, myLow, myHigh, allowMyHighI, mineIntersected, idx, otherLow, otherHigh, allowOtherHighI, otherIntersected, numMatches);
                    }
                    break;
                }
            }
        }
        byte resultR = determineResults(modeI, getNptimes(), myLowest, myHighest, allowMyHighI, mineIntersected, otherI.getNptimes(), otherLowest, otherHighest, allowOtherHighI, otherIntersected, numMatches);
        return (resultR);
    }

    private final byte matchMonotonicRandom(boolean reversedI, int modeI, int startI, int incrementI, double myDurationI, boolean allowMyHighI, TimeRange otherI, double otherDurationI, boolean allowOtherHighI) {
        int[] numMatches = new int[4];
        boolean[] mineIntersected = new boolean[getNptimes()], otherIntersected = new boolean[otherI.getNptimes()];
        double myLowest = (reversedI ? -(getPtimes()[getNptimes() - 1 - startI] + myDurationI) : getPtimes()[startI]), myHighest = (reversedI ? -getPtimes()[startI] : (getPtimes()[getNptimes() - 1 - startI] + myDurationI)), otherLowest = Double.MAX_VALUE, otherHighest = -Double.MAX_VALUE;
        for (int idx = 0; idx < otherI.getNptimes(); ++idx) {
            double otherLow = (reversedI ? -(otherI.getPtimes()[idx] + otherDurationI) : otherI.getPtimes()[idx]), otherHigh = otherLow + otherDurationI;
            otherLowest = Math.min(otherLow, otherLowest);
            otherHighest = Math.max(otherHigh, otherHighest);
            for (int low = 0, high = getNptimes() - 1, idx1 = (low + high) / 2; (low <= high); idx1 = (low + high) / 2) {
                double myLow = (reversedI ? -(getPtimes()[startI + idx1 * incrementI] + myDurationI) : getPtimes()[startI + idx1 * incrementI]), myHigh = myLow + myDurationI;
                if ((otherLow > myHigh) || (!allowMyHighI && (otherLow == myHigh))) {
                    low = idx1 + 1;
                } else if ((otherHigh < myLow) || (!allowOtherHighI && (otherHigh == myLow))) {
                    high = idx1 - 1;
                } else {
                    compareTimeIntervals(idx1, myLow, myHigh, allowMyHighI, mineIntersected, idx, otherLow, otherHigh, allowOtherHighI, otherIntersected, numMatches);
                    for (int idx2 = idx1 - 1; idx2 >= low; --idx2) {
                        myLow = (reversedI ? -(getPtimes()[startI + idx2 * incrementI] + myDurationI) : getPtimes()[startI + idx2 * incrementI]);
                        myHigh = myLow + myDurationI;
                        if ((myHigh < otherLow) || (!allowMyHighI && (myHigh == otherLow))) {
                            break;
                        }
                        compareTimeIntervals(idx2, myLow, myHigh, allowMyHighI, mineIntersected, idx, otherLow, otherHigh, allowOtherHighI, otherIntersected, numMatches);
                    }
                    for (int idx2 = idx1 + 1; idx2 <= high; ++idx2) {
                        myLow = (reversedI ? -(getPtimes()[startI + idx2 * incrementI] + myDurationI) : getPtimes()[startI + idx2 * incrementI]);
                        myHigh = myLow + myDurationI;
                        if ((myLow > otherHigh) || (!allowOtherHighI && (myLow == otherHigh))) {
                            break;
                        }
                        compareTimeIntervals(idx2, myLow, myHigh, allowMyHighI, mineIntersected, idx, otherLow, otherHigh, allowOtherHighI, otherIntersected, numMatches);
                    }
                }
            }
        }
        return (determineResults(modeI, getNptimes(), myLowest, myHighest, allowMyHighI, mineIntersected, otherI.getNptimes(), otherLowest, otherHighest, allowOtherHighI, otherIntersected, numMatches));
    }

    private final byte matchRandom(boolean reversedI, double myDurationI, boolean allowMyHighI, TimeRange requestI, double requestDurationI, boolean allowRequestHighI) {
        int[] numMatches = new int[4];
        boolean[] mineIntersected = new boolean[getNptimes()], requestIntersected = new boolean[requestI.getNptimes()];
        double myLowest = Double.MAX_VALUE, myHighest = -Double.MAX_VALUE, requestLowest = Double.MAX_VALUE, requestHighest = -Double.MAX_VALUE;
        for (int idx = 0; idx < getNptimes(); ++idx) {
            double myLow = (reversedI ? -(getPtimes()[idx] + myDurationI) : getPtimes()[idx]), myHigh = myLow + myDurationI;
            myLowest = Math.min(myLow, myLowest);
            myHighest = Math.max(myHigh, myHighest);
            for (int idx1 = 0; idx1 < requestI.getNptimes(); ++idx1) {
                double requestLow = (reversedI ? -(requestI.getPtimes()[idx1] + requestDurationI) : requestI.getPtimes()[idx1]), requestHigh = requestLow + requestDurationI;
                requestLowest = Math.min(requestLow, requestLowest);
                requestHighest = Math.max(requestHigh, requestHighest);
                compareTimeIntervals(idx, myLow, myHigh, allowMyHighI, mineIntersected, idx1, requestLow, requestHigh, allowRequestHighI, requestIntersected, numMatches);
            }
        }
        byte resultR = determineResults(0, getNptimes(), myLowest, myHighest, allowMyHighI, mineIntersected, requestI.getNptimes(), requestLowest, requestHighest, allowRequestHighI, requestIntersected, numMatches);
        return (resultR);
    }

    final TimeRelativeResponse matchTimeRelative(TimeRelativeRequest requestI, RequestOptions roI, int nPointsI) {
        TimeRelativeResponse responseR = new TimeRelativeResponse();
        if (getNptimes() == 1) {
            if (getDuration() == 0.) {
                switch(requestI.getRelationship()) {
                    case TimeRelativeRequest.AT_OR_BEFORE:
                    case TimeRelativeRequest.AT_OR_AFTER:
                        responseR.setStatus(0);
                        responseR.setTime(getTime());
                        responseR.setInvert(requestI.getRelationship() == TimeRelativeRequest.AT_OR_BEFORE);
                        break;
                    case TimeRelativeRequest.BEFORE:
                        responseR.setStatus(-1);
                        break;
                    case TimeRelativeRequest.AFTER:
                        responseR.setStatus(1);
                        break;
                }
            } else {
                double offset = requestI.getTimeRange().getTime() - getTime();
                double step = getDuration() / nPointsI;
                int point_old = (int) (offset / step);
                int point_round = (int) (0.5 + offset / step);
                int point = (int) (offset / step + 0.00000000000005);
                switch(requestI.getRelationship()) {
                    case TimeRelativeRequest.BEFORE:
                        if (--point < 0) {
                            responseR.setStatus(-1);
                        } else {
                            responseR.setTime(getTime() + (point + 1) * step);
                            responseR.setInvert(true);
                        }
                        break;
                    case TimeRelativeRequest.AT_OR_BEFORE:
                        if ((roI != null) && roI.getExtendStart()) {
                            responseR.setTime(getTime() + point * step);
                        } else {
                            responseR.setTime(getTime() + (point + 1) * step);
                            responseR.setInvert(true);
                        }
                        break;
                    case TimeRelativeRequest.AT_OR_AFTER:
                        if (point * step == offset) {
                            responseR.setTime(getTime() + point * step);
                            responseR.setInvert(false);
                        } else if (++point == nPointsI) {
                            responseR.setStatus(1);
                        } else {
                            responseR.setTime(getTime() + point * step);
                            responseR.setInvert(false);
                        }
                        break;
                    case TimeRelativeRequest.AFTER:
                        if (++point >= nPointsI) {
                            responseR.setStatus(1);
                        } else {
                            if (requestI.getTimeRange().getDuration() == 0.0) {
                                responseR.setTime(getTime() + point * step + 0.00000000000005);
                            } else {
                                responseR.setTime(getTime() + point * step - 0.00000000000005);
                            }
                            responseR.setInvert(false);
                        }
                }
            }
        } else {
            int lo = 0;
            int hi = nPointsI - 1;
            int lastIdx = 0;
            int idx;
            double direction = 0.;
            double time = 0.;
            for (idx = (lo + hi) / 2; lo <= hi; idx = (lo + hi) / 2) {
                time = getPointTime(idx, nPointsI);
                direction = requestI.getTimeRange().getTime() - time;
                lastIdx = idx;
                if (direction < 0.) {
                    hi = idx - 1;
                } else if (direction == 0.) {
                    break;
                } else {
                    lo = idx + 1;
                }
            }
            if (lo <= hi) {
                double lPtTime = 0.;
                double wPtTime = getPointTime(lastIdx, nPointsI);
                switch(requestI.getRelationship()) {
                    case TimeRelativeRequest.AT_OR_BEFORE:
                    case TimeRelativeRequest.AT_OR_AFTER:
                        responseR.setStatus(0);
                        responseR.setTime(time);
                        responseR.setInvert(requestI.getRelationship() == TimeRelativeRequest.AT_OR_BEFORE);
                        break;
                    case TimeRelativeRequest.BEFORE:
                        for (; lastIdx > 0; --lastIdx) {
                            lPtTime = getPointTime(lastIdx - 1, nPointsI);
                            if (lPtTime < wPtTime) {
                                break;
                            }
                        }
                        if (lastIdx == 0) {
                            responseR.setStatus(-1);
                        } else {
                            responseR.setTime(lPtTime);
                            responseR.setInvert(true);
                        }
                        break;
                    case TimeRelativeRequest.AFTER:
                        for (; lastIdx < nPointsI - 1; ++lastIdx) {
                            lPtTime = getPointTime(lastIdx + 1, nPointsI);
                            if (lPtTime > wPtTime) {
                                break;
                            }
                        }
                        if (lastIdx == nPointsI - 1) {
                            responseR.setStatus(1);
                        } else {
                            responseR.setTime(lPtTime);
                            responseR.setInvert(false);
                        }
                        break;
                }
            } else if ((lastIdx == 0) && (direction < 0)) {
                responseR.setStatus(-1);
            } else if ((lastIdx == nPointsI - 1) && (direction > 0)) {
                responseR.setStatus(1);
            } else {
                if (direction > 0) {
                    ++lastIdx;
                }
                switch(requestI.getRelationship()) {
                    case TimeRelativeRequest.BEFORE:
                    case TimeRelativeRequest.AT_OR_BEFORE:
                        responseR.setTime(getPointTime(lastIdx - 1, nPointsI));
                        responseR.setInvert(true);
                        break;
                    case TimeRelativeRequest.AT_OR_AFTER:
                    case TimeRelativeRequest.AFTER:
                        responseR.setTime(getPointTime(lastIdx, nPointsI));
                        responseR.setInvert(false);
                        break;
                }
            }
        }
        return (responseR);
    }

    private final TimeRange mergeOffsets(TimeRange iRangeI, DataBlock iBlockI) {
        TimeRange tRangeR = null;
        boolean tInclusive = getInclusive();
        double rDuration = getDuration();
        if (rDuration == INHERIT_DURATION) {
            rDuration = iRangeI.getDuration();
            tInclusive = tInclusive || iRangeI.getInclusive();
        }
        if (getPtimes() == INHERIT_TIMES) {
            tRangeR = new TimeRange(iRangeI.getPtimes(), rDuration);
        } else {
            double ltimes[] = new double[iBlockI.getNpts() * getNptimes()];
            for (int idx = 0, idx2 = 0, iNpts = iBlockI.getNpts(); idx < iNpts; ++idx) {
                double itime = iRangeI.getPointTime(idx, iNpts);
                for (int idx1 = 0; idx1 < getNptimes(); ++idx1, ++idx2) {
                    ltimes[idx2] = itime + getPtimes()[idx1];
                }
            }
            tRangeR = new TimeRange(ltimes, rDuration);
        }
        tRangeR.setInclusive(tInclusive);
        return (tRangeR);
    }

    public final void nullify() {
        setPtimes(null);
    }

    final void read(InputStream isI, DataInputStream disI) throws com.rbnb.api.SerializeException, java.io.EOFException, java.io.IOException {
        read(null, isI, disI);
    }

    final void read(com.rbnb.api.TimeRange otherI, InputStream isI, DataInputStream disI) throws com.rbnb.api.SerializeException, java.io.EOFException, java.io.IOException {
        boolean[] seen = new boolean[PARAMETERS.length];
        Serialize.readOpenBracket(isI);
        int parameter;
        while ((parameter = Serialize.readParameter(PARAMETERS, isI)) != -1) {
            seen[parameter] = true;
            switch(parameter) {
                case PAR_DUR:
                    setDuration(isI.readDouble());
                    break;
                case PAR_INC:
                    setInclusive(isI.readBoolean());
                    break;
                case PAR_PTM:
                    setPtimes(new double[isI.readInt()]);
                    for (int idx = 0; idx < getNptimes(); ++idx) {
                        getPtimes()[idx] = isI.readDouble();
                    }
                    break;
                case PAR_STA:
                    setPtimes(new double[1]);
                    getPtimes()[0] = isI.readDouble();
                    break;
            }
        }
        if (otherI != null) {
            if (!seen[PAR_DUR]) {
                setDuration(otherI.getDuration());
            }
            if (!seen[PAR_PTM] && !seen[PAR_STA]) {
                setPtimes((double[]) otherI.getPtimes().clone());
            }
            if (!seen[PAR_INC]) {
                setInclusive(otherI.getInclusive());
            }
        }
    }

    public final void set(double timeI) {
        set(timeI, INHERIT_DURATION);
    }

    public final void set(double startI, double durationI) {
        if ((durationI < 0.) && (duration != INHERIT_DURATION) && ((durationI != -Double.MAX_VALUE / 10.) || (startI != 0.))) {
            throw new IllegalStateException("Cannot set negative duration. Value = " + durationI);
        }
        setPtimes(new double[1]);
        ptimes[0] = startI;
        setDuration(durationI);
        changing = UNKNOWN;
    }

    public final void set(double[] ptimesI) {
        set(ptimesI, INHERIT_DURATION);
    }

    public final void set(double[] ptimesI, double durationI) {
        if ((durationI < 0.) && (duration != INHERIT_DURATION)) {
            throw new IllegalStateException("Cannot set negative duration.");
        }
        setPtimes(ptimesI);
        setDuration(durationI);
        changing = UNKNOWN;
    }

    final void setDirection(boolean directionI) {
        direction = directionI;
    }

    public final void setDuration(double durationI) {
        duration = durationI;
    }

    public final void setInclusive(boolean inclusiveI) {
        inclusive = inclusiveI;
    }

    public final void setPtimes(double[] ptimesI) {
        ptimes = ptimesI;
    }

    final TimeRange subtract(TimeRange otherI) {
        if (otherI == null) {
            return (this);
        }
        if ((getPtimes() == INHERIT_TIMES) || (getDuration() == INHERIT_DURATION)) {
            throw new java.lang.IllegalArgumentException("Cannot subtract " + otherI + " from " + this);
        }
        double oDuration = otherI.getDuration();
        if (otherI.getDuration() == INHERIT_DURATION) {
            oDuration = getDuration();
        }
        double fDuration = Math.min(getDuration(), oDuration);
        TimeRange differenceR = null;
        if (otherI.getPtimes() == INHERIT_TIMES) {
            differenceR = new TimeRange(getPtimes(), fDuration);
        } else if ((getChanging() != INCREASING) || (otherI.getChanging() != INCREASING)) {
            throw new java.lang.IllegalStateException("Cannot subtract " + otherI + " from " + this);
        } else {
            boolean reversed = (getDirection() || otherI.getDirection());
            java.util.Vector values = new java.util.Vector();
            int theirBase = 0;
            for (int idx = 0; idx < getNptimes(); ++idx) {
                double myMin = getPtimes()[idx], myMax = myMin + getDuration(), cMyMin = (reversed ? -myMax : myMin), cMyMax = (reversed ? -myMin : myMax);
                for (int idx2 = theirBase; idx2 < otherI.getNptimes(); ++idx2) {
                    double theirMin = otherI.getPtimes()[idx2], theirMax = theirMin + oDuration, cTheirMin = (reversed ? -theirMax : theirMin), cTheirMax = (reversed ? -theirMin : theirMax);
                    if (cMyMin < cTheirMin) {
                        if ((cMyMax > cTheirMin) || (((getDuration() == 0.) || (oDuration == 0.)) && (cMyMax == cTheirMin))) {
                            double value = Math.min(myMax, theirMax) - fDuration - theirMin;
                            values.addElement(new Double(value));
                        }
                    } else if (cMyMax > cTheirMax) {
                        if ((cMyMin < cTheirMax) || (((getDuration() == 0) || (oDuration == 0.)) && (cMyMin == cTheirMax))) {
                            values.addElement(new Double(0.));
                        }
                    } else {
                        values.addElement(new Double(myMin - theirMin));
                    }
                }
            }
            if (values.size() == 0) {
                throw new java.lang.IllegalArgumentException("No intersection between " + this + " and " + otherI);
            }
            double[] lptimes = new double[values.size()];
            for (int idx = 0; idx < lptimes.length; ++idx) {
                lptimes[idx] = ((Double) values.elementAt(idx)).doubleValue();
            }
            differenceR = new TimeRange(lptimes, fDuration);
        }
        differenceR.setInclusive(getInclusive() && otherI.getInclusive());
        return (differenceR);
    }

    public String toString() {
        String stringR = "";
        if (getNptimes() > 0) {
            try {
                stringR += "[" + ToString.toString("%.17f", getPtimes()[0]);
                for (int idx = 1; idx < getNptimes(); ++idx) {
                    stringR += "," + ToString.toString("%.17f", getPtimes()[idx]);
                }
                stringR += "]";
            } catch (Exception e) {
            }
        }
        if (getDuration() != INHERIT_DURATION) {
            stringR += "+" + getDuration();
        }
        if (getDirection()) {
            stringR += " (reversed comparisons)";
        }
        if (isInclusive()) {
            stringR += " (inclusive)";
        }
        return (stringR);
    }

    final void write(String[] parametersI, int parameterI, OutputStream osI, DataOutputStream dosI) throws java.io.IOException {
        write(null, parametersI, parameterI, osI, dosI);
    }

    final void write(TimeRange otherI, String[] parametersI, int parameterI, OutputStream osI, DataOutputStream dosI) throws java.io.IOException {
        boolean trChanged = false;
        if (otherI == null) {
            trChanged = true;
        } else {
            trChanged = (getInclusive() != otherI.getInclusive()) || (getDuration() != otherI.getDuration()) || (getPtimes() == otherI.getPtimes());
            if (!trChanged) {
                trChanged = getNptimes() != otherI.getNptimes();
                for (int idx = 0; !trChanged && (idx < getNptimes()); ++idx) {
                    trChanged = getPtimes()[idx] != otherI.getPtimes()[idx];
                }
            }
        }
        if (trChanged) {
            osI.writeParameter(parametersI, parameterI);
            Serialize.writeOpenBracket(osI);
            if (getDuration() != INHERIT_DURATION) {
                osI.writeParameter(PARAMETERS, PAR_DUR);
                osI.writeDouble(getDuration());
            }
            if (IsSupported.isSupported(IsSupported.FEATURE_TIME_RANGE_INCLUSIVE, osI.getBuildVersion(), osI.getBuildDate())) {
                osI.writeParameter(PARAMETERS, PAR_INC);
                osI.writeBoolean(getInclusive());
            }
            if (getNptimes() == 1) {
                osI.writeParameter(PARAMETERS, PAR_STA);
                osI.writeDouble(getTime());
            } else if (getPtimes() != INHERIT_TIMES) {
                osI.writeParameter(PARAMETERS, PAR_PTM);
                osI.writeInt(getNptimes());
                for (int idx = 0; idx < getNptimes(); ++idx) {
                    osI.writeDouble(getPtimes()[idx]);
                }
            }
            Serialize.writeCloseBracket(osI);
        }
    }
}
