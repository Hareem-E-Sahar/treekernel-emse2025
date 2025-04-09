package test.sts.framework;

import junit.framework.*;
import kellinwood.meshi.manager.*;
import kellinwood.hibernate_util.*;
import sts.gui.importing.*;
import sts.framework.*;
import sts.hibernate.*;
import sts.hibernate.Result;
import java.util.*;
import java.util.List;
import java.util.Set;
import java.util.zip.*;
import java.io.*;
import javax.swing.*;

/**
 *  Test export of CSV regatta, entries, and race data.
 * @author ken
 */
public class TestExport1 extends TestCase {

    public TestExport1(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
        Init.commonInit();
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testImport() throws Exception {
        Init.importEverything();
        String filename = Framework.onlyInstance().getAppHomeDir() + "sts-sql/example.stz";
        ZipOutputStream zo = new ZipOutputStream(new FileOutputStream(filename));
        StringWriter swt;
        byte[] data;
        swt = new StringWriter();
        ExportCSVRegattaWizard erw = new ExportCSVRegattaWizard(new JFrame());
        erw.doExport(swt);
        data = swt.toString().getBytes();
        zo.putNextEntry(new ZipEntry("regatta.csv"));
        zo.write(data);
        swt = new StringWriter();
        ExportCSVEntriesWizard eew = new ExportCSVEntriesWizard(new JFrame());
        eew.doExport(swt);
        data = swt.toString().getBytes();
        zo.putNextEntry(new ZipEntry("entries.csv"));
        zo.write(data);
        swt = new StringWriter();
        ExportCSVRaceDataWizard edw = new ExportCSVRaceDataWizard(new JFrame());
        edw.doExport(swt);
        data = swt.toString().getBytes();
        zo.putNextEntry(new ZipEntry("race-data.csv"));
        zo.write(data);
        zo.close();
    }
}
