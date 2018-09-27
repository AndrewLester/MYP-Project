package lestera.me.mypproject.packets;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class OutgoingLEDPacket extends BluetoothPacket {

    private byte[] buffer;
    private boolean ledState;

    protected OutgoingLEDPacket(ByteBuffer buffer) {
        super((byte) 0x01);
        this.buffer = buffer.array();
        setLEDState(this.buffer[2] == 0x01);
    }

    public OutgoingLEDPacket(boolean on) {
        super((byte) 0x01);
        this.buffer = new byte[2 + getDataLength()];
        setLEDState(on);
    }

    public void setLEDState(boolean on) {
        buffer[2] = on ? (byte) 0x01 : (byte) 0x00;
        ledState = on;
    }

    public boolean getLEDState() {
        return ledState;
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
