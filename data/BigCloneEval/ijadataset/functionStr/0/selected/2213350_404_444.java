public class Test {    @Test
    public void roundtrip_write_read() throws PermissionDeniedException, IOException {
        final SecurityManager mockSecurityManager = EasyMock.createMock(SecurityManager.class);
        final Database mockDatabase = EasyMock.createMock(Database.class);
        final Subject mockCurrentSubject = EasyMock.createMock(Subject.class);
        replay(mockSecurityManager, mockDatabase, mockCurrentSubject);
        SimpleACLPermission permission = new SimpleACLPermission(mockSecurityManager);
        assertEquals(0, permission.getACECount());
        final int userId1 = 1;
        final int mode1 = ALL;
        permission.addUserACE(ACE_ACCESS_TYPE.ALLOWED, userId1, mode1);
        final int groupId2 = 2;
        final int mode2 = Permission.READ;
        permission.addGroupACE(ACE_ACCESS_TYPE.DENIED, groupId2, mode2);
        final VariableByteOutputStream os = new VariableByteOutputStream();
        permission.write(os);
        verify(mockSecurityManager, mockDatabase, mockCurrentSubject);
        assertEquals(2, permission.getACECount());
        assertEquals(ACE_ACCESS_TYPE.ALLOWED, permission.getACEAccessType(0));
        assertEquals(userId1, permission.getACEId(0));
        assertEquals(ACE_TARGET.USER, permission.getACETarget(0));
        assertEquals(mode1, permission.getACEMode(0));
        assertEquals(ACE_ACCESS_TYPE.DENIED, permission.getACEAccessType(1));
        assertEquals(groupId2, permission.getACEId(1));
        assertEquals(ACE_TARGET.GROUP, permission.getACETarget(1));
        assertEquals(mode2, permission.getACEMode(1));
        ByteArray buf = os.data();
        byte data[] = new byte[buf.size()];
        buf.copyTo(data, 0);
        SimpleACLPermission permission2 = new SimpleACLPermission(mockSecurityManager);
        permission2.read(new VariableByteInputStream(new ByteArrayInputStream(data)));
        assertEquals(2, permission2.getACECount());
        assertEquals(ACE_ACCESS_TYPE.ALLOWED, permission2.getACEAccessType(0));
        assertEquals(userId1, permission2.getACEId(0));
        assertEquals(ACE_TARGET.USER, permission2.getACETarget(0));
        assertEquals(mode1, permission2.getACEMode(0));
        assertEquals(ACE_ACCESS_TYPE.DENIED, permission2.getACEAccessType(1));
        assertEquals(groupId2, permission2.getACEId(1));
        assertEquals(ACE_TARGET.GROUP, permission2.getACETarget(1));
        assertEquals(mode2, permission2.getACEMode(1));
    }
}