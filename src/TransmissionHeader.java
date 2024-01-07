import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class allows TransmissionHeader objects containing information about a
 * transmission to be created. These objects contain authentication information,
 * the directory structure of the files to be transmitted, and the number of
 * files to be transmitted.
 */
public class TransmissionHeader implements Serializable {
    private String username;
    private String password;
    private ArrayList<String> paths;
    private int fileCount;

    /**
     * Constructor for TransmissionHeader objects.
     * @param username The username of the user trying to send the transmission.
     * @param password The password of the user trying to send the transmission.
     * @param fileCount The number of files to be transmitted.
     * @param paths The directory structure of the files to be transmitted..
     */
    TransmissionHeader(String username, String password, int fileCount,
                       ArrayList<String> paths) {
        this.username = username;
        this.password = password;
        this.fileCount = fileCount;
        this.paths = paths;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Returns the password provided by the user requesting the transmission.
     * @return The password provided by the user requesting the transmission.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the number of files included in the transmission.
     * @return The number of files included in the transmission.
     */
    public int getFileCount() {
        return fileCount;
    }

    /**
     * Returns the directory structure of the files included in the
     * transmission.
     * @return The directory structure of the files included in the
     *     transmission.
     */
    public ArrayList<String> getPaths() {
        return paths;
    }
}
