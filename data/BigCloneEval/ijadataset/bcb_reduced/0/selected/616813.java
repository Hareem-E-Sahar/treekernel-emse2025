package net.sourceforge.atides;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

public class Station implements Serializable {

    private static final long serialVersionUID = 6367126318375863701L;

    public float pi;

    private Calendar cal;

    public Constituents cons;

    public int index;

    public float datum;

    public float[] amp;

    public float[] pha;

    public float[] spd;

    public int[] oIndex;

    public int numCons;

    private float maxAmp;

    public float[] maxdt = new float[4];

    public int currentYear, iYear;

    public long epoch;

    public int dstSet;

    protected long maxTimeOffset;

    public Station(Constituents constituents, int ind, boolean useImperial) {
        pi = (float) Math.PI;
        currentYear = 0;
        iYear = 0;
        dstSet = 0;
        maxTimeOffset = 10000;
        cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cons = constituents;
        numCons = 0;
        index = ind;
        datum = cons.stationDatums[index];
        for (int i = 0; i < cons.numConstituents; i++) {
            if (cons.stationAmps[index][i] != 0.0) {
                numCons++;
            }
        }
        amp = new float[numCons];
        pha = new float[numCons];
        spd = new float[numCons];
        oIndex = new int[numCons];
        numCons = 0;
        for (int i = 0; i < cons.numConstituents; i++) {
            if (cons.stationAmps[index][i] != 0.0) {
                amp[numCons] = cons.stationAmps[index][i];
                pha[numCons] = -cons.stationPhases[index][i];
                spd[numCons] = cons.speeds[i];
                pha[numCons] -= cons.stationTimeOffset[index] * spd[numCons];
                oIndex[numCons] = i;
                numCons++;
            }
        }
        float max;
        for (int deriv = 0; deriv < 4; deriv++) {
            maxdt[deriv] = 0;
            for (int tmpYear = 0; tmpYear < cons.numYears; tmpYear++) {
                max = 0;
                for (int a = 0; a < numCons; a++) {
                    float tmpAmp = amp[a] * cons.nods[oIndex[a]][tmpYear];
                    for (int b = deriv; b > 0; b--) tmpAmp *= spd[a];
                    max += tmpAmp;
                }
                if (max > maxdt[deriv]) {
                    maxdt[deriv] = max;
                }
            }
            if (deriv == 0) maxAmp = maxdt[deriv];
            maxdt[deriv] *= 1.1;
        }
    }

    public float minTide() {
        return (datum - maxAmp);
    }

    public float maxTide() {
        return (datum + maxAmp);
    }

    public void setYear(int year) {
        currentYear = year;
        iYear = year - cons.firstYear;
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date dd = cal.getTime();
        epoch = dd.getTime() / 1000;
        for (int a = 0; a < numCons; a++) {
            amp[a] = cons.stationAmps[index][oIndex[a]] * cons.nods[oIndex[a]][iYear];
            pha[a] = -cons.stationPhases[index][oIndex[a]];
            pha[a] -= -cons.stationTimeOffset[index] * spd[a];
            pha[a] += cons.args[oIndex[a]][iYear];
        }
    }

    private float tideDerivative(long sinceEpoch, int deriv) {
        float tide = 0;
        float term;
        float tempd = pi / 2 * deriv;
        for (int a = 0; a < numCons; a++) {
            term = amp[a] * (float) Math.cos(tempd + spd[a] * sinceEpoch + pha[a]);
            for (int b = deriv; b > 0; b--) term *= spd[a];
            tide += term;
        }
        return (tide);
    }

    protected float tideDerivDate(long t, int deriv) {
        long sinceEpoch = t - epoch;
        return (tideDerivative(sinceEpoch, deriv));
    }

    private long findZero(long tl, long tr) {
        float fl = tideDerivDate(tl, 1);
        float fr = tideDerivDate(tr, 1);
        float scale = 1;
        long dt = 0;
        long t = 0;
        float fp = 0, ft = 0, f_thresh = 0;
        if (fl > 0) {
            scale = -1;
            fl = -fl;
            fr = -fr;
        }
        while (tr - tl > 15) {
            if (t == 0) {
                dt = 0;
            } else if (Math.abs(ft) > f_thresh || (ft > 0 ? (fp <= ft / (t - tl)) : (fp <= -ft / (tr - t)))) {
                dt = 0;
            } else {
                dt = Math.round(-ft / fp);
                if (Math.abs(dt) < 15) {
                    dt = (ft < 0 ? 15 : -15);
                }
                t += dt;
                if (t >= tr || t <= tl) {
                    dt = 0;
                }
                f_thresh = Math.abs(ft) / (float) 2.0;
            }
            if (dt == 0) {
                t = tl + (tr - tl) / 2;
                f_thresh = fr > -fl ? fr : -fl;
            }
            if ((ft = scale * tideDerivDate(t, 1)) == 0.0) {
                return t;
            } else if (ft > 0.0) {
                tr = t;
                fr = ft;
            } else {
                tl = t;
                fl = ft;
            }
            fp = scale * tideDerivDate(t, 2);
        }
        return tr;
    }

    protected TideEvent nextMaxMin(long t) {
        TideEvent e = new TideEvent();
        float max_fp = maxdt[2];
        float max_fpp = maxdt[3];
        long t_left, t_right;
        float step, step1, step2;
        float f_left, df_left, f_right;
        float scale = 1;
        t_left = t;
        while ((f_left = tideDerivDate(t_left, 1)) == 0) {
            t_left += 15;
        }
        if (f_left < 0) {
            e.isMax = false;
        } else {
            e.isMax = true;
            scale = -1;
            f_left = -f_left;
        }
        while (true) {
            step1 = Math.abs(f_left) / max_fp;
            df_left = scale * tideDerivDate(t_left, 2);
            step2 = Math.abs(df_left) / max_fpp;
            if (df_left < 0) {
                step = step1 + step2;
            } else {
                step = step1 > step2 ? step1 : step2;
            }
            if (step < 15) {
                step = 15;
            }
            t_right = t_left + (long) step;
            while ((f_right = scale * tideDerivDate(t_right, 1)) == 0) {
                t_right += 15;
            }
            if (f_right > 0) {
                e.time = findZero(t_left, t_right);
                return (e);
            }
            t_left = t_right;
            f_left = f_right;
        }
    }

    public float predictTideLevel(long time) {
        return (tideDerivDate(time, 0) + datum);
    }

    private void finishTideEvent(TideEvent te) {
        te.tide = predictTideLevel(te.time);
        te.localTime = te.time + cons.stationTimeZone[index] + dstSet;
    }

    public Vector<TideEvent> simpleTideEvents(long startTime, long endTime) {
        Vector<TideEvent> v = new Vector<TideEvent>();
        TideEvent e;
        long loopStart = startTime - maxTimeOffset;
        long loopTime = loopStart;
        long loopEndTime = endTime + maxTimeOffset;
        while (loopTime <= loopEndTime) {
            e = nextMaxMin(loopTime);
            loopTime = e.time;
            finishTideEvent(e);
            if (e.time >= startTime && e.time < endTime) {
                v.addElement(e);
            }
        }
        return (v);
    }
}
