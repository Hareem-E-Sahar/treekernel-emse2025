public class Test {    private TransferDropTargetListener createTransferDropTargetListener() {
        return new WsmoTransferDropTargetListener(getGraphicalViewer());
    }
}