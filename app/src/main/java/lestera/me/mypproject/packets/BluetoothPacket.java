package lestera.me.mypproject.packets;

import android.util.SparseArray;

import java.nio.ByteBuffer;
import java.util.function.Function;

public abstract class BluetoothPacket {
    private static final SparseArray<Function<ByteBuffer, BluetoothPacket>> incomingPackets
            = new SparseArray<Function<ByteBuffer, BluetoothPacket>>() {{
        append(0x00, IncomingHumidityDataPacket::new);
        append(0x01, IncomingPlantDataPacket::new);
    }};

    public static BluetoothPacket obtain(byte[] bytes) {
        byte type = bytes[0];
        ByteBuffer byteBuff = ByteBuffer.wrap(bytes);

        Function<ByteBuffer, BluetoothPacket> packet = incomingPackets.get((int) type);

        return packet == null ? null : packet.apply(byteBuff);
    }

    private final byte type;

    protected BluetoothPacket(byte type) {
        this.type = type;
    }

    public abstract byte getDataLength();

    public abstract byte[] getDataAsByteArray();

    public byte getType() {
        return type;
    }

    public byte[] toByteArray() {
        byte[] heading = new byte[2];
        heading[0] = type;
        heading[1] = getDataLength();

        byte[] data = getDataAsByteArray();

        ByteBuffer buff = ByteBuffer.allocate(heading.length + data.length);
        buff.put(heading);
        buff.put(data);

        return buff.array();
    }
}
