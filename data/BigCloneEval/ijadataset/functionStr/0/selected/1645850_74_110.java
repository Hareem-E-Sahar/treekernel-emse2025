public class Test {    public String runCommand(long serviceId, int port, String commandToRun) throws Exception {
        POP3Client client = new POP3Client();
        client.setConnectTimeout(timeout);
        Session session = org.zkoss.zkplus.hibernate.HibernateUtil.getSessionFactory().getCurrentSession();
        ServiceFacade serviceFacade = new ServiceFacade();
        Service service = (Service) session.load(Service.class, serviceId);
        String hostname = service.getDevice().getName();
        Login login = service.getLogin();
        String username = login.getArg1();
        String password = login.getArg2();
        try {
            client.connect(hostname, port);
            if (!username.trim().equals("") && !password.trim().equals("")) {
                if (client.login(username, password)) {
                    client.sendCommand(commandToRun);
                    commandResult = client.getReplyString();
                } else {
                    service.setStatus("down");
                    serviceFacade.saveService(service);
                }
            } else {
                client.sendCommand(commandToRun);
                commandResult = client.getReplyString();
                service.setStatus("up");
                serviceFacade.saveService(service);
            }
        } catch (Exception e) {
            omnilog.error("Error trying to connect to POP3 server at " + hostname + " on port " + port);
            service.setStatus("down");
            serviceFacade.saveService(service);
            throw e;
        } finally {
            client.logout();
            client.disconnect();
        }
        return commandResult;
    }
}