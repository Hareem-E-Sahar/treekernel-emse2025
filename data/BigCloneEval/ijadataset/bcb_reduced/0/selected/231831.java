package net.sourceforge.gpj.cardservices;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CapFile {

    public static final String[] componentNames = { "Header", "Directory", "Import", "Applet", "Class", "Method", "StaticField", "Export", "ConstantPool", "RefLocation", "Descriptor", "Debug" };

    private HashMap<String, byte[]> capComponents = new HashMap<String, byte[]>();

    private String packageName = null;

    private AID packageAID = null;

    private List<AID> appletAIDs = new ArrayList<AID>();

    private List<byte[]> dapBlocks = new ArrayList<byte[]>();

    private List<byte[]> loadTokens = new ArrayList<byte[]>();

    private List<byte[]> installTokens = new ArrayList<byte[]>();

    public CapFile(InputStream in) throws IOException {
        this(in, null);
    }

    public CapFile(InputStream in, String packageName) throws IOException {
        ZipInputStream zip = new ZipInputStream(in);
        Map<String, byte[]> entries = getEntries(zip);
        if (packageName != null) {
            packageName = packageName.replace('.', '/') + "/javacard/";
        } else {
            Iterator<? extends String> it = entries.keySet().iterator();
            String lookFor = "Header.cap";
            while (it.hasNext()) {
                String s = it.next();
                if (s.endsWith(lookFor)) {
                    packageName = s.substring(0, s.lastIndexOf(lookFor));
                    break;
                }
            }
        }
        GPUtil.debug("packagePath: " + packageName);
        this.packageName = packageName.substring(0, packageName.lastIndexOf("/javacard/")).replace('/', '.');
        GPUtil.debug("package: " + this.packageName);
        for (String name : componentNames) {
            String fullName = packageName + name + ".cap";
            byte[] contents = entries.get(fullName);
            capComponents.put(name, contents);
        }
        List<List<byte[]>> tables = new ArrayList<List<byte[]>>();
        tables.add(dapBlocks);
        tables.add(loadTokens);
        tables.add(installTokens);
        String[] names = { "dap", "lt", "it" };
        for (int i = 0; i < names.length; i++) {
            int index = 0;
            while (true) {
                String fullName = "meta-inf/" + packageName.replace('/', '-') + names[i] + (index + 1);
                byte[] contents = entries.get(fullName);
                if (contents == null) break;
                tables.get(i).add(contents);
                index++;
            }
        }
        zip.close();
        byte[] header = capComponents.get("Header");
        int i = 0;
        i++;
        i++;
        i++;
        i += 4;
        i += 2;
        i++;
        i += 2;
        int len = header[i++];
        packageAID = new AID(header, i, len);
        GPUtil.debug("package AID: " + packageAID);
        byte[] applet = capComponents.get("Applet");
        if (applet != null) {
            i = 0;
            i++;
            i++;
            i++;
            int num = applet[i++];
            for (int j = 0; j < num; j++) {
                len = applet[i++];
                appletAIDs.add(new AID(applet, i, len));
                i += len + 2;
            }
            GPUtil.debug("applet AIDs: " + appletAIDs);
        } else {
            GPUtil.debug("No Applet component.");
        }
    }

    private Map<String, byte[]> getEntries(ZipInputStream in) throws IOException {
        Map<String, byte[]> result = new HashMap<String, byte[]>();
        while (true) {
            ZipEntry entry = in.getNextEntry();
            if (entry == null) {
                break;
            }
            if (entry.getName().indexOf("MANIFEST.MF") != -1) {
                continue;
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int c;
            while ((c = in.read(buf)) > 0) bos.write(buf, 0, c);
            result.put(entry.getName(), bos.toByteArray());
        }
        return result;
    }

    public AID getPackageAID() {
        return packageAID;
    }

    public List<AID> getAppletAIDs() {
        List<AID> result = new ArrayList<AID>();
        result.addAll(appletAIDs);
        return result;
    }

    public int getCodeLength(boolean includeDebug) {
        int result = 0;
        for (String name : componentNames) {
            if (!includeDebug && (name.equals("Debug") || name.equals("Descriptor"))) continue;
            byte[] data = capComponents.get(name);
            if (data != null) {
                result += data.length;
            }
        }
        return result;
    }

    private byte[] createHeader(boolean includeDebug) {
        int len = getCodeLength(includeDebug);
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        bo.write((byte) 0xC4);
        if (len < 0x80) {
            bo.write((byte) len);
        } else if (len <= 0xFF) {
            bo.write((byte) 0x81);
            bo.write((byte) len);
        } else if (len <= 0xFFFF) {
            bo.write((byte) 0x82);
            bo.write((byte) ((len & 0xFF00) >> 8));
            bo.write((byte) (len & 0xFF));
        } else {
            bo.write((byte) 0x83);
            bo.write((byte) ((len & 0xFF0000) >> 16));
            bo.write((byte) ((len & 0xFF00) >> 8));
            bo.write((byte) (len & 0xFF));
        }
        return bo.toByteArray();
    }

    public List<byte[]> getLoadBlocks(boolean includeDebug, boolean separateComponents, int blockSize) {
        List<byte[]> blocks = null;
        if (!separateComponents) {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            try {
                bo.write(createHeader(includeDebug));
                bo.write(getRawCode(includeDebug));
            } catch (IOException ioe) {
            }
            blocks = splitArray(bo.toByteArray(), blockSize);
        } else {
            for (String name : componentNames) {
                if (!includeDebug && (name.equals("Debug") || name.equals("Descriptor"))) continue;
                byte[] currentComponent = capComponents.get(name);
                if (currentComponent == null) {
                    continue;
                }
                if (name.equals("Header")) {
                    ByteArrayOutputStream bo = new ByteArrayOutputStream();
                    try {
                        bo.write(createHeader(includeDebug));
                        bo.write(currentComponent);
                    } catch (IOException ioe) {
                    }
                    currentComponent = bo.toByteArray();
                }
                blocks = splitArray(currentComponent, blockSize);
            }
        }
        return blocks;
    }

    private byte[] getRawCode(boolean includeDebug) {
        byte[] result = new byte[getCodeLength(includeDebug)];
        short offset = 0;
        for (String name : componentNames) {
            if (!includeDebug && (name.equals("Debug") || name.equals("Descriptor"))) continue;
            byte[] currentComponent = (byte[]) capComponents.get(name);
            if (currentComponent == null) continue;
            System.arraycopy(currentComponent, 0, result, offset, currentComponent.length);
            offset += currentComponent.length;
        }
        return result;
    }

    public byte[] getLoadFileDataHash(boolean includeDebug) {
        try {
            return MessageDigest.getInstance("SHA1").digest(getRawCode(includeDebug));
        } catch (NoSuchAlgorithmException e) {
            GPUtil.debug("Not possible?");
            return null;
        }
    }

    private List<byte[]> splitArray(byte[] array, int blockSize) {
        List<byte[]> result = new ArrayList<byte[]>();
        int len = array.length;
        int offset = 0;
        int left = len - offset;
        while (left > 0) {
            int currentLen = 0;
            if (left >= blockSize) {
                currentLen = blockSize;
            } else {
                currentLen = left;
            }
            byte[] block = new byte[currentLen];
            System.arraycopy(array, offset, block, 0, currentLen);
            result.add(block);
            left -= currentLen;
            offset += currentLen;
        }
        return result;
    }

    public String dump() {
        String result = "";
        for (String name : componentNames) {
            result = result + name + ".cap:\n";
            byte[] b = (byte[]) capComponents.get(name);
            if (b != null) {
                result = result + GPUtil.byteArrayToString(b) + "\n";
            } else {
                result = result + "(empty)\n";
            }
        }
        List<List<byte[]>> tables = new ArrayList<List<byte[]>>();
        tables.add(dapBlocks);
        tables.add(loadTokens);
        tables.add(installTokens);
        String[] names = { "DAP Blocks", "Load Tokens", "Install Tokens" };
        for (int i = 0; i < names.length; i++) {
            result = result + names[i] + ":\n";
            for (byte[] o : tables.get(i)) {
                result = result + GPUtil.byteArrayToString(o) + "\n";
            }
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        CapFile cp = new CapFile(new FileInputStream(args[0]));
    }
}
