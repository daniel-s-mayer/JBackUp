import java.io.Serializable;
import java.util.ArrayList;

public class StorageServer implements Serializable {
    private int port;
    private ArrayList<User> users;
    private String storageDirectory;
    StorageServer(int port, String storageDirectory) {
        this.port = port;
        this.storageDirectory = storageDirectory;
        this.users = new ArrayList<>();
    }
    
    public int getPort() {
        return port;
    }
    
    public String getStorageDirectory() {
        return storageDirectory;
    }
    
    public ArrayList<User> getUsers() {
        return users;
    }
    
    public void addUser(User newUser) {
        users.add(newUser);
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public void setStorageDirectory(String storageDirectory) {
        this.storageDirectory = storageDirectory;
    }
    
    public void removeUser(User remUser) {
        users.remove(remUser);
    }
}
