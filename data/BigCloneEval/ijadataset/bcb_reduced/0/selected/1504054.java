package alt.jiapi.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import alt.jiapi.file.ConstantPool.Entry;
import alt.jiapi.file.attribute.Attribute;
import alt.jiapi.reflect.JiapiRuntimeException;

/**
 * ClassFile is a low level representation of Java class file. Class file format
 * is specified by JSR-202.
 * 
 * @author Mika Riekkinen
 */
public class ClassFile extends ProgramElement {

    private LinkedList<Interface> interfaces = new LinkedList<Interface>();

    private LinkedList<Field> fields = new LinkedList<Field>();

    private LinkedList<Method> methods = new LinkedList<Method>();

    /**
	 * Public access.
	 */
    public static final int ACC_PUBLIC = 0x0001;

    /**
	 * Final.
	 */
    public static final int ACC_FINAL = 0x0010;

    /**
	 * Super bit.
	 */
    public static final int ACC_SUPER = 0x0020;

    /**
	 * Class is an interface
	 */
    public static final int ACC_INTERFACE = 0x0200;

    /**
	 * Class is abstract
	 */
    public static final int ACC_ABSTRACT = 0x0400;

    /**
	 * Class is synthetic
	 */
    public static final int ACC_SYNTHETIC = 0x1000;

    /**
	 * Class is an Annotation
	 */
    public static final int ACC_ANNOTATION = 0x2000;

    /**
	 * Class is an Enum
	 */
    public static final int ACC_ENUM = 0x4000;

    private int magic_number;

    private short minor_version;

    private short major_version;

    private short this_class;

    private short super_class;

    /**
	 * Used for testing purposes.
	 * 
	 * @param args
	 *            args[0] is a path to java class file
	 */
    public static void main(String[] args) throws Exception {
        ClassFile cf = ClassFile.parse(args[0]);
        System.out.println("Magic: " + cf.getMagicNumber());
        String version = null;
        switch(cf.getMajorVersion()) {
            case 45:
                version = "JDK 1.1";
                break;
            case 46:
                version = "JDK 1.2";
                break;
            case 47:
                version = "JDK 1.3";
                break;
            case 48:
                version = "JDK 1.4";
                break;
            case 49:
                version = "J2SE 5.0";
                break;
            case 50:
                version = "J2SE 6.0";
                break;
            default:
                version = "Unknown version";
        }
        System.out.println(version + ", " + cf.getMajorVersion() + " " + cf.getMinorVersion());
        System.out.println("Access flags: " + cf.getAccessFlags());
        System.out.println(cf.getConstantPool());
    }

    /**
	 * Constructor to build ClassFile from scratch.
	 * 
	 * @param className
	 *            fully qualified name of the class.
	 */
    public ClassFile(String className) {
        super(new ConstantPool());
        String name = className.replace('.', '/');
        this.magic_number = 0xcafebabe;
        this.minor_version = 0;
        this.major_version = 46;
        this.access_flags = ACC_PUBLIC;
        this.this_class = constantPool.addClassInfo(name).getEntryIndex();
        this.super_class = constantPool.addClassInfo("java.lang.Object").getEntryIndex();
        this.interfaces = new LinkedList<Interface>();
        this.methods = new LinkedList<Method>();
        this.fields = new LinkedList<Field>();
        super.attributes = new LinkedList<Attribute>();
    }

    /**
	 * Constructor used, when parsing ClassFile from InputStream
	 */
    private ClassFile() {
        super(null);
    }

    /**
	 * Parse given file and create an instance of ClassFile from it.
	 * 
	 * @param fileName
	 *            name of the file, that is read.
	 * @return an instance of ClassFile, that conforms to java virtual machine
	 *         specification classfile format.
	 * @exception ParseException
	 *                is thrown, if classfile parser cannot understand parsed
	 *                stream.
	 * @exception IOException
	 *                is thrown, if there was problems in reading the stream.
	 */
    public static ClassFile parse(String fileName) throws ParseException, IOException {
        return parse(new FileInputStream(fileName));
    }

    /**
	 * Parse InputStream and create an instance of ClassFile from stream.
	 * 
	 * @param is
	 *            InputStream
	 * @return an instance of ClassFile, that conforms to java virtual machine
	 *         specification classfile format.
	 * @exception ParseException
	 *                is thrown, if classfile parser cannot understand parsed
	 *                stream.
	 * @exception IOException
	 *                is thrown, if there was problems in reading the stream.
	 */
    public static ClassFile parse(InputStream is) throws ParseException, IOException {
        InputStream input = null;
        if (config.getBoolean("alt.jiapi.file.use-ZipFileInputStream-bug-workaround", true)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(is.available());
            int i = 0;
            while ((i = is.read()) != -1) {
                bos.write(i);
            }
            input = new ByteArrayInputStream(bos.toByteArray());
        } else {
            input = is;
        }
        DataInputStream dis = new DataInputStream(input);
        ClassFile cf = null;
        try {
            cf = new ClassFile();
            cf.parseClassFile(dis);
            if (dis.available() != 0) {
                System.out.println(is.available() + ":::" + dis.available() + ":" + is);
                System.out.println("" + dis.readByte());
            }
        } catch (EOFException eof) {
            System.out.println(">> Got EOFException: " + eof + "," + is.available() + ", " + cf.getClassName());
        } catch (IOException ioe) {
            System.out.println("Got IOException: " + ioe + "," + is.available() + ", " + cf.getClassName());
            throw new ParseException(ioe.getMessage(), cf);
        } finally {
            dis.close();
        }
        return cf;
    }

    /**
	 * Gets the constant pool of this ClassFile.
	 * 
	 * @return ConstantPool
	 */
    public ConstantPool getConstantPool() {
        return constantPool;
    }

    /**
	 * Adds a new interface, that class represented by this ClassFile
	 * implements.
	 * 
	 * @param fully
	 *            qualified name of the interface to implement
	 */
    public void addInterface(String name) {
        String iType = name.replace('.', '/');
        short nameIndex = constantPool.addUtf8Info(iType).getEntryIndex();
        short cInfo = constantPool.addClassInfo(nameIndex);
        interfaces.add(new Interface(constantPool, cInfo));
    }

    /**
	 * Gets the magic number
	 * 
	 * @return magic number
	 */
    public int getMagicNumber() {
        return magic_number;
    }

    /**
	 * Gets the minor version of the class file
	 * 
	 * @return Minor version
	 */
    public short getMinorVersion() {
        return minor_version;
    }

    /**
	 * Gets the major version of the class file
	 * 
	 * @return Major version
	 */
    public short getMajorVersion() {
        return major_version;
    }

    /**
	 * Gets all the Fields of this class file
	 * 
	 * @return a List of Fields
	 */
    public List<Field> getFields() {
        return fields;
    }

    /**
	 * Gets all the Interfaces of this class file
	 * 
	 * @return a List of Interfaces
	 */
    public List<Interface> getInterfaces() {
        return interfaces;
    }

    /**
	 * Gets all the Methods of this class file
	 * 
	 * @return a List of Methods
	 */
    public List<Method> getMethods() {
        return methods;
    }

    /**
	 * Gets the name of the class represented by this ClassFile.
	 * 
	 * @return Name of the class
	 */
    public String getClassName() {
        return constantPool.getClassName(this_class);
    }

    /**
	 * Gets the name of the superclass of the class represented by this
	 * ClassFile.
	 * 
	 * @return Name of the superclass, or null if this ClassFile represents
	 *         java.lang.Object
	 */
    public String getSuperclassName() {
        if (super_class != 0) {
            return constantPool.getClassName(super_class);
        }
        return null;
    }

    public void setAccessFlags(short access_flags) {
        this.access_flags = access_flags;
    }

    /**
	 * Gets the index in constant-pool, that holds a class-info for super class
	 * of this class. This method returns 0, if this ClassFile represents
	 * java.lang.Object. Note, that indexing in constant-pool starts from 1.
	 * 
	 * @return index into constant pool
	 */
    public short getSuperClassIndex() {
        return super_class;
    }

    /**
	 * Gets the index in constant-pool, that holds a class-info of this class.
	 * 
	 * @return index into constant pool
	 */
    public short getThisClassIndex() {
        return this_class;
    }

    /**
	 * Converts this ClassFile into bytes. No checking against the class file
	 * format specification is made.
	 * 
	 * @return bytes representing a Java class.
	 */
    public byte[] toBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeInt(magic_number);
            dos.writeShort(minor_version);
            dos.writeShort(major_version);
            writeConstantPool(dos);
            dos.writeShort(access_flags);
            dos.writeShort(this_class);
            dos.writeShort(super_class);
            writeInterfaces(dos);
            writeFields(dos);
            writeMethods(dos);
            writeAttributes(dos);
        } catch (IOException ioe) {
            throw new ParseException(ioe.getMessage(), this);
        }
        return baos.toByteArray();
    }

    private void parseClassFile(DataInputStream dis) throws ParseException, IOException {
        this.magic_number = dis.readInt();
        this.minor_version = dis.readShort();
        this.major_version = dis.readShort();
        parseConstantPool(dis);
        this.access_flags = dis.readShort();
        this.this_class = dis.readShort();
        this.super_class = dis.readShort();
        readInterfaces(dis);
        readFields(dis);
        readMethods(dis);
        readAttributes(dis);
    }

    void addInterface(short constantClassIndex) {
        interfaces.add(new Interface(constantPool, constantClassIndex));
    }

    private void readInterfaces(DataInputStream dis) throws IOException {
        short iCount = dis.readShort();
        for (int i = 0; i < iCount; i++) {
            short constantClassIndex = dis.readShort();
            addInterface(constantClassIndex);
        }
    }

    private void readFields(DataInputStream dis) throws IOException {
        short fCount = dis.readShort();
        for (int i = 0; i < fCount; i++) {
            fields.add(new Field(constantPool, dis));
        }
    }

    private void readMethods(DataInputStream dis) throws IOException {
        short mCount = dis.readShort();
        for (int i = 0; i < mCount; i++) {
            methods.add(new Method(constantPool, dis));
        }
    }

    private void parseConstantPool(DataInputStream dis) throws IOException {
        short constantPoolCount = dis.readShort();
        constantPool = new ConstantPool(constantPoolCount - 1);
        for (int i = 0; i < constantPoolCount - 1; i++) {
            byte tag = dis.readByte();
            switch(tag) {
                case ConstantPool.CONSTANT_Class:
                    constantPool.addClassInfo(dis.readShort());
                    break;
                case ConstantPool.CONSTANT_Fieldref:
                    constantPool.addFieldRefInfo(dis.readShort(), dis.readShort());
                    break;
                case ConstantPool.CONSTANT_Methodref:
                    constantPool.addMethodRefInfo(dis.readShort(), dis.readShort());
                    break;
                case ConstantPool.CONSTANT_InterfaceMethodref:
                    constantPool.addInterfaceMethodRefInfo(dis.readShort(), dis.readShort());
                    break;
                case ConstantPool.CONSTANT_String:
                    constantPool.addString_info(dis.readShort());
                    break;
                case ConstantPool.CONSTANT_Integer:
                    constantPool.addInteger_info(dis.readInt());
                    break;
                case ConstantPool.CONSTANT_Float:
                    constantPool.addFloat_info(dis.readInt());
                    break;
                case ConstantPool.CONSTANT_Long:
                    constantPool.addLong_info(dis.readInt(), dis.readInt());
                    i++;
                    break;
                case ConstantPool.CONSTANT_Double:
                    constantPool.addDouble_info(dis.readInt(), dis.readInt());
                    i++;
                    break;
                case ConstantPool.CONSTANT_NameAndType:
                    constantPool.addNameAndTypeInfo(dis.readShort(), dis.readShort());
                    break;
                case ConstantPool.CONSTANT_Utf8:
                    short length = dis.readShort();
                    byte[] byteArray = new byte[length];
                    for (int j = 0; j < byteArray.length; j++) {
                        byteArray[j] = dis.readByte();
                        if (byteArray[j] == 0 || byteArray[j] >= 0xf0) {
                            System.out.println("  " + Integer.toHexString(byteArray[j]));
                        }
                    }
                    constantPool.addUtf8_info(byteArray);
                    break;
                default:
                    throw new ParseException(constantPool + "\nInvalid constant pool tag: " + tag, this);
            }
        }
    }

    private void writeConstantPool(DataOutputStream dos) throws IOException {
        List<Entry> cp = constantPool.getList();
        dos.writeShort(cp.size() + 1);
        Iterator<Entry> i = cp.iterator();
        while (i.hasNext()) {
            ConstantPool.Entry e = i.next();
            if (e instanceof ConstantPool.NullEntry) {
                continue;
            }
            if (e.getTag() == 0) {
                throw new JiapiRuntimeException("ERROR: invalid constant pool tag: 0");
            }
            dos.writeByte(e.getTag());
            e.writeData(dos);
        }
    }

    private void writeInterfaces(DataOutputStream dos) throws IOException {
        dos.writeShort(interfaces.size());
        Iterator<Interface> i = interfaces.iterator();
        while (i.hasNext()) {
            Interface iFace = i.next();
            dos.writeShort(iFace.getConstantClassIndex());
        }
    }

    private void writeFields(DataOutputStream dos) throws IOException {
        dos.writeShort(fields.size());
        Iterator<Field> i = fields.iterator();
        while (i.hasNext()) {
            Field f = i.next();
            dos.writeShort(f.getAccessFlags());
            dos.writeShort(f.getNameIndex());
            dos.writeShort(f.getDescriptorIndex());
            f.writeAttributes(dos);
        }
    }

    private void writeMethods(DataOutputStream dos) throws IOException {
        dos.writeShort(methods.size());
        Iterator<Method> i = methods.iterator();
        while (i.hasNext()) {
            Method m = i.next();
            dos.writeShort(m.getAccessFlags());
            dos.writeShort(m.getNameIndex());
            dos.writeShort(m.getDescriptorIndex());
            m.writeAttributes(dos);
        }
    }
}
