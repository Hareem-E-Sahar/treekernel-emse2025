public class Test {            @Override
            public BufferedInputStream open() throws Exception {
                return new BufferedInputStream(url.openStream());
            }
}