public class Test {    public static void write(org.omg.CORBA.portable.OutputStream ostream, com.sun.corba.se.PortableActivationIDL.ServerAlreadyInstalled value) {
        ostream.write_string(id());
        ostream.write_string(value.serverId);
    }
}