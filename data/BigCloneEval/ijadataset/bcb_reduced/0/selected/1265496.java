package edu.ucsd.ncmir.jinx.core;

import edu.ucsd.ncmir.jinx.objects.JxPlaneTraceList;
import edu.ucsd.ncmir.jinx.objects.trace.JxTrace;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

public class JxTraceFinder {

    private Double[] _zvals;

    private Hashtable<Double, ArrayList<JxTrace>> _trace_table = new Hashtable<Double, ArrayList<JxTrace>>();

    private double _dzmin;

    public JxTraceFinder(JxPlaneTraceList[] trace_list) {
        for (JxPlaneTraceList traces : trace_list) for (JxTrace trace : traces) {
            Double z = new Double(trace.get(0).getW());
            ArrayList<JxTrace> list = this._trace_table.get(z);
            if (list == null) this._trace_table.put(z, list = new ArrayList<JxTrace>());
            list.add(trace);
        }
        this._zvals = this._trace_table.keySet().toArray(new Double[this._trace_table.size()]);
        Arrays.sort(this._zvals);
        this._dzmin = Double.MAX_VALUE;
        for (int i = 1; i < this._zvals.length; i++) {
            double dz = this._zvals[i].doubleValue() - this._zvals[i - 1].doubleValue();
            if (dz < this._dzmin) this._dzmin = dz;
        }
    }

    public double getDzMin() {
        return this._dzmin;
    }

    public int getIntervals() {
        double z0 = this.getZ0();
        double z1 = this._zvals[this._zvals.length - 1].doubleValue() + (this._dzmin / 2);
        return (int) ((z1 - z0) / this._dzmin);
    }

    public double getZ0() {
        return this._zvals[0].doubleValue();
    }

    public JxTrace[] findTracesByZLevel(double z) {
        int zp0 = 0;
        int zp;
        int zp1 = this._zvals.length;
        double zv;
        int nz = this._zvals.length;
        do {
            zp = (zp1 + zp0) / 2;
            zv = this._zvals[zp].doubleValue();
            if (zv > z) zp1 = zp; else if (zv < z) zp0 = zp; else break;
        } while ((nz >>= 1) > 0);
        JxTrace[] traces;
        if (Math.abs(zv - z) < this._dzmin) {
            ArrayList<JxTrace> tl = this._trace_table.get(this._zvals[zp]);
            traces = tl.toArray(new JxTrace[tl.size()]);
        } else traces = new JxTrace[0];
        return traces;
    }
}
