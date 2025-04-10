public class Test {    public static void updateItem(ItemIF item) throws Exception {
        StringBuffer sql = new StringBuffer();
        sql.append("UPDATE ITEMS SET ");
        sql.append("CHANNEL_ID = ?, ");
        sql.append("TITLE = ?, ");
        sql.append("DESCRIPTION = ?, ");
        sql.append("UNREAD = ?, ");
        sql.append("LINK = ?, ");
        sql.append("CREATOR = ?, ");
        sql.append("SUBJECT = ?, ");
        sql.append("DATE = ?, ");
        sql.append("FOUND = ?, ");
        sql.append("GUID = ?, ");
        sql.append("COMMENTS = ?, ");
        sql.append("SOURCE = ?, ");
        sql.append("ENCLOSURE = ? ");
        sql.append("WHERE ITEM_ID = ? ");
        Connection con = Database.getInstance().getConnection();
        PreparedStatement stmt = con.prepareStatement(sql.toString());
        stmt.setLong(1, item.getChannel().getId());
        stmt.setString(2, Utils.stripToSafeDatabaseString(item.getTitle()));
        stmt.setString(3, Utils.stripToSafeDatabaseString(item.getDescription()));
        stmt.setInt(4, item.getUnRead() ? 1 : 0);
        stmt.setBytes(5, item.getLink() == null ? null : Utils.serialize(item.getLink()));
        stmt.setString(6, Utils.stripToSafeDatabaseString(item.getCreator()));
        stmt.setString(7, Utils.stripToSafeDatabaseString(item.getSubject()));
        stmt.setDate(8, item.getDate() == null ? null : new Date(item.getDate().getTime()));
        stmt.setDate(9, item.getFound() == null ? null : new Date(item.getFound().getTime()));
        stmt.setObject(10, null);
        stmt.setBytes(11, item.getComments() == null ? null : Utils.serialize(item.getComments()));
        stmt.setObject(12, null);
        stmt.setObject(13, null);
        stmt.setLong(14, item.getId());
        stmt.executeUpdate();
        stmt.close();
    }
}