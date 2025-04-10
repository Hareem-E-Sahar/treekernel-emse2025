public class Test {    protected void assembleConnectorModuleProductAdaptors(PluginDescriptor pd, File localDestDir, XMLStreamWriter writer) throws IOException, XMLStreamException {
        ExtensionPoint exnPt = pd.getExtensionPoint(EXNPT_CONNECTORMODULE_ADPTR);
        if (exnPt != null) {
            for (Extension exn : exnPt.getConnectedExtensions()) {
                for (File src : getAdaptorFiles(exn)) {
                    File dest = new File(localDestDir, "/" + src.getName());
                    dest.getParentFile().mkdirs();
                    logger.debug("Copy " + src.getPath() + " to " + dest.getPath());
                    FileUtils.copyFile(src, dest);
                    addConnectorModule(dest.getName(), writer);
                }
            }
        }
    }
}