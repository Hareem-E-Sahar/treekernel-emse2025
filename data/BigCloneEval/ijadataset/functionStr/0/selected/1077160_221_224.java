public class Test {    @Override
    protected boolean isAllowedThread(boolean write) {
        return !write || getMainState().getContext().isCurrent();
    }
}