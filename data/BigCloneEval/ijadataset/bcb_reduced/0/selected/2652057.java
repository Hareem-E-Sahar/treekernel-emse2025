package satan.sb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author satan
 * 
 */
public class ReadDict {

    private static int DEFAULT_BUFFER_SIZE = 1024 * 4;

    private String subSrc;

    private int index = 0;

    public void find(File src, Integer deep) throws IOException {
        if (src.isDirectory()) {
            File[] files = src.listFiles();
            for (File file : files) {
                if (deep == 1) {
                    subSrc = file.getAbsolutePath();
                }
                if (file.isDirectory()) {
                    find(file, ++deep);
                    if (file.delete()) {
                        System.out.println("刪除成功！");
                    }
                    deep--;
                } else {
                    if (deep == 2) {
                        file.renameTo(new File(file.getParent() + "\\" + generateName(++index)));
                    }
                    if (deep > 2) {
                        String dest = subSrc + "\\" + generateName(++index);
                        this.copy(file.getAbsolutePath(), dest);
                        if (file.delete()) {
                            System.out.println("刪除成功！");
                        }
                    }
                }
            }
        } else {
            System.err.println("根文件路徑有誤！");
        }
    }

    public File copy(String source, String target) throws IOException {
        FileInputStream input = new FileInputStream(source);
        FileOutputStream output = new FileOutputStream(target);
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        input.close();
        output.close();
        return new File(target);
    }

    public File rename(File file, int index) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        String prefix = file.getParent();
        String name = file.getName();
        String last = name.substring(name.lastIndexOf("."));
        String newName = prefix + "\\" + dateStr + index + last;
        return new File(newName);
    }

    public String generateName(int index) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        return dateStr + index + ".jpg";
    }

    public static void invokeChain() {
    }

    public static void main(String[] args) throws IOException {
        ReadDict rd = new ReadDict();
        rd.find(new File("D:\\test"), 1);
    }
}
