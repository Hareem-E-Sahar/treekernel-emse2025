public class Test {    public void writeClientOutput(final OctaneClientHandlerThread self) {
        System.out.println(this.requestState);
        System.out.println(this.getHtdocsDir());
        Throwable lastErrorReadHtml = null;
        try {
            readWriteHtmlDoc();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            lastErrorReadHtml = e;
        }
        if (lastErrorReadHtml != null) {
            this.writeDefaultIndexPage();
        }
    }
}