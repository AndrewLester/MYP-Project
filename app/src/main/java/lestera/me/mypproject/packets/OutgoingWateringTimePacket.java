package lestera.me.mypproject.packets;

import java.util.Arrays;

public class OutgoingWateringTimePacket extends BluetoothPacket {

    private byte[] buffer;
    private byte wateringTimeData;

    public OutgoingWateringTimePacket(byte data) {
        super((byte) 0x03);
        this.buffer = new byte[2 + getDataLength()];
        setWaterLimitData(data);
    }

    public void setWaterLimitData(byte data) {
        buffer[2] = data;
        wateringTimeData = data;
    }

    public short getWaterLimitData() {
        return wateringTimeData;
    }

    @Override
    public byte getDataLength() {
        return 0x01;
    }

    @Override
    public byte[] getDataAsByteArray() {
        return Arrays.copyOfRange(buffer, 2, buffer.length);
    }
}
