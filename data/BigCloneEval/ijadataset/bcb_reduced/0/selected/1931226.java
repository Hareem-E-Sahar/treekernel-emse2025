package com.yerihyo.program.filenameconverter;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilenameConverter {

    private static String compileCode(File f, String code) {
        String returnCode = code;
        for (CodeConverter codeConverter : codeConverters) {
            if (!(codeConverter instanceof FilenameDependentCodeConverter)) {
                continue;
            }
            FilenameDependentCodeConverter filenameDependentCodeConverter = (FilenameDependentCodeConverter) codeConverter;
            returnCode = filenameDependentCodeConverter.replace(f, returnCode);
        }
        return returnCode;
    }

    public static void main(String[] args) {
        File testFolder = new File("C:\\yeri\\testw\\data\\filenameiterator");
        File[] files = testFolder.listFiles();
        convert(files, "###_%f_%f_hello_%e_%e_####.%e", 1);
    }

    public static void convert(File[] files, String code, int startIndex) {
        convert(files, code, startIndex, null);
    }

    public static void convert(File[] files, String code, int startIndex, Writer messageWriter) {
        PrintWriter writer = null;
        if (messageWriter != null) {
            writer = new PrintWriter(messageWriter);
        }
        for (int i = 0, index = startIndex; i < files.length; i++, index++) {
            File file = files[i];
            String compiledCode = compileCode(file, code);
            NumberCodeConverter numberCodeConverter = new NumberCodeConverter();
            String newFilename = numberCodeConverter.replace(compiledCode, index).toString();
            if (writer != null) {
                writer.println("'" + file.getName() + "' => '" + newFilename + "'");
            }
            file.renameTo(new File(file.getParentFile(), newFilename));
        }
    }

    public static interface CodeConverter {

        String getCode();

        String getDescription();

        Pattern getCodePattern();
    }

    public abstract static class AbstractCodeConverter implements CodeConverter {

        public String toString() {
            return this.getDescription();
        }
    }

    private static class NumberCodeConverter extends AbstractCodeConverter {

        @Override
        public String getCode() {
            return "#";
        }

        private static Pattern codePattern = Pattern.compile("(#|0)+");

        @Override
        public Pattern getCodePattern() {
            return codePattern;
        }

        public static CharSequence repeat(String s, int count) {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < count; i++) {
                buffer.append(s);
            }
            return buffer;
        }

        public CharSequence replace(String codeString, int index) {
            StringBuffer buffer = new StringBuffer(codeString);
            Pattern pattern = Pattern.compile("(#)+");
            Matcher matcher = pattern.matcher(buffer);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                String group = matcher.group();
                CharSequence decimalFormatString = repeat("0", group.length());
                DecimalFormat format = new DecimalFormat(decimalFormatString.toString());
                String result = format.format(index);
                buffer.replace(start, end, result);
            }
            return buffer;
        }

        @Override
        public String getDescription() {
            return getCode() + "(iterator number)";
        }
    }

    public static AbstractCodeConverter[] codeConverters = new AbstractCodeConverter[] { new ExtensionCodeConverter(), new ParentFolderCodeConverter(), new NumberCodeConverter() };

    public abstract static class FilenameDependentCodeConverter extends AbstractCodeConverter {

        public String replace(File f, String code) {
            Pattern pattern = getCodePattern();
            Matcher matcher = pattern.matcher(code);
            String result = matcher.replaceAll(getValue(f, code));
            return result;
        }

        public abstract String getValue(File f, String pattern);
    }

    public static class ExtensionCodeConverter extends FilenameDependentCodeConverter {

        @Override
        public String getCode() {
            return "%e";
        }

        private static Pattern codePattern = Pattern.compile("%(e|E)");

        @Override
        public Pattern getCodePattern() {
            return codePattern;
        }

        public String getValue(File f, String pattern) {
            String filename = f.getName();
            int lastIndex = filename.lastIndexOf('.');
            if (lastIndex < 0) {
                return "";
            }
            String extension = filename.substring(lastIndex + 1);
            return extension;
        }

        @Override
        public String getDescription() {
            return getCode() + " (Extension)";
        }
    }

    public static class ParentFolderCodeConverter extends FilenameDependentCodeConverter {

        @Override
        public String getCode() {
            return "%f";
        }

        private static Pattern codePattern = Pattern.compile("%(f|F|p|P)");

        @Override
        public Pattern getCodePattern() {
            return codePattern;
        }

        public String getValue(File f, String pattern) {
            File parent = f.getParentFile();
            if (parent == null) {
                return "";
            }
            return parent.getName();
        }

        @Override
        public String getDescription() {
            return getCode() + "(Parent folder)";
        }
    }
}
