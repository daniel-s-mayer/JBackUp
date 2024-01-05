import java.io.Serializable;
import java.util.ArrayList;

public class Transmission implements Serializable {
    private String username;
    private String password;
    private ArrayList<TransmitFile> files;
    private ArrayList<String> paths;
    private int fileCount;
    Transmission(String username, String password, ArrayList<TransmitFile> files, int fileCount, ArrayList<String> paths) {
        this.username = username;
        this.password = password;
        this.files = files;
        this.fileCount = fileCount;
        this.paths = paths;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public ArrayList<TransmitFile> getFiles() {
        return files;
    }
    
    public int getFileCount() {
        return fileCount;
    }
    
    public ArrayList<String> getPaths() {
        return paths;
    }
}
