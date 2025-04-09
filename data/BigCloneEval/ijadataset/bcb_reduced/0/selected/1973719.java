package org.hardtokenmgmt.ws.server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.hardtokenmgmt.common.Constants;
import org.hardtokenmgmt.common.vo.ResourceDataVO;

public class TestDynamicClassLoading extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testGenVersionHashCode() throws Exception {
        List<ResourceDataVO> resources = new ArrayList<ResourceDataVO>();
        resources.add(new ResourceDataVO("jar1", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar1.jar")));
        resources.add(new ResourceDataVO("jar2", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar2.jar")));
        int versionHashCode = DynamicClassLoaderMgr.genVersionHashCode(resources);
        assertTrue(versionHashCode != 0);
        List<ResourceDataVO> resources2 = new ArrayList<ResourceDataVO>();
        resources2.add(new ResourceDataVO("jar1", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar1.jar")));
        resources2.add(new ResourceDataVO("jar2", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar2.jar")));
        int versionHashCode2 = DynamicClassLoaderMgr.genVersionHashCode(resources2);
        assertTrue(versionHashCode == versionHashCode2);
        List<ResourceDataVO> resources3 = new ArrayList<ResourceDataVO>();
        resources3.add(new ResourceDataVO("jar1", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.1", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar1.jar")));
        resources3.add(new ResourceDataVO("jar2", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar2.jar")));
        int versionHashCode3 = DynamicClassLoaderMgr.genVersionHashCode(resources3);
        assertTrue(versionHashCode != versionHashCode3);
        List<ResourceDataVO> resources4 = new ArrayList<ResourceDataVO>();
        resources4.add(new ResourceDataVO("jar1.1", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar1.jar")));
        resources4.add(new ResourceDataVO("jar2", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar2.jar")));
        List<ResourceDataVO> resources5 = new ArrayList<ResourceDataVO>();
        resources5.add(new ResourceDataVO("jar1", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar1.jar")));
        resources5.add(new ResourceDataVO("jar2", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar2.jar")));
        resources5.add(new ResourceDataVO("jar2", Constants.RESOURCE_TYPE_IMAGE, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar2.jar")));
        int versionHashCode5 = DynamicClassLoaderMgr.genVersionHashCode(resources5);
        assertTrue(versionHashCode == versionHashCode5);
        List<ResourceDataVO> resources6 = new ArrayList<ResourceDataVO>();
        resources6.add(new ResourceDataVO("jar1", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar1.jar")));
        resources6.add(new ResourceDataVO("jar2", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar2.jar")));
        resources6.add(new ResourceDataVO("jar2", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_TOLIMA, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar2.jar")));
        int versionHashCode6 = DynamicClassLoaderMgr.genVersionHashCode(resources6);
        assertTrue(versionHashCode == versionHashCode6);
    }

    public void testBasicClassLoading() throws Exception {
        List<ResourceDataVO> resources = new ArrayList<ResourceDataVO>();
        resources.add(new ResourceDataVO("jar1", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar1.jar")));
        DynamicClassLoader cl1 = DynamicClassLoaderMgr.getDynamicClassloader("org1", resources);
        assertNotNull(cl1);
        testClassLoading(cl1, "Hello: test");
        Class<?> c = cl1.loadClass("java.util.Date");
        assertNotNull(c);
        assertTrue(c.getName().equals("java.util.Date"));
        int version1 = cl1.getVersionHashCode();
        resources = new ArrayList<ResourceDataVO>();
        resources.add(new ResourceDataVO("jar1", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar1.jar")));
        DynamicClassLoader cl2 = DynamicClassLoaderMgr.getDynamicClassloader("org1", resources);
        assertEquals(cl1, cl2);
        assertEquals(version1, cl2.getVersionHashCode());
        resources = new ArrayList<ResourceDataVO>();
        resources.add(new ResourceDataVO("jar1", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar1.jar")));
        resources.add(new ResourceDataVO("jar2", Constants.RESOURCE_TYPE_IMAGE, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar2.jar")));
        DynamicClassLoader cl3 = DynamicClassLoaderMgr.getDynamicClassloader("org1", resources);
        assertEquals(cl1, cl3);
        assertEquals(version1, cl3.getVersionHashCode());
        resources = new ArrayList<ResourceDataVO>();
        resources.add(new ResourceDataVO("jar1", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar1.jar")));
        resources.add(new ResourceDataVO("jar2", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_TOLIMA, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar2.jar")));
        DynamicClassLoader cl4 = DynamicClassLoaderMgr.getDynamicClassloader("org1", resources);
        assertEquals(cl1, cl4);
        assertEquals(version1, cl4.getVersionHashCode());
        resources = new ArrayList<ResourceDataVO>();
        resources.add(new ResourceDataVO("jar1", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.1", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar1.jar")));
        DynamicClassLoader cl5 = DynamicClassLoaderMgr.getDynamicClassloader("org1", resources);
        assertFalse(cl1.equals(cl5));
        assertFalse(version1 == cl5.getVersionHashCode());
        resources = new ArrayList<ResourceDataVO>();
        resources.add(new ResourceDataVO("jar1", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar1.jar")));
        resources.add(new ResourceDataVO("jar2", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar2.jar")));
        DynamicClassLoader cl6 = DynamicClassLoaderMgr.getDynamicClassloader("org1", resources);
        assertFalse(cl1.equals(cl6));
        assertFalse(version1 == cl6.getVersionHashCode());
        testClassLoading(cl6, "Hello: test");
        resources = new ArrayList<ResourceDataVO>();
        resources.add(new ResourceDataVO("jar2", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar2.jar")));
        resources.add(new ResourceDataVO("jar1", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar1.jar")));
        DynamicClassLoader cl7 = DynamicClassLoaderMgr.getDynamicClassloader("org1", resources);
        assertFalse(cl1.equals(cl7));
        assertFalse(version1 == cl7.getVersionHashCode());
        testClassLoading(cl7, "Bye: test");
    }

    public void testLotsOfClassLoading() throws Exception {
        for (int i = 0; i < 500; i++) {
            List<ResourceDataVO> resources = new ArrayList<ResourceDataVO>();
            resources.add(new ResourceDataVO("jar1", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar1.jar")));
            DynamicClassLoader cl1 = DynamicClassLoaderMgr.getDynamicClassloader("org1", resources);
            testClassLoading(cl1, "Hello: test");
            resources = new ArrayList<ResourceDataVO>();
            resources.add(new ResourceDataVO("jar2", Constants.RESOURCE_TYPE_CUSTOMCODEJAR, Constants.APPLICATION_ALL, "org1", "1.0", null, readFile("src/test/org/hardtokenmgmt/ws/server/sometestjar2.jar")));
            DynamicClassLoader cl2 = DynamicClassLoaderMgr.getDynamicClassloader("org1", resources);
            testClassLoading(cl2, "Bye: test");
        }
    }

    private void testClassLoading(DynamicClassLoader cl, String result) throws Exception {
        Class<?> c = cl.loadClass("sometestjar.SomeFunctionImple");
        assertNotNull(c);
        assertTrue(c.getName().equals("sometestjar.SomeFunctionImple"));
        Object o = c.newInstance();
        assertNotNull(o);
        assertTrue(o instanceof ISomeFunction);
        assertTrue(((ISomeFunction) o).sayHello("test"), ((ISomeFunction) o).sayHello("test").equals(result));
    }

    private byte[] readFile(String path) throws Exception {
        File f = new File(path);
        assertTrue(f.exists());
        FileInputStream fis = new FileInputStream(f);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = fis.read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return baos.toByteArray();
    }
}
