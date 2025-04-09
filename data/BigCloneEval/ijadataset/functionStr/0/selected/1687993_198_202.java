public class Test {    @Override
    public void StopSound(int handle) {
        int hnd = getChannelFromHandle(handle);
        if (hnd >= 0) channels[hnd].stopSound();
    }
}