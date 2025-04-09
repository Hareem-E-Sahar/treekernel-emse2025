import java.util.*;
import java.text.*;
import org.omg.CORBA.*;
import Tango.*;
import TangoDs.*;

/**
 *	Class Description:
 *	a clock device server
 *	
 *	RP LGM/ESRF
 */
public class DSClock extends DeviceImpl implements TangoConst {

    protected int state;

    long offset = 0;

    protected short[] attr_Seconde_read = new short[1];

    DSClock(DeviceClass cl, String s) throws DevFailed {
        super(cl, s);
        init_device();
    }

    DSClock(DeviceClass cl, String s, String d) throws DevFailed {
        super(cl, s, d);
        init_device();
    }

    public void init_device() {
        System.out.println("DSClock() create " + dev_name);
        set_state(DevState.ON);
        offset = 0;
        System.out.println("DSClock(" + dev_name + ") initialized");
    }

    public void read_attr_hardware(Vector attr_list) {
        TangoUtil.out2.println("In read_attr_hardware for " + attr_list.size() + " attribute(s)");
    }

    public void read_attr(Attribute attr) throws DevFailed {
        String attr_name = attr.get_name();
        TangoUtil.out2.println("In read_attr for attribute " + attr_name);
        if (attr_name == "Seconde") {
        }
    }

    public DevState state_cmd() {
        TangoUtil.out2.println("In PowerSupply state command");
        return dev_state;
    }

    public String status_cmd() {
        TangoUtil.out2.println("In PowerSupply status command");
        return dev_status;
    }

    public String get_time_date() throws DevFailed {
        String argout = new String();
        TangoUtil.out2.println("Entering get_time_date()");
        argout = new Date(new Date().getTime() - offset).toString();
        TangoUtil.out2.println("Exiting get_time_date()");
        return argout;
    }

    public void set_date_time(String argin) throws DevFailed {
        TangoUtil.out2.println("Entering set_date_time()");
        DateFormat df = DateFormat.getDateTimeInstance();
        System.out.println("date format = " + df.format(new Date()) + "\n" + argin);
        try {
            long d1 = df.parse(argin).getTime();
            long d2 = new Date().getTime();
            offset = d2 - d1;
        } catch (java.text.ParseException e) {
            System.out.println("date parsing error");
        }
        TangoUtil.out2.println("Exiting set_date_time()");
    }

    public static void main(String[] argv) {
        try {
            TangoUtil tg = TangoUtil.init(argv, "dsclock");
            tg.server_init();
            System.out.println("Ready to accept request");
            tg.get_boa().impl_is_ready(null);
        } catch (OutOfMemoryError ex) {
            System.err.println("Can't allocate memory !!!!");
            System.err.println("Exiting");
        } catch (UserException ex) {
            TangoUtil.print_exception(ex);
            System.err.println("Received a CORBA user exception");
            System.err.println("Exiting");
        } catch (SystemException ex) {
            TangoUtil.print_exception(ex);
            System.err.println("Received a CORBA system exception");
            System.err.println("Exiting");
        }
        System.exit(-1);
    }
}
