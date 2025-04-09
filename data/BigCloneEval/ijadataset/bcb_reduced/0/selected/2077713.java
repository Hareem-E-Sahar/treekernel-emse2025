package org.docsfree.xlsplugins;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.zip.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.extentech.ExtenXLS.CellHandle;
import com.extentech.ExtenXLS.ExtenXLS;
import com.extentech.ExtenXLS.NameHandle;
import org.docsfree.core.DocumentLoader;
import org.docsfree.core.WorkBookCommander;
import org.docsfree.legacy.auth.AclEntry;
import org.docsfree.legacy.auth.User;
import org.docsfree.plugin.AbstractPlugin;
import org.docsfree.plugin.RestCall;
import org.docsfree.plugin.RestTest;
import com.extentech.ExtenXLS.web.MemeDocument;
import com.extentech.ExtenXLS.web.MemeWorkBook;
import com.extentech.luminet.*;
import com.extentech.toolkit.*;

/**
 * System plugin provides app-server instance level functions.
 * 
 */
public class PluginSystem extends AbstractPlugin {

    private Serve serve = null;

    public void setServe(Serve sy) {
        serve = sy;
    }

    public String getPluginName() {
        return "system";
    }

    /** return statistics for loaded workbooks
     * 
     * 
     * @author John [ Oct 22, 2007 ]
     * @return
     */
    public String getworkbookstats() {
        return "not implemented";
    }

    /**
     * returns a delimited list of performance counter values
     * 
     * @return list of counters
     */
    public String getcounters() {
        String retdata = "";
        Hashtable counters = serve.getCounters();
        Iterator ctx = counters.keySet().iterator();
        while (ctx.hasNext()) {
            String ctn = (String) ctx.next();
            retdata += ctn;
            retdata += "=";
            retdata += String.valueOf(counters.get(ctn));
            retdata += ",";
        }
        return WorkBookCommander.returnXMLResponse(retdata);
    }

    /** logs in user with supplied credentials
	 * 
	 *  on success returns a security token which is valid for the session.
	 * 
	 * @return
	 */
    public String login(Map parameters) throws Exception {
        return getStorageAuth().login(parameters);
    }

    /** loads all names tagged global (public) docs
	 * 
	 * @return String csv of name:meme id pair
	 * @param reload whether to reset the cache on server and reload all names
	 */
    public String getgloballists(Map parameters) {
        HttpServletRequest req = (HttpServletRequest) parameters.get(WorkBookCommander.REQUEST);
        HttpServletResponse res = (HttpServletResponse) parameters.get(WorkBookCommander.REQUEST);
        User usr = (User) parameters.get(WorkBookCommander.USER);
        Serve sx = ((Serve) parameters.get(WorkBookCommander.SERVERCONFIG));
        DocumentLoader lx = ((DocumentLoader) parameters.get(WorkBookCommander.LOADER));
        String resx = parameters.get(WorkBookCommander.RESOURCE).toString();
        String rel = req.getParameter("reload");
        boolean reload = false;
        if (rel != null) reload = rel.equals("true");
        Map globalnames = null;
        if (sx.getAttribute("org.docsfree.legacy.global_names") != null) globalnames = (Map) sx.getAttribute("org.docsfree.legacy.global_names");
        try {
            StringBuffer ret = new StringBuffer();
            if (true) {
                globalnames = new HashMap();
                Connection e360conn = sx.getExtenConfig().getE360Connection();
                final PreparedStatement getmemes = e360conn.prepareStatement("SELECT id,description FROM kb_memes WHERE meme_type=26 AND status=" + AclEntry.PUBLIC);
                ResultSet rsx = getmemes.executeQuery();
                while (rsx.next()) {
                    try {
                        int meme_id = rsx.getInt("id");
                        if (!globalnames.values().contains(new Integer(meme_id)) || reload) {
                            MemeWorkBook mwb = null;
                            mwb = (MemeWorkBook) lx.getDocument(meme_id, usr, null, false);
                            NameHandle[] namex = mwb.getNamedRanges();
                            for (int t = 0; t < namex.length; t++) {
                                globalnames.put(namex[t].getName(), namex[t]);
                            }
                        }
                    } catch (Exception e) {
                        Logger.logErr("PluginSystem.loadglobalnames failed.", e);
                    }
                }
                sx.setAttribute("org.docsfree.legacy.global_names", globalnames);
            }
            Iterator it = globalnames.keySet().iterator();
            String format = (String) parameters.get(WorkBookCommander.FORMAT);
            if (format.equals("json")) {
                JSONObject ob = new JSONObject();
                try {
                    Object itx = globalnames.get(resx);
                    if (itx != null) {
                        JSONArray listvals = new JSONArray();
                        NameHandle namx = (NameHandle) globalnames.get(itx.toString());
                        CellHandle[] listv = namx.getCells();
                        for (int t = 0; t < listv.length; t++) {
                            listvals.put(t, listv[t].getFormattedStringVal());
                        }
                        ob.put("name", namx.getName());
                        ob.put("vals", listvals);
                    }
                } catch (JSONException e) {
                    return WorkBookCommander.returnXMLErrorResponse("false");
                }
                return ob.toString();
            }
            return ret.toString();
        } catch (Exception e) {
            Logger.logErr("PluginSystem.loadglobalnames failed.", e);
            return WorkBookCommander.returnXMLErrorResponse("PluginSystem.loadglobalnames failed." + e.toString());
        }
    }

    /** exports all user data as a zip file containing XLS files
	 * 
	 * @return
	 */
    public String exportall(Map parameters) {
        HttpServletRequest req = (HttpServletRequest) parameters.get(WorkBookCommander.REQUEST);
        HttpServletResponse res = (HttpServletResponse) parameters.get(WorkBookCommander.REQUEST);
        User usr = (User) parameters.get(WorkBookCommander.USER);
        Serve sx = ((Serve) parameters.get(WorkBookCommander.SERVERCONFIG));
        try {
            Connection e360conn = sx.getExtenConfig().getE360Connection();
            final PreparedStatement getmemes = e360conn.prepareStatement("SELECT id,description FROM kb_memes WHERE meme_type=26" + " AND owner_id=?");
            getmemes.setInt(1, usr.getId());
            ResultSet rsx = getmemes.executeQuery();
            String wdx = ((ServeConnection) req).getRequestURI(true);
            String webdir = sx.getRealPath("/media/");
            webdir += usr.getId() + "/backups/";
            wdx = wdx + "/media/" + usr.getId() + "/backups/";
            String zipfnx = "data_export-" + System.currentTimeMillis() + ".zip";
            File tmpzip = new File(webdir + zipfnx);
            tmpzip.mkdirs();
            tmpzip.delete();
            ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tmpzip)));
            final PreparedStatement getdesc = e360conn.prepareStatement("SELECT description2 FROM kb_memes WHERE id=?");
            while (rsx.next()) {
                try {
                    getdesc.setInt(1, rsx.getInt("id"));
                    ResultSet rsx2 = getdesc.executeQuery();
                    if (rsx2.next()) {
                        ZipEntry zx = new ZipEntry(rsx.getString("id") + "-" + rsx.getString("description") + ".xls");
                        zout.putNextEntry(zx);
                        byte[] content = ExtenXLS.getXLS(rsx2.getString("description2")).getBytes();
                        for (int t = 0; t < content.length; t++) zout.write(content[t]);
                        zout.closeEntry();
                    }
                } catch (Exception e) {
                    Logger.logErr("PluginSystem.exportall entry failed.", e);
                }
            }
            zout.close();
            res.sendRedirect(wdx + zipfnx);
            return null;
        } catch (Exception e) {
            Logger.logErr("Error exporting all data.", e);
            return WorkBookCommander.returnXMLErrorResponse("Error exporting all data." + e.toString());
        }
    }

    public String getfilelisting(Map parameters) {
        String result = "error";
        HttpServletRequest req = (HttpServletRequest) parameters.get(WorkBookCommander.REQUEST);
        String sid = req.getSession().getId();
        User usr = (User) parameters.get(WorkBookCommander.USER);
        String midi = (String) parameters.get(WorkBookCommander.ID);
        String value = (String) parameters.get(WorkBookCommander.VALUE);
        String resource = (String) parameters.get(WorkBookCommander.RESOURCE);
        Serve sx = ((Serve) parameters.get(WorkBookCommander.SERVERCONFIG));
        String format = (String) parameters.get(WorkBookCommander.FORMAT);
        String action = req.getParameter("a");
        String parentdir = req.getParameter("p");
        String ret = "{'path':'/" + parentdir + "'," + "'parent':'/'," + "'status':'success'," + "'files':[";
        try {
            String userMedia = "/media/" + usr.getId() + "/" + parentdir + "/";
            File fx = new File(sx.getRealPath(userMedia));
            if (fx.isDirectory()) {
                ret += "{'type':'directory','name':'" + fx.getName() + "','size':0,'path':'/mydir'},";
                File[] fxs = fx.listFiles();
                for (int t = 0; t < fxs.length; t++) ret += "{'type':'file','name':'" + fxs[t].getName() + "','size':" + fxs[t].length() + ",'url':'" + userMedia + "/" + fxs[t].getName() + "'},";
            } else {
                ret += "{'type':'file','name':'" + fx.getName() + "','size':" + fx.length() + ",'url':'" + userMedia + "/" + fx.getName() + "'},";
            }
        } catch (Exception ex) {
            Logger.logErr("PluginSystem.getAuthToken failed", ex);
        }
        ret += "],'fileManager':'/workbook/id/" + midi + "/txt/system/getfilelisting/'}";
        return ret;
    }

    public void initialize() {
    }

    /**
     * Check acccess for different methods based on userid, memeid, and methodcalled
     * 
     * This method cannot be reached unless the user has at least read permissions
     * to the meme, so methods that only read can return true without checking.
     */
    public boolean checkAccess(Map parameters) {
        User usr = (User) parameters.get(WorkBookCommander.USER);
        String command = (String) parameters.get(WorkBookCommander.COMMAND);
        String memeid = (String) parameters.get(WorkBookCommander.ID);
        MemeDocument webdoc = (MemeDocument) parameters.get(WorkBookCommander.WORKBOOK);
        command = command.toLowerCase();
        if (command.equals("getworkbookstats")) {
            if (usr.checkAccess("meme_" + memeid, AclEntry.UPDATE)) {
                return true;
            }
        } else if (command.equals("exportall")) {
            if (((User) usr).isValid()) {
                return true;
            }
        } else if (command.equals("getcounters")) {
            return true;
        } else if (command.equals("login")) {
            return true;
        } else if (command.equals("getgloballists")) {
            return true;
        }
        return false;
    }
}
