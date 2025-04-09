public class Test {    String fileWriter(File file, JList InstanceList) throws IOException {
        useAppletJS = JmolViewer.checkOption(viewer, "webMakerCreateJS");
        String datadirPath = file.getPath().replace('\\', '/');
        String datadirName = file.getName();
        String fileName;
        if (datadirName.indexOf(".htm") < 0) {
            File f = new File(datadirPath + ".html");
            if (f.exists()) {
                datadirName += ".html";
                file = f;
            } else if ((f = new File(datadirPath + ".htm")).exists()) {
                datadirName += ".htm";
                file = f;
            }
        }
        if (datadirName.indexOf(".htm") > 0) {
            fileName = datadirName;
            datadirPath = file.getParent();
            file = new File(datadirPath);
            datadirName = file.getName();
        } else {
            fileName = datadirName + ".html";
        }
        datadirPath = datadirPath.replace('\\', '/');
        fileName = datadirPath + "/" + fileName;
        boolean made_datadir = (file.exists() && file.isDirectory() || file.mkdir());
        DefaultListModel listModel = (DefaultListModel) InstanceList.getModel();
        LogPanel.log("");
        if (made_datadir) {
            LogPanel.log(GT._("Using directory {0}", datadirPath));
            LogPanel.log("  " + GT._("adding {0}", "JmolPopIn.js"));
            try {
                viewer.writeTextFile(datadirPath + "/JmolPopIn.js", WebExport.getResourceString(this, "JmolPopIn.js"));
            } catch (IOException IOe) {
                throw IOe;
            }
            for (int i = 0; i < listModel.getSize(); i++) {
                JmolInstance thisInstance = (JmolInstance) (listModel.getElementAt(i));
                String javaname = thisInstance.javaname;
                String script = thisInstance.script;
                LogPanel.log("  ...jmolApplet" + i);
                LogPanel.log("      ..." + GT._("adding {0}", javaname + ".png"));
                try {
                    thisInstance.movepict(datadirPath);
                } catch (IOException IOe) {
                    throw IOe;
                }
                List<String> filesToCopy = new ArrayList<String>();
                String localPath = localAppletPath.getText();
                if (localPath.equals(".") || remoteAppletPath.getText().equals(".")) {
                    filesToCopy.add(localPath + "/Jmol.js");
                    filesToCopy.add(localPath + "/JmolApplet.jar");
                }
                FileManager.getFileReferences(script, filesToCopy);
                List<String> copiedFileNames = new ArrayList<String>();
                int nFiles = filesToCopy.size();
                for (int iFile = 0; iFile < nFiles; iFile++) {
                    String newName = copyBinaryFile(filesToCopy.get(iFile), datadirPath);
                    copiedFileNames.add(newName.substring(newName.lastIndexOf('/') + 1));
                }
                script = TextFormat.replaceQuotedStrings(script, filesToCopy, copiedFileNames);
                LogPanel.log("      ..." + GT._("adding {0}", javaname + ".spt"));
                viewer.writeTextFile(datadirPath + "/" + javaname + ".spt", script);
            }
            String html = WebExport.getResourceString(this, panelName + "_template");
            html = fixHtml(html);
            String jsStr = "";
            BitSet whichWidgets = allSelectedWidgets();
            for (int i = 0; i < nWidgets; i++) {
                if (whichWidgets.get(i)) {
                    String scriptFileName = theWidgets.widgetList[i].getJavaScriptFileName();
                    if (!scriptFileName.equalsIgnoreCase("none")) {
                        jsStr += "\n<script src=\"" + scriptFileName + "\" type=\"text/javascript\"></script>";
                        LogPanel.log("  " + GT._("adding {0}", scriptFileName));
                        viewer.writeTextFile(datadirPath + "/" + scriptFileName + "", WebExport.getResourceString(this, scriptFileName));
                    }
                    String[] supportFileNames = theWidgets.widgetList[i].getSupportFileNames();
                    int nFiles = supportFileNames.length;
                    if (nFiles != 0) {
                        for (int fileN = 0; fileN < nFiles; fileN++) {
                            String inFile = supportFileNames[fileN];
                            String outFile = inFile;
                            if ((inFile.lastIndexOf("/")) != -1) {
                                outFile = inFile.substring((inFile.lastIndexOf("/") + 1));
                            }
                            URL fileURL = WebExport.getResource(this, inFile);
                            if (fileURL == null) {
                                LogPanel.log("    " + GT._("Unable to load resource {0}", inFile));
                                errCount += 1;
                            } else {
                                InputStream is = fileURL.openConnection().getInputStream();
                                FileOutputStream os = new FileOutputStream(datadirPath + "/" + outFile);
                                int temp = is.read();
                                while (temp != -1) {
                                    os.write(temp);
                                    temp = is.read();
                                }
                                os.flush();
                                os.close();
                                LogPanel.log("  " + GT._("adding {0}", outFile));
                            }
                        }
                    }
                }
            }
            html = TextFormat.simpleReplace(html, "@WIDGETJSFILES@", jsStr);
            appletInfoDivs = "";
            StringBuffer appletDefs = new StringBuffer();
            if (!useAppletJS) htmlAppletTemplate = WebExport.getResourceString(this, panelName + "_template2");
            for (int i = 0; i < listModel.getSize(); i++) html = getAppletDefs(i, html, appletDefs, (JmolInstance) listModel.getElementAt(i));
            html = TextFormat.simpleReplace(html, "@AUTHOR@", GT.escapeHTML(pageAuthorName.getText()));
            html = TextFormat.simpleReplace(html, "@TITLE@", GT.escapeHTML(webPageTitle.getText()));
            html = TextFormat.simpleReplace(html, "@REMOTEAPPLETPATH@", remoteAppletPath.getText());
            html = TextFormat.simpleReplace(html, "@LOCALAPPLETPATH@", localAppletPath.getText());
            html = TextFormat.simpleReplace(html, "@DATADIRNAME@", datadirName);
            if (appletInfoDivs.length() > 0) appletInfoDivs = "\n<div style='display:none'>\n" + appletInfoDivs + "\n</div>\n";
            String str = appletDefs.toString();
            if (useAppletJS) str = "<script type='text/javascript'>\n" + str + "\n</script>";
            html = TextFormat.simpleReplace(html, "@APPLETINFO@", appletInfoDivs);
            html = TextFormat.simpleReplace(html, "@APPLETDEFS@", str);
            html = TextFormat.simpleReplace(html, "@CREATIONDATA@", GT.escapeHTML(WebExport.TimeStamp_WebLink()));
            html = TextFormat.simpleReplace(html, "@AUTHORDATA@", GT.escapeHTML(GT._("Based on template by A. Herr&#x00E1;ez as modified by J. Gutow")));
            html = TextFormat.simpleReplace(html, "@LOGDATA@", "<pre>\n" + LogPanel.getText() + "\n</pre>\n");
            LogPanel.log("      ..." + GT._("creating {0}", fileName));
            viewer.writeTextFile(fileName, html);
        } else {
            IOException IOe = new IOException("Error creating directory: " + datadirPath);
            throw IOe;
        }
        return fileName;
    }
}