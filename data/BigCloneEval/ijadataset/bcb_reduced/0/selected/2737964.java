package com.skype;

import com.skype.Application;
import com.skype.ApplicationAdapter;
import com.skype.Friend;
import com.skype.Skype;
import com.skype.SkypeException;
import com.skype.Stream;
import com.skype.StreamAdapter;
import junit.framework.TestCase;

public final class Ap2ApAPIStressTest extends TestCase {

    static final String APPLICATION_NAME = Ap2ApAPIStressTest.class.getName();

    public void testWriteOnMultiThread() throws Exception {
        Skype.setDebug(true);
        Application application = Skype.addApplication(APPLICATION_NAME);
        Friend friend = TestData.getFriend();
        try {
            Stream[] streams = application.connect(friend);
            assertEquals(1, streams.length);
            Stream stream = streams[0];
            checkConnectedFriends(application, friend);
            checkWriteOnMultiThread(stream);
            checkDisconnect(application, stream);
        } finally {
            application.finish();
        }
    }

    private void checkConnectedFriends(Application application, Friend friend) throws SkypeException {
        Friend[] connectableFriends = application.getAllConnectedFriends();
        assertEquals(1, connectableFriends.length);
        assertEquals(friend, connectableFriends[0]);
    }

    private void checkWriteOnMultiThread(final Stream stream) throws SkypeException {
        Thread[] threads = new Thread[20];
        final String[] results = new String[threads.length];
        for (int i = 0; i < threads.length; i++) {
            final int index = i;
            Thread thread = new Thread("write-" + index) {

                @Override
                public void run() {
                    try {
                        final Object lock = new Object();
                        stream.addStreamListener(new StreamAdapter() {

                            @Override
                            public void textReceived(String text) throws SkypeException {
                                results[index] = text;
                                synchronized (lock) {
                                    lock.notify();
                                }
                            }
                        });
                        synchronized (lock) {
                            stream.write("Hello, World!");
                            try {
                                lock.wait(10000);
                            } catch (InterruptedException e) {
                            }
                        }
                    } catch (SkypeException e) {
                    }
                }

                ;
            };
            thread.start();
            threads[i] = thread;
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
        }
        for (String result : results) {
            assertEquals("Hello, World!", result);
        }
    }

    private void checkDisconnect(Application application, Stream stream) throws Exception {
        final Object lock = new Object();
        final boolean[] result = new boolean[1];
        application.addApplicationListener(new ApplicationAdapter() {

            @Override
            public void disconnected(Stream stream) throws SkypeException {
                result[0] = true;
                synchronized (lock) {
                    lock.notify();
                }
            }
        });
        synchronized (lock) {
            stream.write("disconnect");
            try {
                lock.wait(10000);
            } catch (InterruptedException e) {
                fail();
            }
        }
        assertTrue(result[0]);
    }
}
