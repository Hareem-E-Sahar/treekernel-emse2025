public class Test {    protected void assembleDeployFiles() throws IOException {
        ExtensionPoint exnPt = getDescriptor().getExtensionPoint(EXNPT_DEPLOY);
        ExtensionPoint appServerExnPt = getParentExtensionPoint(exnPt);
        String relativeConfigExtDirPath = getDescriptor().getAttribute(ATTR_STAGE_DEPLOY_DIR).getValue();
        File destDir = new File(getPluginTmpDir(), getAppServerDirname() + "/" + relativeConfigExtDirPath);
        for (Extension exn : appServerExnPt.getConnectedExtensions()) {
            PluginDescriptor pd = exn.getDeclaringPluginDescriptor();
            for (Parameter param : exn.getParameters("file")) {
                File src = getFilePath(pd, param.valueAsString());
                FileUtils.copyFileToDirectory(src, destDir);
            }
            for (Parameter param : exn.getParameters("dir")) {
                File srcDir = getFilePath(pd, param.valueAsString());
                FileUtils.copyDirectoryToDirectory(srcDir, destDir);
            }
        }
    }
}