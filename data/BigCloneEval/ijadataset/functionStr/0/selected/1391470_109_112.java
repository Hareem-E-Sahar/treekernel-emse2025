public class Test {    @Test(expected = IllegalArgumentException.class)
    public void testGetMalformedActions() {
        new CtxPermission(ctxAttributeId, "read,write,eat");
    }
}