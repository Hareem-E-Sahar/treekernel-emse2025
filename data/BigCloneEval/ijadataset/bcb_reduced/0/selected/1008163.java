package br.com.efitness.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.PropertyUtils;
import br.com.efitness.repository.Persistable;
import br.com.efitness.repository.Repository;

public class R2D2ReflectionUtils {

    public static boolean setValue(Persistable invoker, Persistable persistable, String atributo, Object valor, Repository repository) {
        boolean found = false;
        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(persistable);
        for (PropertyDescriptor propertyDescriptor : descriptors) {
            if (propertyDescriptor.getName().equals(atributo)) {
                try {
                    propertyDescriptor.getWriteMethod().invoke(persistable, valor);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                found = true;
                repository.save(persistable);
                break;
            }
            try {
                Object o = propertyDescriptor.getReadMethod().invoke(persistable);
                if (o == null) {
                    o = persistable.getClass().getConstructor().newInstance();
                }
                if (o instanceof Persistable && (invoker == null || !invoker.equals(o))) {
                    if (setValue(persistable, (Persistable) o, atributo, valor, repository)) {
                        repository.save(persistable);
                        break;
                    }
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return found;
    }

    public static boolean setValue(Persistable persistable, String atributo, Object valor, Repository repository) {
        boolean found = false;
        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(persistable);
        for (PropertyDescriptor propertyDescriptor : descriptors) {
            if (propertyDescriptor.getName().equals(atributo)) {
                try {
                    propertyDescriptor.getWriteMethod().invoke(persistable, valor);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                found = true;
                repository.save(persistable);
                break;
            }
        }
        return found;
    }

    public static synchronized boolean setValue(Repository repository, Persistable persistable, String[] atributos, Object valor, int j) {
        boolean success = false;
        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(persistable);
        for (int i = 0; i < descriptors.length; i++) {
            if (descriptors[i].getName().equals(atributos[j])) {
                success = true;
                try {
                    Object o = descriptors[i].getReadMethod().invoke(persistable);
                    if (o == null) {
                        o = descriptors[i].getReadMethod().getReturnType().getConstructor().newInstance();
                        descriptors[i].getWriteMethod().invoke(persistable, o);
                        repository.save(persistable);
                    }
                    if (j < atributos.length - 1) {
                        if (o instanceof Persistable) {
                            success = setValue(repository, (Persistable) o, atributos, valor, ++j);
                        }
                    } else {
                        if (o.equals(valor)) {
                            success = false;
                            break;
                        }
                        success = true;
                        descriptors[i].getWriteMethod().invoke(persistable, valor);
                        repository.save(persistable);
                    }
                    break;
                } catch (IllegalArgumentException e) {
                    success = false;
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    success = false;
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    success = false;
                    e.printStackTrace();
                } catch (SecurityException e) {
                    success = false;
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    success = false;
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    success = false;
                    e.printStackTrace();
                }
            }
        }
        return success;
    }
}
