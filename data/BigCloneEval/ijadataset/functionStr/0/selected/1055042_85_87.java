public class Test {    private Integer getChannelId(Map<String, TemplateModel> params) throws TemplateException {
        return DirectiveUtils.getInt(PARAM_CHANNEL_ID, params);
    }
}