import java.io.*;
import java.util.Date;

public class LogParser {

    public static void main(String args[]) throws Exception {
        String fileName = args[1];
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line = null;
        LogLineProcessor processor = getLogLineProcessor(args[0]);
        while ((line = reader.readLine()) != null) {
            processor.processLine(line);
        }
        processor.lastLine();
    }

    public static LogLineProcessor getLogLineProcessor(String type) throws Exception {
        return (LogLineProcessor) Class.forName(type).getConstructor().newInstance();
    }
}
