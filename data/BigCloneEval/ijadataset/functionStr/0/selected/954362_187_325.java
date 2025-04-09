public class Test {    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        this.monitor = monitor;
        runResult = "";
        String address = Activator.getDefault().getPreferenceStore().getString("WEBSHOP_URL");
        String user = Activator.getDefault().getPreferenceStore().getString("WEBSHOP_USER");
        String password = Activator.getDefault().getPreferenceStore().getString("WEBSHOP_PASSWORD");
        Integer maxProducts = Activator.getDefault().getPreferenceStore().getInt("WEBSHOP_MAX_PRODUCTS");
        Boolean onlyModifiedProducts = Activator.getDefault().getPreferenceStore().getBoolean("WEBSHOP_ONLY_MODIFIED_PRODUCTS");
        useEANasItemNr = Activator.getDefault().getPreferenceStore().getBoolean("WEBSHOP_USE_EAN_AS_ITEMNR");
        Boolean useAuthorization = Activator.getDefault().getPreferenceStore().getBoolean("WEBSHOP_AUTHORIZATION_ENABLED");
        String authorizationUser = Activator.getDefault().getPreferenceStore().getString("WEBSHOP_AUTHORIZATION_USER");
        String authorizationPassword = Activator.getDefault().getPreferenceStore().getString("WEBSHOP_AUTHORIZATION_PASSWORD");
        if (address.isEmpty()) {
            runResult = _("Web shop URL is not set.");
            return;
        }
        if (!address.toLowerCase().startsWith("http://") && !address.toLowerCase().startsWith("https://") && !address.toLowerCase().startsWith("file://")) address = "http://" + address;
        readOrdersToSynchronize();
        factory = DocumentBuilderFactory.newInstance();
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        try {
            worked = 0;
            URLConnection conn = null;
            monitor.beginTask(_("Connection to web shop"), 100);
            monitor.subTask(_("Connected to:") + " " + address);
            setProgress(10);
            URL url = new URL(address);
            conn = url.openConnection();
            conn.setDoInput(true);
            conn.setConnectTimeout(4000);
            if (!address.toLowerCase().startsWith("file://")) {
                conn.setDoOutput(true);
                if (useAuthorization) {
                    String encodedPassword = Base64Coder.encodeString(authorizationUser + ":" + authorizationPassword);
                    conn.setRequestProperty("Authorization", "Basic " + encodedPassword);
                }
                OutputStream outputStream = null;
                outputStream = conn.getOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                setProgress(20);
                String postString = "username=" + URLEncoder.encode(user, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8");
                String actionString = "";
                if (getProducts) actionString += "_products";
                if (getOrders) actionString += "_orders";
                if (!actionString.isEmpty()) actionString = "&action=get" + actionString;
                postString += actionString;
                postString += "&setstate=" + orderstosynchronize.toString();
                if (maxProducts > 0) {
                    postString += "&maxproducts=" + maxProducts.toString();
                }
                if (onlyModifiedProducts) {
                    String lasttime = Data.INSTANCE.getProperty("lastwebshopimport", "");
                    if (!lasttime.isEmpty()) postString += "&lasttime=" + lasttime.toString();
                }
                writer.write(postString);
                writer.flush();
                writer.close();
            }
            String line;
            setProgress(30);
            importXMLContent = "";
            InterruptConnection interruptConnection = new InterruptConnection(conn);
            new Thread(interruptConnection).start();
            while (!monitor.isCanceled() && !interruptConnection.isFinished() && !interruptConnection.isError()) ;
            if (!interruptConnection.isFinished()) {
                ((HttpURLConnection) conn).disconnect();
                if (interruptConnection.isError()) {
                    runResult = _("Error while connecting to webserver.");
                }
                return;
            }
            if (interruptConnection.isError()) {
                ((HttpURLConnection) conn).disconnect();
                runResult = _("Error reading web shop data.");
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(interruptConnection.getInputStream()));
            monitor.subTask(_("Loading Data"));
            double progress = worked;
            String filename = Activator.getDefault().getPreferenceStore().getString("GENERAL_WORKSPACE");
            File logFile = null;
            BufferedWriter bos = null;
            if (!filename.isEmpty()) {
                filename += "/Log/";
                File directory = new File(filename);
                if (!directory.exists()) directory.mkdirs();
                filename += "WebShopImport.log";
                logFile = new File(filename);
                if (logFile.exists()) logFile.delete();
                bos = new BufferedWriter(new FileWriter(logFile, true));
            }
            StringBuffer sb = new StringBuffer();
            while (((line = reader.readLine()) != null) && (!monitor.isCanceled())) {
                sb.append(line);
                sb.append("\n");
                progress += (50 - progress) * 0.01;
                setProgress((int) progress);
            }
            importXMLContent = sb.toString();
            if (bos != null) bos.write(importXMLContent);
            if (bos != null) bos.close();
            if (!monitor.isCanceled()) {
                if (!importXMLContent.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {
                    runResult = _("No webshop data:") + "\n" + address + importXMLContent;
                    return;
                }
                ByteArrayInputStream importInputStream = new ByteArrayInputStream(importXMLContent.getBytes());
                document = builder.parse(importInputStream);
                NodeList ndList = document.getElementsByTagName("webshopexport");
                if (ndList.getLength() != 0) {
                    orderstosynchronize = new Properties();
                } else {
                    runResult = importXMLContent;
                }
                ndList = document.getElementsByTagName("error");
                if (ndList.getLength() > 0) {
                    runResult = ndList.item(0).getTextContent();
                }
            } else {
            }
            reader.close();
            if (runResult.isEmpty()) interpretWebShopData(monitor);
            String now = DataUtils.DateAsISO8601String();
            Data.INSTANCE.setProperty("lastwebshopimport", now);
            monitor.done();
        } catch (SAXException e) {
            runResult = "Error parsing XML content:\n" + e.getLocalizedMessage() + "\n" + importXMLContent;
        } catch (Exception e) {
            runResult = _("Error opening:") + "\n" + address + "\n";
            runResult += "Message:" + e.getLocalizedMessage() + "\n";
            if (e.getStackTrace().length > 0) runResult += "Trace:" + e.getStackTrace()[0].toString() + "\n";
            if (!importXMLContent.isEmpty()) runResult += "\n\n" + importXMLContent;
        }
    }
}