public class Test {            @SuppressWarnings("unchecked")
            public S next() {
                Object target = null;
                final String clazzname = classNames.get(pointer);
                try {
                    final Class<?> clazz = classLoader.loadClass(clazzname);
                    final Constructor[] constructors = clazz.getConstructors();
                    for (int i = 0; i < constructors.length && target == null; i++) {
                        Constructor c = constructors[i];
                        if (c.getGenericParameterTypes().length == 0) {
                            target = c.newInstance(new Object[] {});
                        }
                    }
                } catch (Exception transform) {
                    throw new RuntimeException(transform);
                } finally {
                    pointer++;
                }
                if (target == null) {
                    throw new IllegalStateException("Class " + clazzname + " does not have a public default constructor!");
                }
                return (S) target;
            }
}