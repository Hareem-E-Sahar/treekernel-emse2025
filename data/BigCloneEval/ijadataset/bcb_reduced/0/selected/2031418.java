package fr.macymed.modulo.module.user;

import java.lang.reflect.Constructor;
import java.util.Properties;
import fr.macymed.modulo.framework.module.Module;
import fr.macymed.modulo.framework.module.PropertiesConfiguration;
import fr.macymed.modulo.framework.service.user.UserService;
import fr.macymed.modulo.lib.user.UserDatabase;

/** 
 * <p>
 * This is the module class for the User Module.
 * </p>
 * @author <a href="mailto:alexandre.cartapanis@macymed.fr">Cartapanis Alexandre</a>
 * @version 2.0.0
 * @since Modulo User Management Module 2.0
 */
public class UserModule extends Module {

    /** The user's database. */
    private UserDatabase database;

    /**
     * @see fr.macymed.modulo.framework.module.Module#initialize()
     * @throws Exception
     */
    @Override
    public void initialize() throws Exception {
        Properties props = ((PropertiesConfiguration) this.getConfiguration()).getProperties();
        Class cla = Class.forName(props.getProperty("user.database.class"));
        Class[] paramsCla = new Class[2];
        paramsCla[0] = String.class;
        paramsCla[1] = Properties.class;
        Object[] params = new Object[2];
        params[0] = props.getProperty("user.database.id");
        params[1] = props;
        Constructor cons = cla.getConstructor(paramsCla);
        this.database = (UserDatabase) cons.newInstance(params);
    }

    /**
     * @see fr.macymed.modulo.framework.module.Module#start()
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        this.database.open();
        UserServiceImpl service = new UserServiceImpl(this.database);
        this.getServiceRegistry().bind(UserService.BINDING_NAME, service);
    }

    /**
     * @see fr.macymed.modulo.framework.module.Module#stop()
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        this.getServiceRegistry().unbind(UserService.BINDING_NAME);
        this.database.close();
    }

    /**
     * @see fr.macymed.modulo.framework.module.Module#deinitialize()
     * @throws Exception
     */
    @Override
    public void deinitialize() throws Exception {
        this.database = null;
    }
}
