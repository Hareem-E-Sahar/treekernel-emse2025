public class Test {    @Override
    public LoginInfo retrieve() {
        String requestUri = getRequest().getResourceRef().getQueryAsForm().getValues(REQUEST_URI_PARAM_NAME);
        LoginInfo info;
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        if (user != null) {
            PreferredChannels preferredChannels = getPreferredChannels(user);
            Set<String> channels;
            if (preferredChannels != null) {
                channels = new HashSet<String>(preferredChannels.getChannels());
            } else {
                channels = channelService.getDefaultSelectedChannels();
            }
            info = new LoginInfo(user.getNickname(), userService.createLogoutURL(requestUri), "Esci", channels);
        } else {
            info = new LoginInfo(null, userService.createLoginURL(requestUri), "Entra", channelService.getDefaultSelectedChannels());
        }
        return info;
    }
}