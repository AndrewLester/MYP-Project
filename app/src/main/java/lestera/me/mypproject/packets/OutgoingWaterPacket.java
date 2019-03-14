package lestera.me.mypproject.packets;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class OutgoingWaterPacket extends BluetoothPacket {

    private byte[] buffer;

    public OutgoingWaterPacket() {
        super((byte) 0x05);
        this.buffer = new byte[2 + getDataLength()];
        setWaterState();
    }

    public void setWaterState() {
        buffer[2] = 0x01; // Doesn't matter
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
