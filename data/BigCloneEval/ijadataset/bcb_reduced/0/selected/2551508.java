package com.mockturtlesolutions.snifflib.statmodeltools.database;

import java.io.*;
import java.sql.*;
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Vector;
import java.lang.reflect.Constructor;
import com.mysql.jdbc.Driver;
import com.mockturtlesolutions.snifflib.sqldig.database.*;
import com.mockturtlesolutions.snifflib.reposconfig.database.RepositoryStorage;
import com.mockturtlesolutions.snifflib.reposconfig.database.RepositoryStorageNameQuery;
import com.mockturtlesolutions.snifflib.reposconfig.database.RepositoryMaintenance;
import com.mockturtlesolutions.snifflib.reposconfig.database.RepositorySQLConnection;
import com.mockturtlesolutions.snifflib.reposconfig.database.ReposConfig;
import com.mockturtlesolutions.snifflib.reposconfig.database.ReposConfig;
import com.mockturtlesolutions.snifflib.reposconfig.database.RepositoryConnectivity;
import com.mockturtlesolutions.snifflib.statmodeltools.workbench.StatisticalModelFindNameDialog;

/**
Establish a connection with an SQL server and process requests.
*/
public class StatisticalModelSQLConnection extends RepositorySQLConnection implements StatisticalModelStorageConnectivity {

    public StatisticalModelSQLConnection(ReposConfig config, String repos, boolean connect) {
        super((ReposConfig) config, repos, connect);
    }

    public StatisticalModelSQLConnection(ReposConfig config, String repos) {
        super((ReposConfig) config, repos);
    }

    public Class resolveStorageFor(Class iface) {
        Class out = null;
        System.out.println("The class to resolve is " + iface);
        if (iface.isAssignableFrom(StatisticalModelStorage.class)) {
            out = StatisticalModelSQL.class;
        } else {
            throw new RuntimeException("Unable to resolve storage class for " + iface + ".");
        }
        System.out.println("Resolved storage to " + out);
        return (out);
    }

    public SQLDatabase getSkeleton() {
        SQLDatabase DB = super.getSkeleton();
        return (DB);
    }

    public Class[] what() {
        Class[] out = new Class[] { StatisticalModelStorage.class };
        return (out);
    }

    public RepositoryStorageNameQuery getStorageNameQuery(Class storageclass) {
        RepositoryStorageNameQuery OUT = null;
        if (StatisticalModelStorage.class.isAssignableFrom(storageclass)) {
            OUT = new StatisticalModelNameSQLQuery(this);
        }
        return (OUT);
    }

    public Class determineClassOf(String storagename) {
        Class out = null;
        try {
            Statement s = this.Connection.createStatement();
            String cmd = "SELECT name_id FROM statisticalmodeldetails WHERE name_id IN (SELECT name_id FROM names WHERE nickname='" + storagename + "');";
            s.executeQuery(cmd);
            ResultSet rs = s.getResultSet();
            if (rs.next()) {
                out = StatisticalModelSQL.class;
                rs.close();
            }
        } catch (java.sql.SQLException err) {
            throw new RuntimeException(err);
        }
        return (out);
    }

    public RepositoryStorage getStorage(String storagename) {
        RepositoryStorage out = null;
        if (storagename == null) {
            throw new RuntimeException("Can not get storage when the given nickname is null.");
        }
        if (storagename.equals("")) {
            throw new RuntimeException("Can not get the storage when the given nickname is empty.");
        }
        Class targetclass = determineClassOf(storagename);
        if (targetclass != null) {
            Class[] cnstrargs = new Class[] { RepositoryConnectivity.class, String.class };
            Constructor cnstr = null;
            try {
                cnstr = targetclass.getConstructor(cnstrargs);
                if (cnstr == null) {
                    throw new RuntimeException("Constructor is null.");
                }
            } catch (Exception err) {
                throw new RuntimeException("Problem obtaining constructor for storage.", err);
            }
            try {
                out = (RepositoryStorage) cnstr.newInstance(this, storagename);
            } catch (Exception err) {
                throw new RuntimeException("Unable to construct.", err);
            }
        } else {
            if (storageExists(storagename)) {
                throw new RuntimeException("The storage " + storagename + " was indicated to exist but its class could not be determined.  Database may be in a corrupted state.");
            }
        }
        return (out);
    }

    public boolean storageExists(String storagename) {
        boolean out = false;
        try {
            Statement s = this.Connection.createStatement();
            String cmd = "SELECT name_id FROM names WHERE  nickname='" + storagename + "';";
            s.executeQuery(cmd);
            ResultSet rs = s.getResultSet();
            if (rs.next()) {
                out = true;
                rs.close();
            }
        } catch (java.sql.SQLException err) {
            throw new RuntimeException(err);
        }
        return (out);
    }

    public Class getFindNameDialogForClass(Class storageclass) {
        return (StatisticalModelFindNameDialog.class);
    }
}
