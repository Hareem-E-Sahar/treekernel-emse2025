public class Test {    public void delete(I id) {
        try {
            C actual = get(id);
            sess().delete(actual);
        } catch (HibernateException e) {
            hTransaction.rollback();
            log.error(e.getMessage());
            log.error("Be sure your Database is in read-write mode!");
            throw e;
        }
    }
}