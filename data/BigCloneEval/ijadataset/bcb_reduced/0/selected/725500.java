package marubinotto.piggydb.model.ri;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import marubinotto.piggydb.model.Fragment;
import marubinotto.util.Assert;
import marubinotto.util.ZipUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

public class FileRepositoryRI extends FileRepositoryBase {

    private Map<String, byte[]> files = new HashMap<String, byte[]>();

    private Map<String, byte[]> getFiles() {
        return files;
    }

    public void putFile(Fragment fragment) throws Exception {
        Assert.Arg.notNull(fragment, "fragment");
        Assert.Arg.notNull(fragment.id, "fragment.id");
        Assert.Arg.notNull(fragment.fileInput, "fragment.fileInput");
        this.files.put(getFragmentFileKey(fragment), fragment.fileInput.get());
    }

    public void getFile(OutputStream output, Fragment fragment) throws Exception {
        Assert.Arg.notNull(output, "output");
        Assert.Arg.notNull(fragment, "fragment");
        Assert.Arg.notNull(fragment.id, "fragment.id");
        byte[] file = this.files.get(getFragmentFileKey(fragment));
        if (file == null) {
            return;
        }
        output.write(file);
    }

    public int size() throws Exception {
        return this.files.size();
    }

    public void outputAll(String namePrefix, ZipOutputStream zipOut) throws Exception {
        Assert.Arg.notNull(namePrefix, "namePrefix");
        Assert.Arg.notNull(zipOut, "zipOut");
        for (String key : this.files.keySet()) {
            ZipEntry zipEntry = new ZipEntry(namePrefix + key);
            zipOut.putNextEntry(zipEntry);
            zipOut.write(this.files.get(key));
        }
    }

    public void clear() throws Exception {
        this.files.clear();
    }

    public ZipUtils.EntryReader getEntryReader() throws Exception {
        return new ZipUtils.EntryReader() {

            public void readEntry(String name, InputStream input) throws Exception {
                getFiles().put(name, IOUtils.toByteArray(input));
            }
        };
    }
}
