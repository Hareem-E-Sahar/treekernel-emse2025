package com.joebertj.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class DateUtil {

    private String value;

    private String effective;

    private String maturity;

    private String expiry;

    private String sdfIn;

    private String sdfOut;

    public DateUtil() {
    }

    public String getExpiry() {
        SimpleDateFormat sdfIn = new SimpleDateFormat(this.sdfIn);
        SimpleDateFormat sdfOut = new SimpleDateFormat(this.sdfOut);
        GregorianCalendar expiry = new GregorianCalendar();
        try {
            expiry.setTimeInMillis(sdfIn.parse(value).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        expiry.add(java.util.GregorianCalendar.DAY_OF_MONTH, 120);
        this.expiry = sdfOut.format(expiry.getTime());
        return this.expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public String getEffective() {
        SimpleDateFormat sdfIn = new SimpleDateFormat(this.sdfIn);
        SimpleDateFormat sdfOut = new SimpleDateFormat(this.sdfOut);
        GregorianCalendar loan = new GregorianCalendar();
        try {
            loan.setTimeInMillis(sdfIn.parse(value).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.effective = sdfOut.format(loan.getTime());
        return this.effective;
    }

    public void setEffective(String effective) {
        this.effective = effective;
    }

    public String getMaturity() {
        SimpleDateFormat sdfIn = new SimpleDateFormat(this.sdfIn);
        SimpleDateFormat sdfOut = new SimpleDateFormat(this.sdfOut);
        GregorianCalendar maturity = new GregorianCalendar();
        try {
            maturity.setTimeInMillis(sdfIn.parse(value).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        maturity.add(java.util.GregorianCalendar.DAY_OF_MONTH, 30);
        this.maturity = sdfOut.format(maturity.getTime());
        return this.maturity;
    }

    public void setMaturity(String maturity) {
        this.maturity = maturity;
    }

    public String getSdfIn() {
        return sdfIn;
    }

    public void setSdfIn(String sdfIn) {
        this.sdfIn = sdfIn;
    }

    public String getSdfOut() {
        return sdfOut;
    }

    public void setSdfOut(String sdfOut) {
        this.sdfOut = sdfOut;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
