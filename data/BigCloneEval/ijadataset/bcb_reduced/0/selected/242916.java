package com.xmultra.processor.xformer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateFormatterXSLTExtension {

    public static String parseDuration(String durationString) {
        String parsedDuration = "";
        int length = durationString.length();
        int indexOfM = 0;
        for (int i = 0; i < length; i++) {
            if (durationString.charAt(i) == 'M' || durationString.charAt(i) == 'm') {
                indexOfM = i;
            }
        }
        if (indexOfM + 1 != length - 1) {
            parsedDuration = durationString.substring(indexOfM + 1, length - 1);
            return parsedDuration;
        } else return "Error occurred. No duration value in input";
    }

    public static String getDates(String startDate, int i, String inputFormat, String outputFormat) throws ParseException {
        SimpleDateFormat inputFormatter = new SimpleDateFormat(inputFormat);
        SimpleDateFormat outputFormatter = new SimpleDateFormat(outputFormat);
        Date inputDate = inputFormatter.parse(startDate);
        if (i == 1) {
            return outputFormatter.format(inputDate);
        } else {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(inputDate);
            calendar.add(Calendar.DATE, i - 1);
            return outputFormatter.format(calendar.getTime());
        }
    }
}
