public class Test {    private static void writeReadTag(ReadAceTag readTag, OutputStream out) throws IOException {
        writeString(String.format("RT{%n%s %s %s %d %d %s%n}%n", readTag.getId(), readTag.getType(), readTag.getCreator(), readTag.getStart(), readTag.getEnd(), AceFileUtil.TAG_DATE_TIME_FORMATTER.print(readTag.getCreationDate().getTime())), out);
    }
}