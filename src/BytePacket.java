import java.io.Serializable;

/**
 * This class provides an object for individual byte arrays collected from files
 * to be transmitted in.
 */
public class BytePacket implements Serializable {
    private byte[] byteArr;
    private int seqNum;

    /**
     * Constructor for BytePacket objects.
     * @param byteArr The underlying array of bytes.
     * @param seqNum The sequence number of the packet.
     */
    BytePacket(byte[] byteArr, int seqNum) {
        this.byteArr = byteArr;
        this.seqNum = seqNum;
    }

    /**
     * Returns the underlying byte array.
     * @return The underling byte array of the packet.
     */
    public byte[] getByteArr() {
        return byteArr;
    }

    /**
     * Returns the sequence number of the packet.
     * @return The sequence number of the packet.
     */
    public int getSeqNum() {
        return seqNum;
    }
}
