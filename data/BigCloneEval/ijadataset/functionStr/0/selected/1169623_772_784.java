public class Test {    public static String getTomorrow(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date strtodate = formatter.parse(strDate);
            long tomorrowLong = strtodate.getTime() + 24 * 60 * 60 * 1000;
            System.err.println(tomorrowLong);
            Date tomorrow_date = new Date(tomorrowLong);
            return formatter.format(tomorrow_date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}