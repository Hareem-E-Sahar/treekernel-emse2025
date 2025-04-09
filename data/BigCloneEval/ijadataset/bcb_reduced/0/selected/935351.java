package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import java.io.File;

/**
 * this extension deletes the packages sepcifies by the user from the
 * generated code.
 * this feature is very important to remove some commonly generated classes.
 */
public class ExcludePackageExtension extends AbstractCodeGenerationExtension {

    public void engage(CodeGenConfiguration configuration) throws CodeGenerationException {
        String excludePackagesString = configuration.getExcludeProperties();
        if ((excludePackagesString != null) && (excludePackagesString.trim().length() > 0)) {
            String[] excludePackages = excludePackagesString.split(",");
            File outPutFileLocation = null;
            if (configuration.isFlattenFiles()) {
                outPutFileLocation = getOutputDirectory(configuration.getOutputLocation(), null);
            } else {
                outPutFileLocation = getOutputDirectory(configuration.getOutputLocation(), configuration.getSourceLocation());
            }
            String excluePackage = null;
            File tempFile = null;
            for (int i = 0; i < excludePackages.length; i++) {
                tempFile = outPutFileLocation;
                String[] directories = excludePackages[i].split("\\.");
                int j = 0;
                for (; j < directories.length; j++) {
                    tempFile = new File(tempFile, directories[j]);
                    if (!tempFile.exists()) {
                        break;
                    }
                }
                if (j == directories.length) {
                    deleteDirectory(tempFile);
                }
            }
        }
    }

    private void deleteDirectory(File directory) {
        File[] children = directory.listFiles();
        for (int i = 0; i < children.length; i++) {
            if (children[i].isDirectory()) {
                deleteDirectory(children[i]);
            } else {
                children[i].delete();
            }
        }
        directory.delete();
    }

    private File getOutputDirectory(File outputDir, String dir2) {
        if (dir2 != null && !"".equals(dir2)) {
            outputDir = new File(outputDir, dir2);
        }
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        return outputDir;
    }
}
