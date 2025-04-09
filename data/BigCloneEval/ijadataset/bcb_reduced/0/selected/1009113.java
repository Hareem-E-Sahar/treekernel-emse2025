package com.avatal.persistency.dao.group;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.avatal.DatabaseTableConstants;
import com.avatal.persistency.dao.base.BaseDAO;
import com.avatal.persistency.dao.exception.DAOException;
import com.avatal.vo.rightmanagement.RightProfileVo;
import com.avatal.vo.user.GroupVo;
import com.avatal.vo.user.PersonVo;
import com.avatal.vo.user.UserVo;

/**
 * @author m0550
 *
 * Created on 24.06.2003
 */
public class RDBMSGroupDAO extends BaseDAO implements GroupDAO {

    private ArrayList groups;

    private static RDBMSGroupDAO instance = null;

    /**
     * @throws DAOException
     */
    public RDBMSGroupDAO() throws DAOException {
        super();
    }

    public static RDBMSGroupDAO getInstance() throws DAOException {
        if (instance == null) {
            instance = new RDBMSGroupDAO();
        }
        return instance;
    }

    /**
     * Gibt alle Gruppen zurueck, die einen schluessel zu einem schloss basierend auf diesem 
     * rechteprofil besitzen
     * @param rightProfileVo
     * @return
     * @throws DAOException
     */
    public ArrayList getAllGroups(RightProfileVo rightProfileVo) throws DAOException {
        ArrayList groups = new ArrayList();
        try {
            this.acquire();
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT Id ");
            sql.append(",Comments ");
            sql.append(",Group_Name ");
            sql.append(",Group_Type ");
            sql.append(",Description ");
            sql.append(",Org_Unit ");
            sql.append(",Org_Type ");
            sql.append(",Email ");
            sql.append(",Url ");
            sql.append(",Object_State ");
            sql.append(",Time_Modified ");
            sql.append(",Time_Created ");
            sql.append(",Time_Frame_Begin ");
            sql.append(",Time_Frame_End ");
            sql.append(",Time_Frame_Admin_Period ");
            sql.append("FROM ").append(DatabaseTableConstants.GROUP_TABLE).append(" g,");
            sql.append(DatabaseTableConstants.GROUP_ACCESS_LOCK_KEY_TABLE).append(" a,");
            sql.append(DatabaseTableConstants.ACCESS_LOCK_KEY_TABLE).append(" b,");
            sql.append(DatabaseTableConstants.ACCESS_LOCK_TABLE).append(" c,");
            sql.append(DatabaseTableConstants.RIGHT_PROFILE_TABLE).append(" r");
            sql.append("WHERE g.Id = a.Group_Id AND a.Access_Lock_Key_Id = b.Id");
            sql.append("AND b.Access_Lock_Id = c.Id AND c.Right_Profile_Id = r.Id");
            sql.append("AND r.Title=").append(rightProfileVo.getTitle()).append(";");
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(sql.toString());
            while (result.next()) {
                GroupVo group = new GroupVo();
                group.setId(new Integer(result.getString(1)));
                group.setComments(result.getString(2));
                group.setGroupName(result.getString(3));
                group.setGroupType(result.getString(4));
                group.setDescription(result.getString(5));
                group.setOrgUnit(result.getString(6));
                group.setOrgType(result.getString(7));
                group.setEmail(result.getString(8));
                group.setUrl(result.getString(9));
                group.setObjectState(new Integer(result.getString(10)));
                group.setTimeModified((Date) result.getTimestamp(11));
                group.setTimeCreated((Date) result.getTimestamp(12));
                group.setTimeFrameBegin((Date) result.getTimestamp(13));
                group.setTimeFrameEnd((Date) result.getTimestamp(14));
                group.setTimeFrameAdminPeriod(result.getString(15));
                groups.add(group);
            }
            statement.close();
            result.close();
        } catch (Exception e) {
            throw new DAOException();
        } finally {
            try {
                this.release();
            } catch (Exception e) {
                System.out.println("Exception releasing connection !" + e.toString());
            }
        }
        return groups;
    }

    /**
	* @author Thomas Fuhrmann
	* @date 11.03.2004
	*
	* �nderung: Benutzereingabenspezifische Generierung des SQL-Strings
	* 
	*/
    public Collection getAllGroupsByNameAndState(String groupName, Integer state) throws DAOException {
        ArrayList groups = new ArrayList();
        try {
            StringBuffer sql = new StringBuffer();
            StringBuffer sqlZusatz = new StringBuffer();
            this.acquire();
            sql.append("SELECT id ");
            sql.append(",Comments ");
            sql.append(",Group_Name ");
            sql.append(",Group_Type ");
            sql.append(",Description ");
            sql.append(",Org_Unit ");
            sql.append(",Org_Type ");
            sql.append(",Email ");
            sql.append(",Url ");
            sql.append(",Object_State ");
            sql.append(",Time_Modified ");
            sql.append(",Time_Created ");
            sql.append(",Time_Frame_Begin ");
            sql.append(",Time_Frame_End ");
            sql.append(",Time_Frame_Admin_Period ");
            sql.append(",Org_Name ");
            sql.append("FROM ").append(DatabaseTableConstants.GROUP_TABLE);
            sql.append(" WHERE");
            if (state != null) {
                sqlZusatz.append(" AND Object_State = ").append(state);
            } else {
                sqlZusatz.append(" AND (Object_State = '0' OR Object_State = '1')");
            }
            if (groupName != null) {
                if (!(groupName.equals("*") || groupName.equals("") || groupName.startsWith("&") || groupName.endsWith("&") || groupName.startsWith(" ") || groupName.endsWith(" "))) {
                    groupName = groupName.replace('*', '%');
                    Pattern p = Pattern.compile("&");
                    Matcher m = p.matcher(groupName);
                    if (m.find()) {
                        String groupAnf = groupName.substring(0, m.start()).trim();
                        String gross = groupAnf.substring(0, 1).toUpperCase() + groupAnf.substring(1, groupAnf.length());
                        String klein = groupAnf.substring(0, 1).toLowerCase() + groupAnf.substring(1, groupAnf.length());
                        if (groupAnf.length() >= 1) {
                            sql.append(" Group_Name like '").append(gross).append("%'").append(sqlZusatz);
                            sql.append(" OR Group_Name like '").append(klein).append("%'").append(sqlZusatz);
                        } else {
                            sql.append(" Group_Name like '%'").append(sqlZusatz);
                        }
                        groupName = groupName.substring(m.end(), groupName.length()).trim();
                        m = p.matcher(groupName);
                        while (m.find()) {
                            groupAnf = groupName.substring(0, m.start()).trim();
                            if (groupAnf.length() >= 1) {
                                gross = groupAnf.substring(0, 1).toUpperCase() + groupAnf.substring(1, groupAnf.length());
                                klein = groupAnf.substring(0, 1).toLowerCase() + groupAnf.substring(1, groupAnf.length());
                                sql.append(" OR Group_Name like '").append(gross).append("%'").append(sqlZusatz);
                                sql.append(" OR Group_Name like '").append(klein).append("%'").append(sqlZusatz);
                            } else {
                                sql.append(" OR Group_Name like '%'").append(sqlZusatz);
                            }
                            groupName = groupName.substring(m.end(), groupName.length()).trim();
                            m = p.matcher(groupName);
                        }
                        if (groupName.length() >= 1) {
                            gross = groupName.substring(0, 1).toUpperCase() + groupName.substring(1, groupName.length());
                            klein = groupName.substring(0, 1).toLowerCase() + groupName.substring(1, groupName.length());
                            sql.append(" OR Group_Name like '").append(gross).append("%'").append(sqlZusatz);
                            sql.append(" OR Group_Name like '").append(klein).append("%'").append(sqlZusatz);
                        } else {
                            sql.append(" OR Group_Name like '%'").append(sqlZusatz);
                        }
                    } else {
                        String gross = groupName.substring(0, 1).toUpperCase() + groupName.substring(1, groupName.length());
                        String klein = groupName.substring(0, 1).toLowerCase() + groupName.substring(1, groupName.length());
                        if (groupName.length() >= 1) {
                            sql.append(" Group_Name like '").append(gross).append("%'").append(sqlZusatz);
                            sql.append(" OR Group_Name like '").append(klein).append("%'").append(sqlZusatz);
                        } else {
                            sql.append(" OR Group_Name like '%'").append(sqlZusatz);
                        }
                    }
                } else {
                    sql.append(" Group_Name like '%'").append(sqlZusatz);
                }
            } else {
                sql.append(" (Group_Name like '%'").append(sqlZusatz);
            }
            sql.append(" order by Group_Name");
            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(sql.toString());
            while (result.next()) {
                GroupVo group = new GroupVo();
                group.setId(new Integer(result.getString(1)));
                group.setComments(result.getString(2));
                group.setGroupName(result.getString(3));
                group.setGroupType(result.getString(4));
                group.setDescription(result.getString(5));
                group.setOrgUnit(result.getString(6));
                group.setOrgType(result.getString(7));
                group.setEmail(result.getString(8));
                group.setUrl(result.getString(9));
                group.setObjectState(new Integer(result.getString(10)));
                group.setTimeModified((Date) result.getTimestamp(11));
                group.setTimeCreated((Date) result.getTimestamp(12));
                group.setTimeFrameBegin((Date) result.getTimestamp(13));
                group.setTimeFrameEnd((Date) result.getTimestamp(14));
                group.setTimeFrameAdminPeriod(result.getString(15));
                group.setOrgName(result.getString(16));
                groups.add(group);
            }
            statement.close();
            result.close();
        } catch (Exception e) {
            throw new DAOException();
        } finally {
            try {
                this.release();
            } catch (Exception e) {
                System.out.println("Exception releasing connection !" + e.toString());
            }
        }
        return groups;
    }

    /**
	* @author Thomas Fuhrmann
	* @date 11.03.2004
	*
	* �nderung: Benutzereingabenspezifische Generierung des SQL-Strings
	* 
	*/
    public Collection getGroupsAndUserByName(String groupName, Integer state) throws DAOException {
        ArrayList groups = new ArrayList();
        try {
            StringBuffer sql = new StringBuffer();
            StringBuffer sqlZusatz = new StringBuffer();
            this.acquire();
            sql.append("SELECT Id ");
            sql.append(",Group_Name ");
            sql.append("FROM ").append(DatabaseTableConstants.GROUP_TABLE);
            sql.append(" WHERE");
            if (state != null) {
                sqlZusatz.append(" AND Object_State = ").append(state);
            } else {
                sqlZusatz.append(" AND (Object_State = '0' OR Object_State = '1')");
            }
            if (groupName != null) {
                if (!(groupName.equals("*") || groupName.equals(""))) {
                    groupName = groupName.replace('*', '%');
                    Pattern p = Pattern.compile("&");
                    Matcher m = p.matcher(groupName);
                    if (m.find()) {
                        String groupAnf = groupName.substring(0, m.start()).trim();
                        String gross = groupAnf.substring(0, 1).toUpperCase() + groupAnf.substring(1, groupAnf.length());
                        String klein = groupAnf.substring(0, 1).toLowerCase() + groupAnf.substring(1, groupAnf.length());
                        if (groupAnf.length() >= 1) {
                            sql.append(" Group_Name like '").append(gross).append("%'").append(sqlZusatz);
                            sql.append(" OR Group_Name like '").append(klein).append("%'").append(sqlZusatz);
                        }
                        groupName = groupName.substring(m.end(), groupName.length()).trim();
                        m = p.matcher(groupName);
                        while (m.find()) {
                            groupAnf = groupName.substring(0, m.start()).trim();
                            gross = groupAnf.substring(0, 1).toUpperCase() + groupAnf.substring(1, groupAnf.length());
                            klein = groupAnf.substring(0, 1).toLowerCase() + groupAnf.substring(1, groupAnf.length());
                            if (groupAnf.length() >= 1) {
                                sql.append(" OR Group_Name like '").append(gross).append("%'").append(sqlZusatz);
                                sql.append(" OR Group_Name like '").append(klein).append("%'").append(sqlZusatz);
                            }
                            groupName = groupName.substring(m.end(), groupName.length()).trim();
                            m = p.matcher(groupName);
                        }
                        gross = groupName.substring(0, 1).toUpperCase() + groupName.substring(1, groupName.length());
                        klein = groupName.substring(0, 1).toLowerCase() + groupName.substring(1, groupName.length());
                        if (groupName.length() >= 1) {
                            sql.append(" OR Group_Name like '").append(gross).append("%'").append(sqlZusatz);
                            sql.append(" OR Group_Name like '").append(klein).append("%'").append(sqlZusatz);
                        }
                    } else {
                        String gross = groupName.substring(0, 1).toUpperCase() + groupName.substring(1, groupName.length());
                        String klein = groupName.substring(0, 1).toLowerCase() + groupName.substring(1, groupName.length());
                        if (groupName.length() >= 1) {
                            sql.append(" Group_Name like '").append(gross).append("%'").append(sqlZusatz);
                            sql.append(" OR Group_Name like '").append(klein).append("%'").append(sqlZusatz);
                        }
                    }
                } else {
                    sql.append(" Group_Name like '%'").append(sqlZusatz);
                }
            } else {
                sql.append(" Group_Name like '%'").append(sqlZusatz);
            }
            sql.append(" order by Group_Name");
            Statement statement = getConnection().createStatement();
            String s = sql.toString();
            ResultSet result = statement.executeQuery(s);
            while (result.next()) {
                GroupVo group = new GroupVo();
                group.setId(new Integer(result.getString(1)));
                group.setGroupName(result.getString(2));
                groups.add(group);
            }
            statement.close();
            result.close();
            StringBuffer userSearch = new StringBuffer();
            userSearch.append("SELECT u.Id ");
            userSearch.append(",u.Login ");
            userSearch.append(",u.Object_State ");
            userSearch.append(",p.id ");
            userSearch.append(",p.Name ");
            userSearch.append(",p.First_Name ");
            userSearch.append("FROM ").append(DatabaseTableConstants.USER_TABLE).append(" u, ");
            userSearch.append(DatabaseTableConstants.PERSON_TABLE).append(" p ");
            userSearch.append(",").append(DatabaseTableConstants.USER_GROUP_TABLE).append(" up ");
            userSearch.append("WHERE u.Person_Id = p.id");
            userSearch.append(" AND u.Id = up.User_Id AND up.Group_Id = ?");
            userSearch.append(" order by p.Name");
            String us = userSearch.toString();
            PreparedStatement prestmt = getConnection().prepareStatement(userSearch.toString());
            for (Iterator it = groups.iterator(); it.hasNext(); ) {
                GroupVo group = (GroupVo) it.next();
                ArrayList users = new ArrayList();
                prestmt.setInt(1, group.getId().intValue());
                ResultSet res = prestmt.executeQuery();
                while (res.next()) {
                    UserVo user = new UserVo();
                    PersonVo person = new PersonVo();
                    user.setId(new Integer(res.getInt(1)));
                    user.setLogin(res.getString(2));
                    user.setObjectState(new Integer(res.getInt(3)));
                    person.setId(new Integer(res.getInt(4)));
                    person.setName(res.getString(5));
                    person.setFirstName(res.getString(6));
                    user.setPerson(person);
                    users.add(user);
                    group.setUser(users);
                }
                res.close();
            }
            prestmt.close();
        } catch (Exception e) {
            throw new DAOException();
        } finally {
            try {
                this.release();
            } catch (Exception e) {
                System.out.println("Exception releasing connection !" + e.toString());
            }
        }
        return groups;
    }

    public String getSourceType() {
        return null;
    }

    public ArrayList getMajorGroupTree(Integer groupId, ArrayList majorGroupList) throws DAOException {
        Integer majorGroup = null;
        try {
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT Major_Group_Id from ").append(DatabaseTableConstants.GROUP_TABLE);
            sql.append(" where id=").append(groupId);
            this.acquire();
            Statement statement = getConnection().createStatement();
            String query = sql.toString();
            ResultSet result = statement.executeQuery(query);
            while (result.next()) {
                majorGroup = new Integer(result.getInt(1));
            }
            result.close();
            statement.close();
        } catch (Exception e) {
            throw new DAOException();
        } finally {
            try {
                this.release();
            } catch (Exception e) {
                System.out.println("Exception releasing connection !" + e.toString());
            }
        }
        if (majorGroup.intValue() != 0) {
            majorGroupList.add(majorGroup);
            getMajorGroupTree(majorGroup, majorGroupList);
            return majorGroupList;
        } else {
            return majorGroupList;
        }
    }
}
