import java.io.Serializable;

public class BytePacket implements Serializable {
    private byte[] byteArr;
    private int seqNum;
    BytePacket(byte[] byteArr, int seqNum) {
        this.byteArr = byteArr;
        this.seqNum = seqNum;
    }
    
    public byte[] getByteArr() {
        return byteArr;
    }
    
    public int getSeqNum() {
        return seqNum;
    }
}
