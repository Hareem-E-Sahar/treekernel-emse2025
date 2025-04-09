package deduced.fetch;

import java.util.*;
import assertion.AssertUtility;
import deduced.PropertyCollection;

public class DefaultFetcherFactory implements FetcherFactory {

    private Map modelFetcherMap = new IdentityHashMap();

    private Class fetcherClass = MemoryFetcher.class;

    private Object configuration = null;

    public Fetcher getFetcher(PropertyCollection model) {
        Fetcher retVal = (Fetcher) modelFetcherMap.get(model);
        if (retVal == null) {
            retVal = createFetcher(model);
        }
        return retVal;
    }

    private Fetcher createFetcher(PropertyCollection model) {
        Fetcher newFetcher = null;
        try {
            newFetcher = (Fetcher) fetcherClass.getConstructor(null).newInstance(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        newFetcher.configure(getConfiguration());
        newFetcher.setModel(model);
        modelFetcherMap.put(model, newFetcher);
        return newFetcher;
    }

    /**
     * @return Returns the configuration.
     */
    public Object getConfiguration() {
        return configuration;
    }

    /**
     * @param setConfiguration The configuration to set.
     */
    public void setConfiguration(Object setConfiguration) {
        assertEmpty();
        this.configuration = setConfiguration;
    }

    /**
     * @return Returns the fetcherClass.
     */
    public Class getFetcherClass() {
        return fetcherClass;
    }

    /**
     * @param setFetcherClass The fetcherClass to set.
     */
    public void setFetcherClass(Class setFetcherClass) {
        assertEmpty();
        this.fetcherClass = setFetcherClass;
    }

    private void assertEmpty() {
        AssertUtility.assertEquals(0, modelFetcherMap.size());
    }
}
