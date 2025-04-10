public class Test {    private StyledLayerType generateStyle() throws IOException {
        final MyBasePool pool = NofdpCorePlugin.getProjectManager().getPool(POOL_TYPE.eGeodata);
        final IGeodataCategory category = GeneralConfigGmlUtil.getConflictAreaCategory((PoolGeoData) pool);
        final IFile template = CDUtils.getStandardTemplateOfCategory(category);
        if (!template.exists()) throw new IllegalStateException(Messages.CDMapGenerator_5 + template.getLocation().toOSString());
        IFolder tmpFolder = (IFolder) m_destination.getParent();
        final IFile workingSld = tmpFolder.getFile("conflict.sld");
        FileUtils.copyFile(template.getLocation().toFile(), workingSld.getLocation().toFile());
        WorkspaceSync.sync(workingSld, IResource.DEPTH_INFINITE);
        if (!workingSld.exists()) throw new IllegalStateException(Messages.CDMapGenerator_8 + workingSld.getLocation().toOSString());
        final StyleReplacerConflictDetection replacer = new StyleReplacerConflictDetection(m_conflict, category, m_destination.getLocation().toFile(), workingSld.getLocation().toFile());
        boolean replaced = replacer.replace();
        if (!replaced) throw new IllegalStateException(Messages.CDMapGenerator_9);
        final Style style = new Style();
        style.setStyle("conflict.shp");
        style.setLinktype("sld");
        style.setType("simple");
        style.setHref("./../.tmp/conflict.sld");
        style.setActuate("onRequest");
        final StyledLayerType layer = new StyledLayerType();
        layer.setVisible(true);
        layer.setName(Messages.CDMapGenerator_2);
        layer.setId("1");
        layer.setFeaturePath("featureMember");
        layer.setType("simple");
        layer.setActuate("onRequest");
        layer.setHref("./../.tmp/conflict#" + KalypsoDeegreePlugin.getDefault().getCoordinateSystem());
        layer.setLinktype("shape");
        layer.getStyle().add(style);
        final List<Property> properties = layer.getProperty();
        final Property property = new StyledLayerType.Property();
        property.setName(LAYER_ID);
        property.setValue(LAYER_VALUE);
        properties.add(property);
        return layer;
    }
}