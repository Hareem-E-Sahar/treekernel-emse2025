package java.lang;

import java.io.File;
import junit.framework.TestCase;

/**
 * ###############################################################################
 * ###############################################################################
 * TODO: 1.
 * ###############################################################################
 * ###############################################################################
 */
public class RuntimeAdditionalTest41 extends TestCase {

    /**
     *  create tree-process and cat-process, organize their one_byte-interconnection using flushing
     */
    public void test_39() {
        System.out.println("==test_39===");
        String tds = System.getProperty("java.io.tmpdir") + File.separator + System.getProperty("user.name") + "_" + System.getProperty("user.name") + "_" + System.getProperty("user.name");
        File fff = new File(tds);
        fff.mkdir();
        fff.deleteOnExit();
        for (int ind = 0; ind < 300; ind++) {
            (fff = new File(tds + File.separator + System.getProperty("user.name") + Integer.toString(ind, 10))).mkdir();
            fff.deleteOnExit();
        }
        String cmnd1 = null;
        String cmnd2 = null;
        if (RuntimeAdditionalTest0.os.equals("Win")) {
            cmnd1 = RuntimeAdditionalTest0.treeStarter + " \"" + tds + "\"";
            System.out.println(cmnd1);
            cmnd2 = RuntimeAdditionalTest0.catStarter;
        } else if (RuntimeAdditionalTest0.os.equals("Lin")) {
            cmnd1 = RuntimeAdditionalTest0.treeStarter + " /bin";
            cmnd2 = RuntimeAdditionalTest0.catStarter;
        } else {
            fail("WARNING (test_39): unknown operating system.");
        }
        try {
            Process pi3 = Runtime.getRuntime().exec(cmnd1);
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
            }
            pi3.getOutputStream();
            pi3.getErrorStream();
            java.io.InputStream is = pi3.getInputStream();
            while ((is.available()) == 0) {
            }
            RuntimeAdditionalTest0.doMessage("control point 1");
            Process pi4 = Runtime.getRuntime().exec(cmnd2);
            java.io.OutputStream os4 = pi4.getOutputStream();
            pi4.getErrorStream();
            java.io.InputStream is4 = pi4.getInputStream();
            int b1;
            int ia;
            int ii = 0;
            MMM: while (true) {
                while ((ia = is.available()) != 0) {
                    if (RuntimeAdditionalTest0.os.equals("Win")) {
                        b1 = is.read();
                        os4.write(b1);
                        os4.write(10);
                        os4.flush();
                        while ((ia = is4.available()) != 2) {
                            RuntimeAdditionalTest0.doMessage(Integer.toString(ia, 10) + "\n");
                            try {
                                pi3.exitValue();
                                if (ii > 300 + 300 * System.getProperty("user.name").length()) break MMM;
                            } catch (IllegalThreadStateException _) {
                                continue;
                            }
                            break;
                        }
                        if (ia > 2) {
                            byte[] bb = new byte[ia];
                            is4.read(bb);
                            if (ii < 200) fail("ERROR (test_39): flush() problem.");
                        }
                        if (is4.available() > 0) System.out.print(Character.toString((char) is4.read()));
                        if (is4.available() > 0) is4.read();
                    } else if (RuntimeAdditionalTest0.os.equals("Lin")) {
                        os4.write(is.read());
                        os4.write(10);
                        os4.flush();
                        while ((ia = is4.available()) == 0) {
                        }
                        is4.read();
                        while ((ia = is4.available()) == 0) {
                        }
                        is4.read();
                        try {
                            Thread.sleep(10);
                        } catch (Exception e) {
                        }
                        if (is4.available() > 0) {
                            byte[] bb = new byte[ia];
                            is4.read(bb);
                            fail("ERROR (test_39): flush() problem.");
                        }
                    }
                    ii++;
                    if (ii > 2000) {
                        break MMM;
                    }
                }
                try {
                    pi3.exitValue();
                    while ((ia = is.available()) != 0) {
                        os4.write(is.read());
                        if ((ia = is4.available()) == 0) {
                            os4.flush();
                            if (is4.available() != 1) {
                                fail("ERROR (test_39): 3.");
                            }
                        } else if (ia > 1) {
                            fail("ERROR (test_39): 4.");
                        }
                        is4.read();
                    }
                    break;
                } catch (IllegalThreadStateException e) {
                    continue;
                }
            }
            try {
                pi4.exitValue();
            } catch (IllegalThreadStateException e) {
                pi4.destroy();
            }
            RuntimeAdditionalTest0.killCat();
            RuntimeAdditionalTest0.killTree();
        } catch (Exception eeee) {
            eeee.printStackTrace();
            fail("ERROR (test_39): unexpected exception.");
        }
    }
}
