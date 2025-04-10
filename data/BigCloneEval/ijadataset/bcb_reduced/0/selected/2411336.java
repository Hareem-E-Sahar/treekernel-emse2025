package sun.rmi.rmic.newrmic.jrmp;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Type;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.security.MessageDigest;
import java.security.DigestOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import sun.rmi.rmic.newrmic.BatchEnvironment;
import static sun.rmi.rmic.newrmic.Constants.*;
import static sun.rmi.rmic.newrmic.jrmp.Constants.*;

/**
 * Encapsulates RMI-specific information about a remote implementation
 * class (a class that implements one or more remote interfaces).
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 *
 * @author Peter Jones
 **/
final class RemoteClass {

    /** rmic environment for this object */
    private final BatchEnvironment env;

    /** the remote implementation class this object represents */
    private final ClassDoc implClass;

    /** remote interfaces implemented by this class */
    private ClassDoc[] remoteInterfaces;

    /** the remote methods of this class */
    private Method[] remoteMethods;

    /** stub/skeleton "interface hash" for this class */
    private long interfaceHash;

    /**
     * Creates a RemoteClass instance that represents the RMI-specific
     * information about the specified remote implementation class.
     *
     * If the class is not a valid remote implementation class or if
     * some other error occurs, the return value will be null, and
     * errors will have been reported to the supplied
     * BatchEnvironment.
     **/
    static RemoteClass forClass(BatchEnvironment env, ClassDoc implClass) {
        RemoteClass remoteClass = new RemoteClass(env, implClass);
        if (remoteClass.init()) {
            return remoteClass;
        } else {
            return null;
        }
    }

    /**
     * Creates a RemoteClass instance for the specified class.  The
     * resulting object is not yet initialized.
     **/
    private RemoteClass(BatchEnvironment env, ClassDoc implClass) {
        this.env = env;
        this.implClass = implClass;
    }

    /**
     * Returns the ClassDoc for this remote implementation class.
     **/
    ClassDoc classDoc() {
        return implClass;
    }

    /**
     * Returns the remote interfaces implemented by this remote
     * implementation class.
     *
     * A remote interface is an interface that is a subinterface of
     * java.rmi.Remote.  The remote interfaces of a class are the
     * direct superinterfaces of the class and all of its superclasses
     * that are remote interfaces.
     *
     * The order of the array returned is arbitrary, and some elements
     * may be superfluous (i.e., superinterfaces of other interfaces
     * in the array).
     **/
    ClassDoc[] remoteInterfaces() {
        return remoteInterfaces.clone();
    }

    /**
     * Returns an array of RemoteClass.Method objects representing all
     * of the remote methods of this remote implementation class (all
     * of the member methods of the class's remote interfaces).
     *
     * The methods in the array are ordered according to a comparison
     * of strings consisting of their name followed by their
     * descriptor, so each method's index in the array corresponds to
     * its "operation number" in the JDK 1.1 version of the JRMP
     * stub/skeleton protocol.
     **/
    Method[] remoteMethods() {
        return remoteMethods.clone();
    }

    /**
     * Returns the "interface hash" used to match a stub/skeleton pair
     * for this remote implementation class in the JDK 1.1 version of
     * the JRMP stub/skeleton protocol.
     **/
    long interfaceHash() {
        return interfaceHash;
    }

    /**
     * Validates this remote implementation class and computes the
     * RMI-specific information.  Returns true if successful, or false
     * if an error occurred.
     **/
    private boolean init() {
        if (implClass.isInterface()) {
            env.error("rmic.cant.make.stubs.for.interface", implClass.qualifiedName());
            return false;
        }
        List<ClassDoc> remotesImplemented = new ArrayList<ClassDoc>();
        for (ClassDoc cl = implClass; cl != null; cl = cl.superclass()) {
            for (ClassDoc intf : cl.interfaces()) {
                if (!remotesImplemented.contains(intf) && intf.subclassOf(env.docRemote())) {
                    remotesImplemented.add(intf);
                    if (env.verbose()) {
                        env.output("[found remote interface: " + intf.qualifiedName() + "]");
                    }
                }
            }
            if (cl == implClass && remotesImplemented.isEmpty()) {
                if (implClass.subclassOf(env.docRemote())) {
                    env.error("rmic.must.implement.remote.directly", implClass.qualifiedName());
                } else {
                    env.error("rmic.must.implement.remote", implClass.qualifiedName());
                }
                return false;
            }
        }
        remoteInterfaces = remotesImplemented.toArray(new ClassDoc[remotesImplemented.size()]);
        Map<String, Method> methods = new HashMap<String, Method>();
        boolean errors = false;
        for (ClassDoc intf : remotesImplemented) {
            if (!collectRemoteMethods(intf, methods)) {
                errors = true;
            }
        }
        if (errors) {
            return false;
        }
        String[] orderedKeys = methods.keySet().toArray(new String[methods.size()]);
        Arrays.sort(orderedKeys);
        remoteMethods = new Method[methods.size()];
        for (int i = 0; i < remoteMethods.length; i++) {
            remoteMethods[i] = methods.get(orderedKeys[i]);
            if (env.verbose()) {
                String msg = "[found remote method <" + i + ">: " + remoteMethods[i].operationString();
                ClassDoc[] exceptions = remoteMethods[i].exceptionTypes();
                if (exceptions.length > 0) {
                    msg += " throws ";
                    for (int j = 0; j < exceptions.length; j++) {
                        if (j > 0) {
                            msg += ", ";
                        }
                        msg += exceptions[j].qualifiedName();
                    }
                }
                msg += "\n\tname and descriptor = \"" + remoteMethods[i].nameAndDescriptor();
                msg += "\n\tmethod hash = " + remoteMethods[i].methodHash() + "]";
                env.output(msg);
            }
        }
        interfaceHash = computeInterfaceHash();
        return true;
    }

    /**
     * Collects and validates all methods from the specified interface
     * and all of its superinterfaces as remote methods.  Remote
     * methods are added to the supplied table.  Returns true if
     * successful, or false if an error occurred.
     **/
    private boolean collectRemoteMethods(ClassDoc intf, Map<String, Method> table) {
        if (!intf.isInterface()) {
            throw new AssertionError(intf.qualifiedName() + " not an interface");
        }
        boolean errors = false;
        nextMethod: for (MethodDoc method : intf.methods()) {
            boolean hasRemoteException = false;
            for (ClassDoc ex : method.thrownExceptions()) {
                if (env.docRemoteException().subclassOf(ex)) {
                    hasRemoteException = true;
                    break;
                }
            }
            if (!hasRemoteException) {
                env.error("rmic.must.throw.remoteexception", intf.qualifiedName(), method.name() + method.signature());
                errors = true;
                continue nextMethod;
            }
            MethodDoc implMethod = findImplMethod(method);
            if (implMethod != null) {
                for (ClassDoc ex : implMethod.thrownExceptions()) {
                    if (!ex.subclassOf(env.docException())) {
                        env.error("rmic.must.only.throw.exception", implMethod.name() + implMethod.signature(), ex.qualifiedName());
                        errors = true;
                        continue nextMethod;
                    }
                }
            }
            Method newMethod = new Method(method);
            String key = newMethod.nameAndDescriptor();
            Method oldMethod = table.get(key);
            if (oldMethod != null) {
                newMethod = newMethod.mergeWith(oldMethod);
            }
            table.put(key, newMethod);
        }
        for (ClassDoc superintf : intf.interfaces()) {
            if (!collectRemoteMethods(superintf, table)) {
                errors = true;
            }
        }
        return !errors;
    }

    /**
     * Returns the MethodDoc for the method of this remote
     * implementation class that implements the specified remote
     * method of a remote interface.  Returns null if no matching
     * method was found in this remote implementation class.
     **/
    private MethodDoc findImplMethod(MethodDoc interfaceMethod) {
        String name = interfaceMethod.name();
        String desc = Util.methodDescriptorOf(interfaceMethod);
        for (MethodDoc implMethod : implClass.methods()) {
            if (name.equals(implMethod.name()) && desc.equals(Util.methodDescriptorOf(implMethod))) {
                return implMethod;
            }
        }
        return null;
    }

    /**
     * Computes the "interface hash" of the stub/skeleton pair for
     * this remote implementation class.  This is the 64-bit value
     * used to enforce compatibility between a stub class and a
     * skeleton class in the JDK 1.1 version of the JRMP stub/skeleton
     * protocol.
     *
     * It is calculated using the first 64 bits of an SHA digest.  The
     * digest is of a stream consisting of the following data:
     *     (int) stub version number, always 1
     *     for each remote method, in order of operation number:
     *         (UTF-8) method name
     *         (UTF-8) method descriptor
     *         for each declared exception, in alphabetical name order:
     *             (UTF-8) name of exception class
     * (where "UTF-8" includes a 16-bit length prefix as written by
     * java.io.DataOutput.writeUTF).
     **/
    private long computeInterfaceHash() {
        long hash = 0;
        ByteArrayOutputStream sink = new ByteArrayOutputStream(512);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA");
            DataOutputStream out = new DataOutputStream(new DigestOutputStream(sink, md));
            out.writeInt(INTERFACE_HASH_STUB_VERSION);
            for (Method method : remoteMethods) {
                MethodDoc methodDoc = method.methodDoc();
                out.writeUTF(methodDoc.name());
                out.writeUTF(Util.methodDescriptorOf(methodDoc));
                ClassDoc exceptions[] = methodDoc.thrownExceptions();
                Arrays.sort(exceptions, new ClassDocComparator());
                for (ClassDoc ex : exceptions) {
                    out.writeUTF(Util.binaryNameOf(ex));
                }
            }
            out.flush();
            byte hashArray[] = md.digest();
            for (int i = 0; i < Math.min(8, hashArray.length); i++) {
                hash += ((long) (hashArray[i] & 0xFF)) << (i * 8);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
        return hash;
    }

    /**
     * Compares ClassDoc instances according to the lexicographic
     * order of their binary names.
     **/
    private static class ClassDocComparator implements Comparator<ClassDoc> {

        public int compare(ClassDoc o1, ClassDoc o2) {
            return Util.binaryNameOf(o1).compareTo(Util.binaryNameOf(o2));
        }
    }

    /**
     * Encapsulates RMI-specific information about a particular remote
     * method in the remote implementation class represented by the
     * enclosing RemoteClass.
     **/
    final class Method implements Cloneable {

        /**
         * MethodDoc for this remove method, from one of the remote
         * interfaces that this method was found in.
         *
         * Note that this MethodDoc may be only one of multiple that
         * correspond to this remote method object, if multiple of
         * this class's remote interfaces contain methods with the
         * same name and descriptor.  Therefore, this MethodDoc may
         * declare more exceptions thrown that this remote method
         * does.
         **/
        private final MethodDoc methodDoc;

        /** java.rmi.server.Operation string for this remote method */
        private final String operationString;

        /** name and descriptor of this remote method */
        private final String nameAndDescriptor;

        /** JRMP "method hash" for this remote method */
        private final long methodHash;

        /**
         * Exceptions declared to be thrown by this remote method.
         *
         * This list may include superfluous entries, such as
         * unchecked exceptions and subclasses of other entries.
         **/
        private ClassDoc[] exceptionTypes;

        /**
         * Creates a new Method instance for the specified method.
         **/
        Method(MethodDoc methodDoc) {
            this.methodDoc = methodDoc;
            exceptionTypes = methodDoc.thrownExceptions();
            Arrays.sort(exceptionTypes, new ClassDocComparator());
            operationString = computeOperationString();
            nameAndDescriptor = methodDoc.name() + Util.methodDescriptorOf(methodDoc);
            methodHash = computeMethodHash();
        }

        /**
         * Returns the MethodDoc object corresponding to this method
         * of a remote interface.
         **/
        MethodDoc methodDoc() {
            return methodDoc;
        }

        /**
         * Returns the parameter types declared by this method.
         **/
        Type[] parameterTypes() {
            Parameter[] parameters = methodDoc.parameters();
            Type[] paramTypes = new Type[parameters.length];
            for (int i = 0; i < paramTypes.length; i++) {
                paramTypes[i] = parameters[i].type();
            }
            return paramTypes;
        }

        /**
         * Returns the exception types declared to be thrown by this
         * remote method.
         *
         * For methods with the same name and descriptor inherited
         * from multiple remote interfaces, the array will contain the
         * set of exceptions declared in all of the interfaces'
         * methods that can be legally thrown by all of them.
         **/
        ClassDoc[] exceptionTypes() {
            return exceptionTypes.clone();
        }

        /**
         * Returns the JRMP "method hash" used to identify this remote
         * method in the JDK 1.2 version of the stub protocol.
         **/
        long methodHash() {
            return methodHash;
        }

        /**
         * Returns the string representation of this method
         * appropriate for the construction of a
         * java.rmi.server.Operation object.
         **/
        String operationString() {
            return operationString;
        }

        /**
         * Returns a string consisting of this method's name followed
         * by its descriptor.
         **/
        String nameAndDescriptor() {
            return nameAndDescriptor;
        }

        /**
         * Returns a new Method object that is a legal combination of
         * this Method object and another one.
         *
         * Doing this requires determining the exceptions declared by
         * the combined method, which must be (only) all of the
         * exceptions declared in both old Methods that may thrown in
         * either of them.
         **/
        Method mergeWith(Method other) {
            if (!nameAndDescriptor().equals(other.nameAndDescriptor())) {
                throw new AssertionError("attempt to merge method \"" + other.nameAndDescriptor() + "\" with \"" + nameAndDescriptor());
            }
            List<ClassDoc> legalExceptions = new ArrayList<ClassDoc>();
            collectCompatibleExceptions(other.exceptionTypes, exceptionTypes, legalExceptions);
            collectCompatibleExceptions(exceptionTypes, other.exceptionTypes, legalExceptions);
            Method merged = clone();
            merged.exceptionTypes = legalExceptions.toArray(new ClassDoc[legalExceptions.size()]);
            return merged;
        }

        /**
         * Cloning is supported by returning a shallow copy of this
         * object.
         **/
        protected Method clone() {
            try {
                return (Method) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e);
            }
        }

        /**
         * Adds to the supplied list all exceptions in the "froms"
         * array that are subclasses of an exception in the "withs"
         * array.
         **/
        private void collectCompatibleExceptions(ClassDoc[] froms, ClassDoc[] withs, List<ClassDoc> list) {
            for (ClassDoc from : froms) {
                if (!list.contains(from)) {
                    for (ClassDoc with : withs) {
                        if (from.subclassOf(with)) {
                            list.add(from);
                            break;
                        }
                    }
                }
            }
        }

        /**
         * Computes the JRMP "method hash" of this remote method.  The
         * method hash is a long containing the first 64 bits of the
         * SHA digest from the UTF-8 encoded string of the method name
         * and descriptor.
         **/
        private long computeMethodHash() {
            long hash = 0;
            ByteArrayOutputStream sink = new ByteArrayOutputStream(512);
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                DataOutputStream out = new DataOutputStream(new DigestOutputStream(sink, md));
                String methodString = nameAndDescriptor();
                out.writeUTF(methodString);
                out.flush();
                byte hashArray[] = md.digest();
                for (int i = 0; i < Math.min(8, hashArray.length); i++) {
                    hash += ((long) (hashArray[i] & 0xFF)) << (i * 8);
                }
            } catch (IOException e) {
                throw new AssertionError(e);
            } catch (NoSuchAlgorithmException e) {
                throw new AssertionError(e);
            }
            return hash;
        }

        /**
         * Computes the string representation of this method
         * appropriate for the construction of a
         * java.rmi.server.Operation object.
         **/
        private String computeOperationString() {
            Type returnType = methodDoc.returnType();
            String op = returnType.qualifiedTypeName() + " " + methodDoc.name() + "(";
            Parameter[] parameters = methodDoc.parameters();
            for (int i = 0; i < parameters.length; i++) {
                if (i > 0) {
                    op += ", ";
                }
                op += parameters[i].type().toString();
            }
            op += ")" + returnType.dimension();
            return op;
        }
    }
}
