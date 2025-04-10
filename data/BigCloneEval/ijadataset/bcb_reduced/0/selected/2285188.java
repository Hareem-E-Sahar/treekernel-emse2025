package org.datanucleus.store.json;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.transaction.xa.XAResource;
import org.datanucleus.exceptions.NucleusDataStoreException;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.connection.AbstractConnectionFactory;
import org.datanucleus.store.connection.AbstractManagedConnection;
import org.datanucleus.store.connection.ManagedConnection;

/**
 * Implementation of a ConnectionFactory for JSON. The connections are
 * only created and they are not managed. All operations besides getConnection are no-op.
 */
public class ConnectionFactoryImpl extends AbstractConnectionFactory {

    public static final String STORE_JSON_URL = "org.datanucleus.store.json.url";

    /**
     * Constructor.
     * @param storeMgr Store Manager
     * @param resourceType Type of resource (tx, nontx)
     */
    public ConnectionFactoryImpl(StoreManager storeMgr, String resourceType) {
        super(storeMgr, resourceType);
    }

    /**
     * Obtain a connection from the Factory. The connection will be enlisted within the {@link org.datanucleus.Transaction} 
     * associated to the <code>poolKey</code> if "enlist" is set to true.
     * @param poolKey the pool that is bound the connection during its lifecycle (or null)
     * @param options Any options for then creating the connection
     * @return the {@link org.datanucleus.store.connection.ManagedConnection}
     */
    public ManagedConnection createManagedConnection(Object poolKey, Map options) {
        return new ManagedConnectionImpl(options);
    }

    /**
     * Implementation of a ManagedConnection for JSON.
     */
    public class ManagedConnectionImpl extends AbstractManagedConnection {

        Map options;

        public ManagedConnectionImpl(Map options) {
            this.options = options;
        }

        public void close() {
        }

        public Object getConnection() {
            String urlStr = storeMgr.getConnectionURL();
            urlStr = urlStr.substring(urlStr.indexOf(storeMgr.getStoreManagerKey() + ":") + storeMgr.getStoreManagerKey().length() + 1);
            if (options.containsKey(STORE_JSON_URL)) {
                if (urlStr.endsWith("/") && options.get(STORE_JSON_URL).toString().startsWith("/")) {
                    urlStr += options.get(STORE_JSON_URL).toString().substring(1);
                } else if (!urlStr.endsWith("/") && !options.get(STORE_JSON_URL).toString().startsWith("/")) {
                    urlStr += "/" + options.get(STORE_JSON_URL).toString();
                } else {
                    urlStr += options.get(STORE_JSON_URL).toString();
                }
            }
            URL url;
            try {
                url = new URL(urlStr);
                return url.openConnection();
            } catch (MalformedURLException e) {
                throw new NucleusDataStoreException(e.getMessage(), e);
            } catch (IOException e) {
                throw new NucleusDataStoreException(e.getMessage(), e);
            }
        }

        public XAResource getXAResource() {
            return null;
        }
    }
}
