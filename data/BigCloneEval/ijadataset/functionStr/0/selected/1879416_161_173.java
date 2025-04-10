public class Test {    @TestTargets({ @TestTargetNew(level = TestLevel.ADDITIONAL, method = "update", args = { byte.class }), @TestTargetNew(level = TestLevel.ADDITIONAL, method = "digest", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, method = "method", args = {  }) })
    public void testfips180_2_longMessage() {
        digest.update(source3.getBytes(), 0, source3.length());
        byte[] computedDigest = digest.digest();
        assertNotNull("computed digest is null", computedDigest);
        StringBuilder sb = new StringBuilder();
        String res;
        for (int i = 0; i < computedDigest.length; i++) {
            res = Integer.toHexString(computedDigest[i] & 0xFF);
            sb.append((res.length() == 1 ? "0" : "") + res);
        }
        assertEquals("computed and check digest differ", expected3, sb.toString());
    }
}