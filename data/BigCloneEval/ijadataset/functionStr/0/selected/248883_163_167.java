public class Test {    public void swap() {
        GridValueLayer tmp = read;
        read = write;
        write = tmp;
    }
}