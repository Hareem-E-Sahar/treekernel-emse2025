package com.android.monkeyrunner.adb;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.monkeyrunner.MonkeyDevice;
import com.android.monkeyrunner.MonkeyImage;
import com.android.monkeyrunner.MonkeyManager;
import com.android.monkeyrunner.adb.LinearInterpolator.Point;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

public class AdbMonkeyDevice extends MonkeyDevice {

    private static final Logger LOG = Logger.getLogger(AdbMonkeyDevice.class.getName());

    private static final String[] ZERO_LENGTH_STRING_ARRAY = new String[0];

    private static final long MANAGER_CREATE_TIMEOUT_MS = 30 * 1000;

    private static final long MANAGER_CREATE_WAIT_TIME_MS = 1000;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final IDevice device;

    private MonkeyManager manager;

    public AdbMonkeyDevice(IDevice device) {
        this.device = device;
        this.manager = createManager("127.0.0.1", 12345);
        Preconditions.checkNotNull(this.manager);
    }

    @Override
    public MonkeyManager getManager() {
        return manager;
    }

    @Override
    public void dispose() {
        try {
            manager.quit();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error getting the manager to quit", e);
        }
        manager = null;
    }

    private void executeAsyncCommand(final String command, final LoggingOutputReceiver logger) {
        executor.submit(new Runnable() {

            public void run() {
                try {
                    device.executeShellCommand(command, logger);
                } catch (TimeoutException e) {
                    LOG.log(Level.SEVERE, "Error starting command: " + command, e);
                    throw new RuntimeException(e);
                } catch (AdbCommandRejectedException e) {
                    LOG.log(Level.SEVERE, "Error starting command: " + command, e);
                    throw new RuntimeException(e);
                } catch (ShellCommandUnresponsiveException e) {
                    LOG.log(Level.INFO, "Error starting command: " + command, e);
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "Error starting command: " + command, e);
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private MonkeyManager createManager(String address, int port) {
        try {
            device.createForward(port, port);
        } catch (TimeoutException e) {
            LOG.log(Level.SEVERE, "Timeout creating adb port forwarding", e);
            return null;
        } catch (AdbCommandRejectedException e) {
            LOG.log(Level.SEVERE, "Adb rejected adb port forwarding command: " + e.getMessage(), e);
            return null;
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Unable to create adb port forwarding: " + e.getMessage(), e);
            return null;
        }
        String command = "monkey --port " + port;
        executeAsyncCommand(command, new LoggingOutputReceiver(LOG, Level.FINE));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOG.log(Level.SEVERE, "Unable to sleep", e);
        }
        InetAddress addr;
        try {
            addr = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            LOG.log(Level.SEVERE, "Unable to convert address into InetAddress: " + address, e);
            return null;
        }
        boolean success = false;
        MonkeyManager mm = null;
        long start = System.currentTimeMillis();
        while (!success) {
            long now = System.currentTimeMillis();
            long diff = now - start;
            if (diff > MANAGER_CREATE_TIMEOUT_MS) {
                LOG.severe("Timeout while trying to create monkey mananger");
                return null;
            }
            try {
                Thread.sleep(MANAGER_CREATE_WAIT_TIME_MS);
            } catch (InterruptedException e) {
                LOG.log(Level.SEVERE, "Unable to sleep", e);
            }
            Socket monkeySocket;
            try {
                monkeySocket = new Socket(addr, port);
            } catch (IOException e) {
                LOG.log(Level.FINE, "Unable to connect socket", e);
                success = false;
                continue;
            }
            mm = new MonkeyManager(monkeySocket);
            try {
                mm.wake();
            } catch (IOException e) {
                LOG.log(Level.FINE, "Unable to wake up device", e);
                success = false;
                continue;
            }
            success = true;
        }
        return mm;
    }

    @Override
    public MonkeyImage takeSnapshot() {
        try {
            return new AdbMonkeyImage(device.getScreenshot());
        } catch (TimeoutException e) {
            LOG.log(Level.SEVERE, "Unable to take snapshot", e);
            return null;
        } catch (AdbCommandRejectedException e) {
            LOG.log(Level.SEVERE, "Unable to take snapshot", e);
            return null;
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Unable to take snapshot", e);
            return null;
        }
    }

    @Override
    public String getSystemProperty(String key) {
        return device.getProperty(key);
    }

    @Override
    public String getProperty(String key) {
        try {
            return manager.getVariable(key);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Unable to get variable: " + key, e);
            return null;
        }
    }

    @Override
    public void wake() {
        try {
            manager.wake();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Unable to wake device (too sleepy?)", e);
        }
    }

    private String shell(String... args) {
        StringBuilder cmd = new StringBuilder();
        for (String arg : args) {
            cmd.append(arg).append(" ");
        }
        return shell(cmd.toString());
    }

    @Override
    public String shell(String cmd) {
        CommandOutputCapture capture = new CommandOutputCapture();
        try {
            device.executeShellCommand(cmd, capture);
        } catch (TimeoutException e) {
            LOG.log(Level.SEVERE, "Error executing command: " + cmd, e);
            return null;
        } catch (ShellCommandUnresponsiveException e) {
            LOG.log(Level.SEVERE, "Error executing command: " + cmd, e);
            return null;
        } catch (AdbCommandRejectedException e) {
            LOG.log(Level.SEVERE, "Error executing command: " + cmd, e);
            return null;
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error executing command: " + cmd, e);
            return null;
        }
        return capture.toString();
    }

    @Override
    public boolean installPackage(String path) {
        try {
            String result = device.installPackage(path, true);
            if (result != null) {
                LOG.log(Level.SEVERE, "Got error installing package: " + result);
                return false;
            }
            return true;
        } catch (InstallException e) {
            LOG.log(Level.SEVERE, "Error installing package: " + path, e);
            return false;
        }
    }

    @Override
    public boolean removePackage(String packageName) {
        try {
            String result = device.uninstallPackage(packageName);
            if (result != null) {
                LOG.log(Level.SEVERE, "Got error uninstalling package " + packageName + ": " + result);
                return false;
            }
            return true;
        } catch (InstallException e) {
            LOG.log(Level.SEVERE, "Error installing package: " + packageName, e);
            return false;
        }
    }

    @Override
    public void press(String keyName, TouchPressType type) {
        try {
            switch(type) {
                case DOWN_AND_UP:
                    manager.press(keyName);
                    break;
                case DOWN:
                    manager.keyDown(keyName);
                    break;
                case UP:
                    manager.keyUp(keyName);
                    break;
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error sending press event: " + keyName + " " + type, e);
        }
    }

    @Override
    public void type(String string) {
        try {
            manager.type(string);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error Typing: " + string, e);
        }
    }

    @Override
    public void touch(int x, int y, TouchPressType type) {
        try {
            switch(type) {
                case DOWN:
                    manager.touchDown(x, y);
                    break;
                case UP:
                    manager.touchUp(x, y);
                    break;
                case DOWN_AND_UP:
                    manager.tap(x, y);
                    break;
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error sending touch event: " + x + " " + y + " " + type, e);
        }
    }

    @Override
    public void reboot(String into) {
        try {
            device.reboot(into);
        } catch (TimeoutException e) {
            LOG.log(Level.SEVERE, "Unable to reboot device", e);
        } catch (AdbCommandRejectedException e) {
            LOG.log(Level.SEVERE, "Unable to reboot device", e);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Unable to reboot device", e);
        }
    }

    @Override
    public void startActivity(String uri, String action, String data, String mimetype, Collection<String> categories, Map<String, Object> extras, String component, int flags) {
        List<String> intentArgs = buildIntentArgString(uri, action, data, mimetype, categories, extras, component, flags);
        shell(Lists.asList("am", "start", intentArgs.toArray(ZERO_LENGTH_STRING_ARRAY)).toArray(ZERO_LENGTH_STRING_ARRAY));
    }

    @Override
    public void broadcastIntent(String uri, String action, String data, String mimetype, Collection<String> categories, Map<String, Object> extras, String component, int flags) {
        List<String> intentArgs = buildIntentArgString(uri, action, data, mimetype, categories, extras, component, flags);
        shell(Lists.asList("am", "broadcast", intentArgs.toArray(ZERO_LENGTH_STRING_ARRAY)).toArray(ZERO_LENGTH_STRING_ARRAY));
    }

    private static boolean isNullOrEmpty(@Nullable String string) {
        return string == null || string.length() == 0;
    }

    private List<String> buildIntentArgString(String uri, String action, String data, String mimetype, Collection<String> categories, Map<String, Object> extras, String component, int flags) {
        List<String> parts = Lists.newArrayList();
        if (!isNullOrEmpty(action)) {
            parts.add("-a");
            parts.add(action);
        }
        if (!isNullOrEmpty(data)) {
            parts.add("-d");
            parts.add(data);
        }
        if (!isNullOrEmpty(mimetype)) {
            parts.add("-t");
            parts.add(mimetype);
        }
        for (String category : categories) {
            parts.add("-c");
            parts.add(category);
        }
        for (Entry<String, Object> entry : extras.entrySet()) {
            Object value = entry.getValue();
            String valueString;
            String arg;
            if (value instanceof Integer) {
                valueString = Integer.toString((Integer) value);
                arg = "--ei";
            } else if (value instanceof Boolean) {
                valueString = Boolean.toString((Boolean) value);
                arg = "--ez";
            } else {
                valueString = value.toString();
                arg = "--esmake";
            }
            parts.add(arg);
            parts.add(valueString);
        }
        if (!isNullOrEmpty(component)) {
            parts.add("-n");
            parts.add(component);
        }
        if (flags != 0) {
            parts.add("-f");
            parts.add(Integer.toString(flags));
        }
        if (!isNullOrEmpty(uri)) {
            parts.add(uri);
        }
        return parts;
    }

    @Override
    public Map<String, Object> instrument(String packageName, Map<String, Object> args) {
        List<String> shellCmd = Lists.newArrayList("am", "instrument", "-w", "-r", packageName);
        String result = shell(shellCmd.toArray(ZERO_LENGTH_STRING_ARRAY));
        return convertInstrumentResult(result);
    }

    /**
     * Convert the instrumentation result into it's Map representation.
     *
     * @param result the result string
     * @return the new map
     */
    @VisibleForTesting
    static Map<String, Object> convertInstrumentResult(String result) {
        Map<String, Object> map = Maps.newHashMap();
        Pattern pattern = Pattern.compile("^INSTRUMENTATION_(\\w+): ", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(result);
        int previousEnd = 0;
        String previousWhich = null;
        while (matcher.find()) {
            if ("RESULT".equals(previousWhich)) {
                String resultLine = result.substring(previousEnd, matcher.start()).trim();
                int splitIndex = resultLine.indexOf("=");
                String key = resultLine.substring(0, splitIndex);
                String value = resultLine.substring(splitIndex + 1);
                map.put(key, value);
            }
            previousEnd = matcher.end();
            previousWhich = matcher.group(1);
        }
        if ("RESULT".equals(previousWhich)) {
            String resultLine = result.substring(previousEnd, matcher.start()).trim();
            int splitIndex = resultLine.indexOf("=");
            String key = resultLine.substring(0, splitIndex);
            String value = resultLine.substring(splitIndex + 1);
            map.put(key, value);
        }
        return map;
    }

    @Override
    public void drag(int startx, int starty, int endx, int endy, int steps, long ms) {
        final long iterationTime = ms / steps;
        LinearInterpolator lerp = new LinearInterpolator(steps);
        LinearInterpolator.Point start = new LinearInterpolator.Point(startx, starty);
        LinearInterpolator.Point end = new LinearInterpolator.Point(endx, endy);
        lerp.interpolate(start, end, new LinearInterpolator.Callback() {

            public void step(Point point) {
                try {
                    manager.touchMove(point.getX(), point.getY());
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "Error sending drag start event", e);
                }
                try {
                    Thread.sleep(iterationTime);
                } catch (InterruptedException e) {
                    LOG.log(Level.SEVERE, "Error sleeping", e);
                }
            }

            public void start(Point point) {
                try {
                    manager.touchDown(point.getX(), point.getY());
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "Error sending drag start event", e);
                }
                try {
                    Thread.sleep(iterationTime);
                } catch (InterruptedException e) {
                    LOG.log(Level.SEVERE, "Error sleeping", e);
                }
            }

            public void end(Point point) {
                try {
                    manager.touchUp(point.getX(), point.getY());
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "Error sending drag end event", e);
                }
            }
        });
    }
}
