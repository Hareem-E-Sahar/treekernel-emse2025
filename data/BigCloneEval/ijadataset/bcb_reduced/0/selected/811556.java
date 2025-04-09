package net.sourceforge.purrpackage.recording.instrument;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class SimpleInstrumenter extends DirectoryTraverser implements ClassFileTransformer {

    public SimpleInstrumenter(ClassTransformer transformer) {
        this.transformer = transformer;
    }

    ClassTransformer transformer;

    File targetDir;

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            return transformer.transformClass(classfileBuffer);
        } catch (Exception e) {
            throw new RuntimeException("Transformation of " + className + " failed", e);
        }
    }

    public void transformFile(File f, File targetDir) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(f);
        try {
            int c = 0;
            while (c != -1) {
                c = fis.read();
                if (c != -1) {
                    bos.write((byte) c);
                }
            }
            bos.flush();
        } finally {
            fis.close();
        }
        File target = new File(targetDir, f.getName());
        FileOutputStream fos = new FileOutputStream(target);
        try {
            byte[] transd = transformer.transformClass(bos.toByteArray());
            fos.write(transd);
            fos.flush();
        } finally {
            fos.close();
        }
    }

    public void transformEverything(File dir, File target) throws Exception {
        target.mkdirs();
        targetDir = target;
        this.dir = dir;
        process();
    }

    @Override
    protected void visitFile(File f) {
        File td = new File(targetDir, relPath(f.getParentFile().getAbsolutePath()));
        try {
            transformFile(f, td);
            transformer.getLogMessages().add("Processed " + f + " to " + td);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Exception processing " + f, e);
        }
    }

    @Override
    protected void visitDir(File f) {
        String relPath = relPath(f.getAbsolutePath());
        File x = new File(targetDir, relPath);
        x.mkdir();
    }

    @Override
    protected void skipFile(File f) {
        transformer.getLogMessages().add("Skipping " + f);
    }
}
