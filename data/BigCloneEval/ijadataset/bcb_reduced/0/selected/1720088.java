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

/**
Establish a connection with an SQL server and process requests.
*/
public class GLMStorageSQLConnection extends StatisticalModelSQLConnection implements GLMStorageConnectivity {

    public GLMStorageSQLConnection(ReposConfig config, String repos, boolean x) {
        super((ReposConfig) config, repos, x);
    }

    public GLMStorageSQLConnection(ReposConfig config, String repos) {
        super((ReposConfig) config, repos);
    }

    public Class resolveStorageFor(Class iface) {
        Class out = null;
        System.out.println("The class to resolve is " + iface);
        if (iface.isAssignableFrom(GLMStorage.class)) {
            out = GLMStorageSQL.class;
        } else {
            throw new RuntimeException("Unable to resolve storage class for " + iface + ".");
        }
        System.out.println("Resolved storage to " + out);
        return (out);
    }

    public SQLDatabase getSkeleton() {
        SQLDatabase DB = new SQLDatabase("sql_database");
        SQLField field = null;
        SQLClause clause = null;
        SQLTable table = null;
        table = new SQLTable("names");
        field = new SQLField("name_id", "int(11)");
        field.setNull("NOT NULL");
        field.setExtra("auto_increment");
        table.addField(field);
        table.addField(field);
        field = new SQLField("nickname", "tinytext");
        clause = new SQLClause("PRIMARY KEY (`name_id`)");
        table.addClause(clause);
        clause = new SQLClause("UNIQUE (`nickname`(255))");
        table.addClause(clause);
        DB.addTable(table);
        table = new SQLTable("glmstoragedetails");
        field = new SQLField("standard_name", "varchar(100)");
        field.setNull("NOT NULL");
        field.setDefault("");
        table.addField(field);
        field = new SQLField("enabled", "tinyint(1)");
        field.setNull("NOT NULL");
        field.setDefault("0");
        table.addField(field);
        field = new SQLField("created_on", "date");
        field.setNull("NOT NULL");
        field.setDefault("0000-00-00");
        table.addField(field);
        field = new SQLField("comment", "mediumtext");
        field.setNull("NOT NULL");
        table.addField(field);
        field = new SQLField("created_by", "varchar(50)");
        field.setNull("NOT NULL");
        field.setDefault("");
        table.addField(field);
        field = new SQLField("name_id", "int(10) unsigned");
        field.setNull("NOT NULL");
        table.addField(field);
        field = new SQLField("glmstoragedetails_id", "int(10) unsigned");
        field.setNull("NOT NULL");
        field.setExtra("auto_increment");
        clause = new SQLClause("PRIMARY KEY  (`glmstoragedetails_id`)");
        table.addClause(clause);
        clause = new SQLClause("UNIQUE (`name_id`)");
        table.addClause(clause);
        DB.addTable(table);
        table = new SQLTable("datasets");
        field = new SQLField("glmname_id", "int(10) unsigned");
        field.setNull("NOT NULL");
        table.addField(field);
        field = new SQLField("datasetname", "mediumtext");
        field.setNull("NOT NULL");
        field.setDefault("");
        table.addField(field);
        clause = new SQLClause("UNIQUE (`glmname_id`)");
        table.addClause(clause);
        DB.addTable(table);
        table = new SQLTable("class_terms");
        field = new SQLField("class_term", "mediumtext");
        field.setNull("NOT NULL");
        table.addField(field);
        field = new SQLField("glmname_id", "int(10) unsigned");
        field.setNull("NOT NULL");
        table.addField(field);
        clause = new SQLClause("UNIQUE (`class_term`(255),`glmname_id`)");
        table.addClause(clause);
        DB.addTable(table);
        table = new SQLTable("models");
        field = new SQLField("glmname_id", "int(10) unsigned");
        field.setNull("NOT NULL");
        table.addField(field);
        field = new SQLField("lefthandside", "mediumtext");
        field.setNull("NOT NULL");
        field.setDefault("");
        table.addField(field);
        field = new SQLField("righthandside", "mediumtext");
        field.setNull("NOT NULL");
        field.setDefault("");
        table.addField(field);
        field = new SQLField("analysis_by", "mediumtext");
        field.setNull("NOT NULL");
        field.setDefault("");
        table.addField(field);
        clause = new SQLClause("UNIQUE (`glmname_id`)");
        table.addClause(clause);
        DB.addTable(table);
        table = new SQLTable("parameters");
        field = new SQLField("glmname_id", "int(10) unsigned");
        field.setNull("NOT NULL");
        table.addField(field);
        field = new SQLField("parameter_name", "mediumtext");
        field.setNull("NOT NULL");
        table.addField(field);
        field = new SQLField("parameter_value", "double");
        clause = new SQLClause("UNIQUE (`parameter_name`(255),`glmname_id`)");
        table.addClause(clause);
        DB.addTable(table);
        table = new SQLTable("random_terms");
        field = new SQLField("glmname_id", "int(10) unsigned");
        field.setNull("NOT NULL");
        table.addField(field);
        field = new SQLField("random_term", "mediumtext");
        field.setNull("NOT NULL");
        field.setDefault("");
        table.addField(field);
        clause = new SQLClause("UNIQUE (`random_term`(255),`glmname_id`)");
        DB.addTable(table);
        table = new SQLTable("variables");
        field = new SQLField("glmname_id", "int(10) unsigned");
        field.setNull("NOT NULL");
        table.addField(field);
        field = new SQLField("variable_name", "mediumtext");
        field.setNull("NOT NULL");
        table.addField(field);
        clause = new SQLClause("UNIQUE (`variable_name`(255),`glmname_id`)");
        table.addClause(clause);
        DB.addTable(table);
        table = new SQLTable("variable_mappings");
        field = new SQLField("glmname_id", "int(10) unsigned");
        field.setNull("NOT NULL");
        table.addField(field);
        field = new SQLField("variable_name", "mediumtext");
        field.setNull("NOT NULL");
        table.addField(field);
        field = new SQLField("data_column", "mediumtext");
        table.addField(field);
        clause = new SQLClause("UNIQUE (`variable_name`(255),`glmname_id`)");
        table.addClause(clause);
        DB.addTable(table);
        return (DB);
    }

    public Class[] what() {
        Class[] out = new Class[] { GLMStorage.class };
        return (out);
    }

    public RepositoryStorageNameQuery getStorageNameQuery(Class storageclass) {
        RepositoryStorageNameQuery OUT = null;
        if (GLMStorage.class.isAssignableFrom(storageclass)) {
            OUT = new GLMStorageNameSQLQuery(this);
        }
        return (OUT);
    }

    public Class determineClassOf(String storagename) {
        Class out = null;
        try {
            Statement s = this.Connection.createStatement();
            String cmd = "SELECT name_id FROM glmstoragedetails WHERE name_id IN (SELECT name_id FROM names WHERE nickname='" + storagename + "');";
            s.executeQuery(cmd);
            ResultSet rs = s.getResultSet();
            if (rs.next()) {
                out = GLMStorageSQL.class;
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
        return (null);
    }
}
