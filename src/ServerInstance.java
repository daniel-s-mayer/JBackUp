import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Class to create new ServerInstances on which the backup server is run.
 */
public class ServerInstance {
    private StorageServer storeServe;
    private Thread serverThread;

    private ServerSocket receiving;

    private boolean status; // True if running, false if stopped

    /**
     * Constructor for ServerInstance objects.
     * @param storeServe The StorageServer to base the new ServerInstance on.
     */
    ServerInstance(StorageServer storeServe) {
        this.storeServe = storeServe;
    }

    /**
     * Starts the underlying backup server from a pre-constructed
     * ServerInstance.
     */
    public void startInstance() {
        try {
            // Create a ServerSocket to receive client messages on, send it to a
            // new thread, and start that thread. The new thread will handle all
            // communication-related taks.s
            receiving = new ServerSocket(storeServe.getPort());
            serverThread = new Thread(new ServerThread(storeServe, receiving));
            serverThread.start();
            status = true; // The server is now running.
        } catch (Exception e) {
            // Don't do anything, as the user may want to try again.
        }
    }

    /**
     * Stop the server underlying this instance.
     */
    public void stopInstance() {
        try {
            // If receiving is null, we don't need to worry -- there's no socket
            // yet.
            if (receiving != null) {
                // Close the socket to force the threads to unlock.
                receiving.close();
            }
            status = false; // The server is now stopped.
        } catch (Exception e) {
            // Don't do anything, as the user may want to try again.
        }
    }

    /**
     * Returns the status (true = running, false = stopped) of the server
     * underlying the instance.
     * @return The status of the underlying server.
     */
    public boolean getStatus() {
        return status;
    }
}

/**
 * Thread on which connection management (incl. waiting for connections on the
 * server port) occurs.
 */
class ServerThread implements Runnable {
    StorageServer storeServe;
    private ServerSocket receiving;

    /**
     * Constructor for new ServerThreads.
     * @param storeServe The StorageServer object containing the settings for
     *     the underlying server.
     * @param receiving The socket on which messages from the client will be
     *     received.
     */
    ServerThread(StorageServer storeServe, ServerSocket receiving) {
        this.storeServe = storeServe;
        this.receiving = receiving;
    }

    /**
     * The interface-required run() method for the thread.
     */
    @Override
    public void run() {
        try {
            // Use a while loop to accept connections one after another if
            // multiple clients want to connect.
            while (true) {
                // Wait until a client tries to connect, then accept it.
                Socket client = receiving.accept();
                // Now, create a new thread for the client.
                ClientServerThread cst =
                        new ClientServerThread(client, storeServe);
                cst.start();
            }
        } catch (IOException e) {
            // This is a serious error, so throw a RuntimeException to inform
            // the user.
            throw new RuntimeException(e);
        }
    }
}

/**
 * Thread on which file processing and authentication occur.
 */
class ClientServerThread extends Thread {
    Socket clientSocket;
    StorageServer storeServe;

    /**
     * Constructor for ClientServerThreads.
     * @param clientSocket The socket on which the client has connected.
     * @param storeServe The StorageServer containing settings for the current
     *     server.
     */
    ClientServerThread(Socket clientSocket, StorageServer storeServe) {
        this.clientSocket = clientSocket;
        this.storeServe = storeServe;
    }

    /**
     * Interface-required run() method.
     */
    public void run() {
        // Begin receiving files from the client.
        try {
            // Create the InputStream for the client.
            InputStream inputStream = clientSocket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            // Read in the TransmissionHeader object
            TransmissionHeader transHead =
                    (TransmissionHeader) ois.readUnshared();
            // Extract the Username/Password.
            String clientUName = transHead.getUsername();
            String clientPassword = transHead.getPassword();
            User current =
                    null; // This is the user who we are trying to update files for.
            // Create a new ObjectOutputStream to send the authentication result
            // back to the client.
            ObjectOutputStream returnStatus =
                    new ObjectOutputStream(clientSocket.getOutputStream());
            for (User u : storeServe.getUsers()) {
                if (clientUName.equals(u.getUserName())
                        && clientPassword.equals(u.getPassword())) {
                    // Username and password matched.
                    current = u;
                    // Notify the client of the successful auth.
                    returnStatus.writeBoolean(true);
                    returnStatus.flush();
                    break; // No more looping
                }
            }

            // Case: No matching user was found.
            if (current == null) {
                // Notify the client of the unsuccessful auth.
                returnStatus.writeBoolean(false);
                returnStatus.flush();
                // Return -- this thread must be killed so that a new one can be
                // created when the user tries to re-connect.
                return;
            }

            // Get the storage path for the files that will be received by
            // combining the base path and the username of the connected user.
            String curPath = storeServe.getStorageDirectory();
            if (curPath.charAt(curPath.length() - 1) == '/'
                    || curPath.charAt(curPath.length() - 1) == '\\') {
                // Case: The base path ends in a slash.
                curPath = curPath.concat(current.getUserName());
            } else {
                // Case: The base path does not end in a slash.
                curPath = curPath.concat("/" + current.getUserName());
            }

            // Create the user directory if it doesn't already exist.
            File userDirectory = new File(curPath);
            userDirectory.mkdir();

            // Go through each of the paths transmitted in the header (which
            // will be in "tree" order because of their construction with a
            // queue) and create these directories (if they don't already
            // exist).
            for (String path : transHead.getPaths()) {
                File subDirectory = new File(curPath + "/" + path);
                subDirectory.mkdir();
            }

            // Get the number of files that are expected to be received.
            // Loop that number of times, reading a FileHeader and the file
            // contents each time. Re-assemble and store each file.
            int fileCount = transHead.getFileCount();
            for (int i = 0; i < fileCount; i++) {
                // Read the FileHeader for this file from the ObjectInputStream.
                FileHeader fileHead = (FileHeader) ois.readUnshared();
                // Make the server-side storage path for the file.
                String filePath = curPath.concat("/" + fileHead.getShortPath());
                File uploadSideFile = new File(filePath);
                // Delete the file to avoid corrupting it -- we will be
                // REPLACING the existing file.
                uploadSideFile.delete();
                // Initialize an OutputStream for the file being backed up.
                FileOutputStream fos = new FileOutputStream(uploadSideFile);
                // Go through each packet in the file, retrieving it and
                // appending it to the server-side backup file.
                for (int j = 0; j < fileHead.getPacketCount(); j++) {
                    BytePacket bytePack = (BytePacket) ois.readUnshared();
                    fos.write(bytePack.getByteArr());
                }
                // We're done writing. Close the writing.
                fos.close();
            }
            // Kill the object input stream -- the transmission is over.
            // Tell the client that the transmission was successful.
            returnStatus.writeBoolean(true);
            returnStatus.flush();
            System.out.println("File transactions complete.");
            ois.close();
        } catch (Exception e) {
            // Don't do anything -- the client will be notified by a drop of
            // connection and can try again.
        }
    }
}
