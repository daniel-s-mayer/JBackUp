import java.io.Serializable;
import java.util.ArrayList;

public class TransmissionHeader implements Serializable {
    private String username;
    private String password;
    private ArrayList<String> paths;
    private int fileCount;
    TransmissionHeader(String username, String password, int fileCount, ArrayList<String> paths) {
        this.username = username;
        this.password = password;
        this.fileCount = fileCount;
        this.paths = paths;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public int getFileCount() {
        return fileCount;
    }
    
    public ArrayList<String> getPaths() {
        return paths;
    }
}
