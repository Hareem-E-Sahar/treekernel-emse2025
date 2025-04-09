package cn.edu.dutir.indri;

import java.io.File;
import java.io.IOException;
import lemurproject.indri.IndexEnvironment;
import lemurproject.indri.IndexStatus;

public class IndriIncrementalIndexer {

    class IndexStatusPrinter extends IndexStatus {

        public void status(int code, String documentFile, String error, int documentsIndexed, int documentsSeen) {
            if (code == action_code.FileOpen.swigValue()) {
                System.out.println("Documents: " + documentsIndexed);
                System.out.println("Opened " + documentFile);
            } else if (code == action_code.FileSkip.swigValue()) {
                System.out.println("Skipped " + documentFile);
            } else if (code == action_code.FileError.swigValue()) {
                System.out.println("Error in " + documentFile + " : " + error);
            } else if (code == action_code.DocumentCount.swigValue()) {
                if ((documentsIndexed % 500) == 0) System.out.println("Documents: " + documentsIndexed);
            } else if (code == action_code.FileClose.swigValue()) {
                System.out.println("Closed " + documentFile);
            }
        }
    }

    private IndexEnvironment mIndexEnv = new IndexEnvironment();

    private String mIndexPath;

    private String mFieldNames[] = { "docno", "text" };

    private long mMemory = 512 * 1024 * 1024;

    private String mStemmer = "porter";

    private boolean mIndexStatus = true;

    private static final String DEFALUT_FILE_CLASS = "trectext";

    public IndriIncrementalIndexer(String indexPath, boolean create) {
        this(indexPath, create, true);
    }

    public IndriIncrementalIndexer(String indexPath, boolean create, boolean verbose) {
        mIndexPath = indexPath;
        mIndexStatus = verbose;
        try {
            if (create) {
                delete(indexPath);
            }
            if (mIndexStatus) {
                mIndexEnv.create(mIndexPath, new IndexStatusPrinter());
            } else {
                mIndexEnv.create(mIndexPath);
            }
            setMemory(mMemory);
            setStemmer(mStemmer);
            setIndexedFields(mFieldNames);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void delete(String path) {
        File fileOrDirectory = new File(path);
        if (fileOrDirectory.isDirectory()) {
            File files[] = fileOrDirectory.listFiles();
            for (File file : files) {
                delete(file.getAbsolutePath());
            }
            fileOrDirectory.delete();
        } else {
            fileOrDirectory.delete();
        }
    }

    public void setIndexPath(String indexPath) {
        mIndexPath = indexPath;
        try {
            mIndexEnv.close();
            mIndexEnv.open(mIndexPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getIndexPath() {
        return mIndexPath;
    }

    public long getMemory() {
        return mMemory;
    }

    public void setMemory(long memory) {
        mMemory = memory;
        try {
            mIndexEnv.setMemory(mMemory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] getIndexedFields() {
        return mFieldNames;
    }

    public void setIndexedFields(String[] fieldNames) {
        mFieldNames = fieldNames;
        try {
            mIndexEnv.setIndexedFields(fieldNames);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getStemmer() {
        return mStemmer;
    }

    public void setStemmer(String stemmer) {
        mStemmer = stemmer;
        try {
            mIndexEnv.setStemmer(mStemmer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int documentsIndexed() {
        try {
            return mIndexEnv.documentsIndexed();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setindexStatus(boolean indexStatus) {
        mIndexStatus = indexStatus;
    }

    public void add(File file) {
        add(file, DEFALUT_FILE_CLASS);
    }

    public void add(File file, String fileClass) {
        if (file.isDirectory()) {
            addDirectory(file, fileClass);
        } else {
            addOrdinaryFile(file, fileClass);
        }
    }

    public void addDirectory(File dir, String fileClass) {
        File files[] = dir.listFiles();
        for (File file : files) {
            add(file, fileClass);
        }
    }

    public void addOrdinaryFile(File file, String fileClass) {
        try {
            mIndexEnv.addFile(file.getCanonicalPath(), fileClass);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            mIndexEnv.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        String indexPath = "d:/testDir";
        File in = new File("E:/CWT200g/result_cwt200g_3");
        IndriIncrementalIndexer indexer = new IndriIncrementalIndexer(indexPath, true);
        System.out.println("Total documents indexed: " + indexer.documentsIndexed());
        indexer.add(in);
        indexer.close();
    }
}
