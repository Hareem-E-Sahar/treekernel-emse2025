public class Test {    private void initDSpace(FilterConfig arg0) throws ServletException {
        try {
            String osName = System.getProperty("os.name");
            if (osName != null) {
                osName = osName.toLowerCase();
            }
            if (osName != null && osName.contains("windows")) {
                URL url = new URL("http://localhost/");
                URLConnection urlConn = url.openConnection();
                urlConn.setDefaultUseCaches(false);
            }
        } catch (RuntimeException e) {
            LOG.error(e.getMessage(), e);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        String dspaceConfig = null;
        dspaceConfig = arg0.getInitParameter(DSPACE_CONFIG_PARAMETER);
        if (dspaceConfig == null) {
            dspaceConfig = arg0.getServletContext().getInitParameter(DSPACE_CONFIG_PARAMETER);
        }
        if (dspaceConfig == null || "".equals(dspaceConfig)) {
            throw new ServletException("\n\nDSpace has failed to initialize. This has occurred because it was unable to determine \n" + "where the dspace.cfg file is located. The path to the configuration file should be stored \n" + "in a context variable, '" + DSPACE_CONFIG_PARAMETER + "', in either the local servlet or global contexts. \n" + "No context variable was found in either location.\n\n");
        }
        try {
            if (!ConfigurationManager.isConfigured()) {
                ConfigurationManager.loadConfig(dspaceConfig);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException("\n\nDSpace has failed to initialize, during stage 2. Error while attempting to read the \n" + "DSpace configuration file (Path: '" + dspaceConfig + "'). \n" + "This has likely occurred because either the file does not exist, or it's permissions \n" + "are set incorrectly, or the path to the configuration file is incorrect. The path to \n" + "the DSpace configuration file is stored in a context variable, 'dspace-config', in \n" + "either the local servlet or global context.\n\n", e);
        }
    }
}