import javax.microedition.lcdui.*;
import javax.microedition.sensor.Data;
import javax.microedition.sensor.DataListener;
import javax.microedition.sensor.SensorConnection;
import javax.microedition.sensor.SensorInfo;
import javax.microedition.sensor.SensorManager;
import java.io.IOException;
import javax.microedition.io.Connector;

public class Displayxyz extends Canvas implements DataListener {

    private static SensorConnection sensor = null;

    byte[] sen = new byte[4];

    int flag;

    int width = getWidth();

    int height = getHeight();

    AccelerometerPointer var;

    private SensorInfo infos[];

    private static boolean isStopped = false;

    private static boolean sensor_found = false;

    private static final int BUFFER_SIZE = 3;

    private int x, y, z;

    public Displayxyz(AccelerometerPointer var) {
        this.var = var;
    }

    public void paint(Graphics g) {
        g.setColor(255, 255, 255);
        g.fillRect(0, 0, width, height);
        g.setColor(0, 0, 0);
        g.drawString("Connected", 10, 10, Graphics.TOP | Graphics.LEFT);
        g.drawString("x:" + x, 40, 40, Graphics.TOP | Graphics.LEFT);
        g.drawString("y:" + y, 40, 80, Graphics.TOP | Graphics.LEFT);
        g.drawString("z:" + z, 40, 120, Graphics.TOP | Graphics.LEFT);
    }

    synchronized void initSensor() {
        sensor = openSensor();
        if (sensor == null) return;
        try {
            sensor.setDataListener(this, BUFFER_SIZE);
            while (!isStopped) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                }
            }
            sensor.removeDataListener();
        } catch (IllegalMonitorStateException imse) {
            imse.printStackTrace();
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        }
        try {
            sensor.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        if (isStopped) {
            sensor = null;
        }
    }

    private SensorConnection openSensor() {
        infos = SensorManager.findSensors("acceleration", null);
        if (infos.length == 0) return null;
        int datatypes[] = new int[infos.length];
        int i = 0;
        String sensor_url = "";
        {
            System.out.println("Searching TYPE_INT sensor...");
            while (!sensor_found) {
                datatypes[i] = infos[i].getChannelInfos()[0].getDataType();
                if (datatypes[i] == 2) {
                    sensor_url = infos[i].getUrl();
                    System.out.println("Sensor: " + sensor_url + ": TYPE_INT found.");
                    sensor_found = true;
                } else i++;
            }
        }
        System.out.println("Sensor: " + sensor_url);
        try {
            return (SensorConnection) Connector.open(sensor_url);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    public void dataReceived(SensorConnection sensor, Data[] data, boolean isDataLost) {
        {
            int[] directions = getIntegerDirections(data);
            x = directions[0];
            y = directions[1];
            z = directions[2];
            sen[0] = (byte) x;
            sen[1] = (byte) y;
            sen[2] = (byte) z;
            sen[3] = 0;
            try {
                var.out.write((sen));
                var.out.flush();
                repaint();
            } catch (Exception e) {
            }
        }
    }

    private static int[] getIntegerDirections(Data[] data) {
        int[][] intValues = new int[3][BUFFER_SIZE];
        int[] directions = new int[3];
        for (int i = 0; i < 3; i++) {
            intValues[i] = data[i].getIntValues();
            int temp = 0;
            for (int j = 0; j < BUFFER_SIZE; j++) {
                temp = temp + intValues[i][j];
            }
            directions[i] = temp / BUFFER_SIZE;
        }
        return directions;
    }

    public void pointerPressed(int x, int y) {
        flag = 1;
    }

    public void pointerDragged(int x, int y) {
        flag = 0;
    }

    public void pointerReleased(int x, int y) {
        if (flag == 1) {
            sen[0] = 0;
            sen[1] = 0;
            sen[2] = 0;
            sen[3] = 1;
            try {
                var.out.write((sen));
                var.out.flush();
            } catch (Exception e) {
            }
        }
    }
}
