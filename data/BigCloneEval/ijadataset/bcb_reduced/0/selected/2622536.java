package org.hsqldb;

import org.hsqldb.HsqlNameManager.HsqlName;
import org.hsqldb.error.Error;
import org.hsqldb.error.ErrorCode;
import org.hsqldb.lib.HashMappedList;
import org.hsqldb.lib.HashSet;
import org.hsqldb.lib.OrderedHashSet;
import org.hsqldb.lib.OrderedIntHashSet;
import org.hsqldb.result.Result;
import org.hsqldb.result.ResultConstants;
import org.hsqldb.types.Type;

/**
 * Implementation of Statement for PSM compound statements.

 * @author Fred Toussi (fredt@users dot sourceforge.net)
 * @version 1.9.0
 * @since 1.9.0
 */
public class StatementCompound extends Statement {

    final boolean isLoop;

    HsqlName label;

    StatementHandler[] handlers = StatementHandler.emptyExceptionHandlerArray;

    boolean hasUndoHandler;

    StatementQuery loopCursor;

    Statement[] statements;

    StatementExpression condition;

    boolean isAtomic;

    ColumnSchema[] variables = ColumnSchema.emptyArray;

    StatementCursor[] cursors = StatementCursor.emptyArray;

    HashMappedList scopeVariables;

    RangeVariable[] rangeVariables = RangeVariable.emptyArray;

    Table[] tables = Table.emptyArray;

    HashMappedList scopeTables;

    public static final StatementCompound[] emptyStatementArray = new StatementCompound[] {};

    StatementCompound(int type, HsqlName label) {
        super(type, StatementTypes.X_SQL_CONTROL);
        this.label = label;
        isTransactionStatement = false;
        switch(type) {
            case StatementTypes.FOR:
            case StatementTypes.LOOP:
            case StatementTypes.WHILE:
            case StatementTypes.REPEAT:
                isLoop = true;
                break;
            case StatementTypes.BEGIN_END:
            case StatementTypes.IF:
                isLoop = false;
                break;
            default:
                throw Error.runtimeError(ErrorCode.U_S0500, "StatementCompound");
        }
    }

    public String getSQL() {
        return sql;
    }

    protected String describe(Session session, int blanks) {
        StringBuffer sb = new StringBuffer();
        sb.append('\n');
        for (int i = 0; i < blanks; i++) {
            sb.append(' ');
        }
        sb.append(Tokens.T_STATEMENT);
        return sb.toString();
    }

    public void setLocalDeclarations(Object[] declarations) {
        int varCount = 0;
        int handlerCount = 0;
        int cursorCount = 0;
        int tableCount = 0;
        for (int i = 0; i < declarations.length; i++) {
            if (declarations[i] instanceof ColumnSchema) {
                varCount++;
            } else if (declarations[i] instanceof StatementHandler) {
                handlerCount++;
            } else if (declarations[i] instanceof Table) {
                tableCount++;
            } else {
                cursorCount++;
            }
        }
        if (varCount > 0) {
            variables = new ColumnSchema[varCount];
        }
        if (handlerCount > 0) {
            handlers = new StatementHandler[handlerCount];
        }
        if (tableCount > 0) {
            tables = new Table[tableCount];
        }
        if (cursorCount > 0) {
            cursors = new StatementCursor[cursorCount];
        }
        varCount = 0;
        handlerCount = 0;
        tableCount = 0;
        cursorCount = 0;
        for (int i = 0; i < declarations.length; i++) {
            if (declarations[i] instanceof ColumnSchema) {
                variables[varCount++] = (ColumnSchema) declarations[i];
            } else if (declarations[i] instanceof StatementHandler) {
                StatementHandler handler = (StatementHandler) declarations[i];
                handler.setParent(this);
                handlers[handlerCount++] = handler;
                if (handler.handlerType == StatementHandler.UNDO) {
                    hasUndoHandler = true;
                }
            } else if (declarations[i] instanceof Table) {
                Table table = (Table) declarations[i];
                tables[tableCount++] = table;
            } else {
                StatementCursor cursor = (StatementCursor) declarations[i];
                cursors[cursorCount++] = cursor;
            }
        }
        setVariables();
        setHandlers();
        setTables();
        setCursors();
    }

    public void setLoopStatement(StatementQuery cursorStatement) {
        loopCursor = cursorStatement;
        HsqlName[] colNames = cursorStatement.queryExpression.getResultColumnNames();
        Type[] colTypes = cursorStatement.queryExpression.getColumnTypes();
        ColumnSchema[] columns = new ColumnSchema[colNames.length];
        for (int i = 0; i < colNames.length; i++) {
            columns[i] = new ColumnSchema(colNames[i], colTypes[i], false, false, null);
            columns[i].setParameterMode(SchemaObject.ParameterModes.PARAM_IN);
        }
        setLocalDeclarations(columns);
    }

    void setStatements(Statement[] statements) {
        for (int i = 0; i < statements.length; i++) {
            statements[i].setParent(this);
        }
        this.statements = statements;
    }

    public void setCondition(StatementExpression condition) {
        this.condition = condition;
    }

    public Result execute(Session session) {
        Result result;
        switch(type) {
            case StatementTypes.BEGIN_END:
                {
                    initialiseVariables(session);
                    result = executeBlock(session);
                    break;
                }
            case StatementTypes.FOR:
                result = executeForLoop(session);
                break;
            case StatementTypes.LOOP:
            case StatementTypes.WHILE:
            case StatementTypes.REPEAT:
                {
                    result = executeLoop(session);
                    break;
                }
            case StatementTypes.IF:
                {
                    result = executeIf(session);
                    break;
                }
            default:
                throw Error.runtimeError(ErrorCode.U_S0500, "StatementCompound");
        }
        if (result.isError()) {
            result.getException().setStatementType(group, type);
        }
        return result;
    }

    private Result executeBlock(Session session) {
        Result result = Result.updateZeroResult;
        boolean push = !root.isTrigger();
        if (push) {
            session.sessionContext.push();
            if (hasUndoHandler) {
                String name = HsqlNameManager.getAutoSavepointNameString(session.actionTimestamp, session.sessionContext.depth);
                session.savepoint(name);
            }
        }
        for (int i = 0; i < statements.length; i++) {
            result = statements[i].execute(session);
            result = handleCondition(session, result);
            if (result.isError()) {
                break;
            }
            if (result.getType() == ResultConstants.VALUE || result.getType() == ResultConstants.DATA) {
                break;
            }
        }
        if (result.getType() == ResultConstants.VALUE) {
            if (result.getErrorCode() == StatementTypes.LEAVE) {
                if (result.getMainString() == null) {
                    result = Result.updateZeroResult;
                } else if (label != null && label.name.equals(result.getMainString())) {
                    result = Result.updateZeroResult;
                }
            }
        }
        if (push) {
            session.sessionContext.pop();
        }
        return result;
    }

    private Result handleCondition(Session session, Result result) {
        String sqlState = null;
        if (result.isError()) {
            sqlState = result.getSubString();
        } else if (session.getLastWarning() != null) {
            sqlState = session.getLastWarning().getSQLState();
        } else {
            return result;
        }
        if (sqlState != null) {
            for (int i = 0; i < handlers.length; i++) {
                StatementHandler handler = handlers[i];
                session.clearWarnings();
                if (handler.handlesCondition(result.getSubString())) {
                    session.resetSchema();
                    switch(handler.handlerType) {
                        case StatementHandler.CONTINUE:
                            result = Result.updateZeroResult;
                            break;
                        case StatementHandler.UNDO:
                            session.rollbackToSavepoint();
                            result = Result.newPSMResult(StatementTypes.LEAVE, label.name, null);
                            break;
                        case StatementHandler.EXIT:
                            result = Result.newPSMResult(StatementTypes.LEAVE, null, null);
                            break;
                    }
                    Result actionResult = handler.statement.execute(session);
                    if (actionResult.isError()) {
                        result = actionResult;
                        handleCondition(session, result);
                    } else {
                        return result;
                    }
                }
            }
            if (parent != null) {
                return parent.handleCondition(session, result);
            }
        }
        return result;
    }

    private Result executeForLoop(Session session) {
        Result queryResult = loopCursor.getResult(session);
        if (queryResult.isError()) {
            return queryResult;
        }
        Result result = Result.updateZeroResult;
        while (queryResult.navigator.hasNext()) {
            queryResult.navigator.next();
            Object[] data = queryResult.navigator.getCurrent();
            initialiseVariables(session, data);
            for (int i = 0; i < statements.length; i++) {
                result = statements[i].execute(session);
                if (result.isError()) {
                    break;
                }
                if (result.getType() == ResultConstants.VALUE) {
                    break;
                }
                if (result.getType() == ResultConstants.DATA) {
                    break;
                }
            }
            if (result.isError()) {
                break;
            }
            if (result.getType() == ResultConstants.VALUE) {
                if (result.getErrorCode() == StatementTypes.ITERATE) {
                    if (result.getMainString() == null) {
                        continue;
                    }
                    if (label != null && label.name.equals(result.getMainString())) {
                        continue;
                    }
                    break;
                }
                if (result.getErrorCode() == StatementTypes.LEAVE) {
                    break;
                }
                break;
            }
            if (result.getType() == ResultConstants.DATA) {
                break;
            }
        }
        queryResult.navigator.close();
        return result;
    }

    private Result executeLoop(Session session) {
        Result result = Result.updateZeroResult;
        while (true) {
            if (type == StatementTypes.WHILE) {
                result = condition.execute(session);
                if (result.isError()) {
                    break;
                }
                if (!Boolean.TRUE.equals(result.getValueObject())) {
                    result = Result.updateZeroResult;
                    break;
                }
            }
            for (int i = 0; i < statements.length; i++) {
                result = statements[i].execute(session);
                if (result.isError()) {
                    break;
                }
                if (result.getType() == ResultConstants.VALUE) {
                    break;
                }
                if (result.getType() == ResultConstants.DATA) {
                    break;
                }
            }
            if (result.isError()) {
                break;
            }
            if (result.getType() == ResultConstants.VALUE) {
                if (result.getErrorCode() == StatementTypes.ITERATE) {
                    if (result.getMainString() == null) {
                        continue;
                    }
                    if (label != null && label.name.equals(result.getMainString())) {
                        continue;
                    }
                    break;
                }
                if (result.getErrorCode() == StatementTypes.LEAVE) {
                    if (result.getMainString() == null) {
                        result = Result.updateZeroResult;
                    }
                    if (label != null && label.name.equals(result.getMainString())) {
                        result = Result.updateZeroResult;
                    }
                    break;
                }
                break;
            }
            if (result.getType() == ResultConstants.DATA) {
                break;
            }
            if (type == StatementTypes.REPEAT) {
                result = condition.execute(session);
                if (result.isError()) {
                    break;
                }
                if (Boolean.TRUE.equals(result.getValueObject())) {
                    result = Result.updateZeroResult;
                    break;
                }
            }
        }
        return result;
    }

    private Result executeIf(Session session) {
        Result result = Result.updateZeroResult;
        boolean execute = false;
        for (int i = 0; i < statements.length; i++) {
            if (statements[i].getType() == StatementTypes.CONDITION) {
                if (execute) {
                    break;
                }
                result = statements[i].execute(session);
                if (result.isError()) {
                    break;
                }
                Object value = result.getValueObject();
                execute = Boolean.TRUE.equals(value);
                i++;
            }
            result = Result.updateZeroResult;
            if (!execute) {
                continue;
            }
            result = statements[i].execute(session);
            if (result.isError()) {
                break;
            }
            if (result.getType() == ResultConstants.VALUE) {
                break;
            }
        }
        return result;
    }

    public void resolve(Session session) {
        for (int i = 0; i < statements.length; i++) {
            if (statements[i].getType() == StatementTypes.LEAVE || statements[i].getType() == StatementTypes.ITERATE) {
                if (!findLabel((StatementSimple) statements[i])) {
                    throw Error.error(ErrorCode.X_42508, ((StatementSimple) statements[i]).label.name);
                }
                continue;
            }
            if (statements[i].getType() == StatementTypes.RETURN) {
                if (!root.isFunction()) {
                    throw Error.error(ErrorCode.X_42602, Tokens.T_RETURN);
                }
            }
        }
        for (int i = 0; i < statements.length; i++) {
            statements[i].resolve(session);
        }
        for (int i = 0; i < handlers.length; i++) {
            handlers[i].resolve(session);
        }
        OrderedHashSet writeTableNamesSet = new OrderedHashSet();
        OrderedHashSet readTableNamesSet = new OrderedHashSet();
        OrderedHashSet set = new OrderedHashSet();
        for (int i = 0; i < variables.length; i++) {
            set.addAll(variables[i].getReferences());
        }
        if (condition != null) {
            set.addAll(condition.getReferences());
            readTableNamesSet.addAll(condition.getTableNamesForRead());
        }
        for (int i = 0; i < statements.length; i++) {
            set.addAll(statements[i].getReferences());
            readTableNamesSet.addAll(statements[i].getTableNamesForRead());
            writeTableNamesSet.addAll(statements[i].getTableNamesForWrite());
        }
        for (int i = 0; i < handlers.length; i++) {
            set.addAll(handlers[i].getReferences());
            readTableNamesSet.addAll(handlers[i].getTableNamesForRead());
            writeTableNamesSet.addAll(handlers[i].getTableNamesForWrite());
        }
        readTableNamesSet.removeAll(writeTableNamesSet);
        readTableNames = new HsqlName[readTableNamesSet.size()];
        readTableNamesSet.toArray(readTableNames);
        writeTableNames = new HsqlName[writeTableNamesSet.size()];
        writeTableNamesSet.toArray(writeTableNames);
        references = set;
    }

    public void setRoot(Routine routine) {
        root = routine;
    }

    public String describe(Session session) {
        return "";
    }

    public OrderedHashSet getReferences() {
        return references;
    }

    public void setAtomic(boolean atomic) {
        this.isAtomic = atomic;
    }

    private void setVariables() {
        HashMappedList list = new HashMappedList();
        if (variables.length == 0) {
            if (parent == null) {
                rangeVariables = root.getParameterRangeVariables();
            } else {
                rangeVariables = parent.rangeVariables;
            }
            scopeVariables = list;
            return;
        }
        if (parent != null && parent.scopeVariables != null) {
            for (int i = 0; i < parent.scopeVariables.size(); i++) {
                list.add(parent.scopeVariables.getKey(i), parent.scopeVariables.get(i));
            }
        }
        for (int i = 0; i < variables.length; i++) {
            String name = variables[i].getName().name;
            boolean added = list.add(name, variables[i]);
            if (!added) {
                throw Error.error(ErrorCode.X_42606, name);
            }
            if (root.getParameterIndex(name) != -1) {
                throw Error.error(ErrorCode.X_42606, name);
            }
        }
        scopeVariables = list;
        RangeVariable range = new RangeVariable(list, null, true, RangeVariable.VARIALBE_RANGE);
        rangeVariables = new RangeVariable[] { root.getParameterRangeVariables()[0], range };
        root.variableCount = list.size();
    }

    private void setHandlers() {
        if (handlers.length == 0) {
            return;
        }
        HashSet statesSet = new HashSet();
        OrderedIntHashSet typesSet = new OrderedIntHashSet();
        for (int i = 0; i < handlers.length; i++) {
            int[] types = handlers[i].getConditionTypes();
            for (int j = 0; j < types.length; j++) {
                if (!typesSet.add(types[j])) {
                    throw Error.error(ErrorCode.X_42601);
                }
            }
            String[] states = handlers[i].getConditionStates();
            for (int j = 0; j < states.length; j++) {
                if (!statesSet.add(states[j])) {
                    throw Error.error(ErrorCode.X_42601);
                }
            }
        }
    }

    private void setTables() {
        if (tables.length == 0) {
            return;
        }
        HashMappedList list = new HashMappedList();
        if (parent != null && parent.scopeTables != null) {
            for (int i = 0; i < parent.scopeTables.size(); i++) {
                list.add(parent.scopeTables.getKey(i), parent.scopeTables.get(i));
            }
        }
        for (int i = 0; i < tables.length; i++) {
            String name = tables[i].getName().name;
            boolean added = list.add(name, tables[i]);
            if (!added) {
                throw Error.error(ErrorCode.X_42606, name);
            }
        }
        scopeTables = list;
    }

    private void setCursors() {
        if (cursors.length == 0) {
            return;
        }
        HashSet list = new HashSet();
        for (int i = 0; i < cursors.length; i++) {
            StatementCursor cursor = cursors[i];
            boolean added = list.add(cursor.getCursorName().name);
            if (!added) {
                throw Error.error(ErrorCode.X_42606, cursor.getCursorName().name);
            }
        }
    }

    private boolean findLabel(StatementSimple statement) {
        if (label != null && statement.label.name.equals(label.name)) {
            if (!isLoop && statement.getType() == StatementTypes.ITERATE) {
                return false;
            }
            return true;
        }
        if (parent == null) {
            return false;
        }
        return parent.findLabel(statement);
    }

    private void initialiseVariables(Session session) {
        Object[] vars = session.sessionContext.routineVariables;
        int offset = parent == null ? 0 : parent.scopeVariables.size();
        for (int i = 0; i < variables.length; i++) {
            try {
                vars[offset + i] = variables[i].getDefaultValue(session);
            } catch (HsqlException e) {
            }
        }
    }

    private void initialiseVariables(Session session, Object[] data) {
        Object[] vars = session.sessionContext.routineVariables;
        int offset = parent == null ? 0 : parent.scopeVariables.size();
        for (int i = 0; i < data.length; i++) {
            try {
                vars[offset + i] = data[i];
            } catch (HsqlException e) {
            }
        }
    }

    public RangeVariable[] getRangeVariables() {
        return rangeVariables;
    }
}
