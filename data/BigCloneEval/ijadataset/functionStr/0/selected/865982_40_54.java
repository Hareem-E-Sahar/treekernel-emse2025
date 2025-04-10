public class Test {    public void parseDirectory(byte[] segmentData, int offset, int length) {
        if ("Rv".equals(new String(segmentData, offset, 2))) {
            String asciiText = new String(segmentData, 0, segmentData.length);
            Pattern pattern = Pattern.compile("(\\w{2})([\\w]+)[;|:]");
            Matcher matcher = pattern.matcher(asciiText);
            while (matcher.find()) {
                RicohTag tag = RicohTag.getTagByIdentifier(matcher.group(1));
                if (tag != null) {
                    values.put(tag, Arrays.copyOfRange(segmentData, matcher.start(2), matcher.end(2)));
                } else {
                    log.log(Level.WARNING, "Found unknown tag: " + matcher.group(1));
                }
            }
        }
    }
}