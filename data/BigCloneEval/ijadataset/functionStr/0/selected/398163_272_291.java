public class Test {        void checkMembership(final String name, final boolean isMember) throws Exception {
            txnScheduler.runTask(new TestAbstractKernelRunnable() {

                public void run() {
                    Channel channel = getChannel(name);
                    Set<ClientSession> sessions = getSessions(channel);
                    for (DummyClient client : clients.values()) {
                        ClientSession session = getClientSession(client.name);
                        if (session != null && sessions.contains(session)) {
                            if (!isMember) {
                                fail("ClientGroup.checkMembership session: " + session.getName() + " is a member of " + name);
                            }
                        } else if (isMember) {
                            String sessionName = (session == null) ? "null" : session.getName();
                            fail("ClientGroup.checkMembership session: " + sessionName + " is not a member of " + name);
                        }
                    }
                }
            }, taskOwner);
        }
}