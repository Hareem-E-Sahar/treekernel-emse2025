package com.bocoon.app.cms.front.directive;

import static com.bocoon.app.cms.Constants.TPL_STYLE_LIST;
import static com.bocoon.app.cms.Constants.TPL_SUFFIX;
import static com.bocoon.app.cms.utils.FrontUtils.PARAM_STYLE_LIST;
import static com.bocoon.common.web.Constants.UTF8;
import static com.bocoon.common.web.freemarker.DirectiveUtils.OUT_LIST;
import static freemarker.template.ObjectWrapper.DEFAULT_WRAPPER;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.bocoon.app.cms.utils.FrontUtils;
import com.bocoon.common.web.freemarker.DirectiveUtils;
import com.bocoon.common.web.freemarker.ParamsRequiredException;
import com.bocoon.common.web.freemarker.DirectiveUtils.InvokeType;
import com.bocoon.entity.cms.main.CmsSite;
import com.bocoon.entity.cms.main.CmsTopic;
import com.jeecms.cms.manager.main.CmsTopicMng;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * 专题列表标签
 * 
 * @author liufang
 * 
 */
public class CmsTopicListDirective implements TemplateDirectiveModel {

    /**
	 * 模板名称
	 */
    public static final String TPL_NAME = "topic_list";

    /**
	 * 输入参数，栏目ID。
	 */
    public static final String PARAM_CHANNEL_ID = "channelId";

    /**
	 * 输入参数，是否推荐。
	 */
    public static final String PARAM_RECOMMEND = "recommend";

    @SuppressWarnings("unchecked")
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        CmsSite site = FrontUtils.getSite(env);
        List<CmsTopic> list = cmsTopicMng.getListForTag(getChannelId(params), getRecommend(params), FrontUtils.getCount(params));
        Map<String, TemplateModel> paramWrap = new HashMap<String, TemplateModel>(params);
        paramWrap.put(OUT_LIST, DEFAULT_WRAPPER.wrap(list));
        Map<String, TemplateModel> origMap = DirectiveUtils.addParamsToVariable(env, paramWrap);
        InvokeType type = DirectiveUtils.getInvokeType(params);
        String listStyle = DirectiveUtils.getString(PARAM_STYLE_LIST, params);
        if (InvokeType.sysDefined == type) {
            if (StringUtils.isBlank(listStyle)) {
                throw new ParamsRequiredException(PARAM_STYLE_LIST);
            }
            env.include(TPL_STYLE_LIST + listStyle + TPL_SUFFIX, UTF8, true);
        } else if (InvokeType.userDefined == type) {
            if (StringUtils.isBlank(listStyle)) {
                throw new ParamsRequiredException(PARAM_STYLE_LIST);
            }
            FrontUtils.includeTpl(TPL_STYLE_LIST, site, env);
        } else if (InvokeType.custom == type) {
            FrontUtils.includeTpl(TPL_NAME, site, params, env);
        } else if (InvokeType.body == type) {
            body.render(env.getOut());
        } else {
            throw new RuntimeException("invoke type not handled: " + type);
        }
        DirectiveUtils.removeParamsFromVariable(env, paramWrap, origMap);
    }

    private Integer getChannelId(Map<String, TemplateModel> params) throws TemplateException {
        return DirectiveUtils.getInt(PARAM_CHANNEL_ID, params);
    }

    private boolean getRecommend(Map<String, TemplateModel> params) throws TemplateException {
        Boolean recommend = DirectiveUtils.getBool(PARAM_RECOMMEND, params);
        return recommend != null ? recommend : false;
    }

    @Autowired
    private CmsTopicMng cmsTopicMng;
}
