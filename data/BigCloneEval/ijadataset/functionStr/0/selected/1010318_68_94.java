public class Test {    public void addOrderAuthorization(Comboitem comboItem, boolean readAuthorization, boolean writeAuthorization) {
        if (comboItem != null) {
            if (!readAuthorization && !writeAuthorization) {
                messagesForUser.showMessage(Level.WARNING, _("No authorizations were added because you did not select any."));
                return;
            }
            List<OrderAuthorizationType> authorizations = new ArrayList<OrderAuthorizationType>();
            if (readAuthorization) {
                authorizations.add(OrderAuthorizationType.READ_AUTHORIZATION);
            }
            if (writeAuthorization) {
                authorizations.add(OrderAuthorizationType.WRITE_AUTHORIZATION);
            }
            if (comboItem.getValue() instanceof User) {
                List<OrderAuthorizationType> result = orderAuthorizationModel.addUserOrderAuthorization((User) comboItem.getValue(), authorizations);
                if (result != null && result.size() == authorizations.size()) {
                    messagesForUser.showMessage(Level.WARNING, _("Could not add those authorizations to user {0} " + "because they were already present.", ((User) comboItem.getValue()).getLoginName()));
                }
            } else if (comboItem.getValue() instanceof Profile) {
                List<OrderAuthorizationType> result = orderAuthorizationModel.addProfileOrderAuthorization((Profile) comboItem.getValue(), authorizations);
                if (result != null && result.size() == authorizations.size()) {
                    messagesForUser.showMessage(Level.WARNING, _("Could not add those authorizations to profile {0} " + "because they were already present.", ((Profile) comboItem.getValue()).getProfileName()));
                }
            }
        }
        Util.reloadBindings(window);
    }
}