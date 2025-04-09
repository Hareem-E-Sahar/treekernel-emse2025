package tafat.engine.tools;

public class Spline {

    private int mBeginIndex = 0;

    private int mNumPoints = 0;

    private double[] mX;

    private double[] mY;

    private double[] mA;

    private double[] mB;

    private double[] mC;

    private double[] mBoundCond1 = new double[2];

    private double[] mBoundCondN = new double[2];

    public Spline(double[] X, double[] Y) {
        int NumPoints = X.length;
        mBeginIndex = 0;
        SetupConstants(NumPoints);
        for (int i = 0; i < NumPoints; i++) mX[i] = X[i + mBeginIndex];
        for (int i = 0; i < NumPoints; i++) mY[i] = Y[i + mBeginIndex];
        SetupBoundaryConditions();
        CalcCoefficients();
    }

    public Spline(double[] X, double[] Y, int NumPoints, int StartPoint) {
        mBeginIndex = StartPoint;
        SetupConstants(NumPoints);
        for (int i = 0; i < NumPoints; i++) mX[i] = X[i + mBeginIndex];
        for (int i = 0; i < NumPoints; i++) mY[i] = Y[i + mBeginIndex];
        SetupBoundaryConditions();
        CalcCoefficients();
    }

    private void SetupConstants(int NumPoints) {
        mNumPoints = NumPoints;
        mX = new double[NumPoints];
        mY = new double[NumPoints];
        mA = new double[NumPoints - 1];
        mB = new double[NumPoints - 1];
        mC = new double[NumPoints - 1];
    }

    private void SetupBoundaryConditions() {
        mBoundCond1[0] = 0.0f;
        mBoundCond1[1] = 0.0f;
        mBoundCondN[0] = 0.0f;
        mBoundCondN[1] = 0.0f;
    }

    private void CalcCoefficients() {
        double dx1, dx2;
        double dy1, dy2;
        dx1 = mX[1] - mX[0];
        dy1 = mY[1] - mY[0];
        for (int i = 1; i < mNumPoints - 1; i++) {
            dx2 = mX[i + 1] - mX[i];
            dy2 = mY[i + 1] - mY[i];
            mC[i] = dx2 / (dx1 + dx2);
            mB[i] = 1.0f - mC[i];
            mA[i] = 6.0f * (dy2 / dx2 - dy1 / dx1) / (dx1 + dx2);
            dx1 = dx2;
            dy1 = dy2;
        }
        mC[0] = -mBoundCond1[0] / 2.0f;
        mB[0] = mBoundCond1[1] / 2.0f;
        mA[0] = 0.0f;
        for (int i = 1; i < mNumPoints - 1; i++) {
            double p = mB[i] * mC[i - 1] + 2.0f;
            mC[i] = -mC[i] / p;
            mB[i] = (mA[i] - mB[i] * mB[i - 1]) / p;
        }
        dy1 = (mBoundCondN[1] - mBoundCondN[0] * mB[mNumPoints - 2]) / (mBoundCondN[0] * mC[mNumPoints - 2] + 2.0f);
        for (int i = mNumPoints - 2; i >= 0; i--) {
            dx1 = mX[i + 1] - mX[i];
            dy2 = mC[i] * dy1 + mB[i];
            mA[i] = (dy1 - dy2) / (6.0f * dx1);
            mB[i] = dy2 / 2.0f;
            mC[i] = (mY[i + 1] - mY[i]) / dx1 - dx1 * (mB[i] + dx1 * mA[i]);
            dy1 = dy2;
        }
    }

    public int GetNumPoints() {
        return mNumPoints;
    }

    public double evaluate(double x) {
        if (mNumPoints < 2) return 0.0f;
        int i = GetSegmentNumb(x);
        double t = (x - mX[i]);
        return mA[i] * t * t * t + mB[i] * t * t + mC[i] * t + mY[i];
    }

    public double CalcValue(int i, double p) {
        if (mNumPoints < 2) return 0.0f;
        i -= mBeginIndex;
        if (i < 0) i = 0; else if (i >= mNumPoints - 2) i = mNumPoints - 2;
        double t = p * (double) (mX[i + 1] - mX[i]);
        return mA[i] * t * t * t + mB[i] * t * t + mC[i] * t + mY[i];
    }

    public int GetSegmentNumb(double x) {
        int left = 0;
        int right = mNumPoints - 1;
        while (left + 1 < right) {
            int middle = (left + right) / 2;
            if (mX[middle] <= x) left = middle; else right = middle;
        }
        return left;
    }

    public void show() {
        for (int i = 0; i < mNumPoints - 1; i++) {
            System.out.println("(x,y)[" + i + "] = (" + mX[i] + "," + mY[i] + ")");
            System.out.println("(a,b,c) = (" + mA[i] + "," + mB[i] + "," + mC[i] + ")");
        }
    }
}
