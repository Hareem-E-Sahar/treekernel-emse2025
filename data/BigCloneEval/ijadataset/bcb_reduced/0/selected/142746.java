package de.haumacher.timecollect.common.report;

import static de.haumacher.timecollect.common.report.RE.SPACE;
import static de.haumacher.timecollect.common.report.RE.any;
import static de.haumacher.timecollect.common.report.RE.choice;
import static de.haumacher.timecollect.common.report.RE.group;
import static de.haumacher.timecollect.common.report.RE.literal;
import static de.haumacher.timecollect.common.report.RE.many;
import static de.haumacher.timecollect.common.report.RE.notIn;
import static de.haumacher.timecollect.common.report.RE.optional;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Query {

    private Set<String> hiddenColumns = new HashSet<String>();

    private String sql;

    private List<Grouping> groupOperations;

    public Query(String query) throws QuerySyntaxError {
        int groupSpecial = 1;
        int groupContent = 2;
        Pattern commentPattern = Pattern.compile("^" + any(SPACE) + literal("#") + optional(group(literal("!"))) + group(any(".")) + "$", Pattern.MULTILINE);
        Matcher matcher = commentPattern.matcher(query);
        groupOperations = new ArrayList<Grouping>();
        int level = 0;
        StringBuilder buffer = new StringBuilder();
        int idx = 0;
        while (matcher.find(idx)) {
            buffer.append(query.substring(idx, matcher.start()));
            if (matcher.group(groupSpecial) != null) {
                String commandLine = matcher.group(groupContent);
                int groupGroup = 1;
                int headingGroup = 2;
                int hideGroup = 3;
                Pattern commandPattern = Pattern.compile("^" + any(SPACE) + choice(group(literal("group")), group(literal("heading")), group(literal("hide"))) + literal(":") + any(SPACE));
                Matcher commandMatcher = commandPattern.matcher(commandLine);
                if (!commandMatcher.lookingAt()) {
                    throw new QuerySyntaxError("Invalid command line: Expected command in '" + commandLine + "'");
                }
                String commandArg = commandLine.substring(commandMatcher.end());
                if (commandMatcher.group(groupGroup) != null) {
                    String[] columns = commandArg.split(any(SPACE) + literal(",") + any(SPACE));
                    groupOperations.add(new Group(columns));
                } else if (commandMatcher.group(hideGroup) != null) {
                    String[] columns = commandArg.split(any(SPACE) + literal(",") + any(SPACE));
                    for (String column : columns) {
                        hiddenColumns.add(column);
                    }
                } else if (commandMatcher.group(headingGroup) != null) {
                    int varGroup = 1;
                    Pattern varPattern = Pattern.compile(literal("${") + group(many(notIn("}"))) + literal("}"));
                    List<String> columns = new ArrayList<String>();
                    Matcher varMatcher = varPattern.matcher(commandArg);
                    int varIdx = 0;
                    while (varMatcher.find(varIdx)) {
                        String varName = varMatcher.group(varGroup);
                        columns.add(varName);
                        varIdx = varMatcher.end();
                    }
                    int columnCount = columns.size();
                    if (columnCount == 0) {
                        throw new QuerySyntaxError("Invalid heading command: Missing variable reference in heading command.");
                    }
                    groupOperations.add(new Heading(columns.toArray(new String[columnCount]), level++, commandArg));
                } else {
                    throw new AssertionError("Unreachable.");
                }
            }
            idx = matcher.end();
        }
        buffer.append(query.substring(idx, query.length()));
        sql = buffer.toString();
    }

    protected boolean isHidden(String columnName) {
        return hiddenColumns.contains(columnName);
    }

    protected void initColumnIndices(Map<String, Integer> columnIndexMapping) throws ReportError {
        for (Grouping grouping : groupOperations) {
            grouping.init(columnIndexMapping);
        }
    }

    protected void updateGroups(String[] values, ReportBuilder builder, Object body) {
        boolean parentGroupMatch = false;
        for (int n = 0, cnt = groupOperations.size(); n < cnt; n++) {
            parentGroupMatch = groupOperations.get(n).update(parentGroupMatch, values, builder, body);
        }
    }

    public void process(Connection connection, ReportBuilder builder) throws ReportError {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            try {
                ResultSet resultSet = statement.executeQuery();
                try {
                    QueryResult result = new QueryResult(resultSet);
                    int resultColumnCount = result.getColumnCount();
                    int[] resultIndex = new int[resultColumnCount];
                    int displayedColumnCount = 0;
                    for (int col = 0; col < resultColumnCount; col++) {
                        if (!isHidden(result.getColumnName(col))) {
                            resultIndex[displayedColumnCount++] = col;
                        }
                    }
                    Object report = builder.createReport();
                    Object table = builder.createTable(report);
                    Object head = builder.createHead(table);
                    {
                        for (int col = 0; col < displayedColumnCount; col++) {
                            builder.createColumn(table, col, result.getColumnLabel(resultIndex[col]));
                        }
                    }
                    builder.finishHead(head);
                    Object body = builder.createBody(table);
                    {
                        initColumnIndices(result.getColumnIndexMapping());
                        String[] values = new String[resultColumnCount];
                        while (resultSet.next()) {
                            result.fillValues(values);
                            updateGroups(values, builder, body);
                            Object row = builder.createRow(table);
                            {
                                for (int col = 0; col < displayedColumnCount; col++) {
                                    builder.createCell(row, col, values[resultIndex[col]]);
                                }
                            }
                            builder.finishRow(row);
                        }
                    }
                    builder.finishBody(body);
                    builder.finishTable(table);
                    builder.finishReport(report);
                } finally {
                    resultSet.close();
                }
            } finally {
                statement.close();
            }
        } catch (SQLException ex) {
            throw new ReportError("Database reported failure: " + ex.getMessage(), ex);
        }
    }
}
