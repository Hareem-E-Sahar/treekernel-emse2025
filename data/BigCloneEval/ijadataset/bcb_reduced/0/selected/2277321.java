package org.antlride.core.antlr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

/**
 * @author Edgar Espina
 * @since 2.1.0
 * @test Eclipse Head Less.
 */
public class InvalidAntlrPackageTest {

    @Test(expected = NoJarsAntlrPackageException.class)
    public void noJarsFound() throws InvalidAntlrPackageException {
        AntlrPackageProvider.SINGLETON.create(Path.fromOSString("target/classes"));
    }

    @Test(expected = MultipleAntlrDistributionException.class)
    public void multipleDistribution() throws InvalidAntlrPackageException {
        AntlrPackageProvider.SINGLETON.create(Path.fromOSString("target/antlr-distribution/multiple"));
    }

    @Test(expected = CannotCreateAntlrPackageException.class)
    public void couldNotCreatePackage() throws InvalidAntlrPackageException, IOException {
        File mockDir = new File("target/mock/lib");
        mockDir.mkdirs();
        JarOutputStream jarStream = new JarOutputStream(new FileOutputStream(new File(mockDir, "test.jar")));
        ZipEntry testEntry = new ZipEntry("test.txt");
        jarStream.putNextEntry(testEntry);
        jarStream.closeEntry();
        jarStream.close();
        AntlrPackageProvider.SINGLETON.create(Path.fromOSString(mockDir.getAbsolutePath()));
    }
}
