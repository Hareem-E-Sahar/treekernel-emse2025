package org.stars.daostars.exceptions.translators;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.HashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stars.dao.exception.DaoException;
import org.stars.dao.exception.parser.SyntaxErrorException;
import org.stars.daostars.SQLExceptionTranslator;
import org.stars.daostars.exceptions.DataIntegrityViolationException;
import org.stars.database.DatabaseInformation;
import org.stars.database.DatabaseType;
import org.stars.util.StringUtility;

/**
 * Lo scopo di questa traduttore � ottenere un'eccezione piu' granulare possibile
 * rispetto all'errore che si � verificato.
 * <p>
 * La prima cosa che si cerca di fare e' quella di ottenere informazioni
 * specifiche del vendor del database, mediante il codice
 * <code>vendorCode</code>. Nel caso in cui questo tipo di ricerca non dia esito
 * positivo, si cerca di dare una eccezione ricavata dal codice SQLState che
 * dovrebbe essere condiviso da tutti i db supportati.
 * <p>
 * Se ci troviamo innanzi ad un'eccezione non supportata, viene restituita una
 * DaoException generica.
 * 
 * @author Francesco Benincasa (908099)
 * 
 */
public class DefaultSQLExceptionTranslatorImpl implements SQLExceptionTranslator {

    /**
	 * 
	 */
    private static final long serialVersionUID = 9209156194108765271L;

    /**
	 * traduttori delle eccezion di secondo livello. Sono quelli legati alle
	 * specifiche implementazioni di vendor
	 */
    protected HashMap<DatabaseType, SQLExceptionTranslator> secondLevelTranslators;

    /**
	 * Tipo di query. Le query sono di tipo:
	 * <ul>
	 * <li>SELECT</li> <li>UPDATE</li> <li>INSERT</li> <li>DELETE</li> <li>
	 * UNKNOWN: nessuna delle precedenti</li>
	 * </ul>
	 * 
	 * @author Francesco Benincasa (908099)
	 * 
	 */
    public enum SqlType {

        SELECT, INSERT, UPDATE, DELETE, STORE_PROCEDURE, OTHER
    }

    ;

    /**
	 * In base alle regole presenti nell'hashtable codici, crea un'eccezione.
	 * <p>
	 * Cerca di creare un'eccezione in base al tipo, altrimenti prova a prendere
	 * l'eccezione associata al tipo ALL oppure l'eccezione piu' generica
	 * DaoException.
	 * 
	 * @param e
	 * @param type
	 * @return
	 * @throws Exception
	 */
    protected DaoException retrieveExceptionFromTable(SQLException e, SqlType type) {
        try {
            String key = e.getSQLState();
            if (codici.containsKey(key)) {
                DaoException ret = createException(codici.get(key), e);
                return ret;
            }
        } catch (Exception e1) {
            Log log = LogFactory.getLog(DefaultSQLExceptionTranslatorImpl.class);
            log.error(e.getMessage());
        }
        return null;
    }

    /**
	 * Crea un'istanza di eccezione in base al nome. Il secondo parametro �
	 * l'eccezione che causa il problema.
	 * 
	 * @param nome
	 *            nome della classe dell'eccezione
	 * @param e
	 *            eccezione causa
	 * @return eccezione
	 * @throws Exception
	 *             in caso di errore
	 */
    static DaoException createException(Class<?> clazz, Exception e) throws Exception {
        Constructor<?> c = clazz.getConstructor(Exception.class);
        DaoException ret = (DaoException) c.newInstance(e);
        return ret;
    }

    /**
	 * Identifica il tipo di sql.
	 * 
	 * @param sql
	 *            query sql
	 * @return tipo di sql
	 */
    protected SqlType getSqlType(String sql) {
        sql = StringUtility.nvl(sql).toLowerCase().trim();
        SqlType type = SqlType.OTHER;
        if (sql.startsWith("select")) {
            type = SqlType.SELECT;
        } else if (sql.startsWith("insert")) {
            type = SqlType.INSERT;
        } else if (sql.startsWith("update")) {
            type = SqlType.UPDATE;
        } else if (sql.startsWith("delete")) {
            type = SqlType.DELETE;
        } else if (sql.startsWith("call")) {
            type = SqlType.STORE_PROCEDURE;
        }
        return type;
    }

    /**
	 * Costruttore
	 */
    public DefaultSQLExceptionTranslatorImpl() {
        codici = new HashMap<String, Class<?>>();
        for (Object[] item : translation) {
            codici.put((String) item[0], (Class<?>) item[1]);
        }
        secondLevelTranslators = new HashMap<DatabaseType, SQLExceptionTranslator>();
        secondLevelTranslators.put(DatabaseType.ORACLE, new OracleSQLExceptionTranslator());
    }

    /**
	 * Per ogni tipo di mappatura, deve essere definito un codice sql, il tipo
	 * di sql da gestire, e l'eccezione da generare. Per ogni codice deve essere
	 * definito
	 */
    protected static Object translation[][] = { { "42000", SyntaxErrorException.class }, { "23000", DataIntegrityViolationException.class } };

    /**
	 * Mappatura codici eccezioni.
	 */
    protected HashMap<String, Class<?>> codici;

    @Override
    public DaoException translate(String sql, SQLException e, DatabaseInformation dbInfo) {
        DaoException ret = null;
        SqlType type = getSqlType(sql);
        if (secondLevelTranslators.containsKey(dbInfo.getType())) {
            SQLExceptionTranslator translator = secondLevelTranslators.get(dbInfo.getType());
            ret = translator.translate(sql, e, dbInfo);
            if (ret != null) {
                return ret;
            }
        }
        ret = retrieveExceptionFromTable(e, type);
        if (ret != null) {
            return ret;
        }
        ret = new DaoException(e);
        return ret;
    }
}
