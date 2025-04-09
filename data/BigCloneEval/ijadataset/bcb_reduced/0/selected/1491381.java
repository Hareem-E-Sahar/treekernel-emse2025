package org.fb4j.impl;

import java.util.Set;
import java.util.zip.CRC32;
import org.apache.commons.codec.digest.DigestUtils;
import org.fb4j.Session;
import org.fb4j.connect.Account;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Mino Togna
 * 
 */
public class ConnectTest extends SessionTestBase {

    @Test
    public void testRegisterUsers() {
        Session session = getSession();
        Account[] accounts = new Account[3];
        CRC32 crc32 = new CRC32();
        crc32.update("mino.togna@gmail.com".getBytes());
        long abs = Math.abs(crc32.getValue());
        String md5Hex = DigestUtils.md5Hex(String.valueOf(abs));
        String emailHash = abs + "_" + md5Hex;
        Account account = new Account(emailHash);
        account.setUser(752141196L);
        accounts[0] = account;
        log.debug("adding+ " + account.getUser() + ", " + account.getEmailHash());
        crc32 = new CRC32();
        crc32.update("gino75@gmail.com".getBytes());
        abs = Math.abs(crc32.getValue());
        md5Hex = DigestUtils.md5Hex(String.valueOf(abs));
        emailHash = abs + "_" + md5Hex;
        account = new Account(emailHash);
        account.setUser(733192756L);
        accounts[1] = account;
        log.debug("adding+ " + account.getUser() + ", " + account.getEmailHash());
        crc32 = new CRC32();
        crc32.update("geebay@belasius.com".getBytes());
        abs = Math.abs(crc32.getValue());
        md5Hex = DigestUtils.md5Hex(String.valueOf(abs));
        emailHash = abs + "_" + md5Hex;
        account = new Account(emailHash);
        account.setUser(591307764L);
        accounts[2] = account;
        log.debug("adding+ " + account.getUser() + ", " + account.getEmailHash());
        Set<String> emails = session.registerUsers(accounts);
        log.debug(emails);
        Assert.assertNotNull(emails);
        Assert.assertTrue(emails.size() > 0);
    }

    @Test
    public void testUnregisterUsers() {
        CRC32 crc32 = new CRC32();
        crc32.update("mino.togna@gmail.com".getBytes());
        long abs = Math.abs(crc32.getValue());
        String md5Hex = DigestUtils.md5Hex(String.valueOf(abs));
        String emailHash = abs + "_" + md5Hex;
        Session session = getSession();
        Set<String> set = session.unregisterUsers(new String[] { emailHash });
        log.debug(set);
        Assert.assertNotNull(set);
        Assert.assertTrue(set.size() > 0);
    }

    @Test
    public void testGetUnconnectedFriendsCount() {
        Session session = getSession();
        int unconnectedFriendsCount = session.getUnconnectedFriendsCount();
        log.debug(unconnectedFriendsCount);
    }
}
