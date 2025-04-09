public class Test {    @Override
    public boolean SoundIsPlaying(int handle) {
        return getChannelFromHandle(handle) != BUSY_HANDLE;
    }
}