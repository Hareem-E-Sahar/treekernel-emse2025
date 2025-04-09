import java.io.*;
import java.util.*;

public abstract class ConfProcessor {

    public static void parseConfigurationFile(String file, Map settingsMap, Map actionHandlers, Map mimeTypes) {
        Map specialKeyWords = new HashMap();
        specialKeyWords.put("addtype", "1");
        specialKeyWords.put("action", "2");
        BufferedReader config = null;
        try {
            if (new File(file).exists() == true) {
                config = new BufferedReader(new FileReader(file));
                String aLine, keyWord, value;
                while (config.ready()) {
                    aLine = config.readLine();
                    if (aLine != null && aLine.length() > 0) {
                        aLine = aLine.replace('\t', ' ').replace('\"', ' ').trim();
                        if (aLine.length() > 0 && aLine.charAt(0) != '#') {
                            int positionOfSpace = aLine.indexOf(' ');
                            if (positionOfSpace == -1) Misc.putSysMessage(8, "ConfProcessor: error: unknown entry " + aLine + " found in " + file + ". Ignoring."); else {
                                keyWord = aLine.substring(0, positionOfSpace).trim().toLowerCase();
                                value = aLine.substring(positionOfSpace + 1).trim();
                                if (specialKeyWords.get(keyWord) != null) {
                                    switch(Integer.parseInt(specialKeyWords.get(keyWord).toString())) {
                                        case 1:
                                            {
                                                positionOfSpace = value.indexOf(' ');
                                                if (positionOfSpace != -1) {
                                                    String Mime = value.substring(0, positionOfSpace).trim();
                                                    String extvalue = value.substring(positionOfSpace + 1).trim();
                                                    String aValue;
                                                    StringTokenizer extTokened = new StringTokenizer(extvalue);
                                                    while (extTokened.hasMoreTokens() == true) {
                                                        aValue = extTokened.nextToken();
                                                        if (aValue.charAt(0) == '.') aValue = aValue.substring(1);
                                                        mimeTypes.put(aValue, Mime);
                                                    }
                                                }
                                                break;
                                            }
                                        case 2:
                                            {
                                                aLine = new String(value);
                                                positionOfSpace = aLine.indexOf(' ');
                                                if (positionOfSpace != -1) {
                                                    String Mime = aLine.substring(0, positionOfSpace).trim();
                                                    String preProcessor = aLine.substring(positionOfSpace + 1).trim();
                                                    actionHandlers.put(Mime, preProcessor);
                                                }
                                                break;
                                            }
                                    }
                                } else {
                                    settingsMap.put(keyWord, value);
                                }
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            try {
                if (config != null) config.close();
            } catch (IOException e) {
            }
        }
    }

    public static void parseMimeTypes(String file, Map mimeTypes) {
        BufferedReader mimefile = null;
        try {
            if (new File(file).exists() == true) {
                mimefile = new BufferedReader(new FileReader(file));
                String aLine, Mime, value;
                while (mimefile.ready()) {
                    aLine = mimefile.readLine();
                    if (aLine != null && aLine.length() > 0) {
                        aLine = aLine.trim().replace('\t', ' ');
                        if (aLine.length() > 0 && aLine.charAt(0) != '#') {
                            int positionOfSpace = aLine.indexOf(' ');
                            if (positionOfSpace != -1) {
                                Mime = aLine.substring(0, positionOfSpace).trim();
                                value = aLine.substring(positionOfSpace + 1).trim();
                                String aValue;
                                StringTokenizer valueTokened = new StringTokenizer(value);
                                while (valueTokened.hasMoreTokens() == true) {
                                    aValue = valueTokened.nextToken();
                                    mimeTypes.put(aValue, Mime);
                                }
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            try {
                if (mimefile != null) mimefile.close();
            } catch (IOException e) {
            }
        }
    }

    public static int checkConfigurationData(Map configData) {
        if (configData.containsKey("die") == true) {
            Misc.putSysMessage(8, "'Die' found -- " + (String) configData.get("die"));
            return 1;
        }
        if (configData.containsKey("serverroot") == false) {
            Misc.putSysMessage(8, "The ServerRoot directive is missing from the configuration file! -- Cannot continue.");
            return 1;
        } else {
            File serverRoot = new File(configData.get("serverroot").toString());
            if (serverRoot.exists() != true) {
                Misc.putSysMessage(8, "ServerRoot does not exist!");
                return 1;
            }
        }
        if (configData.containsKey("port") == false) {
            Misc.putSysMessage(8, "The Port directive is missing from the configuration file! -- Cannot continue.");
            return 1;
        }
        if (configData.containsKey("servername") == false) {
            Misc.putSysMessage(8, "The ServerName directive is missing from the config file! -- Cannot continue.");
            return 1;
        }
        if (configData.containsKey("documentroot") == false) configData.put("documentroot", "");
        if (configData.containsKey("timeout") == false) configData.put("timeout", "10");
        if (configData.containsKey("maxclients") == false) configData.put("maxclients", "150");
        if (configData.containsKey("directoryindex") == false) configData.put("directoryindex", "index.html index.htm");
        if (configData.containsKey("serveradmin") == false) configData.put("serveradmin", "you@your.address");
        if (configData.containsKey("defaulttype") == false) configData.put("defaulttype", "text/plain");
        if (configData.containsKey("serversignature") == false) configData.put("serversignature", "off");
        if (configData.containsKey("readbuffersize") == true) {
            if ((Integer.parseInt(configData.get("readbuffersize").toString()) <= 0) || (Integer.parseInt(configData.get("readbuffersize").toString()) >= 2097152)) {
                configData.put("readbuffersize", Integer.toString(pws.defaultReadBufferSize()));
                Misc.putSysMessage(8, "checkConfigurationData: restored readBufferSize to default value (" + pws.defaultReadBufferSize() + ")");
            }
        } else configData.put("readbuffersize", Integer.toString(pws.defaultReadBufferSize()));
        if (pws.useLogFiles() == true) {
            if (configData.containsKey("errorlog") != true) configData.put("errorlog", configData.get("serverroot").toString() + "/logs/error.log");
            if (configData.containsKey("accesslog") != true) configData.put("accesslog", configData.get("serverroot").toString() + "/logs/access.log");
            File accessLog = new File(configData.get("accesslog").toString());
            File errorLog = new File(configData.get("errorlog").toString());
            try {
                errorLog.createNewFile();
                accessLog.createNewFile();
                if (errorLog.isFile() && accessLog.isFile()) {
                    pws.errorLog = new BufferedWriter(new FileWriter(errorLog.getPath(), true));
                    pws.accessLog = new BufferedWriter(new FileWriter(accessLog.getPath(), true));
                }
            } catch (IOException e) {
                Misc.putSysMessage(8, "Error while creating/opening log files. Logging disabled.");
                pws.setUseLogFiles(false);
            }
        }
        return 0;
    }

    public static void parseMimeTypes2(String file, Map mimeTypes) {
        BufferedReader mimefile = null;
        try {
            if (new File(file).exists() == true) {
                mimefile = new BufferedReader(new FileReader(file));
                String aLine, Mime, value;
                while (mimefile.ready()) {
                    aLine = mimefile.readLine();
                    if (aLine != null && aLine.length() > 0) {
                        aLine = aLine.trim().replace('\t', ' ');
                        if (aLine.length() > 0 && aLine.charAt(0) != '#') {
                            int positionOfSpace = aLine.indexOf(' ');
                            if (positionOfSpace != -1) {
                                Mime = aLine.substring(0, positionOfSpace).trim();
                                value = aLine.substring(positionOfSpace + 1).trim();
                                mimeTypes.put(Mime, value);
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            try {
                if (mimefile != null) mimefile.close();
            } catch (IOException e) {
            }
        }
    }

    public static void parseConfigurationFile2(String file, Map settingsMap, Map actionHandlers, Map mimeTypes) {
        BufferedReader config = null;
        try {
            if (new File(file).exists() == true) {
                config = new BufferedReader(new FileReader(file));
                String aLine, keyWord, value;
                while (config.ready()) {
                    aLine = config.readLine();
                    if (aLine != null && aLine.length() > 0) {
                        aLine = aLine.replace('\t', ' ').replace('\"', ' ').trim();
                        if (aLine.length() > 0 && aLine.charAt(0) != '#') {
                            int positionOfSpace = aLine.indexOf(' ');
                            keyWord = aLine.substring(0, positionOfSpace).trim().toLowerCase();
                            value = aLine.substring(positionOfSpace + 1).trim();
                            if (keyWord.equals("addtype")) {
                                positionOfSpace = value.indexOf(' ');
                                if (positionOfSpace != -1) {
                                    String Mime = value.substring(0, positionOfSpace).trim();
                                    String extvalue = value.substring(positionOfSpace + 1).trim();
                                    mimeTypes.put(Mime, extvalue);
                                }
                            } else {
                                if (keyWord.equals("action")) {
                                    aLine = new String(value);
                                    positionOfSpace = aLine.indexOf(' ');
                                    if (positionOfSpace != -1) {
                                        String Mime = aLine.substring(0, positionOfSpace).trim();
                                        String preProcessor = aLine.substring(positionOfSpace + 1).trim();
                                        Misc.putSysMessage(6, "Added actionHandler for: " + Mime + " to: " + preProcessor);
                                        actionHandlers.put(Mime, preProcessor);
                                    }
                                } else {
                                    settingsMap.put(keyWord, value);
                                }
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            try {
                if (config != null) config.close();
            } catch (IOException e) {
            }
        }
    }

    public static void dumpConfigurationFile(String file, Map settingsMap, Map actionHandlers, Map mimeTypes) {
        BufferedWriter config = null;
        try {
            if (new File(file).exists() == true) {
                new File((String) (file + ".bak")).delete();
                new File(file).renameTo(new File((String) (file + ".bak")));
                config = new BufferedWriter(new FileWriter(file));
                new File(file).createNewFile();
                Iterator keyIter = settingsMap.keySet().iterator();
                String currentKey;
                while (keyIter.hasNext()) {
                    currentKey = (String) keyIter.next();
                    config.write(currentKey + " " + (String) settingsMap.get(currentKey));
                    config.newLine();
                }
            }
        } catch (IOException e) {
        } finally {
            try {
                if (config != null) config.close();
            } catch (IOException e) {
            }
        }
    }
}
