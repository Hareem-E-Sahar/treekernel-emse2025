public class Test {    @Override
    protected void write() {
        super.write();
        writeStringNZ(32, name);
        write32(attr);
        write32(lwMutexUid);
        write32(lwMutexOpaqueWorkAreaAddr);
        write32(initCount);
        write32(lockedCount);
        write32(numWaitThreads);
    }
}