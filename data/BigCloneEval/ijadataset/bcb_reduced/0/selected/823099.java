package com.meidusa.amoeba.benchmark;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.math.RandomUtils;
import com.meidusa.amoeba.util.Initialisable;
import com.meidusa.amoeba.util.InitialisationException;
import com.meidusa.amoeba.util.MappedByteBufferUtil;
import com.meidusa.amoeba.util.StringUtil;

/**
 * �����ļ��ڴ�ӳ�䣬�ļ���������ݽ��ᵽ������
 * �ļ����˹��
 * @author Struct
 *
 */
public class FileLineRandomData implements RandomData<Object>, Initialisable {

    private File file;

    private RandomAccessFile raf = null;

    private int size;

    private String lineSplit;

    private boolean needSplit = true;

    private boolean closed = false;

    private MappedByteBuffer buffer = null;

    private String encoding = "gbk";

    private int lineMaxLength = 10 * 1024;

    public int getLineMaxLength() {
        return lineMaxLength;
    }

    public void setLineMaxLength(int lineMaxLength) {
        this.lineMaxLength = lineMaxLength;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public boolean isNeedSplit() {
        return needSplit;
    }

    public void setNeedSplit(boolean needSplit) {
        this.needSplit = needSplit;
    }

    public String getLineSplit() {
        return lineSplit;
    }

    public void setLineSplit(String lineSplit) {
        if (StringUtil.isEmpty(lineSplit)) {
            lineSplit = null;
        } else {
            this.lineSplit = lineSplit;
        }
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    private ThreadLocal<ByteBuffer> localBuffer = new ThreadLocal<ByteBuffer>() {

        protected ByteBuffer initialValue() {
            return buffer.duplicate();
        }
    };

    private ThreadLocal<ByteBuffer> localTempBuffer = new ThreadLocal<ByteBuffer>() {

        protected ByteBuffer initialValue() {
            return ByteBuffer.allocate(lineMaxLength);
        }
    };

    @Override
    public void init() throws InitialisationException {
        try {
            raf = new RandomAccessFile(file, "r");
            size = raf.length() > Integer.MAX_VALUE ? Integer.MAX_VALUE : Long.valueOf(raf.length()).intValue();
            System.out.println("file size =" + size);
            buffer = raf.getChannel().map(MapMode.READ_ONLY, 0, size);
            buffer.load();
            Runtime.getRuntime().addShutdownHook(new Thread() {

                public void run() {
                    closed = true;
                    MappedByteBufferUtil.unmap(buffer);
                    try {
                        raf.close();
                    } catch (IOException e) {
                    }
                }
            });
        } catch (IOException e) {
            throw new InitialisationException(e);
        }
    }

    @Override
    public Object nextData() {
        if (closed) throw new IllegalStateException("file closed..");
        int position = RandomUtils.nextInt(size - 1);
        ByteBuffer buffer = localBuffer.get();
        goNextNewLineHead(buffer, position);
        String[] obj = null;
        String line = readLine(buffer);
        if (needSplit) {
            if (lineSplit == null) {
                obj = StringUtil.split(line);
            } else {
                obj = StringUtil.split(line, lineSplit);
            }
            return obj;
        } else {
            return line;
        }
    }

    private void goNextNewLineHead(ByteBuffer buffer, int position) {
        if (closed) throw new IllegalStateException("file closed..");
        buffer.position(position);
        boolean eol = false;
        int c = -1;
        while (!eol) {
            switch(c = buffer.get()) {
                case -1:
                case '\n':
                    eol = true;
                    break;
                case '\r':
                    eol = true;
                    int cur = buffer.position();
                    if ((buffer.get()) != '\n') {
                        buffer.position(cur);
                    }
                    break;
            }
            if (!eol) {
                if (position > 0) {
                    buffer.position(--position);
                } else {
                    eol = true;
                }
            }
        }
    }

    private final String readLine(ByteBuffer buffer) {
        if (closed) throw new IllegalStateException("file closed..");
        ByteBuffer tempbuffer = localTempBuffer.get();
        tempbuffer.position(0);
        tempbuffer.limit(tempbuffer.capacity());
        byte c = -1;
        boolean eol = false;
        while (!eol) {
            switch(c = buffer.get()) {
                case -1:
                case '\n':
                    eol = true;
                    break;
                case '\r':
                    eol = true;
                    int cur = buffer.position();
                    if ((buffer.get()) != '\n') {
                        buffer.position(cur);
                    }
                    break;
                default:
                    tempbuffer.put(c);
                    break;
            }
        }
        if ((c == -1) && (tempbuffer.position() == 0)) {
            return null;
        }
        tempbuffer.flip();
        try {
            return new String(tempbuffer.array(), encoding);
        } catch (UnsupportedEncodingException e) {
            return new String(tempbuffer.array());
        }
    }

    public static void main(String[] args) throws Exception {
        final FileLineRandomData mapping = new FileLineRandomData();
        mapping.setFile(new File("c:/role.txt"));
        mapping.init();
        List<Thread> list = new ArrayList<Thread>();
        long start = System.currentTimeMillis();
        for (int j = 0; j < 1; j++) {
            Thread thread = new Thread() {

                public void run() {
                    for (int i = 0; i < 1000; i++) {
                        System.out.println(((String[]) mapping.nextData())[1]);
                    }
                }
            };
            list.add(thread);
            thread.start();
        }
        for (int i = 0; i < list.size(); i++) {
            list.get(i).join();
        }
        System.out.println("time=" + (System.currentTimeMillis() - start));
    }
}
