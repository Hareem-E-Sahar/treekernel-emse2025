package ces.platform.infoplat.core.dao;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import ces.coral.dbo.DBOperation;
import ces.coral.file.FileOperation;
import ces.platform.infoplat.core.Site;
import ces.platform.infoplat.core.base.BaseDAO;
import ces.platform.infoplat.core.base.ConfigInfo;
import ces.platform.infoplat.core.tree.TreeNode;
import ces.platform.system.common.Constant;
import ces.platform.system.common.XmlInfo;

/**
 * @author mysheros
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class BakAndRecoverDAO extends BaseDAO {

    private static final String dataDIR = ConfigInfo.getInstance().getInfoplatDataDir();

    private static final String BAKFOLDER_PREFIX = dataDIR.substring(0, dataDIR.substring(0, dataDIR.length() - 1).lastIndexOf("/")) + "/pigeonhole";

    ;

    private static final String SITERESBAKFOLDER = "site";

    private static final String DOCRESBAKFOLDER = "doc";

    private static final String BAKRES_PREFIX = "";

    private static final String BAKCHANNELRES_PREFIX = "";

    private static final String BAKDOCSFILE = "bakdoc.sql";

    private static final String BAKBROWFILE = "bakbrow.sql";

    private static final String BAKRESFILE = "bakres.sql";

    private static final String SPLIT_TAG = ",";

    private static final String DATA_SPLIT_TAG = "~~";

    private List docIdList = null;

    private HashSet channelSet = new HashSet();

    private String dir = "";

    private static final String SEL_DOC = "select id,acty_inst_id,doctype_path,channel_path," + "content_file,attach_status,year_no,periodical_no,word_no,title,title_color," + "sub_title,author,emit_date,emit_unit,editor_remark,keywords,pertinent_words," + "abstract_words,source_id,security_level_id,creater,create_date,lastest_modify_date," + "remark_prop,notes,workflow_id,reservation1,reservation2,reservation3,reservation4," + "reservation5,reservation6,hyperlink from t_ip_doc where ";

    private static final String INS_DOC = " insert into t_ip_doc (id,acty_inst_id,doctype_path,channel_path," + "content_file,attach_status,year_no,periodical_no,word_no,title,title_color," + "sub_title,author,emit_date,emit_unit,editor_remark,keywords,pertinent_words," + "abstract_words,source_id,security_level_id,creater,create_date,lastest_modify_date," + "remark_prop,notes,workflow_id,reservation1,reservation2,reservation3,reservation4," + "reservation5,reservation6,hyperlink) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

    private static final String SEL_BROWSE = "select doc_id,channel_path,doctype_path,publisher,publish_date," + "order_no,valid_startdate,valid_enddate,syn_status,content_file,attach_status,year_no," + "periodical_no,word_no,title,title_color,sub_title,author,emit_date,emit_unit,editor_remark," + "keywords,pertinent_words,abstract_words,source_id,security_level_id,creater,create_date," + "lastest_modify_date,remark_prop,notes,reservation1,reservation2,reservation3,reservation4," + "reservation5,reservation6,hyperlink from t_ip_browse  where ";

    private static final String INS_BROWSE = "insert into t_ip_browse (doc_id,channel_path,doctype_path,publisher,publish_date," + "order_no,valid_startdate,valid_enddate,syn_status,content_file,attach_status,year_no," + "periodical_no,word_no,title,title_color,sub_title,author,emit_date,emit_unit,editor_remark," + "keywords,pertinent_words,abstract_words,source_id,security_level_id,creater,create_date," + "lastest_modify_date,remark_prop,notes,reservation1,reservation2,reservation3,reservation4," + "reservation5,reservation6,hyperlink) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";

    private static final String SEL_DOC_RES = "select id,doc_id,type,uri,original_file,file_size,file_ext,autoplay,order_no,creater,create_date from t_ip_doc_res where  ";

    private static final String INS_DOC_RES = "insert into t_ip_doc_res (id,doc_id,type,uri,original_file,file_size,file_ext,autoplay,order_no,creater,create_date) values (?,?,?,?,?,?,?,?,?,?,?)";

    private DBOperation dbo = null;

    private Connection con = null;

    private PreparedStatement pstm = null;

    private ResultSet rs = null;

    private void openConnection() throws Exception {
        try {
            if (dbo == null) {
                dbo = createDBOperation();
            }
            if (con == null) {
                con = dbo.getConnection();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void close() throws Exception {
        try {
            if (rs != null) {
                rs.close();
            }
            if (pstm != null) {
                pstm.close();
            }
            if (con != null) {
                con.close();
            }
            if (dbo != null) {
                dbo.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void startTrans(boolean flag) throws Exception {
        try {
            if (con != null) {
                con.setAutoCommit(flag);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void commit() throws Exception {
        try {
            if (con != null) {
                con.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void rollback() throws Exception {
        try {
            if (con != null) {
                con.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * ���Թ鵵��ݻָ�
     */
    public static void main(String args[]) {
        try {
            new BakAndRecoverDAO().doRecovery("D:/pigeonhole/200509/" + BAKDOCSFILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * �鵵�ָ�
     * @param sourcePath
     * @throws Exception
     */
    private void recoverFile(String sourcePath) throws Exception {
        String toSiteFileDir = ConfigInfo.getInstance().getInfoplatDataDir() + "pub/";
        String toDocFileDir = ConfigInfo.getInstance().getInfoplatDataDir() + "workflow/docs/";
        File fileSite = new File(sourcePath + "/" + SITERESBAKFOLDER);
        if (fileSite.exists()) {
            FileOperation.copyDir(sourcePath + "/" + SITERESBAKFOLDER, toSiteFileDir);
        }
        File fileDoc = new File(sourcePath + "/" + DOCRESBAKFOLDER);
        if (fileDoc.exists()) {
            FileOperation.copyDir(sourcePath + "/" + DOCRESBAKFOLDER, toDocFileDir);
        }
    }

    /**
     * ִ�лָ�
     * @param filePath
     * @throws Exception
     */
    public void doRecovery(String filePath) throws Exception {
        DataInputStream dis = null;
        filePath = this.fullFillDate(filePath);
        filePath = filePath.substring(0, 4) + filePath.substring(5);
        filePath = BAKFOLDER_PREFIX + "/" + filePath + "/";
        String sFilePath = filePath + BAKDOCSFILE;
        try {
            File file = new File(sFilePath);
            if (!file.exists()) {
                throw new Exception("�鵵�ָ��ļ������ڻ����������ָ������飡");
            }
            File parent = file.getParentFile();
            String parentPath = parent.getAbsolutePath();
            openConnection();
            startTrans(false);
            dis = getDIS(sFilePath);
            String obj = null;
            if ((obj = dis.readLine()) != null) {
                obj = obj.trim() + "  ";
                obj = new String(obj.getBytes("ISO-8859-1"));
                pstm = con.prepareStatement(INS_DOC);
                while ((obj = dis.readLine()) != null) {
                    obj = obj.trim() + "  ";
                    obj = new String(obj.getBytes("ISO-8859-1"));
                    obj = obj.substring(5);
                    setDocPstm(obj);
                    pstm.executeUpdate();
                }
            }
            dis.close();
            sFilePath = filePath + BAKBROWFILE;
            dis = getDIS(sFilePath);
            if ((obj = dis.readLine()) != null) {
                obj = obj.trim() + "  ";
                obj = new String(obj.getBytes("ISO-8859-1"));
                pstm = con.prepareStatement(INS_BROWSE);
                while ((obj = dis.readLine()) != null) {
                    obj = obj.trim() + "  ";
                    obj = new String(obj.getBytes("ISO-8859-1"));
                    obj = obj.substring(5);
                    setBrowsePstm(obj);
                    pstm.executeUpdate();
                }
            }
            dis.close();
            sFilePath = filePath + BAKRESFILE;
            dis = getDIS(sFilePath);
            if ((obj = dis.readLine()) != null) {
                obj = obj.trim() + "  ";
                obj = new String(obj.getBytes("ISO-8859-1"));
                pstm = con.prepareStatement(INS_DOC_RES);
                while ((obj = dis.readLine()) != null && !"".equals(obj.trim())) {
                    obj = obj.trim() + "  ";
                    obj = new String(obj.getBytes("ISO-8859-1"));
                    obj = obj.substring(5);
                    setResPstm(obj);
                    pstm.executeUpdate();
                }
            }
            dis.close();
            recoverFile(parentPath);
            destroyFile(parentPath);
            commit();
            close();
        } catch (Exception e) {
            if (dis != null) {
                dis.close();
            }
            rollback();
            close();
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * �ָ���Ϻ�ɾ��鵵�ļ�
     * @param parentPath
     * @throws Exception
     */
    private void destroyFile(String parentPath) throws Exception {
        File file = new File(parentPath);
        if (file.exists()) {
            FileOperation.recursiveRemoveDir(file);
        }
    }

    /**
     * ����t_ip_doc�Ļָ�
     * @param value
     * @throws Exception
     */
    private void setDocPstm(String value) throws Exception {
        String[] obj = convertStrToArray(value, DATA_SPLIT_TAG);
        pstm.setInt(1, Integer.parseInt(obj[0]));
        pstm.setLong(2, Long.parseLong(obj[1]));
        for (int i = 3; i <= 6; i++) {
            pstm.setString(i, obj[i - 1]);
        }
        for (int i = 7; i <= 9; i++) {
            if (null == obj[i - 1] || "".equals(obj[i - 1])) {
                pstm.setNull(i, Types.INTEGER);
            } else {
                pstm.setInt(i, Integer.parseInt(obj[i - 1]));
            }
        }
        for (int i = 10; i <= 13; i++) {
            pstm.setString(i, obj[i - 1]);
        }
        pstm.setTimestamp(14, convertStrToDate(obj[14 - 1]));
        for (int i = 15; i <= 19; i++) {
            pstm.setString(i, obj[i - 1]);
        }
        for (int i = 20; i <= 22; i++) {
            if (null == obj[i - 1] || "".equals(obj[i - 1])) {
                pstm.setNull(i, Types.INTEGER);
            } else {
                pstm.setInt(i, Integer.parseInt(obj[i - 1]));
            }
        }
        pstm.setTimestamp(23, convertStrToDate(obj[23 - 1]));
        pstm.setTimestamp(24, convertStrToDate(obj[24 - 1]));
        for (int i = 25; i <= 26; i++) {
            pstm.setString(i, obj[i - 1]);
        }
        if (null == obj[27 - 1] || "".equals(obj[27 - 1])) {
            pstm.setNull(27, Types.INTEGER);
        } else {
            pstm.setInt(27, Integer.parseInt(obj[27 - 1]));
        }
        for (int i = 28; i <= 34; i++) {
            pstm.setString(i, obj[i - 1]);
        }
    }

    /**
     * ����t_ip_browse�Ļָ�
     * @param value
     * @throws Exception
     */
    private void setBrowsePstm(String value) throws Exception {
        String[] obj = convertStrToArray(value, DATA_SPLIT_TAG);
        pstm.setInt(1, Integer.parseInt(obj[1 - 1]));
        pstm.setString(2, obj[2 - 1]);
        pstm.setString(3, obj[3 - 1]);
        pstm.setInt(4, Integer.parseInt(obj[4 - 1]));
        pstm.setTimestamp(5, convertStrToDate(obj[5 - 1]));
        pstm.setInt(6, Integer.parseInt(obj[6 - 1]));
        pstm.setTimestamp(7, convertStrToDate(obj[7 - 1]));
        pstm.setTimestamp(8, convertStrToDate(obj[8 - 1]));
        for (int i = 9; i <= 11; i++) {
            pstm.setString(i, obj[i - 1]);
        }
        for (int i = 12; i <= 14; i++) {
            if (null == obj[i - 1] || "".equals(obj[i - 1])) {
                pstm.setNull(i, Types.INTEGER);
            } else {
                pstm.setInt(i, Integer.parseInt(obj[i - 1]));
            }
        }
        for (int i = 15; i <= 18; i++) {
            pstm.setString(i, obj[i - 1]);
        }
        pstm.setTimestamp(19, convertStrToDate(obj[19 - 1]));
        for (int i = 20; i <= 25; i++) {
            pstm.setString(i, obj[i - 1]);
        }
        for (int i = 26; i <= 27; i++) {
            if (null == obj[i - 1] || "".equals(obj[i - 1])) {
                pstm.setNull(i, Types.INTEGER);
            } else {
                pstm.setInt(i, Integer.parseInt(obj[i - 1]));
            }
        }
        pstm.setTimestamp(28, convertStrToDate(obj[28 - 1]));
        pstm.setTimestamp(29, convertStrToDate(obj[29 - 1]));
        for (int i = 30; i <= 38; i++) {
            pstm.setString(i, obj[i - 1]);
        }
    }

    /**
     * ����t_ip_doc_res�Ļָ�
     * @param value
     * @throws Exception
     */
    private void setResPstm(String value) throws Exception {
        String[] obj = convertStrToArray(value, DATA_SPLIT_TAG);
        for (int i = 1; i <= 10; i++) {
            pstm.setString(i, obj[i - 1]);
        }
        pstm.setTimestamp(11, convertStrToDate(obj[11 - 1]));
    }

    /**
     * ��ݶ�ȡ���ļ�������������
     * @param FilePath
     * @return
     * @throws Exception
     */
    private DataInputStream getDIS(String FilePath) throws Exception {
        try {
            FileInputStream fis = new FileInputStream(FilePath);
            DataInputStream dis = new DataInputStream(fis);
            return dis;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * ����TAG�ָ���Stringת��ΪSring[] 
     * @param token
     * @param tag
     * @return
     */
    private String[] convertStrToArray(String token, String tag) {
        if (token == null || token.trim().equals("")) {
            return new String[0];
        }
        String obj[] = null;
        StringTokenizer st = new StringTokenizer(token, tag);
        obj = new String[st.countTokens()];
        int i = 0;
        int j = 0;
        while (st.hasMoreTokens()) {
            obj[i++] = st.nextToken().trim();
        }
        return obj;
    }

    /**
     * ��String ������ת��ΪTimestamp
     * @param date
     * @return
     * @throws Exception
     */
    private Timestamp convertStrToDate(String date) throws Exception {
        if (date == null || date.equals("")) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date d = (java.util.Date) sdf.parse(date);
            Timestamp sTemp = new java.sql.Timestamp(d.getTime());
            return sTemp;
        } catch (ParseException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * ���й鵵��������������
     * @param bakChannelPath
     * @param startDate
     * @param endDate
     * @throws Exception
     */
    public void generateBakSql(String bakChannelPath, String startDate, String endDate) throws Exception {
        List channellist = convertStrToList(bakChannelPath, SPLIT_TAG);
        List dateList = getBakDateZone(startDate, endDate);
        BufferedOutputStream bos = null;
        try {
            openConnection();
            startTrans(false);
            bos = new BufferedOutputStream(getOS(BAKDOCSFILE));
            writeDoc(bos, startDate, endDate, channellist);
            bos.flush();
            bos.close();
            bos = new BufferedOutputStream(getOS(BAKBROWFILE));
            writeDocByTabName(bos, "t_ip_browse");
            bos.flush();
            bos.close();
            bos = new BufferedOutputStream(getOS(BAKRESFILE));
            writeDocByTabName(bos, "t_ip_doc_res");
            bos.flush();
            bos.close();
            backFile(channellist, dateList);
            commit();
            close();
        } catch (Exception e) {
            rollback();
            close();
            if (bos != null) bos.close();
            clearFile();
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    /**
     * ��ȡ��ʼʱ�������ʱ��֮����м��·�
     * @param startDate
     * @param endDate
     * @return
     */
    private List getBakDateZone(String startDate, String endDate) throws Exception {
        startDate = fullFillDate(startDate);
        endDate = fullFillDate(endDate);
        List list = new ArrayList();
        while (compareDate(startDate, endDate)) {
            list.add(startDate.substring(0, 4) + startDate.substring(5));
            startDate = addMonth(startDate);
        }
        return list;
    }

    /**
     * �ڸ���·��ϼ�һ����
     * @param yyyymm
     * @return
     * @throws Exception
     */
    private String addMonth(String yyyymm) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Timestamp ts = new Timestamp(sdf.parse(yyyymm).getTime());
        Calendar c = Calendar.getInstance();
        c.setTime(ts);
        c.add(Calendar.MONTH, 1);
        SimpleDateFormat sdfStr = new SimpleDateFormat("yyyy-MM");
        return sdfStr.format(new Timestamp(c.getTime().getTime()));
    }

    /**
     * �Ƚ� �����Ƿ�С��Ŀ������
     * @param compareDate
     * @param target
     * @return
     * @throws Exception
     */
    private boolean compareDate(String compareDate, String target) throws Exception {
        compareDate = compareDate.substring(0, 4) + compareDate.substring(5);
        target = target.substring(0, 4) + target.substring(5);
        if (Integer.parseInt(compareDate) <= Integer.parseInt(target)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * �����ļ�ϵͳ
     * @param channelList
     * @param dateList
     * @throws Exception
     */
    private void backFile(List channelList, List dateList) throws Exception {
        HashSet hs = new HashSet();
        for (int i = 0; i < channelList.size(); i++) {
            hs.add(((String) channelList.get(i)).substring(0, 10));
        }
        String[] sitePath = null;
        Iterator iter = null;
        if (hs.size() > 0) {
            iter = hs.iterator();
            sitePath = new String[hs.size()];
        } else if (channelSet.size() > 0) {
            iter = channelSet.iterator();
            sitePath = new String[channelSet.size()];
        } else {
            return;
        }
        int i = 0;
        while (iter.hasNext()) {
            sitePath[i++] = (String) iter.next();
        }
        String fromDocFileDir = ConfigInfo.getInstance().getInfoplatDataDir() + "workflow/docs/";
        String toTargetDir = BAKFOLDER_PREFIX + "/";
        for (int j = 0; j < sitePath.length; j++) {
            TreeNode treeNode = TreeNode.getInstance(sitePath[j]);
            Site site = (Site) treeNode;
            String fromSiteFileDir = ConfigInfo.getInstance().getInfoplatDataDir() + "pub/" + site.getAsciiName() + "/docs/";
            for (int k = 0; k < dateList.size(); k++) {
                if ((new File(fromSiteFileDir + (String) dateList.get(k))).exists()) {
                    FileOperation.copyDir(fromSiteFileDir + (String) dateList.get(k), toTargetDir + dir + "/" + SITERESBAKFOLDER + "/" + site.getAsciiName() + "/docs/" + (String) dateList.get(k));
                }
                if ((new File(fromDocFileDir + (String) dateList.get(k))).exists()) {
                    FileOperation.copyDir(fromDocFileDir + (String) dateList.get(k), toTargetDir + dir + "/" + DOCRESBAKFOLDER + "/" + (String) dateList.get(k));
                }
            }
        }
        if ((new File(fromDocFileDir + "other").exists())) {
            FileOperation.copyDir(fromDocFileDir + "other", toTargetDir + dir + "/" + DOCRESBAKFOLDER + "/other");
        }
    }

    /**
     * ��t_ip_doc���еı������д���ļ���ɾ�������Ӧ���
     * @param fos
     * @param startDate
     * @param endDate
     * @param channelPath
     * @throws Exception
     */
    private void writeDoc(BufferedOutputStream bos, String startDate, String endDate, List channelPath) throws Exception {
        startDate = fullFillDate(startDate);
        int iYear = Integer.parseInt(endDate.substring(0, 4));
        int iMon = Integer.parseInt(endDate.substring(5));
        iMon++;
        if (iMon == 13) {
            iYear++;
            iMon = 1;
        }
        endDate = iYear + "-";
        if (iMon < 10) endDate += "0" + iMon; else endDate += iMon;
        String delDocSql = "delete from t_ip_doc where ";
        StringBuffer condition = new StringBuffer();
        if (XmlInfo.getInstance().getSysDataBaseType().equalsIgnoreCase(Constant.SQLSERVER) || XmlInfo.getInstance().getSysDataBaseType().equalsIgnoreCase(Constant.MYSQL) || XmlInfo.getInstance().getSysDataBaseType().equalsIgnoreCase(Constant.SYBASE)) {
            condition.append(" create_date >= '" + startDate + "-01' and create_date < '" + endDate + "-01' ");
        } else {
            condition.append(" to_char(create_date,'yyyy-mm')  ");
            condition.append(" between '" + startDate + "' and '" + endDate + "' ");
        }
        for (int i = 0; i < channelPath.size(); i++) {
            if (i == 0) {
                condition.append(" and ( channel_path = '" + (String) channelPath.get(i) + "' ");
            } else {
                condition.append(" or  channel_path = '" + (String) channelPath.get(i) + "' ");
            }
            if (i == channelPath.size() - 1) {
                condition.append(" )");
            }
        }
        pstm = con.prepareStatement(SEL_DOC + condition.toString());
        rs = pstm.executeQuery();
        resultSetOperate("t_ip_doc", bos);
        pstm = con.prepareStatement(delDocSql + condition.toString());
        int count = pstm.executeUpdate();
    }

    /**
     * ��ݴ���ı��������ݱ���д���ļ���ɾ�������Ӧ
     * @param fos
     * @throws Exception
     */
    private void writeDocByTabName(BufferedOutputStream bos, String tabName) throws Exception {
        StringBuffer browseSql = new StringBuffer();
        String docId = getDocId();
        if (tabName.equalsIgnoreCase("t_ip_browse")) {
            browseSql.append(SEL_BROWSE);
        } else if (tabName.equalsIgnoreCase("t_ip_doc_res")) {
            browseSql.append(SEL_DOC_RES);
        } else {
            throw new Exception("�鵵ʧ��,�鵵Ŀ��?��ȷ!");
        }
        if (docId.equals("")) {
            browseSql.append("  1=0 ");
        } else {
            browseSql.append(" doc_id in (" + docId + ")");
        }
        pstm = con.prepareStatement(browseSql.toString());
        rs = pstm.executeQuery();
        resultSetOperate(tabName, bos);
        StringBuffer delBrowse = new StringBuffer();
        delBrowse.append("delete from " + tabName + " where ");
        if (docId.equals("")) {
            delBrowse.append("  1=0 ");
        } else {
            delBrowse.append(" doc_id in (" + docId + ")");
        }
        pstm = con.prepareStatement(delBrowse.toString());
        int count = pstm.executeUpdate();
    }

    /**
     * ����ݲ�д���ļ�
     * @param tabName
     * @param fos
     * @throws Exception
     */
    private void resultSetOperate(String tabName, BufferedOutputStream bos) throws Exception {
        String tabPrefix = "";
        if (tabName.equalsIgnoreCase("t_ip_doc")) {
            docIdList = new ArrayList();
            tabPrefix = "doc==";
        } else if (tabName.equalsIgnoreCase("t_ip_browse")) {
            tabPrefix = "brw==";
        } else if (tabName.equalsIgnoreCase("t_ip_doc_res")) {
            tabPrefix = "res==";
        } else {
            throw new Exception("�鵵ʧ��,�鵵Ŀ��?��ȷ!");
        }
        ResultSetMetaData rsmd = rs.getMetaData();
        String[] title = new String[rsmd.getColumnCount()];
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            title[i - 1] = rsmd.getColumnName(i);
        }
        rsmd = null;
        while (rs.next()) {
            if (tabName.equalsIgnoreCase("t_ip_doc")) {
                docIdList.add(rs.getString(1));
            }
            if (tabName.equalsIgnoreCase("t_ip_doc") || tabName.equalsIgnoreCase("t_ip_browse")) {
                channelSet.add(rs.getString("CHANNEL_PATH").substring(0, 10));
            }
            String value = tabPrefix;
            for (int i = 0; i < title.length; i++) {
                if (i == 0) {
                    if (rs.getString(i + 1) == null) {
                        value = value + " ";
                    } else {
                        value = value + rs.getString(i + 1);
                    }
                } else {
                    if (rs.getString(i + 1) == null) {
                        value = value + DATA_SPLIT_TAG + " ";
                    } else {
                        value = value + DATA_SPLIT_TAG + rs.getString(i + 1);
                    }
                }
            }
            bos.write((value + " \n").getBytes());
        }
        rs.close();
        rs = null;
    }

    /**
     * ��2005-4����ת��Ϊ2005-04
     * @param date
     * @return
     */
    private String fullFillDate(String date) {
        if (date.length() == 6) {
            return date.substring(0, 5) + "0" + date.substring(5);
        } else {
            return date;
        }
    }

    /**
     * 
     * @return
     */
    private String getDocId() {
        String docId = "";
        for (int i = 0; i < docIdList.size(); i++) {
            if (i == 0) {
                docId = (String) docIdList.get(i);
            } else {
                docId = docId + "," + (String) docIdList.get(i);
            }
        }
        return docId;
    }

    /**
     * ����splitTag �ָ���source��ֲ�����List
     * @param source
     * @param splitTag
     * @return
     */
    private List convertStrToList(String source, String splitTag) {
        if (source == null || source.trim().equals("")) {
            return new ArrayList();
        }
        List list = new ArrayList();
        StringTokenizer st = new StringTokenizer(source, splitTag);
        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        return list;
    }

    /**
     * ��ȡ�����ļ�������
     * @return
     * @throws Exception
     */
    private OutputStream getOS(String fileName) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        dir = sdf.format(Calendar.getInstance().getTime());
        File file = new File(BAKFOLDER_PREFIX + "/" + dir + "/" + fileName);
        OutputStream os = null;
        try {
            if (!file.exists()) {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                file.createNewFile();
                file = new File(BAKFOLDER_PREFIX + "/" + dir + "/" + fileName);
                os = new FileOutputStream(file, true);
                if (fileName.equalsIgnoreCase(BAKDOCSFILE)) {
                    os.write(("docSQL=" + INS_DOC + " \n ").getBytes());
                } else if (fileName.equalsIgnoreCase(BAKBROWFILE)) {
                    os.write(("brwSQL=" + INS_BROWSE + " \n ").getBytes());
                } else if (fileName.equalsIgnoreCase(BAKRESFILE)) {
                    os.write(("resSQL=" + INS_DOC_RES + " \n ").getBytes());
                }
            } else {
                file = new File(BAKFOLDER_PREFIX + "/" + dir + "/" + fileName);
                os = new FileOutputStream(file, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
        return os;
    }

    /**
     * �������ʧ��,��ɾ����ļ�
     * @throws Exception
     */
    private void clearFile() throws Exception {
        File file = new File(BAKFOLDER_PREFIX + "/" + dir + "/" + BAKDOCSFILE);
        if (file.exists()) {
            file.delete();
        }
        file = new File(BAKFOLDER_PREFIX + "/" + dir + "/" + BAKBROWFILE);
        if (file.exists()) {
            file.delete();
        }
        file = new File(BAKFOLDER_PREFIX + "/" + dir + "/" + BAKRESFILE);
        if (file.exists()) {
            file.delete();
        }
    }
}
