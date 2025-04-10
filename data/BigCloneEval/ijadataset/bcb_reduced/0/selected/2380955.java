package com.apelon.dts.db.config;

import java.io.*;
import java.net.URL;
import java.util.Map;
import java.sql.Connection;
import com.apelon.common.sql.SQL;
import com.apelon.common.util.db.DBSystemConfig;
import com.apelon.common.log4j.LogConfigLoader;

public class DTSDBConfig implements DBSystemConfig {

    private static final String DTD = "dtsconfig.dtd";

    public DTSDBConfig() {
    }

    public void init(Connection source, Connection target) {
    }

    public String getDaoDTDFileName() {
        return DTD;
    }

    public String getDaoDTDURL() {
        return "http://apelon.com/dtd/dts/" + DTD;
    }

    public Class getDaoDTDClass() {
        return com.apelon.dts.db.admin.config.DBLoadConfig.class;
    }

    public InputStream getDaoConfig(String connectionType) throws IOException {
        URL url = null;
        if (connectionType.equals(SQL.ORACLE)) {
            url = DTSDBConfig.class.getResource("oracle.xml");
        } else if (connectionType.equals(SQL.SQL2K)) {
            url = DTSDBConfig.class.getResource("sql2k.xml");
        } else if (connectionType.equals(SQL.CACHE)) {
            url = DTSDBConfig.class.getResource("cache.xml");
        }
        return url.openStream();
    }

    public InputStream getTableLists() throws IOException {
        return null;
    }

    public LogConfigLoader logConfigLoader() {
        return null;
    }

    public void validateInput(Map propertyMap) throws Exception {
    }

    public String[] getConnectionProperties() {
        return null;
    }
}
