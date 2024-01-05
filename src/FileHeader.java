import java.io.Serializable;

public class FileHeader implements Serializable {
    private String shortPath;
    private String name;
    private int packetCount;
    FileHeader(String shortPath, String name, int packetCount) {
        this.shortPath = shortPath;
        this.name = name;
        this.packetCount = packetCount;
    }
    
    public String getShortPath() {
        return shortPath;
    }
    
    public String getName() {
        return name;
    }
    
    public int getPacketCount() {
        return packetCount;
    }
}
