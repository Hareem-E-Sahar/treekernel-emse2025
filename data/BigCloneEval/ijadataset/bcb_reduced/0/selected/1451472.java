package net.sourceforge.basher.internal.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import net.sourceforge.basher.Task;
import net.sourceforge.basher.BasherException;
import net.sourceforge.basher.Phase;
import net.sourceforge.basher.annotations.*;
import net.sourceforge.basher.internal.TaskDecorator;
import net.sourceforge.basher.tasks.AbstractTask;
import org.ops4j.gaderian.service.*;
import org.apache.commons.logging.Log;

/**
 * @author Johan Lindquist
 * @version $Revision$
 */
public class TaskDecoratorImpl implements TaskDecorator {

    private ClassFactory _classFactory;

    private Log _log;

    public void setLog(final Log log) {
        _log = log;
    }

    public void setClassFactory(final ClassFactory classFactory) {
        _classFactory = classFactory;
    }

    public Task decorateInstance(final Object taskInstance) {
        try {
            if (_log.isDebugEnabled()) {
                _log.debug("Decorating instance: " + taskInstance.getClass().getName());
            }
            final String taskInstanceClassName = taskInstance.getClass().getName();
            final String className = taskInstanceClassName.substring(taskInstanceClassName.lastIndexOf('.') + 1);
            final String taskClassName = className + "BasherDecoratedTask";
            final String executionMethodName = determineExecutionMethodName(taskInstance);
            return createDecoratedTask(taskClassName, executionMethodName, taskInstance);
        } catch (Exception e) {
            throw new BasherException(e.getMessage(), e);
        }
    }

    private void processMethods(final Class<? extends Object> taskInstanceClass, final ClassFab decoratedTask) {
        if (taskInstanceClass.isAssignableFrom(Task.class)) {
            return;
        }
        final Class superClass = taskInstanceClass.getSuperclass();
        if (!superClass.equals(Object.class)) {
            processMethods(superClass, decoratedTask);
        }
        final Method[] taskMethods = Task.class.getDeclaredMethods();
        for (int i = 0; i < taskMethods.length; i++) {
            Method taskMethod = taskMethods[i];
            try {
                final Method declaredMethod = taskInstanceClass.getDeclaredMethod(taskMethod.getName(), taskMethod.getParameterTypes());
                final MethodSignature methodSignature = new MethodSignature(declaredMethod);
                final MethodFab methodFab = decoratedTask.getMethodFab(methodSignature);
                if (methodFab == null) {
                    decoratedTask.addMethod(Modifier.PUBLIC, methodSignature, "return _taskInstance." + declaredMethod.getName() + "();");
                }
            } catch (NoSuchMethodException e) {
            }
        }
    }

    private Task createDecoratedTask(final String decoratedTaskClassName, final String executionMethodName, final Object taskInstance) throws Exception {
        final ClassFab fab = _classFactory.newClass(decoratedTaskClassName, DecoratedTask.class);
        fab.addField("_taskInstance", taskInstance.getClass());
        final BodyBuilder bodyBuilder = new BodyBuilder();
        bodyBuilder.begin();
        bodyBuilder.addln("_taskInstance." + executionMethodName + "();");
        bodyBuilder.end();
        fab.addMethod(java.lang.reflect.Modifier.PUBLIC, new MethodSignature(void.class, "doExecuteTask", new Class[0], new Class[] { Throwable.class }), bodyBuilder.toString());
        bodyBuilder.clear();
        processMethods(taskInstance.getClass(), fab);
        bodyBuilder.begin();
        bodyBuilder.addln("super();");
        bodyBuilder.addln("_taskInstance = $1;");
        bodyBuilder.end();
        fab.addConstructor(new Class[] { taskInstance.getClass() }, new Class[0], bodyBuilder.toString());
        final Class<Task> decoratedTask = fab.createClass();
        final Constructor<Task> constructor = decoratedTask.getConstructor(taskInstance.getClass());
        return constructor.newInstance(taskInstance);
    }

    String determineExecutionMethodName(final Object taskInstance) {
        final Method[] methods = taskInstance.getClass().getMethods();
        Method selectedMethod = null;
        for (final Method method : methods) {
            if (method.getAnnotation(BasherExecuteMethod.class) != null) {
                if (selectedMethod == null) {
                    selectedMethod = method;
                } else {
                    throw new BasherException("Found more than 1 execute method", null);
                }
            }
        }
        if (selectedMethod != null) {
            validateExecutionMethod(selectedMethod);
            return selectedMethod.getName();
        }
        try {
            taskInstance.getClass().getMethod("executeTask");
            return "executeTask";
        } catch (NoSuchMethodException e) {
            throw new BasherException("Could not find executeTask", e);
        }
    }

    private void validateExecutionMethod(Method method) {
        if (method.getParameterTypes().length != 0) {
            throw new BasherException("Execution method '" + method.getName() + "'  must not take any parameters", null);
        }
    }
}
