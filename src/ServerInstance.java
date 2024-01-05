import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerInstance {
    private StorageServer storeServe;
    private Thread serverThread;
    ServerInstance(StorageServer storeServe) {
        this.storeServe = storeServe;
    }
    
    public void startInstance() {
        serverThread = new Thread(new ServerThread(storeServe));
        try {
            serverThread.start();
        } catch (Exception e) {
            System.out.println("E");
        }
        System.out.println("Ret");
    }
    
    public void stopInstance() {
        try {
            serverThread.interrupt();
        } catch (Exception e) {
            System.out.println("Couldn't stop!");
        }
    }
}

class ServerThread implements Runnable {
    // This will set up the listener server. 
    StorageServer storeServe;
    ServerThread(StorageServer storeServe) {
        this.storeServe = storeServe;
    }
    @Override
    public void run() {
        try {
            System.out.println("Port: " + storeServe.getPort());
            ServerSocket receiving = new ServerSocket(storeServe.getPort());
            while (true) {
                Socket client = receiving.accept();
                System.out.println(client.toString());
                // Now, create a new thread for the client.
                ClientServerThread cst = new ClientServerThread(client, storeServe);
                cst.start();
                
                
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
            // Read in the Transmission object
            Transmission trans = (Transmission) ois.readObject();
            // Check for username/password match (add some handling)
            String clientUName = trans.getUsername();
            String clientPassword = trans.getPassword();
            User current = null; // This is the user who we are trying to update files for.
            for (User u : storeServe.getUsers()) {
                if (clientUName.equals(u.getUserName())) {
                    // There was a username match.
                    if (clientPassword.equals(u.getPassword())) {
                        // Complete match.
                        current = u;
                        break; // No more looping
                    } else {
                        // Password error. (TODO add handling)
                        System.exit(403);
                    }
                }
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
            for (String path : trans.getPaths()) {
                File subDirectory = new File(curPath +  "/" + path);
                System.out.println("Creating directory: " + curPath + "/" + path);
                subDirectory.mkdir();
            }
            
            
            // Now, actually write the files to the local filesystem.
            for (TransmitFile tf : trans.getFiles()) {
                // System.out.println("Size of file byte array: " + tf.getBytes().length);
                String filePath = curPath.concat("/" + tf.getShortPath()); // We want to replicate the directory structure with short paths.
                //System.out.println("File Path: " + filePath);
                File uploadSideFile = new File(filePath);
                // We need to delete the file to avoid appending trouble
                uploadSideFile.delete();
                
                
                //uploadSideFile.createNewFile(); // We apparently need to create the new file.
                FileOutputStream fos = new FileOutputStream(uploadSideFile);
                
                // Now, write the bytes.
                ArrayList<byte[]> fileByteList = tf.getBytes();
                for (byte[] byteArr : fileByteList) {
                    fos.write(byteArr);
                }
                //fos.write(tf.getBytes()); // Write the bytes. 
                fos.close();
            }
            
            // Kill the object input stream -- the transmission is over.
            System.out.println("File transactions for " + clientUName + " are complete. " + trans.getFileCount() + " files transmitted.");
            ois.close();
            return; // Kill the thread. 
            
            
            
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Disaster!");
        }
        
        
    }
}
