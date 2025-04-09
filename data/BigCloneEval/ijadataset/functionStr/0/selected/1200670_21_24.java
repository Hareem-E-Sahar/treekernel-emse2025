public class Test {            @Override
            public InputStream open() throws Exception {
                return url.openStream();
            }
}