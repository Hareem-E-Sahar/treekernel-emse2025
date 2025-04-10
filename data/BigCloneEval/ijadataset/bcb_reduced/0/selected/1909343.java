package net.sf.katta.indexing;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.SequenceFile.Writer;

/**
 * Illustrates how to create a {@link SequenceFile}.
 * 
 */
public class SequenceFileCreator {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println(getUsage());
            System.exit(-1);
        }
        SequenceFileCreator creator = new SequenceFileCreator();
        try {
            String path = args[0];
            String sampleText = getSampleText(args[1]);
            int numOfRecords = Integer.parseInt(args[2]);
            creator.create(path, sampleText, numOfRecords);
        } catch (Exception e) {
            String msg = "Unable to create SequenceFile. Check your parameters: ";
            System.out.println(msg + getUsage());
            e.printStackTrace();
        }
    }

    private static String getUsage() {
        return "SequnceFileCreator: <targetURI> (e.g. hdfs://path) <pathToTextDocument> <nunOfRecords>";
    }

    public void create(String pathString, String sampleText, int num) throws IOException, URISyntaxException {
        String[] strings = sampleText.split("\n");
        Path path = new Path(pathString);
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(path.toUri(), conf);
        Class kClass = LongWritable.class;
        Class vClass = Text.class;
        Writer writer = new SequenceFile.Writer(fs, conf, path, kClass, vClass);
        int numOfLines = strings.length;
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < num; i++) {
            LongWritable key = new LongWritable(i);
            Text value = new Text(strings[random.nextInt(numOfLines)]);
            writer.append(key, value);
        }
        writer.close();
    }

    public static String getSampleText(String path) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        FileInputStream inputStream = new FileInputStream(path);
        byte[] bytes = new byte[1028];
        int lenght = -1;
        while ((lenght = inputStream.read(bytes)) > -1) {
            buffer.write(bytes, 0, lenght);
        }
        inputStream.close();
        return new String(buffer.toByteArray());
    }
}
