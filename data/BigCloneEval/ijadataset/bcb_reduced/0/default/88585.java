import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

public class ObjectOutputStreamTest extends Test {

    public static void testSerial(Object obj, String filename) {
        if (writeMode) {
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
                oos.writeObject(obj);
                oos.close();
            } catch (ObjectStreamException e) {
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(bytes);
                oos.writeObject(obj);
                oos.close();
            } catch (ObjectStreamException e) {
            } catch (IOException ioe) {
                fail();
                return;
            }
            byte[] jcl_bytes = bytes.toByteArray();
            int data;
            FileInputStream jdk_file;
            try {
                jdk_file = new FileInputStream(filename);
                for (int i = 0; i < jcl_bytes.length; i++) {
                    data = jdk_file.read();
                    if (data == -1) {
                        fail();
                        return;
                    }
                    if ((byte) data != jcl_bytes[i]) {
                        fail();
                        return;
                    }
                }
                if (jdk_file.read() != -1) {
                    fail();
                    return;
                }
            } catch (IOException e) {
                error();
                return;
            }
            pass();
        }
    }

    public static void main(String[] args) {
        writeMode = (args.length != 0);
        testSerial(new OOSNotSerial(), "notserial.data");
        System.out.println("Non-serializable class");
        testSerial(new OOSBadField(1, 2, new OOSNotSerial()), "notserialfield.data");
        System.out.println("Object with non-serializable field");
        testSerial(new OOSCallDefault(1, 3.14, "test"), "calldefault.data");
        System.out.println("Object calling defaultWriteObject()");
        testSerial(new OOSNoCallDefault(17, "no\ndefault", false), "nocalldefault.data");
        System.out.println("Object not calling defaultWriteObject()");
        testSerial(new OOSExtern(-1, "", true), "external.data");
        System.out.println("Externalizable class");
        testSerial(new HairyGraph(), "graph.data");
        System.out.println("Graph of objects with circular references");
    }

    public static boolean writeMode;
}

class OOSNotSerial {
}

class OOSBadField implements Serializable {

    int x;

    int y;

    OOSNotSerial o;

    OOSBadField(int X, int Y, OOSNotSerial O) {
        x = X;
        y = Y;
        o = O;
    }
}
