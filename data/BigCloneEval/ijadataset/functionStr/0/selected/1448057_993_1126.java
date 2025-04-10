public class Test {    public static boolean exportAsAGREGA(String zipFilename, String loName, String authorName, String organization, boolean windowed, String gameFilename, AdventureDataControl adventureData) {
        boolean dataSaved = true;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            TransformerFactory tFactory = TransformerFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = null;
            Transformer transformer = null;
            OutputStream fout = null;
            OutputStreamWriter writeFile = null;
            File tempDir = new File(Controller.createTempDirectory().getAbsolutePath());
            for (File tempFile : tempDir.listFiles()) {
                if (tempFile.isDirectory()) tempFile.deleteAll();
                tempFile.delete();
            }
            dataSaved &= writeWebPage(tempDir.getAbsolutePath(), loName, windowed, "es.eucm.eadventure.engine.EAdventureAppletScorm");
            File jarUnsigned = new File(tempDir.getAbsolutePath() + "/eAdventure.zip");
            FileOutputStream mergedFile = new FileOutputStream(jarUnsigned);
            ZipOutputStream os = new ZipOutputStream(mergedFile);
            String manifestText = Writer.defaultManifestFile("es.eucm.eadventure.engine.EAdventureAppletScorm");
            ZipEntry manifestEntry = new ZipEntry("META-INF/MANIFEST.MF");
            ZipEntry manifestEntry2 = new ZipEntry("META-INF/services/javax.xml.parsers.SAXParserFactory");
            ZipEntry manifestEntry3 = new ZipEntry("META-INF/services/javax.xml.parsers.DocumentBuilderFactory");
            os.putNextEntry(manifestEntry);
            os.write(manifestText.getBytes());
            os.putNextEntry(manifestEntry2);
            os.putNextEntry(manifestEntry3);
            os.closeEntry();
            os.flush();
            File.mergeZipAndDirToJar("web/eAdventure_temp.jar", gameFilename, os);
            addNeededLibrariesToJar(os, Controller.getInstance());
            os.close();
            dataSaved &= jarUnsigned.renameTo(new File(tempDir.getAbsolutePath() + "/" + loName + "_unsigned.jar"));
            dataSaved = JARSigner.signJar(authorName, organization, tempDir.getAbsolutePath() + "/" + loName + "_unsigned.jar", tempDir.getAbsolutePath() + "/" + loName + ".jar");
            new File(tempDir.getAbsolutePath() + "/" + loName + "_unsigned.jar").delete();
            db = dbf.newDocumentBuilder();
            doc = db.newDocument();
            Element manifest = null;
            manifest = doc.createElement("manifest");
            manifest.setAttribute("identifier", "eAdventureGame");
            manifest.setAttribute("xmlns", "http://www.imsglobal.org/xsd/imscp_v1p1");
            manifest.setAttribute("xmlns:lomes", "http://ltsc.ieee.org/xsd/LOM");
            manifest.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            manifest.setAttribute("xmlns:adlcp", "http://www.adlnet.org/xsd/adlcp_v1p3");
            manifest.setAttribute("xmlns:imsss", "http://www.imsglobal.org/xsd/imsss");
            manifest.setAttribute("xmlns:adlseq", "http://www.adlnet.org/xsd/adlseq_v1p3");
            manifest.setAttribute("xmlns:adlnav", "http://www.adlnet.org/xsd/adlnav_v1p3");
            manifest.setAttribute("xsi:schemaLocation", "http://www.imsglobal.org/xsd/imscp_v1p1 imscp_v1p1.xsd http://ltsc.ieee.org/xsd/LOM lom.xsd http://www.adlnet.org/xsd/adlcp_v1p3 adlcp_v1p3.xsd http://www.imsglobal.org/xsd/imsss imsss_v1p0.xsd http://www.adlnet.org/xsd/adlseq_v1p3 adlseq_v1p3.xsd http://www.adlnet.org/xsd/adlnav_v1p3 adlnav_v1p3.xsd");
            Element metadata = doc.createElement("metadata");
            Element schema = doc.createElement("schema");
            schema.setTextContent("ADL SCORM");
            metadata.appendChild(schema);
            Element schemaversion = doc.createElement("schemaversion");
            schemaversion.setTextContent("2004 3rd Edition");
            metadata.appendChild(schemaversion);
            Node lomNode = LOMESDOMWriter.buildLOMESDOM(adventureData.getLOMESController());
            doc.adoptNode(lomNode);
            metadata.appendChild(lomNode);
            manifest.appendChild(metadata);
            Element organizations = doc.createElement("organizations");
            organizations.setAttribute("default", ORGANIZATION_IDENTIFIER);
            Element organizationEl = doc.createElement("organization");
            organizationEl.setAttribute("identifier", ORGANIZATION_IDENTIFIER);
            organizationEl.setAttribute("structure", ORGANIZATION_STRUCTURE);
            Node organizationTitleNode = doc.createElement("title");
            organizationTitleNode.setTextContent(adventureData.getTitle());
            organizationEl.appendChild(organizationTitleNode);
            Element itemEl = doc.createElement("item");
            itemEl.setAttribute("identifier", ITEM_IDENTIFIER);
            itemEl.setAttribute("identifierref", RESOURCE_IDENTIFIER);
            itemEl.setAttribute("isvisible", "true");
            Node itemTitleNode = doc.createElement("title");
            itemTitleNode.setTextContent(adventureData.getTitle());
            itemEl.appendChild(itemTitleNode);
            organizationEl.appendChild(itemEl);
            organizations.appendChild(organizationEl);
            manifest.appendChild(organizations);
            Node resources = doc.createElement("resources");
            Element resource = doc.createElement("resource");
            resource.setAttribute("identifier", RESOURCE_IDENTIFIER);
            resource.setAttribute("adlcp:scormType", "sco");
            resource.setAttribute("type", "webcontent");
            resource.setAttribute("href", loName + ".html");
            Element file = doc.createElement("file");
            file.setAttribute("href", loName + ".html");
            resource.appendChild(file);
            Element file2 = doc.createElement("file");
            file2.setAttribute("href", "eadventure.js");
            resource.appendChild(file2);
            Element file3 = doc.createElement("file");
            file3.setAttribute("href", loName + ".jar");
            resource.appendChild(file3);
            Element file4 = doc.createElement("file");
            file4.setAttribute("href", "splashScreen.gif");
            resource.appendChild(file4);
            resources.appendChild(resource);
            manifest.appendChild(resources);
            indentDOM(manifest, 0);
            doc.adoptNode(manifest);
            doc.appendChild(manifest);
            transformer = tFactory.newTransformer();
            fout = new FileOutputStream(tempDir.getAbsolutePath() + "/imsmanifest.xml");
            writeFile = new OutputStreamWriter(fout, "UTF-8");
            transformer.transform(new DOMSource(doc), new StreamResult(writeFile));
            writeFile.close();
            fout.close();
            File.unzipDir("web/Scorm2004AgregaContent.zip", tempDir.getAbsolutePath() + "/");
            File javaScript = new File("web/eadventure.js");
            javaScript.copyTo(new File(tempDir.getAbsolutePath() + "/eadventure.js"));
            File splashScreen = new File("web/splashScreen.gif");
            if (windowed) {
                splashScreen = new File("web/splashScreen_red.gif");
            }
            splashScreen.copyTo(new File(tempDir.getAbsolutePath() + "/splashScreen.gif"));
            File.zipDirectory(tempDir.getAbsolutePath() + "/", zipFilename);
        } catch (IOException exception) {
            Controller.getInstance().showErrorDialog(TC.get("Error.Title"), TC.get("Error.WriteData"));
            ReportDialog.GenerateErrorReport(exception, true, TC.get("Error.WriteData"));
            dataSaved = false;
        } catch (ParserConfigurationException exception) {
            Controller.getInstance().showErrorDialog(TC.get("Error.Title"), TC.get("Error.WriteData"));
            ReportDialog.GenerateErrorReport(exception, true, TC.get("Error.WriteData"));
            dataSaved = false;
        } catch (TransformerConfigurationException exception) {
            Controller.getInstance().showErrorDialog(TC.get("Error.Title"), TC.get("Error.WriteData"));
            ReportDialog.GenerateErrorReport(exception, true, TC.get("Error.WriteData"));
            dataSaved = false;
        } catch (TransformerException exception) {
            Controller.getInstance().showErrorDialog(TC.get("Error.Title"), TC.get("Error.WriteData"));
            ReportDialog.GenerateErrorReport(exception, true, TC.get("Error.WriteData"));
            dataSaved = false;
        }
        return dataSaved;
    }
}