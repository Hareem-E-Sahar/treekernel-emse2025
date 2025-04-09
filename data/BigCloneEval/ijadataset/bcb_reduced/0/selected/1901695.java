package com.javaeye.lonlysky.lforum.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.struts2.config.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import com.javaeye.lonlysky.lforum.ForumBaseAction;
import com.javaeye.lonlysky.lforum.comm.ForumAction;
import com.javaeye.lonlysky.lforum.comm.LForumRequest;
import com.javaeye.lonlysky.lforum.comm.MD5;
import com.javaeye.lonlysky.lforum.comm.utils.ForumUtils;
import com.javaeye.lonlysky.lforum.comm.utils.Utils;
import com.javaeye.lonlysky.lforum.entity.forum.Admingroups;
import com.javaeye.lonlysky.lforum.entity.forum.Bonuslog;
import com.javaeye.lonlysky.lforum.entity.forum.Forums;
import com.javaeye.lonlysky.lforum.entity.forum.Pms;
import com.javaeye.lonlysky.lforum.entity.forum.PollOptionView;
import com.javaeye.lonlysky.lforum.entity.forum.Polls;
import com.javaeye.lonlysky.lforum.entity.forum.PostpramsInfo;
import com.javaeye.lonlysky.lforum.entity.forum.Posts;
import com.javaeye.lonlysky.lforum.entity.forum.ShowtopicPageAttachmentInfo;
import com.javaeye.lonlysky.lforum.entity.forum.ShowtopicPagePostInfo;
import com.javaeye.lonlysky.lforum.entity.forum.Smilies;
import com.javaeye.lonlysky.lforum.entity.forum.Topicidentify;
import com.javaeye.lonlysky.lforum.entity.forum.Topics;
import com.javaeye.lonlysky.lforum.entity.forum.UserExtcreditsInfo;
import com.javaeye.lonlysky.lforum.entity.forum.Users;
import com.javaeye.lonlysky.lforum.service.AdminGroupManager;
import com.javaeye.lonlysky.lforum.service.BonuManager;
import com.javaeye.lonlysky.lforum.service.CachesManager;
import com.javaeye.lonlysky.lforum.service.EditorManager;
import com.javaeye.lonlysky.lforum.service.ForumManager;
import com.javaeye.lonlysky.lforum.service.MessageManager;
import com.javaeye.lonlysky.lforum.service.ModeratorManager;
import com.javaeye.lonlysky.lforum.service.PaymentLogManager;
import com.javaeye.lonlysky.lforum.service.PollManager;
import com.javaeye.lonlysky.lforum.service.PostManager;
import com.javaeye.lonlysky.lforum.service.ScoresetManager;
import com.javaeye.lonlysky.lforum.service.SmilieManager;
import com.javaeye.lonlysky.lforum.service.TopicAdminManager;
import com.javaeye.lonlysky.lforum.service.TopicManager;
import com.javaeye.lonlysky.lforum.service.TopicStatManager;

/**
 * 树形显示帖子
 * 
 * @author 黄磊
 *
 */
@ParentPackage("default")
public class ShowtreeAction extends ForumBaseAction {

    private static final long serialVersionUID = -6130285898489889255L;

    /**
	 * 主题信息
	 */
    private Topics topic;

    /**
	 * 主题所属帖子
	 */
    private ShowtopicPagePostInfo post;

    /**
	 * 投票选项列表
	 */
    private List<PollOptionView> polloptionlist;

    /**
	 * 投票帖类型
	 */
    private Polls pollinfo;

    /**
	 * 是否显示投票结果
	 */
    private boolean showpollresult = true;

    /**
	 * 附件列表
	 */
    private List<ShowtopicPageAttachmentInfo> attachmentlist = new ArrayList<ShowtopicPageAttachmentInfo>();

    /**
	 * 短消息列表
	 */
    private List<Pms> pmlist = new ArrayList<Pms>();

    /**
	 * 悬赏给分日志
	 */
    private List<Bonuslog> bonuslogs;

    /**
	 * 主题回复列表
	 */
    private List<Object[]> posttree;

    /**
	 * 页内文字广告
	 */
    private String[] pagewordad;

    /**
	 * 对联广告
	 */
    private String doublead;

    /**
	 * 浮动广告
	 */
    private String floatad;

    /**
	 * 快速编辑器背景广告
	 */
    private String quickbgadimg = "";

    private String quickbgadlink = "";

    /**
	 * 版块Id
	 */
    private int forumid;

    /**
	 * 版块名称
	 */
    private String forumname;

    /**
	 * 论坛导航信息
	 */
    private String forumnav;

    /**
	 * 主题Id
	 */
    private int topicid;

    /**
	 * 帖子Id
	 */
    private int postid;

    /**
	 * 是否是投票帖
	 */
    private boolean ispoll;

    /**
	 * 是否允许投票
	 */
    private boolean allowvote;

    /**
	 * 主题标题
	 */
    private String topictitle;

    /**
	 * 回复标题
	 */
    private String replytitle;

    /**
	 * 主题浏览量
	 */
    private int topicviews;

    /**
	 * 主题帖子数
	 */
    private int postcount;

    /**
	 * 表情Javascript数组
	 */
    private String smilies;

    /**
	 * 参与投票的用户列表
	 */
    private String voters;

    /**
	 * 当前用户是否是管理者
	 */
    private int ismoder;

    /**
	 * 是否显示评分记录
	 */
    private int showratelog;

    /**
	 * 是否解析URL
	 */
    private int parseurloff;

    /**
	 * 是否解析表情
	 */
    private int smileyoff;

    /**
	 * 是否解析 LForum 代码
	 */
    private int bbcodeoff;

    /**
	 * 是否使用签名
	 */
    private int usesig;

    /**
	 * 可用的扩展积分名称
	 */
    private String[] score;

    /**
	 * 可用的扩展积分单位
	 */
    private String[] scoreunit;

    /**
	 * 
	 */
    private String ignorelink;

    /**
	 * 主题鉴定信息
	 */
    private int quickpost;

    /**
	 * 是否受发贴灌水限制
	 */
    private int disablepostctrl;

    /**
	 * 主题鉴定信息
	 */
    private Topicidentify topicidentify;

    /**
	 * 当前用户所属的管理组信息
	 */
    private Admingroups admininfo;

    /**
	 * 当前版块信息
	 */
    private Forums forum;

    /**
	 * 是否允许下载附件
	 */
    private List<Smilies> smilietypes = new ArrayList<Smilies>();

    /**
	 * 是否显示回复链接
	 */
    private boolean isshowreplaylink = false;

    /**
	 * 是否显示下载链接
	 */
    private boolean allowdownloadattach = false;

    /**
	 * 是否有发表主题的权限
	 */
    private boolean canposttopic = false;

    /**
	 * 是否有回复的权限
	 */
    private boolean canreply = false;

    /**
	 * 论坛弹出导航菜单HTML代码
	 */
    private String navhomemenu = "";

    /**
	 * 是否显示短消息提示
	 */
    private boolean showpmhint = false;

    /**
	 * 帖内广告
	 */
    private String inpostad = "";

    /**
	 * 快速发帖广告
	 */
    private String quickeditorad = "";

    /**
	 * 主题分类列表
	 */
    private String topictypes = "";

    /**
	 * 积分策略信息
	 */
    private UserExtcreditsInfo userextcreditsinfo;

    /**
	 * 是否显示需要登录后访问的错误提示
	 */
    private boolean needlogin = false;

    /**
	 * 第一页表情的JSON
	 */
    private String firstpagesmilies = "";

    /**
	 * 本版是否启用Tag
	 */
    private boolean enabletag = false;

    @Autowired
    private CachesManager cachesManager;

    @Autowired
    private TopicManager topicManager;

    @Autowired
    private ForumManager forumManager;

    @Autowired
    private ModeratorManager moderatorManager;

    @Autowired
    private AdminGroupManager adminGroupManager;

    @Autowired
    private ScoresetManager scoresetManager;

    @Autowired
    private PaymentLogManager paymentLogManager;

    @Autowired
    private BonuManager bonuManager;

    @Autowired
    private PollManager pollManager;

    @Autowired
    private PostManager postManager;

    @Autowired
    private SmilieManager smilieManager;

    @Autowired
    private EditorManager editorManager;

    @Autowired
    private MessageManager messageManager;

    @Autowired
    private TopicStatManager topicStatManager;

    @Autowired
    private TopicAdminManager topicAdminManager;

    @Override
    public String execute() throws Exception {
        ismoder = 0;
        disablepostctrl = 0;
        quickpost = 1;
        forumnav = "";
        topictitle = "";
        ignorelink = "";
        forumid = 0;
        firstpagesmilies = cachesManager.getSmiliesFirstPageCache();
        if (useradminid != 1 && usergroupinfo.getDisableperiodctrl() != 1) {
            String visittime = scoresetManager.betweenTime(config.getPostbanperiods());
            if (!visittime.equals("")) {
                quickpost = 0;
            }
        }
        headerad = "";
        footerad = "";
        pagewordad = new String[0];
        doublead = "";
        floatad = "";
        inpostad = "";
        quickeditorad = "";
        String[] quickbgad = new String[0];
        if (quickbgad.length > 1) {
            quickbgadimg = quickbgad[0];
            quickbgadlink = quickbgad[1];
        }
        topicid = LForumRequest.getParamIntValue("topicid", -1);
        postid = LForumRequest.getParamIntValue("postid", -1);
        if (topicid != -1) {
            postid = LForumRequest.getParamIntValue("postid", -1);
            if (postid == -1) {
                postid = postManager.getFirstPostId(topicid);
            }
        }
        Posts postinfo = postManager.getPostInfo(postid);
        if (postinfo == null) {
            reqcfg.addErrLine("错误的主题");
            return SUCCESS;
        } else {
            if (topicid == -1) {
                topicid = postinfo.getTopics().getTid();
            }
        }
        if (topicid != postinfo.getTopics().getTid()) {
            reqcfg.addErrLine("主题ID无效");
            return SUCCESS;
        }
        smilies = cachesManager.getSmiliesCache();
        smilietypes = cachesManager.getSmilieTypesCache();
        parseurloff = 0;
        usesig = config.getShowsignatures();
        topic = topicManager.getTopicInfo(topicid);
        if (topic == null) {
            reqcfg.addErrLine("不存在的主题ID");
            return SUCCESS;
        }
        if (topic.getDisplayorder() == -1) {
            reqcfg.addErrLine("此主题已被删除！");
            return SUCCESS;
        }
        if (topic.getDisplayorder() == -2) {
            reqcfg.addErrLine("此主题未经审核！");
            return SUCCESS;
        }
        if (topic.getIdentify() > 0) {
            topicidentify = cachesManager.getTopicIdentify(topic.getIdentify());
            if (topic.getIdentify() != topicidentify.getIdentifyid()) {
                topicAdminManager.identifyTopic(topicid + "", 0);
            }
        }
        forumid = topic.getForums().getFid();
        ismoder = moderatorManager.isModer(useradminid, userid, forumid) ? 1 : 0;
        admininfo = adminGroupManager.getAdminGroup(useradminid);
        if (admininfo != null) {
            disablepostctrl = admininfo.getDisablepostctrl();
        }
        if (topic.getReadperm() > usergroupinfo.getReadaccess() && topic.getUsersByPosterid().getUid() != userid && useradminid != 1 && ismoder == 0) {
            reqcfg.addErrLine("本主题阅读权限为: " + topic.getReadperm() + ", 您当前的身份 \"" + usergroupinfo.getGrouptitle() + "\" 阅读权限不够");
            if (userid == -1) {
                needlogin = true;
            }
            return SUCCESS;
        }
        topictitle = topic.getTitle().trim();
        replytitle = topictitle;
        if (replytitle.length() >= 50) {
            replytitle = Utils.format(replytitle, 50, true);
        }
        topicviews = topic.getViews() + 1;
        forum = forumManager.getForumInfo(forumid);
        enabletag = (config.getEnabletag() & forum.getAllowtag()) == 1;
        forumname = forum.getName();
        pagetitle = topic.getTitle();
        forumnav = forum.getPathlist().trim();
        navhomemenu = cachesManager.getForumListMenuDiv(usergroupid, userid, "");
        smileyoff = 1 - forum.getAllowsmilies();
        bbcodeoff = 1;
        if (forum.getAllowbbcode() == 1) {
            if (usergroupinfo.getAllowcusbbcode() == 1) {
                bbcodeoff = 0;
            }
        }
        if (!forum.getForumfields().getPassword().equals("") && !MD5.encode(forum.getForumfields().getPassword()).equals(ForumUtils.getCookie("forum" + forumid + "password"))) {
            reqcfg.addErrLine("本版块被管理员设置了密码");
            response.sendRedirect("showforum.action?forumid=" + forumid);
            return null;
        }
        if (!forumManager.allowViewByUserID(forum.getForumfields().getPermuserlist(), userid)) {
            if (Utils.null2String(forum.getForumfields().getViewperm()).equals("")) {
                if (usergroupinfo.getAllowvisit() != 1) {
                    reqcfg.addErrLine("您当前的身份 \"" + usergroupinfo.getGrouptitle() + "\" 没有浏览该版块的权限");
                    if (userid == -1) {
                        needlogin = true;
                    }
                    return SUCCESS;
                }
            } else {
                if (!forumManager.allowView(forum.getForumfields().getViewperm(), usergroupid)) {
                    reqcfg.addErrLine("您没有浏览该版块的权限");
                    if (userid == -1) {
                        needlogin = true;
                    }
                    return SUCCESS;
                }
            }
        }
        if (forumManager.allowReplyByUserID(forum.getForumfields().getPermuserlist().trim(), userid)) {
            isshowreplaylink = true;
        } else {
            if (forum.getForumfields().getReplyperm().trim().equals("")) {
                if (usergroupinfo.getAllowreply() == 1) {
                    isshowreplaylink = true;
                }
            } else if (forumManager.allowReply(forum.getForumfields().getReplyperm().trim(), usergroupid)) {
                isshowreplaylink = true;
            }
        }
        if (forumManager.allowGetAttachByUserID(forum.getForumfields().getPermuserlist(), userid)) {
            allowdownloadattach = true;
        } else {
            if (forum.getForumfields().getGetattachperm().trim().equals("")) {
                if (usergroupinfo.getAllowgetattach() == 1) {
                    allowdownloadattach = true;
                }
            } else if (forumManager.allowGetAttach(forum.getForumfields().getGetattachperm().trim(), usergroupid)) {
                allowdownloadattach = true;
            }
        }
        topictypes = Utils.null2String(cachesManager.getTopicTypeArray().get(topic.getTopictypes().getTypeid()));
        topictypes = topictypes != "" ? "[" + topictypes + "]" : "";
        userextcreditsinfo = scoresetManager.getScoreSet(scoresetManager.getCreditsTrans());
        if (quickpost == 1) {
            if (userid > -1 && forumManager.allowPostByUserID(forum.getForumfields().getPermuserlist(), userid)) {
                canposttopic = true;
            }
            if (forum.getForumfields().getPostperm().trim().equals("")) {
                if (usergroupinfo.getAllowpost() == 1) {
                    canposttopic = true;
                }
            } else if (forumManager.allowPost(forum.getForumfields().getPostperm().trim(), usergroupid)) {
                canposttopic = true;
            }
        }
        if (topic.getClosed() == 0 || ismoder == 1) {
            if (config.getFastpost() == 2 || config.getFastpost() == 3) {
                if (forumManager.allowReplyByUserID(forum.getForumfields().getPermuserlist(), userid)) {
                    canreply = true;
                } else {
                    if (forum.getForumfields().getReplyperm().trim().equals("")) {
                        if (usergroupinfo.getAllowreply() == 1) {
                            canreply = true;
                        }
                    } else if (forumManager.allowReply(forum.getForumfields().getReplyperm(), usergroupid)) {
                        canreply = true;
                    }
                }
            }
        }
        showratelog = config.getDisplayratecount() > 0 ? 1 : 0;
        int price = 0;
        if (topic.getSpecial() == 0) {
            if (topic.getPrice() > 0 && userid != topic.getUsersByPosterid().getUid() && ismoder != 1) {
                price = topic.getPrice();
                if (paymentLogManager.isBuyer(topicid, userid) || (Utils.strDateDiffHours(topic.getPostdatetime(), scoresetManager.getMaxChargeSpan()) > 0 && scoresetManager.getMaxChargeSpan() != 0)) {
                    price = -1;
                }
            }
            if (price > 0) {
                response.sendRedirect("buytopic.action?topicid=" + topic.getTid());
                return null;
            }
        }
        if (newpmcount > 0) {
            pmlist = messageManager.getPrivateMessageListForIndex(userid, 5, 1, 1);
            showpmhint = userManager.getUserInfo(userid).getNewsletter() > 4;
        }
        ispoll = false;
        allowvote = false;
        Map<Integer, Boolean> tmp = new HashMap<Integer, Boolean>();
        if (topic.getSpecial() == 1) {
            ispoll = true;
            polloptionlist = pollManager.getPollOptionList(topicid);
            pollinfo = pollManager.getPollInfo(topicid);
            voters = pollManager.getVoters(topicid, userid, username, tmp);
            allowvote = tmp.get(0);
            if (pollinfo.getUsers().getUid() != userid && useradminid != 1) {
                if (pollinfo.getVisible() == 1 && (allowvote || (userid == -1 && !Utils.inArray(topicid + "", ForumUtils.getCookie("polled"))))) {
                    showpollresult = false;
                }
            }
        }
        if (topic.getSpecial() == 3) {
            bonuslogs = bonuManager.getLogs(topic.getTid());
        }
        SimpleDateFormat sdf = new SimpleDateFormat(Utils.FULL_DATEFORMAT);
        if (ispoll && sdf.parse(pollinfo.getExpiration()).before(Utils.getNowDate())) {
            allowvote = false;
        }
        postcount = topic.getReplies() + 1;
        int hide = 1;
        if (topic.getHide() == 1 && (postManager.isReplier(topicid, userid) || ismoder == 1)) {
            hide = -1;
        }
        PostpramsInfo postpramsInfo = new PostpramsInfo();
        postpramsInfo.setPid(postid);
        postpramsInfo.setFid(forum.getFid());
        postpramsInfo.setTid(topicid);
        postpramsInfo.setJammer(forum.getJammer());
        postpramsInfo.setPagesize(1);
        postpramsInfo.setPageindex(1);
        postpramsInfo.setGetattachperm(forum.getForumfields().getGetattachperm());
        postpramsInfo.setUsergroupid(usergroupid);
        postpramsInfo.setAttachimgpost(config.getAttachimgpost());
        postpramsInfo.setShowattachmentpath(config.getShowattachmentpath());
        postpramsInfo.setHide(hide);
        postpramsInfo.setPrice(price);
        postpramsInfo.setUbbmode(false);
        postpramsInfo.setUsergroupreadaccess(usergroupinfo.getReadaccess());
        if (ismoder == 1) {
            postpramsInfo.setUsergroupreadaccess(Integer.MAX_VALUE);
        }
        postpramsInfo.setCurrentuserid(userid);
        postpramsInfo.setShowimages(forum.getAllowimgcode());
        postpramsInfo.setSmiliesinfo(smilieManager.getSmiliesListWithInfo());
        postpramsInfo.setCustomeditorbuttoninfo(editorManager.getCustomEditButtonListWithInfo());
        postpramsInfo.setSmiliesmax(config.getSmiliesmax());
        postpramsInfo.setBbcodemode(0);
        postpramsInfo.setCurrentusergroup(usergroupinfo);
        postpramsInfo.setCondition("");
        Map<Integer, List<ShowtopicPageAttachmentInfo>> map = new HashMap<Integer, List<ShowtopicPageAttachmentInfo>>();
        map.put(0, attachmentlist);
        post = postManager.getSinglePost(postpramsInfo, map, ismoder == 1);
        attachmentlist = map.get(0);
        if (post == null) {
            reqcfg.addErrLine("读取信息失败");
            return SUCCESS;
        }
        posttree = postManager.getPostTree(topicid);
        topicStatManager.track(topicid, 1);
        onlineUserManager.updateAction(olid, ForumAction.ShowTopic.ACTION_ID, forumid, forumname, topicid, topictitle, config.getOnlinetimeout());
        score = scoresetManager.getValidScoreName();
        scoreunit = scoresetManager.getValidScoreUnit();
        if (userid != -1) {
            Users userinfo = userManager.getUserInfo(userid);
            if (userinfo != null) {
                if (userinfo.getNewpm() == 0) {
                    newpmcount = 0;
                }
            }
        }
        return SUCCESS;
    }

    public Topics getTopic() {
        return topic;
    }

    public ShowtopicPagePostInfo getPost() {
        return post;
    }

    public List<PollOptionView> getPolloptionlist() {
        return polloptionlist;
    }

    public Polls getPollinfo() {
        return pollinfo;
    }

    public boolean isShowpollresult() {
        return showpollresult;
    }

    public List<ShowtopicPageAttachmentInfo> getAttachmentlist() {
        return attachmentlist;
    }

    public List<Pms> getPmlist() {
        return pmlist;
    }

    public List<Bonuslog> getBonuslogs() {
        return bonuslogs;
    }

    public List<Object[]> getPosttree() {
        return posttree;
    }

    public String[] getPagewordad() {
        return pagewordad;
    }

    public String getDoublead() {
        return doublead;
    }

    public String getFloatad() {
        return floatad;
    }

    public String getQuickbgadimg() {
        return quickbgadimg;
    }

    public String getQuickbgadlink() {
        return quickbgadlink;
    }

    public int getForumid() {
        return forumid;
    }

    public String getForumname() {
        return forumname;
    }

    public String getForumnav() {
        return forumnav;
    }

    public int getTopicid() {
        return topicid;
    }

    public int getPostid() {
        return postid;
    }

    public boolean isIspoll() {
        return ispoll;
    }

    public boolean isAllowvote() {
        return allowvote;
    }

    public String getTopictitle() {
        return topictitle;
    }

    public String getReplytitle() {
        return replytitle;
    }

    public int getTopicviews() {
        return topicviews;
    }

    public int getPostcount() {
        return postcount;
    }

    public String getSmilies() {
        return smilies;
    }

    public String getVoters() {
        return voters;
    }

    public int getIsmoder() {
        return ismoder;
    }

    public int getShowratelog() {
        return showratelog;
    }

    public int getParseurloff() {
        return parseurloff;
    }

    public int getSmileyoff() {
        return smileyoff;
    }

    public int getBbcodeoff() {
        return bbcodeoff;
    }

    public int getUsesig() {
        return usesig;
    }

    public String[] getScore() {
        return score;
    }

    public String[] getScoreunit() {
        return scoreunit;
    }

    public String getIgnorelink() {
        return ignorelink;
    }

    public int getQuickpost() {
        return quickpost;
    }

    public int getDisablepostctrl() {
        return disablepostctrl;
    }

    public Topicidentify getTopicidentify() {
        return topicidentify;
    }

    public Admingroups getAdmininfo() {
        return admininfo;
    }

    public Forums getForum() {
        return forum;
    }

    public List<Smilies> getSmilietypes() {
        return smilietypes;
    }

    public boolean isIsshowreplaylink() {
        return isshowreplaylink;
    }

    public boolean isAllowdownloadattach() {
        return allowdownloadattach;
    }

    public boolean isCanposttopic() {
        return canposttopic;
    }

    public boolean isCanreply() {
        return canreply;
    }

    public String getNavhomemenu() {
        return navhomemenu;
    }

    public boolean isShowpmhint() {
        return showpmhint;
    }

    public String getInpostad() {
        return inpostad;
    }

    public String getQuickeditorad() {
        return quickeditorad;
    }

    public String getTopictypes() {
        return topictypes;
    }

    public UserExtcreditsInfo getUserextcreditsinfo() {
        return userextcreditsinfo;
    }

    public boolean isNeedlogin() {
        return needlogin;
    }

    public String getFirstpagesmilies() {
        return firstpagesmilies;
    }

    public boolean isEnabletag() {
        return enabletag;
    }

    public TopicManager getTopicManager() {
        return topicManager;
    }
}
