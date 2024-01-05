import java.io.Serializable;

public class TransmitFile implements Serializable {
    private String shortPath;
    private String name;
    private byte[] bytes;
    TransmitFile(String shortPath, String name, byte[] bytes) {
        this.shortPath = shortPath;
        this.name = name;
        this.bytes = bytes;
    }
    
    public String getShortPath() {
        return shortPath;
    }
    
    public String getName() {
        return name;
    }
    
    public byte[] getBytes() {
        return bytes;
    }
}
