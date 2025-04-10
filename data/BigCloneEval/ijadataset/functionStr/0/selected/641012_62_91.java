public class Test {    public CField(JComponent theApp, int theXoff, int theYoff, int theWidth, int theHeight) {
        super();
        myLength = CC.FF_Length;
        myWidth = CC.FF_Width;
        myCenterRadius = CC.FF_CenterRadius;
        myGateLength = CC.FF_GateLength;
        myGateZHeight = CC.FF_GateZHeight;
        myGateZoneLenght = CC.FF_GateZoneLenght;
        myGateZoneWidth = CC.FF_GateZoneWidth;
        myPenZoneLenght = CC.FF_PenZoneLenght;
        myPenZoneWidth = CC.FF_PenZoneWidth;
        myPenLenght = CC.FF_PenLenght;
        myPenRadius = CC.FF_PenRadius;
        myCornerRadius = CC.FF_CornerRadius;
        myPointRadius = CC.FF_PointRadius;
        myApp = theApp;
        myXoff = theXoff;
        myYoff = theYoff;
        width = theWidth;
        height = theHeight;
        kx = theWidth / myLength;
        ky = theHeight / myWidth;
        kz = (kx + ky) / 2;
        heightZ = (int) (kz * (theHeight + theWidth) / 2);
        myG = (Graphics2D) this.getGraphics();
        this.setPreferredSize(new Dimension(width, height));
        this.setSize(width, height);
        this.setFocusable(true);
        this.setBackground(Color.GREEN);
    }
}