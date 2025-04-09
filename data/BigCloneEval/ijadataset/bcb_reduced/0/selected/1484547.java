package net.sf.csv2sql.grammars.mysql;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Properties;
import net.sf.csv2sql.grammars.AbstractField;
import net.sf.csv2sql.grammars.AbstractGrammarFactory;
import net.sf.csv2sql.grammars.exceptions.GrammarFactoryException;

/**
 * @see net.sf.csv2sql.grammars.AbstractGrammarFactory AbstractGrammarFactory
 * @author <a href="mailto:dconsonni@enter.it">Davide Consonni</a>
 */
public final class GrammarFactory extends AbstractGrammarFactory {

    public static final Hashtable mapping = new Hashtable();

    static {
        mapping.put("VARCHAR", "net.sf.csv2sql.grammars.mysql.MysqlFieldChar");
        mapping.put("CHAR", "net.sf.csv2sql.grammars.mysql.MysqlFieldChar");
        mapping.put("TEXT", "net.sf.csv2sql.grammars.mysql.MysqlFieldChar");
        mapping.put("BIT", "net.sf.csv2sql.grammars.mysql.MysqlFieldBit");
        mapping.put("TINYINT", "net.sf.csv2sql.grammars.mysql.MysqlFieldTinyint");
        mapping.put("BOOL", "net.sf.csv2sql.grammars.mysql.MysqlFieldBit");
        mapping.put("BOOLEAN", "net.sf.csv2sql.grammars.mysql.MysqlFieldBit");
        mapping.put("SMALLINT", "net.sf.csv2sql.grammars.mysql.MysqlFieldSmallint");
        mapping.put("MEDIUMINT", "net.sf.csv2sql.grammars.mysql.MysqlFieldMediumint");
        mapping.put("INT", "net.sf.csv2sql.grammars.mysql.MysqlFieldInt");
        mapping.put("INTEGER", "net.sf.csv2sql.grammars.mysql.MysqlFieldInt");
        mapping.put("BIGINT", "net.sf.csv2sql.grammars.mysql.MysqlFieldBigint");
        mapping.put("FLOAT", "net.sf.csv2sql.grammars.mysql.MysqlFieldFloat");
        mapping.put("DOUBLE", "net.sf.csv2sql.grammars.mysql.MysqlFieldDouble");
        mapping.put("REAL", "net.sf.csv2sql.grammars.mysql.MysqlFieldDouble");
        mapping.put("DECIMAL", "net.sf.csv2sql.grammars.mysql.MysqlFieldDouble");
        mapping.put("DEC", "net.sf.csv2sql.grammars.mysql.MysqlFieldDouble");
        mapping.put("NUMERIC", "net.sf.csv2sql.grammars.mysql.MysqlFieldDouble");
        mapping.put("FIXED", "net.sf.csv2sql.grammars.mysql.MysqlFieldDouble");
        mapping.put("DATE", "net.sf.csv2sql.grammars.mysql.MysqlFieldDate");
    }

    public AbstractField createField(String type, String name, Properties prop) throws GrammarFactoryException {
        AbstractField field = null;
        try {
            String className = (String) mapping.get(type.toUpperCase());
            if (className == null || "".equals(className)) {
                throw new ClassNotFoundException("datatype not found in mapping");
            }
            Constructor c = Class.forName(className).getConstructor(new Class[] { String.class, Properties.class });
            Object o = c.newInstance(new Object[] { name, prop });
            field = (AbstractField) o;
        } catch (ClassNotFoundException e) {
            throw new GrammarFactoryException("cannot create field. type: " + type + ", name: " + name + ")", e.getException());
        } catch (NoSuchMethodException e) {
            throw new GrammarFactoryException("cannot create field. type: " + type + ", name: " + name + ")", e.getCause());
        } catch (InstantiationException e) {
            throw new GrammarFactoryException("cannot create field. type: " + type + ", name: " + name + ")", e.getCause());
        } catch (IllegalAccessException e) {
            throw new GrammarFactoryException("cannot create field. type: " + type + ", name: " + name + ")", e.getCause());
        } catch (InvocationTargetException e) {
            throw new GrammarFactoryException("cannot create field. type: " + type + ", name: " + name + ")", e.getCause());
        }
        if (field == null) {
            throw new GrammarFactoryException("field type [" + type + "]  doesn't exist. (field name: " + name + ")", null);
        } else {
            return field;
        }
    }
}
