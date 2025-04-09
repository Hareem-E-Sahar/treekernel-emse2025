package com.amazon.carbonado.spi;

import java.lang.reflect.Method;
import junit.framework.TestSuite;
import com.amazon.carbonado.Cursor;
import com.amazon.carbonado.Repository;
import com.amazon.carbonado.Storable;
import com.amazon.carbonado.Storage;
import com.amazon.carbonado.TestStorableBase;
import com.amazon.carbonado.TestStorables;
import com.amazon.carbonado.stored.StorableTestBasic;
import com.amazon.carbonado.stored.STBContainer;

/**
 *
 *
 * @author Brian S O'Neill
 * @author Don Schneider
 */
public class TestWrappedStorableFactory extends TestStorableBase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static TestSuite suite() {
        return new TestSuite(TestWrappedStorableFactory.class);
    }

    public TestWrappedStorableFactory() {
        super();
    }

    /**
     * Test setAndGet
     */
    public void test_proxiedSetAndGet() throws Exception {
        Class<? extends StorableTestBasic> wrapperClass = StorableGenerator.getWrappedClass(StorableTestBasic.class);
        TestStorables.InvocationTracker props = new TestStorables.InvocationTracker("props");
        TestStorables.InvocationTracker handler = new TestStorables.InvocationTracker("handler");
        StorableTestBasic wrapper = wrapperClass.getConstructor(WrappedSupport.class, Storable.class).newInstance(handler, props);
        setPrimaryKeyProperties(wrapper);
        setBasicProperties(wrapper);
        wrapper.getStringProp();
        wrapper.getIntProp();
        wrapper.getLongProp();
        wrapper.getDoubleProp();
        wrapper.getId();
        for (Method method : Storable.class.getMethods()) {
            if (method.getParameterTypes().length > 0) {
                if (method.getParameterTypes()[0] != String.class) {
                    method.invoke(wrapper, wrapper);
                }
            } else {
                method.invoke(wrapper, (Object[]) null);
            }
        }
        props.assertTrack(TestStorables.ALL_GET_METHODS | TestStorables.ALL_SET_METHODS | TestStorables.sCopy | TestStorables.sToStringKeyOnly | TestStorables.sHasDirtyProperties | TestStorables.sEqualKeys | TestStorables.sEqualProperties | TestStorables.sMarkPropertiesClean | TestStorables.sMarkAllPropertiesClean | TestStorables.sMarkPropertiesDirty | TestStorables.sMarkAllPropertiesDirty | TestStorables.ALL_COPY_PROP_METHODS);
        handler.assertTrack(TestStorables.sTryLoad | TestStorables.sLoad | TestStorables.sInsert | TestStorables.sTryInsert | TestStorables.sUpdate | TestStorables.sTryUpdate | TestStorables.sDelete | TestStorables.sTryDelete);
    }

    /**
     * Verify that storables from joined property are also wrapped.
     */
    public void test_wrappedJoin() throws Exception {
        Storage<StorableTestBasic> stbStorage = getRepository().storageFor(StorableTestBasic.class);
        StorableTestBasic stb = stbStorage.prepare();
        stb.setId(1);
        stb.initBasicProperties();
        stb.setStringProp("Hello");
        stb.insert();
        stb = stbStorage.prepare();
        stb.setId(2);
        stb.initBasicProperties();
        stb.setStringProp("Hello");
        stb.insert();
        stb = stbStorage.prepare();
        stb.setId(3);
        stb.initBasicProperties();
        stb.setStringProp("World");
        stb.insert();
        Storage<STBContainer> containerStorage = getRepository().storageFor(STBContainer.class);
        STBContainer container = containerStorage.prepare();
        container.setName("A");
        container.setCategory("Hello");
        container.setCount(2);
        container.insert();
        container = containerStorage.prepare();
        container.setName("B");
        container.setCategory("World");
        container.setCount(1);
        container.insert();
    }
}
