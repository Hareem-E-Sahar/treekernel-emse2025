public class Test {    @TestTargetNew(level = TestLevel.COMPLETE, notes = "", method = "getActions", args = {  })
    public void test_getActions() {
        assertEquals("getActions did not return proper action", "read", javaPP.getActions());
        assertEquals("getActions did not return proper canonical representation of actions", "read,write", userPP.getActions());
    }
}