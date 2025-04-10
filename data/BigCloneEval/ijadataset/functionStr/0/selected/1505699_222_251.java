public class Test {    private void addFormTemplates(Connection connection) throws Exception {
        String sql = "insert into J_FORM_TEMPLATES values (?,?,?)";
        PreparedStatement pstmnt = connection.prepareStatement(sql);
        pstmnt.setLong(1, 1000000);
        URL u = URLHelper.newExtendedURL(PDF_TEMPLATE);
        InputStream is = u.openStream();
        pstmnt.setBinaryStream(2, is, is.available());
        u = URLHelper.newExtendedURL(PDF_TEMPLATE + ".csv");
        is = u.openStream();
        pstmnt.setBinaryStream(3, is, is.available());
        pstmnt.execute();
        pstmnt.clearParameters();
        pstmnt.setLong(1, 1000001);
        u = URLHelper.newExtendedURL(VELOCITY_TEMPLATE);
        is = u.openStream();
        pstmnt.setBinaryStream(2, is, is.available());
        pstmnt.setObject(3, null);
        pstmnt.execute();
        pstmnt.clearParameters();
        pstmnt.setLong(1, 1000002);
        u = URLHelper.newExtendedURL(PDF_TEMPLATE3);
        is = u.openStream();
        pstmnt.setBinaryStream(2, is, is.available());
        u = URLHelper.newExtendedURL(PDF_TEMPLATE3 + ".csv");
        is = u.openStream();
        pstmnt.setBinaryStream(3, is, is.available());
        pstmnt.execute();
        pstmnt.clearParameters();
        pstmnt.close();
    }
}