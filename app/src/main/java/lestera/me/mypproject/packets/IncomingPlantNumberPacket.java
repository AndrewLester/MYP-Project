package lestera.me.mypproject.packets;

import java.nio.ByteBuffer;
import java.util.Arrays;

public final class IncomingPlantNumberPacket extends BluetoothPacket {
    private int plantNumber;
    private byte[] buffer;

    protected IncomingPlantNumberPacket(ByteBuffer buff) {
        super((byte) 0x02);

        this.buffer = buff.array();
        setPlantNumber(buff.get(2));
    }

    private void setPlantNumber(byte number) {
        buffer[2] = number;
        plantNumber = number;
    }

    public int getPlantNumber() {
        return plantNumber;
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
