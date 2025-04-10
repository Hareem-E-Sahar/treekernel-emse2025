public class Test {    @Override
    public void update(SessionType obj) throws UpdateException, DBConnectionException, XmlIOException {
        Statement stmt = OracleJDBConnector.getInstance().getStatement();
        Criteria newCrit = new Criteria();
        newCrit.addCriterion("SESSION_TYPE_NAME", obj.getName());
        newCrit.addCriterion("SESSION_TYPE_EQV_TD", obj.getEquivTuto());
        newCrit.addCriterion("SESSION_TYPE_ACRONYM", obj.getAcronym());
        Criteria critWhere = new Criteria();
        critWhere.addCriterion("SESSION_TYPE_ID", obj.getId());
        try {
            stmt.executeUpdate(new UpdateQuery(TABLE_NAME, newCrit, critWhere).toString());
            stmt.getConnection().commit();
            stmt.close();
        } catch (SQLException e) {
            try {
                stmt.getConnection().rollback();
            } catch (SQLException e1) {
                throw new DBConnectionException(TABLE_NAME + " Rollback Exception :", e1);
            }
            throw new UpdateException(TABLE_NAME + " Update exception", e);
        }
    }
}