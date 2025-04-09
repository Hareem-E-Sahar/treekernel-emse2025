package com.rbnb.api;

public class DataArray implements com.rbnb.compat.Cloneable {

    /**
     * the data.
     * <p>
     * This field needs to be cast to the appropriate array type to be used.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 11/30/2000
     */
    private Object data = null;

    /**
     * the data type.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/17/2002
     */
    private byte dType = DataBlock.UNKNOWN;

    /**
     * the frame information.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0;
     * @version 04/12/2002
     */
    java.util.Vector frameRanges = null;

    /**
     * the individual frame values.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/15/2002
     */
    private double[] frames = null;

    /**
     * the current number of points in the data array.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/12/2002
     */
    private int numberInArray = 0;

    /**
     * the total number of points.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/12/2002
     */
    private int numberOfPoints = 0;

    /**
     * the MIME type.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 01/24/2002
     */
    private String mimeType = null;

    /**
     * the number of points per <code>TimeRange</code>.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/12/2002
     */
    java.util.Vector pointsPerRange = null;

    /**
     * the point size.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 09/17/2002
     */
    private int ptSize = 0;

    /**
     * the time information.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0;
     * @version 04/12/2002
     */
    java.util.Vector timeRanges = null;

    /**
     * the individual times.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 04/15/2002
     */
    private double[] times = null;

    public DataArray() {
        super();
    }

    final void add(int nPointsI, DataBlock dBlockI, TimeRange tRangeI, TimeRange fRangeI) {
        if (numberInArray + nPointsI > getNumberOfPoints()) {
            throw new java.lang.IllegalArgumentException("Cannot add " + nPointsI + " additional points to data array.");
        } else if (tRangeI != null) {
            if (numberInArray == 0) {
                timeRanges = new java.util.Vector();
                if (fRangeI != null) {
                    frameRanges = new java.util.Vector();
                }
                insertData(nPointsI, dBlockI, tRangeI, fRangeI, timeRanges, frameRanges);
            } else if (timeRanges == null) {
                addData(nPointsI, dBlockI);
            } else {
                insertData(nPointsI, dBlockI, tRangeI, fRangeI, timeRanges, frameRanges);
            }
        } else if (fRangeI != null) {
            if (numberInArray == 0) {
                frameRanges = new java.util.Vector();
                insertData(nPointsI, dBlockI, fRangeI, null, frameRanges, timeRanges);
            } else if (frameRanges == null) {
                addData(nPointsI, dBlockI);
            } else {
                insertData(nPointsI, dBlockI, fRangeI, null, frameRanges, timeRanges);
            }
        } else if (dBlockI != null) {
            addData(nPointsI, dBlockI);
        }
    }

    private final void addData(int nPointsI, DataBlock dBlockI) {
        if ((frameRanges != null) || (timeRanges != null)) {
            throw new java.lang.IllegalArgumentException("Cannot add data without time or frame information to a " + "data array containing time or frame information.");
        }
        if ((dBlockI != null) || (data != null)) {
            if (dBlockI.getDtype() == DataBlock.TYPE_BYTEARRAY) {
                byte[][] ba = (byte[][]) data;
                for (int idx = 0; idx < nPointsI; ++idx) {
                    ba[numberInArray + idx] = new byte[dBlockI.getPtsize()];
                }
            }
            dBlockI.getDataPoints(0, data, numberInArray, nPointsI);
        }
        numberInArray += nPointsI;
    }

    private static final void addRange(int sIndexI, int nPointsI, int tPointsI, TimeRange rangeI, int storeIndexI, java.util.Vector storeI) {
        TimeRange range;
        if (nPointsI == tPointsI) {
            range = rangeI;
        } else if (rangeI.getNptimes() == 1) {
            range = new TimeRange(rangeI.getPointTime(sIndexI, tPointsI), (nPointsI * rangeI.getDuration() / tPointsI));
        } else {
            double[] times = new double[nPointsI];
            System.arraycopy(rangeI.getPtimes(), sIndexI, times, 0, nPointsI);
            range = new TimeRange(times, rangeI.getDuration());
        }
        storeI.insertElementAt(range, storeIndexI);
    }

    public final Object clone() {
        try {
            DataArray clonedR = new DataArray();
            clonedR.data = data;
            clonedR.dType = dType;
            clonedR.frameRanges = frameRanges;
            clonedR.frames = frames;
            clonedR.numberInArray = numberInArray;
            clonedR.numberOfPoints = numberOfPoints;
            clonedR.mimeType = mimeType;
            clonedR.pointsPerRange = pointsPerRange;
            clonedR.ptSize = ptSize;
            clonedR.timeRanges = timeRanges;
            clonedR.times = times;
            return clonedR;
        } catch (Exception e) {
            return (null);
        }
    }

    private final DataBlock extractDataBlock(int startI, int nPointsI) {
        DataBlock dataBlockR = null;
        if (nPointsI == numberOfPoints) {
            if ((dType != DataBlock.TYPE_STRING) && (dType != DataBlock.TYPE_BYTEARRAY)) {
                dataBlockR = new DataBlock(data, nPointsI, ptSize, dType, DataBlock.ORDER_MSB, false, 0, ptSize);
            }
        } else {
            Object nData = null;
            switch(dType) {
                case DataBlock.TYPE_BOOLEAN:
                    boolean[] boolData = new boolean[nPointsI];
                    System.arraycopy(getData(), startI, boolData, 0, nPointsI);
                    nData = boolData;
                    break;
                case DataBlock.TYPE_INT8:
                    if (ptSize == 1) {
                        byte[] byteData = new byte[nPointsI];
                        System.arraycopy(getData(), startI, byteData, 0, nPointsI);
                        nData = byteData;
                    } else {
                        byte[][] baData = new byte[nPointsI][], obaData = (byte[][]) getData();
                        for (int idx = 0; idx < nPointsI; ++idx) {
                            baData[idx] = new byte[ptSize];
                            System.arraycopy(obaData[startI + idx], 0, baData[idx], 0, ptSize);
                        }
                        nData = baData;
                    }
                    break;
                case DataBlock.TYPE_INT16:
                    short[] shortData = new short[nPointsI];
                    System.arraycopy(getData(), startI, shortData, 0, nPointsI);
                    nData = shortData;
                    break;
                case DataBlock.TYPE_INT32:
                    int[] intData = new int[nPointsI];
                    System.arraycopy(getData(), startI, intData, 0, nPointsI);
                    nData = intData;
                    break;
                case DataBlock.TYPE_INT64:
                    long[] longData = new long[nPointsI];
                    System.arraycopy(getData(), startI, longData, 0, nPointsI);
                    nData = longData;
                    break;
                case DataBlock.TYPE_FLOAT32:
                    float[] floatData = new float[nPointsI];
                    System.arraycopy(getData(), startI, floatData, 0, nPointsI);
                    nData = floatData;
                    break;
                case DataBlock.TYPE_FLOAT64:
                    double[] doubleData = new double[nPointsI];
                    System.arraycopy(getData(), startI, doubleData, 0, nPointsI);
                    nData = doubleData;
                    break;
            }
            dataBlockR = new DataBlock(nData, nPointsI, ptSize, dType, DataBlock.ORDER_MSB, false, 0, ptSize);
        }
        dataBlockR.setMIMEType(getMIMEType());
        return (dataBlockR);
    }

    public final double getDuration() {
        double durationR = 0.;
        if (timeRanges != null) {
            double startTime = getStartTime();
            TimeRange lTrange = (TimeRange) timeRanges.lastElement();
            double endTime = (lTrange.getPtimes()[lTrange.getNptimes() - 1] + lTrange.getDuration());
            durationR = endTime - startTime;
        }
        return (durationR);
    }

    private final int findInsertionPoint(int pIndexI, int nPointsI, TimeRange refI, java.util.Vector refStoreI, java.util.Vector otherStoreI) {
        int low = 0, high = refStoreI.size() - 1;
        double ptime = refI.getPointTime(pIndexI, nPointsI), erange;
        for (int idx = (high + low) / 2; (high >= low); idx = (high + low) / 2) {
            TimeRange tRange = (TimeRange) refStoreI.elementAt(idx);
            boolean greater = (tRange.getDuration() == 0.);
            if (ptime < tRange.getTime()) {
                high = idx - 1;
            } else if ((ptime > (erange = (tRange.getPtimes()[tRange.getNptimes() - 1] + tRange.getDuration()))) || (!greater && (ptime == erange))) {
                low = idx + 1;
            } else {
                int nPoints = ((pointsPerRange != null) ? ((Integer) pointsPerRange.elementAt(idx)).intValue() : tRange.getNptimes());
                if (nPoints == 1) {
                    low = idx + 1;
                    continue;
                }
                TimeRange other = ((otherStoreI == null) ? null : (TimeRange) otherStoreI.elementAt(idx));
                int lPoint, nPoints2;
                if (tRange.getNptimes() == 1) {
                    double value = ptime - tRange.getTime();
                    lPoint = (int) (value * nPoints / tRange.getDuration());
                } else {
                    int low2 = 0, high2 = tRange.getNptimes() - 1;
                    for (int idx1 = (low2 + high2) / 2; low2 <= high2; idx1 = (low2 + high2) / 2) {
                        double ttime = tRange.getPtimes()[idx1];
                        if (ptime < ttime) {
                            high2 = idx1 - 1;
                        } else {
                            low2 = idx1 + 1;
                        }
                    }
                    lPoint = high2;
                }
                nPoints2 = lPoint + 1;
                if (nPoints2 < nPoints) {
                    numberInArray -= (nPoints - nPoints2);
                    TimeRange ref2, other2, ref3 = null, other3 = null;
                    if (tRange.getNptimes() == 1) {
                        ref2 = new TimeRange(tRange.getTime(), nPoints2 * tRange.getDuration() / nPoints);
                        ref3 = new TimeRange(tRange.getPointTime(nPoints2, nPoints), tRange.getDuration() * (nPoints - nPoints2) / nPoints);
                    } else {
                        double[] times = new double[nPoints2];
                        System.arraycopy(tRange.getPtimes(), 0, times, 0, nPoints2);
                        ref2 = new TimeRange(times, tRange.getDuration());
                        times = new double[nPoints - nPoints2];
                        System.arraycopy(tRange.getPtimes(), nPoints2, times, 0, times.length);
                        ref3 = new TimeRange(times, tRange.getDuration());
                    }
                    refStoreI.setElementAt(ref2, idx);
                    if (other != null) {
                        if (other.getNptimes() == 1) {
                            other2 = new TimeRange(other.getTime(), nPoints2 * other.getDuration() / nPoints);
                            other3 = new TimeRange(other.getPointTime(nPoints2, nPoints), other.getDuration() * (nPoints - nPoints2) / nPoints);
                        } else {
                            double[] times = new double[nPoints2];
                            System.arraycopy(other.getPtimes(), 0, times, 0, nPoints2);
                            other2 = new TimeRange(times, other.getDuration());
                            times = new double[nPoints - nPoints2];
                            System.arraycopy(other.getPtimes(), nPoints2, times, 0, times.length);
                            other3 = new TimeRange(times, other.getDuration());
                        }
                        otherStoreI.setElementAt(other2, idx);
                    }
                    if (pointsPerRange != null) {
                        pointsPerRange.setElementAt(new Integer(nPoints2), idx);
                    }
                    if (nPoints2 < nPoints) {
                        insertConsecutive(0, nPoints - nPoints2, nPoints - nPoints2, null, ref3, other3, idx + 1, refStoreI, otherStoreI);
                    }
                }
                low = idx + 1;
                break;
            }
        }
        return (low);
    }

    public final Object getData() {
        return (data);
    }

    public final int getDataType() {
        return dType;
    }

    public final int getPointSize() {
        return ptSize;
    }

    public final int getNumInArray() {
        return numberInArray;
    }

    public final double[] getFrame() {
        if (frames == null) {
            if (frameRanges != null) {
                frames = new double[getNumberOfPoints()];
                if (pointsPerRange != null) {
                    for (int idx = 0, idx1 = 0; idx < frameRanges.size(); ++idx) {
                        TimeRange fRange = (TimeRange) frameRanges.elementAt(idx);
                        int ppR = ((Integer) pointsPerRange.elementAt(idx)).intValue();
                        for (int idx2 = 0; idx2 < ppR; ++idx2, ++idx1) {
                            frames[idx1] = fRange.getPointTime(idx2, ppR);
                        }
                    }
                } else if (frameRanges.size() == getNumberOfPoints()) {
                    for (int idx = 0; idx < frameRanges.size(); ++idx) {
                        TimeRange fRange = (TimeRange) frameRanges.elementAt(idx);
                        frames[idx] = fRange.getTime();
                    }
                } else if (timeRanges != null) {
                    for (int idx = 0, idx1 = 0; idx < timeRanges.size(); ++idx) {
                        TimeRange tRange = (TimeRange) timeRanges.elementAt(idx), fRange = (TimeRange) frameRanges.elementAt(idx);
                        for (int idx2 = 0; idx2 < tRange.getNptimes(); ++idx2, ++idx1) {
                            frames[idx1] = fRange.getPointTime(idx2, tRange.getNptimes());
                        }
                    }
                } else {
                    for (int idx = 0, idx1 = 0; idx < frameRanges.size(); ++idx) {
                        TimeRange fRange = (TimeRange) frameRanges.elementAt(idx);
                        System.arraycopy(fRange.getPtimes(), 0, frames, idx1, fRange.getNptimes());
                        idx1 += fRange.getNptimes();
                    }
                }
            }
        }
        return (frames);
    }

    public final String getMIMEType() {
        return (mimeType);
    }

    public final int getNumberOfPoints() {
        return (numberOfPoints);
    }

    public final double getStartTime() {
        double startTimeR = -Double.MAX_VALUE;
        if (timeRanges != null) {
            startTimeR = ((TimeRange) timeRanges.elementAt(0)).getTime();
        }
        return (startTimeR);
    }

    public final double[] getTime() {
        if (times == null) {
            if (timeRanges != null) {
                times = new double[getNumberOfPoints()];
                if (pointsPerRange != null) {
                    for (int idx = 0, idx1 = 0; idx < timeRanges.size(); ++idx) {
                        TimeRange tRange = (TimeRange) timeRanges.elementAt(idx);
                        int ppR = ((Integer) pointsPerRange.elementAt(idx)).intValue();
                        for (int idx2 = 0; idx2 < ppR; ++idx2, ++idx1) {
                            times[idx1] = tRange.getPointTime(idx2, ppR);
                        }
                    }
                } else if (timeRanges.size() == getNumberOfPoints()) {
                    for (int idx = 0; idx < timeRanges.size(); ++idx) {
                        TimeRange tRange = (TimeRange) timeRanges.elementAt(idx);
                        times[idx] = tRange.getTime();
                    }
                } else {
                    for (int idx = 0, idx1 = 0; idx < timeRanges.size(); ++idx) {
                        TimeRange tRange = (TimeRange) timeRanges.elementAt(idx);
                        System.arraycopy(tRange.getPtimes(), 0, times, idx1, tRange.getNptimes());
                        idx1 += tRange.getNptimes();
                    }
                }
            }
        }
        return (times);
    }

    private final void insertConsecutive(int sIndexI, int nPointsI, int tPointsI, DataBlock dBlockI, TimeRange refI, TimeRange otherI, int refIndexI, java.util.Vector refStoreI, java.util.Vector otherStoreI) {
        if ((dBlockI != null) && (data != null)) {
            int dIndex = numberInArray;
            if (refIndexI < refStoreI.size()) {
                dIndex = 0;
                if (pointsPerRange == null) {
                    TimeRange tRange;
                    for (int idx = 0; idx < refIndexI; ++idx) {
                        tRange = (TimeRange) refStoreI.elementAt(idx);
                        dIndex += tRange.getNptimes();
                    }
                } else {
                    for (int idx = 0; idx < refIndexI; ++idx) {
                        dIndex += ((Integer) pointsPerRange.elementAt(idx)).intValue();
                    }
                }
                if (dBlockI.getDtype() == DataBlock.TYPE_BYTEARRAY) {
                    byte[][] ba = (byte[][]) data;
                    for (int idx = numberInArray - dIndex - 1; idx >= 0; --idx) {
                        ba[dIndex + nPointsI + idx] = ba[dIndex + idx];
                    }
                    for (int idx = 0; idx < nPointsI; ++idx) {
                        ba[dIndex + idx] = new byte[dBlockI.getPtsize()];
                    }
                } else {
                    if (dIndex < numberInArray) {
                        System.arraycopy(data, dIndex, data, dIndex + nPointsI, numberInArray - dIndex);
                    }
                }
            }
            dBlockI.getDataPoints(sIndexI, data, dIndex, nPointsI);
        }
        if ((nPointsI != refI.getNptimes()) || (pointsPerRange != null)) {
            if (pointsPerRange == null) {
                pointsPerRange = new java.util.Vector();
                for (int idx = 0; idx < refStoreI.size(); ++idx) {
                    TimeRange tRange = (TimeRange) refStoreI.elementAt(idx);
                    pointsPerRange.addElement(new Integer(tRange.getNptimes()));
                }
            }
            pointsPerRange.insertElementAt(new Integer(nPointsI), refIndexI);
        }
        addRange(sIndexI, nPointsI, tPointsI, refI, refIndexI, refStoreI);
        if ((otherI != null) && (otherStoreI != null)) {
            addRange(sIndexI, nPointsI, tPointsI, otherI, refIndexI, otherStoreI);
        }
        numberInArray += nPointsI;
    }

    private final void insertData(int nPointsI, DataBlock dBlockI, TimeRange refI, TimeRange otherI, java.util.Vector refStoreI, java.util.Vector otherStoreI) {
        byte changing = refI.getChanging();
        if ((changing == TimeRange.UNKNOWN) || (changing == TimeRange.RANDOM) || (changing == TimeRange.DECREASING)) {
            insertRandom(nPointsI, dBlockI, refI, otherI, refStoreI, otherStoreI);
        } else if (numberInArray == 0) {
            insertConsecutive(0, nPointsI, nPointsI, dBlockI, refI, otherI, 0, refStoreI, otherStoreI);
        } else {
            TimeRange last = (TimeRange) refStoreI.lastElement();
            if (refI.getTime() >= (last.getPtimes()[last.getNptimes() - 1] + last.getDuration())) {
                insertConsecutive(0, nPointsI, nPointsI, dBlockI, refI, otherI, refStoreI.size(), refStoreI, otherStoreI);
            } else {
                insertRandom(nPointsI, dBlockI, refI, otherI, refStoreI, otherStoreI);
            }
        }
    }

    private final void insertRandom(int nPointsI, DataBlock dBlockI, TimeRange refI, TimeRange otherI, java.util.Vector refStoreI, java.util.Vector otherStoreI) {
        int sIndex = 0, refIndex = -1;
        for (int idx = 0; idx < nPointsI; ++idx) {
            int nIndex = findInsertionPoint(idx, nPointsI, refI, refStoreI, otherStoreI);
            if ((refIndex != -1) && (nIndex != refIndex) && (idx != sIndex)) {
                insertConsecutive(sIndex, idx - sIndex, nPointsI, dBlockI, refI, otherI, refIndex, refStoreI, otherStoreI);
                refIndex = -1;
                ++nIndex;
            }
            if (refIndex == -1) {
                sIndex = idx;
            }
            refIndex = nIndex;
        }
        insertConsecutive(sIndex, nPointsI - sIndex, nPointsI, dBlockI, refI, otherI, refIndex, refStoreI, otherStoreI);
    }

    final void setMIMEType(String mimeTypeI) {
        mimeType = mimeTypeI;
    }

    final void setNumberOfPoints(int numberOfPointsI, int ptSizeI, byte dTypeI) {
        numberOfPoints = numberOfPointsI;
        dType = dTypeI;
        ptSize = ptSizeI;
        if (ptSizeI > 0) {
            switch(dTypeI) {
                case DataBlock.TYPE_BOOLEAN:
                    data = new boolean[numberOfPointsI];
                    break;
                case DataBlock.TYPE_INT16:
                    data = new short[numberOfPointsI];
                    break;
                case DataBlock.TYPE_INT32:
                    data = new int[numberOfPointsI];
                    break;
                case DataBlock.TYPE_INT64:
                    data = new long[numberOfPointsI];
                    break;
                case DataBlock.TYPE_FLOAT32:
                    data = new float[numberOfPointsI];
                    break;
                case DataBlock.TYPE_FLOAT64:
                    data = new double[numberOfPointsI];
                    break;
                case DataBlock.TYPE_STRING:
                    data = new String[numberOfPointsI];
                    break;
                case DataBlock.TYPE_INT8:
                case DataBlock.TYPE_BYTEARRAY:
                case DataBlock.UNKNOWN:
                default:
                    if (((dType == DataBlock.TYPE_INT8) || (dType == DataBlock.UNKNOWN)) && (ptSizeI == 1)) {
                        data = new byte[numberOfPointsI];
                    } else {
                        byte[][] dataT = new byte[numberOfPointsI][];
                        if ((dType == DataBlock.TYPE_INT8) || (dType == DataBlock.UNKNOWN)) {
                            for (int idx = 0; idx < numberOfPointsI; ++idx) {
                                dataT[idx] = null;
                            }
                        }
                        data = dataT;
                    }
                    break;
            }
        }
    }

    final Rmap toRmap() throws com.rbnb.api.AddressException, com.rbnb.api.SerializeException, java.io.EOFException, java.io.IOException, java.lang.InterruptedException {
        if ((timeRanges == null) || (dType == DataBlock.TYPE_STRING) || (dType == DataBlock.TYPE_BYTEARRAY) || (dType == DataBlock.UNKNOWN) || (ptSize == 0)) {
            return (null);
        }
        Rmap rmapR = new Rmap();
        TimeRange ltRange = (TimeRange) ((TimeRange) timeRanges.firstElement()).clone();
        TimeRange workRange;
        DataBlock ldBlock;
        int lnPoints = ((pointsPerRange == null) ? ((timeRanges.size() > 1) ? ((TimeRange) timeRanges.firstElement()).getPtimes().length : numberOfPoints) : ((Integer) pointsPerRange.firstElement()).intValue());
        int workPoints;
        int startPoint = 0;
        double duration = ltRange.getDuration() / lnPoints;
        double[] pTimes;
        java.util.Vector times = null;
        if (duration == 0.) {
            times = new java.util.Vector();
            if (ltRange.getPtimes().length == lnPoints) {
                times.addElement(ltRange.getPtimes());
            } else {
                for (int idx = 0; idx < lnPoints; ++idx) {
                    times.addElement(ltRange.getPtimes());
                }
            }
        }
        for (int idx = 1; idx < timeRanges.size(); ++idx) {
            workRange = (TimeRange) timeRanges.elementAt(idx);
            if (pointsPerRange == null) {
                workPoints = workRange.getPtimes().length;
            } else {
                workPoints = ((Integer) pointsPerRange.elementAt(idx)).intValue();
            }
            if (duration > 0.) {
                if ((workRange.getTime() == ltRange.getTime() + ltRange.getDuration()) && (workRange.getDuration() / workPoints == duration)) {
                    ltRange.setDuration(ltRange.getDuration() + workRange.getDuration());
                    lnPoints += workPoints;
                } else {
                    ldBlock = extractDataBlock(startPoint, lnPoints);
                    startPoint += lnPoints;
                    if (times != null) {
                        pTimes = new double[lnPoints];
                        for (int idx1 = 0, idx2 = 0; idx1 < times.size(); ++idx1) {
                            double[] entry = (double[]) times.elementAt(idx1);
                            for (int idx3 = 0; idx3 < entry.length; ++idx3) {
                                pTimes[idx2++] = entry[idx3];
                            }
                        }
                        ltRange = new TimeRange(pTimes, 0.);
                    }
                    rmapR.addChild(new Rmap(null, ldBlock, ltRange));
                    ltRange = workRange;
                    lnPoints = workPoints;
                    duration = ltRange.getDuration() / lnPoints;
                    if (duration == 0.) {
                        times = new java.util.Vector();
                        times.addElement(workRange.getPtimes());
                    } else {
                        times = null;
                    }
                }
            } else if (ltRange.getDuration() == 0.) {
                if (times != null) {
                    if (workRange.getPtimes().length == workPoints) {
                        times.addElement(workRange.getPtimes());
                    } else {
                        double[] tempTimes = new double[workPoints];
                        for (int idx1 = 0; idx1 < workPoints; ++idx1) {
                            tempTimes[idx1] = workRange.getPointTime(idx1, workPoints);
                        }
                        times.addElement(tempTimes);
                    }
                    lnPoints += workPoints;
                } else {
                    ldBlock = extractDataBlock(startPoint, lnPoints);
                    startPoint += lnPoints;
                    if (times != null) {
                        pTimes = new double[lnPoints];
                        for (int idx1 = 0, idx2 = 0; idx1 < times.size(); ++idx1) {
                            double[] entry = (double[]) times.elementAt(idx1);
                            for (int idx3 = 0; idx3 < entry.length; ++idx3) {
                                pTimes[idx2++] = entry[idx3];
                            }
                        }
                        ltRange = new TimeRange(pTimes, 0.);
                    }
                    rmapR.addChild(new Rmap(null, ldBlock, ltRange));
                    ltRange = workRange;
                    lnPoints = workPoints;
                    duration = ltRange.getDuration() / lnPoints;
                    if (duration == 0.) {
                        times = new java.util.Vector();
                        times.addElement(workRange.getPtimes());
                    } else {
                        times = null;
                    }
                }
            } else {
                if ((duration != 0.) && (workRange.getTime() == (ltRange.getTime() + ltRange.getDuration())) && (workRange.getDuration() / workPoints == duration / lnPoints)) {
                    ltRange.setDuration(ltRange.getDuration() - duration);
                    lnPoints += workPoints;
                } else {
                    ldBlock = extractDataBlock(startPoint, lnPoints);
                    startPoint += lnPoints;
                    if (times != null) {
                        pTimes = new double[lnPoints];
                        for (int idx1 = 0, idx2 = 0; idx1 < times.size(); ++idx1) {
                            double[] entry = (double[]) times.elementAt(idx1);
                            for (int idx3 = 0; idx3 < entry.length; ++idx3) {
                                pTimes[idx2++] = entry[idx3];
                            }
                        }
                        ltRange = new TimeRange(pTimes, 0.);
                    }
                    rmapR.addChild(new Rmap(null, ldBlock, ltRange));
                    ltRange = workRange;
                    lnPoints = workPoints;
                    duration = ltRange.getDuration() / lnPoints;
                    if (duration == 0.) {
                        times = new java.util.Vector();
                        times.addElement(workRange.getPtimes());
                    } else {
                        times = null;
                    }
                }
            }
        }
        ldBlock = extractDataBlock(startPoint, lnPoints);
        if (times != null) {
            pTimes = new double[lnPoints];
            for (int idx1 = 0, idx2 = 0; idx1 < times.size(); ++idx1) {
                double[] entry = (double[]) times.elementAt(idx1);
                for (int idx3 = 0; idx3 < entry.length; ++idx3) {
                    pTimes[idx2++] = entry[idx3];
                }
            }
            ltRange = new TimeRange(pTimes, 0.);
        }
        if (rmapR.getNchildren() == 0) {
            rmapR.setTrange(ltRange);
            rmapR.setDblock(ldBlock);
        } else {
            rmapR.addChild(new Rmap(null, ldBlock, ltRange));
        }
        return (rmapR);
    }

    public final String toString() {
        String stringR = "DataArray: ";
        double[] frames = getFrame(), times = getTime();
        for (int idx = 0; idx < getNumberOfPoints(); ++idx) {
            stringR += (((times != null) ? ("" + times[idx]) : "") + ((frames != null) ? (", F" + frames[idx]) : "") + ((getMIMEType() != null) ? (", " + getMIMEType()) : "") + ((getData() != null) ? (" = (" + com.rbnb.compat.Utilities.arrayGet(getData(), idx) + ")") : "") + "\n");
        }
        return (stringR);
    }
}
