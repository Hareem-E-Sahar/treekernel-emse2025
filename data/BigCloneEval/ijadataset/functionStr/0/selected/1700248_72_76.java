public class Test {    @Override
    public void afterMoveCollection(DBBroker broker, Txn transaction, Collection collection, XmldbURI newUri) throws TriggerException {
        if (comm == null) return;
        comm.callRemoteMethods(new MethodCall(Communicator.AFTER_MOVE_COLLECTION, comm.getChannel().getName(), collection.getURI()));
    }
}