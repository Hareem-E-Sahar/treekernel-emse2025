package net.sf.joafip.service;

import java.util.Set;
import net.sf.joafip.AbstractDeleteFileTestCase;
import net.sf.joafip.NotStorableClass;
import net.sf.joafip.StorableAccess;
import net.sf.joafip.TestException;
import net.sf.joafip.entity.EnumFilePersistenceCloseAction;
import net.sf.joafip.java.util.PLinkedTreeSet;
import net.sf.joafip.store.service.objectfortest.BobSerialize;

/**
 * FIXMELUC ___fail on linux 64bits
 * 
 * @author luc peuvrier
 */
@NotStorableClass
@StorableAccess
public class TestBobSerializeStackOverflow extends AbstractDeleteFileTestCase {

    private static final String BIG_BOB_KEY = "bigBobKey";

    protected IFilePersistence filePersistence;

    private IDataAccessSession session;

    public TestBobSerializeStackOverflow() throws TestException {
        super();
    }

    public TestBobSerializeStackOverflow(final String name) throws TestException {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final FilePersistenceBuilder builder = new FilePersistenceBuilder();
        builder.setPathName(path.getPath());
        builder.setProxyMode(true);
        builder.setRemoveFiles(false);
        builder.setGarbageManagement(false);
        builder.setCrashSafeMode(false);
        filePersistence = builder.build();
        session = filePersistence.createDataAccessSession();
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            filePersistence.close();
        } catch (final Throwable throwable) {
            logger.warn("closing file persistence at teardown", throwable);
        }
        filePersistence = null;
        session = null;
        super.tearDown();
    }

    @SuppressWarnings("PMD")
    public void testSerializeStackOverflow() throws Exception {
        int max = 500 * 1024 * 1024;
        int min = 0;
        boolean hasExpectedFailure = false;
        int middle = (max + min) / 2;
        while (!hasExpectedFailure && max > min) {
            try {
                if (smallStackTestSerializeStackOverflow(middle)) {
                    min = middle + 1;
                } else {
                    hasExpectedFailure = true;
                }
            } catch (Throwable throwable) {
                boolean stackOvf = false;
                Throwable current = throwable;
                while (!stackOvf && current != null) {
                    if (current instanceof StackOverflowError) {
                        max = middle;
                        stackOvf = true;
                    }
                    current = current.getCause();
                }
                if (!stackOvf) {
                    throw new Exception(throwable);
                }
                if (session.isOpened()) {
                    session.close(EnumFilePersistenceCloseAction.DO_NOT_SAVE);
                }
            }
            middle = (max + min) / 2;
        }
        assertTrue("has not expected failure", hasExpectedFailure);
        assertTrue("file persistence must be closed after error", filePersistence.isClosed());
        setUp();
        session.open();
        final BobSerialize bobSerialize = (BobSerialize) session.getObject(BIG_BOB_KEY);
        assertNull(bobSerialize);
        session.closeAndWait(EnumFilePersistenceCloseAction.SAVE);
    }

    private boolean smallStackTestSerializeStackOverflow(final int count) throws FilePersistenceException, FilePersistenceClassNotFoundException, FilePersistenceInvalidClassException, FilePersistenceDataCorruptedException, FilePersistenceNotSerializableException {
        final boolean saveSucceed;
        if (count == 0) {
            saveSucceed = saveSerialized();
        } else {
            saveSucceed = smallStackTestSerializeStackOverflow(count - 1);
        }
        return saveSucceed;
    }

    private boolean saveSerialized() throws FilePersistenceException, FilePersistenceClassNotFoundException, FilePersistenceInvalidClassException, FilePersistenceDataCorruptedException, FilePersistenceNotSerializableException {
        final BobSerialize bobSerialize = new BobSerialize();
        final Set<Long> set = new PLinkedTreeSet<Long>();
        for (long value = 1; value < 500; value++) {
            set.add(value);
        }
        bobSerialize.setObject(set);
        session.open();
        session.setObject(BIG_BOB_KEY, bobSerialize);
        Object toBigObject = null;
        String exportFilePath = null;
        boolean saveSucceed;
        try {
            session.close(EnumFilePersistenceCloseAction.SAVE);
            saveSucceed = true;
        } catch (FilePersistenceTooBigForSerializationException exception) {
            toBigObject = exception.getObject();
            exportFilePath = exception.getFilePath();
            assertNotNull("must retrieve too big object", toBigObject);
            assertNotNull("must retrieve export file path", exportFilePath);
            assertSame("bad too big object", bobSerialize, toBigObject);
            saveSucceed = false;
        }
        return saveSucceed;
    }
}
