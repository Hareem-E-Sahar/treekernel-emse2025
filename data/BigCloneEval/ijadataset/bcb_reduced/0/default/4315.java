import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.t2framework.lucy.Lucy;

/**
 * <#if locale="en">
 * <p>
 * 
 * </p>
 * 
 * <#else>
 * <p>
 * このサンプルはLucyの基本的な使い方を学ぶサンプルです. それぞれのサンプルは下記にClass変数として用意しているので、
 * Eclipseのようなお使いのIDEでジャンプしてみてください.
 * </p>
 * </#if>
 * 
 * @author shot
 * 
 */
public class README {

    /**
	 * <#if locale="en">
	 * <p>
	 * 
	 * </p>
	 * 
	 * <#else>
	 * <p>
	 * このサンプルはアノテーションを使ったLucyの依存性解決を学びます. 依存性解決方式はプロパティインジェクションです.
	 * </p>
	 * </#if>
	 * 
	 * @see lucy.examples.example1.Main
	 */
    public static final Class<?> EXAMPLE1 = lucy.examples.example1.Main.class;

    /**
	 * <#if locale="en">
	 * <p>
	 * 
	 * </p>
	 * 
	 * <#else>
	 * <p>
	 * このサンプルはLucyによる複数の依存性解決の方法を学びます. 依存性解決方式はメソッドインジェクションです.
	 * </p>
	 * </#if>
	 * 
	 * @see lucy.examples.example2.Main
	 */
    public static final Class<?> EXAMPLE2 = lucy.examples.example2.Main.class;

    /**
	 * <#if locale="en">
	 * <p>
	 * 
	 * </p>
	 * 
	 * <#else>
	 * <p>
	 * このサンプルは設定ファイルによるLucyの依存性解決を学びます.
	 * </p>
	 * </#if>
	 * 
	 * @see lucy.examples.example3.Main
	 */
    public static final Class<?> EXAMPLE3 = lucy.examples.example3.Main.class;

    /**
	 * <#if locale="en">
	 * <p>
	 * 
	 * </p>
	 * 
	 * <#else>
	 * <p>
	 * このサンプルはLucyのアノテーションによるAOP機能を学びます.
	 * </p>
	 * </#if>
	 * 
	 * @see lucy.examples.example4.Main
	 */
    public static final Class<?> EXAMPLE4 = lucy.examples.example4.Main.class;

    /**
	 * <#if locale="en">
	 * <p>
	 * 
	 * </p>
	 * 
	 * <#else>
	 * <p>
	 * このサンプルはLucyのXMLによるAOP機能を学びます.
	 * </p>
	 * </#if>
	 * 
	 * @see lucy.examples.example5.Main
	 */
    public static final Class<?> EXAMPLE5 = lucy.examples.example5.Main.class;

    /**
	 * <#if locale="en">
	 * <p>
	 * 
	 * </p>
	 * 
	 * <#else>
	 * <p>
	 * このサンプルは設定ファイルを使ったコンストラクタインジェクションを学びます.
	 * </p>
	 * </#if>
	 * 
	 * @see lucy.examples.example6.Main
	 */
    public static final Class<?> EXAMPLE6 = lucy.examples.example6.Main.class;

    /**
	 * <#if locale="en">
	 * <p>
	 * 
	 * </p>
	 * 
	 * <#else>
	 * <p>
	 * このサンプルはアノテーションを使ったコンストラクタインジェクションを学びます.
	 * </p>
	 * </#if>
	 * 
	 * @see lucy.examples.example7.Main
	 */
    public static final Class<?> EXAMPLE7 = lucy.examples.example7.Main.class;

    /**
	 * <#if locale="en">
	 * <p>
	 * 
	 * </p>
	 * 
	 * <#else>
	 * <p>
	 * このサンプルは自動インジェクションの仕方を学びます.
	 * </p>
	 * </#if>
	 * 
	 * @see lucy.examples.example8.Main
	 */
    public static final Class<?> EXAMPLE8 = lucy.examples.example8.Main.class;

    /**
	 * <#if locale="en">
	 * <p>
	 * This sample shows how to use {@link Lucy#pretend}.
	 * </p>
	 * 
	 * <#else>
	 * <p>
	 * 
	 * </p>
	 * </#if>
	 * 
	 * @see lucy.examples.example9.Main
	 */
    public static final Class<?> EXAMPLE9 = lucy.examples.example9.Main.class;

    /**
	 * <#if locale="en">
	 * <p>
	 * This sample shows how to use java properties file injection.
	 * </p>
	 * 
	 * <#else>
	 * <p>
	 * このサンプルはプロパティファイルインジェクションの仕方を学びます.
	 * </p>
	 * </#if>
	 * 
	 * @see lucy.examples.example10.Main
	 */
    public static final Class<?> EXAMPLE10 = lucy.examples.example10.Main.class;

    /**
	 * <#if locale="en">
	 * <p>
	 * 
	 * </p>
	 * 
	 * <#else>
	 * <p>
	 * 全てのサンプルをまとめて実行します.
	 * </p>
	 * </#if>
	 * 
	 * @param args
	 * @throws Throwable
	 */
    public static void main(String[] args) throws Throwable {
        for (Field f : README.class.getDeclaredFields()) {
            final String name = f.getName();
            Class<?> c = (Class<?>) f.get(null);
            Method m = c.getDeclaredMethod("main", new Class<?>[] { String[].class });
            System.out.println("[begin : " + name + " ]");
            m.invoke(null, new Object[] { null });
            System.out.println("[end   : " + name + " ]\n");
        }
    }
}
