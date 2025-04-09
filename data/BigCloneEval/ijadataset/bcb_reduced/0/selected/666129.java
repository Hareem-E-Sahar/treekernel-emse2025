package org.ximtec.igesture.tool.view.devicemanager.discoveryservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ximtec.igesture.io.AbstractGestureDevice;
import org.ximtec.igesture.io.DeviceDiscoveryService;
import org.ximtec.igesture.io.android.AndroidReader3D;
import org.ximtec.igesture.util.Constant;

/**
 * A Tuio 3D device discovery service. It extends {@link org.ximtec.igesture.tool.view.devicemanager.discoveryservice.AbstractTuioDeviceDiscoveryService}.
 * @author Bjorn Puype, bpuype@gmail.com
 *
 */
public class Android3DDeviceDiscoveryService implements DeviceDiscoveryService {

    private static final Logger LOGGER = Logger.getLogger(Android3DDeviceDiscoveryService.class.getName());

    private Set<AbstractGestureDevice<?, ?>> devices;

    /**
	 * Constructor
	 */
    public Android3DDeviceDiscoveryService() {
        devices = new HashSet<AbstractGestureDevice<?, ?>>();
    }

    @Override
    public Set<AbstractGestureDevice<?, ?>> discover() {
        LOGGER.log(Level.INFO, "Android Device discovery started!");
        try {
            InetAddress addr = InetAddress.getLocalHost();
            String ipAddr = addr.getHostAddress();
            LOGGER.log(Level.INFO, "Connect to: " + ipAddr.toString());
            LOGGER.log(Level.INFO, "Starting Android Gesture Server on port 80");
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(80);
            } catch (IOException e) {
                System.out.println("Could not listen on port: 80");
                System.exit(1);
            }
            LOGGER.log(Level.INFO, "Socket created.");
            LOGGER.log(Level.INFO, "Listening to socket on port 80");
            Socket clientSocket = null;
            try {
                clientSocket = serverSocket.accept();
                @SuppressWarnings("rawtypes") Constructor ctor;
                try {
                    ctor = AndroidReader3D.class.getConstructor(Socket.class, BufferedReader.class);
                    try {
                        BufferedReader in = null;
                        String name = "";
                        try {
                            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                            name = in.readLine();
                        } catch (IOException e1) {
                            System.out.println("Error:" + e1);
                        }
                        AbstractGestureDevice<?, ?> device = (AbstractGestureDevice<?, ?>) ctor.newInstance(clientSocket, in);
                        String[] temp = name.split(":");
                        device.setName(temp[0]);
                        device.setDeviceType(Constant.TYPE_3D);
                        device.setConnectionType(Constant.CONNECTION_IP);
                        device.setIsConnected(true);
                        device.setDeviceID(temp[1]);
                        devices.add(device);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                System.out.println("Accept failed");
                System.exit(1);
            }
        } catch (UnknownHostException e) {
        }
        System.out.println("return devices:" + devices);
        return devices;
    }

    @Override
    public void dispose() {
        devices.clear();
    }
}
