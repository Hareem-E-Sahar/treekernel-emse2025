public class Test {    @Test
    public void testTomcatInformations() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final PdfDocumentFactory pdfDocumentFactory = new PdfDocumentFactory(TEST_APP, null, output);
        final MBeanServer mBeanServer = MBeans.getPlatformMBeanServer();
        final List<ObjectName> mBeans = new ArrayList<ObjectName>();
        try {
            mBeans.add(mBeanServer.registerMBean(new ThreadPool(), new ObjectName("Catalina:type=ThreadPool,name=jk-8009")).getObjectName());
            mBeans.add(mBeanServer.registerMBean(new GlobalRequestProcessor(), new ObjectName("Catalina:type=GlobalRequestProcessor,name=jk-8009")).getObjectName());
            TomcatInformations.initMBeans();
            final List<JavaInformations> myJavaInformationsList = Arrays.asList(new JavaInformations(null, true));
            final Document document = pdfDocumentFactory.createDocument();
            document.open();
            final PdfJavaInformationsReport pdfReport = new PdfJavaInformationsReport(myJavaInformationsList, document);
            pdfReport.writeInformationsDetails();
            document.close();
            assertNotEmptyAndClear(output);
            mBeans.add(mBeanServer.registerMBean(new ThreadPool(), new ObjectName("Catalina:type=ThreadPool,name=jk-8010")).getObjectName());
            final GlobalRequestProcessor jk8010 = new GlobalRequestProcessor();
            jk8010.setrequestCount(0);
            mBeans.add(mBeanServer.registerMBean(jk8010, new ObjectName("Catalina:type=GlobalRequestProcessor,name=jk-8010")).getObjectName());
            TomcatInformations.initMBeans();
            final List<JavaInformations> myJavaInformationsList2 = Arrays.asList(new JavaInformations(null, true));
            final Document document2 = pdfDocumentFactory.createDocument();
            document2.open();
            final PdfJavaInformationsReport pdfReport2 = new PdfJavaInformationsReport(myJavaInformationsList2, document2);
            pdfReport2.writeInformationsDetails();
            document2.close();
            assertNotEmptyAndClear(output);
            jk8010.setrequestCount(1000);
            final List<JavaInformations> myJavaInformationsList3 = Arrays.asList(new JavaInformations(null, true));
            final Document document3 = pdfDocumentFactory.createDocument();
            document3.open();
            final PdfJavaInformationsReport pdfReport3 = new PdfJavaInformationsReport(myJavaInformationsList3, document3);
            pdfReport3.writeInformationsDetails();
            document3.close();
            assertNotEmptyAndClear(output);
        } finally {
            for (final ObjectName registeredMBean : mBeans) {
                mBeanServer.unregisterMBean(registeredMBean);
            }
            TomcatInformations.initMBeans();
        }
    }
}