package cn.webwheel.utils.fileupload;

import cn.webwheel.Filter;
import cn.webwheel.FilterChain;
import cn.webwheel.WebContext;
import cn.webwheel.di.Key;
import cn.webwheel.di.NativeProvider;
import cn.webwheel.di.Provider;
import cn.webwheel.di.utils.RichContainer;
import cn.webwheel.di.utils.TypeLiteral;
import cn.webwheel.utils.DIFilter;
import cn.webwheel.utils.WebParam;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;

public class FileUploadFilter implements Filter {

    /**
     * request中存储文件上传信息的attribute名字
     */
    public static final String WFU = "webwheel.wfu";

    private int fileSizeMax;

    private int sizeMax;

    private String charset;

    private File tempDir;

    private RichContainer container;

    private DIFilter diFilter;

    public FileUploadFilter(RichContainer container, DIFilter diFilter, int fileSizeMax, int sizeMax, String charset, File tempDir) {
        this.container = container;
        this.diFilter = diFilter;
        this.fileSizeMax = fileSizeMax;
        this.sizeMax = sizeMax;
        this.charset = charset;
        this.tempDir = tempDir;
    }

    @SuppressWarnings("unchecked")
    public void init() throws Exception {
        container.bind(new Key<Map<String, List<FileInfo>>, WebParam>(new TypeLiteral<Map<String, List<FileInfo>>>() {
        }.getType(), WebParam.class), new Provider<Map<String, List<FileInfo>>, WebParam>() {

            public Map<String, List<FileInfo>> get(Key<Map<String, List<FileInfo>>, WebParam> key, WebParam an, String sign, NativeProvider<?> nativeProvider) {
                HttpServletRequest request = container.getInstance(HttpServletRequest.class);
                if (request == null) return null;
                return (Map<String, List<FileInfo>>) request.getAttribute(WFU);
            }
        });
        container.bind(new Key<Map<String, List<File>>, WebParam>(new TypeLiteral<Map<String, List<File>>>() {
        }.getType(), WebParam.class), new Provider<Map<String, List<File>>, WebParam>() {

            public Map<String, List<File>> get(Key<Map<String, List<File>>, WebParam> key, WebParam an, String sign, NativeProvider<?> nativeProvider) {
                HttpServletRequest request = container.getInstance(HttpServletRequest.class);
                if (request == null) return null;
                Map<String, List<FileInfo>> map = (Map<String, List<FileInfo>>) request.getAttribute(WFU);
                if (map == null) return null;
                Map<String, List<File>> nmap = new HashMap<String, List<File>>(map.size());
                for (Map.Entry<String, List<FileInfo>> entry : map.entrySet()) {
                    List<File> list = new ArrayList<File>(entry.getValue().size());
                    for (FileInfo info : entry.getValue()) {
                        list.add(info.getFile());
                    }
                    nmap.put(entry.getKey(), list);
                }
                return nmap;
            }
        });
        container.bind(new Key<Map<String, FileInfo[]>, WebParam>(new TypeLiteral<Map<String, FileInfo[]>>() {
        }.getType(), WebParam.class), new Provider<Map<String, FileInfo[]>, WebParam>() {

            public Map<String, FileInfo[]> get(Key<Map<String, FileInfo[]>, WebParam> key, WebParam an, String sign, NativeProvider<?> nativeProvider) {
                HttpServletRequest request = container.getInstance(HttpServletRequest.class);
                if (request == null) return null;
                Map<String, List<FileInfo>> map = (Map<String, List<FileInfo>>) request.getAttribute(WFU);
                if (map == null) return null;
                Map<String, FileInfo[]> nmap = new HashMap<String, FileInfo[]>(map.size());
                for (Map.Entry<String, List<FileInfo>> entry : map.entrySet()) {
                    nmap.put(entry.getKey(), entry.getValue().toArray(new FileInfo[entry.getValue().size()]));
                }
                return nmap;
            }
        });
        container.bind(new Key<Map<String, File[]>, WebParam>(new TypeLiteral<Map<String, File[]>>() {
        }.getType(), WebParam.class), new Provider<Map<String, File[]>, WebParam>() {

            public Map<String, File[]> get(Key<Map<String, File[]>, WebParam> key, WebParam an, String sign, NativeProvider<?> nativeProvider) {
                HttpServletRequest request = container.getInstance(HttpServletRequest.class);
                if (request == null) return null;
                Map<String, List<FileInfo>> map = (Map<String, List<FileInfo>>) request.getAttribute(WFU);
                if (map == null) return null;
                Map<String, File[]> nmap = new HashMap<String, File[]>(map.size());
                for (Map.Entry<String, List<FileInfo>> entry : map.entrySet()) {
                    File[] list = new File[entry.getValue().size()];
                    for (int i = 0; i < list.length; i++) {
                        list[i] = entry.getValue().get(i).getFile();
                    }
                    nmap.put(entry.getKey(), list);
                }
                return nmap;
            }
        });
        container.bind(new Key<List<FileInfo>, WebParam>(new TypeLiteral<List<FileInfo>>() {
        }.getType(), WebParam.class), new Provider<List<FileInfo>, WebParam>() {

            public List<FileInfo> get(Key<List<FileInfo>, WebParam> key, WebParam an, String sign, NativeProvider<?> nativeProvider) {
                HttpServletRequest request = container.getInstance(HttpServletRequest.class);
                if (request == null) return null;
                Map<String, List<FileInfo>> map = (Map<String, List<FileInfo>>) request.getAttribute(WFU);
                if (map == null) return null;
                return map.get(sign);
            }
        });
        container.bind(new Key<List<File>, WebParam>(new TypeLiteral<List<File>>() {
        }.getType(), WebParam.class), new Provider<List<File>, WebParam>() {

            public List<File> get(Key<List<File>, WebParam> key, WebParam an, String sign, NativeProvider<?> nativeProvider) {
                HttpServletRequest request = container.getInstance(HttpServletRequest.class);
                if (request == null) return null;
                Map<String, List<FileInfo>> map = (Map<String, List<FileInfo>>) request.getAttribute(WFU);
                if (map == null) return null;
                List<FileInfo> list = map.get(sign);
                if (list == null) return null;
                List<File> nlist = new ArrayList<File>(list.size());
                for (FileInfo info : list) {
                    nlist.add(info.getFile());
                }
                return nlist;
            }
        });
        container.bind(new Key<FileInfo[], WebParam>(FileInfo[].class, WebParam.class), new Provider<FileInfo[], WebParam>() {

            public FileInfo[] get(Key<FileInfo[], WebParam> key, WebParam an, String sign, NativeProvider<?> nativeProvider) {
                HttpServletRequest request = container.getInstance(HttpServletRequest.class);
                if (request == null) return null;
                Map<String, List<FileInfo>> map = (Map<String, List<FileInfo>>) request.getAttribute(WFU);
                if (map == null) return null;
                List<FileInfo> list = map.get(sign);
                if (list == null) return null;
                FileInfo[] infos = new FileInfo[list.size()];
                for (int i = 0; i < infos.length; i++) {
                    infos[i] = list.get(i);
                }
                return infos;
            }
        });
        container.bind(new Key<File[], WebParam>(File[].class, WebParam.class), new Provider<File[], WebParam>() {

            public File[] get(Key<File[], WebParam> key, WebParam an, String sign, NativeProvider<?> nativeProvider) {
                HttpServletRequest request = container.getInstance(HttpServletRequest.class);
                if (request == null) return null;
                Map<String, List<FileInfo>> map = (Map<String, List<FileInfo>>) request.getAttribute(WFU);
                if (map == null) return null;
                List<FileInfo> list = map.get(sign);
                if (list == null) return null;
                File[] nlist = new File[list.size()];
                for (int i = 0; i < nlist.length; i++) {
                    nlist[i] = list.get(i).getFile();
                }
                return nlist;
            }
        });
        container.bind(new Key<FileInfo, WebParam>(FileInfo.class, WebParam.class), new Provider<FileInfo, WebParam>() {

            public FileInfo get(Key<FileInfo, WebParam> key, WebParam an, String sign, NativeProvider<?> nativeProvider) {
                HttpServletRequest request = container.getInstance(HttpServletRequest.class);
                if (request == null) return null;
                Map<String, List<FileInfo>> map = (Map<String, List<FileInfo>>) request.getAttribute(WFU);
                if (map == null) return null;
                List<FileInfo> list = map.get(sign);
                if (list == null) return null;
                return list.get(0);
            }
        });
        container.bind(new Key<File, WebParam>(File.class, WebParam.class), new Provider<File, WebParam>() {

            public File get(Key<File, WebParam> key, WebParam an, String sign, NativeProvider<?> nativeProvider) {
                HttpServletRequest request = container.getInstance(HttpServletRequest.class);
                if (request == null) return null;
                Map<String, List<FileInfo>> map = (Map<String, List<FileInfo>>) request.getAttribute(WFU);
                if (map == null) return null;
                List<FileInfo> list = map.get(sign);
                if (list == null) return null;
                return list.get(0).getFile();
            }
        });
        container.bind(new Key<byte[], WebParam>(byte[].class, WebParam.class), new Provider<byte[], WebParam>() {

            public byte[] get(Key<byte[], WebParam> webParamKey, WebParam an, String sign, NativeProvider<?> nativeProvider) {
                HttpServletRequest request = container.getInstance(HttpServletRequest.class);
                if (request == null) return null;
                Map<String, List<FileInfo>> map = (Map<String, List<FileInfo>>) request.getAttribute(WFU);
                if (map == null) return null;
                List<FileInfo> list = map.get(sign);
                if (list == null) return null;
                File file = list.get(0).getFile();
                try {
                    FileInputStream fis = new FileInputStream(file);
                    byte[] data = new byte[(int) file.length()];
                    try {
                        fis.read(data);
                        return data;
                    } finally {
                        fis.close();
                    }
                } catch (IOException e) {
                    return null;
                }
            }
        });
        container.bind(new Key<byte[][], WebParam>(byte[][].class, WebParam.class), new Provider<byte[][], WebParam>() {

            public byte[][] get(Key<byte[][], WebParam> webParamKey, WebParam an, String sign, NativeProvider<?> nativeProvider) {
                HttpServletRequest request = container.getInstance(HttpServletRequest.class);
                if (request == null) return null;
                Map<String, List<FileInfo>> map = (Map<String, List<FileInfo>>) request.getAttribute(WFU);
                if (map == null) return null;
                List<FileInfo> list = map.get(sign);
                if (list == null) return null;
                byte[][] data = new byte[list.size()][];
                for (int i = 0; i < data.length; i++) {
                    File file = list.get(i).getFile();
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        data[i] = new byte[(int) file.length()];
                        try {
                            fis.read(data[i]);
                        } finally {
                            fis.close();
                        }
                    } catch (IOException e) {
                        data[i] = null;
                    }
                }
                return data;
            }
        });
        container.bind(new Key<List<byte[]>, WebParam>(new TypeLiteral<List<byte[]>>() {
        }.getType(), WebParam.class), new Provider<List<byte[]>, WebParam>() {

            public List<byte[]> get(Key<List<byte[]>, WebParam> key, WebParam an, String sign, NativeProvider<?> nativeProvider) {
                byte[][] data = container.getInstance(new Key<byte[][], WebParam>(byte[][].class, WebParam.class), an, sign);
                if (data == null) return null;
                return Arrays.asList(data);
            }
        });
        container.bind(new Key<InputStream[], WebParam>(InputStream[].class, WebParam.class), new Provider<InputStream[], WebParam>() {

            @Override
            public InputStream[] get(Key<InputStream[], WebParam> inputStreamWebParamKey, WebParam an, String sign, NativeProvider<?> nativeProvider) {
                HttpServletRequest request = container.getInstance(HttpServletRequest.class);
                if (request == null) return null;
                Map<String, List<FileInfo>> map = (Map<String, List<FileInfo>>) request.getAttribute(WFU);
                if (map == null) return null;
                List<FileInfo> list = map.get(sign);
                InputStream[] is = new InputStream[list.size()];
                for (int i = 0; i < is.length; i++) {
                    is[i] = list.get(i).getStream();
                }
                return is;
            }
        });
        container.bind(new Key<InputStream, WebParam>(InputStream.class, WebParam.class), new Provider<InputStream, WebParam>() {

            @Override
            public InputStream get(Key<InputStream, WebParam> inputStreamWebParamKey, WebParam an, String sign, NativeProvider<?> nativeProvider) {
                HttpServletRequest request = container.getInstance(HttpServletRequest.class);
                if (request == null) return null;
                Map<String, List<FileInfo>> map = (Map<String, List<FileInfo>>) request.getAttribute(WFU);
                if (map == null) return null;
                List<FileInfo> list = map.get(sign);
                if (list.isEmpty()) return null;
                return list.get(0).getStream();
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean process(FilterChain chain) throws Exception {
        HttpServletRequest request = chain.getWebContext().getRequest();
        if (!ServletFileUpload.isMultipartContent(request)) return chain.go();
        ServletFileUpload fileUpload = new ServletFileUpload();
        int sizeMax = this.sizeMax;
        String charset = this.charset;
        int fileSizeMax = this.fileSizeMax;
        FileUploadSettings settings = chain.getActionClass().getAnnotation(FileUploadSettings.class);
        if (settings != null) {
            sizeMax = settings.sizeMax();
            charset = settings.charset();
            fileSizeMax = settings.fileSizeMax();
        }
        if (sizeMax > 0) {
            fileUpload.setSizeMax(sizeMax);
        }
        if (charset != null) {
            fileUpload.setHeaderEncoding(charset);
        }
        if (fileSizeMax > 0) {
            fileUpload.setFileSizeMax(fileSizeMax);
        }
        Map<String, List<String>> params = new HashMap<String, List<String>>();
        ThreadLocal<WebContext> threadLocal = diFilter.getThreadLocal();
        WebContext ctx = threadLocal.get();
        threadLocal.set(new WebContextWrapper(ctx, params));
        Map<String, List<FileInfo>> fileInfoMap = new HashMap<String, List<FileInfo>>();
        try {
            FileItemIterator it;
            try {
                it = fileUpload.getItemIterator(request);
            } catch (IOException e) {
                return true;
            } catch (FileUploadException e) {
                if (FileUploadExceptionAware.class.isAssignableFrom(chain.getActionClass())) {
                    ((FileUploadExceptionAware) chain.getAction()).occur(e);
                    return chain.go();
                } else {
                    throw e;
                }
            }
            loop: while (true) {
                FileItemStream item;
                try {
                    if (!it.hasNext()) break;
                    item = it.next();
                } catch (IOException e) {
                    return true;
                } catch (FileUploadException e) {
                    if (FileUploadExceptionAware.class.isAssignableFrom(chain.getActionClass())) {
                        ((FileUploadExceptionAware) chain.getAction()).occur(e);
                        break;
                    } else {
                        throw e;
                    }
                }
                String name = item.getFieldName();
                if (item.isFormField()) {
                    List<String> list = params.get(name);
                    if (list == null) params.put(name, list = new ArrayList<String>(1));
                    String s;
                    try {
                        s = Streams.asString(item.openStream(), charset == null ? "utf-8" : charset);
                    } catch (IOException e) {
                        return true;
                    }
                    list.add(s);
                } else if (!item.getName().isEmpty()) {
                    InputStream is;
                    try {
                        is = item.openStream();
                    } catch (IOException e) {
                        return true;
                    }
                    File file = File.createTempFile("wfu", null, tempDir);
                    file.deleteOnExit();
                    List<FileInfo> list = fileInfoMap.get(name);
                    if (list == null) fileInfoMap.put(name, list = new ArrayList<FileInfo>(1));
                    FileInfoImpl fileInfo = new FileInfoImpl(file);
                    list.add(fileInfo);
                    FileOutputStream fos = new FileOutputStream(file);
                    byte[] buf = new byte[8192];
                    try {
                        while (true) {
                            int rd;
                            try {
                                rd = is.read(buf);
                                if (rd == -1) break;
                            } catch (FileUploadBase.FileUploadIOException e) {
                                FileUploadException fue = (FileUploadException) e.getCause();
                                if (FileUploadExceptionAware.class.isAssignableFrom(chain.getActionClass())) {
                                    ((FileUploadExceptionAware) chain.getAction()).occur(fue);
                                    break loop;
                                } else {
                                    throw fue;
                                }
                            } catch (IOException e) {
                                return true;
                            }
                            fos.write(buf, 0, rd);
                        }
                    } finally {
                        try {
                            fos.close();
                        } catch (IOException e) {
                        }
                    }
                    fileInfo.fileName = item.getName();
                    fileInfo.contentType = item.getContentType();
                }
            }
            request.setAttribute(WFU, fileInfoMap);
            return chain.go();
        } finally {
            threadLocal.set(ctx);
            for (Map.Entry<String, List<FileInfo>> entry : fileInfoMap.entrySet()) {
                for (FileInfo info : entry.getValue()) {
                    FileInputStream fis = ((FileInfoImpl) info).fis;
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                        }
                    }
                    info.getFile().delete();
                }
            }
        }
    }

    public void destroy() {
    }

    private static class FileInfoImpl implements FileInfo {

        File file;

        String fileName;

        String contentType;

        FileInputStream fis;

        public FileInfoImpl(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        public String getFileName() {
            return fileName;
        }

        public String getContentType() {
            return contentType;
        }

        public String getSimpleFileName() {
            return fileName.substring(Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\')) + 1);
        }

        public String getExtension() {
            String s = getSimpleFileName();
            int i = s.lastIndexOf('.');
            if (i == -1) return "";
            return s.substring(i);
        }

        @Override
        public InputStream getStream() {
            try {
                if (fis == null) fis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                return null;
            }
            return fis;
        }

        @Override
        public byte[] toBytes() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[8192];
            int rd;
            try {
                while ((rd = fis.read(data)) != -1) {
                    baos.write(data, 0, rd);
                }
                return baos.toByteArray();
            } finally {
                fis.close();
            }
        }
    }
}
