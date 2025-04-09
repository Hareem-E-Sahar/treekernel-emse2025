package fireteam.orb;

import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantActivatorPOA;
import java.text.DateFormat;
import java.util.Date;

public class PoaServantActivator extends ServantActivatorPOA {

    public String m_sPackageName;

    public Servant incarnate(byte[] oid, POA adapter) throws ForwardRequest {
        try {
            System.out.println("[" + DateFormat.getDateTimeInstance().format(new Date()) + "]  " + "connected " + adapter.the_name());
            Class cl = Class.forName(m_sPackageName + adapter.the_name().replace("POA", "Impl"));
            return (Servant) cl.getConstructor(new Class[] { oid.getClass() }).newInstance(new Object[] { oid });
        } catch (Exception e) {
            System.err.println("preinvoke: Caught exception - " + e);
        }
        return null;
    }

    public void etherealize(byte[] oid, POA adapter, Servant serv, boolean cleanup_in_progress, boolean remaining_activations) {
        try {
            adapter.deactivate_object(oid);
            System.out.println("[" + DateFormat.getDateTimeInstance().format(new Date()) + "]  " + "disconnected " + adapter.the_name());
            Runtime.getRuntime().gc();
        } catch (Exception e) {
            System.err.println("postinvoke: Caught exception - " + e);
        }
    }
}
