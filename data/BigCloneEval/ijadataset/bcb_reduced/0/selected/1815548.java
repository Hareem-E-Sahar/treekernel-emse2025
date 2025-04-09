package tests.jfun.yan.benchmark;

import java.lang.reflect.Constructor;
import org.springframework.beans.factory.FactoryBean;
import jfun.util.Misc;
import jfun.yan.Component;
import jfun.yan.Container;
import jfun.yan.etc.injection.Injection;
import jfun.yan.spring.SpringAdapter;
import jfun.yan.xml.NutsProcessor;
import jfun.yan.xml.nuts.spring.SpringNuts;
import junit.framework.TestCase;
import tests.jfun.yan.benchmark.models.Bar;
import tests.jfun.yan.benchmark.models.Foo;
import tests.jfun.yan.benchmark.models.Noop;
import tests.jfun.yan.benchmark.models.Soo;

public class NutsTest extends TestCase {

    private static final boolean spring_involved = true;

    private static final long LOOP = 200000;

    private Container yan = null;

    protected void setUp() throws Exception {
        super.setUp();
        final NutsProcessor processor = new NutsProcessor(getClass().getClassLoader());
        if (spring_involved) SpringNuts.setSpringAware("spring integration", processor);
        processor.processResource("tests/jfun/yan/benchmark/yan_component_config.xml");
        this.yan = processor.getContainer();
        Thread.sleep(100);
        System.gc();
        Thread.sleep(100);
        System.gc();
        Thread.sleep(100);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBenchCreateComponentInstance() throws Exception {
        new Benchmark("Nuts: Create bean without injection", LOOP) {

            public void run() throws Exception {
                yan.getInstance("foo0");
            }
        }.start(true);
        Soo soo = (Soo) yan.getInstance("soo");
        assertNotNull(soo.getBar());
    }

    public void testBenchCreateProxyInstance() throws Exception {
        new Benchmark("Nuts: Create proxy without injection", LOOP) {

            public void run() throws Exception {
                yan.getInstance("barproxy");
            }
        }.start(true);
        Noop bar = (Noop) yan.getInstance("barproxy");
        assertNotNull(bar);
    }

    public void testBenchCreateComplexProxyInstance() throws Exception {
        new Benchmark("Nuts: Create complex proxy without injection", LOOP) {

            public void run() throws Exception {
                yan.getInstance("complex_proxy");
            }
        }.start(true);
        Noop bar = (Noop) yan.getInstance("complex_proxy");
        assertNotNull(bar);
    }

    public void testBenchInjectionOnly() throws Exception {
        final Soo soo = new Soo();
        new Benchmark("Nuts: <injection>", LOOP) {

            public void run() throws Exception {
                Injection inj = (Injection) yan.getInstance("injection");
                inj.inject(soo);
            }
        }.start(true);
        Noop bar = (Noop) yan.getInstance("complex_proxy");
        assertNotNull(bar);
    }

    public void testBenchCreateComponentInstanceWithFactory() throws Exception {
        final jfun.yan.factory.Factory factory = yan.getFactory("bar");
        new Benchmark("Nuts: Create bean without injection with Factory", LOOP) {

            public void run() throws Exception {
                factory.create();
            }
        }.start(true);
    }

    public void testBenchPlainNew() throws Exception {
        new Benchmark("Nuts: new Bar()", LOOP) {

            public void run() throws Exception {
                new Bar();
            }
        }.start(true);
    }

    public void testBenchPlainReflection() throws Exception {
        new Benchmark("Nuts: Bar.class.newInstance()", LOOP) {

            public void run() throws Exception {
                Bar.class.newInstance();
            }
        }.start(true);
    }

    public void testBenchConstructorReflectionCall() throws Exception {
        final Constructor ctor = Bar.class.getConstructor(null);
        new Benchmark("Nuts: BarConstructor.newInstance()", LOOP) {

            public void run() throws Exception {
                ctor.newInstance(null);
            }
        }.start(true);
    }

    public void testBenchConstructorInjection() throws Exception {
        new Benchmark("Nuts: Create bean with Constructor Dependency Injection", LOOP) {

            public void run() throws Exception {
                yan.getInstance("foo");
            }
        }.start(true);
        Foo foo = (Foo) yan.getInstance("foo");
        assertNotNull(foo.getBar());
    }

    public void testBenchSetterInjectio() throws Exception {
        new Benchmark("Nuts: Create bean with Setter Dependency Injection", LOOP) {

            public void run() throws Exception {
                yan.getInstance("soo");
            }
        }.start(true);
        Soo soo = (Soo) yan.getInstance("soo");
        assertNotNull(soo.getBar());
    }

    public void testBenchBytypeSetterInjectio() throws Exception {
        new Benchmark("Nuts: Create bean with bytype autowiring and Setter Dependency Injection", LOOP) {

            public void run() throws Exception {
                yan.getInstance("auto_soo");
            }
        }.start(true);
        Soo soo = (Soo) yan.getInstance("auto_soo");
        assertNotNull(soo.getBar());
    }

    public void testBenchSingleton() throws Exception {
        new Benchmark("Nuts: Create singleton bean with Setter Dependency Injection", LOOP * 10) {

            public void run() throws Exception {
                yan.getInstance("ssoo");
            }
        }.start(true);
        Soo soo = (Soo) yan.getInstance("ssoo");
        assertNotNull(soo.getBar());
    }

    public void testBenchSingletonFactory() throws Exception {
        final jfun.yan.factory.Factory factory = yan.getFactory("ssoo");
        Benchmark bench = new Benchmark("Nuts: Singleton Bean with Factory", LOOP * 10) {

            public void run() throws Exception {
                final Soo foo = (Soo) factory.create();
                foo.noop();
            }
        };
        bench.start(true);
    }

    public void testBenchSingletonCustomFactory() throws Exception {
        final Component ssoo = yan.getComponent("ssoo").factory(MyFactory.class);
        final MyFactory factory = (MyFactory) yan.instantiateComponent(ssoo);
        Benchmark bench = new Benchmark("Nuts: Singleton Bean with custom factory", LOOP * 10) {

            public void run() throws Exception {
                final Soo foo = (Soo) factory.create();
                foo.noop();
            }
        };
        bench.start(true);
    }

    public void testCtorFactory() throws Exception {
        final jfun.yan.factory.Factory factory = yan.getFactory("foo");
        Benchmark bench = new Benchmark("Nuts: Constructor Injection with Factory", LOOP) {

            public void run() throws Exception {
                final Foo foo = (Foo) factory.create();
                foo.noop();
            }
        };
        bench.start(true);
    }

    public void testSetterFactory() throws Exception {
        final jfun.yan.factory.Factory factory = yan.getFactory("soo");
        Benchmark bench = new Benchmark("Nuts: Setter Injection with Factory", LOOP) {

            public void run() throws Exception {
                final Soo foo = (Soo) factory.create();
                foo.noop();
            }
        };
        bench.start(true);
    }

    public void testBenchEmptyInterceptor() throws Exception {
        if (!spring_involved) return;
        Benchmark bench = new Benchmark("Nuts: Bean method invocation with empty interceptor applied", LOOP * 100) {

            Soo foo = (Soo) yan.getInstance("sooProxy");

            public void run() throws Exception {
                foo.noop();
            }
        };
        bench.start(true);
        FactoryBean fb = SpringAdapter.getFactoryBean(yan, "fooProxy");
        assertNotNull(fb);
    }

    public void testBenchCreateAsectizedBean() throws Exception {
        if (!spring_involved) return;
        new Benchmark("Nuts: Create aspectized bean", LOOP / 10) {

            private Object old = null;

            public void run() throws Exception {
                final Soo nw = (Soo) yan.getInstance("sooProxy");
                if (old != null) {
                    assertNotSame(old, nw);
                }
                old = nw;
            }
        }.start(true);
    }
}
