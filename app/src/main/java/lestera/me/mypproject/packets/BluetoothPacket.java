package lestera.me.mypproject.packets;

import android.util.SparseArray;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Function;

public abstract class BluetoothPacket {
    private static final SparseArray<Function<ByteBuffer, BluetoothPacket>> incomingPackets
            = new SparseArray<Function<ByteBuffer, BluetoothPacket>>() {{
        append(0x00, IncomingHumidityDataPacket::new);
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
        byte[] buffer = new byte[2];
        buffer[0] = type;
        buffer[1] = 1;

        byte[] data = getDataAsByteArray();

        ByteBuffer buff = ByteBuffer.allocate(buffer.length + data.length);
        buff.put(buffer);
        buff.put(data);
        buffer = buff.array();

        return buffer;
    }
}
