package jode.obfuscator;

import jode.GlobalOptions;
import jode.bytecode.*;
import jode.util.Comparator;
import jode.util.Collection;
import jode.util.Collections;
import jode.util.ArrayList;
import jode.util.Arrays;
import jode.util.Iterator;
import jode.util.List;
import jode.util.LinkedList;
import jode.util.Map;
import jode.util.UnsupportedOperationException;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

public class ClassIdentifier extends Identifier {

    PackageIdentifier pack;

    String name;

    ClassInfo info;

    String superName;

    String[] ifaceNames;

    List fieldIdents, methodIdents;

    List knownSubClasses = new LinkedList();

    List virtualReachables = new LinkedList();

    public ClassIdentifier(PackageIdentifier pack, String name, ClassInfo info) {
        super(name);
        this.pack = pack;
        this.name = name;
        this.info = info;
    }

    public void addSubClass(ClassIdentifier ci) {
        knownSubClasses.add(ci);
        for (Iterator i = virtualReachables.iterator(); i.hasNext(); ) {
            String[] method = (String[]) i.next();
            ci.reachableIdentifier(method[0], method[1], true);
        }
    }

    public void preserveMatchingIdentifier(WildCard wildcard) {
        String fullName = getFullName() + ".";
        for (Iterator i = getChilds(); i.hasNext(); ) {
            Identifier ident = (Identifier) i.next();
            System.err.println("checking " + ident);
            if (wildcard.matches(fullName + ident.getName()) || wildcard.matches(fullName + ident.getName() + "." + ident.getType())) {
                if (GlobalOptions.verboseLevel > 0) GlobalOptions.err.println("Preserving " + ident);
                setPreserved();
                ident.setPreserved();
                ident.setReachable();
            }
        }
    }

    private FieldIdentifier findField(String name, String typeSig) {
        for (Iterator i = fieldIdents.iterator(); i.hasNext(); ) {
            FieldIdentifier ident = (FieldIdentifier) i.next();
            if (ident.getName().equals(name) && ident.getType().equals(typeSig)) return ident;
        }
        return null;
    }

    private MethodIdentifier findMethod(String name, String typeSig) {
        for (Iterator i = methodIdents.iterator(); i.hasNext(); ) {
            MethodIdentifier ident = (MethodIdentifier) i.next();
            if (ident.getName().equals(name) && ident.getType().equals(typeSig)) return ident;
        }
        return null;
    }

    public void reachableIdentifier(String name, String typeSig, boolean isVirtual) {
        boolean found = false;
        for (Iterator i = getChilds(); i.hasNext(); ) {
            Identifier ident = (Identifier) i.next();
            if (name.equals(ident.getName()) && typeSig.equals(ident.getType())) {
                ident.setReachable();
                found = true;
            }
        }
        if (!found) {
            ClassIdentifier superIdent = Main.getClassBundle().getClassIdentifier(info.getSuperclass().getName());
            if (superIdent != null) superIdent.reachableIdentifier(name, typeSig, false);
        }
        if (isVirtual) {
            for (Iterator i = knownSubClasses.iterator(); i.hasNext(); ) ((ClassIdentifier) i.next()).reachableIdentifier(name, typeSig, false);
            virtualReachables.add(new String[] { name, typeSig });
        }
    }

    public void chainMethodIdentifier(Identifier chainIdent) {
        String name = chainIdent.getName();
        String typeSig = chainIdent.getType();
        for (Iterator i = methodIdents.iterator(); i.hasNext(); ) {
            Identifier ident = (Identifier) i.next();
            if (ident.getName().equals(name) && ident.getType().equals(typeSig)) chainIdent.addShadow(ident);
        }
    }

    /**
     * This is partly taken from the classpath project.
     */
    public long calcSerialVersionUID() {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            GlobalOptions.err.println("Can't calculate serialVersionUID");
            return 0L;
        }
        OutputStream digest = new OutputStream() {

            public void write(int b) {
                md.update((byte) b);
            }

            public void write(byte[] data, int offset, int length) {
                md.update(data, offset, length);
            }
        };
        DataOutputStream out = new DataOutputStream(digest);
        try {
            out.writeUTF(info.getName());
            int modifiers = info.getModifiers();
            modifiers = modifiers & (Modifier.ABSTRACT | Modifier.FINAL | Modifier.INTERFACE | Modifier.PUBLIC);
            out.writeInt(modifiers);
            ClassInfo[] interfaces = (ClassInfo[]) info.getInterfaces().clone();
            Arrays.sort(interfaces, new Comparator() {

                public int compare(Object o1, Object o2) {
                    return ((ClassInfo) o1).getName().compareTo(((ClassInfo) o2).getName());
                }
            });
            for (int i = 0; i < interfaces.length; i++) {
                out.writeUTF(interfaces[i].getName());
            }
            Comparator identCmp = new Comparator() {

                public int compare(Object o1, Object o2) {
                    Identifier i1 = (Identifier) o1;
                    Identifier i2 = (Identifier) o2;
                    boolean special1 = (i1.equals("<init>") || i1.equals("<clinit>"));
                    boolean special2 = (i2.equals("<init>") || i2.equals("<clinit>"));
                    if (special1 != special2) {
                        return special1 ? -1 : 1;
                    }
                    int comp = i1.getName().compareTo(i2.getName());
                    if (comp != 0) {
                        return comp;
                    } else {
                        return i1.getType().compareTo(i2.getType());
                    }
                }
            };
            List fields = Arrays.asList(fieldIdents.toArray());
            List methods = Arrays.asList(methodIdents.toArray());
            Collections.sort(fields, identCmp);
            Collections.sort(methods, identCmp);
            for (Iterator i = fields.iterator(); i.hasNext(); ) {
                FieldIdentifier field = (FieldIdentifier) i.next();
                modifiers = field.info.getModifiers();
                if ((modifiers & Modifier.PRIVATE) != 0 && (modifiers & (Modifier.STATIC | Modifier.TRANSIENT)) != 0) continue;
                out.writeUTF(field.getName());
                out.writeInt(modifiers);
                out.writeUTF(field.getType());
            }
            for (Iterator i = methods.iterator(); i.hasNext(); ) {
                MethodIdentifier method = (MethodIdentifier) i.next();
                modifiers = method.info.getModifiers();
                if (Modifier.isPrivate(modifiers)) continue;
                out.writeUTF(method.getName());
                out.writeInt(modifiers);
                out.writeUTF(method.getType().replace('/', '.'));
            }
            out.close();
            byte[] sha = md.digest();
            long result = 0;
            for (int i = 0; i < 8; i++) {
                result += (long) (sha[i] & 0xFF) << (8 * i);
            }
            return result;
        } catch (IOException ex) {
            ex.printStackTrace();
            GlobalOptions.err.println("Can't calculate serialVersionUID");
            return 0L;
        }
    }

    /**
     * Preserve all fields, that are necessary, to serialize
     * a compatible class.
     */
    public void preserveSerializable() {
        Identifier method = findMethod("writeObject", "(Ljava.io.ObjectOutputStream)V");
        if (method != null) method.setPreserved();
        method = findMethod("readObject", "(Ljava.io.ObjectInputStream)V");
        if (method != null) method.setPreserved();
        if ((Main.options & Main.OPTION_PRESERVESERIAL) != 0) {
            setPreserved();
            Identifier UIDident = findField("serialVersionUID", "J");
            if (UIDident == null) {
                long serialVersion = calcSerialVersionUID();
                FieldInfo UIDField = new FieldInfo(info, "serialVersionUID", "J", Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
                UIDField.setConstant(new Long(serialVersion));
                UIDident = new FieldIdentifier(this, UIDField);
                fieldIdents.add(UIDident);
            }
            UIDident.setReachable();
            UIDident.setPreserved();
            for (Iterator i = getFieldIdents().iterator(); i.hasNext(); ) {
                FieldIdentifier ident = (FieldIdentifier) i.next();
                if ((ident.info.getModifiers() & (Modifier.TRANSIENT | Modifier.STATIC)) == 0) {
                    ident.setPreserved();
                    ident.setNotConstant();
                }
            }
        }
    }

    /**
     * Marks the package as preserved, too.
     */
    protected void setSinglePreserved() {
        pack.setPreserved();
    }

    public void setSingleReachable() {
        super.setSingleReachable();
        Main.getClassBundle().analyzeIdentifier(this);
    }

    public void analyzeSuperClasses(ClassInfo superclass) {
        while (superclass != null) {
            if (superclass.getName().equals("java.io.Serializable")) preserveSerializable();
            ClassIdentifier superident = Main.getClassBundle().getClassIdentifier(superclass.getName());
            if (superident != null) {
                superident.addSubClass(this);
            } else {
                MethodInfo[] topmethods = superclass.getMethods();
                for (int i = 0; i < topmethods.length; i++) {
                    int modif = topmethods[i].getModifiers();
                    if (((Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL) & modif) == 0 && !topmethods[i].getName().equals("<init>")) {
                        reachableIdentifier(topmethods[i].getName(), topmethods[i].getType(), true);
                    }
                }
            }
            ClassInfo[] ifaces = superclass.getInterfaces();
            for (int i = 0; i < ifaces.length; i++) analyzeSuperClasses(ifaces[i]);
            superclass = superclass.getSuperclass();
        }
    }

    public void analyze() {
        if (GlobalOptions.verboseLevel > 0) GlobalOptions.err.println("Reachable: " + this);
        ClassInfo[] ifaces = info.getInterfaces();
        for (int i = 0; i < ifaces.length; i++) analyzeSuperClasses(ifaces[i]);
        analyzeSuperClasses(info.getSuperclass());
    }

    public void initSuperClasses(ClassInfo superclass) {
        while (superclass != null) {
            if (superclass.getName().equals("java.lang.Serializable")) preserveSerializable();
            ClassIdentifier superident = Main.getClassBundle().getClassIdentifier(superclass.getName());
            if (superident != null) {
                for (Iterator i = superident.getMethodIdents().iterator(); i.hasNext(); ) {
                    MethodIdentifier mid = (MethodIdentifier) i.next();
                    int modif = mid.info.getModifiers();
                    if (((Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL) & modif) == 0 && !(mid.getName().equals("<init>"))) {
                        chainMethodIdentifier(mid);
                    }
                }
            } else {
                MethodInfo[] topmethods = superclass.getMethods();
                for (int i = 0; i < topmethods.length; i++) {
                    int modif = topmethods[i].getModifiers();
                    if (((Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL) & modif) == 0 && !topmethods[i].getName().equals("<init>")) {
                        Identifier method = findMethod(topmethods[i].getName(), topmethods[i].getType());
                        if (method != null) method.setPreserved();
                    }
                }
            }
            ClassInfo[] ifaces = superclass.getInterfaces();
            for (int i = 0; i < ifaces.length; i++) initSuperClasses(ifaces[i]);
            superclass = superclass.getSuperclass();
        }
    }

    public void initClass() {
        info.loadInfo(info.FULLINFO);
        FieldInfo[] finfos = info.getFields();
        MethodInfo[] minfos = info.getMethods();
        if (Main.swapOrder) {
            Random rand = new Random();
            Collections.shuffle(Arrays.asList(finfos), rand);
            Collections.shuffle(Arrays.asList(minfos), rand);
        }
        fieldIdents = new ArrayList(finfos.length);
        methodIdents = new ArrayList(minfos.length);
        for (int i = 0; i < finfos.length; i++) fieldIdents.add(new FieldIdentifier(this, finfos[i]));
        for (int i = 0; i < minfos.length; i++) {
            MethodIdentifier ident = new MethodIdentifier(this, minfos[i]);
            methodIdents.add(ident);
            if (ident.getName().equals("<clinit>")) {
                ident.setPreserved();
                ident.setReachable();
            } else if (ident.getName().equals("<init>")) ident.setPreserved();
        }
        ClassInfo[] ifaces = info.getInterfaces();
        ifaceNames = new String[ifaces.length];
        for (int i = 0; i < ifaces.length; i++) {
            ifaceNames[i] = ifaces[i].getName();
            ClassIdentifier ifaceident = Main.getClassBundle().getClassIdentifier(ifaceNames[i]);
            initSuperClasses(ifaces[i]);
        }
        if (info.getSuperclass() != null) {
            superName = info.getSuperclass().getName();
            ClassIdentifier superident = Main.getClassBundle().getClassIdentifier(superName);
            initSuperClasses(info.getSuperclass());
        }
        if ((Main.stripping & Main.STRIP_SOURCE) != 0) {
            info.setSourceFile(null);
        }
        if ((Main.stripping & Main.STRIP_INNERINFO) != 0) {
            info.setInnerClasses(new InnerClassInfo[0]);
            info.setOuterClasses(new InnerClassInfo[0]);
            info.setExtraClasses(new InnerClassInfo[0]);
        }
        InnerClassInfo[] innerClasses = info.getInnerClasses();
        InnerClassInfo[] outerClasses = info.getOuterClasses();
        InnerClassInfo[] extraClasses = info.getExtraClasses();
        if (outerClasses != null) {
            for (int i = 0; i < outerClasses.length; i++) {
                if (outerClasses[i].outer != null) {
                    Main.getClassBundle().getClassIdentifier(outerClasses[i].outer);
                }
            }
        }
        if (innerClasses != null) {
            for (int i = 0; i < innerClasses.length; i++) {
                Main.getClassBundle().getClassIdentifier(innerClasses[i].inner);
            }
        }
        if (extraClasses != null) {
            for (int i = 0; i < extraClasses.length; i++) {
                Main.getClassBundle().getClassIdentifier(extraClasses[i].inner);
                if (extraClasses[i].outer != null) Main.getClassBundle().getClassIdentifier(extraClasses[i].outer);
            }
        }
    }

    /**
     * Add the ClassInfo objects of the interfaces of ancestor.  But if
     * an interface of ancestor is not reachable it will add its interfaces
     * instead.
     * @param result The Collection where the interfaces should be added to.
     * @param ancestor The ancestor whose interfaces should be added.
     */
    public void addIfaces(Collection result, ClassIdentifier ancestor) {
        String[] ifaces = ancestor.ifaceNames;
        ClassInfo[] ifaceInfos = ancestor.info.getInterfaces();
        for (int i = 0; i < ifaces.length; i++) {
            ClassIdentifier ifaceident = Main.getClassBundle().getClassIdentifier(ifaces[i]);
            if (ifaceident != null && !ifaceident.isReachable()) addIfaces(result, ifaceident); else result.add(ifaceInfos[i]);
        }
    }

    /**
     * Generates the new super class and interfaces, removing super
     * classes and interfaces that are not reachable.
     * @return an array of class names (full qualified, dot separated)
     * where the first entry is the super class (may be null) and the
     * other entries are the interfaces.
     */
    public void transformSuperIfaces() {
        if ((Main.stripping & Main.STRIP_UNREACH) == 0) return;
        Collection newIfaces = new LinkedList();
        ClassIdentifier ancestor = this;
        while (true) {
            addIfaces(newIfaces, ancestor);
            ClassIdentifier superident = Main.getClassBundle().getClassIdentifier(ancestor.superName);
            if (superident == null || superident.isReachable()) break;
            ancestor = superident;
        }
        ClassInfo superInfo = ancestor.info.getSuperclass();
        ClassInfo[] ifaces = (ClassInfo[]) newIfaces.toArray(new ClassInfo[newIfaces.size()]);
        info.setSuperclass(superInfo);
        info.setInterfaces(ifaces);
    }

    public void transformInnerClasses() {
        InnerClassInfo[] outerClasses = info.getOuterClasses();
        if (outerClasses != null) {
            int newOuterCount = outerClasses.length;
            if ((Main.stripping & Main.STRIP_UNREACH) != 0) {
                for (int i = 0; i < outerClasses.length; i++) {
                    if (outerClasses[i].outer != null) {
                        ClassIdentifier outerIdent = Main.getClassBundle().getClassIdentifier(outerClasses[i].outer);
                        if (outerIdent != null && !outerIdent.isReachable()) newOuterCount--;
                    }
                }
            }
            if (newOuterCount == 0) {
                info.setOuterClasses(null);
            } else {
                InnerClassInfo[] newOuters = new InnerClassInfo[newOuterCount];
                int pos = 0;
                String lastClass = getFullAlias();
                for (int i = 0; i < outerClasses.length; i++) {
                    ClassIdentifier outerIdent = outerClasses[i].outer != null ? (Main.getClassBundle().getClassIdentifier(outerClasses[i].outer)) : null;
                    if (outerIdent != null && !outerIdent.isReachable()) continue;
                    String inner = lastClass;
                    String outer = outerIdent == null ? outerClasses[i].outer : outerIdent.getFullAlias();
                    String name = outerClasses[i].name == null ? null : ((outer != null && inner.startsWith(outer + "$")) ? inner.substring(outer.length() + 1) : inner.substring(inner.lastIndexOf('.') + 1));
                    newOuters[pos++] = new InnerClassInfo(inner, outer, name, outerClasses[i].modifiers);
                    lastClass = outer;
                }
                info.setOuterClasses(newOuters);
            }
        }
        InnerClassInfo[] innerClasses = info.getInnerClasses();
        if (innerClasses != null) {
            int newInnerCount = innerClasses.length;
            if ((Main.stripping & Main.STRIP_UNREACH) != 0) {
                for (int i = 0; i < innerClasses.length; i++) {
                    ClassIdentifier innerIdent = Main.getClassBundle().getClassIdentifier(innerClasses[i].inner);
                    if (innerIdent != null && !innerIdent.isReachable()) newInnerCount--;
                }
            }
            if (newInnerCount == 0) {
                info.setInnerClasses(null);
            } else {
                InnerClassInfo[] newInners = new InnerClassInfo[newInnerCount];
                int pos = 0;
                for (int i = 0; i < innerClasses.length; i++) {
                    ClassIdentifier innerIdent = Main.getClassBundle().getClassIdentifier(innerClasses[i].inner);
                    if (innerIdent != null && (Main.stripping & Main.STRIP_UNREACH) != 0 && !innerIdent.isReachable()) continue;
                    String inner = innerIdent == null ? innerClasses[i].inner : innerIdent.getFullAlias();
                    String outer = getFullAlias();
                    String name = innerClasses[i].name == null ? null : ((outer != null && inner.startsWith(outer + "$")) ? inner.substring(outer.length() + 1) : inner.substring(inner.lastIndexOf('.') + 1));
                    newInners[pos++] = new InnerClassInfo(inner, outer, name, innerClasses[i].modifiers);
                }
                info.setInnerClasses(newInners);
            }
        }
        InnerClassInfo[] extraClasses = info.getExtraClasses();
        if (extraClasses != null) {
            int newExtraCount = extraClasses.length;
            if ((Main.stripping & Main.STRIP_UNREACH) != 0) {
                for (int i = 0; i < extraClasses.length; i++) {
                    ClassIdentifier outerIdent = extraClasses[i].outer != null ? (Main.getClassBundle().getClassIdentifier(extraClasses[i].outer)) : null;
                    ClassIdentifier innerIdent = Main.getClassBundle().getClassIdentifier(extraClasses[i].inner);
                    if ((outerIdent != null && !outerIdent.isReachable()) || (innerIdent != null && !innerIdent.isReachable())) newExtraCount--;
                }
            }
            if (newExtraCount == 0) {
                info.setExtraClasses(null);
            } else {
                InnerClassInfo[] newExtras = newExtraCount > 0 ? new InnerClassInfo[newExtraCount] : null;
                int pos = 0;
                for (int i = 0; i < extraClasses.length; i++) {
                    ClassIdentifier outerIdent = extraClasses[i].outer != null ? (Main.getClassBundle().getClassIdentifier(extraClasses[i].outer)) : null;
                    ClassIdentifier innerIdent = Main.getClassBundle().getClassIdentifier(extraClasses[i].inner);
                    if (innerIdent != null && !innerIdent.isReachable()) continue;
                    if (outerIdent != null && !outerIdent.isReachable()) continue;
                    String inner = innerIdent == null ? extraClasses[i].inner : innerIdent.getFullAlias();
                    String outer = outerIdent == null ? extraClasses[i].outer : outerIdent.getFullAlias();
                    String name = extraClasses[i].name == null ? null : ((outer != null && inner.startsWith(outer + "$")) ? inner.substring(outer.length() + 1) : inner.substring(inner.lastIndexOf('.') + 1));
                    newExtras[pos++] = new InnerClassInfo(inner, outer, name, extraClasses[i].modifiers);
                }
                info.setExtraClasses(newExtras);
            }
        }
    }

    public void doTransformations() {
        if (GlobalOptions.verboseLevel > 0) GlobalOptions.err.println("Transforming " + this);
        info.setName(getFullAlias());
        transformSuperIfaces();
        transformInnerClasses();
        Collection newFields = new ArrayList(fieldIdents.size());
        Collection newMethods = new ArrayList(methodIdents.size());
        for (Iterator i = fieldIdents.iterator(); i.hasNext(); ) {
            FieldIdentifier ident = (FieldIdentifier) i.next();
            if ((Main.stripping & Main.STRIP_UNREACH) == 0 || ident.isReachable()) {
                ident.doTransformations();
                newFields.add(ident.info);
            }
        }
        for (Iterator i = methodIdents.iterator(); i.hasNext(); ) {
            MethodIdentifier ident = (MethodIdentifier) i.next();
            if ((Main.stripping & Main.STRIP_UNREACH) == 0 || ident.isReachable()) {
                ident.doTransformations();
                newMethods.add(ident.info);
            }
        }
        info.setFields((FieldInfo[]) newFields.toArray(new FieldInfo[newFields.size()]));
        info.setMethods((MethodInfo[]) newMethods.toArray(new MethodInfo[newMethods.size()]));
    }

    public void storeClass(DataOutputStream out) throws IOException {
        if (GlobalOptions.verboseLevel > 0) GlobalOptions.err.println("Writing " + this);
        info.write(out);
        info = null;
        fieldIdents = methodIdents = null;
    }

    public Identifier getParent() {
        return pack;
    }

    /**
     * @return the full qualified name, excluding trailing dot.
     */
    public String getFullName() {
        if (pack.parent == null) return getName(); else return pack.getFullName() + "." + getName();
    }

    /**
     * @return the full qualified alias, excluding trailing dot.
     */
    public String getFullAlias() {
        if (pack.parent == null) return getAlias(); else return pack.getFullAlias() + "." + getAlias();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return "Ljava/lang/Class;";
    }

    public List getFieldIdents() {
        return fieldIdents;
    }

    public List getMethodIdents() {
        return methodIdents;
    }

    public Iterator getChilds() {
        final Iterator fieldIter = fieldIdents.iterator();
        final Iterator methodIter = methodIdents.iterator();
        return new Iterator() {

            boolean fieldsNext = fieldIter.hasNext();

            public boolean hasNext() {
                return fieldsNext ? true : methodIter.hasNext();
            }

            public Object next() {
                if (fieldsNext) {
                    Object result = fieldIter.next();
                    fieldsNext = fieldIter.hasNext();
                    return result;
                }
                return methodIter.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public String toString() {
        return "ClassIdentifier " + getFullName();
    }

    public Identifier getIdentifier(String fieldName, String typeSig) {
        for (Iterator i = getChilds(); i.hasNext(); ) {
            Identifier ident = (Identifier) i.next();
            if (ident.getName().equals(fieldName) && ident.getType().startsWith(typeSig)) return ident;
        }
        if (superName != null) {
            ClassIdentifier superident = Main.getClassBundle().getClassIdentifier(superName);
            if (superident != null) {
                Identifier ident = superident.getIdentifier(fieldName, typeSig);
                if (ident != null) return ident;
            }
        }
        return null;
    }

    public boolean containsFieldAliasDirectly(String fieldName, String typeSig, ModifierMatcher matcher) {
        for (Iterator i = fieldIdents.iterator(); i.hasNext(); ) {
            Identifier ident = (Identifier) i.next();
            if (((Main.stripping & Main.STRIP_UNREACH) == 0 || ident.isReachable()) && ident.wasAliased() && ident.getAlias().equals(fieldName) && ident.getType().startsWith(typeSig) && matcher.matches(ident)) return true;
        }
        return false;
    }

    public boolean containsMethodAliasDirectly(String methodName, String paramType, ModifierMatcher matcher) {
        for (Iterator i = methodIdents.iterator(); i.hasNext(); ) {
            Identifier ident = (Identifier) i.next();
            if (((Main.stripping & Main.STRIP_UNREACH) == 0 || ident.isReachable()) && ident.wasAliased() && ident.getAlias().equals(methodName) && ident.getType().startsWith(paramType) && matcher.matches(ident)) return true;
        }
        return false;
    }

    public boolean fieldConflicts(FieldIdentifier field, String newAlias) {
        String typeSig = (Main.options & Main.OPTION_STRONGOVERLOAD) != 0 ? field.getType() : "";
        ModifierMatcher mm = ModifierMatcher.allowAll;
        if (containsFieldAliasDirectly(newAlias, typeSig, mm)) return true;
        return false;
    }

    public boolean methodConflicts(MethodIdentifier method, String newAlias) {
        String paramType = method.getType();
        if ((Main.options & Main.OPTION_STRONGOVERLOAD) == 0) paramType = paramType.substring(0, paramType.indexOf(')') + 1);
        ModifierMatcher matcher = ModifierMatcher.allowAll;
        if (containsMethodAliasDirectly(newAlias, paramType, matcher)) return true;
        ModifierMatcher packMatcher = matcher.forceAccess(0, true);
        if (method.info.isStatic()) {
            packMatcher.forbidModifier(Modifier.STATIC);
        }
        ClassInfo superInfo = info.getSuperclass();
        ClassIdentifier superIdent = this;
        while (superInfo != null) {
            ClassIdentifier superident = Main.getClassBundle().getClassIdentifier(superInfo.getName());
            if (superident != null) {
                if (superident.containsMethodAliasDirectly(newAlias, paramType, packMatcher)) return true;
            } else {
                MethodInfo[] minfos = superInfo.getMethods();
                for (int i = 0; i < minfos.length; i++) {
                    if (minfos[i].getName().equals(newAlias) && minfos[i].getType().startsWith(paramType) && packMatcher.matches(minfos[i].getModifiers())) return true;
                }
            }
            superInfo = superInfo.getSuperclass();
        }
        if (packMatcher.matches(method)) {
            for (Iterator i = knownSubClasses.iterator(); i.hasNext(); ) {
                ClassIdentifier ci = (ClassIdentifier) i.next();
                if (ci.containsMethodAliasDirectly(newAlias, paramType, packMatcher)) return true;
            }
        }
        return false;
    }

    public boolean conflicting(String newAlias) {
        return pack.contains(newAlias, this);
    }
}
