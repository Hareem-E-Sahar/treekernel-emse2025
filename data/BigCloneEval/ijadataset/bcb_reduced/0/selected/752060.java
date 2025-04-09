package server;

import java.io.IOException;
import java.util.HashMap;
import serialization.Marshaler;
import serialization.MarshalerException;
import client.IRequest;
import client.TransportManager;

public class Waiter extends Thread {

    private HashMap<String, Class<?>> servicesMap;

    private HashMap<SimpleUUID, Object> objectsMap;

    private Marshaler marshaler;

    private TransportManager transport;

    public Waiter(TransportManager transport, HashMap<String, Class<?>> servicesMap, HashMap<SimpleUUID, Object> objectsMap, Marshaler marshaler) {
        this.servicesMap = servicesMap;
        this.objectsMap = objectsMap;
        this.marshaler = marshaler;
        this.transport = transport;
    }

    @Override
    public void run() {
        try {
            IRequest request = (IRequest) marshaler.decode(transport.recieve());
            Object result = request.process(servicesMap, objectsMap);
            transport.send(marshaler.encode(result));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MarshalerException e) {
            e.printStackTrace();
        } finally {
            try {
                this.transport.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
