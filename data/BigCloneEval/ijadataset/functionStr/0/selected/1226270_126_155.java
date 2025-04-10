public class Test {    public void testWriteAndRead_Delete() throws Exception {
        rs = st.executeQuery("SELECT * FROM USER_INFO");
        webRs = newWebRowSet();
        webRs.populate(rs);
        assertTrue(webRs.absolute(3));
        webRs.deleteRow();
        webRs.beforeFirst();
        StringWriter writer = new StringWriter();
        webRs.writeXml(writer);
        another = newWebRowSet();
        another.readXml(new StringReader(writer.getBuffer().toString()));
        webRs.setShowDeleted(true);
        another.setShowDeleted(true);
        assertCachedRowSetEquals(webRs, another);
        rs = st.executeQuery("SELECT * FROM USER_INFO");
        webRs = newWebRowSet();
        webRs.populate(rs);
        assertTrue(webRs.absolute(4));
        webRs.setShowDeleted(true);
        webRs.deleteRow();
        webRs.absolute(3);
        webRs.deleteRow();
        writer = new StringWriter();
        webRs.writeXml(writer);
        another = newWebRowSet();
        another.readXml(new StringReader(writer.getBuffer().toString()));
        webRs.setShowDeleted(true);
        another.setShowDeleted(true);
        assertCachedRowSetEquals(webRs, another);
    }
}