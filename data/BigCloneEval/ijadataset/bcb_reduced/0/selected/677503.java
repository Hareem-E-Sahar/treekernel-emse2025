package net.sourceforge.jpp.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.jpp.test.CompilableSuite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class WrapperTest extends CompilableSuite {

    public static final String PACKAGE = TEST_PACKAGE + PACKAGE_SEPARATOR + "wrapper";

    public static final String PACKAGE_PREFIX = PACKAGE + PACKAGE_SEPARATOR;

    private static final String[][] _parameters = { { "Case1", "Case1Wrapper", "Case1Expected", "Case1", "Case1Impl" }, { "Case2", "fancy.FancyCase2Wrapper", "fancy.Case2Expected", "Case2", "Case2" }, { "Case3", "Case3aWrapper", "Case3Expected", "Case3", "Case3Impl" }, { "Case4", "Case4Wrapper", "Case4Expected", "Case4", "Case4" }, { "Case5", "Case5Wrapper", "Case5Expected", "Case5", "Case5Impl" } };

    @Parameterized.Parameters
    public static List<String[]> getParameters() {
        return Arrays.asList(_parameters);
    }

    private final String _sourceClassName;

    private final String _wrapperClassName;

    private final String _expectedWrapperClassName;

    private final String _wrappableClassName;

    private final String _baseImplClassName;

    private final String[] _classNames;

    public WrapperTest(String src, String wrap, String exWrap, String base, String baseImpl) {
        _sourceClassName = PACKAGE_PREFIX + src;
        _wrapperClassName = PACKAGE_PREFIX + wrap;
        _expectedWrapperClassName = PACKAGE_PREFIX + exWrap;
        _wrappableClassName = PACKAGE_PREFIX + base;
        _baseImplClassName = PACKAGE_PREFIX + baseImpl;
        _classNames = sources(_sourceClassName, _expectedWrapperClassName, _wrappableClassName, _baseImplClassName);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test() throws Exception {
        assertTrue("Compilation error", compileClass(_classNames));
        Class wrapperClass = loadClass(_wrapperClassName);
        Class expectedWrapperClass = loadClass(_expectedWrapperClassName);
        Class wrappableClass = loadClass(_wrappableClassName);
        assertSimilar(expectedWrapperClass, wrapperClass);
        if ((wrapperClass.getModifiers() & Modifier.ABSTRACT) == 0) {
            Constructor constructor = wrapperClass.getConstructor(wrappableClass);
            Object wrappableObject = loadClass(_baseImplClassName).newInstance();
            Object instance = constructor.newInstance(wrappableObject);
            assertNotNull(instance);
        }
    }
}
