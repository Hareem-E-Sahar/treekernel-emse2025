public class Test {    public void process(Group group, List resourcesName, List excludeResources, ServletContext servletContext, Writer out, String location) throws IOException {
        for (Iterator iterator = group.getSubgroups().iterator(); iterator.hasNext(); ) {
            Group subGroup = (Group) iterator.next();
            String subLocation = subGroup.getBestLocation(location);
            ResourcesProcessor subGroupProcessor = null;
            if (subGroup.isMinimize() == null) subGroupProcessor = this; else subGroupProcessor = subGroup.getJSProcessor();
            subGroupProcessor.process(subGroup, subGroup.getJsNames(), excludeResources, servletContext, out, subLocation);
        }
        for (Iterator it = resourcesName.iterator(); it.hasNext(); ) {
            URL url = null;
            String path = (String) it.next();
            if (!excludeResources.contains(path)) {
                url = URLUtils.getLocalURL(path, servletContext);
                if (url == null) {
                    String webPath = URLUtils.concatUrlWithSlaches(group.getBestLocation(location), path);
                    url = URLUtils.getWebUrlResource(webPath);
                }
                if (url == null) {
                    throw new IOException("The resources '" + path + "' could not be found neither in the webapp folder nor in a jar");
                }
                InputStream in = null;
                try {
                    in = url.openStream();
                    minifier.minify(in, out);
                    out.flush();
                } catch (Exception e) {
                    LOG.error("The JS minifier failed for file " + path, e);
                } finally {
                    if (in != null) in.close();
                }
                excludeResources.add(path);
            }
        }
    }
}