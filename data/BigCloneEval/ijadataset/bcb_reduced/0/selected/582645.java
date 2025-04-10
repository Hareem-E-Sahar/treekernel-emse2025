package org.actorsguildframework.internal.codegenerator;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.actorsguildframework.Actor;
import org.actorsguildframework.ActorException;
import org.actorsguildframework.ActorRuntimeException;
import org.actorsguildframework.AsyncResult;
import org.actorsguildframework.ConfigurationException;
import org.actorsguildframework.annotations.ConcurrencyModel;
import org.actorsguildframework.annotations.Shared;
import org.actorsguildframework.immutable.ImmutableHelper;
import org.actorsguildframework.immutable.SerializableFreezer;
import org.actorsguildframework.internal.ActorClassDescriptor;
import org.actorsguildframework.internal.ActorProxy;
import org.actorsguildframework.internal.BeanClassDescriptor;
import org.actorsguildframework.internal.BeanFactory;
import org.actorsguildframework.internal.MessageCaller;
import org.actorsguildframework.internal.MessageImplDescriptor;
import org.actorsguildframework.internal.MultiThreadedActorState;
import org.actorsguildframework.internal.SingleThreadedActorState;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * ProxyCreator is a singleton that creates proxy classes for all Actor classes of the agent.
 * It works system-wide, for all Controllers. 
 * 
 * For Actor classes themselves, there is one factory class (extends {@link BeanFactory}y)
 * and the actual proxy class extending {@link ActorProxy}. 
 * 
 * Additionally, there are {@link MessageCaller} classes for each message of the Actor.
 */
public final class ActorProxyCreator {

    /**
	 * The only instance of ProxyCreator.
	 */
    private static final ActorProxyCreator instance = new ActorProxyCreator();

    /**
	 * The version of the generated Java classes. 1.5 or 1.6, depending on the
	 * Java version. 
	 */
    private static final int codeVersion = System.getProperty("java.version").startsWith("1.5") ? Opcodes.V1_5 : Opcodes.V1_6;

    /**
	 * Private constructor.
	 */
    private ActorProxyCreator() {
    }

    /**
	 * Returns the ProxyCreator.
	 * @return the ProxyCreator
	 */
    public static ActorProxyCreator getInstance() {
        return instance;
    }

    /**
	 * Creates a new factory.
	 * @param actorClass
	 * @return the factory
	 * @throws ConfigurationException if the agent is not configured correctly
	 */
    @SuppressWarnings("unchecked")
    public BeanFactory createFactory(Class<?> actorClass) {
        ActorClassDescriptor acd = ActorClassDescriptor.create((Class<? extends Actor>) actorClass);
        try {
            generateProxyClass(actorClass, acd);
            return generateFactoryClass(actorClass, acd);
        } catch (NoSuchMethodException e) {
            throw new ActorException("Unexpected error while creating proxy", e);
        }
    }

    /**
	 * Returns a number for the method that's unique across all methods of the same
	 * class with the same name.
	 * @param method the method
	 * @return the method number
	 */
    private static int getMethodNumber(Method method) {
        ArrayList<Method> methods = new ArrayList<Method>();
        for (Method m : method.getDeclaringClass().getMethods()) if (m.getName().equals(method.getName())) methods.add(m);
        Collections.sort(methods, new Comparator<Method>() {

            public int compare(Method o1, Method o2) {
                Class<?>[] args1 = o1.getParameterTypes();
                Class<?>[] args2 = o2.getParameterTypes();
                if (args1.length != args2.length) return args1.length - args2.length;
                for (int i = 0; i < args1.length; i++) if (!args1[i].equals(args2[i])) return args1[i].getName().compareTo(args2[i].getName());
                return 0;
            }
        });
        return methods.indexOf(method);
    }

    /**
	 * Create or get a MessageCaller implementation for the given method.
	 * @param ownerClass the class that owns the message
	 * @param method the method to invoke
	 * @return the message caller
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 */
    @SuppressWarnings("unchecked")
    public static Class<MessageCaller<?>> createMessageCaller(Class<?> ownerClass, Method method) throws SecurityException, NoSuchMethodException {
        String className = String.format("%s_%s_%d__MESSAGECALLER", ownerClass.getName(), method.getName(), getMethodNumber(method));
        String classNameInternal = className.replace('.', '/');
        java.lang.reflect.Type fullReturnType = method.getGenericReturnType();
        if ((!(fullReturnType instanceof ParameterizedType)) && AsyncResult.class.isAssignableFrom(((Class) ((ParameterizedType) fullReturnType).getRawType()))) throw new RuntimeException("Something's wrong here: should not be called for such a method");
        String returnSignature = GenericTypeHelper.getSignature(((ParameterizedType) fullReturnType).getActualTypeArguments()[0]);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        MethodVisitor mv;
        cw.visit(codeVersion, Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER + Opcodes.ACC_SYNTHETIC, classNameInternal, "L" + classNameInternal + "<" + returnSignature + ">;", "org/actorsguildframework/internal/MessageCaller", null);
        cw.visitSource(null, null);
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "org/actorsguildframework/internal/MessageCaller", "<init>", "()V");
            mv.visitInsn(Opcodes.RETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", "L" + classNameInternal + ";", null, l0, l1, 0);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "invoke", "(Lorg/actorsguildframework/Actor;[Ljava/lang/Object;)Lorg/actorsguildframework/AsyncResult;", "(Lorg/actorsguildframework/Actor;[Ljava/lang/Object;)Lorg/actorsguildframework/AsyncResult<" + returnSignature + ">;", null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(method.getDeclaringClass()) + "__ACTORPROXY");
            int idx = 0;
            for (Class<?> t : method.getParameterTypes()) {
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                mv.visitIntInsn(Opcodes.BIPUSH, idx);
                mv.visitInsn(Opcodes.AALOAD);
                if (t.isPrimitive()) {
                    String wrapperDescr = GenerationUtils.getWrapperInternalName(t);
                    mv.visitTypeInsn(Opcodes.CHECKCAST, wrapperDescr);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, wrapperDescr, t.getName() + "Value", "()" + Type.getDescriptor(t));
                } else {
                    if (isArgumentFreezingRequired(method, idx, t)) {
                        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(SerializableFreezer.class));
                        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(SerializableFreezer.class), "get", Type.getMethodDescriptor(SerializableFreezer.class.getMethod("get")));
                    }
                    if (!t.equals(Object.class)) mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(t));
                }
                idx++;
            }
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(method.getDeclaringClass()) + "__ACTORPROXY", String.format(SUPER_CALLER_NAME_FORMAT, method.getName()), Type.getMethodDescriptor(method));
            mv.visitInsn(Opcodes.ARETURN);
            Label l2 = new Label();
            mv.visitLabel(l2);
            mv.visitLocalVariable("this", "L" + classNameInternal + ";", null, l0, l2, 0);
            mv.visitLocalVariable("instance", "Lorg/actorsguildframework/Actor;", null, l0, l2, 1);
            mv.visitLocalVariable("arguments", "[Ljava/lang/Object;", null, l0, l2, 2);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getMessageName", "()Ljava/lang/String;", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitLdcInsn(method.getName());
            mv.visitInsn(Opcodes.ARETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", "L" + classNameInternal + ";", null, l0, l1, 0);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        cw.visitEnd();
        return (Class<MessageCaller<?>>) GenerationUtils.loadClass(className, cw.toByteArray());
    }

    /**
	 * Creates and loads the actor's proxy factory class.
	 * @param actorClass the Actor class
	 * @param acd the actor's class descriptor
	 * @return the new factory
	 */
    public static BeanFactory generateFactoryClass(Class<?> actorClass, ActorClassDescriptor acd) {
        String generatedBeanClassName = String.format("%s__ACTORPROXY", actorClass.getName());
        return BeanCreator.generateFactoryClass(actorClass, generatedBeanClassName, acd.getBeanClassDescriptor(), true);
    }

    private static final String MESSAGE_CALLER_NAME_FORMAT = "messageCaller_%d__ACTORPROXY";

    private static final String SUPER_CALLER_NAME_FORMAT = "%s__ACTORPROXYMETHOD_original";

    /**
	 * Creates a synchronized delegate method for a message method.
	 * @param actorClass the actor class
	 * @param classNameDescriptor the descriptor of the resulting class
	 * @param cw the class writer to write to
	 * @param method the method being invoked
	 * @param simpleDescriptor the descriptor of the method
	 * @param genericSignature the generic signature of the method
	 * @param isSynchronized true to make the method synchronized, false otherwise
	 */
    private static void writeSuperProxyMethod(Class<?> actorClass, String classNameDescriptor, ClassWriter cw, Method method, String simpleDescriptor, String genericSignature, boolean isSynchronized) throws NoSuchMethodException {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC + (isSynchronized ? Opcodes.ACC_SYNCHRONIZED : 0), String.format(SUPER_CALLER_NAME_FORMAT, method.getName()), simpleDescriptor, genericSignature, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        for (int j = 0; j < method.getParameterTypes().length; j++) mv.visitVarInsn(Type.getType(method.getParameterTypes()[j]).getOpcode(Opcodes.ILOAD), j + 1);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(actorClass), method.getName(), simpleDescriptor);
        mv.visitInsn(Type.getType(method.getReturnType()).getOpcode(Opcodes.IRETURN));
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable("this", classNameDescriptor, null, l0, l1, 0);
        for (int j = 0; j < method.getParameterTypes().length; j++) mv.visitLocalVariable("arg" + j, Type.getDescriptor(method.getParameterTypes()[j]), GenericTypeHelper.getSignatureIfGeneric(method.getGenericParameterTypes()[j]), l0, l1, j + 1);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
	 * Creates and loads the actor's proxy class.
	 * @param actorClass the Actor class
	 * @param acd the actor's class descriptor
	 * @throws ConfigurationException if the agent is not configured correctly
	 */
    @SuppressWarnings("unchecked")
    private static Class<?> generateProxyClass(Class<?> actorClass, final ActorClassDescriptor acd) throws NoSuchMethodException {
        BeanClassDescriptor bcd = acd.getBeanClassDescriptor();
        String className = String.format("%s__ACTORPROXY", actorClass.getName());
        final String classNameInternal = className.replace('.', '/');
        String classNameDescriptor = "L" + classNameInternal + ";";
        final Type actorState = Type.getType(acd.getConcurrencyModel().isMultiThreadingCapable() ? MultiThreadedActorState.class : SingleThreadedActorState.class);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        MethodVisitor mv;
        cw.visit(codeVersion, Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER + Opcodes.ACC_SYNTHETIC, classNameInternal, null, Type.getInternalName(actorClass), new String[] { "org/actorsguildframework/internal/ActorProxy" });
        cw.visitSource(null, null);
        {
            for (int i = 0; i < acd.getMessageCount(); i++) cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL + Opcodes.ACC_STATIC, String.format(MESSAGE_CALLER_NAME_FORMAT, i), "Lorg/actorsguildframework/internal/MessageCaller;", "Lorg/actorsguildframework/internal/MessageCaller<*>;", null).visitEnd();
            cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, "actorState__ACTORPROXY", actorState.getDescriptor(), null, null).visitEnd();
        }
        BeanCreator.writePropFields(bcd, cw);
        {
            mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();
            for (int i = 0; i < acd.getMessageCount(); i++) {
                Class<?> caller = createMessageCaller(acd.getMessage(i).getOwnerClass(), acd.getMessage(i).getMethod());
                String mcName = Type.getInternalName(caller);
                mv.visitTypeInsn(Opcodes.NEW, mcName);
                mv.visitInsn(Opcodes.DUP);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, mcName, "<init>", "()V");
                mv.visitFieldInsn(Opcodes.PUTSTATIC, classNameInternal, String.format(MESSAGE_CALLER_NAME_FORMAT, i), "Lorg/actorsguildframework/internal/MessageCaller;");
            }
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        BeanCreator.writeConstructor(actorClass, bcd, classNameInternal, cw, new BeanCreator.SnippetWriter() {

            @Override
            public void write(MethodVisitor mv) {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitTypeInsn(Opcodes.NEW, actorState.getInternalName());
                mv.visitInsn(Opcodes.DUP);
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, actorState.getInternalName(), "<init>", "(Lorg/actorsguildframework/internal/Controller;Lorg/actorsguildframework/Actor;)V");
                mv.visitFieldInsn(Opcodes.PUTFIELD, classNameInternal, "actorState__ACTORPROXY", actorState.getDescriptor());
            }
        });
        BeanCreator.writePropAccessors(bcd, classNameInternal, cw);
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getState__ACTORPROXYMETHOD", "()Lorg/actorsguildframework/internal/ActorState;", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, classNameInternal, "actorState__ACTORPROXY", actorState.getDescriptor());
            mv.visitInsn(Opcodes.ARETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", classNameDescriptor, null, l0, l1, 0);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        for (int i = 0; i < acd.getMessageCount(); i++) {
            MessageImplDescriptor mid = acd.getMessage(i);
            Method method = mid.getMethod();
            String simpleDescriptor = Type.getMethodDescriptor(method);
            String genericSignature = GenericTypeHelper.getSignature(method);
            writeProxyMethod(classNameInternal, classNameDescriptor, cw, i, actorState, acd.getConcurrencyModel(), mid, method, simpleDescriptor, genericSignature);
            writeSuperProxyMethod(actorClass, classNameDescriptor, cw, method, simpleDescriptor, genericSignature, !acd.getConcurrencyModel().isMultiThreadingCapable());
        }
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_SYNCHRONIZED, "toString", "()Ljava/lang/String;", null, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "toString", "()Ljava/lang/String;");
            mv.visitInsn(Opcodes.ARETURN);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitLocalVariable("this", classNameDescriptor, null, l0, l1, 0);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
        cw.visitEnd();
        try {
            return (Class<? extends ActorProxy>) GenerationUtils.loadClass(className, cw.toByteArray());
        } catch (Exception e) {
            throw new ConfigurationException("Failure loading ActorProxy", e);
        }
    }

    /**
	 * Checks whether the given class, when given as argument of the method, must be frozen.
	 * @param m the method to check
	 * @param idx the parameter index
	 * @param c the class to check
	 * @return true if it must be frozen, false otherwise
	 */
    private static boolean isArgumentFreezingRequired(Method m, int idx, Class<?> c) {
        if (Actor.class.isAssignableFrom(c)) return false;
        if (ImmutableHelper.isImmutableType(c)) return false;
        Annotation[][] paramAnnotations = m.getParameterAnnotations();
        for (Annotation a : paramAnnotations[idx]) if (a.annotationType().equals(Shared.class)) return false;
        return (!c.isInterface()) || Serializable.class.isAssignableFrom(c);
    }

    /**
	 * Writes a proxy method for messages.
	 * @param classNameInternal the internal class name
	 * @param classNameDescriptor the class name descriptor
	 * @param cw the ClassWriter
	 * @param index the message index
	 * @param type the ActorState type to use
	 * @param concurrencyModel the concurrency model of the message
	 * @param messageDescriptor the message's descriptor
	 * @param method the method to override
	 * @param simpleDescriptor a simple descriptor of the message
	 * @param genericSignature the signature of the message
	 */
    private static void writeProxyMethod(String classNameInternal, String classNameDescriptor, ClassWriter cw, int index, Type actorState, ConcurrencyModel concurrencyModel, MessageImplDescriptor messageDescriptor, Method method, String simpleDescriptor, String genericSignature) throws NoSuchMethodException {
        MethodVisitor mv;
        {
            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, method.getName(), simpleDescriptor, genericSignature, null);
            mv.visitCode();
            Label l0 = new Label();
            mv.visitLabel(l0);
            mv.visitIntInsn(Opcodes.BIPUSH, method.getParameterTypes().length);
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
            for (int j = 0; j < method.getParameterTypes().length; j++) {
                mv.visitInsn(Opcodes.DUP);
                mv.visitIntInsn(Opcodes.BIPUSH, j);
                Class<?> paraType = method.getParameterTypes()[j];
                if (paraType.isPrimitive()) {
                    String wrapperClass = GenerationUtils.getWrapperInternalName(paraType);
                    Type primType = Type.getType(paraType);
                    mv.visitVarInsn(primType.getOpcode(Opcodes.ILOAD), j + 1);
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, wrapperClass, "valueOf", "(" + primType.getDescriptor() + ")" + "L" + wrapperClass + ";");
                } else if (isArgumentFreezingRequired(method, j, paraType)) {
                    mv.visitVarInsn(Opcodes.ALOAD, j + 1);
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(SerializableFreezer.class), "freeze", Type.getMethodDescriptor(SerializableFreezer.class.getMethod("freeze", Object.class)));
                } else if (paraType.isInterface()) {
                    mv.visitVarInsn(Opcodes.ALOAD, j + 1);
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitTypeInsn(Opcodes.INSTANCEOF, "org/actorsguildframework/Actor");
                    Label lEndif = new Label();
                    mv.visitJumpInsn(Opcodes.IFNE, lEndif);
                    mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(ActorRuntimeException.class));
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitLdcInsn(String.format("Argument %d is an non-Serializable interface, but you did not give an Actor. If a message's argument type is an interface that does not extend Serializable, only Actors are acceptable as argument.", j));
                    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(ActorRuntimeException.class), "<init>", "(Ljava/lang/String;)V");
                    mv.visitInsn(Opcodes.ATHROW);
                    mv.visitLabel(lEndif);
                } else mv.visitVarInsn(Opcodes.ALOAD, j + 1);
                mv.visitInsn(Opcodes.AASTORE);
            }
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitVarInsn(Opcodes.ASTORE, method.getParameterTypes().length + 1);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, classNameInternal, "actorState__ACTORPROXY", actorState.getDescriptor());
            mv.visitFieldInsn(Opcodes.GETSTATIC, classNameInternal, String.format(MESSAGE_CALLER_NAME_FORMAT, index), "Lorg/actorsguildframework/internal/MessageCaller;");
            mv.visitFieldInsn(Opcodes.GETSTATIC, "org/actorsguildframework/annotations/ThreadUsage", messageDescriptor.getThreadUsage().name(), "Lorg/actorsguildframework/annotations/ThreadUsage;");
            mv.visitVarInsn(Opcodes.ALOAD, method.getParameterTypes().length + 1);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, actorState.getInternalName(), "queueMessage", "(Lorg/actorsguildframework/internal/MessageCaller;Lorg/actorsguildframework/annotations/ThreadUsage;[Ljava/lang/Object;)Lorg/actorsguildframework/internal/AsyncResultImpl;");
            mv.visitInsn(Opcodes.ARETURN);
            Label l4 = new Label();
            mv.visitLabel(l4);
            mv.visitLocalVariable("this", classNameDescriptor, null, l0, l4, 0);
            for (int j = 0; j < method.getParameterTypes().length; j++) mv.visitLocalVariable("arg" + j, Type.getDescriptor(method.getParameterTypes()[j]), GenericTypeHelper.getSignatureIfGeneric(method.getGenericParameterTypes()[j]), l0, l4, j + 1);
            mv.visitLocalVariable("args", "[Ljava/lang/Object;", null, l1, l4, method.getParameterTypes().length + 1);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
}
