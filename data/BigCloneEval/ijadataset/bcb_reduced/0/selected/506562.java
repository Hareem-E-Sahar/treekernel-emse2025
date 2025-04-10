package org.eclipse.datatools.sqltools.core.services;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.datatools.connectivity.sqm.core.definition.DatabaseDefinition;
import org.eclipse.datatools.modelbase.sql.schema.SQLObject;
import org.eclipse.datatools.sqltools.core.EditorCorePlugin;
import org.eclipse.datatools.sqltools.editor.template.GenericSQLContextType;
import org.eclipse.datatools.sqltools.internal.core.Messages;
import org.eclipse.datatools.sqltools.sql.ISQLSyntax;
import org.eclipse.datatools.sqltools.sql.identifier.IIdentifierValidator;
import org.eclipse.datatools.sqltools.sql.parser.ParserParameters;
import org.eclipse.datatools.sqltools.sql.parser.ParserProposalAdvisor;
import org.eclipse.datatools.sqltools.sql.parser.ParsingResult;
import org.eclipse.datatools.sqltools.sql.parser.SQLParser;
import org.eclipse.datatools.sqltools.sql.parser.ast.IASTSQLDelimiter;
import org.eclipse.datatools.sqltools.sql.parser.ast.IASTSQLStatementElement;
import org.eclipse.datatools.sqltools.sql.parser.ast.IASTStart;
import org.eclipse.datatools.sqltools.sql.parser.ast.Node;
import org.eclipse.datatools.sqltools.sql.updater.ProceduralObjectSourceUpdater;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.osgi.util.NLS;

/**
 * A SQL language related service specific to a database definition.
 * @author Hui Cao
 *
 */
public class SQLService {

    /**
     * Returns an <code>ISQLSyntax</code> object which can be used to highlight sql statements in SQL editor.
     * 
     * @return an <code>ISQLSyntax</code> object specific to a SQL dialect.
     */
    public ISQLSyntax getSQLSyntax() {
        return null;
    }

    /**
     * Returns a specific <code>GenericSQLContextType</code> object which identifies the context type of templates
     * used in SQL editor.
     * 
     * @return a <code>GenericSQLContextType</code> object
     */
    public GenericSQLContextType getSQLContextType() {
        return new GenericSQLContextType();
    }

    /**
     * Returns a <code>SQLParser</code> object which is used to parse SQL dialect.
     * 
     * @return a <code>SQLParser</code> object
     */
    public SQLParser getSQLParser() {
        return null;
    }

    /**
     * Splits the sql statement into groups of statements according to SQL statement delimiter such as "go" or ";".
     * 
     * @param sql statement to be splitted
     * @return sql statement array
     */
    public String[] splitSQL(String sql) {
        SQLParser parser = getSQLParser();
        if (parser == null) {
            return new String[] { sql };
        }
        ArrayList groups = new ArrayList();
        try {
            IDocument doc = new Document(sql);
            ParserParameters parserParameters = new ParserParameters(true);
            parserParameters.setProperty(ParserParameters.PARAM_CONSUME_EXCEPTION, Boolean.FALSE);
            ParsingResult result = parser.parse(sql, parserParameters);
            if (result.getExceptions() != null && !result.getExceptions().isEmpty()) {
                return splitSQLByTerminatorLine(sql, parser.getStatementTerminators());
            }
            IASTStart root = result.getRootNode();
            root.setDocument(doc);
            String group = "";
            if (root.jjtGetNumChildren() > 0) {
                for (int i = 0; i < root.jjtGetNumChildren(); i++) {
                    Node node = root.jjtGetChild(i);
                    if (node instanceof IASTSQLDelimiter) {
                        if (!group.trim().equals("")) {
                            groups.add(group);
                            group = "";
                        }
                    } else if (node instanceof IASTSQLStatementElement) {
                        group += node.getSQLText() + " ";
                    } else {
                        group += node.getSQLText() + System.getProperty("line.separator");
                    }
                }
            } else {
                group = sql;
            }
            if (!group.trim().equals("")) {
                groups.add(group);
            }
        } catch (Exception e1) {
            EditorCorePlugin.getDefault().log(NLS.bind(Messages.DefaultSQLSyntax_exception_splitSQL, sql), e1);
        }
        return (String[]) groups.toArray(new String[groups.size()]);
    }

    /**
	 * Splits SQL statements on any ";" characters and stand-alone "GO".
	 */
    public String[] splitSQLByTerminatorLine(String sql, String[] terminators) {
        IDocument doc = new Document(sql);
        ArrayList groups = new ArrayList();
        int index = 0;
        int numberOfLines = doc.getNumberOfLines();
        try {
            for (int i = 0; i < numberOfLines; i++) {
                boolean grouped = false;
                IRegion r = doc.getLineInformation(i);
                String line = doc.get(r.getOffset(), r.getLength());
                for (int j = 0; j < terminators.length; j++) {
                    if (line.trim().equalsIgnoreCase(terminators[j])) {
                        String string = doc.get(index, r.getOffset() - index);
                        if (string.trim().length() > 0) {
                            groups.add(string.trim());
                        }
                        index = r.getOffset() + doc.getLineLength(i);
                        break;
                    } else {
                        int offset = r.getOffset();
                        while (line.indexOf(";") >= 0) {
                            if (line.indexOf(";") >= 0 && !isQuoted(doc, offset + line.indexOf(";") + 1)) {
                                String string = doc.get(index, offset + line.indexOf(";") - index);
                                if (string.trim().length() > 0) {
                                    groups.add(string.trim());
                                }
                                index = offset + line.indexOf(";") + 1;
                                grouped = true;
                                break;
                            }
                            offset += line.indexOf(";") + 1;
                            line = line.substring(line.indexOf(";") + 1);
                        }
                    }
                    if (grouped) {
                        grouped = false;
                        break;
                    }
                }
            }
            if (index < doc.getLength() - 1) {
                String string = doc.get(index, doc.getLength() - index);
                if (string.trim().length() > 0) {
                    groups.add(string);
                }
            }
        } catch (Exception e) {
            return new String[] { sql };
        }
        return (String[]) groups.toArray(new String[groups.size()]);
    }

    /**
	 * Check whether the ";" character is quoted 
	 * @param doc document
	 * @param offset offset of ";" character
	 * @return
	 */
    private boolean isQuoted(IDocument doc, int offset) {
        Pattern pSingle = Pattern.compile("'[^']*('')*[^']*;+[^']*('')*[^']*'");
        Pattern pDouble = Pattern.compile("\"[^\"]*(\"\")*[^\"]*;+[^\"]*(\"\")*[^\"]*\"");
        Matcher mSingle = pSingle.matcher(doc.get());
        while (mSingle.find()) {
            if (mSingle.start() <= offset && mSingle.end() >= offset) {
                return true;
            }
        }
        Matcher mDouble = pDouble.matcher(doc.get());
        while (mDouble.find()) {
            if (mDouble.start() <= offset && mDouble.end() >= offset) {
                return true;
            }
        }
        return false;
    }

    public ParserProposalAdvisor getParserProposalAdvisor() {
        return new ParserProposalAdvisor();
    }

    /**
     * Returns Identifier Validator
     * TODO implement IIdentifierValidator
     * @return
     */
    public IIdentifierValidator getIdentifierValidator() {
        return null;
    }

    /**
     * Returns the ProceduralObjectSourceUpdater object used to update the source of the given sql object 
     * @return
     */
    public ProceduralObjectSourceUpdater getProceduralObjectSourceUpdater(SQLObject object, DatabaseDefinition dbDefinition) {
        return null;
    }
}
