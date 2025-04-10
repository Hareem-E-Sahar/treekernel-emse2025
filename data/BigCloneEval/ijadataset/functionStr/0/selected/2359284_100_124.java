public class Test {    public static Application getService(String strService) {
        try {
            URL url = new URL(SERVER_URL + strService + ".srv");
            InputStream input = url.openStream();
            URLConnection ucon = url.openConnection();
            ucon.connect();
            byte[] data = new byte[ucon.getContentLength()];
            input.read(data);
            Performance.printEvent("HTTPRecv service SRV, size: [" + data.length + "]");
            Service newService = new Service(new String(data));
            getServiceFile(SERVER_URL + strService + ".class", SERVICE_PATH + strService + ".class");
            for (int i = 0; i < newService.resources.length; i++) {
                if (newService.resources[i].indexOf(".class") == -1) {
                    getServiceFile(SERVER_URL + newService.resources[i], RESOURCE_PATH + newService.resources[i]);
                } else {
                    getServiceFile(SERVER_URL + newService.resources[i], SERVICE_PATH + newService.resources[i]);
                }
            }
            Application newApp = (Application) Class.forName("titan.applications." + strService).newInstance();
            return newApp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}