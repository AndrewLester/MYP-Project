package lestera.me.mypproject.packets;

import android.util.Log;

import java.util.Arrays;

public class OutgoingWaterLimitPacket extends BluetoothPacket {

    private byte[] buffer;
    private short waterLimitData;

    public OutgoingWaterLimitPacket(short data) {
        super((byte) 0x04);
        this.buffer = new byte[2 + getDataLength()];
        setWaterLimitData(data);
    }

    public void setWaterLimitData(short data) {
        Log.e("REAL LIMIT", String.valueOf(data));
        this.buffer[2] = (byte) ((data >> 8) & 0xFF);
        this.buffer[3] = (byte) (data & 0xFF);
        Log.e("BUFFER 2", String.valueOf(buffer[2]));
        Log.e("BUFFER 3", String.valueOf(buffer[3]));
        waterLimitData = data;
    }

    public short getWaterLimitData() {
        return waterLimitData;
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
