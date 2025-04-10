public class Test {    public int addPermissionsForUserAndTypeAndAgenda(Integer userId, Integer eventTypeId, Integer agendaId, String permissions) throws TechnicalException {
        if (permissions == null) {
            throw new TechnicalException(new Exception(new Exception("Column 'permissions' cannot be null")));
        }
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getCurrentSession();
            transaction = session.beginTransaction();
            String query = "INSERT INTO j_user_type_agenda (userId, agendaId, eventTypeId, permissions) VALUES(" + userId + "," + agendaId + "," + eventTypeId + ",\"" + permissions + "\")";
            Statement statement = session.connection().createStatement();
            int rowsUpdated = statement.executeUpdate(query);
            transaction.commit();
            return rowsUpdated;
        } catch (HibernateException ex) {
            if (transaction != null) transaction.rollback();
            throw new TechnicalException(ex);
        } catch (SQLException e) {
            if (transaction != null) transaction.rollback();
            throw new TechnicalException(e);
        }
    }
}