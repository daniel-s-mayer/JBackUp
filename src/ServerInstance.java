import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerInstance {
    private StorageServer storeServe;
    private Thread serverThread;
    
    private ServerSocket receiving;
    
    private boolean status; // True if running, false if stopped
    ServerInstance(StorageServer storeServe) {
        this.storeServe = storeServe;
    }
    
    public void startInstance() {
        try {
            receiving = new ServerSocket(storeServe.getPort());
            serverThread = new Thread(new ServerThread(storeServe, receiving));
            serverThread.start();
            status = true; // Now it's running.
        } catch (Exception e) {
            System.out.println("E");
        }
        System.out.println("Ret");
    }
    
    public void stopInstance() {
        System.out.println("Stop requested!");
        try {
            // If receiving is null, we don't need to worry -- there's no socket yet. 
            if (receiving != null) {
                receiving.close();
            }
            status = false; // Now it's not running.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public boolean getStatus() {
        return status;
    }
}

class ServerThread implements Runnable {
    // This will set up the listener server. 
    StorageServer storeServe;
    ServerThread(StorageServer storeServe, ServerSocket receiving) {
        this.storeServe = storeServe;
        this.receiving = receiving;
    }
    private ServerSocket receiving;
   
    @Override
    public void run() {
        try {
            System.out.println("Port: " + storeServe.getPort());
            while (true) {
                Socket client = receiving.accept();
                System.out.println(client.toString());
                // Now, create a new thread for the client.
                ClientServerThread cst = new ClientServerThread(client, storeServe);
                cst.start();
                System.out.println("LOOPED");
                
                
                
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}

class ClientServerThread extends Thread {
    Socket clientSocket;
    StorageServer storeServe;
    ClientServerThread(Socket clientSocket, StorageServer storeServe) {
        this.clientSocket = clientSocket;
        this.storeServe = storeServe;
    }
    public void run() {
        
        try {
            InputStream inputStream = clientSocket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            
            
            // Read in the TransmissionHeader object
            TransmissionHeader transHead = (TransmissionHeader) ois.readObject();
            // Check for username/password match (add some handling)
            String clientUName = transHead.getUsername();
            String clientPassword = transHead.getPassword();
            User current = null; // This is the user who we are trying to update files for.
            ObjectOutputStream returnStatus = new ObjectOutputStream(clientSocket.getOutputStream());
            for (User u : storeServe.getUsers()) {
                if (clientUName.equals(u.getUserName())) {
                    // There was a username match.
                    if (clientPassword.equals(u.getPassword())) {
                        // Complete match.
                        current = u;
                        System.out.println("AUTH SUCCESS");
                        returnStatus.writeBoolean(true);
                        returnStatus.flush();
                        System.out.println("Sent");
                        //returnStatus.close();
                        break; // No more looping
                    } 
                }
            }
            
            if (current == null) {
                System.out.println("NO AUTH!");
                returnStatus.writeBoolean(false);
                returnStatus.flush();
                return;
            }
            
            // A simple null check -- terminate (the thread) if current is null.
            if (current == null) {
                return;
            }
            
            // Now, use current to get the path to use.
            String curPath = storeServe.getStorageDirectory();
            if (curPath.charAt(curPath.length() - 1) == '/' || curPath.charAt(curPath.length() - 1) == '\\') {
                // It ends in a slash
                curPath = curPath.concat(current.getUserName()); // Add the username of the current user for storage purposes.
            } else {
                // It doesn't end in a slash.
                curPath = curPath.concat("/" + current.getUserName());
            }
            
            // Create the user directory if it doesn't already exist.
            File userDirectory = new File(curPath); // The client username was pre-concatenated.
            userDirectory.mkdir();
            
            // Now, loop through the files and create sub-directories (if they don't already exist).
            // Take advantage of the fact that -- because of the queue -- they are organized in layers.
            // An alternative way (if necessary) would be to transmit an ArrayList or similar to directories to create.
            for (String path : transHead.getPaths()) {
                File subDirectory = new File(curPath +  "/" + path);
                System.out.println("Creating directory: " + curPath + "/" + path);
                subDirectory.mkdir();
            }
            
            // Now, get and re-assemble the files. Write them to the filesystem. 
            int fileCount = transHead.getFileCount();
            for (int i = 0; i < fileCount; i++) {
                // Get the header.
                FileHeader fileHead = (FileHeader) ois.readObject();
                System.out.println("Reading in: " + fileHead.getShortPath());
                String filePath = curPath.concat("/" + fileHead.getShortPath());
                File uploadSideFile = new File(filePath);
                // We need to delete the file to avoid appending trouble
                uploadSideFile.delete();
                FileOutputStream fos = new FileOutputStream(uploadSideFile);
                System.out.println("Expected Packet Count: " + fileHead.getPacketCount());
                for (int j = 0; j < fileHead.getPacketCount(); j++) {
                    System.out.println("Got packet: " + j);
                    BytePacket bytePack = (BytePacket) ois.readObject();
                    fos.write(bytePack.getByteArr());
                }
                // We're done writing. Close the writing.
                fos.close();
            }
            // Kill the object input stream -- the transmission is over.
            System.out.println("File transactions for " + clientUName + " are complete. " + transHead.getFileCount() + " files transmitted.");
            ois.close();
            return; // Kill the thread. 
            
            
            
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Disaster!");
        }
        
        
    }
}


