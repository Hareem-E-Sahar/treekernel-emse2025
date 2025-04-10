public class Test {    @Test
    public void testStartWithLowSurrogate() throws IOException {
        int scp = 0x00010400;
        String base = "本日は晴天なり";
        char lowSurrogate = Character.toChars(scp)[1];
        assertTrue(Character.isLowSurrogate(lowSurrogate));
        CharArrayWriter writer = new CharArrayWriter();
        writer.write(lowSurrogate);
        writer.write(base);
        int[] answer = getCodePoints((char) BasicCodePointReader.DEFAULT_ALTERNATION_CODEPOINT + base);
        long[] positions = getPositions((char) BasicCodePointReader.DEFAULT_ALTERNATION_CODEPOINT + base);
        CodePointReader reader = new BasicCodePointReader(new CharArrayReader(writer.toCharArray()));
        if (!match(reader, answer, positions)) {
            fail("コードポイントが一致しません。");
        }
    }
}