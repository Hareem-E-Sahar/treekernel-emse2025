public class Test {    public void testWSDLSerializer() {
        try {
            Definition def = TestUtils.getExtendedWSDLReader().readWSDL(WSDL_DIR, ECHO_SERVICE_NO_EXTENSIONS_WSDL);
            def.addNamespace(Jbi4EjbExtension.DEFAULT_PREFIX, Jbi4EjbExtension.NS_URI_JBI4EJB);
            Service myService = def.createService();
            myService.setQName(new QName(tns, "MyService"));
            def.addService(myService);
            Port myPort = def.createPort();
            myPort.setName("myPort");
            myService.addPort(myPort);
            Jbi4EjbTypes ejbTypes = new Jbi4EjbTypes();
            ejbTypes.setElementType(Jbi4EjbExtension.Q_ELEM_JBI4EJB_TYPES);
            Properties types = new Properties();
            types.put("myclass", "-12");
            types.put("myclass2", "-14");
            ejbTypes.setTypesSerialVersionUIDs(types);
            def.addExtensibilityElement(ejbTypes);
            Jbi4EjbAddress ejbAddress = new Jbi4EjbAddress();
            ejbAddress.setElementType(Jbi4EjbExtension.Q_ELEM_JBI4EJB_ADDRESS);
            ejbAddress.setName("name");
            ejbAddress.setLocalizationType("corbaname");
            myPort.addExtensibilityElement(ejbAddress);
            PortType portType = def.getPortType(new QName(tns, "EchoServicePortType"));
            Binding myBinding = def.createBinding();
            myBinding.setUndefined(false);
            myBinding.setQName(new QName(tns, "MyBinding"));
            myBinding.setPortType(portType);
            myPort.setBinding(myBinding);
            def.addBinding(myBinding);
            BindingOperation myOp = def.createBindingOperation();
            myOp.setName("myOp");
            myOp.setOperation((Operation) portType.getOperations().get(0));
            myBinding.addBindingOperation(myOp);
            Jbi4EjbBinding ejbBinding = new Jbi4EjbBinding();
            ejbBinding.setElementType(Jbi4EjbExtension.Q_ELEM_JBI4EJB_BINDING);
            Properties orbProperties = new Properties();
            orbProperties.setProperty("org.omg.CORBA.ORBInitialPort", "1050");
            orbProperties.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
            ejbBinding.setOrbProperties(orbProperties);
            myBinding.addExtensibilityElement(ejbBinding);
            String wsdlFileName = tempDir + File.separator + "TestWSDLwriter.wsdl";
            File wsdlFile = new File(wsdlFileName);
            FileWriter fileWriter = new FileWriter(wsdlFile);
            TestUtils.getExtendedWSDLWriter().writeWSDL(def, fileWriter);
            Definition def2 = TestUtils.getExtendedWSDLReader().readWSDL(tempDir.getAbsolutePath(), "TestWSDLwriter.wsdl");
            StringWriter strWriter = new StringWriter();
            TestUtils.getExtendedWSDLWriter().writeWSDL(def2, strWriter);
            System.out.println(strWriter);
            wsdlFile.delete();
            Binding extBinding = def2.getBinding(new QName(tns, "MyBinding"));
            Jbi4EjbBinding readenJbi4EjbBinding = (Jbi4EjbBinding) extBinding.getExtensibilityElements().get(0);
            Service extService = def2.getService(new QName(tns, "MyService"));
            Port extPort = (Port) extService.getPort("myPort");
            Jbi4EjbAddress readenJbi4EjbAddress = (Jbi4EjbAddress) extPort.getExtensibilityElements().get(0);
            Jbi4EjbTypes readenJbi4EjbTypes = (Jbi4EjbTypes) def2.getExtensibilityElements().get(0);
            assertTrue(readenJbi4EjbBinding.equals(ejbBinding));
            assertTrue(readenJbi4EjbAddress.equals(ejbAddress));
            assertTrue(readenJbi4EjbTypes.equals(ejbTypes));
        } catch (Exception e) {
            System.out.print(e.getMessage());
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}