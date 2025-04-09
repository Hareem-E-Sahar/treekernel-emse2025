package pkg.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author will
 */
public class DateUtil {

    public static Date convertToDateTZ(Date d, TimeZone fromTZ, TimeZone toTZ) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        sdf.setTimeZone(toTZ);
        String sd = sdf.format(d);
        sdf.setTimeZone(fromTZ);
        sd = sdf.format(d);
        sdf.setTimeZone(toTZ);
        try {
            d = sdf.parse(sd);
        } catch (ParseException ex) {
            Logger.getLogger(DateUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return d;
    }
}
