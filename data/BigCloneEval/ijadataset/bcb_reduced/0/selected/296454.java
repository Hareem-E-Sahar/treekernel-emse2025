package vavi.util;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.StringTokenizer;

/**
 * �N���X�֘A�̃��[�e�B���e�B�N���X�ł��D
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 * @version 0.00 020517 nsano initial version <br>
 *          0.01 040627 nsano add getWrapperClass <br>
 */
public final class ClassUtil {

    /** Cannot access. */
    private ClassUtil() {
    }

    /**
     * �����񂩂�N���X���擾���܂��D
     * TODO �Ȃ񂩂ǂ����ɂ��肻���D
     * 
     * @param className �v���~�e�B�u�^�����̂܂܎w��ł��܂��D
     *                  �t�� java.lang �͔F�����Ȃ��̂ŏ����Ă��������D
     */
    public static Class<?> forName(String className) throws ClassNotFoundException {
        Class<?> clazz;
        if ("boolean".equals(className)) {
            clazz = Boolean.TYPE;
        } else if ("byte".equals(className)) {
            clazz = Byte.TYPE;
        } else if ("char".equals(className)) {
            clazz = Character.TYPE;
        } else if ("double".equals(className)) {
            clazz = Double.TYPE;
        } else if ("float".equals(className)) {
            clazz = Float.TYPE;
        } else if ("int".equals(className)) {
            clazz = Integer.TYPE;
        } else if ("long".equals(className)) {
            clazz = Long.TYPE;
        } else if ("short".equals(className)) {
            clazz = Short.TYPE;
        } else if ("void".equals(className)) {
            clazz = Void.TYPE;
        } else {
            clazz = Class.forName(className);
        }
        return clazz;
    }

    /**
     * �����񂩂�R���X�g���N�^�p�̈�^�̃N���X�̃��X�g���擾���܂��D
     * @param	line	�f���~�^�� { ',', '\t', ' ' }
     *			�v���~�e�B�u�^�͂��̂܂܏����D int, long ...
     */
    public static Class<?>[] getArgumentTypes(String line) throws ClassNotFoundException {
        StringTokenizer st = new StringTokenizer(line, "\t ,");
        Class<?>[] argTypes = new Class[st.countTokens()];
        for (int j = 0; j < argTypes.length; j++) {
            argTypes[j] = forName(st.nextToken());
        }
        return argTypes;
    }

    /**
     * �����񂩂�R���X�g���N�^�p�̈�̃I�u�W�F�N�g�̃��X�g���擾���܂��D
     * @param	line	�f���~�^�� { ',', '\t', ' ' }
     *			null �͂��̂܂܏����D null
     */
    static Object[] getArguments(String line, Class<?>[] argTypes) throws InstantiationException, IllegalAccessException {
        StringTokenizer st = new StringTokenizer(line, "\t ,");
        Object[] args = new Object[st.countTokens()];
        for (int j = 0; j < args.length; j++) {
            String arg = st.nextToken();
            if ("null".equals(arg)) {
                args[j] = null;
            } else if (argTypes[j] == Boolean.TYPE) {
                args[j] = new Boolean(arg);
            } else if (argTypes[j] == Byte.TYPE) {
                args[j] = new Byte(arg);
            } else if (argTypes[j] == Character.TYPE) {
                if (arg.length() > 1) {
                    throw new IllegalArgumentException(arg + " for char");
                }
                args[j] = new Character(arg.charAt(0));
            } else if (argTypes[j] == Double.TYPE) {
                args[j] = new Double(arg);
            } else if (argTypes[j] == Float.TYPE) {
                args[j] = new Float(arg);
            } else if (argTypes[j] == Integer.TYPE) {
                args[j] = new IntegerInstantiator().newInstance(arg);
            } else if (argTypes[j] == Long.TYPE) {
                args[j] = new Long(arg);
            } else if (argTypes[j] == Short.TYPE) {
                args[j] = new Short(arg);
            } else if (argTypes[j] == Void.TYPE) {
                throw new IllegalArgumentException(arg + " for void");
            } else if (argTypes[j] == String.class) {
                args[j] = new StringInstantiator().newInstance(arg);
            } else if (argTypes[j] == java.awt.Color.class) {
                args[j] = new ColorInstantiator().newInstance(arg);
            } else {
                args[j] = argTypes[j].newInstance();
            }
        }
        return args;
    }

    /**
     * �V�����C���X�^���X���擾���܂��D
     * @param	className	�v���~�e�B�u�^�����̂܂܎w��ł��܂��D
     * @param	argTypes	�f���~�^�� { ',', '\t', ' ' }
     *				�v���~�e�B�u�^�͂��̂܂܏����D int, long ...
     * @param	args		�f���~�^�� { ',', '\t', ' ' }
     *				null �͂��̂܂܏����D null
     */
    public static Object newInstance(String className, String argTypes, String args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class<?> clazz = Class.forName(className);
        Class<?>[] ats = getArgumentTypes(argTypes);
        Object[] as = getArguments(args, ats);
        Constructor<?> constructor = clazz.getConstructor(ats);
        return constructor.newInstance(as);
    }

    /**
     */
    static Field getField(String arg) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        int p = arg.lastIndexOf('.');
        String className = arg.substring(0, p);
        String enumName = arg.substring(p + 1, arg.length());
        Class<?> clazz = Class.forName(className);
        return clazz.getDeclaredField(enumName);
    }

    /**
     * �v���~�e�B�u�^���烉�b�p�[�N���X���擾���܂��B
     * @param primitiveClass int.class ��
     */
    public Class<?> getWrapperClass(Class<?> primitiveClass) {
        Object array = Array.newInstance(primitiveClass, 1);
        Object wrapper = Array.get(array, 0);
        return wrapper.getClass();
    }
}

/**
 */
interface Instantiator {

    Object newInstance(String arg) throws InstantiationException;
}

/**
 */
class IntegerInstantiator implements Instantiator {

    public Object newInstance(String arg) throws InstantiationException {
        try {
            return new Integer(arg);
        } catch (NumberFormatException e) {
            try {
                Field field = ClassUtil.getField(arg);
                if (field.getType() != Integer.TYPE) throw new IllegalArgumentException(arg + " for int");
                return new Integer(field.getInt(null));
            } catch (Exception f) {
                throw (InstantiationException) new InstantiationException().initCause(f);
            }
        }
    }
}

/**
 */
class StringInstantiator implements Instantiator {

    public Object newInstance(String arg) throws InstantiationException {
        return arg;
    }
}

/**
 */
class ColorInstantiator implements Instantiator {

    public Object newInstance(String arg) throws InstantiationException {
        try {
            Field field = ClassUtil.getField(arg);
            if (field.getType() != java.awt.Color.class) {
                throw new IllegalArgumentException(arg + " for Color");
            }
            return field.get(null);
        } catch (Exception f) {
            throw (InstantiationException) new InstantiationException().initCause(f);
        }
    }
}
