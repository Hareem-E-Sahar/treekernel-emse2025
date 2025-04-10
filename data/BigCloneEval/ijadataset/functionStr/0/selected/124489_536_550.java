public class Test {    public void writeAll() {
        if (log.isDebugEnabled()) log.debug("writeAll() invoked");
        onlyChanges = false;
        if (getReadOnly()) log.error("unexpected write operation when readOnly is set");
        setToWrite(false);
        setBusy(true);
        super.setState(STORED);
        if (_progState != IDLE) log.warn("Programming state " + _progState + ", not IDLE, in write()");
        isReading = false;
        isWriting = true;
        _progState = -1;
        retries = 0;
        if (log.isDebugEnabled()) log.debug("start series of write operations");
        writeNext();
    }
}