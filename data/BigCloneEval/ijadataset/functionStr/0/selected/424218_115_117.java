public class Test {    public static FileChannel openChannel(@NonNull File file) {
        return openInputStream(file).getChannel();
    }
}