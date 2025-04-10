public class Test {    public void setXYModel(INumberScalar axattribute, INumberScalar ayattribute) {
        if ((xattribute != null) || (yattribute != null)) clearXYModel();
        if ((axattribute == null) || (ayattribute == null)) return;
        xattribute = axattribute;
        yattribute = ayattribute;
        if (xattribute.getMaxValue() > defaultXMaxValue) setDefaultXMaxValue(xattribute.getMaxValue());
        if (xattribute.getMaxAlarm() < defaultXMaxAlarm) setDefaultXMaxAlarm(xattribute.getMaxAlarm());
        if (xattribute.getMinValue() < defaultXMinValue) setDefaultXMinValue(xattribute.getMinValue());
        if (xattribute.getMinAlarm() > defaultXMinAlarm) setDefaultXMinAlarm(xattribute.getMinAlarm());
        if (yattribute.getMaxValue() > defaultYMaxValue) setDefaultYMaxValue(yattribute.getMaxValue());
        if (yattribute.getMaxAlarm() < defaultYMaxAlarm) setDefaultYMaxAlarm(yattribute.getMaxAlarm());
        if (yattribute.getMinValue() < defaultYMinValue) setDefaultYMinValue(yattribute.getMinValue());
        if (yattribute.getMinAlarm() > defaultYMinAlarm) setDefaultYMinAlarm(yattribute.getMinAlarm());
        setHeader(xattribute.getName());
        pointView.setName(yattribute.getName());
        pointView.setLineWidth(2);
        pointView.setColor(ATKConstant.getColor4State("VALID"));
        pointView.setLabelVisible(true);
        pointView.setStyle(JLDataView.FILL_STYLE_SOLID);
        pointView.setLineWidth(1);
        pointView.setFillStyle(JLDataView.FILL_STYLE_SOLID);
        pointView.setMarker(markerStyle);
        getY1Axis().addDataView(pointView);
        yattribute.addNumberScalarListener(this);
        setMiddleLineVisible(middleLineVisible);
        midleLineView.setColor(Color.BLACK);
        midleLineView.setStyle(JLDataView.FILL_STYLE_NONE);
        midleLineView.setMarker(JLDataView.MARKER_HORIZ_LINE);
        midleLineView.setName("X middle axis");
        double xmidle = (defaultXMaxValue + defaultXMinValue) / 2;
        midleLineView.add(xmidle, defaultYMinValue);
        midleLineView.add(xmidle, defaultYMaxValue);
        getY1Axis().addDataView(midleLineView);
    }
}