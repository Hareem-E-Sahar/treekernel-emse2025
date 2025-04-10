public class Test {    private Plane2d buildSideWall(ValuePlane plane1, ValuePlane plane2) {
        float value1Temp = plane1.getModifiedYValue();
        float value2Temp = plane2.getModifiedYValue();
        Point3f A = plane1.getD();
        Point3f B = plane1.getA();
        Point3f C = plane2.getB();
        Point3f D = plane2.getC();
        float[] tempA = new float[3];
        A.get(tempA);
        tempA[1] = value1Temp;
        A.set(tempA);
        float[] tempB = new float[3];
        B.get(tempB);
        tempB[1] = value1Temp;
        B.set(tempB);
        float[] tempC = new float[3];
        C.get(tempC);
        tempC[1] = value2Temp;
        C.set(tempC);
        float[] tempD = new float[3];
        D.get(tempD);
        tempD[1] = value2Temp;
        D.set(tempD);
        float value1 = plane1.getTheValue();
        float value2 = plane2.getTheValue();
        float aveValue = (value1 + value2) / 2;
        Color3f theColor = ColorTable.getColor(aveValue);
        Plane2d plane = new Plane2d(A, B, C, D, theColor, false, 0, 0, 0.0f, true);
        return plane;
    }
}