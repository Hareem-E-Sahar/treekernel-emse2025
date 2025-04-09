package com.etymon.sarcomere.service.impl;

import java.io.*;
import java.sql.*;
import java.util.*;

public class SearchProc extends SarcomereProc {

    private String[] _dbname;

    private String _query;

    private String[] _rset;

    public SearchProc() {
        super();
    }

    public void setDatabaseName(String[] dbname) {
        _dbname = dbname;
    }

    public void setQuery(String query) {
        _query = query;
    }

    public String[] getRset() {
        return _rset;
    }

    public void proc() throws Exception {
        StringBuffer dbname = new StringBuffer();
        for (int x = 0; x < _dbname.length; x++) {
            String[] index = Util.getIndexes(_cn, _dbname[x]);
            if (index == null) {
                _err = 1;
                _errs = "No index exists in database \"" + _dbname[x] + "\"";
                return;
            }
            dbname.append(' ' + Config.getSarcomereDir() + '/' + Config.getDataDir() + '/' + _dbname[x] + '/' + index[0] + "/index");
        }
        String[] search = { "/bin/sh", "-c", "/usr/local/bin/af -s -d" + dbname + " -Q '" + _query + "'" };
        Process process = Runtime.getRuntime().exec(search);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        List lines = new ArrayList();
        String s;
        while ((s = reader.readLine()) != null) {
            lines.add(s);
        }
        _rset = (String[]) lines.toArray(new String[lines.size()]);
        for (int x = 0; x < _rset.length; x++) {
            String[] fields = _rset[x].split(" ");
            _rset[x] = fields[5];
        }
    }
}
