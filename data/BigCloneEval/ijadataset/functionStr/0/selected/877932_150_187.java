public class Test {    @Override
    public boolean pollForEvents() {
        numKilledInJava = 0;
        VM_GreenThread thread = head;
        int readCount = 0, writeCount = 0, exceptCount = 0;
        while (thread != null) {
            if (isKilled(thread)) {
                ++numKilledInJava;
            }
            if (numKilledInJava == 0) {
                thread.waitData.accept(myDowncaster);
                VM_ThreadIOWaitData waitData = myDowncaster.waitData;
                if (VM.VerifyAssertions) VM._assert(waitData == thread.waitData);
                if (waitData.readFds != null) {
                    waitData.readOffset = readCount;
                    readCount += addFileDescriptors(allFds, READ_OFFSET + readCount, waitData.readFds);
                }
                if (waitData.writeFds != null) {
                    waitData.writeOffset = writeCount;
                    writeCount += addFileDescriptors(allFds, WRITE_OFFSET + writeCount, waitData.writeFds);
                }
                if (waitData.exceptFds != null) {
                    waitData.exceptOffset = exceptCount;
                    exceptCount += addFileDescriptors(allFds, EXCEPT_OFFSET + exceptCount, waitData.exceptFds);
                }
            }
            thread = (VM_GreenThread) thread.getNext();
        }
        if (numKilledInJava > 0) {
            return true;
        }
        VM_GreenProcessor.getCurrentProcessor().isInSelect = true;
        selectInProgressMutex.lock("select in progress mutex");
        int ret = sysCall.sysNetSelect(allFds, readCount, writeCount, exceptCount);
        selectInProgressMutex.unlock();
        VM_GreenProcessor.getCurrentProcessor().isInSelect = false;
        return ret != -1;
    }
}