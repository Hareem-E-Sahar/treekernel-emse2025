package org.datanucleus.store.rdbms.query.legacy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ObjectManagerFactoryImpl;
import org.datanucleus.PersistenceConfiguration;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.metadata.Relation;
import org.datanucleus.plugin.ConfigurationElement;
import org.datanucleus.plugin.Extension;
import org.datanucleus.plugin.PluginManager;
import org.datanucleus.query.JDOQLQueryHelper;
import org.datanucleus.query.QueryUtils;
import org.datanucleus.store.mapped.DatastoreClass;
import org.datanucleus.store.mapped.DatastoreIdentifier;
import org.datanucleus.store.mapped.IdentifierType;
import org.datanucleus.store.mapped.MappedStoreManager;
import org.datanucleus.store.mapped.expression.AggregateExpression;
import org.datanucleus.store.mapped.expression.BooleanExpression;
import org.datanucleus.store.mapped.expression.ClassExpression;
import org.datanucleus.store.mapped.expression.CollectionExpression;
import org.datanucleus.store.mapped.expression.Literal;
import org.datanucleus.store.mapped.expression.LogicSetExpression;
import org.datanucleus.store.mapped.expression.MapExpression;
import org.datanucleus.store.mapped.expression.NewObjectExpression;
import org.datanucleus.store.mapped.expression.NullLiteral;
import org.datanucleus.store.mapped.expression.QueryExpression;
import org.datanucleus.store.mapped.expression.Queryable;
import org.datanucleus.store.mapped.expression.ScalarExpression;
import org.datanucleus.store.mapped.expression.UnboundVariable;
import org.datanucleus.store.mapped.mapping.JavaTypeMapping;
import org.datanucleus.store.query.AbstractJavaQuery;
import org.datanucleus.store.query.Query;
import org.datanucleus.store.query.QueryCompilerSyntaxException;
import org.datanucleus.store.rdbms.table.CollectionTable;
import org.datanucleus.store.rdbms.table.MapTable;
import org.datanucleus.util.ClassUtils;
import org.datanucleus.util.Imports;
import org.datanucleus.util.Localiser;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.StringUtils;

/**
 * Base definition of a query compiler for a query language.
 * Should be extended to compile details of individual query languages.
 * This was designed for the old-style compilation process, and is being phased out.
 * New implementations of querying in DataNucleus should make use of compilers under
 * "org.datanucleus.query.compiler".
 */
public abstract class QueryCompiler implements UnboundVariable.VariableBinder {

    public static final int COMPILE_EXPLICIT_PARAMETERS = 1;

    public static final int COMPILE_EXPLICIT_VARIABLES = 2;

    public static final int COMPILE_SYNTAX = 3;

    public static final int COMPILE_EXECUTION = 4;

    /** Localiser for messages. */
    protected static final Localiser LOCALISER = Localiser.getInstance("org.datanucleus.Localisation", ObjectManagerFactoryImpl.class.getClassLoader());

    /** Language of this query (e.g JDOQL, JPQL). */
    protected String language;

    /** The query being compiled. */
    protected Query query;

    /** Imports to use for the resolution of classes. */
    protected Imports imports;

    /** Map of parameter values, keyed by their name. */
    protected Map parameters;

    /** Flag for whether the current compile is for execution (using param values). */
    protected boolean executionCompile = true;

    /** Candidate class for the query. May be updated during the compilation. */
    protected Class candidateClass = null;

    /** Alias for the candidate class. */
    protected String candidateAlias = "this";

    /** The parameter names. */
    protected List<String> parameterNames = null;

    /** Look-up for the parameter types, keyed by the name. */
    protected Map<String, Class> parameterTypesByName = null;

    /** List of variable names. */
    protected List<String> variableNames = null;

    /** Look-up for the variables types, keyed by the name. */
    protected Map<String, Class> variableTypesByName = null;

    /** Internal list of field expressions just being parsed. Used for checking validity of the query components. */
    List<ScalarExpression> fieldExpressions = new ArrayList();

    /** ResultMetaData for the query (set in the compile process, so is null before that). */
    protected QueryResultsMetaData resultMetaData;

    /** QueryExpression for this query. Generated by the compile. */
    protected QueryExpression qs = null;

    /** Parent query expression (if this is a subquery). */
    protected QueryExpression parentExpr;

    protected Map<String, ScalarExpression> expressionsByVariableName = new HashMap();

    /** Parser for the query. */
    protected Parser p = null;

    /** ClassMetaData for the candidate. */
    protected AbstractClassMetaData candidateCmd = null;

    /** Candidates for the query. */
    protected Queryable candidates = null;

    /** Result class to be used. May be updated during the compilation. */
    protected Class resultClass = null;

    /** Range from position (inclusive). */
    protected long rangeFromIncl = -1;

    /** Range to position (exclusive). */
    protected long rangeToExcl = -1;

    /** Candidate expression when treating as a subquery. */
    protected String subqueryCandidateExpr;

    /** Alias join info for the subquery candidate expression root (if not using parent query candidate). */
    protected AliasJoinInformation subqueryCandidateExprRootAliasInfo = null;

    /** Register of user-defined ScalarExpression, provided via plugins. */
    protected static transient Map userDefinedScalarExpressions = new Hashtable();

    /**
     * Constructor for a compiler of java queries.
     * @param query The query to compile
     * @param imports The imports to use
     * @param parameters Any parameters
     */
    public QueryCompiler(Query query, Imports imports, Map parameters) {
        if (userDefinedScalarExpressions.isEmpty()) {
            registerScalarExpressions(query.getObjectManager().getOMFContext().getPluginManager(), query.getObjectManager().getClassLoaderResolver());
        }
        this.query = query;
        this.imports = imports;
        this.parameters = parameters;
        this.candidateClass = query.getCandidateClass();
        this.candidates = ((JDOQLQuery) query).getCandidates();
        this.resultClass = query.getResultClass();
        this.rangeFromIncl = query.getRangeFromIncl();
        this.rangeToExcl = query.getRangeToExcl();
    }

    /**
     * Inner class defining important information about an aliased class.
     */
    class AliasJoinInformation {

        String alias;

        Class cls;

        LogicSetExpression tableExpression;

        boolean candidate;

        public AliasJoinInformation(String alias, Class cls, LogicSetExpression tblExpr, boolean candidate) {
            this.alias = alias;
            this.cls = cls;
            this.tableExpression = tblExpr;
            this.candidate = candidate;
        }

        public String toString() {
            return "Alias=" + alias + " class=" + cls + " tableExpr=" + tableExpression + (candidate ? "[CANDIDATE]" : "");
        }
    }

    /**
     * Method to close the Compiler.
     */
    public void close() {
        this.query = null;
        this.imports = null;
        this.variableNames = null;
        this.parameterNames = null;
        this.variableTypesByName = null;
        this.parameterTypesByName = null;
        this.parameters = null;
        this.expressionsByVariableName = null;
        this.qs = null;
        this.fieldExpressions = null;
    }

    /**
     * Method to compile the query.
     * @param type Type of compilation. This compiler only supports explicit parameters, explicit variables
     * @return the compilation artifact (if any)
     */
    public Object compile(int type) {
        switch(type) {
            case COMPILE_SYNTAX:
                compile(COMPILE_EXPLICIT_PARAMETERS);
                compile(COMPILE_EXPLICIT_VARIABLES);
                preCompile();
                return null;
            case COMPILE_EXECUTION:
                compile(COMPILE_EXPLICIT_PARAMETERS);
                compile(COMPILE_EXPLICIT_VARIABLES);
                return executionCompile();
            case COMPILE_EXPLICIT_PARAMETERS:
                compileExplicitParameters();
                return null;
            case COMPILE_EXPLICIT_VARIABLES:
                compileExplicitVariables();
                return null;
            default:
                throw new NucleusException("Query Compiler doesnt support compilation of type " + type);
        }
    }

    /**
     * Method to provide pre-compilation checks to catch errors.
     * This is performed when calling the JDO Query.compile() method.
     */
    protected void preCompile() {
        executionCompile = false;
        compileCandidates();
        MappedStoreManager storeMgr = (MappedStoreManager) query.getObjectManager().getStoreManager();
        DatastoreIdentifier candidateAliasId = storeMgr.getIdentifierFactory().newIdentifier(IdentifierType.TABLE, candidateAlias);
        qs = candidates.newQueryStatement(candidateClass, candidateAliasId);
        if (parentExpr != null) {
            qs.setParent(parentExpr);
        }
        qs.setCandidateInformation(candidateClass, candidateAlias);
        performCompile(qs);
        executionCompile = true;
    }

    /**
     * Method to execution-compile the query.
     * Generates the query and returns it.
     * @return the execution compiled query
     */
    protected QueryExpression executionCompile() {
        compileCandidates();
        if (candidates instanceof ResultExpressionsQueryable) {
            ((ResultExpressionsQueryable) candidates).setHasAggregatedExpressionsOnly(QueryUtils.resultHasOnlyAggregates(query.getResult()));
        }
        MappedStoreManager storeMgr = (MappedStoreManager) query.getObjectManager().getStoreManager();
        DatastoreIdentifier candidateAliasId = storeMgr.getIdentifierFactory().newIdentifier(IdentifierType.TABLE, candidateAlias);
        qs = candidates.newQueryStatement(candidateClass, candidateAliasId);
        if (parentExpr != null) {
            qs.setParent(parentExpr);
        }
        qs.setCandidateInformation(candidateClass, candidateAlias);
        PersistenceConfiguration config = query.getObjectManager().getOMFContext().getPersistenceConfiguration();
        String propName = "datanucleus.rdbms.jdoql.joinType";
        String joinType = config.getStringProperty(propName);
        if (query.getExtension(propName) != null) {
            String type = (String) query.getExtension(propName);
            if (type.toUpperCase().equals("INNER") || type.toUpperCase().equals("LEFT OUTER")) {
                joinType = type.toUpperCase();
            }
        }
        if (joinType != null) {
            qs.addExtension(propName, joinType);
        }
        propName = "datanucleus.rdbms.jdoql.existsIncludesConstraints";
        boolean existsIncludes = config.getBooleanProperty(propName);
        if (query.getExtension(propName) != null) {
            existsIncludes = Boolean.valueOf((String) query.getExtension(propName)).booleanValue();
            qs.addExtension(propName, "" + existsIncludes);
        }
        if (existsIncludes) {
            qs.addExtension(propName, "true");
        }
        propName = "datanucleus.rdbms.query.containsUsesExistsAlways";
        boolean existsAlways = config.getBooleanProperty(propName);
        if (query.getExtension(propName) != null) {
            existsAlways = Boolean.valueOf((String) query.getExtension(propName)).booleanValue();
            qs.addExtension(propName, "" + existsAlways);
        }
        if (existsAlways) {
            qs.addExtension(propName, "true");
        }
        performCompile(qs);
        return qs;
    }

    /**
     * Perform the actual compilation of the query.
     * @param qs The QueryExpression to use during compilation (if required)
     */
    protected abstract void performCompile(QueryExpression qs);

    /**
     * Convenience method to process the candidates for this query.
     * Processes the "candidateClassName" and "candidateClass" and sets up "candidates".
     */
    protected abstract void compileCandidates();

    /**
     * Accessor for the result MetaData. Will be null until the query is compiled.
     * @return ResultMetaData for the query
     */
    public QueryResultsMetaData getResultMetaData() {
        return resultMetaData;
    }

    /**
     * Accessor for the candidates for the query.
     * @return Candidates for the query
     */
    public Queryable getCandidates() {
        return candidates;
    }

    /**
     * Accessor for the result class. May have been updated during the compile process.
     * @return Result class
     */
    public Class getResultClass() {
        return resultClass;
    }

    /**
     * Accessor for the range "from" value. May have been set during compilation where the
     * "from" was an expression.
     * @return Range "from" value
     */
    public long getRangeFromIncl() {
        return rangeFromIncl;
    }

    /**
     * Accessor for the range "to" value. May have been set during compilation where the
     * "to" was an expression.
     * @return Range "to" value
     */
    public long getRangeToExcl() {
        return rangeToExcl;
    }

    /**
     * Accessor for the candidate class. May have been updated during the compile process.
     * @return Candidate class
     */
    public Class getCandidateClass() {
        return candidateClass;
    }

    /**
     * Accessor for the candidate alias.
     * @return Candidate alias
     */
    public String getCandidateAlias() {
        return candidateAlias;
    }

    /**
     * Accessor for the (explicit) parameter names.
     * @return Parameter names
     */
    public String[] getParameterNames() {
        if (parameterNames == null) {
            return null;
        }
        return parameterNames.toArray(new String[parameterNames.size()]);
    }

    /**
     * Accessor for the parameter types keyed by the parameter name.
     * Generated during compile of explicit parameters.
     * @return Map of parameter type keyed by name
     */
    public Map getParameterTypesByName() {
        return parameterTypesByName;
    }

    /**
     * Method to compile all parameters declared for this query.
     * Takes the input "parameters" and populates "parameterNames", "parameterTypesByName" for convenience.
     */
    protected void compileExplicitParameters() {
        parameterNames = new ArrayList();
        parameterTypesByName = new HashMap();
        String explicitParameters = query.getExplicitParameters();
        if (explicitParameters != null && explicitParameters.length() > 0) {
            StringTokenizer t1 = new StringTokenizer(explicitParameters, ",");
            while (t1.hasMoreTokens()) {
                StringTokenizer t2 = new StringTokenizer(t1.nextToken(), " ");
                if (t2.countTokens() != 2) {
                    throw new NucleusUserException(LOCALISER.msg("021101", explicitParameters));
                }
                String classDecl = t2.nextToken();
                String parameterName = t2.nextToken();
                if (!JDOQLQueryHelper.isValidJavaIdentifierForJDOQL(parameterName)) {
                    throw new NucleusUserException(LOCALISER.msg("021102", parameterName));
                }
                if (parameterNames.contains(parameterName)) {
                    throw new NucleusUserException(LOCALISER.msg("021103", parameterName));
                }
                parameterNames.add(parameterName);
                parameterTypesByName.put(parameterName, query.resolveClassDeclaration(classDecl));
            }
        }
    }

    /**
     * Method to compile all variables declared for this query.
     * Takes the input "variables" and populates "variableNames", "variableTypesByName" for convenience.
     */
    protected void compileExplicitVariables() {
        variableNames = new ArrayList();
        variableTypesByName = new HashMap();
        String explicitVariables = query.getExplicitVariables();
        if (explicitVariables != null && explicitVariables.length() > 0) {
            StringTokenizer t1 = new StringTokenizer(explicitVariables, ";");
            while (t1.hasMoreTokens()) {
                StringTokenizer t2 = new StringTokenizer(t1.nextToken(), " ");
                if (t2.countTokens() != 2) {
                    throw new NucleusUserException(LOCALISER.msg("021104", explicitVariables));
                }
                String classDecl = t2.nextToken();
                String variableName = t2.nextToken();
                if (!JDOQLQueryHelper.isValidJavaIdentifierForJDOQL(variableName)) {
                    throw new NucleusUserException(LOCALISER.msg("021105", variableName));
                }
                if (parameterNames.contains(variableName)) {
                    throw new NucleusUserException(LOCALISER.msg("021106", variableName));
                }
                if (variableNames.contains(variableName)) {
                    throw new NucleusUserException(LOCALISER.msg("021107", variableName));
                }
                variableNames.add(variableName);
                variableTypesByName.put(variableName, query.resolveClassDeclaration(classDecl));
            }
        }
    }

    /**
     * <p>
     * Method to process any "<candidate-expression>" when this is a subquery.
     * Converts the candidate expression into a series of INNER JOINs from the subquery candidate table
     * back to the candidate table of the outer query.
     * For example, <pre>this.department.employees</pre> would result in a table expression for the
     * Department table being added to this subquery FROM, with an INNER JOIN to the subquery candidate table,
     * and an AND condition from the Department table back to the outer query candidate table.
     * </p>
     * <p>
     * The subqueryCandidateExpr should either start "this." (hence from the parent query candidate) or,
     * if not, the "subqueryCandidateExprRootAliasInfo" will be set and will define the candidate root
     * of the subquery candidate expression. This second use is used in JPQL where we have a subquery
     * using a field of another alias from the parent query e.g
     * <pre>
     * SELECT c FROM Customer c JOIN c.orders o WHERE EXISTS (SELECT o FROM o.lineItems l where l.quantity > 3)
     * </pre>
     * so "o" is the root alias for the subquery, and the "subqueryCandidateExprRootAliasInfo" will define
     * the table expression in the parent query, and the class it represents.
     * </p>
     */
    protected void compileSubqueryCandidateExpression(boolean caseSensitive) {
        if (subqueryCandidateExpr != null) {
            String[] tokens = StringUtils.split(subqueryCandidateExpr, ".");
            if (caseSensitive) {
                if (!tokens[0].equals(parentExpr.getCandidateAlias())) {
                    throw new NucleusUserException("Subquery has been specified with a candidate-expression that doesnt start with \"" + parentExpr.getCandidateAlias() + "\"." + " All candidate expressions must start with that to relate them back to the outer query");
                }
            } else {
            }
            Class cls = (subqueryCandidateExprRootAliasInfo != null ? subqueryCandidateExprRootAliasInfo.cls : parentExpr.getCandidateClass());
            ClassLoaderResolver clr = query.getObjectManager().getClassLoaderResolver();
            MetaDataManager mmgr = query.getObjectManager().getMetaDataManager();
            MappedStoreManager storeMgr = (MappedStoreManager) query.getStoreManager();
            AbstractClassMetaData leftCmd = mmgr.getMetaDataForClass(cls, clr);
            LogicSetExpression leftTblExpr = (subqueryCandidateExprRootAliasInfo != null ? subqueryCandidateExprRootAliasInfo.tableExpression : parentExpr.getMainTableExpression());
            String leftAlias = (subqueryCandidateExprRootAliasInfo != null ? subqueryCandidateExprRootAliasInfo.alias : parentExpr.getCandidateAlias());
            DatastoreClass leftTable = (DatastoreClass) leftTblExpr.getMainTable();
            for (int i = 1; i < tokens.length; i++) {
                AbstractMemberMetaData leftMmd = leftCmd.getMetaDataForMember(tokens[i]);
                AbstractClassMetaData rightCmd = null;
                int relationType = leftMmd.getRelationType(clr);
                AbstractMemberMetaData rightMmd = null;
                if (relationType == Relation.ONE_TO_ONE_BI || relationType == Relation.ONE_TO_MANY_BI || relationType == Relation.MANY_TO_MANY_BI || relationType == Relation.MANY_TO_ONE_BI) {
                    rightMmd = leftMmd.getRelatedMemberMetaData(clr)[0];
                }
                if (i == tokens.length - 1) {
                    cls = candidateClass;
                } else {
                    if (relationType == Relation.ONE_TO_ONE_BI || relationType == Relation.ONE_TO_ONE_UNI || relationType == Relation.MANY_TO_ONE_BI) {
                        cls = leftMmd.getType();
                        rightCmd = mmgr.getMetaDataForClass(cls, clr);
                    } else if (relationType == Relation.ONE_TO_MANY_BI || relationType == Relation.ONE_TO_MANY_UNI || relationType == Relation.MANY_TO_MANY_BI) {
                        if (leftMmd.hasCollection()) {
                            cls = clr.classForName(leftMmd.getCollection().getElementType());
                            rightCmd = mmgr.getMetaDataForClass(cls, clr);
                        } else if (leftMmd.hasMap()) {
                            cls = clr.classForName(leftMmd.getMap().getValueType());
                            rightCmd = mmgr.getMetaDataForClass(cls, clr);
                        }
                    } else {
                        throw new NucleusUserException("Subquery has been specified with a candidate-expression that" + " includes \"" + tokens[i] + "\" that isnt a relation field!!");
                    }
                }
                LogicSetExpression rightTblExpr;
                String rightAlias;
                DatastoreIdentifier rightTblAlias;
                DatastoreClass rightTable;
                if (i == tokens.length - 1) {
                    rightTblExpr = qs.getMainTableExpression();
                    rightTblAlias = qs.getMainTableAlias();
                    rightTable = (DatastoreClass) rightTblExpr.getMainTable();
                    rightAlias = candidateAlias;
                } else {
                    rightTable = storeMgr.getDatastoreClass(cls.getName(), clr);
                    rightAlias = "T" + i;
                    rightTblAlias = storeMgr.getIdentifierFactory().newIdentifier(IdentifierType.TABLE, rightAlias);
                    rightTblExpr = qs.newTableExpression(rightTable, rightTblAlias);
                }
                if (relationType == Relation.ONE_TO_ONE_UNI || (relationType == Relation.ONE_TO_ONE_BI && leftMmd.getMappedBy() == null) || (relationType == Relation.MANY_TO_ONE_BI && (leftMmd.getJoinMetaData() == null && rightMmd.getJoinMetaData() == null))) {
                    ScalarExpression leftExpr = leftTblExpr.newFieldExpression(tokens[i]);
                    ScalarExpression rightExpr = rightTable.getIdMapping().newScalarExpression(qs, rightTblExpr);
                    if (i == 1) {
                        qs.andCondition(leftExpr.eq(rightExpr), true);
                    } else {
                        qs.innerJoin(rightExpr, leftExpr, leftTblExpr, true, true);
                    }
                } else if (relationType == Relation.ONE_TO_ONE_BI && leftMmd.getMappedBy() != null) {
                    ScalarExpression leftExpr = leftTable.getIdMapping().newScalarExpression(qs, leftTblExpr);
                    ScalarExpression rightExpr = rightTblExpr.newFieldExpression(rightMmd.getName());
                    if (i == 1) {
                        qs.andCondition(leftExpr.eq(rightExpr), true);
                    } else {
                        qs.innerJoin(rightExpr, leftExpr, leftTblExpr, true, true);
                    }
                } else if ((relationType == Relation.ONE_TO_MANY_UNI && leftMmd.getJoinMetaData() == null) || (relationType == Relation.ONE_TO_MANY_BI && (leftMmd.getJoinMetaData() == null && rightMmd.getJoinMetaData() == null))) {
                    ScalarExpression leftExpr = leftTable.getIdMapping().newScalarExpression(qs, leftTblExpr);
                    ScalarExpression rightExpr = rightTblExpr.newFieldExpression(rightMmd.getName());
                    if (i == 1) {
                        qs.andCondition(leftExpr.eq(rightExpr), true);
                    } else {
                        qs.innerJoin(rightExpr, leftExpr, leftTblExpr, true, true);
                    }
                } else if (relationType == Relation.ONE_TO_MANY_UNI && leftMmd.getJoinMetaData() != null) {
                    ScalarExpression leftExpr = leftTable.getIdMapping().newScalarExpression(qs, leftTblExpr);
                    ScalarExpression rightExpr = rightTable.getIdMapping().newScalarExpression(qs, rightTblExpr);
                    ScalarExpression leftCentreExpr = null;
                    ScalarExpression rightCentreExpr = null;
                    LogicSetExpression joinTblExpr = null;
                    if (leftMmd.hasCollection()) {
                        CollectionTable joinTbl = (CollectionTable) storeMgr.getDatastoreContainerObject(leftMmd);
                        DatastoreIdentifier joinTblAlias = storeMgr.getIdentifierFactory().newIdentifier(IdentifierType.TABLE, leftAlias + "." + rightAlias);
                        joinTblExpr = qs.newTableExpression(joinTbl, joinTblAlias);
                        leftCentreExpr = joinTbl.getOwnerMapping().newScalarExpression(qs, joinTblExpr);
                        rightCentreExpr = joinTbl.getElementMapping().newScalarExpression(qs, joinTblExpr);
                    } else if (leftMmd.hasMap()) {
                        MapTable joinTbl = (MapTable) storeMgr.getDatastoreContainerObject(leftMmd);
                        DatastoreIdentifier joinTblAlias = storeMgr.getIdentifierFactory().newIdentifier(IdentifierType.TABLE, leftAlias + "." + rightAlias);
                        joinTblExpr = qs.newTableExpression(joinTbl, joinTblAlias);
                        leftCentreExpr = joinTbl.getOwnerMapping().newScalarExpression(qs, joinTblExpr);
                        rightCentreExpr = joinTbl.getValueMapping().newScalarExpression(qs, joinTblExpr);
                    }
                    if (i == 1) {
                        qs.andCondition(leftExpr.eq(leftCentreExpr), true);
                    } else {
                        qs.innerJoin(leftCentreExpr, leftExpr, leftTblExpr, true, true);
                    }
                    qs.innerJoin(rightExpr, rightCentreExpr, joinTblExpr, true, true);
                } else if ((relationType == Relation.ONE_TO_MANY_BI && (leftMmd.getJoinMetaData() != null || rightMmd.getJoinMetaData() != null)) || (relationType == Relation.MANY_TO_ONE_BI && (leftMmd.getJoinMetaData() != null || rightMmd.getJoinMetaData() != null)) || relationType == Relation.MANY_TO_MANY_BI) {
                    ScalarExpression leftExpr = leftTable.getIdMapping().newScalarExpression(qs, leftTblExpr);
                    ScalarExpression rightExpr = rightTable.getIdMapping().newScalarExpression(qs, rightTblExpr);
                    ScalarExpression leftCentreExpr = null;
                    ScalarExpression rightCentreExpr = null;
                    LogicSetExpression joinTblExpr = null;
                    if (leftMmd.hasCollection() || rightMmd.hasCollection()) {
                        CollectionTable joinTbl = (CollectionTable) storeMgr.getDatastoreContainerObject(leftMmd);
                        DatastoreIdentifier joinTblAlias = storeMgr.getIdentifierFactory().newIdentifier(IdentifierType.TABLE, leftAlias + "." + rightAlias);
                        joinTblExpr = qs.newTableExpression(joinTbl, joinTblAlias);
                        leftCentreExpr = joinTbl.getOwnerMapping().newScalarExpression(qs, joinTblExpr);
                        rightCentreExpr = joinTbl.getElementMapping().newScalarExpression(qs, joinTblExpr);
                    } else if (leftMmd.hasMap() || rightMmd.hasMap()) {
                        MapTable joinTbl = (MapTable) storeMgr.getDatastoreContainerObject(leftMmd);
                        DatastoreIdentifier joinTblAlias = storeMgr.getIdentifierFactory().newIdentifier(IdentifierType.TABLE, leftAlias + "." + rightAlias);
                        joinTblExpr = qs.newTableExpression(joinTbl, joinTblAlias);
                        leftCentreExpr = joinTbl.getOwnerMapping().newScalarExpression(qs, joinTblExpr);
                        rightCentreExpr = joinTbl.getValueMapping().newScalarExpression(qs, joinTblExpr);
                    }
                    if (i == 1) {
                        qs.andCondition(leftExpr.eq(leftCentreExpr), true);
                    } else {
                        qs.innerJoin(leftCentreExpr, leftExpr, leftTblExpr, true, true);
                    }
                    qs.innerJoin(rightExpr, rightCentreExpr, joinTblExpr, true, true);
                } else {
                    throw new NucleusUserException("<candidate-expression>=" + subqueryCandidateExpr + " has token " + tokens[i] + " and relationType=" + relationType + " NOT SUPPORTED");
                }
                if (i < tokens.length - 1) {
                    leftTblExpr = rightTblExpr;
                    leftCmd = rightCmd;
                    leftTable = rightTable;
                    leftAlias = rightAlias;
                }
            }
        } else if (query.getFrom() != null) {
            NucleusLogger.QUERY.info(">> performCompile : TODO Add joins for from=" + query.getFrom() + " for real candidate of " + candidateClass.getName());
        }
    }

    /**
     * Convenience method to compile the filter.
     * Processes the filter and updates the QueryExpression accordingly.
     * @param qs The Query Expression to apply the filter to (if specified)
     * @param filter The filter specification
     */
    protected void compileFilter(QueryExpression qs, String filter) {
        if (filter != null && filter.length() > 0) {
            ScalarExpression expr = compileExpressionFromString(filter);
            if (!(expr instanceof BooleanExpression)) {
                throw new NucleusUserException(LOCALISER.msg("021050", filter));
            }
            if (qs != null) {
                qs.andCondition((BooleanExpression) expr, true);
            }
        }
    }

    /**
     * Compile the result expressions and class.
     * @param qs Query Expression to apply the result to (if required)
     * @param result Result clause to compile
     */
    protected void compileResult(QueryExpression qs, String result) {
        final ScalarExpression[] resultExprs;
        if (result != null) {
            String resultDefinition = result;
            resultExprs = compileExpressionsFromString(resultDefinition);
            ((ResultExpressionsQueryable) candidates).setResultExpressions(resultExprs);
            Class[] resultTypes = new Class[resultExprs.length];
            for (int i = 0; i < resultExprs.length; i++) {
                if (resultExprs[i] instanceof CollectionExpression) {
                    throw new NucleusUserException(resultExprs[i].toStatementText(ScalarExpression.PROJECTION) + " is of type java.util.Collection and cannot be in the result.");
                } else if (resultExprs[i] instanceof MapExpression) {
                    throw new NucleusUserException(resultExprs[i].toStatementText(ScalarExpression.PROJECTION) + " is of type java.util.Map and cannot be in the result.");
                }
                if (resultExprs[i].getMapping() != null) {
                    if (resultExprs[i].getMapping().getType() != null) {
                        resultTypes[i] = query.resolveClassDeclaration(resultExprs[i].getMapping().getType());
                    } else {
                        resultTypes[i] = resultExprs[i].getMapping().getJavaType();
                    }
                }
            }
            resultMetaData = new QueryResultsMetaData(resultTypes);
        } else {
            resultMetaData = new QueryResultsMetaData(new Class[] { candidateClass });
            resultExprs = null;
        }
        String resultClassName = query.getResultClassName();
        if (resultClass == null && resultClassName != null) {
            ScalarExpression expr = compileExpressionFromString(resultClassName);
            resultClass = ((ClassExpression) expr).getCls();
        }
        if (resultClass != null && resultExprs != null) {
            AccessController.doPrivileged(new PrivilegedAction() {

                public Object run() {
                    if (QueryUtils.resultClassIsSimple(resultClass.getName())) {
                        if (resultExprs.length > 1) {
                            throw new NucleusUserException(LOCALISER.msg("021201", resultClass.getName()));
                        }
                        Class exprType = resultExprs[0].getMapping().getJavaType();
                        boolean typeConsistent = false;
                        if (exprType == resultClass) {
                            typeConsistent = true;
                        } else if (exprType.isPrimitive()) {
                            Class resultClassPrimitive = ClassUtils.getPrimitiveTypeForType(resultClass);
                            if (resultClassPrimitive == exprType) {
                                typeConsistent = true;
                            }
                        }
                        if (!typeConsistent) {
                            throw new NucleusUserException(LOCALISER.msg("021202", resultClass.getName(), exprType));
                        }
                    } else if (QueryUtils.resultClassIsUserType(resultClass.getName())) {
                        Class[] ctrTypes = new Class[resultExprs.length];
                        for (int i = 0; i < ctrTypes.length; i++) {
                            ctrTypes[i] = resultExprs[i].getMapping().getJavaType();
                        }
                        Constructor ctr = ClassUtils.getConstructorWithArguments(resultClass, ctrTypes);
                        if (ctr == null && !ClassUtils.hasDefaultConstructor(resultClass)) {
                            throw new NucleusUserException(LOCALISER.msg("021205", resultClass.getName()));
                        } else if (ctr == null) {
                            for (int i = 0; i < resultExprs.length; i++) {
                                String fieldName = resultExprs[i].getAlias();
                                Class fieldType = resultExprs[i].getMapping().getJavaType();
                                if (fieldName == null && resultExprs[i].getMapping().getMemberMetaData() != null) {
                                    fieldName = resultExprs[i].getMapping().getMemberMetaData().getName();
                                }
                                if (fieldName != null) {
                                    Class resultFieldType = null;
                                    boolean publicField = true;
                                    try {
                                        Field fld = resultClass.getDeclaredField(fieldName);
                                        resultFieldType = fld.getType();
                                        if (!ClassUtils.typesAreCompatible(fieldType, resultFieldType) && !ClassUtils.typesAreCompatible(resultFieldType, fieldType)) {
                                            throw new NucleusUserException(LOCALISER.msg("021211", fieldName, fieldType.getName(), resultFieldType.getName()));
                                        }
                                        if (!Modifier.isPublic(fld.getModifiers())) {
                                            publicField = false;
                                        }
                                    } catch (NoSuchFieldException nsfe) {
                                        publicField = false;
                                    }
                                    if (!publicField) {
                                        Method setMethod = QueryUtils.getPublicSetMethodForFieldOfResultClass(resultClass, fieldName, resultFieldType);
                                        if (setMethod == null) {
                                            Method putMethod = QueryUtils.getPublicPutMethodForResultClass(resultClass);
                                            if (putMethod == null) {
                                                throw new NucleusUserException(LOCALISER.msg("021212", resultClass.getName(), fieldName));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return null;
                }
            });
        }
    }

    /**
     * Convenience method to compile the grouping.
     * Compiles the grouping definition and applies it to the passed Query Expression as appropriate.
     * @param qs The QueryExpression to update (if specified)
     * @param groupingClause The grouping clause string
     */
    protected void compileGrouping(QueryExpression qs, String groupingClause) {
        if (groupingClause != null && groupingClause.length() > 0) {
            ScalarExpression[] groupExprs = compileExpressionsFromString(groupingClause);
            if (groupExprs != null && qs != null) {
                for (int i = 0; i < groupExprs.length; i++) {
                    qs.addGroupingExpression(groupExprs[i]);
                }
            }
        }
    }

    /**
     * Convenience method to compile the having clause
     * @param qs The QueryExpression to update (if specified)
     * @param havingClause The having clause string
     */
    protected void compileHaving(QueryExpression qs, String havingClause) {
        if (havingClause != null && havingClause.length() > 0) {
            ScalarExpression havingExpr = compileExpressionFromString(havingClause);
            if (qs != null) {
                if (!(havingExpr instanceof BooleanExpression)) {
                    throw new NucleusUserException(LOCALISER.msg("021051", havingExpr));
                }
                qs.setHaving((BooleanExpression) havingExpr);
            }
        }
    }

    /**
     * Convenience method to compile the range.
     * Compiles any range string and extracts the fromInclNo, toExclNo as appropriate.
     * @param qs QueryExpression to apply the range to (if specified)
     */
    protected void compileRange(QueryExpression qs) {
        String range = query.getRange();
        if (range != null) {
            ScalarExpression[] exprs = compileExpressionsFromString(range);
            if (exprs.length > 0) {
                if (!(exprs[0] instanceof Literal)) {
                    throw new NucleusUserException(LOCALISER.msg("021064", "FROM", exprs[0]));
                }
                if (!(((Literal) exprs[0]).getValue() instanceof Number)) {
                    throw new NucleusUserException(LOCALISER.msg("021065", "FROM", exprs[0]));
                }
                rangeFromIncl = ((Number) ((Literal) exprs[0]).getValue()).longValue();
            }
            if (exprs.length > 1) {
                if (!(exprs[1] instanceof Literal)) {
                    throw new NucleusUserException(LOCALISER.msg("021064", "TO", exprs[1]));
                }
                if (!(((Literal) exprs[1]).getValue() instanceof Number)) {
                    throw new NucleusUserException(LOCALISER.msg("021065", "TO", exprs[1]));
                }
                rangeToExcl = ((Number) ((Literal) exprs[1]).getValue()).longValue();
            }
        }
        if (qs != null && (rangeFromIncl > 0 || rangeToExcl != Long.MAX_VALUE)) {
            qs.setRangeConstraint(rangeFromIncl, rangeToExcl != Long.MAX_VALUE ? rangeToExcl - rangeFromIncl : -1);
        }
    }

    /**
     * Convenience method to check the expressions against those specified in the grouping.
     * Throws a JPOXUserException if one of the expressions is not present in the grouping expressions.
     * @param exprs The expressions to check
     * @param groupExprs The grouping expressions
     * @param localiserErrorString Name of a localiser error message to throw as the JPOXUserException message.
     */
    protected void checkExpressionsAgainstGrouping(ScalarExpression[] exprs, ScalarExpression[] groupExprs, String localiserErrorString) {
        if (exprs == null) {
            return;
        }
        for (int i = 0; i < exprs.length; i++) {
            boolean exists = false;
            for (int j = 0; j < groupExprs.length; j++) {
                if (exprs[i].equals(groupExprs[j])) {
                    exists = true;
                    break;
                }
            }
            if (!(exprs[i] instanceof AggregateExpression) && !exists) {
                throw new NucleusUserException(LOCALISER.msg(localiserErrorString, exprs[i]));
            }
        }
    }

    /**
     * Bind a variable to the query.
     * @param name Name of the variable
     * @param expr The expression
     */
    public void bindVariable(String name, ScalarExpression expr) {
        ScalarExpression previousExpr = expressionsByVariableName.put(name, expr);
        if (previousExpr != null) {
            throw new NucleusException(LOCALISER.msg("021060", name, expr, previousExpr)).setFatal();
        }
    }

    /**
     * Convenience method to check that all variables have been bound to the query.
     * @throws NucleusUserException Thrown if a variable is found that is not bound.
     */
    protected void checkVariableBinding() {
        for (int i = 0; i < variableNames.size(); i++) {
            String variableName = variableNames.get(i);
            if (expressionsByVariableName.get(variableName) == null) {
                boolean foundInResult = false;
                if (candidates instanceof ResultExpressionsQueryable) {
                    ScalarExpression[] exprs = ((ResultExpressionsQueryable) candidates).getResultExpressions();
                    for (int j = 0; j < exprs.length; j++) {
                        if (exprs[j] instanceof UnboundVariable) {
                            if (((UnboundVariable) exprs[j]).getVariableName().equals(variableName)) {
                                foundInResult = true;
                            }
                        }
                    }
                }
                if (!foundInResult) {
                    throw new NucleusUserException(LOCALISER.msg("021061", variableName));
                }
            }
        }
    }

    /**
     * Convenience method to parse an expression string into its component query expressions.
     * This splits expressions at comma boundaries, whilst respecting that the string could include
     * expressions like "new MyClass(a, b, c)" and so keeping braced arguments together. If it wasn't
     * for this requirement we would have been able to just use a StringTokenizer.
     * @param str The string
     * @return The query expressions for the passed string
     */
    protected ScalarExpression[] compileExpressionsFromString(String str) {
        String[] exprList = QueryUtils.getExpressionsFromString(str);
        if (exprList != null && exprList.length > 0) {
            ScalarExpression[] exprs = new ScalarExpression[exprList.length];
            for (int i = 0; i < exprs.length; i++) {
                exprs[i] = compileExpressionFromString(exprList[i]);
            }
            return exprs;
        }
        return null;
    }

    /**
     * Convenience method to parse an expression string into its query expression.
     * @param str The string
     * @return The query expression for the passed string
     */
    protected abstract ScalarExpression compileExpressionFromString(String str);

    /**
     * Principal method for compiling an expression.
     * An expression could be the filter, the range, the result, etc.
     * @return The compiled expression
     */
    protected abstract ScalarExpression compileExpression();

    protected ScalarExpression compileAdditiveExpression() {
        ScalarExpression expr = compileMultiplicativeExpression();
        for (; ; ) {
            if (p.parseChar('+')) {
                expr = expr.add(compileMultiplicativeExpression());
            } else if (p.parseChar('-')) {
                expr = expr.sub(compileMultiplicativeExpression());
            } else {
                break;
            }
        }
        return expr;
    }

    protected ScalarExpression compileMultiplicativeExpression() {
        ScalarExpression expr = compileUnaryExpression();
        for (; ; ) {
            if (p.parseChar('*')) {
                expr = expr.mul(compileUnaryExpression());
            } else if (p.parseChar('/')) {
                expr = expr.div(compileUnaryExpression());
            } else if (p.parseChar('%')) {
                expr = expr.mod(compileUnaryExpression());
            } else {
                break;
            }
        }
        return expr;
    }

    protected ScalarExpression compileUnaryExpression() {
        ScalarExpression expr;
        if (p.parseString("++")) {
            throw new NucleusUserException("Unsupported operator '++'");
        } else if (p.parseString("--")) {
            throw new NucleusUserException("Unsupported operator '--'");
        }
        if (p.parseChar('+')) {
            expr = compileUnaryExpression();
        } else if (p.parseChar('-')) {
            expr = compileUnaryExpression().neg();
        } else {
            expr = compileUnaryExpressionNotPlusMinus();
        }
        return expr;
    }

    protected ScalarExpression compileUnaryExpressionNotPlusMinus() {
        ScalarExpression expr;
        if (p.parseChar('~')) {
            expr = compileUnaryExpression().com();
        } else if (p.parseChar('!')) {
            expr = compileUnaryExpression().not();
        } else if ((expr = compileCastExpression()) == null) {
            expr = compilePrimary();
        }
        return expr;
    }

    protected ScalarExpression compileCastExpression() {
        Class type;
        if ((type = p.parseCast((qs == null ? query.getObjectManager().getClassLoaderResolver() : qs.getClassLoaderResolver()), (candidateClass == null ? null : candidateClass.getClassLoader()))) == null) {
            return null;
        }
        return compileUnaryExpression().cast(type);
    }

    /**
     * Compiles a primary. First look for a literal (e.g. "text"), then an identifier(e.g. variable). 
     * In the next step, call a function, if executing a function, on the literal or the identifier found.
     * 
     * @return Scalar Expression for the primary compiled expression
     */
    protected abstract ScalarExpression compilePrimary();

    /**
     * A literal is one value of any type.
     * Supported literals are of types String, Floating Point, Integer,
     * Character, Boolean and null e.g. 'J', "String", 1, 1.8, true, false, null.
     * @return The compiled literal
     */
    protected ScalarExpression compileLiteral() {
        Class litType;
        Object litValue;
        String sLiteral;
        BigDecimal fLiteral;
        BigInteger iLiteral;
        Boolean bLiteral;
        boolean single_quote_next = p.nextIsSingleQuote();
        if ((sLiteral = p.parseStringLiteral()) != null) {
            if (sLiteral.length() == 1 && single_quote_next) {
                litType = Character.class;
                litValue = new Character(sLiteral.charAt(0));
            } else {
                litType = String.class;
                litValue = sLiteral;
            }
        } else if ((fLiteral = p.parseFloatingPointLiteral()) != null) {
            litType = BigDecimal.class;
            litValue = fLiteral;
        } else if ((iLiteral = p.parseIntegerLiteral()) != null) {
            litType = Long.class;
            litValue = Long.valueOf(iLiteral.longValue());
        } else if ((bLiteral = p.parseBooleanLiteral()) != null) {
            litType = Boolean.class;
            litValue = bLiteral;
        } else if (p.parseNullLiteral()) {
            return new NullLiteral(qs);
        } else {
            return null;
        }
        JavaTypeMapping m = qs.getStoreManager().getMappingManager().getMappingWithDatastoreMapping(litType, false, false, qs.getClassLoaderResolver());
        return m.newLiteral(qs, litValue);
    }

    /**
     * Method to generate an expression for a new object.
     * Parser has just parsed "new" and what follows is of the form
     * <pre>
     * new MyObject(param1, param2)
     * </pre>
     * @return Expression for the new object
     */
    protected ScalarExpression compileNewObject() {
        String newClsName = p.parseName();
        Class newCls = null;
        try {
            newCls = query.resolveClassDeclaration(newClsName);
        } catch (NucleusUserException jpue) {
            throw new NucleusUserException(LOCALISER.msg("021057", language, newClsName));
        }
        ArrayList args = new ArrayList();
        if (p.parseChar('(')) {
            if (!p.parseChar(')')) {
                do {
                    ScalarExpression argExpr = compileExpression();
                    args.add(argExpr);
                    fieldExpressions.add(argExpr);
                } while (p.parseChar(','));
                if (!p.parseChar(')')) {
                    throw new QueryCompilerSyntaxException("')' expected", p.getIndex(), p.getInput());
                }
            }
        } else {
            throw new NucleusUserException(LOCALISER.msg("021058", language, ((AbstractJavaQuery) query).getSingleStringQuery()));
        }
        return new NewObjectExpression(qs, newCls, args);
    }

    /**
     * Method to compile an explicit variable.
     * Identifier passed in is a known explicit variable name.
     * @param id Identifier of the variable
     * @return Variable expression
     */
    protected ScalarExpression compileExplicitVariable(String id) {
        ScalarExpression expr = expressionsByVariableName.get(id);
        if (expr == null) {
            expr = new UnboundVariable(qs, id, variableTypesByName.get(id), this);
        }
        fieldExpressions.add(expr);
        return expr;
    }

    /**
     * Instanciate a ScalarExpression and invoke a method
     * @param method the method name prefixed by the class name (fully qualified or not) 
     * @return the ScalarExpression instance
     */
    protected ScalarExpression callUserDefinedScalarExpression(String method) {
        String className = method.substring(0, method.lastIndexOf('.'));
        String methodName = method.substring(method.lastIndexOf('.') + 1);
        if (!userDefinedScalarExpressions.containsKey(className)) {
            Class cls = query.resolveClassDeclaration(className);
            className = cls.getName();
        }
        if (userDefinedScalarExpressions.containsKey(className)) {
            ScalarExpression expr = newScalarExpression((Class) userDefinedScalarExpressions.get(className));
            if (p.parseChar('(')) {
                ArrayList args = new ArrayList();
                if (!p.parseChar(')')) {
                    do {
                        args.add(compileExpression());
                    } while (p.parseChar(','));
                    if (!p.parseChar(')')) {
                        throw new QueryCompilerSyntaxException("')' expected", p.getIndex(), p.getInput());
                    }
                }
                return expr.callMethod(methodName, args);
            }
        }
        return null;
    }

    /**
     * Create a ScalarExpression instance for the given <code>cls</code>
     * @param cls the class that extends ScalarExpression 
     * @return the ScalarExpression instance
     */
    private ScalarExpression newScalarExpression(Class cls) {
        try {
            return (ScalarExpression) cls.getConstructor(new Class[] { QueryExpression.class }).newInstance(new Object[] { qs });
        } catch (IllegalArgumentException e) {
            throw new NucleusException("Cannot create ScalarExpression for class " + cls.getName() + " due to " + e.getMessage(), e).setFatal();
        } catch (SecurityException e) {
            throw new NucleusException("Cannot create ScalarExpression for class " + cls.getName() + " due to " + e.getMessage(), e).setFatal();
        } catch (InstantiationException e) {
            throw new NucleusException("Cannot create ScalarExpression for class " + cls.getName() + " due to " + e.getMessage(), e).setFatal();
        } catch (IllegalAccessException e) {
            throw new NucleusException("Cannot create ScalarExpression for class " + cls.getName() + " due to " + e.getMessage(), e).setFatal();
        } catch (InvocationTargetException e) {
            throw new NucleusException("Cannot create ScalarExpression for class " + cls.getName() + " due to " + e.getMessage(), e).setFatal();
        } catch (NoSuchMethodException e) {
            throw new NucleusException("Cannot create ScalarExpression for class " + cls.getName() + " due to " + e.getMessage(), e).setFatal();
        }
    }

    /**
     * Register ScalarExpression classes delcared as plug-ins extensions
     * @param pluginMgr The PluginManager
     * @param clr The ClassLoaderResolver to load the literal and ScalarExpression classes
     */
    protected void registerScalarExpressions(PluginManager pluginMgr, ClassLoaderResolver clr) {
        Extension[] ex = pluginMgr.getExtensionPoint("org.datanucleus.store.rdbms.rdbms_scalarexpression").getExtensions();
        for (int i = 0; i < ex.length; i++) {
            ConfigurationElement[] confElm = ex[i].getConfigurationElements();
            for (int c = 0; c < confElm.length; c++) {
                Class literalClass = null;
                if (confElm[c].getAttribute("literal-class") != null) {
                    literalClass = pluginMgr.loadClass(confElm[c].getExtension().getPlugin().getSymbolicName(), confElm[c].getAttribute("literal-class"));
                }
                Class scalarExpression = null;
                if (confElm[c].getAttribute("scalar-expression-class") != null) {
                    scalarExpression = pluginMgr.loadClass(confElm[c].getExtension().getPlugin().getSymbolicName(), confElm[c].getAttribute("scalar-expression-class"));
                }
                registerScalarExpression(literalClass, scalarExpression, confElm[c].getAttribute("name"));
            }
        }
    }

    /**
     * Register ScalarExpressions for the given <code>cls</code>. It allows
     * to perform operations in the query on <i>cls.method([arglist])</i>.
     * @param literal the class providing the operations; e.g. java.lang.Math.class
     * @param scalarExpressionClass the class with the corresponding ScalarExpression. eg. org.datanucleus.store.expression.MathExpression.class
     * @param name alternative name of the given literal class
     */
    public static void registerScalarExpression(Class literal, Class scalarExpressionClass, String name) {
        userDefinedScalarExpressions.put(name == null ? literal.getName() : name, scalarExpressionClass);
    }

    /**
     * Accessor for the user-defined scalar expressions.
     * @return Map of user-defined scalar expressions
     */
    public static Map getUserDefinedScalarExpressions() {
        return userDefinedScalarExpressions;
    }
}
