package org.nbplugin.jpa.datasource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 * Thread for testing the connection of a DataSource
 * 
 * @author shofmann
 * @version $Revision: 1.1 $
 * last modified by $Author: sebhof $
 */
public class DataSourceConnectionTestThread extends Thread {

    /** A logger */
    private static final Logger logger = Logger.getLogger(DataSourceConnectionTestThread.class.getName());

    private DataSource dataSource;

    private Throwable throwable;

    public DataSourceConnectionTestThread(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run() {
        try {
            logger.info("Trying to create DummyDataSource...");
            Class dummyDataSourceClass = this.getContextClassLoader().loadClass("org.nbplugin.jpa.tools.DummyDataSource");
            this.getContextClassLoader().loadClass(this.dataSource.getDriverClass());
            Constructor dummyDataSourceConstructor = dummyDataSourceClass.getConstructor(String.class, String.class, String.class);
            Object dummyDataSource = dummyDataSourceConstructor.newInstance(this.dataSource.getUrl(), this.dataSource.getUsername(), this.dataSource.getPassword());
            Method getConnectionMethod = dummyDataSource.getClass().getMethod("getConnection");
            getConnectionMethod.invoke(dummyDataSource);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public boolean wasConnectionTestSuccessfull() {
        return this.throwable == null;
    }

    public Throwable getError() {
        return this.throwable;
    }
}
