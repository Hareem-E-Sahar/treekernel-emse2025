public class Test {    public boolean deleteRewardlogByTid(int tid) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction tr = null;
        try {
            tr = session.beginTransaction();
            Query query = session.createQuery("delete from Rewardlog as r where r.id.tid=?");
            query.setParameter(0, tid);
            query.executeUpdate();
            tr.commit();
            return true;
        } catch (HibernateException er) {
            if (tr != null) {
                tr.rollback();
            }
            er.printStackTrace();
        }
        return false;
    }
}