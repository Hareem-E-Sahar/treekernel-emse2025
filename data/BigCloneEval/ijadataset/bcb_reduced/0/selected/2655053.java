package org.hsqldb;

import org.hsqldb.HsqlNameManager.HsqlName;
import org.hsqldb.error.Error;
import org.hsqldb.error.ErrorCode;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.result.Result;
import org.hsqldb.rights.Grantee;
import org.hsqldb.rights.User;
import org.hsqldb.types.DTIType;
import org.hsqldb.types.IntervalSecondData;
import org.hsqldb.types.Type;

/**
 * Implementation of Statement for SQL session statements.<p>
 *
 * @author Fred Toussi (fredt@users dot sourceforge.net)
 * @version 1.9.0
 * @since 1.9.0
 */
public class StatementSession extends Statement {

    Expression[] expressions;

    Object[] parameters;

    StatementSession(int type, Expression[] args) {
        super(type);
        this.expressions = args;
        isTransactionStatement = false;
        switch(type) {
            case StatementTypes.SET_PATH:
            case StatementTypes.SET_TIME_ZONE:
            case StatementTypes.SET_NAMES:
            case StatementTypes.SET_ROLE:
            case StatementTypes.SET_SCHEMA:
            case StatementTypes.SET_CATALOG:
            case StatementTypes.SET_SESSION_AUTHORIZATION:
            case StatementTypes.SET_COLLATION:
                group = StatementTypes.X_SQL_SESSION;
                break;
            default:
                throw Error.runtimeError(ErrorCode.U_S0500, "StateemntSession");
        }
    }

    StatementSession(int type, Object[] args) {
        super(type);
        this.parameters = args;
        isTransactionStatement = false;
        isLogged = false;
        switch(type) {
            case StatementTypes.SET_SCHEMA:
                group = StatementTypes.X_SQL_SESSION;
                isLogged = true;
                break;
            case StatementTypes.DECLARE_VARIABLE:
                group = StatementTypes.X_HSQLDB_SESSION;
                isLogged = true;
                break;
            case StatementTypes.ALLOCATE_CURSOR:
                group = StatementTypes.X_SQL_DATA;
                break;
            case StatementTypes.ALLOCATE_DESCRIPTOR:
            case StatementTypes.DEALLOCATE_DESCRIPTOR:
            case StatementTypes.DEALLOCATE_PREPARE:
                group = StatementTypes.X_DYNAMIC;
                break;
            case StatementTypes.DYNAMIC_DELETE_CURSOR:
                group = StatementTypes.X_SQL_DATA_CHANGE;
                break;
            case StatementTypes.DYNAMIC_CLOSE:
            case StatementTypes.DYNAMIC_FETCH:
            case StatementTypes.DYNAMIC_OPEN:
                group = StatementTypes.X_SQL_DATA;
                break;
            case StatementTypes.OPEN:
            case StatementTypes.FETCH:
            case StatementTypes.FREE_LOCATOR:
            case StatementTypes.GET_DESCRIPTOR:
            case StatementTypes.HOLD_LOCATOR:
                group = StatementTypes.X_SQL_DATA;
                break;
            case StatementTypes.PREPARABLE_DYNAMIC_DELETE_CURSOR:
            case StatementTypes.PREPARABLE_DYNAMIC_UPDATE_CURSOR:
            case StatementTypes.PREPARE:
                group = StatementTypes.X_DYNAMIC;
                break;
            case StatementTypes.DISCONNECT:
                group = StatementTypes.X_SQL_CONNECTION;
                break;
            case StatementTypes.SET_CONNECTION:
            case StatementTypes.SET_CONSTRAINT:
            case StatementTypes.SET_DESCRIPTOR:
            case StatementTypes.SET_SESSION_CHARACTERISTICS:
            case StatementTypes.SET_TRANSFORM_GROUP:
            case StatementTypes.SET_SESSION_RESULT_MAX_ROWS:
            case StatementTypes.SET_SESSION_RESULT_MEMORY_ROWS:
            case StatementTypes.SET_SESSION_AUTOCOMMIT:
            case StatementTypes.SET_SESSION_SQL_IGNORECASE:
                group = StatementTypes.X_HSQLDB_SESSION;
                break;
            case StatementTypes.COMMIT_WORK:
            case StatementTypes.RELEASE_SAVEPOINT:
            case StatementTypes.ROLLBACK_SAVEPOINT:
            case StatementTypes.ROLLBACK_WORK:
            case StatementTypes.SAVEPOINT:
            case StatementTypes.SET_TRANSACTION:
            case StatementTypes.START_TRANSACTION:
                group = StatementTypes.X_SQL_TRANSACTION;
                break;
            case StatementTypes.DECLARE_SESSION_TABLE:
            case StatementTypes.DROP_TABLE:
                group = StatementTypes.X_SQL_SESSION;
                break;
            default:
                throw Error.runtimeError(ErrorCode.U_S0500, "StatementSession");
        }
    }

    StatementSession(int type, HsqlName[] readNames, HsqlName[] writeNames) {
        super(type);
        this.isTransactionStatement = true;
        this.readTableNames = readNames;
        writeTableNames = writeNames;
        switch(type) {
            case StatementTypes.TRANSACTION_LOCK_TABLE:
                group = StatementTypes.X_HSQLDB_TRANSACTION;
                break;
            default:
                throw Error.runtimeError(ErrorCode.U_S0500, "StatementSession");
        }
    }

    public Result execute(Session session) {
        Result result;
        try {
            result = getResult(session);
        } catch (Throwable t) {
            result = Result.newErrorResult(t, null);
        }
        if (result.isError()) {
            result.getException().setStatementType(group, type);
            return result;
        }
        try {
            if (isLogged) {
                session.database.logger.writeOtherStatement(session, sql);
            }
        } catch (Throwable e) {
            return Result.newErrorResult(e, sql);
        }
        return result;
    }

    Result getResult(Session session) {
        boolean startTransaction = false;
        if (this.isExplain) {
            return Result.newSingleColumnStringResult("OPERATION", describe(session));
        }
        switch(type) {
            case StatementTypes.ALLOCATE_CURSOR:
            case StatementTypes.ALLOCATE_DESCRIPTOR:
                return Result.updateZeroResult;
            case StatementTypes.COMMIT_WORK:
                {
                    try {
                        boolean chain = parameters != null;
                        session.commit(chain);
                        return Result.updateZeroResult;
                    } catch (HsqlException e) {
                        return Result.newErrorResult(e, sql);
                    }
                }
            case StatementTypes.DEALLOCATE_DESCRIPTOR:
            case StatementTypes.DEALLOCATE_PREPARE:
                return Result.updateZeroResult;
            case StatementTypes.DISCONNECT:
                session.close();
                return Result.updateZeroResult;
            case StatementTypes.DYNAMIC_CLOSE:
            case StatementTypes.DYNAMIC_DELETE_CURSOR:
            case StatementTypes.DYNAMIC_FETCH:
            case StatementTypes.DYNAMIC_OPEN:
            case StatementTypes.FETCH:
            case StatementTypes.FREE_LOCATOR:
            case StatementTypes.GET_DESCRIPTOR:
            case StatementTypes.HOLD_LOCATOR:
            case StatementTypes.OPEN:
            case StatementTypes.PREPARABLE_DYNAMIC_DELETE_CURSOR:
            case StatementTypes.PREPARABLE_DYNAMIC_UPDATE_CURSOR:
            case StatementTypes.PREPARE:
                return Result.updateZeroResult;
            case StatementTypes.TRANSACTION_LOCK_TABLE:
                {
                    return Result.updateZeroResult;
                }
            case StatementTypes.RELEASE_SAVEPOINT:
                {
                    String savepoint = (String) parameters[0];
                    try {
                        session.releaseSavepoint(savepoint);
                        return Result.updateZeroResult;
                    } catch (HsqlException e) {
                        return Result.newErrorResult(e, sql);
                    }
                }
            case StatementTypes.ROLLBACK_WORK:
                {
                    boolean chain = ((Boolean) parameters[0]).booleanValue();
                    session.rollback(chain);
                    return Result.updateZeroResult;
                }
            case StatementTypes.ROLLBACK_SAVEPOINT:
                {
                    String savepoint = (String) parameters[0];
                    try {
                        session.rollbackToSavepoint(savepoint);
                        return Result.updateZeroResult;
                    } catch (HsqlException e) {
                        return Result.newErrorResult(e, sql);
                    }
                }
            case StatementTypes.SAVEPOINT:
                {
                    String savepoint = (String) parameters[0];
                    session.savepoint(savepoint);
                    return Result.updateZeroResult;
                }
            case StatementTypes.SET_CATALOG:
                {
                    String name;
                    try {
                        name = (String) expressions[0].getValue(session);
                        name = (String) Type.SQL_VARCHAR.trim(session, name, ' ', true, true);
                        if (session.database.getCatalogName().name.equals(name)) {
                            return Result.updateZeroResult;
                        }
                        return Result.newErrorResult(Error.error(ErrorCode.X_3D000), sql);
                    } catch (HsqlException e) {
                        return Result.newErrorResult(e, sql);
                    }
                }
            case StatementTypes.SET_CONNECTION:
            case StatementTypes.SET_CONSTRAINT:
            case StatementTypes.SET_DESCRIPTOR:
                return Result.updateZeroResult;
            case StatementTypes.SET_TIME_ZONE:
                {
                    Object value = null;
                    if (expressions[0].getType() == OpTypes.VALUE && expressions[0].getConstantValueNoCheck(session) == null) {
                        session.setZoneSeconds(session.sessionTimeZoneSeconds);
                        return Result.updateZeroResult;
                    }
                    try {
                        value = expressions[0].getValue(session);
                    } catch (HsqlException e) {
                    }
                    if (value instanceof Result) {
                        Result result = (Result) value;
                        if (result.isData()) {
                            Object[] data = (Object[]) result.getNavigator().getNext();
                            boolean single = !result.getNavigator().next();
                            if (single && data != null && data[0] != null) {
                                value = data[0];
                                result.getNavigator().close();
                            } else {
                                result.getNavigator().close();
                                return Result.newErrorResult(Error.error(ErrorCode.X_22009), sql);
                            }
                        } else {
                            return Result.newErrorResult(Error.error(ErrorCode.X_22009), sql);
                        }
                    } else {
                        if (value == null) {
                            return Result.newErrorResult(Error.error(ErrorCode.X_22009), sql);
                        }
                    }
                    long seconds = ((IntervalSecondData) value).getSeconds();
                    if (-DTIType.timezoneSecondsLimit <= seconds && seconds <= DTIType.timezoneSecondsLimit) {
                        session.setZoneSeconds((int) seconds);
                        return Result.updateZeroResult;
                    }
                    return Result.newErrorResult(Error.error(ErrorCode.X_22009), sql);
                }
            case StatementTypes.SET_NAMES:
                return Result.updateZeroResult;
            case StatementTypes.SET_PATH:
                return Result.updateZeroResult;
            case StatementTypes.SET_ROLE:
                {
                    String name;
                    Grantee role = null;
                    try {
                        name = (String) expressions[0].getValue(session);
                        if (name != null) {
                            name = (String) Type.SQL_VARCHAR.trim(session, name, ' ', true, true);
                            role = session.database.granteeManager.getRole(name);
                        }
                    } catch (HsqlException e) {
                        return Result.newErrorResult(Error.error(ErrorCode.X_0P000), sql);
                    }
                    if (session.isInMidTransaction()) {
                        return Result.newErrorResult(Error.error(ErrorCode.X_25001), sql);
                    }
                    if (role == null) {
                        session.setRole(null);
                    }
                    if (session.getGrantee().hasRole(role)) {
                        session.setRole(role);
                        return Result.updateZeroResult;
                    } else {
                        return Result.newErrorResult(Error.error(ErrorCode.X_0P000), sql);
                    }
                }
            case StatementTypes.SET_SCHEMA:
                {
                    String name;
                    HsqlName schema;
                    try {
                        if (expressions == null) {
                            name = ((HsqlName) parameters[0]).name;
                        } else {
                            name = (String) expressions[0].getValue(session);
                        }
                        name = (String) Type.SQL_VARCHAR.trim(session, name, ' ', true, true);
                        schema = session.database.schemaManager.getSchemaHsqlName(name);
                        session.setCurrentSchemaHsqlName(schema);
                        return Result.updateZeroResult;
                    } catch (HsqlException e) {
                        return Result.newErrorResult(e, sql);
                    }
                }
            case StatementTypes.SET_SESSION_AUTHORIZATION:
                {
                    if (session.isInMidTransaction()) {
                        return Result.newErrorResult(Error.error(ErrorCode.X_25001), sql);
                    }
                    try {
                        String user;
                        String password = null;
                        user = (String) expressions[0].getValue(session);
                        user = (String) Type.SQL_VARCHAR.trim(session, user, ' ', true, true);
                        if (expressions[1] != null) {
                            password = (String) expressions[1].getValue(session);
                        }
                        User userObject;
                        if (password == null) {
                            userObject = session.database.userManager.get(user);
                        } else {
                            userObject = session.database.getUserManager().getUser(user, password);
                        }
                        if (userObject == null) {
                            throw Error.error(ErrorCode.X_28501);
                        }
                        sql = userObject.getConnectUserSQL();
                        if (userObject == session.getGrantee()) {
                            return Result.updateZeroResult;
                        }
                        if (password == null && !session.isProcessingLog() && userObject.isAdmin() && !session.getGrantee().isAdmin()) {
                            throw Error.error(ErrorCode.X_28000);
                        }
                        if (session.getGrantee().canChangeAuthorisation()) {
                            session.setUser((User) userObject);
                            session.setRole(null);
                            session.resetSchema();
                            return Result.updateZeroResult;
                        }
                        throw Error.error(ErrorCode.X_28000);
                    } catch (HsqlException e) {
                        return Result.newErrorResult(e, sql);
                    }
                }
            case StatementTypes.SET_SESSION_CHARACTERISTICS:
                {
                    try {
                        if (parameters[0] != null) {
                            boolean readonly = ((Boolean) parameters[0]).booleanValue();
                            session.setReadOnlyDefault(readonly);
                        }
                        if (parameters[1] != null) {
                            int level = ((Integer) parameters[1]).intValue();
                            session.setIsolationDefault(level);
                        }
                        return Result.updateZeroResult;
                    } catch (HsqlException e) {
                        return Result.newErrorResult(e, sql);
                    }
                }
            case StatementTypes.SET_COLLATION:
                return Result.updateZeroResult;
            case StatementTypes.SET_TRANSFORM_GROUP:
                return Result.updateZeroResult;
            case StatementTypes.START_TRANSACTION:
                startTransaction = true;
            case StatementTypes.SET_TRANSACTION:
                {
                    try {
                        if (parameters[0] != null) {
                            boolean readonly = ((Boolean) parameters[0]).booleanValue();
                            session.setReadOnly(readonly);
                        }
                        if (parameters[1] != null) {
                            int level = ((Integer) parameters[1]).intValue();
                            session.setIsolation(level);
                        }
                        if (startTransaction) {
                            session.startTransaction();
                        }
                        return Result.updateZeroResult;
                    } catch (HsqlException e) {
                        return Result.newErrorResult(e, sql);
                    }
                }
            case StatementTypes.SET_SESSION_AUTOCOMMIT:
                {
                    boolean mode = ((Boolean) parameters[0]).booleanValue();
                    try {
                        session.setAutoCommit(mode);
                        return Result.updateZeroResult;
                    } catch (HsqlException e) {
                        return Result.newErrorResult(e, sql);
                    }
                }
            case StatementTypes.DECLARE_VARIABLE:
                {
                    ColumnSchema[] variables = (ColumnSchema[]) parameters[0];
                    try {
                        for (int i = 0; i < variables.length; i++) {
                            session.sessionContext.addSessionVariable(variables[i]);
                        }
                        return Result.updateZeroResult;
                    } catch (HsqlException e) {
                        return Result.newErrorResult(e, sql);
                    }
                }
            case StatementTypes.SET_SESSION_RESULT_MAX_ROWS:
                {
                    int size = ((Integer) parameters[0]).intValue();
                    session.setSQLMaxRows(size);
                    return Result.updateZeroResult;
                }
            case StatementTypes.SET_SESSION_RESULT_MEMORY_ROWS:
                {
                    int size = ((Integer) parameters[0]).intValue();
                    session.setResultMemoryRowCount(size);
                    return Result.updateZeroResult;
                }
            case StatementTypes.SET_SESSION_SQL_IGNORECASE:
                {
                    try {
                        boolean mode = ((Boolean) parameters[0]).booleanValue();
                        session.setIgnoreCase(mode);
                        return Result.updateZeroResult;
                    } catch (HsqlException e) {
                        return Result.newErrorResult(e, sql);
                    }
                }
            case StatementTypes.DECLARE_SESSION_TABLE:
                {
                    Table table = (Table) parameters[0];
                    HsqlArrayList tempConstraints = (HsqlArrayList) parameters[1];
                    StatementDMQL statement = (StatementDMQL) parameters[2];
                    try {
                        if (tempConstraints.size() != 0) {
                            table = ParserDDL.addTableConstraintDefinitions(session, table, tempConstraints, null, false);
                        }
                        table.compile(session, null);
                        session.sessionContext.addSessionTable(table);
                        if (table.hasLobColumn) {
                            throw Error.error(ErrorCode.X_07000);
                        }
                        if (statement != null) {
                            Result result = statement.execute(session);
                            table.insertIntoTable(session, result);
                        }
                        return Result.updateZeroResult;
                    } catch (HsqlException e) {
                        return Result.newErrorResult(e, sql);
                    }
                }
            case StatementTypes.DROP_TABLE:
                {
                    HsqlName name = (HsqlName) parameters[0];
                    Boolean ifExists = (Boolean) parameters[1];
                    Table table = session.sessionContext.findSessionTable(name.name);
                    if (table == null) {
                        if (ifExists.booleanValue()) {
                            return Result.updateZeroResult;
                        } else {
                            throw Error.error(ErrorCode.X_42501, name.name);
                        }
                    }
                    session.sessionContext.dropSessionTable(name.name);
                    return Result.updateZeroResult;
                }
            default:
                throw Error.runtimeError(ErrorCode.U_S0500, "StatementSession");
        }
    }

    public boolean isAutoCommitStatement() {
        return false;
    }

    public String describe(Session session) {
        return sql;
    }

    public boolean isCatalogChange() {
        return false;
    }
}
