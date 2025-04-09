package fgen;

import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JComponent;

/**
 *
 * @author Oleg Rachaev
 */
public class CField extends JPanel {

    public static double myLength = 100;

    public static double myWidth = 65;

    public static double myCenterRadius = 9.15;

    public static double myGateLength = 7.32;

    public static double myGateZHeight = 2.44;

    public static double myGateZoneLenght = 5.5;

    public static double myGateZoneWidth = 5.5;

    public static double myPenZoneLenght = 16.5;

    public static double myPenZoneWidth = 16.5;

    public static double myPenLenght = 11;

    public static double myPenRadius = 9.15;

    public static double myCornerRadius = 1;

    public static double myPointRadius = 0.5;

    private Graphics2D myG = null;

    public Object myApp = null;

    public int myXoff = 0;

    public int myYoff = 0;

    public int width = 640;

    public int height = 480;

    public int heightZ = 20;

    private double kx;

    private double ky;

    private double kz;

    public CField(JComponent theApp, int theXoff, int theYoff, int theWidth, int theHeight) {
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

    public CField(JComponent theApp, int theXoff, int theYoff, int _kx, int _ky, int _kz) {
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
        kx = _kx;
        ky = _ky;
        kz = _kz;
        width = (int) (kx * myLength);
        height = (int) (ky * myWidth);
        heightZ = (int) (kz * (height + width) / 2);
        myG = (Graphics2D) this.getGraphics();
        this.setPreferredSize(new Dimension(width, height));
        this.setSize(width, height);
        this.setFocusable(true);
        this.setBackground(Color.GREEN);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        myG = (Graphics2D) g;
        if (myG != null) {
            myG.setColor(new Color(0, 150, 0));
            myG.fillRect(0, 0, width, height);
            myG.setColor(Color.WHITE);
            myG.drawRect(myXoff, myYoff, width, height);
            myG.drawLine(myXoff + width / 2, myYoff, myXoff + width / 2, myYoff + height);
            myG.drawOval((int) (myXoff + width / 2 - myCenterRadius * kx), (int) (myYoff + height / 2 - myCenterRadius * ky), (int) (2 * myCenterRadius * kx), (int) (2 * myCenterRadius * ky));
            myG.fillOval((int) (myXoff + width / 2 - myPointRadius * kx), (int) (myYoff + height / 2 - myPointRadius * ky), (int) (2 * myPointRadius * kx), (int) (2 * myPointRadius * ky));
            myG.drawArc(myXoff - (int) (myCornerRadius * kx), myYoff - (int) (myCornerRadius * ky), (int) (2 * myCornerRadius * kx), (int) (2 * myCornerRadius * ky), 270, 90);
            myG.drawArc(myXoff + width - (int) (myCornerRadius * kx), myYoff - (int) (myCornerRadius * ky), (int) (2 * myCornerRadius * kx), (int) (2 * myCornerRadius * ky), 180, 90);
            myG.drawArc(myXoff + width - (int) (myCornerRadius * kx), myYoff + height - (int) (myCornerRadius * ky), (int) (2 * myCornerRadius * kx), (int) (2 * myCornerRadius * ky), 90, 90);
            myG.drawArc(myXoff - (int) (myCornerRadius * kx), myYoff + height - (int) (myCornerRadius * ky), (int) (2 * myCornerRadius * kx), (int) (2 * myCornerRadius * ky), 0, 90);
            myG.fillRect(myXoff / 2, (int) (myYoff + (height - ky * myGateLength) / 2), myXoff, (int) (ky * myGateLength));
            myG.fillRect(myXoff + width, (int) (myYoff + (height - ky * myGateLength) / 2), myXoff, (int) (ky * myGateLength));
            myG.drawRect(myXoff, (int) (myYoff + (height - ky * (myGateLength + myGateZoneWidth)) / 2), (int) (kx * myGateZoneLenght), (int) (ky * (myGateLength + myGateZoneWidth)));
            myG.drawRect((int) (myXoff + width - (kx * myGateZoneLenght)), (int) (myYoff + (height - ky * (myGateLength + myGateZoneWidth)) / 2), (int) (kx * myGateZoneLenght), (int) (ky * (myGateLength + myGateZoneWidth)));
            myG.drawRect(myXoff, (int) (myYoff + (height - ky * (myGateLength + 2 * myPenZoneWidth)) / 2), (int) (kx * myPenZoneLenght), (int) (ky * (myGateLength + 2 * myPenZoneWidth)));
            myG.drawRect((int) (myXoff + width - (kx * myPenZoneLenght)), (int) (myYoff + (height - ky * (myGateLength + 2 * myPenZoneWidth)) / 2), (int) (kx * myPenZoneLenght), (int) (ky * (myGateLength + 2 * myPenZoneWidth)));
            myG.fillOval((int) (myXoff + myPenLenght * kx), (int) (myYoff + height / 2), (int) (2 * myPointRadius * kx), (int) (2 * myPointRadius * ky));
            myG.fillOval((int) (myXoff + width - myPenLenght * kx), (int) (myYoff + height / 2), (int) (2 * myPointRadius * kx), (int) (2 * myPointRadius * ky));
            myG.drawArc(myXoff + (int) (kx * (myPenLenght - myPenRadius)), myYoff + (int) (height / 2 - ky * myPenRadius), (int) (2 * myPenRadius * kx), (int) (2 * myPenRadius * ky), 308, 104);
            myG.drawArc(myXoff + width - (int) (kx * (myPenLenght + myPenRadius)), myYoff + (int) (height / 2 - ky * myPenRadius), (int) (2 * myPenRadius * kx), (int) (2 * myPenRadius * ky), 128, 104);
        }
    }
}
