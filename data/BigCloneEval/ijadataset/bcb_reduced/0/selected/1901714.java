package edu.uga.galileo.idt.db.dao;

import java.lang.reflect.Constructor;
import edu.uga.galileo.idt.exceptions.NoAvailableDAOException;
import edu.uga.galileo.idt.logging.Logger;
import edu.uga.galileo.idt.model.Configuration;

/**
 * Class that returns various data access objects for use throughout the system.
 * DAOs are created in here by reflection, taking the
 * {@link edu.uga.galileo.voci.model.Configuration} file's <code>dbType</code>
 * and uppercasing it, then appending the DAO type (<code>User</code>,
 * e.g.), and the string <code>DAO</code>. A <code>dbType</code> of 'psql',
 * for example, with a request for the user dao would require a class by the
 * name of PSQLUserDAO in this package.
 * 
 * @author <a href="mailto:mdurant@uga.edu">Mark Durant</a>
 * @version 1.0
 */
public class DAOFactory {

    /**
	 * Instantiate a data access object by reflection.
	 * 
	 * @return A <code>DAO</code>.
	 * @throws NoAvailableDAOException
	 *             If no <code>ProjectDAO</code> is found.
	 */
    private static DAO instantiateDAO(String type) throws NoAvailableDAOException {
        DAO dao;
        String dbType = Configuration.getString("dbType").toUpperCase();
        Class c;
        Constructor constructor;
        String fullDAOName = dbType + type + "DAO";
        try {
            c = Class.forName("edu.uga.galileo.voci.db.dao." + fullDAOName);
            constructor = c.getConstructor((Class[]) null);
            dao = (DAO) constructor.newInstance((Object[]) null);
            return dao;
        } catch (Exception e1) {
            Logger.error("Couldn't instantiate '" + fullDAOName + "'", e1);
            throw new NoAvailableDAOException("Couldn't instantiate a " + fullDAOName);
        }
    }
}
