public class Test {    public static boolean zipListFileRecursiveOld(List<File> filesToZip, File zipFileName, String cutPrefix, String excludeRegEx, boolean compress) {
        try {
            byte[] buffer = new byte[18024];
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
            if (!compress) {
                out.setLevel(Deflater.NO_COMPRESSION);
            } else {
                out.setLevel(Deflater.DEFAULT_COMPRESSION);
            }
            for (int i = 0; i < filesToZip.size(); i++) {
                if (excludeRegEx == null || !filesToZip.get(i).getName().contains(excludeRegEx)) {
                    FileInputStream in = new FileInputStream(filesToZip.get(i));
                    String filePath = filesToZip.get(i).getPath();
                    filePath = filePath.substring(cutPrefix.length());
                    System.out.println("Deflating: " + filePath);
                    out.putNextEntry(new ZipEntry(filePath));
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                    out.closeEntry();
                    in.close();
                }
            }
            out.close();
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            return (false);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            return (false);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return (false);
        }
        return (true);
    }
}