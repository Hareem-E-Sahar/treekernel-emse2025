package com.aimluck.eip.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import org.apache.cayenne.DataRow;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.jetspeed.capability.CapabilityMap;
import org.apache.jetspeed.capability.CapabilityMapFactory;
import org.apache.jetspeed.om.profile.Entry;
import org.apache.jetspeed.om.profile.Layout;
import org.apache.jetspeed.om.profile.Portlets;
import org.apache.jetspeed.om.profile.Profile;
import org.apache.jetspeed.om.profile.ProfileLocator;
import org.apache.jetspeed.om.profile.psml.PsmlLayout;
import org.apache.jetspeed.om.registry.MediaTypeEntry;
import org.apache.jetspeed.om.security.Role;
import org.apache.jetspeed.om.security.UserIdPrincipal;
import org.apache.jetspeed.portal.Portlet;
import org.apache.jetspeed.portal.portlets.VelocityPortlet;
import org.apache.jetspeed.portal.security.portlets.PortletWrapper;
import org.apache.jetspeed.services.JetspeedSecurity;
import org.apache.jetspeed.services.PortletFactory;
import org.apache.jetspeed.services.Profiler;
import org.apache.jetspeed.services.PsmlManager;
import org.apache.jetspeed.services.Registry;
import org.apache.jetspeed.services.logging.JetspeedLogFactoryService;
import org.apache.jetspeed.services.logging.JetspeedLogger;
import org.apache.jetspeed.services.resources.JetspeedResources;
import org.apache.jetspeed.services.rundata.JetspeedRunData;
import org.apache.jetspeed.services.security.JetspeedSecurityException;
import org.apache.jetspeed.util.MimeType;
import org.apache.jetspeed.util.template.BaseJetspeedLink;
import org.apache.jetspeed.util.template.ContentTemplateLink;
import org.apache.jetspeed.util.template.JetspeedLink;
import org.apache.jetspeed.util.template.JetspeedLinkFactory;
import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.util.DynamicURI;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.TurbineException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import com.aimluck.commons.field.ALDateTimeField;
import com.aimluck.commons.field.ALStringField;
import com.aimluck.eip.cayenne.om.account.AipoLicense;
import com.aimluck.eip.cayenne.om.account.EipMCompany;
import com.aimluck.eip.cayenne.om.account.EipMUserPosition;
import com.aimluck.eip.cayenne.om.portlet.EipMFacilityGroup;
import com.aimluck.eip.cayenne.om.security.TurbineGroup;
import com.aimluck.eip.cayenne.om.security.TurbineUser;
import com.aimluck.eip.cayenne.om.security.TurbineUserGroupRole;
import com.aimluck.eip.common.ALBaseUser;
import com.aimluck.eip.common.ALDBErrorException;
import com.aimluck.eip.common.ALEipConstants;
import com.aimluck.eip.common.ALEipGroup;
import com.aimluck.eip.common.ALEipManager;
import com.aimluck.eip.common.ALEipUser;
import com.aimluck.eip.common.ALMyGroups;
import com.aimluck.eip.common.ALPermissionException;
import com.aimluck.eip.modules.actions.controls.Restore;
import com.aimluck.eip.orm.Database;
import com.aimluck.eip.orm.query.Operations;
import com.aimluck.eip.orm.query.SelectQuery;
import com.aimluck.eip.services.accessctl.ALAccessControlConstants;
import com.aimluck.eip.services.accessctl.ALAccessControlFactoryService;
import com.aimluck.eip.services.accessctl.ALAccessControlHandler;
import com.aimluck.eip.services.config.ALConfigHandler.Property;
import com.aimluck.eip.services.config.ALConfigService;

/**
 * Aimluck EIP のユーティリティクラスです。 <br />
 * 
 */
public class ALEipUtils {

    public static final String dummy_user_head = "dummy_";

    /** logger */
    private static final JetspeedLogger logger = JetspeedLogFactoryService.getLogger(ALEipUtils.class.getName());

    /**
   * セッション変数に値を格納します。 <br />
   * セッション変数は各ポートレット毎に管理されます。
   * 
   * @param rundata
   * @param context
   * @param key
   *          セッション変数名
   * @param value
   *          セッション変数の値
   */
    public static void setTemp(RunData rundata, Context context, String key, String value) {
        JetspeedRunData jdata = (JetspeedRunData) rundata;
        VelocityPortlet portlet = ((VelocityPortlet) context.get(JetspeedResources.PATH_PORTLET_KEY));
        if (portlet == null) {
            String js_peid = rundata.getParameters().getString(JetspeedResources.PATH_PORTLETID_KEY);
            jdata.getUser().setTemp(new StringBuffer().append(js_peid).append(key).toString(), value);
        } else {
            jdata.getUser().setTemp(new StringBuffer().append(portlet.getID()).append(key).toString(), value);
        }
    }

    /**
   * セッション変数を削除します。 <br />
   * セッション変数は各ポートレット毎に管理されます。
   * 
   * @param rundata
   * @param context
   * @param key
   *          セッション変数名
   */
    public static void removeTemp(RunData rundata, Context context, String key) {
        JetspeedRunData jdata = (JetspeedRunData) rundata;
        VelocityPortlet portlet = ((VelocityPortlet) context.get(JetspeedResources.PATH_PORTLET_KEY));
        if (portlet == null) {
            String js_peid = rundata.getParameters().getString(JetspeedResources.PATH_PORTLETID_KEY);
            jdata.getUser().removeTemp(new StringBuffer().append(js_peid).append(key).toString());
        } else {
            jdata.getUser().removeTemp(new StringBuffer().append(portlet.getID()).append(key).toString());
        }
    }

    public static void removeTemp(RunData rundata, Context context, List<String> list) {
        JetspeedRunData jdata = (JetspeedRunData) rundata;
        VelocityPortlet portlet = ((VelocityPortlet) context.get(JetspeedResources.PATH_PORTLET_KEY));
        int size = list.size();
        if (portlet == null) {
            String js_peid = rundata.getParameters().getString(JetspeedResources.PATH_PORTLETID_KEY);
            for (int i = 0; i < size; i++) {
                jdata.getUser().removeTemp(new StringBuffer().append(js_peid).append(list.get(i).toString()).toString());
            }
        } else {
            String peid = portlet.getID();
            for (int i = 0; i < size; i++) {
                jdata.getUser().removeTemp(new StringBuffer().append(peid).append(list.get(i).toString()).toString());
            }
        }
    }

    /**
   * セッション変数の値を取得します。 <br />
   * セッション変数は各ポートレット毎に管理されます。
   * 
   * @param rundata
   * @param context
   * @param key
   *          セッション変数名
   * @return セッション変数の値
   */
    public static String getTemp(RunData rundata, Context context, String key) {
        JetspeedRunData jdata = (JetspeedRunData) rundata;
        Object obj = null;
        VelocityPortlet portlet = ((VelocityPortlet) context.get(JetspeedResources.PATH_PORTLET_KEY));
        if (portlet == null) {
            String js_peid = rundata.getParameters().getString(JetspeedResources.PATH_PORTLETID_KEY);
            obj = jdata.getUser().getTemp(new StringBuffer().append(js_peid).append(key).toString());
        } else {
            obj = jdata.getUser().getTemp(new StringBuffer().append(portlet.getID()).append(key).toString());
        }
        return (obj == null) ? null : obj.toString();
    }

    /**
   * セッションに保存されているエンティティIDを整数値として返します。
   * 
   * @param rundata
   * @param context
   * @return
   */
    public static int getEntityId(RunData rundata, Context context) {
        int entity_id = 0;
        String entity_id_str = ALEipUtils.getTemp(rundata, context, ALEipConstants.ENTITY_ID);
        try {
            entity_id = Integer.parseInt(entity_id_str);
        } catch (Exception e) {
            entity_id = 0;
        }
        return entity_id;
    }

    /**
   * ユーザーIDを返します。
   * 
   * @param rundata
   * @return ユーザーID
   */
    public static int getUserId(RunData rundata) {
        JetspeedRunData jdata = (JetspeedRunData) rundata;
        String id = jdata.getJetspeedUser().getUserId();
        return Integer.parseInt(id);
    }

    /**
   * ポートレットを返します。
   * 
   * @param rundata
   * @param context
   * @return 自ポートレット
   */
    public static VelocityPortlet getPortlet(RunData rundata, Context context) {
        return ((VelocityPortlet) context.get(JetspeedResources.PATH_PORTLET_KEY));
    }

    /**
   * 指定したポートレット ID を持つポートレットのオブジェクトを取得します。
   * 
   * @param rundata
   * @param portletId
   * @return 自ポートレット
   */
    public static Portlet getPortlet(RunData rundata, String portletId) {
        try {
            Portlets portlets = ((JetspeedRunData) rundata).getProfile().getDocument().getPortlets();
            if (portlets == null) {
                return null;
            }
            @SuppressWarnings("unchecked") Iterator<Entry> iterator = portlets.getEntriesIterator();
            while (iterator.hasNext()) {
                Entry next = iterator.next();
                if (portletId.equals(next.getId())) {
                    PortletWrapper activityWrapper = (PortletWrapper) PortletFactory.getPortlet(next);
                    if (activityWrapper != null) {
                        return activityWrapper.getPortlet();
                    }
                }
            }
            Portlets[] portletList = portlets.getPortletsArray();
            if (portletList == null) {
                return null;
            }
            int length = portletList.length;
            for (int i = 0; i < length; i++) {
                Entry[] entries = portletList[i].getEntriesArray();
                if (entries == null || entries.length <= 0) {
                    continue;
                }
                int ent_length = entries.length;
                for (int j = 0; j < ent_length; j++) {
                    if (entries[j].getId().equals(portletId)) {
                        PortletWrapper wrapper = (PortletWrapper) PortletFactory.getPortlet(entries[j]);
                        if (wrapper != null) {
                            return wrapper.getPortlet();
                        } else {
                            return null;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Exception", ex);
            return null;
        }
        return null;
    }

    /**
   * リクエストが自ポートレットに対するものであるかを返します。 <br />
   * true となる場合、そのポートレットに対するフォーム送信となります。
   * 
   * @param rundata
   * @param context
   * @return
   */
    public static boolean isMatch(RunData rundata, Context context) {
        VelocityPortlet portlet = getPortlet(rundata, context);
        if (portlet == null) {
            return true;
        }
        String peid1 = portlet.getID();
        String peid2 = rundata.getParameters().getString(JetspeedResources.PATH_PORTLETID_KEY);
        if (peid1 == null || peid2 == null) {
            return false;
        }
        return peid1.equals(peid2);
    }

    /**
   * 指定されたグループに所属するユーザーを取得します。<br/>
   * DISABLEDがNのユーザー（即ち無効化されたユーザー）は取得しないことに注意してください。
   * 
   * @param groupname
   *          グループ名
   * @return ALEipUser の List
   */
    public static List<ALEipUser> getUsers(String groupname) {
        List<ALEipUser> list = new ArrayList<ALEipUser>();
        StringBuffer statement = new StringBuffer();
        statement.append("SELECT DISTINCT ");
        statement.append("  B.USER_ID, B.LOGIN_NAME, B.FIRST_NAME, B.LAST_NAME, D.POSITION ");
        statement.append("FROM turbine_user_group_role as A ");
        statement.append("LEFT JOIN turbine_user as B ");
        statement.append("  on A.USER_ID = B.USER_ID ");
        statement.append("LEFT JOIN turbine_group as C ");
        statement.append("  on A.GROUP_ID = C.GROUP_ID ");
        statement.append("LEFT JOIN eip_m_user_position as D ");
        statement.append("  on A.USER_ID = D.USER_ID ");
        statement.append("WHERE B.USER_ID > 3 AND B.DISABLED = 'F'");
        statement.append(" AND C.GROUP_NAME = #bind($groupName) ");
        statement.append("ORDER BY D.POSITION");
        String query = statement.toString();
        try {
            List<TurbineUser> list2 = Database.sql(TurbineUser.class, query).param("groupName", groupname).fetchList();
            ALEipUser user;
            for (TurbineUser tuser : list2) {
                user = new ALEipUser();
                user.initField();
                user.setUserId(tuser.getUserId());
                user.setName(tuser.getLoginName());
                user.setAliasName(tuser.getFirstName(), tuser.getLastName());
                list.add(user);
            }
        } catch (Throwable t) {
            logger.error("[ALEipUtils]", t);
        }
        return list;
    }

    /**
   * 指定されたグループに所属するユーザーを取得します。<br/>
   * DISABLEDがNのユーザー（即ち無効化されたユーザー）も取得します。
   * 
   * @param groupname
   *          グループ名
   * @return ALEipUser の List
   */
    public static List<ALEipUser> getUsersIncludingN(String groupname) {
        List<ALEipUser> list = new ArrayList<ALEipUser>();
        StringBuffer statement = new StringBuffer();
        statement.append("SELECT DISTINCT ");
        statement.append("  B.USER_ID, B.LOGIN_NAME, B.FIRST_NAME, B.LAST_NAME, D.POSITION ");
        statement.append("FROM turbine_user_group_role as A ");
        statement.append("LEFT JOIN turbine_user as B ");
        statement.append("  on A.USER_ID = B.USER_ID ");
        statement.append("LEFT JOIN turbine_group as C ");
        statement.append("  on A.GROUP_ID = C.GROUP_ID ");
        statement.append("LEFT JOIN eip_m_user_position as D ");
        statement.append("  on A.USER_ID = D.USER_ID ");
        statement.append("WHERE B.USER_ID > 3 AND B.DISABLED != 'T'");
        statement.append(" AND C.GROUP_NAME = #bind($groupName) ");
        statement.append("ORDER BY D.POSITION");
        String query = statement.toString();
        try {
            List<TurbineUser> list2 = Database.sql(TurbineUser.class, query).param("groupName", groupname).fetchList();
            ALEipUser user;
            for (TurbineUser tuser : list2) {
                user = new ALEipUser();
                user.initField();
                user.setUserId(tuser.getUserId());
                user.setName(tuser.getLoginName());
                user.setAliasName(tuser.getFirstName(), tuser.getLastName());
                list.add(user);
            }
        } catch (Throwable t) {
            logger.error("[ALEipUtils]", t);
        }
        return list;
    }

    /**
   * 指定されたグループに所属するユーザーのIDを取得します。
   * 
   * @param groupname
   *          グループ名
   * @return Integer の List
   */
    public static List<Integer> getUserIds(String groupname) {
        List<Integer> list = new ArrayList<Integer>();
        StringBuffer statement = new StringBuffer();
        statement.append("SELECT DISTINCT ");
        statement.append("  B.USER_ID, D.POSITION ");
        statement.append("FROM turbine_user_group_role as A ");
        statement.append("LEFT JOIN turbine_user as B ");
        statement.append("  on A.USER_ID = B.USER_ID ");
        statement.append("LEFT JOIN turbine_group as C ");
        statement.append("  on A.GROUP_ID = C.GROUP_ID ");
        statement.append("LEFT JOIN eip_m_user_position as D ");
        statement.append("  on A.USER_ID = D.USER_ID ");
        statement.append("WHERE B.USER_ID > 3 AND B.DISABLED = 'F'");
        statement.append(" AND C.GROUP_NAME = #bind($groupName) ");
        statement.append("ORDER BY D.POSITION");
        String query = statement.toString();
        try {
            List<TurbineUser> list2 = Database.sql(TurbineUser.class, query).param("groupName", groupname).fetchList();
            for (TurbineUser tuser : list2) {
                list.add(tuser.getUserId());
            }
        } catch (Throwable t) {
            logger.error("[ALEipUtils]", t);
        }
        return list;
    }

    /**
   * 指定された部署に所属するユーザーを取得します。
   * 
   * @param postid
   *          部署ID
   * @return ALEipUser の List
   */
    public static List<ALEipUser> getUsersFromPost(int postid) {
        List<ALEipUser> list = new ArrayList<ALEipUser>();
        StringBuffer statement = new StringBuffer();
        statement.append("SELECT DISTINCT ");
        statement.append("  B.USER_ID, B.LOGIN_NAME, B.FIRST_NAME, B.LAST_NAME, D.POSITION ");
        statement.append("FROM turbine_user_group_role as A ");
        statement.append("LEFT JOIN turbine_user as B ");
        statement.append("  on A.USER_ID = B.USER_ID ");
        statement.append("LEFT JOIN turbine_group as C ");
        statement.append("  on A.GROUP_ID = C.GROUP_ID ");
        statement.append("LEFT JOIN eip_m_user_position as D ");
        statement.append("  on A.USER_ID = D.USER_ID ");
        statement.append("WHERE B.USER_ID > 3 AND B.DISABLED = 'F'");
        statement.append(" AND B.POST_ID = #bind($postId) ");
        statement.append("ORDER BY D.POSITION");
        String query = statement.toString();
        try {
            List<TurbineUser> list2 = Database.sql(TurbineUser.class, query).param("postId", postid).fetchList();
            ALEipUser user;
            for (TurbineUser tuser : list2) {
                user = new ALEipUser();
                user.initField();
                user.setUserId(tuser.getUserId());
                user.setName(tuser.getLoginName());
                user.setAliasName(tuser.getFirstName(), tuser.getLastName());
                list.add(user);
            }
        } catch (Throwable t) {
            logger.error("[ALEipUtils]", t);
        }
        return list;
    }

    /**
   * <code>SelectQuery</code> の条件に従ってユーザーを取得します。
   * 
   * @param crt
   * @return ALEipUser の List
   */
    public static List<ALEipUser> getUsersFromSelectQuery(SelectQuery<TurbineUser> query) {
        List<ALEipUser> list = new ArrayList<ALEipUser>();
        try {
            List<TurbineUser> ulist = query.orderAscending(TurbineUser.EIP_MUSER_POSITION_PROPERTY + "." + EipMUserPosition.POSITION_PROPERTY).fetchList();
            for (TurbineUser record : ulist) {
                ALEipUser user = new ALEipUser();
                user.initField();
                user.setUserId(record.getUserId().intValue());
                user.setName(record.getLoginName());
                user.setAliasName(record.getFirstName(), record.getLastName());
                list.add(user);
            }
        } catch (Throwable t) {
            logger.error("[ALEipUtils]", t);
        }
        return list;
    }

    /**
   * 自ユーザーの簡易オブジェクトを取得します。
   * 
   * @param crt
   * @return ALEipUser
   */
    public static ALEipUser getALEipUser(RunData rundata) {
        JetspeedRunData jdata = (JetspeedRunData) rundata;
        ALEipUser user = new ALEipUser();
        user.initField();
        user.setUserId(Integer.parseInt(jdata.getJetspeedUser().getUserId()));
        user.setName(jdata.getJetspeedUser().getUserName());
        user.setAliasName(jdata.getJetspeedUser().getFirstName(), jdata.getJetspeedUser().getLastName());
        return user;
    }

    /**
   * 指定したユーザーIDの簡易オブジェクトを取得します。
   * 
   * @param id
   * @return
   */
    public static ALEipUser getALEipUser(int id) throws ALDBErrorException {
        TurbineUser tuser = getTurbineUser(id);
        return getALEipUser(tuser);
    }

    /**
   * 指定したユーザーIDの簡易オブジェクトを取得します。
   * 
   * @param id
   * @return
   */
    public static ALEipUser getALEipUser(TurbineUser tuser) throws ALDBErrorException {
        if (tuser == null) {
            return null;
        }
        ALEipUser user = new ALEipUser();
        user.initField();
        user.setUserId(tuser.getUserId().intValue());
        user.setName(tuser.getLoginName());
        user.setAliasName(tuser.getFirstName(), tuser.getLastName());
        return user;
    }

    /**
   * 指定したユーザーIDの簡易オブジェクトを取得します。
   * 
   * @param id
   * @return
   */
    public static ALEipUser getALEipUser(String loginname) throws ALDBErrorException {
        TurbineUser tuser = getTurbineUser(loginname);
        if (tuser == null) {
            return null;
        }
        ALEipUser user = new ALEipUser();
        user.initField();
        user.setUserId(tuser.getUserId().intValue());
        user.setName(tuser.getLoginName());
        user.setAliasName(tuser.getFirstName(), tuser.getLastName());
        return user;
    }

    /**
   * 指定したユーザーIDのオブジェクトを取得します。
   * 
   * @param userid
   *          ユーザID
   * @return
   */
    public static ALBaseUser getBaseUser(int userid) {
        String uid = String.valueOf(userid);
        try {
            if (uid == null) {
                logger.debug("Empty ID...");
                return null;
            }
            return (ALBaseUser) JetspeedSecurity.getUser(new UserIdPrincipal(uid));
        } catch (Exception ex) {
            logger.error("Exception", ex);
            return null;
        }
    }

    /**
   * 指定したユーザーIDのユーザーオブジェクトを取得します。
   * 
   * @param id
   * @return
   */
    public static TurbineUser getTurbineUser(int id) throws ALDBErrorException {
        Object obj = ALEipManager.getInstance().getTurbineUser(id);
        TurbineUser tuser = null;
        if (obj == null) {
            tuser = Database.get(TurbineUser.class, id);
            ALEipManager.getInstance().setTurbineUser(id, tuser);
        } else {
            tuser = (TurbineUser) obj;
        }
        return tuser;
    }

    /**
   * 指定したログイン名のユーザーオブジェクトを取得します。
   * 
   * @param login_name
   * @return
   */
    public static TurbineUser getTurbineUser(String login_name) throws ALDBErrorException {
        Object obj = ALEipManager.getInstance().getTurbineUser(login_name);
        TurbineUser tuser = null;
        if (obj == null) {
            tuser = Database.query(TurbineUser.class).where(Operations.eq(TurbineUser.LOGIN_NAME_PROPERTY, login_name)).fetchSingle();
            ALEipManager.getInstance().setTurbineUser(login_name, tuser);
        } else {
            tuser = (TurbineUser) obj;
        }
        return tuser;
    }

    /**
   * 指定したユーザーIDが有効か（無効化、削除されていないか）どうか調べます。
   * 
   * @param id
   * @return
   */
    public static boolean isEnabledUser(int id) throws ALDBErrorException {
        TurbineUser tuser = getTurbineUser(id);
        if (tuser == null) {
            return false;
        }
        return "F".equals(tuser.getDisabled());
    }

    /**
   * ユーザーのフルネームを取得します。
   * 
   * @param userid
   *          ユーザID
   * @return
   */
    public static String getUserFullName(int userid) {
        String userName = "";
        ALBaseUser user = getBaseUser(userid);
        if (user != null) {
            userName = new StringBuffer().append(user.getLastName()).append(" ").append(user.getFirstName()).toString();
        }
        return userName;
    }

    /**
   * 部署の変更を行います。 <br>
   * 部署に関連付けされているグループの更新も同時に行います。
   * 
   * @param rundata
   * @param username
   *          ユーザー名
   * @param postid
   *          部署ID
   * @return true 部署変更成功 false 部署変更失敗
   */
    public static boolean changePost(RunData rundata, String username, int postid) throws ALDBErrorException {
        try {
            ALBaseUser user = (ALBaseUser) JetspeedSecurity.getUser(username);
            JetspeedSecurity.joinGroup(username, (ALEipManager.getInstance().getPostMap().get(Integer.valueOf(postid))).getGroupName().getValue());
            user.setPostId(postid);
            JetspeedSecurity.saveUser(user);
            ALBaseUser currentUser = (ALBaseUser) rundata.getUser();
            if (currentUser.getUserName().equals(user.getUserName())) {
                currentUser.setPostId(user.getPostId());
            }
        } catch (JetspeedSecurityException ex) {
            logger.error("[ALEipUtils]", ex);
            throw new ALDBErrorException();
        }
        return true;
    }

    /**
   * 自ユーザーのマイグループを再読み込みします。 <br>
   * 読み込まれたマイグループはセッションに保存されます。 <br>
   * マイグループの更新が行われた場合はこのメソッドを呼び出してください。
   * 
   * @param rundata
   */
    public static void reloadMygroup(RunData rundata) throws ALDBErrorException {
        List<ALEipGroup> ulist = new ArrayList<ALEipGroup>();
        try {
            Expression exp = ExpressionFactory.matchExp(TurbineGroup.OWNER_ID_PROPERTY, Integer.valueOf(getUserId(rundata)));
            List<TurbineGroup> list = Database.query(TurbineGroup.class, exp).fetchList();
            for (TurbineGroup record : list) {
                ALEipGroup group = new ALEipGroup();
                group.initField();
                group.setName(record.getGroupName());
                group.setAliasName(record.getGroupAliasName());
                ulist.add(group);
            }
        } catch (Exception ex) {
            logger.error("[ALEipUtils]", ex);
            throw new ALDBErrorException();
        }
        JetspeedRunData jdata = (JetspeedRunData) rundata;
        ALMyGroups mygroups = new ALMyGroups();
        mygroups.addList(ulist);
        jdata.getUser().setTemp(ALEipConstants.MYGROUP, mygroups);
    }

    /**
   * 自ユーザーのマイグループを取得します。
   * 
   * @param rundata
   * @return ALEipGroup の List
   */
    public static List<ALEipGroup> getMyGroups(RunData rundata) throws ALDBErrorException {
        JetspeedRunData jdata = (JetspeedRunData) rundata;
        Object obj = jdata.getUser().getTemp(ALEipConstants.MYGROUP);
        if (obj == null || !(obj instanceof ALMyGroups)) {
            reloadMygroup(rundata);
            obj = jdata.getUser().getTemp(ALEipConstants.MYGROUP);
        }
        ALMyGroups mygroups = (ALMyGroups) obj;
        return mygroups.getList();
    }

    public static List<ALEipGroup> getFacilityGroups() {
        List<ALEipGroup> facilityGroupAllList = new ArrayList<ALEipGroup>();
        try {
            SelectQuery<EipMFacilityGroup> query = Database.query(EipMFacilityGroup.class);
            List<EipMFacilityGroup> facility_list = query.orderAscending(EipMFacilityGroup.GROUP_NAME_PROPERTY).fetchList();
            for (EipMFacilityGroup record : facility_list) {
                ALEipGroup bean = new ALEipGroup();
                bean.initField();
                bean.setAliasName(record.getGroupName());
                bean.setName(record.getGroupId().toString());
                facilityGroupAllList.add(bean);
            }
        } catch (Exception ex) {
            logger.error("Exception", ex);
        }
        return facilityGroupAllList;
    }

    public static List<ALEipGroup> getALEipGroups() {
        List<ALEipGroup> facilityGroupAllList = new ArrayList<ALEipGroup>();
        try {
            SelectQuery<EipMFacilityGroup> query = Database.query(EipMFacilityGroup.class);
            List<EipMFacilityGroup> facility_list = query.orderAscending(EipMFacilityGroup.GROUP_NAME_PROPERTY).fetchList();
            for (EipMFacilityGroup record : facility_list) {
                ALEipGroup bean = new ALEipGroup();
                bean.initField();
                bean.setAliasName(record.getGroupName());
                bean.setName(record.getGroupId().toString());
                facilityGroupAllList.add(bean);
            }
        } catch (Exception ex) {
            logger.error("Exception", ex);
        }
        return facilityGroupAllList;
    }

    /**
   * 会社名を取得します。
   * 
   * @param id
   * @return
   */
    public static String getCompanyName(int id) {
        String companyName = null;
        try {
            Expression exp = ExpressionFactory.matchDbExp(EipMCompany.COMPANY_ID_PK_COLUMN, Integer.valueOf(id));
            List<EipMCompany> list = Database.query(EipMCompany.class, exp).select(EipMCompany.COMPANY_NAME_COLUMN).fetchList();
            if (list == null || list.size() == 0) {
                logger.debug("[ALEipUtils] Not found ComapnyID...");
                return null;
            }
            EipMCompany record = list.get(0);
            companyName = record.getCompanyName();
        } catch (Exception ex) {
            logger.error("[ALEipUtils]", ex);
            companyName = null;
        }
        return companyName;
    }

    /**
   * 部署名を取得します。
   * 
   * @param id
   * @return
   */
    public static String getPostName(int id) {
        if (ALEipManager.getInstance().getPostMap().containsKey(Integer.valueOf(id))) {
            return (ALEipManager.getInstance().getPostMap().get(Integer.valueOf(id))).getPostName().getValue();
        }
        return null;
    }

    /**
   * 役職名を取得します。
   * 
   * @param id
   * @return
   */
    public static String getPositionName(int id) {
        if (ALEipManager.getInstance().getPositionMap().containsKey(Integer.valueOf(id))) {
            return (ALEipManager.getInstance().getPositionMap().get(Integer.valueOf(id))).getPositionName().getValue();
        }
        return null;
    }

    /**
   * ページが見つからない場合に、リダイレクト処理します。
   * 
   * @return
   */
    public static boolean redirectPageNotFound(RunData rundata) {
        try {
            JetspeedLink jsLink = JetspeedLinkFactory.getInstance(rundata);
            DynamicURI duri = jsLink.getPage();
            String template = rundata.getParameters().getString(JetspeedResources.PATH_TEMPLATE_KEY);
            if (template != null && !("".equals(template))) {
                if (template.endsWith("DetailScreen")) {
                    VelocityContext context = new VelocityContext();
                    setupContext(rundata, context);
                    try {
                        ServletOutputStream out = null;
                        HttpServletResponse response = rundata.getResponse();
                        out = response.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, ALEipConstants.DEF_CONTENT_ENCODING));
                        Template templete = Velocity.getTemplate("screens/html/AjaxPageNotFound.vm");
                        templete.merge(context, writer);
                        writer.flush();
                        writer.close();
                    } catch (Exception e) {
                        return false;
                    }
                    return true;
                }
            }
            duri.addPathInfo("template", "PageNotFound");
            rundata.setRedirectURI(duri.toString());
            rundata.getResponse().sendRedirect(rundata.getRedirectURI());
            JetspeedLinkFactory.putInstance(jsLink);
            jsLink = null;
            return true;
        } catch (TurbineException e) {
            logger.error("Exception", e);
            return false;
        } catch (IOException e) {
            logger.error("Exception", e);
            return false;
        }
    }

    /**
   * データベースエラーの場合に、リダイレクト処理します。
   * 
   * @return
   */
    public static boolean redirectDBError(RunData rundata) {
        try {
            JetspeedLink jsLink = JetspeedLinkFactory.getInstance(rundata);
            DynamicURI duri = jsLink.getPage();
            String template = rundata.getParameters().getString(JetspeedResources.PATH_TEMPLATE_KEY);
            if (template != null && !("".equals(template))) {
                if (template.endsWith("DetailScreen")) {
                    VelocityContext context = new VelocityContext();
                    setupContext(rundata, context);
                    try {
                        ServletOutputStream out = null;
                        HttpServletResponse response = rundata.getResponse();
                        out = response.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, ALEipConstants.DEF_CONTENT_ENCODING));
                        Template templete = Velocity.getTemplate("screens/html/AjaxDBError.vm");
                        templete.merge(context, writer);
                        writer.flush();
                        writer.close();
                    } catch (Exception e) {
                        return false;
                    }
                    return true;
                }
            }
            duri.addPathInfo(JetspeedResources.PATH_TEMPLATE_KEY, "DBError");
            rundata.setRedirectURI(duri.toString());
            rundata.getResponse().sendRedirect(rundata.getRedirectURI());
            JetspeedLinkFactory.putInstance(jsLink);
            jsLink = null;
            return true;
        } catch (TurbineException e) {
            logger.error("Exception", e);
            return false;
        } catch (IOException e) {
            logger.error("Exception", e);
            return false;
        }
    }

    /**
   * パーミッションエラーの場合に、リダイレクト処理します。
   * 
   * @return
   */
    public static boolean redirectPermissionError(RunData rundata) {
        try {
            JetspeedLink jsLink = JetspeedLinkFactory.getInstance(rundata);
            DynamicURI duri = jsLink.getPage();
            String template = rundata.getParameters().getString(JetspeedResources.PATH_TEMPLATE_KEY);
            if (template != null && !("".equals(template))) {
                if (template.endsWith("JSONScreen")) {
                    VelocityContext context = new VelocityContext();
                    setupContext(rundata, context);
                    try {
                        ServletOutputStream out = null;
                        HttpServletResponse response = rundata.getResponse();
                        out = response.getOutputStream();
                        List<String> list = new ArrayList<String>();
                        list.add("PermissionError");
                        list.add(ALAccessControlConstants.DEF_PERMISSION_ERROR_STR);
                        JSONArray json = JSONArray.fromObject(list);
                        StringBuffer result = new StringBuffer().append("/* ").append(json.toString()).append(" */");
                        byte[] byteResult = result.toString().getBytes(ALEipConstants.DEF_CONTENT_ENCODING);
                        out.write(byteResult);
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        return false;
                    }
                    return true;
                } else if (template.endsWith("FormScreen") || template.endsWith("DetailScreen")) {
                    VelocityContext context = new VelocityContext();
                    setupContext(rundata, context);
                    try {
                        ServletOutputStream out = null;
                        HttpServletResponse response = rundata.getResponse();
                        out = response.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, ALEipConstants.DEF_CONTENT_ENCODING));
                        Template templete = Velocity.getTemplate("screens/html/AjaxPermissionError.vm");
                        templete.merge(context, writer);
                        writer.flush();
                        writer.close();
                    } catch (Exception e) {
                        return false;
                    }
                    return true;
                } else if (template.endsWith("Screen")) {
                    VelocityContext context = new VelocityContext();
                    setupContext(rundata, context);
                    try {
                        ServletOutputStream out = null;
                        HttpServletResponse response = rundata.getResponse();
                        out = response.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, ALEipConstants.DEF_CONTENT_ENCODING));
                        Template templete = Velocity.getTemplate("portlets/html/PermissionError.vm");
                        templete.merge(context, writer);
                        writer.flush();
                        writer.close();
                    } catch (Exception e) {
                        return false;
                    }
                    return true;
                } else if (template.equals("Customize") || template.equals("Home")) {
                    duri.addPathInfo(JetspeedResources.PATH_TEMPLATE_KEY, "PermissionError");
                    rundata.setRedirectURI(duri.toString());
                    rundata.getResponse().sendRedirect(rundata.getRedirectURI());
                    JetspeedLinkFactory.putInstance(jsLink);
                    jsLink = null;
                    return true;
                } else if (isCellularPhone(rundata)) {
                    duri.addPathInfo(JetspeedResources.PATH_TEMPLATE_KEY, "CellPermissionError");
                    rundata.setRedirectURI(duri.toString());
                    rundata.getResponse().sendRedirect(rundata.getRedirectURI());
                    JetspeedLinkFactory.putInstance(jsLink);
                    jsLink = null;
                    Restore restore = new Restore();
                    try {
                        restore.doPerform(rundata);
                    } catch (Exception e) {
                    }
                    return true;
                } else if (isSmartPhone(rundata)) {
                    duri.addPathInfo(JetspeedResources.PATH_TEMPLATE_KEY, "CellPermissionError");
                    rundata.setRedirectURI(duri.toString());
                    rundata.getResponse().sendRedirect(rundata.getRedirectURI());
                    JetspeedLinkFactory.putInstance(jsLink);
                    jsLink = null;
                    Restore restore = new Restore();
                    try {
                        restore.doPerform(rundata);
                    } catch (Exception e) {
                    }
                    return true;
                }
            }
            JetspeedRunData jdata = (JetspeedRunData) rundata;
            if (jdata.getMode() == JetspeedRunData.MAXIMIZE) {
                rundata.getRequest().setAttribute("redirectTemplate", "permission-error-maximize");
            } else {
                Context context = (Context) jdata.getTemplateInfo().getTemplateContext("VelocityPortletContext");
                context.put(JetspeedResources.PATH_TEMPLATE_KEY, "PermissionError");
            }
            return true;
        } catch (TurbineException e) {
            logger.error("Exception", e);
            return false;
        } catch (IOException e) {
            logger.error("Exception", e);
            return false;
        }
    }

    /**
   * 改行コードを含む文字列を、複数行に分割します。
   * 
   * @return
   */
    public static String getMessageList(String msgline) {
        StringBuffer sb = new StringBuffer();
        ALStringField field = null;
        if (msgline == null || msgline.equals("")) {
            return "";
        }
        if (msgline.indexOf("\r") < 0 && msgline.indexOf("\n") < 0 && msgline.indexOf("\r\n") < 0) {
            field = new ALStringField();
            field.setTrim(false);
            field.setValue(msgline);
            return ALCommonUtils.replaceToAutoCR(replaceStrToLink(field.toString()));
        }
        String token = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new StringReader(msgline));
            while ((token = reader.readLine()) != null) {
                field = new ALStringField();
                field.setTrim(false);
                field.setValue(token);
                sb.append(ALCommonUtils.replaceToAutoCR(replaceStrToLink(replaseLeftSpace(field.toString())))).append("<br/>");
            }
            reader.close();
        } catch (IOException ioe) {
            try {
                reader.close();
            } catch (IOException e) {
            }
            return "";
        }
        int index = sb.lastIndexOf("<br/>");
        if (index == -1) {
            return sb.toString();
        }
        return sb.substring(0, index);
    }

    /**
   * 左端の半角空文字を「&nbsp;」に変換する。
   * 
   * @param str
   * @return
   */
    public static String replaseLeftSpace(String str) {
        if (str == null || str.length() <= 0) {
            return str;
        }
        int len = str.length();
        int st = 0;
        char[] val = str.toCharArray();
        StringBuffer sb = new StringBuffer();
        while ((st < len)) {
            if (val[st] == ' ') {
                sb.append("&nbsp;");
            } else {
                sb.append(val[st]);
            }
            st++;
        }
        return (sb.length() > 0) ? sb.toString() : str;
    }

    /**
   * アクセス元の端末が携帯電話であるかを判定します。
   * 
   * @param data
   * @return
   */
    public static boolean isCellularPhone(RunData data) {
        boolean isCellularPhone = false;
        CapabilityMap cm = CapabilityMapFactory.getCapabilityMap(data.getRequest().getHeader("User-Agent"));
        MimeType mime = cm.getPreferredType();
        if (mime != null) {
            MediaTypeEntry media = (MediaTypeEntry) Registry.getEntry(Registry.MEDIA_TYPE, cm.getPreferredMediaType());
            String mediatype = media.getName();
            if ("docomo_imode".equals(mediatype) || "docomo_foma".equals(mediatype) || "au".equals(mediatype) || "vodafone".equals(mediatype)) {
                isCellularPhone = true;
            }
        }
        return isCellularPhone;
    }

    /**
   * アクセス元の端末がスマートフォンであるかを判定します。
   * 
   * @param data
   * @return
   */
    public static boolean isSmartPhone(RunData data) {
        boolean isSmartPhone = false;
        CapabilityMap cm = CapabilityMapFactory.getCapabilityMap(data.getRequest().getHeader("User-Agent"));
        MimeType mime = cm.getPreferredType();
        if (mime != null) {
            MediaTypeEntry media = (MediaTypeEntry) Registry.getEntry(Registry.MEDIA_TYPE, cm.getPreferredMediaType());
            String mediatype = media.getName();
            if ("iphone".equals(mediatype) || "wm".equals(mediatype)) {
                isSmartPhone = true;
            }
        }
        return isSmartPhone;
    }

    /**
   * 指定した2つの日付を比較します。
   * 
   * @param date1
   * @param date2
   * @return 等しい場合、0。date1>date2の場合、1。date1 < date2の場合、2。
   */
    public static int compareToDate(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        int date1Year = cal1.get(Calendar.YEAR);
        int date1Month = cal1.get(Calendar.MONTH) + 1;
        int date1Day = cal1.get(Calendar.DATE);
        int date1Hour = cal1.get(Calendar.HOUR);
        int date1Minute = cal1.get(Calendar.MINUTE);
        int date1Second = cal1.get(Calendar.SECOND);
        int date2Year = cal2.get(Calendar.YEAR);
        int date2Month = cal2.get(Calendar.MONTH) + 1;
        int date2Day = cal2.get(Calendar.DATE);
        int date2Hour = cal2.get(Calendar.HOUR);
        int date2Minute = cal2.get(Calendar.MINUTE);
        int date2Second = cal2.get(Calendar.SECOND);
        if (date1Year == date2Year && date1Month == date2Month && date1Day == date2Day && date1Hour == date2Hour && date1Minute == date2Minute && date1Second == date2Second) {
            return 0;
        }
        if (cal1.after(cal2)) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
   * データベースの検索結果から、指定したキーに対応する値を取得します。
   * 
   * @param dataRow
   * @param key
   * @return
   */
    public static Object getObjFromDataRow(DataRow dataRow, String key) {
        String lowerKey = key.toLowerCase();
        if (dataRow.containsKey(lowerKey)) {
            return dataRow.get(lowerKey);
        } else {
            return dataRow.get(key.toUpperCase());
        }
    }

    /**
   * 会社情報のオブジェクトを取得します。
   * 
   * @param id
   * @return
   */
    public static EipMCompany getEipMCompany(String id) {
        Expression exp = ExpressionFactory.matchDbExp(EipMCompany.COMPANY_ID_PK_COLUMN, Integer.valueOf(id));
        List<EipMCompany> list = Database.query(EipMCompany.class, exp).fetchList();
        if (list == null || list.size() == 0) {
            logger.debug("Not found ID...");
            return null;
        }
        return list.get(0);
    }

    /**
   * @see ALServletUtils#getAccessUrl(String, int, boolean)
   * @param ip
   * @param port
   * @param servername
   * @param isGlobal
   * @return
   * @deprecated
   */
    @Deprecated
    public static String getUrl(String ip, int port, String servername, boolean isGlobal) {
        if (ip == null || ip.length() == 0 || port == -1) {
            return "";
        }
        String protocol = isGlobal ? ALConfigService.get(Property.ACCESS_GLOBAL_URL_PROTOCOL) : ALConfigService.get(Property.ACCESS_LOCAL_URL_PROTOCOL);
        StringBuffer url = new StringBuffer();
        if (port == 80 || port == 443) {
            url.append(protocol).append("://").append(ip).append("/").append(servername).append(servername.isEmpty() ? "" : "/");
        } else {
            url.append(protocol).append("://").append(ip).append(":").append(port).append("/").append(servername).append(servername.isEmpty() ? "" : "/");
        }
        return url.toString();
    }

    /**
   * 
   * @param rundata
   * @param context
   */
    public static void setupContext(RunData rundata, Context context) {
        String js_peid;
        if (!rundata.getParameters().containsKey(JetspeedResources.PATH_PORTLETID_KEY)) {
            return;
        }
        js_peid = rundata.getParameters().getString(JetspeedResources.PATH_PORTLETID_KEY);
        Portlet portlet = getPortlet(rundata, js_peid);
        context.put("portlet", portlet);
        context.put("jslink", new BaseJetspeedLink(rundata));
        context.put("clink", new ContentTemplateLink(rundata));
    }

    /**
   * 
   * @param rundata
   * @param context
   * @param key
   * @return
   */
    public static String getParameter(RunData rundata, Context context, String key) {
        String name = null;
        String idParam = rundata.getParameters().getString(key);
        name = ALEipUtils.getTemp(rundata, context, key);
        if (idParam == null && name == null) {
            ALEipUtils.removeTemp(rundata, context, key);
            name = null;
        } else if (idParam != null) {
            ALEipUtils.setTemp(rundata, context, key, idParam);
            name = idParam;
        }
        return name;
    }

    /**
   * 指定したエントリー名のポートレットへの URI を取得します。
   * 
   * @param rundata
   * @param portletEntryName
   *          PSML ファイルに記述されているタグ entry の要素 parent
   * @return
   */
    public static String getPortletURI(RunData rundata, String portletEntryId) {
        try {
            Portlets portlets = ((JetspeedRunData) rundata).getProfile().getDocument().getPortlets();
            if (portlets == null) {
                return null;
            }
            Portlets[] portletList = portlets.getPortletsArray();
            if (portletList == null) {
                return null;
            }
            int length = portletList.length;
            for (int i = 0; i < length; i++) {
                Entry[] entries = portletList[i].getEntriesArray();
                if (entries == null || entries.length <= 0) {
                    continue;
                }
                int ent_length = entries.length;
                for (int j = 0; j < ent_length; j++) {
                    if (entries[j].getId().equals(portletEntryId)) {
                        JetspeedLink jsLink = JetspeedLinkFactory.getInstance(rundata);
                        DynamicURI duri = jsLink.getLink(JetspeedLink.CURRENT, null, null, JetspeedLink.CURRENT, null);
                        duri = duri.addPathInfo(JetspeedResources.PATH_PANEID_KEY, portletList[i].getId()).addPathInfo(JetspeedResources.PATH_PORTLETID_KEY, entries[j].getId()).addQueryData(JetspeedResources.PATH_ACTION_KEY, "controls.Maximize");
                        return duri.toString();
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Exception", ex);
            return null;
        }
        return null;
    }

    /**
   * 
   * @param rundata
   * @param portletEntryId
   * @return
   */
    public static String getPortletURItoTopPage(RunData rundata, String portletEntryId) {
        try {
            Portlets portlets = ((JetspeedRunData) rundata).getProfile().getDocument().getPortlets();
            if (portlets == null) {
                return null;
            }
            Portlets[] portletList = portlets.getPortletsArray();
            if (portletList == null) {
                return null;
            }
            int length = portletList.length;
            for (int i = 0; i < length; i++) {
                Entry[] entries = portletList[i].getEntriesArray();
                if (entries == null || entries.length <= 0) {
                    continue;
                }
                int ent_length = entries.length;
                for (int j = 0; j < ent_length; j++) {
                    if (entries[j].getId().equals(portletEntryId)) {
                        JetspeedLink jsLink = JetspeedLinkFactory.getInstance(rundata);
                        DynamicURI duri = jsLink.getLink(JetspeedLink.CURRENT, null, null, JetspeedLink.CURRENT, null);
                        duri = duri.addPathInfo(JetspeedResources.PATH_PANEID_KEY, portletList[i].getId()).addQueryData(JetspeedResources.PATH_ACTION_KEY, "controls.Restore");
                        return duri.toString();
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Exception", ex);
            return null;
        }
        return null;
    }

    /**
   * 文字列内のリンクにタグAを追加します。
   * 
   * @param msg
   * @return
   */
    public static String replaceStrToLink(String msg) {
        if (msg != null) {
            String regex = "(https?|ftp|gopher|telnet|whois|news)\\:([\\w|\\:\\!\\#\\$\\%\\=\\&\\-\\^\\`\\\\|\\@\\~\\[\\{\\]\\}\\;\\+\\*\\,\\.\\?\\/]+)";
            Pattern p = Pattern.compile(regex);
            boolean check = true;
            while (check) {
                check = false;
                Matcher m = p.matcher(msg);
                while (m.find()) {
                    if (m.group(0).contains("@")) {
                        String matchString = m.group(0);
                        matchString = matchString.replaceAll("@", "%40");
                        String pre = msg.substring(0, m.start(0));
                        String post = msg.substring(m.end(0), msg.length());
                        msg = pre + matchString + post;
                        check = true;
                    }
                }
            }
            String newMsg = msg.replaceAll("(https?|ftp|gopher|telnet|whois|news)\\:([\\w|\\:\\!\\#\\$\\%\\=\\&\\-\\^\\`\\\\|\\@\\~\\[\\{\\]\\}\\;\\+\\*\\,\\.\\?\\/]+)", "<a href=\"$1\\:$2\" target=\"_blank\">$1\\:$2</a>");
            return newMsg.replaceAll("[\\w\\.\\-]+@([\\w\\-]+\\.)+[\\w\\-]+", "<a href='mailto:$0'>$0</a>");
        } else {
            return "";
        }
    }

    /**
   * ユーザーの所属する部署を取得します。
   * 
   * @param id
   *          ユーザーID
   * @return 所属する部署リスト
   */
    public static List<ALStringField> getPostNameList(int id) {
        SelectQuery<TurbineUserGroupRole> query = Database.query(TurbineUserGroupRole.class);
        Expression exp1 = ExpressionFactory.matchExp(TurbineUserGroupRole.TURBINE_USER_PROPERTY, Integer.valueOf(id));
        Expression exp2 = ExpressionFactory.greaterExp(TurbineUserGroupRole.TURBINE_GROUP_PROPERTY, Integer.valueOf(3));
        Expression exp3 = ExpressionFactory.matchExp(TurbineUserGroupRole.TURBINE_GROUP_PROPERTY + "." + TurbineGroup.OWNER_ID_PROPERTY, Integer.valueOf(1));
        query.setQualifier(exp1);
        query.andQualifier(exp2);
        query.andQualifier(exp3);
        List<TurbineUserGroupRole> list = query.fetchList();
        ALStringField sf = null;
        List<ALStringField> postNames = new ArrayList<ALStringField>();
        for (TurbineUserGroupRole role : list) {
            sf = new ALStringField(role.getTurbineGroup().getGroupAliasName());
            postNames.add(sf);
        }
        return postNames;
    }

    /**
   * ユーザーの所属する部署のIDを取得します。
   * 
   * @param id
   *          ユーザーID
   * @return 所属する部署リスト
   */
    public static List<Integer> getPostIdList(int id) {
        SelectQuery<TurbineUserGroupRole> query = Database.query(TurbineUserGroupRole.class);
        Expression exp1 = ExpressionFactory.matchExp(TurbineUserGroupRole.TURBINE_USER_PROPERTY, Integer.valueOf(id));
        Expression exp2 = ExpressionFactory.greaterExp(TurbineUserGroupRole.TURBINE_GROUP_PROPERTY, Integer.valueOf(3));
        Expression exp3 = ExpressionFactory.matchExp(TurbineUserGroupRole.TURBINE_GROUP_PROPERTY + "." + TurbineGroup.OWNER_ID_PROPERTY, Integer.valueOf(1));
        query.setQualifier(exp1);
        query.andQualifier(exp2);
        query.andQualifier(exp3);
        List<TurbineUserGroupRole> list = query.fetchList();
        List<Integer> postIds = new ArrayList<Integer>();
        for (TurbineUserGroupRole role : list) {
            postIds.add(role.getTurbineGroup().getEipMPost().getPostId());
        }
        return postIds;
    }

    /**
   * アクセス権限をチェックします（ポートレットカスタマイズ）
   * 
   * @return
   */
    public static boolean CheckAclPermissionForCustomize(RunData rundata, Context context, int defineAclType) {
        try {
            if (defineAclType == 0) {
                return true;
            }
            boolean hasAuthority = getHasAuthority(rundata, context, defineAclType);
            if (!hasAuthority) {
                throw new ALPermissionException();
            }
            return true;
        } catch (ALPermissionException e) {
            ALEipUtils.redirectPermissionError(rundata);
            return false;
        }
    }

    public static boolean getHasAuthority(RunData rundata, Context context, int defineAclType) {
        String pfeature = ALAccessControlConstants.POERTLET_FEATURE_PORTLET_CUSTOMIZE;
        if (pfeature == null || "".equals(pfeature)) {
            return true;
        }
        ALAccessControlFactoryService aclservice = (ALAccessControlFactoryService) ((TurbineServices) TurbineServices.getInstance()).getService(ALAccessControlFactoryService.SERVICE_NAME);
        ALAccessControlHandler aclhandler = aclservice.getAccessControlHandler();
        return aclhandler.hasAuthority(ALEipUtils.getUserId(rundata), pfeature, defineAclType);
    }

    public static int getLimitUsers() {
        try {
            List<AipoLicense> list = Database.query(AipoLicense.class).select(AipoLicense.LIMIT_USERS_COLUMN).fetchList();
            if (list != null && list.size() > 0) {
                AipoLicense record = list.get(0);
                Integer result = record.getLimitUsers();
                return result.intValue();
            }
        } catch (Exception e) {
            logger.error("Exception", e);
        }
        return 0;
    }

    /**
   * 現在登録されている有効なユーザー数(システムユーザ、論理削除は除く)を取得します。
   * 
   * @return
   */
    public static int getCurrentUserNumEnabledOnly(RunData rundata) {
        int registeredUserNum = -1;
        try {
            Expression exp = ExpressionFactory.matchExp(TurbineUser.DISABLED_PROPERTY, "F");
            List<TurbineUser> list = Database.query(TurbineUser.class, exp).fetchList();
            if (list == null || list.size() <= 0) {
                return -1;
            }
            int size = list.size();
            registeredUserNum = size - 2;
        } catch (Exception ex) {
            logger.error("ユーザー情報をDBから取得できませんでした。");
            logger.error("[ALEipUtils]", ex);
            return -1;
        }
        return registeredUserNum;
    }

    /**
   * 現在登録されている有効なユーザー数(システムユーザ、論理削除、無効化は除く)を取得します。
   * 
   * @return
   */
    public static int getCurrentUserNum(RunData rundata) {
        int registeredUserNum = -1;
        try {
            Expression exp = ExpressionFactory.noMatchExp(TurbineUser.DISABLED_PROPERTY, "T");
            List<TurbineUser> list = Database.query(TurbineUser.class, exp).fetchList();
            if (list == null || list.size() <= 0) {
                return -1;
            }
            int size = list.size();
            registeredUserNum = size - 2;
        } catch (Exception ex) {
            logger.error("ユーザー情報をDBから取得できませんでした。");
            logger.error("[ALEipUtils]", ex);
            return -1;
        }
        return registeredUserNum;
    }

    /**
   * 指定されたユーザが管理者権限を持っているかを返します。
   * 
   * @param uid
   * @return
   */
    public static boolean isAdmin(int uid) {
        boolean res = false;
        try {
            Role adminrole = JetspeedSecurity.getRole("admin");
            TurbineUserGroupRole role = Database.query(TurbineUserGroupRole.class).where(Operations.eq(TurbineUserGroupRole.TURBINE_ROLE_PROPERTY, adminrole.getId()), Operations.eq(TurbineUserGroupRole.TURBINE_USER_PROPERTY, uid)).fetchSingle();
            res = role != null;
        } catch (JetspeedSecurityException e) {
            logger.error("管理者ロールが存在しません。");
            logger.error("[ALEipUtils]", e);
        }
        return res;
    }

    /**
   * ログインユーザが管理者権限を持っているかを返します。
   * 
   * @param rundata
   * @return
   */
    public static boolean isAdmin(RunData rundata) {
        JetspeedRunData jdata = (JetspeedRunData) rundata;
        return isAdmin(Integer.valueOf(jdata.getUserId()));
    }

    /**
   * is LoginUser Admin ?
   */
    public static boolean isAdminUser(RunData runData) {
        int userId = getUserId(runData);
        return isAdminUser(userId);
    }

    public static boolean isAdminUser(int userId) {
        return userId == 1;
    }

    public static String getLoginName(RunData runData) {
        JetspeedRunData jdata = (JetspeedRunData) runData;
        return jdata.getJetspeedUser().getUserName();
    }

    /**
   * Dateに対して整形されたALDateTimeFieldを返します。
   * 
   * @param date
   * @return 整形されたALDateTimeField
   */
    public static ALDateTimeField getFormattedTime(Date date) {
        Calendar Now = new GregorianCalendar();
        Now.setTime(new Date());
        Calendar Time = new GregorianCalendar();
        Time.setTime(date);
        ALDateTimeField rtn;
        rtn = (Now.get(Calendar.YEAR) == Time.get(Calendar.YEAR)) ? (Now.get(Calendar.MONTH) == Time.get(Calendar.MONTH) && Now.get(Calendar.DATE) == Time.get(Calendar.DATE) ? new ALDateTimeField("H:mm") : new ALDateTimeField("M月d日")) : new ALDateTimeField("yyyy年M月d日");
        rtn.setValue(date);
        return rtn;
    }

    public static ALDateTimeField getFormattedTime(ALDateTimeField timeField) {
        if (!timeField.isNotNullValue()) {
            return null;
        }
        return getFormattedTime(timeField.getValue());
    }

    /**
   * 指定したユーザのPSMLにシステム管理のページを追加します。
   * 
   * @param user_name
   * @throws Exception
   */
    public static void addAdminPage(String user_name) throws Exception {
        ProfileLocator locator = Profiler.createLocator();
        locator.createFromPath(String.format("user/%s/media-type/html", user_name));
        ALEipManager.getInstance().removeProfile(locator);
        Profile profile = Profiler.getProfile(locator);
        Portlets portlets = profile.getDocument().getPortlets();
        if (portlets != null) {
            long max_position = 0;
            int portlet_size = portlets.getPortletsCount();
            for (int i = 0; i < portlet_size; i++) {
                Portlets p = portlets.getPortlets(i);
                if (p.getSecurityRef().getParent().equals("admin-view")) {
                    return;
                }
                if (p.getLayout().getPosition() > max_position) {
                    max_position = p.getLayout().getPosition();
                }
            }
            Layout newLayout = new PsmlLayout();
            newLayout.setPosition(max_position + 1);
            newLayout.setSize(-1);
            ProfileLocator admin_locator = Profiler.createLocator();
            admin_locator.createFromPath("user/admin/media-type/html");
            Portlets admin_portlets = Profiler.createProfile(admin_locator).getDocument().getPortlets();
            admin_portlets = admin_portlets.getPortlets(0);
            Entry[] entriesArray = admin_portlets.getEntriesArray();
            int size = entriesArray.length;
            for (int i = 0; i < size; i++) {
                if (entriesArray[i].getParent().equals("DeleteSample")) {
                    admin_portlets.removeEntry(i);
                    break;
                }
            }
            admin_portlets.setLayout(newLayout);
            portlets.addPortlets(admin_portlets);
            PsmlManager.store(profile);
        }
    }
}
