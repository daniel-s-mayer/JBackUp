import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class for storage of server settings, including port, user list, and base
 * storage directory.
 */
public class StorageServer implements Serializable {
    private int port;
    private ArrayList<User> users;
    private String storageDirectory;

    /**
     * Constructor for StorageServer objects.
     * @param port The port of the StorageServer.
     * @param storageDirectory The base storage directory for the StorageServer.
     */
    StorageServer(int port, String storageDirectory) {
        this.port = port;
        this.storageDirectory = storageDirectory;
        this.users = new ArrayList<>();
    }

    /**
     * Method to get the port associated with this StorageServer.
     * @return The port associated with this StorageServer.
     */
    public int getPort() {
        return port;
    }

    /**
     * Method to get the directory associated with this StorageServer.
     * @return The directory associated with this StorageServer.
     */
    public String getStorageDirectory() {
        return storageDirectory;
    }

    /**
     * Method to get the users list associated with this StorageServer.
     * @return The users list associated with this StorageServer.
     */
    public ArrayList<User> getUsers() {
        return users;
    }

    /**
     * Method to add a new user to this StorageServer.
     * @param newUser The new user to be added to this StorageServer.
     */
    public void addUser(User newUser) {
        users.add(newUser);
    }

    /**
     * Method to set the port associated with this StorageServer.
     * @param port The port to be associated with this StorageServer.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Method to set the storage directory associated with this StorageServer.
     * @param storageDirectory The storage directory to be associated with this
     *     StorageServer.
     */
    public void setStorageDirectory(String storageDirectory) {
        this.storageDirectory = storageDirectory;
    }

    /**
     * Method to remove a user from this StorageServer.
     * @param remUser The user to be removed from this StorageServer.
     */
    public void removeUser(User remUser) {
        users.remove(remUser);
    }
}
