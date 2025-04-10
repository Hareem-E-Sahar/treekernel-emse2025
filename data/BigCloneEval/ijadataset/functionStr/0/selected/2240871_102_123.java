public class Test {    public int updateTagArray(List<Bbcodes> list) {
        int num = -1;
        Transaction tr = null;
        try {
            Session session = HibernateUtil.getSessionFactory().getCurrentSession();
            tr = session.beginTransaction();
            Query query = session.createQuery("update Bbcodes as b set b.tag = :tag where b.id = :id");
            for (int i = 0; i < list.size(); i++) {
                query.setString("tag", list.get(i).getTag());
                query.setInteger("id", list.get(i).getId());
                num += query.executeUpdate();
            }
            session.flush();
            tr.commit();
        } catch (HibernateException he) {
            if (tr != null) tr.rollback();
            tr = null;
            he.printStackTrace();
            num = -1;
        }
        return num;
    }
}