public class Test {    public static <A, W> P2<A, W> unwrap(_<Writer<W>, A> writer) {
        return (P2<A, W>) writer.read(new Writer<W>());
    }
}