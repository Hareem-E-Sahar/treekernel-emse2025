public class Test {            @Override
            public InputStream invoke() {
                try {
                    return url.openStream();
                } catch (IOException ioe) {
                    throw new WrappingRuntimeException(ioe);
                }
            }
}