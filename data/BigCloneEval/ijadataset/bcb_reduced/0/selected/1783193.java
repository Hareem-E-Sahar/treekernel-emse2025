package org.galagosearch.tupleflow.execution;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import org.galagosearch.tupleflow.Counter;
import org.galagosearch.tupleflow.InputClass;
import org.galagosearch.tupleflow.OutputClass;
import org.galagosearch.tupleflow.Parameters;
import org.galagosearch.tupleflow.Processor;
import org.galagosearch.tupleflow.TupleFlowParameters;
import org.galagosearch.tupleflow.Type;
import org.galagosearch.tupleflow.TypeReader;

/**
 *
 * @author trevor
 */
public class Verification {

    private static class VerificationErrorHandler implements ErrorHandler {

        FileLocation location;

        ErrorStore store;

        public VerificationErrorHandler(ErrorStore store, FileLocation location) {
            this.location = location;
            this.store = store;
        }

        public void addWarning(String message) {
            store.addWarning(location, message);
        }

        public void addError(String message) {
            store.addError(location, message);
        }
    }

    private static class VerificationParameters implements TupleFlowParameters {

        Step step;

        Stage stage;

        public VerificationParameters(Stage stage, Step step) {
            this.stage = stage;
            this.step = step;
        }

        public Counter getCounter(String name) {
            return null;
        }

        public Processor getTypeWriter(String specification) throws IOException {
            return null;
        }

        public TypeReader getTypeReader(String specification) throws IOException {
            return null;
        }

        public boolean writerExists(String specification, String className, String[] order) {
            StageConnectionPoint point = stage.connections.get(specification);
            if (point == null) {
                return false;
            }
            if (point.type != ConnectionPointType.Output) {
                return false;
            }
            if (!className.equals(point.getClassName())) {
                return false;
            }
            if (!compatibleOrders(order, point.getOrder())) {
                return false;
            }
            return true;
        }

        public boolean readerExists(String specification, String className, String[] order) {
            StageConnectionPoint point = stage.connections.get(specification);
            if (point == null) {
                return false;
            }
            if (point.type != ConnectionPointType.Input) {
                return false;
            }
            if (!className.equals(point.getClassName())) {
                return false;
            }
            if (!compatibleOrders(point.getOrder(), order)) {
                return false;
            }
            return true;
        }

        public Parameters getXML() {
            return step.getParameters();
        }
    }

    /**
     * Tests to see if two object orders are compatible.  By compatible, we mean that
     * a list of objects in outputOrder is also in inputOrder.  This is true if the orders
     * are identical, but also if inputOrder is more permissive than outputOrder.  
     * 
     * For instance, suppose we are sorting a list of people's names.  People typically
     * have a surname (last name) and a given name (first name).  In Galago notation,
     * consider these two orders you could use:
     *      +surname
     *      +surname +givenName
     *
     * If a list is ordered by (+surname +givenName), then it is also ordered by
     * +surname.  The reverse isn't true, though: if you order by +surname, you
     * haven't necessarily ordered by (+surname +givenName).  Therefore:
     *      compatibleOrders({ "+surname" }, { "+surname", "+givenName" }) == false
     *      compatibleOrders({ "+surname", "+givenName" }, { "+surname" }) == true
     *
     * @param currentOrder  The current order of the data that is supplied.
     * @param requiredOrder The required order of the data.
     */
    public static boolean compatibleOrders(String[] currentOrder, String[] requiredOrder) {
        if (currentOrder.length < requiredOrder.length) {
            return false;
        }
        for (int i = 0; i < requiredOrder.length; i++) {
            if (!currentOrder[i].equals(requiredOrder[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean requireParameters(String[] required, Parameters parameters, ErrorHandler handler) {
        boolean result = true;
        for (String key : required) {
            if (!parameters.containsKey(key)) {
                handler.addError("The parameter '" + key + "' is required.");
                result = false;
            }
        }
        return result;
    }

    public static boolean isOrderAvailable(String typeName, String[] orderSpec) {
        try {
            Class typeClass = Class.forName(typeName);
            Type type = (Type) typeClass.newInstance();
            return type.getOrder(orderSpec) != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isClassAvailable(String name) {
        try {
            Class.forName(name);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static boolean requireOrder(String typeName, String[] orderSpec, ErrorHandler handler) {
        if (!isOrderAvailable(typeName, orderSpec)) {
            StringBuilder builder = new StringBuilder();
            for (String orderKey : orderSpec) {
                builder.append(orderKey);
            }
            handler.addError("The order '" + builder.toString() + "' was not found in " + typeName + ".");
            return false;
        }
        return true;
    }

    public static boolean requireClass(String typeName, ErrorHandler handler) {
        if (!isClassAvailable(typeName)) {
            handler.addError("The class '" + typeName + "' could not be found.");
            return false;
        }
        return true;
    }

    public static boolean requireWriteableFile(String pathname, ErrorHandler handler) {
        File path = new File(pathname);
        if (path.exists() && !path.isFile()) {
            handler.addError("Pathname " + pathname + " exists already and isn't a file.");
            return false;
        }
        return requireWriteableDirectoryParent(pathname, handler);
    }

    public static boolean requireWriteableDirectory(String pathname, ErrorHandler handler) {
        File path = new File(pathname);
        if (path.isFile()) {
            handler.addError("Pathname " + pathname + " is a file, but a directory is required.");
            return false;
        }
        if (path.isDirectory() && !path.canWrite()) {
            handler.addError("Pathname " + pathname + " is a directory, but it isn't writable.");
            return false;
        }
        return requireWriteableDirectoryParent(pathname, handler);
    }

    /**
     * <p>If pathname exists, returns true.  If pathname doesn't exist, checks to
     * see if it's possible for this process to create something called pathname.</p>
     * 
     * <p>This method returns false if the closest existing parent directory of pathname
     * is not writeable (or isn't a directory)</p>
     *
     * <p>For example, if filename is /a/b/c/d/e/f, this method will return true if:
     * <ul>
     * <li>/a/b/c/d/e/f exists</li>
     * <li>/a/b/c/d/e/f doesn't exist, but /a/b/c/d/e does, and is writeable</li>
     * <li>/a/b/d/d/e doesn't exist, but /a/b/c/d does, and is writeable</li>
     * <li>/a doesn't exist, but / does, and is writeable.</li>
     * </ul>
     * </p>
     */
    public static boolean requireWriteableDirectoryParent(final String pathname, final ErrorHandler handler) {
        File path = new File(pathname);
        if (!path.exists()) {
            String parent = path.getParent();
            while (parent != null && !new File(parent).exists()) {
                parent = new File(parent).getParent();
            }
            if (parent == null) {
                parent = System.getProperty("user.dir");
            }
            if (!new File(parent).canWrite()) {
                handler.addError("Pathname " + pathname + " doesn't exist, and the parent directory isn't writable.");
                return false;
            }
        }
        return true;
    }

    private static class TypeState {

        public String className;

        public String[] order;

        public boolean defined;

        public TypeState() {
            this.className = "java.lang.Object";
            this.order = new String[0];
            this.defined = false;
        }

        public TypeState(TypeState state) {
            this.className = state.getClassName();
            this.order = state.getOrder();
            this.defined = state.isDefined();
        }

        public boolean check(String className, String[] order) {
            if (!defined) {
                return true;
            }
            return className.equals(this.className) && Verification.compatibleOrders(order, this.order);
        }

        public String[] getOrder() {
            return order;
        }

        public String getClassName() {
            return className;
        }

        public void update(String className, String[] order) {
            this.className = className;
            this.order = order;
            this.defined = true;
        }

        public void setDefined(boolean defined) {
            this.defined = defined;
        }

        private boolean isDefined() {
            return defined;
        }
    }

    public static void verify(TypeState state, Stage stage, ArrayList<Step> steps, ErrorStore store) {
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            boolean isLastStep = (i == (steps.size() - 1));
            if (step instanceof InputStep) {
                InputStep input = (InputStep) step;
                StageConnectionPoint point = stage.connections.get(input.getId());
                if (point == null) {
                    store.addError(step.getLocation(), "Input references a connection called '" + input.getId() + "', but it isn't listed in the connections section of the stage.");
                } else {
                    state.update(point.getClassName(), point.getOrder());
                }
            } else if (step instanceof OutputStep) {
                OutputStep output = (OutputStep) step;
                StageConnectionPoint point = stage.connections.get(output.getId());
                if (point == null) {
                    store.addError(step.getLocation(), "Output references a connection called '" + output.getId() + "', but it isn't listed in the connections section of the stage.");
                } else {
                    if (state.isDefined() && !state.getClassName().equals(point.getClassName())) {
                        store.addError(step.getLocation(), "Previous step makes '" + state.getClassName() + "' objects, but this output connection wants '" + point.getClassName() + "' objects.");
                    } else if (state.isDefined() && !compatibleOrders(state.getOrder(), point.getOrder())) {
                        store.addError(step.getLocation(), "Previous step outputs objects in '" + Arrays.toString(state.getOrder()) + "' order, but incompatible order '" + Arrays.toString(point.getOrder()) + "' is required.");
                    }
                }
                state.setDefined(false);
            } else if (step instanceof MultiStep) {
                MultiStep multiStep = (MultiStep) step;
                for (ArrayList<Step> group : multiStep.groups) {
                    verify(new TypeState(state), stage, group, store);
                    state.setDefined(false);
                }
            } else {
                Class clazz;
                try {
                    clazz = Class.forName(step.getClassName());
                } catch (ClassNotFoundException ex) {
                    store.addError(step.getLocation(), "Couldn't find class: " + step.getClassName());
                    continue;
                }
                VerificationParameters vp = new VerificationParameters(stage, step);
                verifyInputClass(state, step, clazz, vp, store);
                verifyStepClass(clazz, step, store, vp);
                if (!isLastStep) {
                    verifyOutputClass(state, clazz, step, store, vp);
                }
            }
        }
    }

    private static void verifyOutputClass(TypeState state, final Class clazz, final Step step, final ErrorStore store, final VerificationParameters vp) {
        String[] outputOrder = new String[0];
        String outputClass = "java.lang.Object";
        try {
            OutputClass outputClassAnnotation = (OutputClass) clazz.getAnnotation(OutputClass.class);
            if (outputClassAnnotation != null) {
                outputClass = outputClassAnnotation.className();
                outputOrder = outputClassAnnotation.order();
                state.update(outputClass, outputOrder);
                if (!Verification.isClassAvailable(outputClass)) {
                    store.addError(step.getLocation(), step.getClassName() + ": Class " + step.getClassName() + " has an " + "@OutputClass annotation with the class name '" + outputClass + "' which couldn't be found.");
                    state.setDefined(false);
                } else {
                    state.update(outputClass, outputOrder);
                }
            } else {
                try {
                    Method getOutputClass = clazz.getDeclaredMethod("getOutputClass", TupleFlowParameters.class);
                    if (getOutputClass.getReturnType() == String.class) {
                        outputClass = (String) getOutputClass.invoke(null, vp);
                        outputOrder = new String[0];
                        try {
                            Method getOutputOrder = clazz.getDeclaredMethod("getOutputOrder", TupleFlowParameters.class);
                            outputOrder = (String[]) getOutputOrder.invoke(null, vp);
                        } catch (NoSuchMethodException e) {
                        }
                        if (!Verification.isClassAvailable(outputClass)) {
                            store.addError(step.getLocation(), step.getClassName() + ": Class " + step.getClassName() + " returned " + "an output class name '" + outputClass + "' which couldn't be found.");
                            state.setDefined(false);
                        } else {
                            state.update(outputClass, outputOrder);
                        }
                    } else {
                        store.addError(step.getLocation(), step.getClassName() + " has a class method called getOutputClass, " + "but it returns something other than java.lang.String.");
                        state.setDefined(false);
                    }
                } catch (NoSuchMethodException e) {
                    store.addWarning(step.getLocation(), step.getClassName() + ": Class " + step.getClassName() + " has no suitable " + "getOutputClass method and no @OutputClass annotation.");
                    state.setDefined(false);
                }
            }
        } catch (InvocationTargetException e) {
            store.addError(step.getLocation(), step.getClassName() + ": Caught an InvocationTargetException while verifying class: " + e.getMessage());
            state.setDefined(false);
        } catch (SecurityException e) {
            store.addError(step.getLocation(), step.getClassName() + ": Caught a SecurityException while verifying class: " + e.getMessage());
            state.setDefined(false);
        } catch (IllegalArgumentException e) {
            store.addError(step.getLocation(), step.getClassName() + ": Caught an IllegalArgumentException while verifying class: " + e.getMessage());
            state.setDefined(false);
        } catch (IllegalAccessException e) {
            store.addError(step.getLocation(), step.getClassName() + ": Caught an IllegalAccessException while verifying class: " + e.getMessage());
            state.setDefined(false);
        }
    }

    private static void verifyStepClass(final Class clazz, final Step step, final ErrorStore store, final VerificationParameters vp) {
        try {
            Verified verifiedAnnotation = (Verified) clazz.getAnnotation(Verified.class);
            if (verifiedAnnotation != null) {
                return;
            }
            Method verify = clazz.getDeclaredMethod("verify", TupleFlowParameters.class, ErrorHandler.class);
            if (verify == null) {
                store.addWarning(step.getLocation(), "Class " + step.getClassName() + " has no suitable verify method.");
            } else if (Modifier.isStatic(verify.getModifiers()) == false) {
                store.addWarning(step.getLocation(), "Class " + step.getClassName() + " has a verify method, but it isn't static.");
            } else {
                verify.invoke(null, vp, new VerificationErrorHandler(store, step.getLocation()));
            }
        } catch (InvocationTargetException e) {
            store.addError(step.getLocation(), step.getClassName() + ": Caught an InvocationTargetException while verifying class: " + e.getMessage());
        } catch (SecurityException e) {
            store.addError(step.getLocation(), step.getClassName() + ": Caught a SecurityException while verifying class: " + e.getMessage());
        } catch (NoSuchMethodException e) {
            store.addWarning(step.getLocation(), "Class " + step.getClassName() + " has no suitable verify method.");
        } catch (IllegalArgumentException e) {
            store.addError(step.getLocation(), step.getClassName() + ": Caught an IllegalArgumentException while verifying class: " + e.getMessage());
        } catch (IllegalAccessException e) {
            store.addError(step.getLocation(), step.getClassName() + ": Caught an IllegalAccessException while verifying class: " + e.getMessage());
        }
    }

    private static Class findInputClassType(Class clazz) {
        Method[] allMethods = clazz.getMethods();
        for (Method method : allMethods) {
            if (!method.getName().equals("process")) continue;
            Class[] types = method.getParameterTypes();
            if (types.length != 1) continue;
            if (types[0] == Object.class) continue;
            return types[0];
        }
        return null;
    }

    private static void verifyInputClass(TypeState state, final Step step, final Class clazz, final VerificationParameters vp, final ErrorStore store) {
        if (!state.isDefined()) {
            return;
        }
        try {
            Class inputClass = findInputClassType(clazz);
            InputClass inputClassAnnotation = (InputClass) clazz.getAnnotation(InputClass.class);
            String inputClassName = "unknown";
            String[] inputOrder = new String[0];
            if (inputClassAnnotation != null) {
                inputClassName = inputClassAnnotation.className();
                inputOrder = inputClassAnnotation.order();
                if (inputClass != null && !inputClassName.equals(inputClass.getName())) {
                    String outputMessage = String.format("%s: Class %s has an @InputClass " + "annotation with the class name '%s', but the process() method takes " + "'%s' objects.", step.getClassName(), step.getClassName(), inputClassName, inputClass.getName());
                    store.addError(step.getLocation(), outputMessage);
                }
                if (!Verification.isClassAvailable(inputClassName)) {
                    store.addError(step.getLocation(), step.getClassName() + ": Class " + step.getClassName() + " has an " + "@InputClass annotation with the class name '" + inputClassName + "' which couldn't be found.");
                }
            } else {
                try {
                    Method getInputClass = clazz.getDeclaredMethod("getInputClass", TupleFlowParameters.class);
                    if (getInputClass.getReturnType() == String.class) {
                        inputClassName = (String) getInputClass.invoke(null, vp);
                        try {
                            Method getInputOrder = clazz.getDeclaredMethod("getInputOrder", TupleFlowParameters.class);
                            inputOrder = (String[]) getInputOrder.invoke(null, vp);
                        } catch (NoSuchMethodException e) {
                        }
                        if (!Verification.isClassAvailable(inputClassName)) {
                            store.addError(step.getLocation(), step.getClassName() + ": Class " + step.getClassName() + " has an " + "returned '" + inputClassName + "' from getInputClass, but " + "it couldn't be found.");
                        }
                    } else {
                        store.addError(step.getLocation(), step.getClassName() + " has a class method called getInputClass, " + "but it returns something other than java.lang.String.");
                    }
                } catch (NoSuchMethodException e) {
                    store.addWarning(step.getLocation(), step.getClassName() + ": Class " + step.getClassName() + " has no suitable " + "getInputClass method and has no @InputClass annotation.");
                    return;
                }
            }
            if (state.isDefined()) {
                if (!inputClassName.equals(state.getClassName())) {
                    store.addError(step.getLocation(), "Current pipeline class '" + state.getClassName() + "' is different than the required type: '" + inputClassName + "'.");
                }
                if (!compatibleOrders(state.getOrder(), inputOrder)) {
                    store.addError(step.getLocation(), "Current object order '" + Arrays.toString(state.getOrder()) + "' is incompatible " + "with the required input order: '" + Arrays.toString(inputOrder) + "'.");
                }
            }
        } catch (InvocationTargetException e) {
            store.addError(step.getLocation(), step.getClassName() + ": Caught an InvocationTargetException while verifying class: " + e.getMessage());
        } catch (SecurityException e) {
            store.addError(step.getLocation(), step.getClassName() + ": Caught a SecurityException while verifying class: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            store.addError(step.getLocation(), step.getClassName() + ": Caught an IllegalArgumentException while verifying class: " + e.getMessage());
        } catch (IllegalAccessException e) {
            store.addError(step.getLocation(), step.getClassName() + ": Caught an IllegalAccessException while verifying class: " + e.getMessage());
        }
    }

    public static void verify(Stage stage, ErrorStore store) {
        TypeState state = new TypeState();
        verify(state, stage, stage.steps, store);
    }

    public static void verify(Job job, ErrorStore store) {
        for (Stage stage : job.stages.values()) {
            verify(stage, store);
        }
    }

    public static boolean verifyTypeReader(String readerName, Class typeClass, TupleFlowParameters parameters, ErrorHandler handler) {
        return verifyTypeReader(readerName, typeClass, new String[0], parameters, handler);
    }

    public static boolean verifyTypeReader(String readerName, Class typeClass, String[] order, TupleFlowParameters parameters, ErrorHandler handler) {
        if (!parameters.readerExists(readerName, typeClass.getName(), order)) {
            handler.addError("No reader named '" + readerName + "' was found in this stage.");
            return false;
        }
        return true;
    }

    public static boolean verifyTypeWriter(String readerName, Class typeClass, String order[], TupleFlowParameters parameters, ErrorHandler handler) {
        if (!parameters.writerExists(readerName, typeClass.getName(), order)) {
            handler.addError("No writer named '" + readerName + "' was found in this stage.");
            return false;
        }
        return true;
    }
}
