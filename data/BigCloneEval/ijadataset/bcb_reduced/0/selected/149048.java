package com.liferay.wsrp.consumer.admin;

import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.model.CompanyConstants;
import com.liferay.portal.model.Portlet;
import com.liferay.portal.model.PortletInfo;
import com.liferay.portal.model.impl.PortletImpl;
import com.liferay.portal.util.PortalUtil;
import com.sun.portal.wsrp.common.WSRPConfig;
import com.sun.portal.wsrp.consumer.common.WSRPConsumerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <a href="WSRPPersistenceHelper.java.html"><b><i>View Source</i></b></a>
 *
 * @author Rajesh Thiagarajan
 *
 */
public class WSRPPersistenceHelper {

    public static synchronized WSRPPersistenceHelper getInstance() {
        return _instance;
    }

    public void addWSRPPortlet(Portlet portlet) throws WSRPConsumerException {
        WSRPPortlet wsrpPortlet = new WSRPPortlet();
        PortletInfo portletInfo = portlet.getPortletInfo();
        wsrpPortlet.setChannelName(portlet.getPortletId());
        wsrpPortlet.setConsumerId(portlet.getRemoteConsumerId());
        wsrpPortlet.setDisplayName(portlet.getDisplayName());
        wsrpPortlet.setKeywords(portletInfo.getKeywords());
        wsrpPortlet.setPortletHandle(portlet.getRemotePortletHandle());
        wsrpPortlet.setPortletId(portlet.getPortletId());
        wsrpPortlet.setProducerEntityId(portlet.getRemoteProducerEntityId());
        wsrpPortlet.setShortTitle(portletInfo.getShortTitle());
        wsrpPortlet.setStatus(portlet.isActive());
        wsrpPortlet.setTitle(portletInfo.getTitle());
        Map<String, Set<String>> portletModes = portlet.getPortletModes();
        Set<String> mimeTypes = portletModes.keySet();
        for (String mimeType : mimeTypes) {
            Set<String> mimeTypeModes = portletModes.get(mimeType);
            MimeType mimeTypeModel = new MimeType();
            mimeTypeModel.setMime(mimeType);
            mimeTypeModel.getModes().addAll(mimeTypeModes);
            wsrpPortlet.getMimeTypes().add(mimeTypeModel);
        }
        WSRPPortlets rootNode = getRootNode();
        updatePortlet(rootNode, wsrpPortlet);
    }

    public void deleteWSRPPortlet(Portlet portlet) throws WSRPConsumerException {
        WSRPPortlets rootNode = getRootNode();
        if (rootNode == null) {
            return;
        }
        Iterator<WSRPPortlet> itr = rootNode.getWSRPPortlet().iterator();
        while (itr.hasNext()) {
            WSRPPortlet wsrpPortlet = itr.next();
            if (portlet.getPortletId().equals(wsrpPortlet.getChannelName())) {
                itr.remove();
                break;
            }
        }
        persistWSRPPortlets(rootNode);
    }

    public List<Portlet> getWSRPPortlets() throws WSRPConsumerException {
        WSRPPortlets rootNode = getRootNode();
        if ((rootNode == null) || (rootNode.getWSRPPortlet().size() == 0)) {
            return Collections.EMPTY_LIST;
        }
        List<Portlet> portlets = new ArrayList<Portlet>();
        List<WSRPPortlet> wsrpPortlets = rootNode.getWSRPPortlet();
        for (WSRPPortlet wsrpPortlet : wsrpPortlets) {
            String portletId = PortalUtil.getJsSafePortletId(wsrpPortlet.getPortletId());
            Portlet portlet = new PortletImpl(CompanyConstants.SYSTEM, portletId);
            portlet.setPortletId(portletId);
            portlet.setTimestamp(System.currentTimeMillis());
            portlet.setPortletName(portletId);
            portlet.setInstanceable(true);
            portlet.setActive(true);
            portlet.setRemote(true);
            portlet.setRemoteConsumerId(wsrpPortlet.getConsumerId());
            portlet.setRemoteProducerEntityId(wsrpPortlet.getProducerEntityId());
            portlet.setRemotePortletHandle(wsrpPortlet.getPortletHandle());
            portlet.setRemotePortletId(wsrpPortlet.getPortletHandle());
            List<MimeType> mimeTypes = wsrpPortlet.getMimeTypes();
            Map<String, Set<String>> portletModes = new HashMap<String, Set<String>>();
            for (MimeType mimeType : mimeTypes) {
                Set<String> modes = new HashSet<String>(mimeType.getModes());
                portletModes.put(mimeType.getMime(), modes);
            }
            portlet.setPortletModes(portletModes);
            PortletInfo portletInfo = new PortletInfo(wsrpPortlet.getTitle(), wsrpPortlet.getShortTitle(), wsrpPortlet.getKeywords());
            portlet.setPortletInfo(portletInfo);
            portlets.add(portlet);
        }
        return portlets;
    }

    public void updateWSRPPortlet(Portlet portlet) throws WSRPConsumerException {
        addWSRPPortlet(portlet);
    }

    protected synchronized WSRPPortlets getRootNode() throws WSRPConsumerException {
        synchronized (this) {
            if (_wsrpPortlets == null || !_dirty) {
                FileInputStream fis = null;
                try {
                    File wsrpPortletsFile = new File(_wsrpPortletsFileName);
                    if (wsrpPortletsFile.length() == 0) {
                        return _objectFactory.createWSRPPortlets();
                    } else {
                        fis = new FileInputStream(_wsrpPortletsFileName);
                        JAXBElement<WSRPPortlets> rootElement = (JAXBElement<WSRPPortlets>) _unmarshaller.unmarshal(fis);
                        _wsrpPortlets = rootElement.getValue();
                    }
                } catch (Exception e) {
                    throw new WSRPConsumerException(e.getMessage(), e);
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException ioe) {
                        }
                    }
                }
                _dirty = true;
            }
        }
        return _wsrpPortlets;
    }

    protected synchronized void persistWSRPPortlets(WSRPPortlets wsrpPortlets) throws WSRPConsumerException {
        FileOutputStream fos = null;
        try {
            synchronized (this) {
                fos = new FileOutputStream(_wsrpPortletsFileName);
                JAXBElement<WSRPPortlets> rootElement = _objectFactory.createWSRPPortlets(wsrpPortlets);
                _marshaller.marshal(rootElement, fos);
                _dirty = false;
            }
        } catch (FileNotFoundException fnfe) {
            _log.error(fnfe, fnfe);
            throw new WSRPConsumerException(fnfe.getMessage(), fnfe);
        } catch (JAXBException jaxbe) {
            _log.error(jaxbe, jaxbe);
            throw new WSRPConsumerException(jaxbe.getMessage(), jaxbe);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

    protected void updatePortlet(WSRPPortlets wsrpPortlets, WSRPPortlet wsrpPortlet) throws WSRPConsumerException {
        if (wsrpPortlets == null) {
            return;
        }
        Iterator<WSRPPortlet> itr = wsrpPortlets.getWSRPPortlet().iterator();
        while (itr.hasNext()) {
            WSRPPortlet curWSRPPortlet = itr.next();
            if (curWSRPPortlet.getChannelName().equals(wsrpPortlet.getChannelName())) {
                itr.remove();
                break;
            }
        }
        wsrpPortlets.getWSRPPortlet().add(wsrpPortlet);
        persistWSRPPortlets(wsrpPortlets);
    }

    private WSRPPersistenceHelper() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            _jaxbContext = JAXBContext.newInstance("com.liferay.wsrp.consumer.admin", classLoader);
            _marshaller = _jaxbContext.createMarshaller();
            _marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            _unmarshaller = _jaxbContext.createUnmarshaller();
            _objectFactory = new ObjectFactory();
            String wsrpDataDir = WSRPConfig.getWSRPDataDirectory();
            FileUtil.mkdirs(wsrpDataDir);
            File wsrpConsumerFile = new File(wsrpDataDir + "/consumer.xml");
            if (!wsrpConsumerFile.exists()) {
                FileUtil.write(wsrpConsumerFile, StringUtil.read(classLoader, "com/liferay/wsrp/consumer/data/consumer.xml"));
            }
            _wsrpPortletsFileName = wsrpDataDir + "/wsrpportlets.xml";
            File wsrpPortletsFile = new File(_wsrpPortletsFileName);
            if (!wsrpPortletsFile.exists()) {
                wsrpPortletsFile.createNewFile();
            }
        } catch (Exception e) {
            _log.error(e, e);
        }
    }

    private static Log _log = LogFactory.getLog(WSRPPersistenceHelper.class);

    private static WSRPPersistenceHelper _instance = new WSRPPersistenceHelper();

    private JAXBContext _jaxbContext;

    private Marshaller _marshaller;

    private Unmarshaller _unmarshaller;

    private ObjectFactory _objectFactory;

    private String _wsrpPortletsFileName;

    private boolean _dirty;

    private WSRPPortlets _wsrpPortlets;
}
