package com.thoughtworks.blipit.servlets;

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.thoughtworks.blipit.Utils;
import com.thoughtworks.blipit.domain.Blip;
import com.thoughtworks.blipit.domain.Channel;
import com.thoughtworks.blipit.persistence.DataStoreHelper;
import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import static com.thoughtworks.blipit.Utils.isValidManageAction;

public class ManageBlips extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if (isValidManageAction(action)) {
            PersistenceManager persistenceManager = null;
            try {
                persistenceManager = DataStoreHelper.getPersistenceManager();
                if (Utils.isDeleteAction(action)) {
                    deleteBlip(persistenceManager, req);
                } else if (Utils.isSaveAction(action)) {
                    saveBlip(persistenceManager, req);
                }
            } finally {
                if (persistenceManager != null) persistenceManager.close();
            }
        }
        resp.sendRedirect("/ManageBlipIt.jsp?category=blips");
    }

    private void saveBlip(PersistenceManager persistenceManager, HttpServletRequest request) {
        String title = request.getParameter("blip.title");
        String desc = request.getParameter("blip.desc");
        Float blipLatitude = Float.valueOf(request.getParameter("blip.loc.lat"));
        Float blipLongitude = Float.valueOf(request.getParameter("blip.loc.long"));
        Set<Key> channelKeys = getChannelKeys(request.getParameterValues("blip.channels"));
        persistenceManager.makePersistent(new Blip(title, desc, new GeoPt(blipLatitude, blipLongitude), channelKeys, null));
    }

    private Set<Key> getChannelKeys(String[] channelKeyStrs) {
        Set<Key> channelKeys = new HashSet<Key>();
        if (channelKeyStrs != null) {
            for (String channelKeyStr : channelKeyStrs) {
                channelKeys.add(Utils.constructKey(Channel.class, channelKeyStr));
            }
        }
        return channelKeys;
    }

    private void deleteBlip(PersistenceManager persistenceManager, HttpServletRequest request) {
        Enumeration parameterNames = request.getParameterNames();
        if (parameterNames != null) {
            while (parameterNames.hasMoreElements()) {
                String parameterName = (String) parameterNames.nextElement();
                if (parameterName.matches("key[0-9]+")) {
                    String blipKeyStr = request.getParameter(parameterName);
                    Key key = Utils.constructKey(Blip.class, blipKeyStr);
                    Blip blip = persistenceManager.getObjectById(Blip.class, key);
                    persistenceManager.deletePersistent(blip);
                }
            }
        }
    }
}
