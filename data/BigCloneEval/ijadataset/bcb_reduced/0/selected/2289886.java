package com.go.teaservlet;

import java.util.Map;
import java.util.Hashtable;
import javax.servlet.ServletContext;
import com.go.trove.log.Log;
import com.go.trove.util.ClassInjector;
import com.go.trove.util.MergedClass;
import com.go.tea.engine.ContextSource;
import com.go.tea.engine.DynamicContextSource;
import com.go.tea.engine.MergedContextSource;
import com.go.tea.engine.ContextCreationException;

/******************************************************************************
 * 
 * @author Jonathan Colwell
 * @version
 * <!--$$Revision: 3 $-->, <!--$$JustDate:-->  2/13/02 <!-- $-->
 */
public class TeaServletContextSource extends MergedContextSource {

    private Map mPastContextTypes;

    private Application[] mApplications;

    private Map mContextTypeMap;

    private DynamicContextSource[] mDynSources;

    private Log mLog;

    TeaServletContextSource(ClassLoader loader, ApplicationDepot appDepot, ServletContext servletContext, Log log, boolean prependWithHttpContext) throws Exception {
        int len = prependWithHttpContext ? 1 : 0;
        mLog = log;
        Application[] applications = appDepot.getApplications();
        String[] prefixes = appDepot.getContextPrefixNames();
        DynamicContextSource[] contextSources = new DynamicContextSource[applications.length + len];
        mApplications = applications;
        mDynSources = contextSources;
        if (prependWithHttpContext) {
            String[] allPrefixes = new String[contextSources.length];
            contextSources[0] = new HttpContextSource(servletContext, log);
            allPrefixes[0] = "HttpContext$";
            System.arraycopy(prefixes, 0, allPrefixes, len, prefixes.length);
            prefixes = allPrefixes;
        }
        for (int j = 0; j < applications.length; j++) {
            contextSources[j + len] = new ApplicationContextSource(applications[j]);
        }
        init(loader, contextSources, prefixes);
    }

    public final Map getApplicationContextTypes() {
        if (mContextTypeMap == null) {
            int tableSize = mApplications.length;
            Class[] contextTypes = getContextsInOrder();
            mContextTypeMap = new Hashtable(tableSize);
            int prependAdjustment = ((tableSize == contextTypes.length) ? 0 : 1);
            for (int j = 0; j < tableSize; j++) {
                mContextTypeMap.put(mApplications[j], contextTypes[j + prependAdjustment]);
            }
        }
        return mContextTypeMap;
    }

    /**
     * a generic method to create context instances 
     */
    public Object createContext(Object param) throws Exception {
        return getConstructor().newInstance(new Object[] { new TSContextFactory(param) });
    }

    private class TSContextFactory implements MergedClass.InstanceFactory {

        private final Object mContextParameter;

        TSContextFactory(Object contextParam) {
            mContextParameter = contextParam;
        }

        public Object getInstance(int i) {
            try {
                return mDynSources[i].createContext(getContextsInOrder()[i], mContextParameter);
            } catch (Exception e) {
                throw new ContextCreationException(e);
            }
        }
    }
}
