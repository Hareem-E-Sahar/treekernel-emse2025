package com.hanhuy.scurp.data;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import com.hanhuy.scurp.data.DatabaseFile.StaleException;
import com.hanhuy.scurp.server.CertificateGenerator;

/**
 *
 * @author pfnguyen
 */
public class DatabaseFileTest {

    private static final char[] TEST_PASSWORD = "SCURP_TEST".toCharArray();

    private static final char[] TEST_PASSWORD2 = "TEST_SCURP".toCharArray();

    @Test
    public void createAndOpenFile() throws Exception {
        runWithNewDatabase(new DatabaseRunnable() {

            public void run(DatabaseFile db) throws IOException {
                db.open(TEST_PASSWORD.clone());
            }
        });
    }

    @Test
    public void verifyPassword() throws Exception {
        runWithNewDatabase(new DatabaseRunnable() {

            public void run(DatabaseFile db) throws IOException, StaleException {
                Assert.assertTrue(db.verifyPassword(TEST_PASSWORD.clone()));
                Assert.assertFalse(db.verifyPassword(TEST_PASSWORD2.clone()));
            }
        });
    }

    @Test
    public void encryptAndDecryptPassword() throws Exception {
        runWithNewDatabase(new DatabaseRunnable() {

            public void run(DatabaseFile db) {
                byte[] encrypted = db.encrypt(TEST_PASSWORD.clone());
                char[] decrypted = db.decryptText(encrypted);
                Assert.assertArrayEquals(TEST_PASSWORD, decrypted);
            }
        });
    }

    @Test
    public void createAndChangePassword() throws Exception {
        runWithNewDatabase(new DatabaseRunnable() {

            public void run(DatabaseFile db) throws IOException {
                db.changePassword(TEST_PASSWORD2.clone());
                try {
                    db.open(TEST_PASSWORD.clone());
                    Assert.fail("Password did not change");
                } catch (IOException e) {
                }
                db.open(TEST_PASSWORD2.clone());
            }
        });
    }

    @Test
    public void saveAndLoadCAPair() throws Exception {
        runWithNewDatabase(new DatabaseRunnable() {

            public void run(DatabaseFile db) throws IOException, StaleException {
                KeyPair kp = CertificateGenerator.generateKeyPair();
                X509Certificate cert;
                try {
                    cert = CertificateGenerator.generateCACertificate(kp, db.getFile().getName());
                } catch (GeneralSecurityException e) {
                    throw new IllegalStateException(e);
                }
                db.setCAKeyPair(cert, kp.getPrivate());
                db.save();
                db.open(TEST_PASSWORD.clone());
                Assert.assertEquals(cert, db.getCAKeyPair().getKey());
            }
        });
    }

    @Test
    public void saveAndLoadKeyPairs() throws Exception {
        runWithNewDatabase(new DatabaseRunnable() {

            public void run(DatabaseFile db) throws IOException, StaleException {
                Map.Entry<X509Certificate, PrivateKey> kp = generateCAKeyPair();
                db.setCAKeyPair(kp.getKey(), kp.getValue());
                Map.Entry<X509Certificate, PrivateKey> serverPair = generateServerKeyPair(kp);
                db.setServerKeyPair(serverPair.getKey(), serverPair.getValue());
                db.save();
                db.open(TEST_PASSWORD.clone());
                Assert.assertEquals(kp.getKey(), db.getCAKeyPair().getKey());
                Assert.assertEquals(kp.getValue(), db.getCAKeyPair().getValue());
                Assert.assertEquals(serverPair.getKey(), db.getServerKeyPair().getKey());
                Assert.assertEquals(serverPair.getValue(), db.getServerKeyPair().getValue());
            }
        });
    }

    @Test
    public void saveAndLoadAndDeleteKeyPairs() throws Exception {
        runWithNewDatabase(new DatabaseRunnable() {

            public void run(DatabaseFile db) throws IOException, StaleException {
                Map.Entry<X509Certificate, PrivateKey> kp = generateCAKeyPair();
                db.setCAKeyPair(kp.getKey(), kp.getValue());
                Map.Entry<X509Certificate, PrivateKey> serverPair = generateServerKeyPair(kp);
                db.setServerKeyPair(serverPair.getKey(), serverPair.getValue());
                db.save();
                db.open(TEST_PASSWORD.clone());
                Assert.assertEquals(kp.getKey(), db.getCAKeyPair().getKey());
                Assert.assertEquals(kp.getValue(), db.getCAKeyPair().getValue());
                Assert.assertEquals(serverPair.getKey(), db.getServerKeyPair().getKey());
                Assert.assertEquals(serverPair.getValue(), db.getServerKeyPair().getValue());
                db.removeServerKeyPair();
                db.save();
                db.open(TEST_PASSWORD.clone());
                Assert.assertNull(db.getServerKeyPair());
                db.removeCAKeyPair();
                db.save();
                db.open(TEST_PASSWORD.clone());
                Assert.assertNull(db.getCAKeyPair());
            }
        });
    }

    @Test
    public void saveAndLoadKeyPairsWithEntriesAndFolders() throws Exception {
        runWithNewDatabase(new DatabaseRunnable() {

            public void run(DatabaseFile db) throws IOException, StaleException {
                PasswordEntry entry1 = new PasswordEntry();
                entry1.setEntryName("Test 1");
                entry1.setEncryptedPassword(db.encrypt(TEST_PASSWORD.clone()));
                db.getEntries().add(entry1);
                db.save();
                db.open(TEST_PASSWORD.clone());
                Assert.assertEquals(entry1, db.getEntries().get(0));
                Assert.assertArrayEquals(entry1.getEncryptedPassword(), db.getEntries().get(0).getEncryptedPassword());
                PasswordEntry entry2 = new PasswordEntry();
                entry2.setEntryName("Test 2");
                entry2.setEncryptedPassword(db.encrypt(TEST_PASSWORD.clone()));
                entry2.setFolderId(0);
                db.getEntries().add(entry2);
                db.save();
                db.open(TEST_PASSWORD.clone());
                Assert.assertEquals(entry1, db.getEntries().get(0));
                Assert.assertEquals(entry2, db.getEntries().get(1));
                Assert.assertArrayEquals(entry1.getEncryptedPassword(), db.getEntries().get(0).getEncryptedPassword());
                Assert.assertArrayEquals(entry2.getEncryptedPassword(), db.getEntries().get(1).getEncryptedPassword());
                Folder folder = new Folder();
                folder.setName("Test Folder");
                db.getFolders().add(folder);
                entry2.setFolderId(folder.getId());
                db.getEntries().get(1).setFolderId(folder.getId());
                db.save();
                db.open(TEST_PASSWORD.clone());
                Assert.assertEquals(entry1, db.getEntries().get(0));
                Assert.assertEquals(entry2, db.getEntries().get(1));
                Assert.assertEquals(folder, db.getFolders().get(0));
                Assert.assertEquals(folder.getId(), db.getEntries().get(1).getFolderId());
                Assert.assertArrayEquals(entry1.getEncryptedPassword(), db.getEntries().get(0).getEncryptedPassword());
                Assert.assertArrayEquals(entry2.getEncryptedPassword(), db.getEntries().get(1).getEncryptedPassword());
                Map.Entry<X509Certificate, PrivateKey> kp = generateCAKeyPair();
                db.setCAKeyPair(kp.getKey(), kp.getValue());
                Map.Entry<X509Certificate, PrivateKey> serverPair = generateServerKeyPair(kp);
                db.setServerKeyPair(serverPair.getKey(), serverPair.getValue());
                db.save();
                db.open(TEST_PASSWORD.clone());
                Assert.assertEquals(entry1, db.getEntries().get(0));
                Assert.assertEquals(entry2, db.getEntries().get(1));
                Assert.assertEquals(folder, db.getFolders().get(0));
                Assert.assertEquals(kp.getKey(), db.getCAKeyPair().getKey());
                Assert.assertEquals(kp.getValue(), db.getCAKeyPair().getValue());
                Assert.assertEquals(serverPair.getKey(), db.getServerKeyPair().getKey());
                Assert.assertEquals(serverPair.getValue(), db.getServerKeyPair().getValue());
            }
        });
    }

    @Test
    public void saveAndLoadKeyPairsWithEntriesAndFoldersAndChangePassword() throws Exception {
        runWithNewDatabase(new DatabaseRunnable() {

            public void run(DatabaseFile db) throws IOException, StaleException {
                PasswordEntry entry1 = new PasswordEntry();
                entry1.setEntryName("Test 1");
                entry1.setEncryptedPassword(db.encrypt(TEST_PASSWORD.clone()));
                db.getEntries().add(entry1);
                db.save();
                db.open(TEST_PASSWORD.clone());
                Assert.assertEquals(entry1, db.getEntries().get(0));
                Folder folder = new Folder();
                folder.setName("Test Folder");
                PasswordEntry entry2 = new PasswordEntry();
                entry2.setEntryName("Test 2");
                entry2.setEncryptedPassword(db.encrypt(TEST_PASSWORD.clone()));
                entry2.setFolderId(folder.getId());
                db.getEntries().add(entry2);
                db.getFolders().add(folder);
                db.save();
                db.open(TEST_PASSWORD.clone());
                Assert.assertEquals(entry1, db.getEntries().get(0));
                Assert.assertEquals(entry2, db.getEntries().get(1));
                Assert.assertEquals(folder, db.getFolders().get(0));
                Map.Entry<X509Certificate, PrivateKey> kp = generateCAKeyPair();
                db.setCAKeyPair(kp.getKey(), kp.getValue());
                Map.Entry<X509Certificate, PrivateKey> serverPair = generateServerKeyPair(kp);
                db.setServerKeyPair(serverPair.getKey(), serverPair.getValue());
                db.save();
                db.changePassword(TEST_PASSWORD2.clone());
                try {
                    db.open(TEST_PASSWORD.clone());
                    Assert.fail("Password did not change");
                } catch (IOException e) {
                }
                db.open(TEST_PASSWORD2.clone());
                Assert.assertEquals(entry1, db.getEntries().get(0));
                Assert.assertEquals(entry2, db.getEntries().get(1));
                Assert.assertEquals(folder, db.getFolders().get(0));
                Assert.assertEquals(kp.getKey(), db.getCAKeyPair().getKey());
                Assert.assertEquals(kp.getValue(), db.getCAKeyPair().getValue());
                Assert.assertEquals(serverPair.getKey(), db.getServerKeyPair().getKey());
                Assert.assertEquals(serverPair.getValue(), db.getServerKeyPair().getValue());
            }
        });
    }

    @Test
    public void saveAndLoadKeyPairsWithEntriesAndFolders2() throws Exception {
        runWithNewDatabase(new DatabaseRunnable() {

            public void run(DatabaseFile db) throws IOException, StaleException {
                Map.Entry<X509Certificate, PrivateKey> kp = generateCAKeyPair();
                db.setCAKeyPair(kp.getKey(), kp.getValue());
                Map.Entry<X509Certificate, PrivateKey> serverPair = generateServerKeyPair(kp);
                db.setServerKeyPair(serverPair.getKey(), serverPair.getValue());
                db.save();
                db.open(TEST_PASSWORD.clone());
                Assert.assertEquals(kp.getKey(), db.getCAKeyPair().getKey());
                Assert.assertEquals(kp.getValue(), db.getCAKeyPair().getValue());
                Assert.assertEquals(serverPair.getKey(), db.getServerKeyPair().getKey());
                Assert.assertEquals(serverPair.getValue(), db.getServerKeyPair().getValue());
                PasswordEntry entry1 = new PasswordEntry();
                entry1.setEntryName("Test 1");
                entry1.setEncryptedPassword(db.encrypt(TEST_PASSWORD.clone()));
                db.getEntries().add(entry1);
                db.save();
                db.open(TEST_PASSWORD.clone());
                Assert.assertEquals(entry1, db.getEntries().get(0));
                Folder folder = new Folder();
                folder.setName("Test Folder");
                PasswordEntry entry2 = new PasswordEntry();
                entry2.setEntryName("Test 2");
                entry2.setEncryptedPassword(db.encrypt(TEST_PASSWORD.clone()));
                entry2.setFolderId(folder.getId());
                db.getEntries().add(entry2);
                db.getFolders().add(folder);
                db.save();
                db.open(TEST_PASSWORD.clone());
                Assert.assertEquals(entry1, db.getEntries().get(0));
                Assert.assertEquals(entry2, db.getEntries().get(1));
                Assert.assertEquals(folder, db.getFolders().get(0));
                Assert.assertEquals(entry1, db.getEntries().get(0));
                Assert.assertEquals(entry2, db.getEntries().get(1));
                Assert.assertEquals(folder, db.getFolders().get(0));
                Assert.assertEquals(kp.getKey(), db.getCAKeyPair().getKey());
                Assert.assertEquals(kp.getValue(), db.getCAKeyPair().getValue());
                Assert.assertEquals(serverPair.getKey(), db.getServerKeyPair().getKey());
                Assert.assertEquals(serverPair.getValue(), db.getServerKeyPair().getValue());
            }
        });
    }

    private Map.Entry<X509Certificate, PrivateKey> generateCAKeyPair() {
        KeyPair kp = CertificateGenerator.generateKeyPair();
        X509Certificate c;
        try {
            c = CertificateGenerator.generateCACertificate(kp, "Test");
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
        return new MapEntry<X509Certificate, PrivateKey>(c, kp.getPrivate());
    }

    private Map.Entry<X509Certificate, PrivateKey> generateServerKeyPair(Map.Entry<X509Certificate, PrivateKey> caPair) {
        KeyPair kp = CertificateGenerator.generateKeyPair();
        X509Certificate c;
        try {
            c = CertificateGenerator.generateCertificate("localhost", kp.getPublic(), caPair.getValue(), caPair.getKey());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
        return new MapEntry<X509Certificate, PrivateKey>(c, kp.getPrivate());
    }

    public static void runWithNewDatabase(DatabaseRunnable r) throws IOException, StaleException {
        File f = File.createTempFile("SCURP", ".scp");
        try {
            DatabaseFile db = new DatabaseFile(f.getPath());
            db.create(TEST_PASSWORD.clone());
            if (r != null) r.run(db);
        } finally {
            f.delete();
        }
    }

    public static interface DatabaseRunnable {

        void run(DatabaseFile db) throws IOException, StaleException;
    }

    private static class MapEntry<K, V> implements Map.Entry<K, V> {

        private K k;

        private V v;

        public MapEntry(K k, V v) {
            this.k = k;
            this.v = v;
        }

        public K getKey() {
            return k;
        }

        public V getValue() {
            return v;
        }

        public V setValue(V value) {
            throw new UnsupportedOperationException("No set");
        }
    }
}
