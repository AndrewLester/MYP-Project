package lestera.me.mypproject.packets;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class IncomingHumidityDataPacket extends BluetoothPacket {

    private short sensorData;
    private byte[] buffer;

    protected IncomingHumidityDataPacket(ByteBuffer buff) {
        super((byte) 0x00);

        this.buffer = buff.array();
        setSensorData(buff.getShort(2));
    }

    public IncomingHumidityDataPacket(short sensorData) {
        super((byte) 0x00);

        this.buffer = new byte[2 + getDataLength()];
        setSensorData(sensorData);
    }

    public void setSensorData(short data) {
        sensorData = data;
        this.buffer[2] = (byte) ((data >> 8) & 0xFF);
        this.buffer[3] = (byte) (data & 0xFF);
    }

    public short getSensorData() {
        return sensorData;
    }

    @Override
    public byte getDataLength() {
        return 0x02;
    }

    @Override
    public byte[] getDataAsByteArray() {
        return Arrays.copyOfRange(buffer, 2, buffer.length);
    }
}
