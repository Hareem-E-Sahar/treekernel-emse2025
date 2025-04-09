import java.lang.reflect.Method;
import java.util.*;
import pl.wcislo.sbql4j.mirror.apt.*;

/**
 * Annotation processor for the @Test annotation.
 * Invokes each method so annotated, and verifies the result.
 * Throws an Error on failure.
 *
 * @author Scott Seligman
 */
public class TestProcessor implements AnnotationProcessor {

    AnnotationProcessorEnvironment env;

    Tester tester = Tester.activeTester;

    TestProcessor(AnnotationProcessorEnvironment env, Tester tester) {
        this.env = env;
        this.tester = tester;
    }

    /**
     * Reflectively invoke the @Test-annotated methods of the live
     * tester.  Those methods perform the actual exercising of the
     * mirror API.  Then back here to verify the results by
     * reading the live annotations.  Convoluted, you say?
     */
    public void process() {
        System.out.printf("\n> Processing %s\n", tester.getClass());
        boolean failed = false;
        for (Method m : tester.getClass().getDeclaredMethods()) {
            Test anno = m.getAnnotation(Test.class);
            Ignore ignore = m.getAnnotation(Ignore.class);
            if (anno != null) {
                if (ignore == null) {
                    System.out.println(">> Invoking test " + m.getName());
                    Object result;
                    try {
                        result = m.invoke(tester);
                    } catch (Exception e) {
                        throw new Error("Test invocation failed", e);
                    }
                    boolean ok = true;
                    if (Collection.class.isAssignableFrom(m.getReturnType())) {
                        ok = verifyResults((Collection) result, anno.result(), anno.ordered());
                    } else if (m.getReturnType() != void.class) {
                        ok = verifyResult(result, anno.result());
                    }
                    if (!ok) {
                        System.out.println(">>> Expected: " + anno);
                        System.out.println(">>> Got: " + result);
                        failed = true;
                    }
                } else {
                    System.out.println(">> Ignoring test " + m.getName());
                    if (ignore.value().length() > 0) {
                        System.out.println(">>> Reason: " + ignore.value());
                    }
                }
            }
        }
        if (failed) {
            throw new Error("Test(s) returned unexpected result");
        }
    }

    /**
     * Verify that a single-valued (non-Collection) result matches
     * its expected value.
     */
    private boolean verifyResult(Object result, String[] expected) {
        assert expected.length == 1 : "Single-valued test expecting " + expected.length + " results";
        return expected[0].equals(String.valueOf(result));
    }

    /**
     * Verify that a multi-valued result (a Collection) matches
     * its expected values.
     */
    private boolean verifyResults(Collection result, String[] expected, boolean ordered) {
        if (result.size() != expected.length) {
            return false;
        }
        String[] res = new String[result.size()];
        int i = 0;
        for (Object e : result) {
            res[i++] = String.valueOf(e);
        }
        if (!ordered) {
            Arrays.sort(res);
            Arrays.sort(expected);
        }
        return Arrays.equals(res, expected);
    }
}
