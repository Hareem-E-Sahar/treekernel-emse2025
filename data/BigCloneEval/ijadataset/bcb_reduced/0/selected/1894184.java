package com.mgensystems.jarindexer.shell;

import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;
import com.mgensystems.jarindexer.model.ModelFactory;
import com.mgensystems.shell.Command;

/**
 * <b>Title:</b> Command Factory<br />
 * <b>Description:</b> Create shell commands for the jar indexer.<br />
 * <b>Changes:</b><ol><li></li></ol>
 * 
 * @author raykroeker@gmail.com
 */
public final class CommandFactory {

    /** A command helper. */
    private final CommandHelper helper;

    /**
	 * Create CommandFactory.
	 * 
	 * @param bundle
	 *            A <code>ResourceBundle</code>.
	 * @param modelFactory
	 *            A <code>ModelFactory</code>.
	 */
    public CommandFactory(final ResourceBundle bundle, final ModelFactory modelFactory) {
        super();
        this.helper = new CommandHelper(bundle, modelFactory);
    }

    /**
	 * Instantiate a command.
	 * 
	 * @param <T>
	 *            A <code>Command</code>.
	 * @param type
	 *            A <code>Class<T></code>.
	 * @return A <code>T</code>.
	 */
    public <T extends Command> T newCommand(final Class<T> type) {
        try {
            return (T) type.getConstructor(new Class<?>[] { CommandHelper.class }).newInstance(helper);
        } catch (final InstantiationException ix) {
            throw new RuntimeException(ix);
        } catch (final IllegalAccessException iax) {
            throw new RuntimeException(iax);
        } catch (final InvocationTargetException itx) {
            throw new RuntimeException(itx.getTargetException());
        } catch (final NoSuchMethodException nsmx) {
            throw new RuntimeException(nsmx);
        }
    }
}
