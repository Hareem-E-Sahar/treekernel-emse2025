public class Test {    @Override
    public void delete(SubjectModel obj) throws DeleteException, DBConnectionException, XmlIOException {
        Statement stmt = OracleJDBConnector.getInstance().getStatement();
        Criteria critDel = new Criteria();
        critDel.addCriterion("SUBJECT_MODEL_ID", obj.getId());
        try {
            stmt.executeUpdate(new DeleteQuery(TABLE_NAME, critDel).toString());
            stmt.getConnection().commit();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                stmt.getConnection().rollback();
            } catch (SQLException e1) {
                throw new DBConnectionException(TABLE_NAME + " Rollback Exception :", e1);
            }
            throw new DeleteException(TABLE_NAME + " Deletion exception :", e);
        }
    }
}