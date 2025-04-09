package ru.maxmatveev.dyndao.impl.hibernate;

import javassist.*;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import ru.maxmatveev.dyndao.*;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Factory for getting DAO instances.
 *
 * @author Max Matveev
 *         Date: Dec 20, 2008
 *         Time: 10:35:14 PM
 */
public class DaoFactory {

    /**
     * Logger.
     */
    private static final Logger log = Logger.getLogger(DaoFactory.class);

    /**
     * Default constructor
     */
    private static final String DEFAULT_CONSTRUCTOR = "public %s() {throw new IllegalArgumentException(\"This class cannot be instantiated directly, please use factory method.\");}";

    /**
     * Constructor with parameter
     */
    private static final String CONSTRUCTOR = "public %s(Class entityClass, SessionFactory sessionFactory) {this.entityClass = entityClass;this.sessionFactory = sessionFactory;id = \"__\" + java.lang.Math.abs(System.nanoTime() / Integer.MAX_VALUE + 31 * this.entityClass.hashCode());}";

    /**
     * delete implementation
     */
    private static final String DELETE = "public Object delete(Object entity) {sessionFactory.getCurrentSession().delete(entity);return entity;}";

    /**
     * merge implementation
     */
    private static final String MERGE = "public Object merge(Object entity) {sessionFactory.getCurrentSession().merge(entity);return entity;}";

    /**
     * getById implementation
     */
    private static final String GET_BY_ID = "public Object getById(Serializable id) {return sessionFactory.getCurrentSession().get(entityClass, id);}";

    /**
     * getAll implementation
     */
    private static final String GET_ALL = "public List getAll() {return sessionFactory.getCurrentSession().createCriteria(entityClass).list();}";

    /**
     * save implementation
     */
    private static final String SAVE = "public Object save(Object entity) {sessionFactory.getCurrentSession().save(entity);return entity;}";

    private static final String CREATE_ALIAS = "private String createAlias(String field, Criteria criteria) { if (!field.contains(\".\")) { return field; } else { String[] tokens = field.split(\"\\\\.\"); String alias = \"alias_\"; String parent = \"\"; for (int i = 0; i < tokens.length - 1; i++) { if (i != 0) { parent += \".\"; alias += \"_\"; } parent += tokens[i]; alias += tokens[i]; } Set currentAliases = (Set) aliases.get(); if (!currentAliases.contains(alias)) {criteria.createAlias(parent, alias); currentAliases.add(alias);} return alias + \".\" + tokens[tokens.length - 1]; } }";

    private static final String CREATE_ALIAS_DT = "private String createAlias(String field, DetachedCriteria criteria) { if (!field.contains(\".\")) { return field; } else { String[] tokens = field.split(\"\\\\.\"); String alias = \"alias_\"; String parent = \"\"; for (int i = 0; i < tokens.length - 1; i++) { if (i != 0) { parent += \".\"; alias += \"_\"; } parent += tokens[i]; alias += tokens[i]; } Set currentAliases = (Set) aliases.get(); if (!currentAliases.contains(alias)) {criteria.createAlias(parent, alias); currentAliases.add(alias);} return alias + \".\" + tokens[tokens.length - 1]; } }";

    /**
     * getCriterion implementation
     */
    private static final String GET_CRITERION = "public Criterion getCriterion(Restriction restriction, Criteria criteria) { if (restriction == null) { return null; } if (restriction.getLogicalOp() != null) { if (restriction.getLogicalOp() == Restriction.LogicalOp.NOT) { return Restrictions.not(getCriterion(restriction.getChild1(), criteria)); } else if (restriction.getLogicalOp() == Restriction.LogicalOp.AND) { return Restrictions.and(getCriterion(restriction.getChild1(), criteria), getCriterion(restriction.getChild2(), criteria)); } else if (restriction.getLogicalOp() == Restriction.LogicalOp.OR) { return Restrictions.or(getCriterion(restriction.getChild1(), criteria), getCriterion(restriction.getChild2(), criteria)); } } else { String alias = createAlias(restriction.getField(), criteria); if (restriction.getComparison() == Restriction.Comparison.EQUALS) { return restriction.getValue() == null ? Restrictions.isNull(alias) : Restrictions.eq(alias, restriction.getValue()); } " + "else if (restriction.getComparison() == Restriction.Comparison.LESS) { return Restrictions.lt(alias, restriction.getValue()); }" + "else if (restriction.getComparison() == Restriction.Comparison.GREATER) { return Restrictions.gt(alias, restriction.getValue()); }" + "else if (restriction.getComparison() == Restriction.Comparison.LESS_OR_EQUALS) { return Restrictions.le(alias, restriction.getValue()); }" + "else if (restriction.getComparison() == Restriction.Comparison.GREATER_OR_EQUALS) { return Restrictions.ge(alias, restriction.getValue()); }" + "else if (restriction.getComparison() == Restriction.Comparison.IEQUALS) { return Restrictions.ilike(alias, (String) restriction.getValue(), MatchMode.EXACT); } else if (restriction.getComparison() == Restriction.Comparison.LIKE) { return Restrictions.like(alias, (String) restriction.getValue(), MatchMode.ANYWHERE); } else if (restriction.getComparison() == Restriction.Comparison.ILIKE) { return Restrictions.ilike(alias, (String) restriction.getValue(), MatchMode.ANYWHERE); } } return null;}";

    /**
     * getCriterion implementation
     */
    private static final String GET_CRITERION_DT = "public Criterion getCriterion(Restriction restriction, DetachedCriteria criteria) { if (restriction == null) { return null; } if (restriction.getLogicalOp() != null) { if (restriction.getLogicalOp() == Restriction.LogicalOp.NOT) { return Restrictions.not(getCriterion(restriction.getChild1(), criteria)); } else if (restriction.getLogicalOp() == Restriction.LogicalOp.AND) { return Restrictions.and(getCriterion(restriction.getChild1(), criteria), getCriterion(restriction.getChild2(), criteria)); } else if (restriction.getLogicalOp() == Restriction.LogicalOp.OR) { return Restrictions.or(getCriterion(restriction.getChild1(), criteria), getCriterion(restriction.getChild2(), criteria)); } } else { String alias = createAlias(restriction.getField(), criteria); if (restriction.getComparison() == Restriction.Comparison.EQUALS) { return restriction.getValue() == null ? Restrictions.isNull(alias) : Restrictions.eq(alias, restriction.getValue()); } " + "else if (restriction.getComparison() == Restriction.Comparison.LESS) { return Restrictions.lt(alias, restriction.getValue()); }" + "else if (restriction.getComparison() == Restriction.Comparison.GREATER) { return Restrictions.gt(alias, restriction.getValue()); }" + "else if (restriction.getComparison() == Restriction.Comparison.LESS_OR_EQUALS) { return Restrictions.le(alias, restriction.getValue()); }" + "else if (restriction.getComparison() == Restriction.Comparison.GREATER_OR_EQUALS) { return Restrictions.ge(alias, restriction.getValue()); }" + "else if (restriction.getComparison() == Restriction.Comparison.IEQUALS) { return Restrictions.ilike(alias, (String) restriction.getValue(), MatchMode.EXACT); } else if (restriction.getComparison() == Restriction.Comparison.LIKE) { return Restrictions.like(alias, (String) restriction.getValue(), MatchMode.ANYWHERE); } else if (restriction.getComparison() == Restriction.Comparison.ILIKE) { return Restrictions.ilike(alias, (String) restriction.getValue(), MatchMode.ANYWHERE); } } return null;}";

    /**
     * getSessionFactory implementation
     */
    private static final String GET_SESSION_FACTORY = "public SessionFactory getSessionFactory() {return sessionFactory;}";

    /**
     * get session
     */
    private static final String GET_SESSION = "{ Session session = sessionFactory.getCurrentSession();";

    /**
     * Field DEFINE_BY_PARENT_CRITERIAS
     */
    private static final String DEFINE_BY_PARENT_CRITERIAS = "DetachedCriteria detachedCriteria = null;Criteria criteria;";

    /**
     * Field RESTRICTION_WITH_PARENT
     */
    private static final String RESTRICTION_WITH_PARENT = "Restriction restrictionValue = $%d;if (restrictionValue != null) {Criterion criterion = getCriterion(restrictionValue, detachedCriteria);if (criterion != null) {detachedCriteria.add(criterion);}}";

    /**
     * Field PARENT_RESTRICTION_1
     */
    private static final String PARENT_RESTRICTION_1 = "detachedCriteria = DetachedCriteria.forClass(%s.class);if (idName == null) {ClassMetadata parentMetadata = session.getSessionFactory().getClassMetadata(%s.class);idName = parentMetadata.getIdentifierPropertyName();}";

    /**
     * Field PARENT_RESTRICTION_2
     */
    private static final String PARENT_RESTRICTION_2 = "Criterion criterion = getCriterion($%d, detachedCriteria);if (criterion != null) {detachedCriteria.add(criterion);}";

    /**
     * Field PARENT_RESTRICTION_3
     */
    private static final String PARENT_RESTRICTION_3 = "detachedCriteria = detachedCriteria.createCriteria(\"%s\", id);";

    /**
     * Field DT_RESTRICTION_EQ
     */
    private static final String DT_RESTRICTION_EQ = "detachedCriteria.add(Restrictions.eq(\"%s\", $%d);";

    /**
     * Field DT_RESTRICTION_IEQ
     */
    private static final String DT_RESTRICTION_IEQ = "detachedCriteria.add(Restrictions.ilike(\"%s\", (String) $%d, MatchMode.EXACT));";

    /**
     * Field DT_RESTRICTION_LIKE
     */
    private static final String DT_RESTRICTION_LIKE = "detachedCriteria.add(Restrictions.like(\"%s\", (String) $%d, MatchMode.ANYWHERE));";

    /**
     * Field DT_RESTRICTION_ILIKE
     */
    private static final String DT_RESTRICTION_ILIKE = "detachedCriteria.add(Restrictions.ilike(\"%s\", (String) $%d, MatchMode.ANYWHERE));";

    /**
     * Field PARENT_ID_RESTRICTION
     */
    private static final String PARENT_ID_RESTRICTION = "detachedCriteria = DetachedCriteria.forClass(%s.class);detachedCriteria.add(Restrictions.idEq($%d));if (idName == null) {ClassMetadata parentMetadata = session.getSessionFactory().getClassMetadata(%s.class);idName = parentMetadata.getIdentifierPropertyName();}detachedCriteria = detachedCriteria.createCriteria(\"%s\", id);";

    /**
     * Field BY_PARENT_CRITERIA
     */
    private static final String BY_PARENT_CRITERIA = "criteria = session.createCriteria(entityClass);criteria.add(Property.forName(idName).in(detachedCriteria.setProjection(Projections.groupProperty(idName))));";

    /**
     * Field ORDER_ASC
     */
    private static final String ORDER_ASC = "temp = createAlias(\"%s\", criteria); criteria.addOrder(Order.asc(temp));";

    /**
     * Field ORDER_DESC
     */
    private static final String ORDER_DESC = "temp = createAlias(\"%s\", criteria); criteria.addOrder(Order.desc(temp));";

    /**
     * Field ORDER_CRITERIA
     */
    private static final String ORDER_CRITERIA = "Ordering sortingCriteriaValue = $%d;while (sortingCriteriaValue != null) {String alias = createAlias(sortingCriteriaValue.getField(), criteria); if (sortingCriteriaValue.getDirection() == Ordering.Direction.ASC) {criteria.addOrder(Order.asc(alias));} else if (sortingCriteriaValue.getDirection() == Ordering.Direction.DESC) {criteria.addOrder(Order.desc(alias));}sortingCriteriaValue = sortingCriteriaValue.getChild();}";

    /**
     * Field PAGINATION
     */
    private static final String PAGINATION = "if ($%d != null) {criteria.setFirstResult($%d.getFirstResult());criteria.setMaxResults($%d.getMaxResults());}";

    /**
     * Field CREATE_CRITERIA
     */
    private static final String CREATE_CRITERIA = "Criteria criteria = session.createCriteria(entityClass); Criteria tempCriteria; String[] path; String source;";

    /**
     * Field RESTRICT_EQ
     */
    private static final String RESTRICT_EQ = "source = \"%s\"; path = source.split(\"\\\\.\"); tempCriteria = criteria; for (int i = 0; i < path.length - 1; i++) {tempCriteria = tempCriteria.createCriteria(path[i]);} if ($%d == null) {tempCriteria.add(Restrictions.isNull(path[path.length - 1]));} else {tempCriteria.add(Restrictions.eq(path[path.length - 1], $%d));}";

    /**
     * Field RESTRICT_IEQ
     */
    private static final String RESTRICT_IEQ = "source = \"%s\"; path = source.split(\"\\\\.\"); tempCriteria = criteria; for (int i = 0; i < path.length - 1; i++) {tempCriteria = tempCriteria.createCriteria(path[i]);} tempCriteria.add(Restrictions.ilike(path[path.length - 1], (String) $%d, MatchMode.EXACT));";

    /**
     * Field RESTRICT_LIKE
     */
    private static final String RESTRICT_LIKE = "source = \"%s\"; path = source.split(\"\\\\.\"); tempCriteria = criteria; for (int i = 0; i < path.length - 1; i++) {tempCriteria = tempCriteria.createCriteria(path[i]);} tempCriteria.add(Restrictions.like(path[path.length - 1], (String) $%d, MatchMode.ANYWHERE));";

    /**
     * Field RESTRICT_ILIKE
     */
    private static final String RESTRICT_ILIKE = "source = \"%s\"; path = source.split(\"\\\\.\"); tempCriteria = criteria; for (int i = 0; i < path.length - 1; i++) {tempCriteria = tempCriteria.createCriteria(path[i]);} tempCriteria.add(Restrictions.ilike(path[path.length - 1], (String) $%d, MatchMode.ANYWHERE));";

    /**
     * Field RESTRICT_CRITERIA
     */
    private static final String RESTRICT_CRITERIA = "Restriction restrictionValue = $%d;Criterion criterion = getCriterion(restrictionValue, criteria);if (criterion != null) {criteria.add(criterion);}";

    /**
     * Field RETURN_COUNT_WITH_PARENT
     */
    private static final String RETURN_COUNT_WITH_PARENT = "return ($r) detachedCriteria.getExecutableCriteria(session).setProjection(Projections.countDistinct(id + \".\" + idName)).uniqueResult();";

    /**
     * Field RETURN_LIST
     */
    private static final String RETURN_LIST = "return ($r) criteria.list();";

    /**
     * Field RETURN_COUNT
     */
    private static final String RETURN_COUNT = "return ($r) criteria.setProjection(Projections.rowCount()).uniqueResult();";

    /**
     * Field RETURN_UNIQUE
     */
    private static final String RETURN_UNIQUE = "return ($r) criteria.uniqueResult();";

    /**
     * Field HQL_1
     */
    private static final String HQL_1 = "Query query = session.createQuery(\"%s\"); query.setFirstResult(%d); query.setMaxResults(%d);";

    /**
     * Field HQL_PARAMETER
     */
    private static final String HQL_PARAMETER = "query.setParameter(%d, $%d);";

    /**
     * Field HQL_PARAMETER
     */
    private static final String HQL_NAMED_PARAMETER = "query.setParameter(\"%s\", $%d);";

    /**
     * Field HQL_PARAMETER
     */
    private static final String HQL_NAMED_COLLECTION_PARAMETER = "query.setParameterList(\"%s\", $%d);";

    /**
     * Field HQL_UPDATE
     */
    private static final String HQL_UPDATE = "return ($r) query.executeUpdate();";

    /**
     * Field HQL_LIST
     */
    private static final String HQL_LIST = "return ($r) query.list();";

    /**
     * Field HQL_UNIQUE
     */
    private static final String HQL_UNIQUE = "return ($r) query.uniqueResult();";

    /**
     * Get dao instance.
     *
     * @param daoClass       Class of desired DAO
     * @param entityClass    Entity class used in specified DAO
     * @param sessionFactory Session Factory to use
     * @return DAO instance
     */
    @SuppressWarnings({ "unchecked" })
    public static Object getInstance(Class daoClass, Class entityClass, SessionFactory sessionFactory) {
        log.info("Creating implementation of " + daoClass.getName());
        log.info("Entity class name: " + entityClass.getName());
        if (daoClass == null || entityClass == null) {
            throw new IllegalArgumentException("Both parameters should not be null");
        }
        String implClassName = daoClass.getName() + "DynDaoImpl";
        String implClassShortName = daoClass.getSimpleName() + "DynDaoImpl";
        try {
            Class daoImpl = Class.forName(implClassName);
            log.info("Already implemented; returning instance of loaded class");
            return daoImpl.getConstructor(Class.class, SessionFactory.class).newInstance(entityClass, sessionFactory);
        } catch (ClassNotFoundException ex) {
            log.info("Creating implementation");
            try {
                ClassPool classPool = ClassPool.getDefault();
                classPool.insertClassPath(new ClassClassPath(daoClass));
                CtClass dao;
                if (daoClass.isInterface()) {
                    dao = classPool.makeClass(implClassName);
                    dao.addInterface(classPool.get(daoClass.getName()));
                } else {
                    CtClass parent = classPool.get(daoClass.getName());
                    dao = classPool.makeClass(implClassName, parent);
                }
                classPool.importPackage("org.hibernate");
                classPool.importPackage("org.hibernate.criterion");
                classPool.importPackage("org.hibernate.metadata");
                classPool.importPackage("ru.maxmatveev.dyndao");
                classPool.importPackage("ru.maxmatveev.dyndao.impl.hibernate");
                classPool.importPackage("java.io");
                classPool.importPackage("java.util");
                dao.addField(CtField.make("private final Class entityClass;", dao));
                dao.addField(CtField.make("private final SessionFactory sessionFactory;", dao));
                dao.addField(CtField.make("private String id;", dao));
                dao.addField(CtField.make("private String temp;", dao));
                dao.addField(CtField.make("private ThreadLocal aliases = new ThreadLocal();", dao));
                dao.addField(CtField.make("private String idName = null;", dao));
                dao.addConstructor(CtNewConstructor.make(String.format(DEFAULT_CONSTRUCTOR, implClassShortName), dao));
                dao.addConstructor(CtNewConstructor.make(String.format(CONSTRUCTOR, implClassShortName), dao));
                dao.addMethod(CtNewMethod.make(DELETE, dao));
                dao.addMethod(CtNewMethod.make(MERGE, dao));
                dao.addMethod(CtNewMethod.make(GET_BY_ID, dao));
                dao.addMethod(CtNewMethod.make(GET_ALL, dao));
                dao.addMethod(CtNewMethod.make(SAVE, dao));
                dao.addMethod(CtNewMethod.make(CREATE_ALIAS, dao));
                dao.addMethod(CtNewMethod.make(CREATE_ALIAS_DT, dao));
                dao.addMethod(CtNewMethod.make(GET_CRITERION, dao));
                dao.addMethod(CtNewMethod.make(GET_CRITERION_DT, dao));
                dao.addMethod(CtNewMethod.make(GET_SESSION_FACTORY, dao));
                for (CtMethod method : dao.getMethods()) {
                    if (Modifier.isAbstract(method.getModifiers()) && isDynDaoMethod(method)) {
                        log.info("Implementing method: " + method.getName());
                        String modifiers = Modifier.toString(method.getModifiers() & ~Modifier.ABSTRACT);
                        String retType = method.getReturnType().getName();
                        String name = method.getName();
                        String params;
                        StringBuilder sb = new StringBuilder();
                        int idx = 0;
                        for (CtClass param : method.getParameterTypes()) {
                            if (idx != 0) {
                                sb.append(", ");
                            }
                            sb.append(String.format("%s p%d", param.getName(), idx++));
                        }
                        params = sb.toString();
                        String exceptions;
                        sb = new StringBuilder();
                        idx = 0;
                        for (CtClass e : method.getExceptionTypes()) {
                            if (idx != 0) {
                                sb.append(", ");
                            } else {
                                sb.append("throws ");
                            }
                            idx++;
                            sb.append(e.getName());
                        }
                        exceptions = sb.toString();
                        StringBuilder impl = new StringBuilder();
                        impl.append(GET_SESSION);
                        impl.append("aliases.set(new HashSet());");
                        boolean count = (getCountAnnotation(method) != null);
                        Hql hql = getHqlAnnotation(method);
                        if (count && hql != null) {
                            throw new IllegalArgumentException("Hql annotation cannot be used in one method with other DynDao annotations");
                        }
                        boolean byParent = false;
                        boolean restrict = false;
                        boolean sorting = false;
                        boolean pagination = false;
                        List<Integer> parentRestrictionIndex = new ArrayList<Integer>();
                        List<Restriction.Parent> parentRestriction = new ArrayList<Restriction.Parent>();
                        List<Integer> parentFieldRestrictionIndex = new ArrayList<Integer>();
                        List<Restriction.ParentField> parentFieldRestriction = new ArrayList<Restriction.ParentField>();
                        List<Integer> parentIdRestrictionIndex = new ArrayList<Integer>();
                        List<Restriction.ParentId> parentIdRestriction = new ArrayList<Restriction.ParentId>();
                        List<Integer> restrictionIndex = new ArrayList<Integer>();
                        List<Restriction.Restrict> restriction = new ArrayList<Restriction.Restrict>();
                        List<Integer> restrictionFieldIndex = new ArrayList<Integer>();
                        List<Restriction.Field> restrictionField = new ArrayList<Restriction.Field>();
                        List<Integer> sortingCriteriaIndex = new ArrayList<Integer>();
                        List<Ordering.Criteria> sortingCriteria = new ArrayList<Ordering.Criteria>();
                        List<Ordering.Field> sortingField = new ArrayList<Ordering.Field>();
                        int paginationIndex = -1;
                        if (getOrderingFieldAnnotation(method) != null) {
                            sortingField.add(getOrderingFieldAnnotation(method));
                            if (count) {
                                throw new IllegalArgumentException("Sorting should not be used with @Count annotated methods");
                            }
                            if (hql != null) {
                                throw new IllegalArgumentException("Hql annotation cannot be used in one method with other DynDao annotations");
                            }
                            sorting = true;
                        }
                        CtClass[] arguments = method.getParameterTypes();
                        for (int i = 0; i < method.getParameterAnnotations().length; i++) {
                            Annotation annotation;
                            if ((annotation = getParamtererAnnotation(method.getParameterAnnotations()[i])) != null) {
                                if (annotation.annotationType() == Restriction.Parent.class) {
                                    if (hql != null) {
                                        throw new IllegalArgumentException("Hql annotation cannot be used in one method with other DynDao annotations");
                                    }
                                    if (!arguments[i].subtypeOf(classPool.get(Restriction.class.getName()))) {
                                        throw new IllegalArgumentException("Annotation @Restriction.Parent should be used only on Resctriction type parameters.");
                                    }
                                    parentRestriction.add((Restriction.Parent) annotation);
                                    parentRestrictionIndex.add(i);
                                    byParent = true;
                                } else if (annotation.annotationType() == Restriction.ParentField.class) {
                                    if (hql != null) {
                                        throw new IllegalArgumentException("Hql annotation cannot be used in one method with other DynDao annotations");
                                    }
                                    parentFieldRestriction.add((Restriction.ParentField) annotation);
                                    parentFieldRestrictionIndex.add(i);
                                    byParent = true;
                                } else if (annotation.annotationType() == Restriction.ParentId.class) {
                                    if (hql != null) {
                                        throw new IllegalArgumentException("Hql annotation cannot be used in one method with other DynDao annotations");
                                    }
                                    if (!arguments[i].subtypeOf(classPool.get("java.io.Serializable"))) {
                                        throw new IllegalArgumentException("Annotation @Restriction.ParentId should be used only on Serializable type parameters.");
                                    }
                                    parentIdRestriction.add((Restriction.ParentId) annotation);
                                    parentIdRestrictionIndex.add(i);
                                    byParent = true;
                                } else if (annotation.annotationType() == Restriction.Restrict.class) {
                                    if (hql != null) {
                                        throw new IllegalArgumentException("Hql annotation cannot be used in one method with other DynDao annotations");
                                    }
                                    if (!arguments[i].subtypeOf(classPool.get(Restriction.class.getName()))) {
                                        throw new IllegalArgumentException("Annotation @Restriction.Restrict should be used only on Resctriction type parameters.");
                                    }
                                    restriction.add((Restriction.Restrict) annotation);
                                    restrictionIndex.add(i);
                                    restrict = true;
                                } else if (annotation.annotationType() == Restriction.Field.class) {
                                    if (hql != null) {
                                        throw new IllegalArgumentException("Hql annotation cannot be used in one method with other DynDao annotations");
                                    }
                                    restrictionField.add((Restriction.Field) annotation);
                                    restrictionFieldIndex.add(i);
                                    restrict = true;
                                } else if (annotation.annotationType() == Ordering.Criteria.class) {
                                    if (hql != null) {
                                        throw new IllegalArgumentException("Hql annotation cannot be used in one method with other DynDao annotations");
                                    }
                                    if (count) {
                                        throw new IllegalArgumentException("Sorting should not be used with @Count annotated methods");
                                    }
                                    if (!arguments[i].subtypeOf(classPool.get(Ordering.class.getName()))) {
                                        throw new IllegalArgumentException("Annotation @Ordering.Criteria should be used only on Ordering type parameters.");
                                    }
                                    sortingCriteria.add((Ordering.Criteria) annotation);
                                    sortingCriteriaIndex.add(i);
                                    sorting = true;
                                } else if (annotation.annotationType() == Pagination.Paginate.class) {
                                    if (hql != null) {
                                        throw new IllegalArgumentException("Hql annotation cannot be used in one method with other DynDao annotations");
                                    }
                                    if (pagination) {
                                        throw new IllegalArgumentException("There is more than one pagination argument in method definition.");
                                    }
                                    if (count) {
                                        throw new IllegalArgumentException("Pagination should not be used with @Count annotated methods");
                                    }
                                    if (!arguments[i].subtypeOf(classPool.get(Pagination.class.getName()))) {
                                        throw new IllegalArgumentException("Annotation @Pagination.Paginate should be used only on Pagination type parameters.");
                                    }
                                    paginationIndex = i;
                                    pagination = true;
                                }
                            }
                        }
                        if (hql != null) {
                            impl.append(String.format(HQL_1, hql.query(), hql.first(), hql.max()));
                            for (int i = 0; i < method.getParameterTypes().length; i++) {
                                Named named = null;
                                for (int j = 0; j < method.getParameterAnnotations()[i].length; j++) {
                                    if (method.getParameterAnnotations()[i][j] instanceof Named) {
                                        named = (Named) method.getParameterAnnotations()[i][j];
                                    }
                                }
                                if (named != null) {
                                    if (Collection.class.isAssignableFrom(Class.forName(method.getParameterTypes()[i].getName()))) {
                                        impl.append(String.format(HQL_NAMED_COLLECTION_PARAMETER, named.value(), i + 1));
                                    } else {
                                        impl.append(String.format(HQL_NAMED_PARAMETER, named.value(), i + 1));
                                    }
                                } else {
                                    impl.append(String.format(HQL_PARAMETER, i, i + 1));
                                }
                            }
                            if (hql.query().toLowerCase().startsWith("update") || hql.query().toLowerCase().startsWith("delete")) {
                                impl.append(HQL_UPDATE);
                            } else {
                                if (method.getReturnType().subtypeOf(classPool.get("java.util.Collection"))) {
                                    impl.append(HQL_LIST);
                                } else {
                                    impl.append(HQL_UNIQUE);
                                }
                            }
                        } else if (byParent) {
                            impl.append(DEFINE_BY_PARENT_CRITERIAS);
                            for (Restriction.Parent aParentRestriction : parentRestriction) {
                                impl.append(String.format(PARENT_RESTRICTION_1, aParentRestriction.parentClass().getName(), aParentRestriction.parentClass().getName()));
                                for (Integer aParentRestrictionIndex : parentRestrictionIndex) {
                                    impl.append(String.format(PARENT_RESTRICTION_2, aParentRestrictionIndex + 1));
                                }
                                impl.append(String.format(PARENT_RESTRICTION_3, aParentRestriction.fieldName()));
                            }
                            for (int i = 0; i < parentFieldRestriction.size(); i++) {
                                impl.append(String.format(PARENT_RESTRICTION_1, parentFieldRestriction.get(i).parentClass().getName(), parentFieldRestriction.get(i).parentClass().getName()));
                                if (parentFieldRestriction.get(i).filterFieldComparison() == Restriction.Comparison.EQUALS) {
                                    impl.append(String.format(DT_RESTRICTION_EQ, parentFieldRestriction.get(i).filterFieldName(), parentFieldRestrictionIndex.get(i) + 1));
                                } else if (parentFieldRestriction.get(i).filterFieldComparison() == Restriction.Comparison.IEQUALS) {
                                    impl.append(String.format(DT_RESTRICTION_IEQ, parentFieldRestriction.get(i).filterFieldName(), parentFieldRestrictionIndex.get(i) + 1));
                                } else if (parentFieldRestriction.get(i).filterFieldComparison() == Restriction.Comparison.LIKE) {
                                    impl.append(String.format(DT_RESTRICTION_LIKE, parentFieldRestriction.get(i).filterFieldName(), parentFieldRestrictionIndex.get(i) + 1));
                                } else if (parentFieldRestriction.get(i).filterFieldComparison() == Restriction.Comparison.ILIKE) {
                                    impl.append(String.format(DT_RESTRICTION_ILIKE, parentFieldRestriction.get(i).filterFieldName(), parentFieldRestrictionIndex.get(i) + 1));
                                }
                                impl.append(String.format(PARENT_RESTRICTION_3, parentFieldRestriction.get(i).fieldName()));
                            }
                            for (int i = 0; i < parentIdRestriction.size(); i++) {
                                impl.append(String.format(PARENT_ID_RESTRICTION, parentIdRestriction.get(i).parentClass().getName(), parentIdRestrictionIndex.get(i) + 1, parentIdRestriction.get(i).parentClass().getName(), parentIdRestriction.get(i).fieldName()));
                            }
                            if (restrict) {
                                for (int i = 0; i < restrictionField.size(); i++) {
                                    if (restrictionField.get(i).comparison() == Restriction.Comparison.EQUALS) {
                                        impl.append(String.format(DT_RESTRICTION_EQ, restrictionField.get(i).fieldName(), restrictionFieldIndex.get(i) + 1));
                                    } else if (restrictionField.get(i).comparison() == Restriction.Comparison.IEQUALS) {
                                        impl.append(String.format(DT_RESTRICTION_IEQ, restrictionField.get(i).fieldName(), restrictionFieldIndex.get(i) + 1));
                                    } else if (restrictionField.get(i).comparison() == Restriction.Comparison.LIKE) {
                                        impl.append(String.format(DT_RESTRICTION_LIKE, restrictionField.get(i).fieldName(), restrictionFieldIndex.get(i) + 1));
                                    } else if (restrictionField.get(i).comparison() == Restriction.Comparison.ILIKE) {
                                        impl.append(String.format(DT_RESTRICTION_ILIKE, restrictionField.get(i).fieldName(), restrictionFieldIndex.get(i) + 1));
                                    }
                                }
                                for (int i = 0; i < restriction.size(); i++) {
                                    impl.append(String.format(RESTRICTION_WITH_PARENT, restrictionIndex.get(i) + 1));
                                }
                            }
                            if (count) {
                                impl.append(RETURN_COUNT_WITH_PARENT);
                            } else {
                                impl.append(BY_PARENT_CRITERIA);
                                if (sorting) {
                                    for (Ordering.Field aSortingField : sortingField) {
                                        if (aSortingField.direction() == Ordering.Direction.ASC) {
                                            impl.append(String.format(ORDER_ASC, aSortingField.fieldName()));
                                        } else if (aSortingField.direction() == Ordering.Direction.DESC) {
                                            impl.append(String.format(ORDER_DESC, aSortingField.fieldName()));
                                        }
                                    }
                                    for (Integer aSortingCriteriaIndex : sortingCriteriaIndex) {
                                        impl.append(String.format(ORDER_CRITERIA, aSortingCriteriaIndex + 1));
                                    }
                                }
                                if (pagination) {
                                    if (paginationIndex != -1) {
                                        impl.append(String.format(PAGINATION, paginationIndex + 1, paginationIndex + 1, paginationIndex + 1));
                                    }
                                }
                                if (count) {
                                    impl.append(RETURN_COUNT);
                                } else {
                                    if (method.getReturnType().subtypeOf(classPool.get("java.util.Collection"))) {
                                        impl.append(RETURN_LIST);
                                    } else {
                                        impl.append(RETURN_UNIQUE);
                                    }
                                }
                            }
                        } else {
                            impl.append(CREATE_CRITERIA);
                            if (restrict) {
                                for (int i = 0; i < restrictionField.size(); i++) {
                                    if (restrictionField.get(i).comparison() == Restriction.Comparison.EQUALS) {
                                        impl.append(String.format(RESTRICT_EQ, restrictionField.get(i).fieldName(), restrictionFieldIndex.get(i) + 1, restrictionFieldIndex.get(i) + 1));
                                    } else if (restrictionField.get(i).comparison() == Restriction.Comparison.IEQUALS) {
                                        impl.append(String.format(RESTRICT_IEQ, restrictionField.get(i).fieldName(), restrictionFieldIndex.get(i) + 1));
                                    } else if (restrictionField.get(i).comparison() == Restriction.Comparison.LIKE) {
                                        impl.append(String.format(RESTRICT_LIKE, restrictionField.get(i).fieldName(), restrictionFieldIndex.get(i) + 1));
                                    } else if (restrictionField.get(i).comparison() == Restriction.Comparison.ILIKE) {
                                        impl.append(String.format(RESTRICT_ILIKE, restrictionField.get(i).fieldName(), restrictionFieldIndex.get(i) + 1));
                                    }
                                }
                                for (Integer aRestrictionIndex : restrictionIndex) {
                                    impl.append(String.format(RESTRICT_CRITERIA, aRestrictionIndex + 1));
                                }
                            }
                            if (sorting) {
                                for (Ordering.Field aSortingField : sortingField) {
                                    if (aSortingField.direction() == Ordering.Direction.ASC) {
                                        impl.append(String.format(ORDER_ASC, aSortingField.fieldName()));
                                    } else if (aSortingField.direction() == Ordering.Direction.DESC) {
                                        impl.append(String.format(ORDER_DESC, aSortingField.fieldName()));
                                    }
                                }
                                for (Integer aSortingCriteriaIndex : sortingCriteriaIndex) {
                                    impl.append(String.format(ORDER_CRITERIA, aSortingCriteriaIndex + 1));
                                }
                            }
                            if (pagination) {
                                if (paginationIndex != -1) {
                                    impl.append(String.format(PAGINATION, paginationIndex + 1, paginationIndex + 1, paginationIndex + 1));
                                }
                            }
                            if (count) {
                                impl.append(RETURN_COUNT);
                            } else {
                                if (method.getReturnType().subtypeOf(classPool.get("java.util.Collection"))) {
                                    impl.append(RETURN_LIST);
                                } else {
                                    impl.append(RETURN_UNIQUE);
                                }
                            }
                        }
                        impl.append("}");
                        String implementation = new StringBuffer().append(modifiers).append(" ").append(retType).append(" ").append(name).append("(").append(params).append(") ").append(exceptions).append(" ").append(impl).toString();
                        log.debug(implementation);
                        CtMethod implementedMethod = CtNewMethod.make(implementation, dao);
                        dao.addMethod(implementedMethod);
                    }
                }
                log.info("Loading generated class");
                Class daoImpl = dao.toClass(entityClass.getClassLoader(), null);
                log.info("Detaching");
                dao.detach();
                log.info("Returning new instance");
                return daoImpl.getConstructor(Class.class, SessionFactory.class).newInstance(entityClass, sessionFactory);
            } catch (NotFoundException e) {
                throw new IllegalArgumentException("Super interface/class for DAO is not found.", e);
            } catch (Exception e) {
                throw new IllegalStateException("Cannot create new DAO class.", e);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot instantiate existing DAO class.", ex);
        }
    }

    /**
     * Get Count method annotation.
     *
     * @param method Method to get annotation from
     * @return annotation, or null if it is not found
     * @throws ClassNotFoundException if class is not found
     */
    private static Count getCountAnnotation(CtMethod method) throws ClassNotFoundException {
        for (Object annotation : method.getAnnotations()) {
            if (annotation instanceof Count) {
                return (Count) annotation;
            }
        }
        return null;
    }

    /**
     * Get Hql method annotation.
     *
     * @param method Method to get annotation from
     * @return annotation, or null if it is not found
     * @throws ClassNotFoundException if class is not found
     */
    private static Hql getHqlAnnotation(CtMethod method) throws ClassNotFoundException {
        for (Object annotation : method.getAnnotations()) {
            if (annotation instanceof Hql) {
                return (Hql) annotation;
            }
        }
        return null;
    }

    /**
     * Get Ordering.Field method annotation.
     *
     * @param method Method to get annotation from
     * @return annotation, or null if it is not found
     * @throws ClassNotFoundException if class is not found
     */
    private static Ordering.Field getOrderingFieldAnnotation(CtMethod method) throws ClassNotFoundException {
        for (Object annotation : method.getAnnotations()) {
            if (annotation instanceof Ordering.Field) {
                return (Ordering.Field) annotation;
            }
        }
        return null;
    }

    /**
     * Check if method definition contains DynDao annotation(s).
     *
     * @param method method to check
     * @return true if contains
     * @throws ClassNotFoundException if class is not found
     */
    private static boolean isDynDaoMethod(CtMethod method) throws ClassNotFoundException {
        for (Object annotation : method.getAnnotations()) {
            if (annotation instanceof Count) {
                return true;
            }
            if (annotation instanceof Hql) {
                return true;
            }
            if (annotation instanceof Ordering.Field) {
                return true;
            }
        }
        for (Object[] annotations : method.getParameterAnnotations()) {
            for (Object annotation : annotations) {
                if (annotation instanceof Ordering.Criteria || annotation instanceof Pagination.Paginate || annotation instanceof Restriction.ParentId || annotation instanceof Restriction.ParentField || annotation instanceof Restriction.Parent || annotation instanceof Restriction.Field || annotation instanceof Restriction.Restrict) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get DynDao parameter annotation.
     *
     * @param annotations annotations array to check
     * @return Annotation, or null if annotation is not present
     */
    private static Annotation getParamtererAnnotation(Object[] annotations) {
        int dynDaoAnnotationCount = 0;
        Object parameterAnnotation = null;
        for (Object annotation : annotations) {
            if (annotation instanceof Ordering.Criteria || annotation instanceof Pagination.Paginate || annotation instanceof Restriction.ParentId || annotation instanceof Restriction.ParentField || annotation instanceof Restriction.Parent || annotation instanceof Restriction.Field || annotation instanceof Restriction.Restrict) {
                dynDaoAnnotationCount++;
                parameterAnnotation = annotation;
            }
        }
        if (dynDaoAnnotationCount > 1) {
            throw new IllegalArgumentException("More than one DynDao annotation are present on one parameter");
        }
        return (Annotation) parameterAnnotation;
    }
}
