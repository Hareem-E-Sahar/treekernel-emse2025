package javax.microedition.sensor;

import java.io.IOException;
import java.util.Hashtable;
import lejos.nxt.Battery;
import lejos.nxt.I2CSensor;
import lejos.nxt.SensorPort;

/**
 * Implementation of the SensorConnection interface for NXT sensors
 * 
 * @author Lawrie Griffiths
 */
public class NXTSensorConnection implements SensorConnection {

    private NXTChannelInfo[] channelInfos;

    private Hashtable<ChannelInfo, Channel> channels = new Hashtable<ChannelInfo, Channel>();

    private I2CSensor i2cSensor;

    private byte[] buf = new byte[2];

    private NXTSensorInfo info;

    private int state = SensorConnection.STATE_CLOSED;

    private SensorPort port;

    /**
	 * Create a sensor connection
	 * 
	 * @param url the sensor url
	 * @throws IOException
	 */
    public NXTSensorConnection(String url) throws IOException {
        SensorURL sensorURL = SensorURL.parseURL(url);
        NXTSensorInfo[] infos = SensorManager.getSensors(sensorURL);
        if (infos == null || infos.length == 0) {
            infos = SensorManager.findQuantity(sensorURL.getQuantity());
            if (infos == null || infos.length == 0) throw new IOException();
        }
        info = infos[0];
        if (info.getConnectionType() == NXTSensorInfo.CONN_WIRED) {
            int portNumber = -1;
            if (info.getWiredType() == NXTSensorInfo.I2C_SENSOR) {
                SensorURL infoURL = SensorURL.parseURL(info.getUrl());
                portNumber = infoURL.getPortNumber();
            } else {
                portNumber = sensorURL.getPortNumber();
            }
            if (portNumber < 0) throw new IOException();
            port = SensorPort.getInstance(portNumber);
            port.setTypeAndMode(info.getSensorType(), info.getMode());
            if (info.getWiredType() == NXTSensorInfo.I2C_SENSOR) {
                i2cSensor = new I2CSensor(port);
            }
        }
        channelInfos = info.getChannelInfos();
        for (int i = 0; i < channelInfos.length; i++) {
            channels.put(channelInfos[i], new NXTChannel(this, channelInfos[i]));
        }
        state = SensorConnection.STATE_OPENED;
    }

    public Channel getChannel(ChannelInfo channelInfo) {
        return channels.get(channelInfo);
    }

    public Data[] getData(int bufferSize) throws IOException {
        if (bufferSize > info.getMaxBufferSize()) throw new IllegalArgumentException("Buffer size too large");
        NXTData[] data = new NXTData[channelInfos.length];
        for (int i = 0; i < channelInfos.length; i++) {
            data[i] = new NXTData(channelInfos[i], bufferSize);
        }
        for (int i = 0; i < bufferSize; i++) {
            for (int j = 0; j < channelInfos.length; j++) {
                data[j].setIntData(i, getChannelData(channelInfos[j]));
            }
        }
        return data;
    }

    public int getChannelData(NXTChannelInfo channelInfo) {
        if (info.getConnectionType() == SensorInfo.CONN_EMBEDDED) {
            return Battery.getVoltageMilliVolt();
        } else if (info.getWiredType() == NXTSensorInfo.I2C_SENSOR) {
            return getI2CChannelData(channelInfo);
        } else {
            return getADChannelData(channelInfo);
        }
    }

    public int getADChannelData(NXTChannelInfo channelInfo) {
        return port.readValue();
    }

    public int getI2CChannelData(NXTChannelInfo channelInfo) {
        int dataLength = channelInfo.getDataLength();
        i2cSensor.getData(channelInfo.getRegister(), buf, (dataLength + 7) / 8);
        int reading = 0;
        if (dataLength == 6) {
            reading = (buf[0] & 0x3F);
        } else if (dataLength == 8) {
            reading = (buf[0] & 0xFF);
        } else if (dataLength == 9) {
            reading = ((buf[0] & 0xff) << 1) + buf[1];
        } else if (dataLength == 16) {
            reading = (buf[0] & 0xFF) | ((buf[1]) << 8);
        }
        return reading - channelInfo.getOffset();
    }

    public Data[] getData(int bufferSize, long bufferingPeriod, boolean isTimestampIncluded, boolean isUncertaintyIncluded, boolean isValidityIncluded) throws IOException {
        return getData(bufferSize);
    }

    public SensorInfo getSensorInfo() {
        return info;
    }

    public int getState() {
        return state;
    }

    public void removeDataListener() {
        SensorManager.removeDataListener(this);
        state = SensorConnection.STATE_OPENED;
    }

    public void setDataListener(DataListener listener, int bufferSize) {
        SensorManager.addDataListener(this, bufferSize, listener, 1000 / (Integer) info.getProperty(SensorInfo.PROP_MAX_RATE));
        state = SensorConnection.STATE_LISTENING;
    }

    public void setDataListener(DataListener listener, int bufferSize, long bufferingPeriod, boolean isTimestampIncluded, boolean isUncertaintyIncluded, boolean isValidityIncluded) {
        setDataListener(listener, bufferSize);
    }

    public void close() throws IOException {
        state = SensorConnection.STATE_CLOSED;
    }
}
