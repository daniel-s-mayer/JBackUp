/***
 * Structural plan: If this is the first run, ask for server information. Otherwise, open an existing file. 
 */
import java.io.*;


public class ServerMain {
    // If this class is run directly, use the main method. 
    public static void main(String[] args) {
        
        // Just start a new server instance.
        // Determine whether the existing data file exists.
        StorageServer storeServe = null;
        try {
            FileInputStream fis = new FileInputStream("myServer.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            storeServe = (StorageServer) ois.readObject();
            ois.close();
        }  catch (Exception e) {
            System.out.println("Unrecoverable error. Shutting down.");
        }
        // Create a server instance
        ServerInstance serverInstance = new ServerInstance(storeServe);
       serverInstance.startInstance();
    }
    
    // For use when running class from another class. 
    ServerInstance startServer() {
        StorageServer storeServe = null;
        // Determine whether the existing data file exists.
        try {
            FileInputStream fis = new FileInputStream("myServer.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            storeServe = (StorageServer) ois.readObject();
            ois.close();
        }  catch (Exception e) {
            System.out.println("Unrecoverable error. Shutting down.");
        }
        // Create a server instance
        ServerInstance serverInstance = new ServerInstance(storeServe);
        serverInstance.startInstance();
        return serverInstance;
    }
    
    
    
   
    
}
    

         
