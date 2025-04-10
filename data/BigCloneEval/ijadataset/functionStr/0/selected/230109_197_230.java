public class Test {    public void testComplexCaseMoreChannels() throws Exception {
        List<String> channels = new ArrayList<String>();
        channels.add("push-1");
        channels.add("push-2");
        channels.add("push-3");
        channels.add("push-4");
        Set<String> stored = new HashSet<String>();
        stored.add("push-1");
        stored.add("push-2");
        PushApp pushApp = new PushApp();
        pushApp.setAppId("blah");
        pushApp.setChannels(stored);
        PushAppController.getInstance().create(pushApp);
        Device device = DeviceController.getInstance().read("IMEI:8675309");
        ExecutionContext.getInstance().setDevice(device);
        Request request = new Request("iphone_push_callback");
        request.setAttribute("os", "iphone");
        request.setAttribute("deviceToken", "device-token");
        request.setAttribute("appId", "blah");
        request.setListAttribute("channels", channels);
        this.service.invoke(request);
        device = DeviceController.getInstance().read("IMEI:8675309");
        String deviceToken = device.getDeviceToken();
        assertNotNull(deviceToken);
        assertEquals(deviceToken, "device-token");
        pushApp = PushAppController.getInstance().readPushApp("blah");
        assertNotNull(pushApp);
        Set<String> storedChannels = pushApp.getChannels();
        assertTrue(storedChannels != null && !storedChannels.isEmpty() && storedChannels.size() == 4);
        Set<String> devices = pushApp.getDevices();
        assertTrue(devices != null && !devices.isEmpty());
        String deviceId = devices.iterator().next();
        assertEquals(deviceId, device.getIdentifier());
    }
}