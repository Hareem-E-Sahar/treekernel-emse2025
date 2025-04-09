package org.wings;

import org.wings.session.SessionManager;
import org.wings.conf.CmsDetail;
import org.wings.conf.Configuration;
import org.wings.adapter.AbstractCmsAdapter;
import org.wings.adapter.CmsAdapter;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * <code>CmsFrame<code>.
 * <p/>
 * User: raedler
 * Date: 08.08.2007
 * Time: 09:27:35
 *
 * @author raedler
 * @version $Id
 */
public class CmsFrame extends SFrame {

    private CmsLayout layout = new CmsLayout();

    /**
     * Default constructor
     */
    public CmsFrame() {
        setContentPane(new CmsForm());
        getContentPane().setLayout(layout);
        Configuration cfg = readConfiguration();
        CmsDetail cmsConfig = cfg.getCmsDetail();
        CmsAdapter adapter = createAdapter(cmsConfig);
        adapter.setFrame(this);
        adapter.setConfiguration(cmsConfig);
        SessionManager.getSession().setResourceMapper(adapter);
    }

    /**
     * Reads the wings to cms configuration file and returns an object containing all required data.
     *
     * @return A configuration object with all required data to access the cms.
     */
    private Configuration readConfiguration() {
        try {
            JAXBContext ctx = JAXBContext.newInstance(Configuration.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            return (Configuration) unmarshaller.unmarshal(getClass().getClassLoader().getResourceAsStream("wings-2-cms.xml"));
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

    private CmsAdapter createAdapter(CmsDetail cfg) {
        Class type = cfg.getAdapter();
        assert type != null : "Adapter class cannot be null.";
        try {
            if (AbstractCmsAdapter.class.isAssignableFrom(type)) {
                Constructor constructor = type.getConstructor(SFrame.class, STemplateLayout.class, CmsDetail.class);
                return (CmsAdapter) constructor.newInstance(this, layout, cfg);
            } else {
                return (CmsAdapter) type.newInstance();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public final SComponent add(SComponent c) {
        throw new UnsupportedOperationException("This method won't be supported by CmsFrame. Use add(SComponent c, Object constraint) instead.");
    }

    @Override
    public void add(SComponent c, Object constraint) {
        getContentPane().add(c, constraint);
    }

    @Override
    public SComponent add(SComponent c, int index) {
        throw new UnsupportedOperationException("This method won't be supported by CmsFrame. Use add(SComponent c, Object constraint) instead.");
    }

    @Override
    public void add(SComponent c, Object constraint, int index) {
        throw new UnsupportedOperationException("This method won't be supported by CmsFrame. Use add(SComponent c, Object constraint) instead.");
    }
}
