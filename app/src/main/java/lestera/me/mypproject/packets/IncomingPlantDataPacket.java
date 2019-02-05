package lestera.me.mypproject.packets;

import java.nio.ByteBuffer;
import java.util.Arrays;

public final class IncomingPlantDataPacket extends BluetoothPacket {

    private byte plantId;

    // Left to Right: Unused | Unused | Unused | Unused | Unused | Unused | Unused | Watering
    private byte plantStates = 0b00000000;
    private byte[] buffer;

    protected IncomingPlantDataPacket(ByteBuffer buff) {
        super((byte) 0x01);

        this.buffer = buff.array();
        this.plantId = buff.get(2);
        this.plantStates = buff.get(3);
    }

    public int getPlantId() {
        return plantId;
    }

    public boolean isWatering() {
        return (plantStates & (0x1 << 0)) == 1;
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
