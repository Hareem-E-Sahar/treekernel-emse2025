package com.jeecms.cms.action.admin.main;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import com.jeecms.cms.entity.main.Channel;
import com.jeecms.cms.entity.main.CmsUser;
import com.jeecms.cms.manager.main.ChannelMng;
import com.jeecms.cms.manager.main.CmsGroupMng;
import com.jeecms.cms.manager.main.CmsLogMng;
import com.jeecms.cms.manager.main.CmsRoleMng;
import com.jeecms.cms.manager.main.CmsSiteMng;
import com.jeecms.cms.manager.main.CmsUserMng;
import com.jeecms.common.web.ResponseUtils;

public class CmsAdminAbstract {

    protected String channelsAddJson(Integer siteId, HttpServletRequest request, HttpServletResponse response, ModelMap model) {
        List<Channel> channelList = channelMng.getTopList(siteId, false);
        model.addAttribute("channelList", channelList);
        response.setHeader("Cache-Control", "no-cache");
        response.setContentType("text/json;charset=UTF-8");
        return "admin/channels_add";
    }

    protected String channelsEditJson(Integer userId, Integer siteId, HttpServletRequest request, HttpServletResponse response, ModelMap model) {
        List<Channel> channelList = channelMng.getTopList(siteId, false);
        CmsUser user = manager.findById(userId);
        model.addAttribute("channelList", channelList);
        model.addAttribute("channelIds", user.getChannelIds(siteId));
        response.setHeader("Cache-Control", "no-cache");
        response.setContentType("text/json;charset=UTF-8");
        return "admin/channels_edit";
    }

    protected void checkUserJson(String username, HttpServletResponse response) {
        String pass;
        if (StringUtils.isBlank(username)) {
            pass = "false";
        } else {
            pass = manager.usernameNotExist(username) ? "true" : "false";
        }
        ResponseUtils.renderJson(response, pass);
    }

    protected void checkEmailJson(String email, HttpServletResponse response) {
        String pass;
        if (StringUtils.isBlank(email)) {
            pass = "false";
        } else {
            pass = manager.emailNotExist(email) ? "true" : "false";
        }
        ResponseUtils.renderJson(response, pass);
    }

    @Autowired
    protected CmsSiteMng cmsSiteMng;

    @Autowired
    protected ChannelMng channelMng;

    @Autowired
    protected CmsRoleMng cmsRoleMng;

    @Autowired
    protected CmsGroupMng cmsGroupMng;

    @Autowired
    protected CmsLogMng cmsLogMng;

    @Autowired
    protected CmsUserMng manager;
}
