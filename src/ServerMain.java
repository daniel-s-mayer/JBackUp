
import java.io.*;

/**
 * This class can be run independently (providing that the user had already configured the server settings and users.
 * This permits headless, command-line/automated operation of the server. 
 */
public class ServerMain {
    // If this class is run directly, use the main method. 
    public static void main(String[] args) {
        // Determine whether an existing settings file exists (if so, load it);
        StorageServer storeServe = null;
        try {
            FileInputStream fis = new FileInputStream("myServer.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            storeServe = (StorageServer) ois.readObject();
            ois.close();
        }  catch (FileNotFoundException fe) {
            System.out.println("Error: Server settings file not found. Please run the Server Management Utility to set up your server.");
            return; // Unrecoverable error.
        } catch (IOException ie) {
            System.out.println("Error: Filesystem operation error. Check that you are permitted to access myServer.dat");
            return; // Unrecoverable error.
        } catch (ClassNotFoundException cne) {
            System.out.println("Error: File corrupted. Please delete myServer.dat and try again.");
            return; // Unrecoverable error.
        }
        // Create a server instance and start it. 
        ServerInstance serverInstance = new ServerInstance(storeServe);
        serverInstance.startInstance();
    }
}
    

         
