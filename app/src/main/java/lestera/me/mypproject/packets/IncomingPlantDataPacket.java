package lestera.me.mypproject.packets;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;

public final class IncomingPlantDataPacket extends BluetoothPacket {

    private byte plantId;

    // Left to Right: Unused | Unused | Unused | Unused | Unused | Unused | Unused | Watering
    private byte plantStates;
    private byte[] buffer;
    private int nextWatering;

    protected IncomingPlantDataPacket(ByteBuffer buff) {
        super((byte) 0x01);

        this.buffer = buff.array();
        setPlantData();
    }

    private void setPlantData() {
        this.plantId = buffer[2];
        this.plantStates = buffer[3];
        this.nextWatering = buffer[4];
    }

    public int getPlantId() {
        return plantId;
    }

    public boolean isWatering() {
        return (plantStates & (0x1)) == 1;
    }

    @Override
    public byte getDataLength() {
        return 0x03;
    }

    @Override
    public byte[] getDataAsByteArray() {
        return Arrays.copyOfRange(buffer, 2, buffer.length);
    }
}
