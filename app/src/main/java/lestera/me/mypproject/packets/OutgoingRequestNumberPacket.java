package lestera.me.mypproject.packets;

public class OutgoingRequestNumberPacket extends BluetoothPacket {

    public OutgoingRequestNumberPacket() {
        super((byte) 0x02);
    }

    @Override
    public byte getDataLength() {
        return 0x00;
    }

    @Override
    public byte[] getDataAsByteArray() {
        return new byte[0];
    }
}
