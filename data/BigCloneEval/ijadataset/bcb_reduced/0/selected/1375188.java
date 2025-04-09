package mipt.crec.lab.compmath.numdiff;

import static mipt.crec.lab.compmath.numdiff.ParamsModule.AUTOCALC;
import static mipt.crec.lab.compmath.numdiff.ParamsModule.BEGIN;
import static mipt.crec.lab.compmath.numdiff.ParamsModule.END;
import static mipt.crec.lab.compmath.numdiff.ParamsModule.FUNCTION;
import static mipt.crec.lab.compmath.numdiff.ParamsModule.MANTISSA;
import static mipt.crec.lab.compmath.numdiff.ParamsModule.ORDER;
import static mipt.crec.lab.compmath.numdiff.ParamsModule.PRECISION;
import static mipt.crec.lab.compmath.numdiff.ParamsModule.STEP;
import mipt.common.DoubleFormat;
import mipt.crec.lab.common.compute.AjustableComputingModule;
import mipt.crec.lab.common.dataview.FullNumberOptionsFormCreator;
import mipt.crec.lab.common.modules.ComputingDataModule;
import mipt.crec.lab.compmath.numdiff.gui.GraphsModuleView;
import mipt.crec.lab.data.AbstractDataModule;
import mipt.data.Data;
import mipt.data.DataWrapper;
import mipt.data.MutableComparableData;
import mipt.data.event.DataModelEvent;
import mipt.data.event.DataModelListener;
import mipt.math.BigNumber;
import mipt.math.FloatNumber;
import mipt.math.Number;
import mipt.math.ScalarNumber;
import mipt.math.arg.func.DefaultDependence;
import mipt.math.arg.func.Dependence;
import mipt.math.arg.func.diff.DependenceDifferentiator;
import mipt.math.arg.func.formula.FormulaDependenceAnalyzer;
import mipt.math.arg.func.formula.simplification.FormulaSimplifier;
import mipt.math.function.Function;
import mipt.math.function.arg.DependenceFunction;
import mipt.math.function.diff.DefaultDifferentiator;
import mipt.math.function.impl.AbsFunction;
import mipt.math.function.set.BigDependenceElements;
import mipt.math.function.set.CountDependenceElements;
import mipt.math.function.set.DefaultDependenceElements;
import mipt.math.function.set.DependenceElements;
import mipt.math.function.set.FloatDependenceElements;
import mipt.math.function.set.MathFactory;

public class GraphsModule extends ComputingDataModule implements AjustableComputingModule, DataModelListener {

    public static final String RESULT = "result", ERROR_NORM = "errorNorm", MIN_ERROR = "minError", MIN_ERROR_STEP = "minErrorStep", CALCULATION_TIME = "calculationTime";

    /** ������ �� ������ Data ����� ������. */
    private Data d;

    /**������� �������� ����������(������) �����������*/
    private Number symbolic[][];

    /**������� �������� ��������� �����������*/
    private Number numerical[][];

    /**�������� ����� ���������� ����. � ������ �����������*/
    private Number difference[][];

    /**�������� ����� ���������� ����. � ������ ����������� � ����������� �� ����*/
    private Number errorOnStep[][];

    /** ���-�� ����� �� �������.
     * ����� ������ ������ ��������� ����� ���� 1, ����� ���� �������� ����� ����������*/
    public static int DEFAULT_POINT_COUNT = 1000;

    private int pointCount = DEFAULT_POINT_COUNT + 1;

    private Number x0, step, delta;

    /**
     * ������������ ��� ��� ���������, ��� � ��� ���������� �����������
     */
    private FormulaDependenceAnalyzer fda;

    private DependenceDifferentiator diff;

    private FormulaSimplifier simp;

    public String getID() {
        return "NumDiff_" + super.getID();
    }

    protected final GraphsModuleView getGraphsView() {
        return (GraphsModuleView) getView();
    }

    /**
	 * @return difference
	 */
    public final Number[][] getDifference() {
        return difference;
    }

    /**
	 * @return numerical
	 */
    public final Number[][] getNumerical() {
        return numerical;
    }

    /**
	 * @return symbolic
	 */
    public final Number[][] getSymbolic() {
        return symbolic;
    }

    /**
	 * @return errorOnStep
	 */
    public final Number[][] getErrorOnStep() {
        return errorOnStep;
    }

    /**
	 * @see mipt.crec.lab.common.compute.ComputingModule#start()
	 */
    public Thread start() {
        dataChanged(null);
        return null;
    }

    /**
	 * �������������, ��� ��� ��������� ���������� ��������������
	 * @see mipt.crec.lab.common.modules.ComputingDataModule#shouldStartOnChange()
	 */
    public boolean shouldStartOnChange() {
        return false;
    }

    /**
	 * @see mipt.crec.lab.common.compute.AjustableComputingModule#getNumberOptions()
	 */
    public Object getNumberOptions() {
        return getData();
    }

    /**
	 * @see mipt.crec.lab.common.compute.AjustableComputingModule#setMathFactory(mipt.math.function.set.MathFactory, boolean)
	 */
    public void setMathFactory(MathFactory factory, boolean start) {
        if (start) fireModelChanged(getOtherModule("NumDiff_ParamsModule"), getDataModel());
    }

    /**
	 * @see mipt.data.event.DataModelListener#dataAdded(mipt.data.event.DataModelEvent)
	 */
    public void dataAdded(DataModelEvent e) {
    }

    /**
	 * @see mipt.data.event.DataModelListener#dataRemoved(mipt.data.event.DataModelEvent)
	 */
    public void dataRemoved(DataModelEvent e) {
    }

    /**
	 * @see mipt.data.event.DataModelListener#dataChanged(mipt.data.event.DataModelEvent)
	 */
    public void dataChanged(DataModelEvent e) {
        d = e == null ? getDataModel().getData() : e.getData();
        if (e == null || d.getBoolean(AUTOCALC)) {
            buildTables();
        } else {
            Data results = d.getData(RESULT);
            getGraphsView().updateResults(string2number(results.getString(ERROR_NORM)), string2number(results.getString(MIN_ERROR)), string2number(results.getString(MIN_ERROR_STEP)), results.getDouble(CALCULATION_TIME));
        }
        getGraphsView().dataChanged(e);
    }

    private Number string2number(String s) {
        switch(d.getInt(PRECISION)) {
            case 0:
                return new FloatNumber(Float.parseFloat(s));
            case 1:
            default:
                return new ScalarNumber(Double.parseDouble(s));
            case 2:
                return new BigNumber(s, d.getInt(MANTISSA));
        }
    }

    /**
	 * @see mipt.crec.lab.data.AbstractDataModule#setData(mipt.data.DataWrapper)
	 */
    public void setData(DataWrapper data) {
        getDataModel().setData(data);
        AbstractDataModule.fireModelChanged(getOtherModule("NumDiff_ParamsModule"), null);
    }

    /** ������� ��� ������������� � ���������� ������ �������� �������
    *  //���������� false, ���� ��������� ������ 
    */
    public void buildTables() {
        double time = 0;
        Number norm = null, minOnStep[] = null;
        try {
            fda = initAnalyzer(d);
            initTables();
            buildSymbolicTable();
            time = buildNumericalTable();
            norm = buildDifferenceTable();
            minOnStep = buildErrorOnStep();
        } catch (RuntimeException e) {
            getGraphsView().showError("DerivativeError", e);
            minOnStep = new Number[2];
        }
        DoubleFormat format = getGraphsView().updateResults(norm, minOnStep[1], minOnStep[0], time);
        updateResults(norm, minOnStep, time, format);
    }

    /**
    * Does not notify listeners not to recalculate
    */
    public void updateResults(Number errorNorm, Number[] minOnStep, double time, DoubleFormat format) {
        MutableComparableData d = (MutableComparableData) this.d.getData(RESULT);
        if (d == null) throw new IllegalStateException("Your variant file should contain 'result' element");
        d.set(number2string(errorNorm, format), ERROR_NORM);
        d.set(number2string(minOnStep[1], format), MIN_ERROR);
        d.set(number2string(minOnStep[0], format), MIN_ERROR_STEP);
        d.setDouble(time, CALCULATION_TIME);
    }

    protected String number2string(Number v, DoubleFormat format) {
        return numberToString(v, format);
    }

    /**
    * used by view
    */
    public String time2string(double time) {
        return timeToString(time);
    }

    public final FormulaDependenceAnalyzer initAnalyzer(Data data) {
        fda = new FormulaDependenceAnalyzer();
        fda.setCheckVariables(true);
        switch(data.getInt(PRECISION)) {
            case 0:
                fda.setElements(new FloatDependenceElements());
                break;
            case 1:
                int count = data.getInt(FullNumberOptionsFormCreator.COUNT);
                fda.setElements(count > 0 ? new CountDependenceElements(count) : new DefaultDependenceElements());
                break;
            case 2:
                fda.setElements(new BigDependenceElements(d.getInt(MANTISSA)));
                break;
        }
        return fda;
    }

    /**
	 * 
	 */
    protected FormulaDependenceAnalyzer initAnalyzer(int precision) {
        return fda;
    }

    /**
    * ������������� ������, � ����� x0, delta � step (� ����������� �� �������� ����������).
    */
    private void initTables() {
        double begin = d.getDouble(BEGIN), end = d.getDouble(END), h = d.getDouble(STEP);
        pointCount = 1 + d.getInt("plotPoints");
        if (pointCount <= 0) pointCount = 1 + (int) Math.round((end - begin) / h);
        symbolic = new Number[pointCount][2];
        numerical = new Number[pointCount][2];
        difference = new Number[pointCount][2];
        x0 = fda.getElements().createNumber(begin);
        step = fda.getElements().createNumber(h);
        delta = fda.getElements().createNumber((end - begin) / (pointCount - 1));
    }

    private DefaultDifferentiator createDifferentiator(boolean optimize) {
        DefaultDifferentiator diff = new DefaultDifferentiator();
        diff.setStoreZeroFactorValues(optimize);
        diff.setOrder(d.getInt(ORDER), d.getBoolean("leftDifference"));
        return diff;
    }

    /**
    * ������ ������� �������� ��������� ����������� �������
    * ���������� ����������� ����� � ������������� 
    */
    private double buildNumericalTable() {
        setFunction(d.getString(FUNCTION));
        Function function = new DependenceFunction(fda.getDependence(), "x");
        boolean optimize = pointCount == 1 + (int) Math.round((d.getDouble(END) - d.getDouble(BEGIN)) / d.getDouble(STEP));
        DefaultDifferentiator diff = createDifferentiator(optimize);
        Number values[] = null;
        double time = System.nanoTime();
        for (int i = 0; i < pointCount; i++) {
            Number x = delta.copy().mult(i).add(x0);
            numerical[i][0] = x;
            if (optimize) {
                values = diff.fillValues(function, x, step, values);
                numerical[i][1] = diff.calcDerivative(values, step);
                values = diff.shiftValues(values, false);
            } else {
                numerical[i][1] = diff.calcDerivative(function, x, step, null);
            }
        }
        return (System.nanoTime() - time) / 1000000;
    }

    /**
    * ���������� ������� �������� ������ �����������
    *
    */
    private void buildSymbolicTable() {
        Dependence dfdx = symbolicDerivative(d.getString(FUNCTION));
        for (int i = 0; i < pointCount; i++) {
            Number x = delta.copy().mult(i).add(x0);
            setArgumentValue(dfdx, x);
            symbolic[i][0] = x.copy();
            symbolic[i][1] = dfdx.getValue();
        }
    }

    private void setArgumentValue(Dependence dfdx, Number x) {
        if (dfdx.getArgumentCount() > 1) throw new RuntimeException("Formula must have the only argument - x");
        try {
            dfdx.setArgumentValue("x", x);
        } catch (IllegalStateException e) {
        }
    }

    /** ���������� ������ � ������(����������) �����������
    * @param function - ������ � ��������������� ��������
    * @return 
    */
    private Dependence symbolicDerivative(String function) {
        setFunction(function);
        fda.setFormula(getDerivative(d).toString());
        return fda.getDependence();
    }

    /**
    * Must be called after initAnalyzer
    * Can be used externally to check function 
    */
    public void setFunction(String function) {
        fda.setFormula(function);
    }

    /**
    * Must be called after setFunction.
    */
    public Dependence getDerivative(Data data) {
        return getDerivative((DefaultDependence) fda.getDependence(), data);
    }

    public double getDerivativeValue(int order, double x, Data data) {
        DefaultDependence dependence = (DefaultDependence) fda.getDependence();
        String temp = dependence.toString();
        for (int i = 1; i < order; i++) {
            dependence = getDerivative(dependence, data);
            dependence.setAllowSetAbsentArgument(true);
        }
        setArgumentValue(dependence, fda.getElements().createNumber(x));
        setFunction(temp);
        return dependence.getValue().doubleValue();
    }

    public double getFunctionValue(double x) {
        setArgumentValue(fda.getDependence(), fda.getElements().createNumber(x));
        return fda.getDependence().getValue().doubleValue();
    }

    /**
    * Data is sent as argument because can be called from ParamsModule
    *  before this module receive own reference to data.
    */
    protected DefaultDependence getDerivative(DefaultDependence dependence, Data data) {
        if (diff == null) diff = new DependenceDifferentiator();
        diff.setDependence(dependence);
        diff.setVarName("x");
        DefaultDependence derivative = diff.getDerivativeDependence();
        if (data.getBoolean("simplifyDerivative")) try {
            if (simp == null) simp = new FormulaSimplifier();
            derivative = simp.simplify(derivative);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        fda.setFormula(derivative.toString());
        derivative = (DefaultDependence) fda.getDependence();
        return derivative;
    }

    /**
    * Change argument to be the power of 10
    * @param value[0] - can be diminished only (i.e. is maximum value of the result)
    * @return power (exponent)
    * @see DoubleFormatter.round()
    */
    private int roundValue(double[] value) {
        double v = 1.0, max = value[0];
        int power = 0;
        if (max > v) {
            double oldV = v;
            while (max > v) {
                oldV = v;
                v *= 10.0;
                power++;
            }
            value[0] = oldV;
        } else {
            while (max < v) {
                v *= 0.1;
                power--;
            }
            value[0] = v;
        }
        return power;
    }

    /**
    * ���������� ������� � �������� �������� ����������� � ����������� �� ����
    *  ���������� ������� �������� [1] � ���, ��� ������� �� ���������� [0] 
    */
    private Number[] buildErrorOnStep() {
        DependenceElements numCreator = fda.getElements();
        Function abs = numCreator.getFunction("abs");
        Function log = numCreator.getFunction("log");
        double x1 = d.getDouble(END);
        int digits = d.getInt(PRECISION);
        digits = ((ParamsModule) getOtherModule("NumDiff_ParamsModule")).getMantissa(digits, d);
        Number X = numCreator.createNumber((x1 + x0.doubleValue()) * 0.5);
        setFunction(d.getString(FUNCTION));
        Function function = new DependenceFunction(fda.getDependence(), "x");
        DefaultDifferentiator diff = createDifferentiator(false);
        Dependence dfdx = symbolicDerivative(d.getString(FUNCTION));
        setArgumentValue(dfdx, X);
        Number symbolic = dfdx.getValue();
        double maxH = (x1 - x0.doubleValue()) / 2;
        if (d.getInt(ORDER) > 2) {
            maxH = maxH * 0.5;
            if (d.getInt(ORDER) > 4) maxH = maxH * 0.5;
        }
        double max[] = new double[] { maxH };
        int stepCount = roundValue(max);
        double logH = stepCount;
        stepCount = stepCount + digits;
        Number h = numCreator.createNumber(max[0]);
        int iStep = 0;
        stepCount *= 6;
        double log7_10 = Math.log10(0.7), log5_7 = -Math.log10(1.4), log21_35 = Math.log10(0.6), log10_15 = -Math.log10(1.5);
        if (maxH >= 7 * h.doubleValue()) {
            iStep = 5;
            h = h.mult(7);
            logH += Math.log10(7);
        } else if (maxH >= 5 * h.doubleValue()) {
            iStep = 4;
            h = h.mult(5);
            logH += Math.log10(5);
        } else if (maxH >= 3.5 * h.doubleValue()) {
            iStep = 3;
            h = h.mult(3.5);
            logH += Math.log10(3.5);
        } else if (maxH >= 2.1 * h.doubleValue()) {
            iStep = 2;
            h = h.mult(2.1);
            logH += Math.log10(2.1);
        } else if (maxH >= 1.5 * h.doubleValue()) {
            iStep = 1;
            h = h.mult(1.5);
            logH += Math.log10(1.5);
        }
        stepCount += iStep;
        stepCount++;
        errorOnStep = new Number[stepCount][2];
        Number min = abs.calc(diff.calcDerivative(function, X, h, null).minus(symbolic));
        if (min.compareTo(0.) == 0) min = numCreator.createNumber(-digits); else min = log.calc(min);
        Number hMin = h.copy();
        int i = errorOnStep.length - 1;
        errorOnStep[i][0] = numCreator.createNumber(logH);
        errorOnStep[i][1] = min;
        for (i = i - 1; i >= 0; i--) {
            switch(iStep) {
                case 0:
                default:
                    h = h.mult(0.7);
                    logH += log7_10;
                    iStep = 5;
                    break;
                case 5:
                    h = h.mult(5. / 7.);
                    logH += log5_7;
                    break;
                case 4:
                    h = h.mult(0.7);
                    logH += log7_10;
                    break;
                case 3:
                    h = h.mult(0.6);
                    logH += log21_35;
                    break;
                case 2:
                    h = h.mult(1.5 / 2.1);
                    logH += log5_7;
                    break;
                case 1:
                    h = h.mult(1. / 1.5);
                    logH += log10_15;
                    break;
            }
            iStep--;
            Number mod = abs.calc(diff.calcDerivative(function, X, h, null).minus(symbolic));
            if (mod.compareTo(0.) == 0) mod = numCreator.createNumber(-digits); else mod = log.calc(mod);
            errorOnStep[i][0] = numCreator.createNumber(logH);
            errorOnStep[i][1] = mod;
            if (min.compareTo(mod) > 0) {
                min = mod;
                hMin = h.copy();
            }
        }
        min = numCreator.createNumber(Math.pow(10, min.doubleValue()));
        return new Number[] { hMin, min };
    }

    /** ���������� ������� � �������� �������� �����������
    *  ���������� ����� �������� (������, �� ���� ��������) 
    */
    private Number buildDifferenceTable() {
        difference[0][1] = numerical[0][1].copy().minus(symbolic[0][1]);
        difference[0][0] = numerical[0][0];
        Function abs = new AbsFunction();
        Number norm = abs.calc(difference[0][1]);
        for (int i = 1; i < pointCount; i++) {
            difference[i][1] = numerical[i][1].copy().minus(symbolic[i][1]);
            difference[i][0] = numerical[i][0];
            Number mod = abs.calc(difference[i][1]);
            if (norm.compareTo(mod) < 0) norm = mod;
        }
        return norm;
    }
}
