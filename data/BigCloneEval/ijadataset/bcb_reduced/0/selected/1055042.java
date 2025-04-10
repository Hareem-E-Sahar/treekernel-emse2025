package com.jeecms.cms.action.directive;

import static com.jeecms.cms.Constants.TPL_STYLE_LIST;
import static com.jeecms.cms.Constants.TPL_SUFFIX;
import static com.jeecms.cms.web.FrontUtils.PARAM_STYLE_LIST;
import static com.jeecms.common.web.Constants.UTF8;
import static com.jeecms.common.web.freemarker.DirectiveUtils.OUT_LIST;
import static com.jeecms.common.web.freemarker.DirectiveUtils.OUT_PAGINATION;
import static freemarker.template.ObjectWrapper.DEFAULT_WRAPPER;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.jeecms.cms.entity.main.CmsSite;
import com.jeecms.cms.manager.main.CmsTopicMng;
import com.jeecms.cms.web.FrontUtils;
import com.jeecms.common.page.Pagination;
import com.jeecms.common.web.freemarker.DirectiveUtils;
import com.jeecms.common.web.freemarker.ParamsRequiredException;
import com.jeecms.common.web.freemarker.DirectiveUtils.InvokeType;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * 专题分页标签
 * 
 * @author liufang
 * 
 */
public class CmsTopicPageDirective implements TemplateDirectiveModel {

    /**
	 * 模板名称
	 */
    public static final String TPL_NAME = "topic_page";

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
        Pagination page = cmsTopicMng.getPageForTag(getChannelId(params), getRecommend(params), FrontUtils.getPageNo(env), FrontUtils.getCount(params));
        Map<String, TemplateModel> paramWrap = new HashMap<String, TemplateModel>(params);
        paramWrap.put(OUT_PAGINATION, DEFAULT_WRAPPER.wrap(page));
        paramWrap.put(OUT_LIST, DEFAULT_WRAPPER.wrap(page.getList()));
        Map<String, TemplateModel> origMap = DirectiveUtils.addParamsToVariable(env, paramWrap);
        InvokeType type = DirectiveUtils.getInvokeType(params);
        String listStyle = DirectiveUtils.getString(PARAM_STYLE_LIST, params);
        if (InvokeType.sysDefined == type) {
            if (StringUtils.isBlank(listStyle)) {
                throw new ParamsRequiredException(PARAM_STYLE_LIST);
            }
            env.include(TPL_STYLE_LIST + listStyle + TPL_SUFFIX, UTF8, true);
            FrontUtils.includePagination(site, params, env);
        } else if (InvokeType.userDefined == type) {
            if (StringUtils.isBlank(listStyle)) {
                throw new ParamsRequiredException(PARAM_STYLE_LIST);
            }
            FrontUtils.includeTpl(TPL_STYLE_LIST, site, env);
            FrontUtils.includePagination(site, params, env);
        } else if (InvokeType.custom == type) {
            FrontUtils.includeTpl(TPL_NAME, site, params, env);
            FrontUtils.includePagination(site, params, env);
        } else if (InvokeType.body == type) {
            body.render(env.getOut());
            FrontUtils.includePagination(site, params, env);
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
