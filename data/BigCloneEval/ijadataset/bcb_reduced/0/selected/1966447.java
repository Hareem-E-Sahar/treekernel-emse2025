package com.cs.util.db.representation;

import com.cs.util.db.Database;
import com.cs.util.db.ResultSetMetaInfo;
import com.cs.util.db.ioc.IOController;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Action is a URI template containing variables within brackets. A URI template can easily be compiled into a URL for linking one resource with another.
 * @author dimitris@jmike.gr
 */
public class Action {

    private final String name;

    private final String label;

    private final String template;

    private final int[] openPos;

    private final int[] closePos;

    private final IOController[] ioc;

    /**
     * Parses the designated URI template and constructs a new Action.
     * @param db the parent database of the action.
     * @param rs the resultset that will be used to compile this action.
     * @param name the name of the action.
     * @param label the label of the action.
     * @param template the URI template containing variables within brackets, i.e. http://www.google.com/q={term}
     * @throws SQLException
     */
    public Action(Database db, ResultSet rs, String name, String label, String template) throws SQLException {
        this(db, db.getResultSetMetaInfo(rs), name, label, template);
    }

    /**
     * Parses the designated URI template and constructs a new Action.
     * @param db the parent database of the action.
     * @param table the table that will be used to compile this action.
     * @param name the name of the action.
     * @param label the label of the action.
     * @param template the URI template containing variables within brackets, i.e. http://www.google.com/q={term}
     * @throws SQLException
     */
    public Action(Database db, String table, String name, String label, String template) throws SQLException {
        this(db, db.getResultSetMetaInfo(table), name, label, template);
    }

    /**
     * Parses the designated URI template and constructs a new Action.
     * @param db the parent database of the action.
     * @param rsmi meta information about the resultset that will be used to compile this action.
     * @param name the name of the action.
     * @param label the label of the action.
     * @param template the URI template containing variables within brackets, i.e. http://www.google.com/q={term}
     * @throws SQLException
     */
    public Action(Database db, ResultSetMetaInfo rsmi, String name, String label, String template) throws SQLException {
        this.name = name;
        this.label = label;
        this.template = template;
        final Pattern pattern = Pattern.compile("\\{\\w+\\.?\\w+\\}");
        final Matcher matcher = pattern.matcher(template);
        int i = 0;
        while (matcher.find()) {
            i = i + 1;
        }
        this.openPos = new int[i];
        this.closePos = new int[i];
        this.ioc = new IOController[i];
        matcher.reset();
        i = 0;
        while (matcher.find()) {
            this.openPos[i] = matcher.start();
            this.closePos[i] = matcher.end();
            final String variable = template.substring(openPos[i] + 1, closePos[i] - 1);
            final String[] s = variable.split("\\.\\s*", 2);
            if (s.length > 1) {
                try {
                    final int columnIndex = rsmi.getPosition(s[0], s[1]);
                    this.ioc[i] = db.getIOController(rsmi, columnIndex);
                } catch (SQLException ex) {
                    this.ioc[i] = null;
                }
            } else {
                try {
                    final int columnIndex = rsmi.getPosition(variable);
                    this.ioc[i] = db.getIOController(rsmi, columnIndex);
                } catch (SQLException ex) {
                    this.ioc[i] = null;
                }
            }
            i++;
        }
    }

    /**
     * Returns the label of this action.
     * @return
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the id of this action.
     * @return
     */
    public String getId() {
        return name;
    }

    /**
     * Returns the URI template of this action.
     * @return
     */
    public String getUriTemplate() {
        return template;
    }

    /**
     * Returns a URL.
     * @param rs the dataset.
     * @return
     * @throws SQLException
     */
    public String getURL(ResultSet rs) throws SQLException {
        StringBuilder url = new StringBuilder(template);
        int dev = 0;
        for (int i = 0; i < ioc.length; i++) {
            final int open = openPos[i] + dev;
            final int close = closePos[i] + dev;
            final String value;
            if (ioc[i] != null) {
                value = ioc[i].getURLEncodedValue(rs);
                url = url.replace(open, close, value);
            } else {
                value = "";
                url = url.delete(open, close);
            }
            dev = dev - (closePos[i] - openPos[i]) + value.length();
        }
        return new String(url);
    }
}
