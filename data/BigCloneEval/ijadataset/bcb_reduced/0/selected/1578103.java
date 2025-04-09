package com.ail.core.configure.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.ail.core.Core;
import com.ail.core.PostconditionException;
import com.ail.core.PreconditionException;
import com.ail.core.Version;
import com.ail.core.XMLString;
import com.ail.core.command.CommandArg;
import com.ail.core.configure.ConfigurationHandler;
import com.ail.core.configure.UnknownNamespaceError;

/**
 * @version $Revision: 1.1 $
 * @state $State: Exp $
 * @date $Date: 2005/09/03 18:07:56 $
 * @source $Source: /home/bob/CVSRepository/projects/core/core.ear/core.jar/com/ail/core/configure/server/PackageCarService.java,v $
 * @stereotype service
 */
public class PackageCarService extends com.ail.core.Service {

    private PackageCarArg args = null;

    private Core core = null;

    /** Default constructor */
    public PackageCarService() {
        core = new Core(this);
    }

    /**
     * Getter to fetch the entry point's code. This method is demanded by the EntryPoint class.
     * @return This entry point's instance of Core.
     */
    public Core getCore() {
        return core;
    }

    /**
     * Fetch the version of this entry point.
     * @return A version object describing the version of this entry point.
     */
    public com.ail.core.Version getVersion() {
        Version v = (Version) core.newType("Version");
        v.setCopyright("Copyright Applied Industrial Logic Limited 2003. All rights reserved.");
        v.setDate("$Date: 2005/09/03 18:07:56 $");
        v.setSource("$Source: /home/bob/CVSRepository/projects/core/core.ear/core.jar/com/ail/core/configure/server/PackageCarService.java,v $");
        v.setState("$State: Exp $");
        v.setVersion("$Revision: 1.1 $");
        return v;
    }

    /**
     * Setter used to the set the entry points arguments.
     * @param args for invoke
     */
    public void setArgs(CommandArg args) {
        this.args = (PackageCarArg) args;
    }

    /**
     * Getter returning the arguments used by this entry point.
     * @return An instance of PackageCarFileArgs.
     */
    public CommandArg getArgs() {
        return args;
    }

    /** The 'business logic' of the entry point. */
    public void invoke() throws PreconditionException, PostconditionException {
        String namespace = null;
        if (args.getNamespacesArg() == null) {
            throw new PreconditionException("args.getNamespacesArg()==null");
        }
        if (args.getNamespacesArg().size() == 0) {
            throw new PreconditionException("args.getNamespacesArg().size()==0");
        }
        try {
            XMLString config = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);
            for (Iterator<String> it = args.getNamespacesArg().iterator(); it.hasNext(); ) {
                namespace = it.next();
                config = core.toXML(ConfigurationHandler.getInstance().loadConfiguration(namespace, core, core));
                zos.putNextEntry(new ZipEntry(namespace));
                zos.write(config.toString().getBytes(), 0, config.toString().getBytes().length);
                zos.closeEntry();
            }
            zos.close();
            args.setCarRet(baos.toByteArray());
            baos.close();
        } catch (IOException ex) {
            throw new CarProcessingError("Failed to build Car file: ", ex);
        } catch (UnknownNamespaceError e) {
            throw new PreconditionException("Configuration is not defined:" + namespace);
        }
        if (args.getCarRet() == null) {
            throw new PostconditionException("args.getCarRet()==null");
        }
    }
}
