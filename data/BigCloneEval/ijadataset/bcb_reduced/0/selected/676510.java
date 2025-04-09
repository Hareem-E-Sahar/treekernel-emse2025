package org.netbeans.cubeon.gcode.persistence;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import junit.framework.TestCase;

/**
 *
 * @author Anuradha
 */
public class AttributesHandlerTest extends TestCase {

    public AttributesHandlerTest(String testName) {
        super(testName);
    }

    public void testPersistAttributes() throws IOException {
        File file = File.createTempFile("AttributesHandlerTest", null);
        System.out.println("testPersistAttributes");
        AttributesHandler persistence = new AttributesHandler(file);
        persistence.loadDefultAttributes();
        persistence.persistAttributes();
        System.out.println(new String(fileToByteArray(file)));
        AttributesHandler savedPersistence = new AttributesHandler(file);
        savedPersistence.loadAttributes();
        assertTrue(persistence.getLabels().containsAll(savedPersistence.getLabels()));
        assertTrue(persistence.getClosedStatuses().containsAll(savedPersistence.getClosedStatuses()));
        assertTrue(persistence.getOpenStatueses().containsAll(savedPersistence.getOpenStatueses()));
    }

    byte[] fileToByteArray(File file) throws FileNotFoundException, IOException {
        byte[] buffer = new byte[(int) file.length()];
        BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
        input.read(buffer, 0, buffer.length);
        input.close();
        return buffer;
    }
}
