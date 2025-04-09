public class Test {    @Override
    public int hashCode() {
        return getUser().hashCode() + getChannel().hashCode();
    }
}