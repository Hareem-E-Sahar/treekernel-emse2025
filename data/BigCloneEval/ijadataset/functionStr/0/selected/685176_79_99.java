public class Test {    public RestServiceResult update(RestServiceResult serviceResult, List listWordsComleteE2, Long nCompleteId) {
        try {
            EntityManagerHelper.beginTransaction();
            Query query = EntityManagerHelper.createNativeQuery(Statements.DELETE_COMPLETE2_WORD);
            query.setParameter(1, new Long(nCompleteId));
            query.executeUpdate();
            EntityManagerHelper.commit();
            if (listWordsComleteE2 != null) {
                for (int i = 0; i < listWordsComleteE2.size(); i++) {
                    CoWordsCompleteE2 coWordsCompleteE2 = (CoWordsCompleteE2) listWordsComleteE2.get(i);
                    serviceResult = this.create(serviceResult, coWordsCompleteE2);
                }
            }
        } catch (PersistenceException e) {
            EntityManagerHelper.rollback();
            log.error("Error al actualizar las palabras: " + e.getMessage());
            serviceResult.setError(true);
            serviceResult.setMessage(MessageFormat.format(bundle.getString("multipleChoice.create.error"), e.getMessage()));
        }
        return serviceResult;
    }
}