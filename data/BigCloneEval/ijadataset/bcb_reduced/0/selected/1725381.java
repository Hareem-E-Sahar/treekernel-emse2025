package aidc.aigui.plot;

import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import com.wolfram.jlink.MathLinkException;
import aidc.aigui.box.abstr.FrequencySelection;
import aidc.aigui.box.abstr.PointSelection;
import aidc.aigui.mathlink.*;
import aidc.aigui.resources.Complex;
import aidc.aigui.resources.MathematicaFormat;

public class JBodePlotPanel extends SchematicCanvas implements Printable {

    MathematicaFormat mf;

    JBodePlotFrame jBPF;

    int pointCounter = 0;

    private FrequencySelection fs;

    private String acSweep, aiSignal;

    private PointSelection pts;

    private static final long serialVersionUID = 12753092850440760L;

    public AffineTransform aTphase = new AffineTransform();

    public AffineTransform aTfrqG = new AffineTransform();

    private AffineTransform izoom;

    private CdgInstance ginstF;

    private CdgInstance ginstPh;

    int highlightedPoint = 0;

    int draggedPoint = -1;

    private int pointspx[];

    private String sError[];

    private Complex cPoint[];

    private boolean isPointAlreadyAdded;

    private double x1;

    private double x2;

    String defaultError = "0.1";

    String minValue;

    String maxValue;

    String function;

    String version;

    double factor = 1D;

    public boolean logarithmic = true, linearY = false;

    private GraphicsContainer txtPhaseY;

    private GraphicsContainer txtFrqGY;

    private GraphicsContainer txtFrqGX;

    private GraphicsContainer txtPhaseX;

    private Color simPlot, aiPlot, axis, fontColor;

    public JBodePlotPanel(JBodePlotFrame jF, FrequencySelection f, PointSelection ps, String acSw, String aiSign, String func, String minVal, String maxVal, Complex[] point, String[] error) {
        super();
        jBPF = jF;
        mf = new MathematicaFormat();
        aiSignal = aiSign;
        acSweep = acSw;
        pointspx = new int[100];
        cPoint = new Complex[100];
        sError = new String[100];
        pts = ps;
        fs = f;
        function = func;
        minValue = minVal;
        maxValue = maxVal;
        x1 = Double.valueOf(minValue.replaceAll("[*][\\Q^\\E]", "E"));
        x2 = Double.valueOf(maxValue.replaceAll("[*][\\Q^\\E]", "E"));
        try {
            version = MathAnalog.evaluateToOutputForm("$Version", 0, false);
        } catch (MathLinkException e) {
            e.printStackTrace();
        }
        if (!version.startsWith("6")) factor = 2.3;
        if (point != null && point.length != 0) {
            pointCounter = 0;
            for (int i = 0; i < point.length; i++) {
                if (point[i] == null) break;
                cPoint[pointCounter] = point[pointCounter];
                if (!version.startsWith("6")) pointspx[pointCounter] = (int) (Math.log(point[pointCounter].im() / (2 * Math.PI)) * 10); else pointspx[pointCounter] = (int) (Math.log(point[pointCounter].im() / (2 * Math.PI)) * 10);
                sError[pointCounter] = error[i];
                if ((!(complexToFrequency(cPoint[pointCounter]) < x1 || complexToFrequency(cPoint[pointCounter]) > x2 && cPoint[pointCounter].re() == 0.0))) pointCounter++;
            }
        }
    }

    public void plot(Point me) {
        cont.clear();
        cont.bbox.setBounds(getBounds());
        aTfrqG.setToIdentity();
        aTphase.setToIdentity();
        String strResult;
        DecimalFormat df = new DecimalFormat();
        df.applyPattern("0.#E0");
        try {
            MathAnalog.evaluate("<<Graphics`Graphics`", false);
            if (!acSweep.isEmpty()) MathAnalog.evaluate("v =" + aiSignal + "/." + acSweep + "[[1]]", false);
            MathAnalog.evaluate("nv = InterpolatingFunctionToList[v]", false);
            MathAnalog.evaluate("vAbs = Interpolation[Abs[nv]]", false);
            String deb = MathAnalog.evaluateToOutputForm("v", 0, false);
            if (!deb.startsWith("v")) {
                if (linearY) {
                    if (logarithmic) strResult = MathAnalog.evaluateToInputForm("FullGraphics[LogLinearPlot[{Abs[" + function + "[f]], vAbs[f]}, {f," + minValue + "," + maxValue + "}" + ",PlotRange->All, GridLines -> Automatic]]", 0, false); else strResult = MathAnalog.evaluateToInputForm("FullGraphics[Plot[{Abs[" + function + "[f]], vAbs[f]}, {f," + minValue + "," + maxValue + "}" + ",PlotRange->All, GridLines -> Automatic]]", 0, false);
                } else {
                    if (logarithmic) strResult = MathAnalog.evaluateToInputForm("FullGraphics[LogLinearPlot[{20*Log[10, Abs[" + function + "[f]]], 20*Log[10, vAbs[f]]}, {f," + minValue + "," + maxValue + "}" + ",PlotRange->All, GridLines -> Automatic]]", 0, false); else strResult = MathAnalog.evaluateToInputForm("FullGraphics[Plot[{20*Log[10, Abs[" + function + "[f]]], 20*Log[10, vAbs[f]]}, {f," + minValue + "," + maxValue + "}" + ",PlotRange->All, GridLines -> Automatic]]", 0, false);
                }
            } else {
                if (linearY) {
                    if (logarithmic) strResult = MathAnalog.evaluateToInputForm("FullGraphics[LogLinearPlot[{Abs[" + function + "[f]]}, {f," + minValue + "," + maxValue + "}" + ",PlotRange->All, GridLines -> Automatic]]", 0, false); else strResult = MathAnalog.evaluateToInputForm("FullGraphics[Plot[{Abs[" + function + "[f]]}, {f," + minValue + "," + maxValue + "}" + ",PlotRange->All, GridLines -> Automatic]]", 0, false);
                } else {
                    if (logarithmic) strResult = MathAnalog.evaluateToInputForm("FullGraphics[LogLinearPlot[{20*Log[10, Abs[" + function + "[f]]]}, {f," + minValue + "," + maxValue + "}" + ",PlotRange->All, GridLines -> Automatic]]", 0, false); else strResult = MathAnalog.evaluateToInputForm("FullGraphics[Plot[{20*Log[10, Abs[" + function + "[f]]]}, {f," + minValue + "," + maxValue + "}" + ",PlotRange->All, GridLines -> Automatic]]", 0, false);
                }
            }
            Pattern pattern = Pattern.compile("Line\\[[^\\]]*\\]");
            Matcher matcher = pattern.matcher(strResult);
            boolean found = false;
            CdgPolyline cdgPL = new CdgPolyline();
            GraphicsContainer frqG = new GraphicsContainer();
            GraphicsContainer phase = new GraphicsContainer();
            GraphicsContainer labelMY = new GraphicsContainer();
            GraphicsContainer labelPhX = new GraphicsContainer();
            GraphicsContainer labelPhY = new GraphicsContainer();
            txtPhaseY = new GraphicsContainer();
            txtFrqGY = new GraphicsContainer();
            txtPhaseX = new GraphicsContainer();
            txtFrqGX = new GraphicsContainer();
            double[] xpoints = null;
            double[] ypoints = null;
            while (matcher.find()) {
                found = true;
                String str = strResult.substring(matcher.start() + 7, matcher.end() - 3).replaceAll("\\Q.\\E*[*][\\Q^\\E]", "E");
                String[] lines = str.split("[}], [{]");
                xpoints = new double[lines.length];
                ypoints = new double[lines.length];
                for (int i = 0; i < lines.length; i++) {
                    xpoints[i] = Double.valueOf(lines[i].split(", ")[0]);
                    ypoints[i] = Double.valueOf(lines[i].split(", ")[1]);
                }
                cdgPL = new CdgPolyline(xpoints, ypoints, xpoints.length, (byte) 0, (byte) 0, axis);
                frqG.add(cdgPL);
            }
            int index = 0;
            if (matcher.find(0)) {
                found = true;
                index = matcher.end();
                String str = strResult.substring(matcher.start() + 7, matcher.end() - 3).replaceAll("\\Q.\\E*[*][\\Q^\\E]", "E");
                String[] lines = str.split("[}], [{]");
                xpoints = new double[lines.length];
                ypoints = new double[lines.length];
                for (int i = 0; i < lines.length; i++) {
                    xpoints[i] = Double.valueOf(lines[i].split(", ")[0]);
                    ypoints[i] = Double.valueOf(lines[i].split(", ")[1]);
                }
                cdgPL = new CdgPolyline(xpoints, ypoints, xpoints.length, (byte) 0, (byte) 0, simPlot);
                frqG.add(cdgPL);
            }
            if (matcher.find(index) && !deb.startsWith("v")) {
                found = true;
                String str = strResult.substring(matcher.start() + 7, matcher.end() - 3).replaceAll("\\Q.\\E*[*][\\Q^\\E]", "E");
                String[] lines = str.split("[}], [{]");
                xpoints = new double[lines.length];
                ypoints = new double[lines.length];
                for (int i = 0; i < lines.length; i++) {
                    xpoints[i] = Double.valueOf(lines[i].split(", ")[0]);
                    ypoints[i] = Double.valueOf(lines[i].split(", ")[1]);
                }
                cdgPL = new CdgPolyline(xpoints, ypoints, xpoints.length, (byte) 0, (byte) 0, aiPlot);
                frqG.add(cdgPL);
            }
            frqG.calculateBounds();
            aTfrqG.scale(cont.getBounds().getWidth() / frqG.getBounds().getWidth(), 0.5 * cont.getBounds().getHeight() / frqG.getBounds().getHeight());
            ginstF = new CdgInstance(0, 0, aTfrqG, frqG);
            ginstF.setClipRegion(frqG.bbox);
            cont.add(ginstF);
            byte layerVar = 0x00B;
            pattern = Pattern.compile("Text\\[[^\\]]*0\\Q.\\E[}]\\]");
            matcher = pattern.matcher(strResult);
            while (matcher.find()) {
                found = true;
                String str = strResult.substring(matcher.start() + 5, matcher.end() - 3).replaceAll("\\Q.\\E*[*][\\Q^\\E]", "E");
                str = str.split(",")[0];
                try {
                    float f = -(float) (Float.valueOf(str) * aTfrqG.getScaleY()) - (float) (ginstF.trf.getTranslateY());
                    CdgText cdgT = new CdgText((float) (frqG.getBounds().getMinX() * aTfrqG.getScaleX() + ginstF.trf.getTranslateX()), f, str, layerVar, (byte) 0, fontColor);
                    txtFrqGY.add(cdgT);
                } catch (NumberFormatException e) {
                }
            }
            pattern = Pattern.compile("Text\\[[^\\]]*1\\Q.\\E[}]\\]");
            matcher = pattern.matcher(strResult);
            while (matcher.find()) {
                found = true;
                String str = strResult.substring(matcher.start() + 5, matcher.end() - 3).replaceAll("\\Q.\\E*[*][\\Q^\\E]", "E");
                str = str.split(",")[0];
                str = str.replace("0, ", "E");
                try {
                    float f;
                    if (logarithmic) {
                        if (version.startsWith("6")) f = (float) (Math.log(Float.valueOf(str)) * aTfrqG.getScaleX()); else f = (float) (Math.log10(Float.valueOf(str)) * aTfrqG.getScaleX());
                    } else f = (float) (Float.valueOf(str) * aTfrqG.getScaleX() / 1);
                    CdgText cdgT = new CdgText(f, -(float) (ginstF.trf.getTranslateY() + frqG.getBounds().getMinY() * aTfrqG.getScaleY()), str, layerVar, (byte) 0, fontColor);
                    txtFrqGX.add(cdgT);
                } catch (NumberFormatException e) {
                }
            }
            pattern = Pattern.compile("Text\\[Superscript\\[[^\\]]*\\][^\\]]*1\\Q.\\E[}]\\]");
            matcher = pattern.matcher(strResult);
            while (matcher.find()) {
                found = true;
                String str = strResult.substring(matcher.start() + 17, matcher.end() - 3).replaceAll("\\Q.\\E*[*][\\Q^\\E]", "E");
                str = str.split("]")[0];
                str = str.replace("0, ", "E");
                try {
                    float f;
                    if (logarithmic) {
                        if (version.startsWith("6")) f = (float) (Math.log(Float.valueOf(str)) * aTfrqG.getScaleX()); else f = (float) (Math.log10(Float.valueOf(str)) * aTfrqG.getScaleX());
                    } else f = (float) (Float.valueOf(str) * aTfrqG.getScaleX() / 1);
                    CdgText cdgT = new CdgText(f, -(float) (ginstF.trf.getTranslateY() + frqG.getBounds().getMinY() * aTfrqG.getScaleY()), str, layerVar, (byte) 0, fontColor);
                    txtFrqGX.add(cdgT);
                } catch (NumberFormatException e) {
                }
            }
            pattern = Pattern.compile("Text\\[Superscript\\[[^\\]]*\\][^\\]]*0\\Q.\\E[}]\\]");
            matcher = pattern.matcher(strResult);
            while (matcher.find()) {
                found = true;
                String str = strResult.substring(matcher.start() + 17, matcher.end() - 3).replaceAll("\\Q.\\E*[*][\\Q^\\E]", "E");
                str = str.split("]")[0];
                str = str.replace("0, ", "E");
                try {
                    float f = -(float) (Float.valueOf(str) * aTfrqG.getScaleY()) - (float) (ginstF.trf.getTranslateY());
                    CdgText cdgT = new CdgText((float) (frqG.getBounds().getMinX() * aTfrqG.getScaleX() + ginstF.trf.getTranslateX()), f, str, layerVar, (byte) 0, fontColor);
                    txtFrqGY.add(cdgT);
                } catch (NumberFormatException e) {
                }
            }
            pattern = Pattern.compile("Text\\[NumberForm\\[[^\\]]*\\][^\\]]*1\\Q.\\E[}]\\]");
            matcher = pattern.matcher(strResult);
            while (matcher.find()) {
                found = true;
                String str = strResult.substring(matcher.start() + 16, matcher.end() - 3).replaceAll("\\Q.\\E*[*][\\Q^\\E]", "E");
                str = str.split("]")[0];
                str = str.replace("0, ", "E");
                try {
                    float f;
                    if (logarithmic) {
                        if (version.startsWith("6")) f = (float) (Math.log(Float.valueOf(str)) * aTfrqG.getScaleX()); else f = (float) (Math.log10(Float.valueOf(str)) * aTfrqG.getScaleX());
                    } else f = (float) (Float.valueOf(str) * aTfrqG.getScaleX() / 1);
                    CdgText cdgT = new CdgText(f, -(float) (ginstF.trf.getTranslateY() + frqG.getBounds().getMinY() * aTfrqG.getScaleY()), str, layerVar, (byte) 0, fontColor);
                    txtFrqGX.add(cdgT);
                } catch (NumberFormatException e) {
                }
            }
            pattern = Pattern.compile("Text\\[NumberForm\\[[^\\]]*\\][^\\]]*0\\Q.\\E[}]\\]");
            matcher = pattern.matcher(strResult);
            while (matcher.find()) {
                found = true;
                String str = strResult.substring(matcher.start() + 16, matcher.end() - 3).replaceAll("\\Q.\\E*[*][\\Q^\\E]", "E");
                str = str.split("]")[0];
                str = str.replace("0, ", "E");
                try {
                    float f = -(float) (Float.valueOf(str) * aTfrqG.getScaleY()) - (float) (ginstF.trf.getTranslateY());
                    CdgText cdgT = new CdgText((float) (frqG.getBounds().getMinX() * aTfrqG.getScaleX() + ginstF.trf.getTranslateX()), f, str, layerVar, (byte) 0, fontColor);
                    txtFrqGY.add(cdgT);
                } catch (NumberFormatException e) {
                }
            }
            CdgText cdgTl = new CdgText((float) (ginstF.trf.getTranslateY() + frqG.getBounds().getMinY() * aTfrqG.getScaleY()), (float) (frqG.getBounds().getMinX() * aTfrqG.getScaleX() + ginstF.trf.getTranslateX()) - 10, "Magnitude", layerVar, (byte) 0, fontColor);
            labelMY.add(cdgTl);
            MathAnalog.evaluate("x1 = InterpolatingFunctionToList[" + function + "]", false);
            MathAnalog.evaluate("x2 = {{x1[[1, 1]], 180/Pi*Arg[x1[[1, 2]]]}}", false);
            MathAnalog.evaluate("offset = 0", false);
            MathAnalog.evaluate("Do[ If[(Abs[180/Pi*(Arg[x1[[i, 2]]] - Arg[x1[[i - 1, 2]]])] > 0.9*360), If[(Arg[x1[[i, 2]]] - Arg[x1[[i - 1, 2]]]) < 0, offset += 360; AppendTo[x2, { x1[[i, 1]], 180/Pi*Arg[x1[[i, 2]]] + offset}], offset -= 360; AppendTo[x2, {x1[[i, 1]], 180/Pi*Arg[x1[[i, 2]]] + offset}]], AppendTo[x2, {x1[[i, 1]], 180/Pi*Arg[x1[[i, 2]]] + offset}]], {i, 2, Length[x1]}]", false);
            MathAnalog.evaluate("ifun = Interpolation[x2]", false);
            MathAnalog.evaluate("x1 = InterpolatingFunctionToList[v]", false);
            MathAnalog.evaluate("x2 = {{x1[[1, 1]], 180/Pi*Arg[x1[[1, 2]]]}}", false);
            MathAnalog.evaluate("offset = 0", false);
            MathAnalog.evaluate("Do[ If[(Abs[180/Pi*(Arg[x1[[i, 2]]] - Arg[x1[[i - 1, 2]]])] > 0.9*360), If[(Arg[x1[[i, 2]]] - Arg[x1[[i - 1, 2]]]) < 0, offset += 360; AppendTo[x2, { x1[[i, 1]], 180/Pi*Arg[x1[[i, 2]]] + offset}], offset -= 360; AppendTo[x2, {x1[[i, 1]], 180/Pi*Arg[x1[[i, 2]]] + offset}]], AppendTo[x2, {x1[[i, 1]], 180/Pi*Arg[x1[[i, 2]]] + offset}]], {i, 2, Length[x1]}]", false);
            MathAnalog.evaluate("iv = Interpolation[x2]", false);
            if (!deb.startsWith("v")) {
                if (logarithmic) strResult = MathAnalog.evaluateToInputForm("FullGraphics[LogLinearPlot[{ifun[f], iv[f]}, {f," + minValue + "," + maxValue + "}" + ", GridLines -> Automatic, PlotRange->All]]", 0, false); else strResult = MathAnalog.evaluateToInputForm("FullGraphics[Plot[{ifun[f], iv[f]}, {f," + minValue + "," + maxValue + "}" + ", GridLines -> Automatic, PlotRange->All]]", 0, false);
            } else {
                if (logarithmic) strResult = MathAnalog.evaluateToInputForm("FullGraphics[LogLinearPlot[{ifun[f]}, {f," + minValue + "," + maxValue + "}" + ", GridLines -> Automatic, PlotRange->All]]", 0, false); else strResult = MathAnalog.evaluateToInputForm("FullGraphics[Plot[{ifun[f]}, {f," + minValue + "," + maxValue + "}" + ", GridLines -> Automatic, PlotRange->All]]", 0, false);
            }
            pattern = Pattern.compile("Line\\[[^\\]]*\\]");
            matcher = pattern.matcher(strResult);
            found = false;
            layerVar = 0x00A;
            while (matcher.find()) {
                found = true;
                String str = strResult.substring(matcher.start() + 7, matcher.end() - 3).replaceAll("\\Q.\\E*[*][\\Q^\\E]", "E");
                String[] lines = str.split("[}], [{]");
                xpoints = new double[lines.length];
                ypoints = new double[lines.length];
                for (int i = 0; i < lines.length; i++) {
                    xpoints[i] = Double.valueOf(lines[i].split(", ")[0]);
                    ypoints[i] = Double.valueOf(lines[i].split(", ")[1]);
                }
                cdgPL = new CdgPolyline(xpoints, ypoints, xpoints.length, layerVar, (byte) 0, axis);
                phase.add(cdgPL);
            }
            index = 0;
            if (matcher.find(0)) {
                found = true;
                index = matcher.end();
                String str = strResult.substring(matcher.start() + 7, matcher.end() - 3).replaceAll("\\Q.\\E*[*][\\Q^\\E]", "E");
                String[] lines = str.split("[}], [{]");
                xpoints = new double[lines.length];
                ypoints = new double[lines.length];
                for (int i = 0; i < lines.length; i++) {
                    xpoints[i] = Double.valueOf(lines[i].split(", ")[0]);
                    ypoints[i] = Double.valueOf(lines[i].split(", ")[1]);
                }
                cdgPL = new CdgPolyline(xpoints, ypoints, xpoints.length, layerVar, (byte) 0, simPlot);
                phase.add(cdgPL);
            }
            if (matcher.find(index) && !deb.startsWith("v")) {
                found = true;
                String str = strResult.substring(matcher.start() + 7, matcher.end() - 3).replaceAll("\\Q.\\E*[*][\\Q^\\E]", "E");
                String[] lines = str.split("[}], [{]");
                xpoints = new double[lines.length];
                ypoints = new double[lines.length];
                for (int i = 0; i < lines.length; i++) {
                    xpoints[i] = Double.valueOf(lines[i].split(", ")[0]);
                    ypoints[i] = Double.valueOf(lines[i].split(", ")[1]);
                }
                cdgPL = new CdgPolyline(xpoints, ypoints, xpoints.length, layerVar, (byte) 0, aiPlot);
                phase.add(cdgPL);
            }
            phase.calculateBounds();
            aTphase.scale(cont.getBounds().getWidth() / phase.getBounds().getWidth(), 0.5 * cont.getBounds().getHeight() / phase.getBounds().getHeight());
            ginstPh = new CdgInstance(0, (int) (aTfrqG.getScaleY() * frqG.bbox.getMinY() - aTphase.getScaleY() * phase.bbox.getMaxY()) - 10, aTphase, phase);
            ginstPh.setClipRegion(phase.bbox);
            cont.add(ginstPh);
            layerVar = 0x00B;
            pattern = Pattern.compile("Text\\[[^\\]]*0\\Q.\\E[}]\\]");
            matcher = pattern.matcher(strResult);
            while (matcher.find()) {
                found = true;
                String str = strResult.substring(matcher.start() + 5, matcher.end() - 3).replaceAll("\\Q.\\E*[*][\\Q^\\E]", "E");
                str = str.split(",")[0];
                try {
                    float f = -(float) (Float.valueOf(str) * aTphase.getScaleY()) - (float) (ginstPh.trf.getTranslateY());
                    CdgText cdgT = new CdgText((float) (phase.getBounds().getMinX() * aTphase.getScaleX() + ginstPh.trf.getTranslateX()), f, str + "�", layerVar, (byte) 0, fontColor);
                    txtPhaseY.add(cdgT);
                } catch (NumberFormatException e) {
                }
            }
            pattern = Pattern.compile("Text\\[[^\\]]*1\\Q.\\E[}]\\]");
            matcher = pattern.matcher(strResult);
            while (matcher.find()) {
                found = true;
                String str = strResult.substring(matcher.start() + 5, matcher.end() - 3).replaceAll("\\Q.\\E*[*][\\Q^\\E]", "E");
                str = str.split(",")[0];
                str = str.replace("0, ", "E");
                try {
                    float f;
                    if (logarithmic) {
                        if (version.startsWith("6")) f = (float) (Math.log(Float.valueOf(str)) * aTphase.getScaleX()); else f = (float) (Math.log10(Float.valueOf(str)) * aTphase.getScaleX());
                    } else f = (float) (Float.valueOf(str) * aTphase.getScaleX() / 1);
                    CdgText cdgT = new CdgText(f, -(float) (ginstPh.trf.getTranslateY() + phase.getBounds().getMinY() * aTphase.getScaleY()), str, layerVar, (byte) 0, fontColor);
                    txtPhaseX.add(cdgT);
                } catch (NumberFormatException e) {
                }
            }
            pattern = Pattern.compile("Text\\[Superscript\\[[^\\]]*\\][^\\]]*1\\Q.\\E[}]\\]");
            matcher = pattern.matcher(strResult);
            while (matcher.find()) {
                found = true;
                String str = strResult.substring(matcher.start() + 17, matcher.end() - 3).replaceAll("\\Q.\\E*[*][\\Q^\\E]", "E");
                str = str.split("]")[0];
                str = str.replace("0, ", "E");
                try {
                    float f;
                    if (logarithmic) {
                        if (version.startsWith("6")) f = (float) (Math.log(Float.valueOf(str)) * aTphase.getScaleX()); else f = (float) (Math.log10(Float.valueOf(str)) * aTphase.getScaleX());
                    } else f = (float) (Float.valueOf(str) * aTphase.getScaleX() / 1);
                    CdgText cdgT = new CdgText(f, -(float) (ginstPh.trf.getTranslateY() + phase.getBounds().getMinY() * aTphase.getScaleY()), str, layerVar, (byte) 0, fontColor);
                    txtPhaseX.add(cdgT);
                } catch (NumberFormatException e) {
                }
            }
            pattern = Pattern.compile("Text\\[Superscript\\[[^\\]]*\\][^\\]]*0\\Q.\\E[}]\\]");
            matcher = pattern.matcher(strResult);
            while (matcher.find()) {
                found = true;
                String str = strResult.substring(matcher.start() + 17, matcher.end() - 3).replaceAll("\\Q.\\E*[*][\\Q^\\E]", "E");
                str = str.split("]")[0];
                str = str.replace("0, ", "E");
                try {
                    float f = -(float) (Float.valueOf(str) * aTphase.getScaleY()) - (float) (ginstPh.trf.getTranslateY());
                    CdgText cdgT = new CdgText((float) (phase.getBounds().getMinX() * aTphase.getScaleX() + ginstPh.trf.getTranslateX()), f, str + "�", layerVar, (byte) 0, fontColor);
                    txtPhaseY.add(cdgT);
                } catch (NumberFormatException e) {
                }
            }
            pattern = Pattern.compile("Text\\[NumberForm\\[[^\\]]*\\][^\\]]*1\\Q.\\E[}]\\]");
            matcher = pattern.matcher(strResult);
            while (matcher.find()) {
                found = true;
                String str = strResult.substring(matcher.start() + 16, matcher.end() - 3).replaceAll("\\Q.\\E*[*][\\Q^\\E]", "E");
                str = str.split("]")[0];
                str = str.replace("0, ", "E");
                try {
                    float f;
                    if (logarithmic) {
                        if (version.startsWith("6")) f = (float) (Math.log(Float.valueOf(str)) * aTphase.getScaleX()); else f = (float) (Math.log10(Float.valueOf(str)) * aTphase.getScaleX());
                    } else f = (float) (Float.valueOf(str) * aTphase.getScaleX() / 1);
                    CdgText cdgT = new CdgText(f, -(float) (ginstPh.trf.getTranslateY() + phase.getBounds().getMinY() * aTphase.getScaleY()), str, layerVar, (byte) 0, fontColor);
                    txtPhaseX.add(cdgT);
                } catch (NumberFormatException e) {
                }
            }
            pattern = Pattern.compile("Text\\[NumberForm\\[[^\\]]*\\][^\\]]*0\\Q.\\E[}]\\]");
            matcher = pattern.matcher(strResult);
            while (matcher.find()) {
                found = true;
                String str = strResult.substring(matcher.start() + 16, matcher.end() - 3).replaceAll("\\Q.\\E*[*][\\Q^\\E]", "E");
                str = str.split("]")[0];
                str = str.replace("0, ", "E");
                try {
                    float f = -(float) (Float.valueOf(str) * aTphase.getScaleY()) - (float) (ginstPh.trf.getTranslateY());
                    CdgText cdgT = new CdgText((float) (phase.getBounds().getMinX() * aTphase.getScaleX() + ginstPh.trf.getTranslateX()), f, str + "�", layerVar, (byte) 0, fontColor);
                    txtPhaseY.add(cdgT);
                } catch (NumberFormatException e) {
                }
            }
            AffineTransform atLabel = new AffineTransform(1, 0, 0, -1, 0, 0);
            cdgTl = new CdgText((float) (phase.getBounds().getMinX() * aTphase.getScaleX() + ginstPh.trf.getTranslateX()), -(float) (ginstPh.trf.getTranslateY() + phase.getBounds().getMinY() * aTphase.getScaleY()) + 20, "Frequency", layerVar, (byte) 0, fontColor);
            labelPhX.add(cdgTl);
            labelPhX.calculateBounds();
            CdgInstance ginstlabelPhX = new CdgInstance(0, 0, atLabel, labelPhX);
            cont.add(ginstlabelPhX);
            atLabel.concatenate(new AffineTransform(0, -1, 1, 0, 0, 0));
            cdgTl = new CdgText((float) (ginstPh.trf.getTranslateY() + phase.getBounds().getMinY() * aTphase.getScaleY()), (float) (phase.getBounds().getMinX() * aTphase.getScaleX() + ginstPh.trf.getTranslateX()) - 10, "Phase", layerVar, (byte) 0, fontColor);
            labelPhY.add(cdgTl);
            labelPhY.calculateBounds();
            CdgInstance ginstlabelPhY = new CdgInstance(0, 0, atLabel, labelPhY);
            cont.add(ginstlabelPhY);
            labelMY.calculateBounds();
            CdgInstance ginstlabelMY = new CdgInstance(0, 0, atLabel, labelMY);
            cont.add(ginstlabelMY);
            txtFrqGY.calculateBounds();
            AffineTransform atTxt = new AffineTransform(1, 0, 0, -1, 0, 0);
            CdgInstance ginstTxtFrqGY = new CdgInstance(0, 0, atTxt, txtFrqGY);
            ginstTxtFrqGY.setClipRegion(txtFrqGY.bbox);
            cont.add(ginstTxtFrqGY);
            txtPhaseY.calculateBounds();
            CdgInstance ginstTxtPhaseY = new CdgInstance(0, 0, atTxt, txtPhaseY);
            ginstTxtPhaseY.setClipRegion(txtPhaseY.bbox);
            cont.add(ginstTxtPhaseY);
            txtFrqGX.calculateBounds();
            CdgInstance ginstTxtFrqGX = new CdgInstance(0, 0, atTxt, txtFrqGX);
            ginstTxtFrqGX.setClipRegion(txtFrqGX.bbox);
            cont.add(ginstTxtFrqGX);
            txtPhaseX.calculateBounds();
            CdgInstance ginstTxtPhaseX = new CdgInstance(0, 0, atTxt, txtPhaseX);
            ginstTxtPhaseX.setClipRegion(txtPhaseX.bbox);
            cont.add(ginstTxtPhaseX);
            cont.calculateBounds();
            for (int i = 0; i < pointCounter; i++) {
                try {
                    double im = (cPoint[i].im() / (2 * Math.PI));
                    if (logarithmic) if (version.startsWith("6")) paintLine(new Point2D.Double(Math.log(im), 0)); else paintLine(new Point2D.Double(Math.log10(im), 0)); else paintLine(new Point2D.Double(im, 0));
                } catch (Exception e) {
                }
            }
            Fit();
            if (!found) {
                System.out.println("wrong Graphics");
            }
        } catch (MathLinkException e) {
            e.printStackTrace();
        }
    }

    public void mouseEntered(MouseEvent me) {
        int i, h = 0;
        int xx = -1;
        if (draggedPoint == -1) {
            for (i = 0; i < pointCounter; i++) {
                xx = pointspx[i];
                if (xx == screenXToInt(me.getPoint())) {
                    h = 1;
                    break;
                }
            }
            if (h == 1) {
                Toolkit.getDefaultToolkit().sync();
                highlightedPoint = i;
            } else {
                Toolkit.getDefaultToolkit().sync();
            }
        }
    }

    public void mouseExited(MouseEvent arg0) {
        if (fs != null) fs.clearFrequency(this.fs);
        Toolkit.getDefaultToolkit().sync();
    }

    public void mousePressed(MouseEvent me) {
        if (me.getButton() == 1 && me.getClickCount() == 1 && (screenXToFrequency(me.getPoint()) > x1) && (screenXToFrequency(me.getPoint()) < x2)) {
            int i;
            for (i = 0; i < pointCounter; i++) {
                if (pointspx[i] == (screenXToInt(me.getPoint()))) {
                    draggedPoint = i;
                    if (pts != null) pts.deletePoint(cPoint[i], sError[i], jBPF);
                    Toolkit.getDefaultToolkit().sync();
                    return;
                }
            }
            cPoint[pointCounter] = screenXToComplex(me.getPoint());
            pointspx[pointCounter] = screenXToInt(me.getPoint());
            sError[pointCounter] = String.valueOf(defaultError);
            highlightedPoint = pointCounter;
            draggedPoint = pointCounter;
            isPointAlreadyAdded = true;
            fs.frequencyChanged(screenXToFrequency(me.getPoint()), fs);
            if (pts != null) {
                pts.addPoint(cPoint[pointCounter], sError[pointCounter], jBPF);
                pts.selectPoint(cPoint[pointCounter], sError[pointCounter], jBPF);
            }
            pointCounter++;
            plot(me.getPoint());
            txtFrqGX.setVisible(true);
            txtFrqGY.setVisible(true);
            txtPhaseX.setVisible(true);
            txtPhaseY.setVisible(true);
            Toolkit.getDefaultToolkit().sync();
        }
        if (me.getButton() == 3 && highlightedPoint != -1) {
            String s = (String) JOptionPane.showInputDialog(null, "Enter MaxError:", "MaxError", JOptionPane.PLAIN_MESSAGE, null, null, sError[highlightedPoint]);
            if (s != null) {
                defaultError = s;
                pts.changePoint(cPoint[highlightedPoint], sError[highlightedPoint], s, jBPF);
                sError[highlightedPoint] = s;
            }
        }
    }

    /**
	 * Method handles events generated when mouse is released.
	 */
    public void mouseReleased(MouseEvent e) {
        int c = pointCounter;
        if ((screenXToFrequency(e.getPoint()) > x1) && (screenXToFrequency(e.getPoint()) < x2)) {
            if (draggedPoint != -1) {
                highlightedPoint = draggedPoint;
                if (isPointAlreadyAdded == false) {
                    if ((Math.exp(factor * pointspx[draggedPoint] / 10) < x1) || (Math.exp(factor * pointspx[draggedPoint] / 10) > x2)) {
                        if (draggedPoint == pointCounter) pointCounter--; else {
                            for (int i = draggedPoint + 1; i < pointCounter; i++) {
                                cPoint[i - 1] = cPoint[i];
                                pointspx[i - 1] = pointspx[i];
                                sError[i - 1] = sError[i];
                            }
                            pointCounter--;
                        }
                    }
                    if (c == pointCounter) {
                        pts.addPoint(cPoint[draggedPoint], sError[draggedPoint], jBPF);
                    }
                }
                draggedPoint = -1;
            }
            isPointAlreadyAdded = false;
        }
    }

    public void mouseDragged(MouseEvent me) {
        if (isPointAlreadyAdded == true) pts.deletePoint(cPoint[draggedPoint], sError[draggedPoint], jBPF);
        if ((screenXToFrequency(new Point(me.getX(), 0)) > x1) && (screenXToFrequency(new Point(me.getX(), 0)) < x2)) {
            fs.frequencyChanged(screenXToFrequency(me.getPoint()), fs);
            if (fs != null) fs.selectFrequency(screenXToFrequency(me.getPoint()), fs);
            plot(me.getPoint());
        } else {
            fs.frequencyChanged(screenXToFrequency(me.getPoint()), fs);
            if (fs != null) fs.clearFrequency(fs);
        }
        if (draggedPoint != -1) {
            cPoint[draggedPoint] = screenXToComplex(me.getPoint());
            pointspx[draggedPoint] = screenXToInt(me.getPoint());
        }
        isPointAlreadyAdded = false;
    }

    public void mouseMoved(MouseEvent me) {
        super.mouseMoved(me);
        int i, h = 0;
        int xx = -1;
        if ((me.getX() > x1) && (me.getX() < x2)) {
            fs.frequencyChanged(screenXToFrequency(me.getPoint()), fs);
            if (fs != null) fs.selectFrequency(screenXToFrequency(me.getPoint()), fs);
        } else fs.frequencyChanged(screenXToFrequency(me.getPoint()), fs);
        if (draggedPoint == -1) {
            for (i = 0; i < pointCounter; i++) {
                xx = pointspx[i];
                if (xx == screenXToInt(me.getPoint())) {
                    h = 1;
                    break;
                }
            }
            if (h == 1) {
                Toolkit.getDefaultToolkit().sync();
                highlightedPoint = i;
                fs.frequencyChanged(screenXToFrequency(me.getPoint()), fs);
                pts.selectPoint(cPoint[i], sError[i], jBPF);
            } else if (highlightedPoint > -1) {
                Toolkit.getDefaultToolkit().sync();
                highlightedPoint = -1;
                if (pts != null) pts.clearSelection(jBPF);
            }
        }
    }

    private void paintLine(Point2D src) {
        try {
            Point2D dst1 = new Point2D.Double();
            GraphicsContainer gc = new GraphicsContainer();
            aTphase.transform(src, dst1);
            CdgLine cdgL = new CdgLine((int) dst1.getX(), (int) cont.getBounds().getMinY(), (int) dst1.getX(), (int) cont.getBounds().getMaxY(), (byte) 0, (byte) 0, Color.RED);
            gc.add(cdgL);
            gc.calculateBounds();
            CdgInstance ginstL = new CdgInstance(0, 0, new AffineTransform(), gc);
            cont.add(ginstL);
            cont.calculateBounds();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private double complexToFrequency(Complex c) {
        return Math.sqrt(c.im() * c.im() + c.re() * c.re()) / (2 * java.lang.Math.PI);
    }

    private double screenXToFrequency(Point src) {
        try {
            Point2D dst1 = new Point2D.Double();
            Point2D dst2 = new Point2D.Double();
            AffineTransform iAfnScreen = afnScreen.createInverse();
            iAfnScreen.transform(src, dst1);
            iAfnScreen = ginstF.trf.createInverse();
            iAfnScreen.transform(dst1, dst2);
            if (!(dst2.getY() > ginstF.getClipRegion().getBounds2D().getMinY())) {
                iAfnScreen = ginstPh.trf.createInverse();
                iAfnScreen.transform(dst1, dst2);
            }
            if (logarithmic) return (Math.exp(factor * dst2.getX()));
            return dst2.getX();
        } catch (NoninvertibleTransformException e1) {
            e1.printStackTrace();
            return -1;
        }
    }

    private Complex frequencyToComplex(double frequency) {
        return new Complex(0.0, frequency * 2 * java.lang.Math.PI);
    }

    private Complex screenXToComplex(Point me) {
        return frequencyToComplex(screenXToFrequency(me));
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);
        try {
            Point src = e.getPoint();
            Point2D dst1 = new Point2D.Double();
            Point2D dst2 = new Point2D.Double();
            AffineTransform iAfnScreen = afnScreen.createInverse();
            iAfnScreen.transform(src, dst1);
            iAfnScreen = ginstF.trf.createInverse();
            iAfnScreen.transform(dst1, dst2);
            if (dst2.getY() > ginstF.getClipRegion().getBounds2D().getMinY()) {
                double fact = Math.exp(0.2 * e.getWheelRotation());
                AffineTransform zoom = new AffineTransform(fact, 0, 0, fact, (dst2.getX() - dst2.getX() * fact), (dst2.getY() - dst2.getY() * fact));
                ginstF.trf.concatenate(zoom);
                izoom = zoom.createInverse();
                Shape clip = ginstF.getClipRegion();
                clip = izoom.createTransformedShape(clip);
                ginstF.setClipRegion(clip);
                txtFrqGX.setVisible(false);
                txtFrqGY.setVisible(false);
            } else {
                iAfnScreen = ginstPh.trf.createInverse();
                iAfnScreen.transform(dst1, dst2);
                double fact = Math.exp(0.2 * e.getWheelRotation());
                AffineTransform zoom = new AffineTransform(fact, 0, 0, fact, (dst2.getX() - dst2.getX() * fact), (dst2.getY() - dst2.getY() * fact));
                ginstPh.trf.concatenate(zoom);
                AffineTransform izoom = zoom.createInverse();
                Shape clip = ginstPh.getClipRegion();
                clip = izoom.createTransformedShape(clip);
                ginstPh.setClipRegion(clip);
                txtPhaseX.setVisible(false);
                txtPhaseY.setVisible(false);
            }
            repaint();
        } catch (NoninvertibleTransformException ex) {
        }
    }

    private int screenXToInt(Point src) {
        try {
            Point2D dst1 = new Point2D.Double();
            Point2D dst2 = new Point2D.Double();
            AffineTransform iAfnScreen = afnScreen.createInverse();
            iAfnScreen.transform(src, dst1);
            iAfnScreen = ginstF.trf.createInverse();
            iAfnScreen.transform(dst1, dst2);
            if (!(dst2.getY() > ginstF.getClipRegion().getBounds2D().getMinY())) {
                iAfnScreen = ginstPh.trf.createInverse();
                iAfnScreen.transform(dst1, dst2);
            }
            if (logarithmic) {
                return (int) (dst2.getX() * 10);
            }
            if (version.startsWith("6")) return (int) (Math.log(dst2.getX()) * 10);
            return (int) (Math.log10(dst2.getX()) * 10);
        } catch (NoninvertibleTransformException e1) {
            e1.printStackTrace();
            return -1;
        }
    }

    public Color getSimPlot() {
        return simPlot;
    }

    public void setSimPlot(Color plot) {
        this.simPlot = plot;
    }

    public Color getAxis() {
        return axis;
    }

    public void setAxis(Color axis) {
        this.axis = axis;
    }

    public Color getFontColor() {
        return fontColor;
    }

    public void setFontColor(Color fontColor) {
        this.fontColor = fontColor;
    }

    public int print(Graphics arg0, PageFormat arg1, int arg2) throws PrinterException {
        if (arg2 == 0) {
            Graphics2D g2d = (Graphics2D) arg0;
            g2d.translate(arg1.getImageableX(), arg1.getImageableY());
            g2d.scale(arg1.getImageableWidth() / getBounds().getWidth(), arg1.getImageableHeight() / getBounds().getHeight());
            paint(g2d);
            return Printable.PAGE_EXISTS;
        } else {
            return Printable.NO_SUCH_PAGE;
        }
    }

    public Color getAiPlot() {
        return aiPlot;
    }

    public void setAiPlot(Color aiPlot) {
        this.aiPlot = aiPlot;
    }

    public void deletePoint(Complex point, String error, PointSelection pts) {
        for (int j = 0; j < pointCounter; j++) {
            if (mf.formatMath(cPoint[j]).equals(mf.formatMath(point)) && sError[j].trim().equals(error.trim())) {
                for (int i = j + 1; i < pointCounter; i++) {
                    cPoint[i - 1] = cPoint[i];
                    pointspx[i - 1] = pointspx[i];
                    sError[i - 1] = sError[i];
                }
                pointCounter--;
                plot(null);
                break;
            }
        }
    }

    public void changePoint(Complex point, String error, String newError, PointSelection pts) {
        for (int j = 0; j < pointCounter; j++) {
            if (mf.formatMath(cPoint[j]).equals(mf.formatMath(point)) && sError[j].trim().equals(error.trim())) {
                sError[j] = newError;
                break;
            }
        }
    }

    public void addPoint(Complex p, String err, PointSelection pts) {
        cPoint[pointCounter] = p;
        pointspx[pointCounter] = (int) (Math.log10(Math.sqrt(p.im() * p.im() + p.re() * p.re()) / (2 * Math.PI)) * 10);
        sError[pointCounter] = err;
        if ((!(complexToFrequency(cPoint[pointCounter]) < x1 || complexToFrequency(cPoint[pointCounter]) > x2))) pointCounter++;
        plot(null);
    }
}
