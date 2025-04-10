public class Test {    public void testWriteAndRead_Update() throws Exception {
        rs = st.executeQuery("SELECT * FROM USER_INFO");
        webRs = newWebRowSet();
        webRs.populate(rs);
        assertTrue(webRs.absolute(3));
        webRs.updateString(2, "updateRow");
        webRs.updateRow();
        assertTrue(webRs.next());
        webRs.updateString(2, "anotherUpdateRow");
        webRs.updateRow();
        StringWriter writer = new StringWriter();
        webRs.writeXml(writer);
        another = newWebRowSet();
        another.readXml(new StringReader(writer.getBuffer().toString()));
        assertCachedRowSetEquals(webRs, another);
    }
}