package org.magiclabs.mosaic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.magiclabs.mosaic.Invokers.MethodMatrixSpec;
import org.magiclabs.mosaic.Invokers.SideEffectInvoker;
import org.magiclabs.mosaic.Mosaic.AppliesTo;
import org.magiclabs.mosaic.Mosaic.AppliesToFilter;
import org.magiclabs.mosaic.MosaicSpec.InstantiationContext;

abstract class TypeSpec {

    public static final TypeSpec[] ArrayType = new TypeSpec[0];

    protected final Class<?> object;

    protected final Injector.Handler<Object> injectMosaic;

    protected final Injector.Handler<Map<Class<?>, Object>> injectRegistry;

    protected final Set<Method> methods = new MethodSet();

    private Constructor<?>[] constructors;

    private final List<Mosaic.AppliesToFilter> waveConstraint;

    public TypeSpec(Class<?> object) {
        this.object = object;
        this.constructors = this.object.getConstructors();
        if (!isGeneric(object)) specialMethods(this.methods, object);
        this.injectMosaic = new Injector.MosaicHandler();
        this.injectRegistry = new Injector.FactoryHandler();
        this.waveConstraint = new ArrayList<Mosaic.AppliesToFilter>();
        {
            AppliesTo ann = object.getAnnotation(AppliesTo.class);
            if (ann != null) for (Class<?> annotation : ann.value()) if (AppliesToFilter.class.isAssignableFrom(annotation)) try {
                this.waveConstraint.add((AppliesToFilter) annotation.newInstance());
            } catch (Exception e) {
                throw new RuntimeException("Can't instantiate class " + annotation + ".\nEither it is not public or not static.", e);
            } else if (Annotation.class.isAssignableFrom(annotation)) this.waveConstraint.add(new AnnotationWaveFilter(annotation)); else this.waveConstraint.add(new SubclassOfWaveFilter(annotation));
        }
    }

    abstract TypeSpec copy();

    private Set<Method> specialMethods(Set<Method> result, Class<?> object) {
        for (Class declaredType : object.getInterfaces()) result.addAll(Arrays.asList(declaredType.getMethods()));
        result.addAll(Arrays.asList(object.getMethods()));
        return result;
    }

    public static boolean isGeneric(Class<?> object) {
        return InvocationHandler.class.isAssignableFrom(object);
    }

    public void wire(Class<?> aggregation, TypeSpec[] followers) {
        this.injectMosaic.analyze(object);
        this.injectRegistry.analyze(object);
    }

    public boolean respondsTo(Method method) {
        if (!isGeneric(object) && !this.methods.contains(method)) return false;
        return isRespondingAllowed(this.waveConstraint, method, method.getDeclaringClass().getAnnotations()) || isRespondingAllowed(this.waveConstraint, method, method.getAnnotations());
    }

    public void inject(Object target, Object mosaic, Map<Class<?>, Object> injectionContext) {
        this.injectMosaic.inject(target, mosaic);
        this.injectRegistry.inject(target, injectionContext);
    }

    public Object instantiate(InstantiationContext context) {
        return this.instantiate(this.object, context.args);
    }

    @Override
    public String toString() {
        return "[" + this.getClass().getSimpleName() + ":" + this.object.getSimpleName() + "]";
    }

    protected Object instantiate(Class<?> type, Object[] args) {
        try {
            if (args.length == 0 || this.constructors.length == 0) return type.newInstance();
            if (this.constructors.length == 1) if (this.constructors[0].getParameterTypes().length == 0) return this.constructors[0].newInstance(new Object[0]); else return this.constructors[0].newInstance(args); else {
                Class<?>[] types = new Class<?>[args.length];
                for (int i = 0; i < args.length; i++) types[i] = args[i].getClass();
                try {
                    return type.getConstructor(types).newInstance(args);
                } catch (NoSuchMethodException e) {
                    return type.newInstance();
                }
            }
        } catch (InstantiationException e) {
            throw new RuntimeException(instantiationError(type, "It is probably a non-static inner class or an abstract class."), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(instantiationError(type, "Either the class itself or its constructor are not public."), e);
        } catch (IllegalArgumentException e) {
            List<String> types = new ArrayList<String>();
            for (Object arg : args) types.add(arg.getClass().getName());
            throw new RuntimeException(instantiationError(type, "Argument mismatch in calling the constructor with arguments." + "\nExpected types are (probably): " + Arrays.asList(this.constructors[0].getParameterTypes()) + "\nProvided types are (sure): " + types.toString()), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(instantiationError(type, "Its construtor has thrown an exception, probably of type: " + e.getCause() + ". See the stack trace for details"), e);
        }
    }

    private final String instantiationError(Class type, String reason) {
        return "Can't instantiate class " + type + ".\n" + reason;
    }

    protected boolean isRespondingAllowed(List<AppliesToFilter> constraints, Method method, Annotation[] annotations) {
        if (constraints.isEmpty()) return true;
        for (Mosaic.AppliesToFilter filter : this.waveConstraint) if (filter.respondsTo(method, annotations)) return true;
        return false;
    }

    static class AggregatorTypeSpec extends TypeSpec {

        public Invokers.MethodMatrixSpec result;

        public AggregatorTypeSpec(Class<?> type) {
            super(type);
        }

        public void wire(Class<?> aggregation, TypeSpec[] followers) {
            super.wire(aggregation, followers);
            this.result = new Invokers.MethodMatrixSpec(aggregation, followers).addBehavior(Object.class, followers);
        }

        public Object instantiate(InstantiationContext context) {
            return Proxy.newProxyInstance(context.aggregation.getClassLoader(), new Class[] { context.aggregation }, this.result.instantiate(context));
        }

        TypeSpec copy() {
            throw new UnsupportedOperationException();
        }
    }

    public static class MixinTypeSpec extends TypeSpec {

        public MixinTypeSpec(Class<?> object) {
            super(object);
        }

        public MixinTypeSpec copy() {
            return new MixinTypeSpec(this.object);
        }
    }

    public static class AbstractValidatorTypeSpec extends ConcernTypeSpec {

        protected Map<Class<? extends Annotation>, MosaicValidator<?, ?>> validators;

        public AbstractValidatorTypeSpec(Class<?> type, Method method, Map<Class<? extends Annotation>, MosaicValidator<?, ?>> validators) {
            super(type);
            this.methods.clear();
            this.methods.add(method);
            this.validators = validators;
        }

        @Override
        public AbstractValidatorTypeSpec copy() {
            return this;
        }
    }

    public static class ValidatorTypeSpec extends AbstractValidatorTypeSpec {

        private final Annotation[][] annotations;

        public ValidatorTypeSpec(Map<Class<? extends Annotation>, MosaicValidator<?, ?>> validators, Class<?> type, Method method, Annotation[][] annotations) {
            super(type, method, validators);
            this.annotations = annotations;
        }

        @Override
        public Object instantiate(InstantiationContext context) {
            return new Invokers.ValidatorInvoker(this.validators, this.annotations, this.next.instantiate(context));
        }
    }

    public static class ResultValidatorTypeSpec extends AbstractValidatorTypeSpec {

        private final Annotation annotation;

        public ResultValidatorTypeSpec(Class<?> type, Method method, Annotation annotation, Map<Class<? extends Annotation>, MosaicValidator<?, ?>> validators) {
            super(type, method, validators);
            this.annotation = annotation;
        }

        @Override
        public Object instantiate(InstantiationContext context) {
            return new Invokers.ResultValidatorInvoker(this.validators, this.annotation, this.next.instantiate(context));
        }
    }

    private static final class AnnotationWaveFilter implements Mosaic.AppliesToFilter {

        private final Class<?> annotation;

        public AnnotationWaveFilter(Class<?> annotation) {
            this.annotation = annotation;
        }

        public boolean respondsTo(Method method, Annotation[] annotation) {
            for (Annotation a : annotation) if (a.annotationType().equals(this.annotation)) return true;
            return false;
        }
    }

    private static final class SubclassOfWaveFilter implements Mosaic.AppliesToFilter {

        private final Class<?> type;

        public SubclassOfWaveFilter(Class<?> type) {
            this.type = type;
        }

        public boolean respondsTo(Method method, Annotation[] annotations) {
            return this.type.isAssignableFrom(method.getDeclaringClass());
        }
    }

    public static class SideEffectTypeSpec extends TypeSpec {

        private MethodMatrixSpec next;

        public SideEffectTypeSpec(Class<?> object) {
            super(object);
        }

        @Override
        SideEffectTypeSpec copy() {
            return new SideEffectTypeSpec(this.object);
        }

        @Override
        public void wire(Class<?> aggregation, TypeSpec[] followers) {
            super.wire(aggregation, followers);
            this.next = new Invokers.MethodMatrixSpec(aggregation, followers);
        }

        @Override
        public Object instantiate(InstantiationContext context) {
            Object sideEffect = super.instantiate(context);
            InvocationHandler nextInstance = this.next.instantiate(context);
            return new Invokers.SideEffectInvoker(sideEffect, nextInstance);
        }

        public void inject(Object target, Object mosaic, Map<Class<?>, Object> injectionContext) {
            Object realObject = ((SideEffectInvoker) target).unwrap();
            this.injectMosaic.inject(realObject, mosaic);
            this.injectRegistry.inject(realObject, injectionContext);
        }
    }

    public static class ConcernTypeSpec extends TypeSpec {

        public Invokers.MethodMatrixSpec next;

        private final Injector.NextHandler nextRegistry = new Injector.NextHandler();

        public ConcernTypeSpec(Class<?> object) {
            super(object);
            this.nextRegistry.analyze(object);
        }

        public ConcernTypeSpec copy() {
            return new ConcernTypeSpec(this.object);
        }

        @Override
        public void wire(Class<?> aggregation, TypeSpec[] followers) {
            super.wire(aggregation, followers);
            this.next = new Invokers.MethodMatrixSpec(aggregation, followers);
        }

        @Override
        public Object instantiate(InstantiationContext context) {
            Object result = super.instantiate(context);
            InvocationHandler nextInstance = this.next.instantiate(context);
            this.nextRegistry.inject2(context.aggregation, result, nextInstance);
            return result;
        }
    }

    public static class MethodSideEffectTypeSpec extends SideEffectTypeSpec {

        private final Method method;

        public MethodSideEffectTypeSpec(Method method, Class<?> object) {
            super(object);
            this.method = method;
        }

        public MethodSideEffectTypeSpec copy() {
            return new MethodSideEffectTypeSpec(this.method, this.object);
        }

        @Override
        public boolean respondsTo(Method method) {
            return this.method.equals(method) && super.respondsTo(method);
        }
    }

    public static class MethodConcernTypeSpec extends ConcernTypeSpec {

        private final Method method;

        public MethodConcernTypeSpec(Method method, Class<?> object) {
            super(object);
            this.method = method;
        }

        public MethodConcernTypeSpec copy() {
            return new MethodConcernTypeSpec(this.method, this.object);
        }

        @Override
        public boolean respondsTo(Method method) {
            return method.equals(this.method) && super.respondsTo(method);
        }
    }

    private static final class MethodSet extends HashSet<Method> {

        @Override
        public boolean contains(Object o) {
            Method input = (Method) o;
            for (Method method : this) if (isCompatible(method, input)) return true;
            return false;
        }

        private boolean isCompatible(Method m1, Method m2) {
            return m1.getName().equals(m2.getName()) && areArgumentsCompatible(m1.getParameterTypes(), m2.getParameterTypes());
        }

        private boolean areArgumentsCompatible(Class<?>[] p1, Class<?>[] p2) {
            if (p1 == null) return p2 == null;
            if (p2 == null) return false;
            if (p1.length != p2.length) return false;
            for (int i = 0; i < p1.length; i++) if (!p1[i].isAssignableFrom(p2[i])) return false;
            return true;
        }
    }
}
