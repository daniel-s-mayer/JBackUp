import java.io.Serializable;
import java.util.ArrayList;

public class TransmitFile implements Serializable {
    private String shortPath;
    private String name;
    private ArrayList<byte[]> fileBytesList;
    TransmitFile(String shortPath, String name, ArrayList<byte[]> fileBytesList) {
        this.shortPath = shortPath;
        this.name = name;
        this.fileBytesList = fileBytesList;
    }
    
    public String getShortPath() {
        return shortPath;
    }
    
    public String getName() {
        return name;
    }
    
    public ArrayList<byte[]> getBytes() {
        return fileBytesList;
    }
}
