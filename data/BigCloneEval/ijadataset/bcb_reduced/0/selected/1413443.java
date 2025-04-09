package net.sf.securejdms.modeler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DynamicBeanWriterTest {

    private static class MyPropertyChangeListener implements PropertyChangeListener {

        public String expectedPropertyName;

        public Object expectedOldValue;

        public Object expectedNewValue;

        private Object expectedSource;

        public MyPropertyChangeListener(DynamicBean dynamicBean) {
            expectedSource = dynamicBean;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            assertNotNull("No property change call expected", expectedPropertyName);
            assertEquals(expectedPropertyName, evt.getPropertyName());
            assertEquals(expectedOldValue, evt.getOldValue());
            assertEquals(expectedNewValue, evt.getNewValue());
            assertTrue(expectedSource == evt.getSource());
            expectedPropertyName = null;
        }
    }

    private static Class<DynamicBean> dynamicBeanClass;

    private MyPropertyChangeListener myPropertyChangeListener;

    private DynamicBean dynamicBean;

    /**
	 * @throws Throwable not expected
	 */
    @BeforeClass
    public static void beforeClass() throws Throwable {
        dynamicBeanClass = new DynamicBeanWriter().generateDynamicBean("Person", new String[] { "name", "age", "dvalue" }, new Class[] { String.class, int.class, double.class });
        assertNotNull(dynamicBeanClass);
    }

    /**
	 * @throws Throwable not expected
	 */
    @Before
    public void before() throws Throwable {
        Constructor<DynamicBean> constructor = dynamicBeanClass.getConstructor(ModelElement.class);
        dynamicBean = constructor.newInstance((ModelElement) null);
        myPropertyChangeListener = new MyPropertyChangeListener(dynamicBean);
    }

    /**
	 * @throws Throwable not expected
	 */
    @Test
    public void addRemovePropertChangeListener() throws Throwable {
        dynamicBean.addPropertyChangeListener(myPropertyChangeListener);
        dynamicBean.removePropertyChangeListener(myPropertyChangeListener);
    }

    /**
	 * @throws Throwable not expected
	 */
    @Test
    public void addRemovePropertChangeListenerWithPropertyName() throws Throwable {
        dynamicBean.addPropertyChangeListener("name", myPropertyChangeListener);
        dynamicBean.removePropertyChangeListener("name", myPropertyChangeListener);
    }

    /**
	 * @throws Throwable not expected
	 */
    @Test
    public void callGetName() throws Throwable {
        String name = get(String.class, "name");
        assertNull(name);
    }

    /**
	 * @throws Throwable not expected
	 */
    @Test
    public void callGetAge() throws Throwable {
        Integer age = get(Integer.class, "age");
        assertNotNull(age);
        assertEquals(Integer.class, age.getClass());
        assertEquals(Integer.valueOf(0), age);
    }

    /**
	 * @throws Throwable not expected
	 */
    @Test
    public void setSetName() throws Throwable {
        set("name", "name1");
        assertEquals("name1", get(String.class, "name"));
    }

    /**
	 * @throws Throwable not expected
	 */
    @Test
    public void setSetAge() throws Throwable {
        set("age", Integer.valueOf(123), int.class);
        assertEquals(Integer.valueOf(123), get(Integer.class, "age"));
    }

    /**
	 * @throws Throwable not expected
	 */
    @Test
    public void testPropertyChangeListenerWithName() throws Throwable {
        dynamicBean.addPropertyChangeListener(myPropertyChangeListener);
        myPropertyChangeListener.expectedPropertyName = "name";
        myPropertyChangeListener.expectedOldValue = null;
        myPropertyChangeListener.expectedNewValue = "test1234";
        set("name", "test1234");
        assertEquals("test1234", get(String.class, "name"));
        myPropertyChangeListener.expectedPropertyName = "name";
        myPropertyChangeListener.expectedOldValue = "test1234";
        myPropertyChangeListener.expectedNewValue = "newValue!!";
        set("name", "newValue!!");
        assertEquals("newValue!!", get(String.class, "name"));
        myPropertyChangeListener.expectedPropertyName = "name";
        myPropertyChangeListener.expectedOldValue = "newValue!!";
        myPropertyChangeListener.expectedNewValue = null;
        set("name", null, String.class);
        assertEquals(null, get(String.class, "name"));
        dynamicBean.removePropertyChangeListener(myPropertyChangeListener);
        set("name", "");
        assertEquals("", get(String.class, "name"));
    }

    /**
	 * @throws Throwable not expected
	 */
    @Test
    public void testPropertyChangeListenerWithAge() throws Throwable {
        dynamicBean.addPropertyChangeListener(myPropertyChangeListener);
        myPropertyChangeListener.expectedPropertyName = "age";
        myPropertyChangeListener.expectedOldValue = Integer.valueOf(0);
        myPropertyChangeListener.expectedNewValue = Integer.valueOf(1);
        set("age", Integer.valueOf(1), int.class);
        assertEquals(Integer.valueOf(1), get(Integer.class, "age"));
        myPropertyChangeListener.expectedPropertyName = "age";
        myPropertyChangeListener.expectedOldValue = Integer.valueOf(1);
        myPropertyChangeListener.expectedNewValue = Integer.valueOf(100);
        set("age", Integer.valueOf(100), int.class);
        assertEquals(Integer.valueOf(100), get(Integer.class, "age"));
        dynamicBean.removePropertyChangeListener(myPropertyChangeListener);
        set("age", Integer.valueOf(-10), int.class);
        assertEquals(Integer.valueOf(-10), get(Integer.class, "age"));
    }

    private void set(String propertyName, Object value, Class<?>... types) throws Throwable {
        Class<?> type = types == null || types.length == 0 ? value.getClass() : types[0];
        Method setMethod = dynamicBeanClass.getDeclaredMethod("set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1), type);
        try {
            setMethod.invoke(dynamicBean, value);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T get(Class<T> clazz, String propertyName) throws Exception {
        Method getMethod = dynamicBeanClass.getDeclaredMethod("get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1));
        Object result = getMethod.invoke(dynamicBean);
        assertTrue(result == null || clazz.isAssignableFrom(result.getClass()));
        return (T) result;
    }
}
