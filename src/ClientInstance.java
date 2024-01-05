import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class ClientInstance {
    public static void main(String[] args) {
        // First, set up the client gui
        SwingUtilities.invokeLater(new ClientGUI());
    }
}

class ClientGUI implements Runnable {

    @Override
    public void run() {
        // Initialize the file utilities
        FileUtilities fu = new FileUtilities();

        JFrame clientFrame = new JFrame("Client Manager");
        JPanel contentForm = new JPanel(new GridLayout(0, 1));
        JPanel usernameRow = new JPanel();
        JPanel passwordRow = new JPanel();
        JPanel pathRow = new JPanel();
        JPanel addressRow = new JPanel();
        JPanel portRow = new JPanel();
        JPanel backupRow = new JPanel();
        // Set up the username row
        JLabel usernameLabel = new JLabel("Username: ");
        JTextField usernameField = new JTextField("", 20);
        usernameRow.add(usernameLabel);
        usernameRow.add(usernameField);
        // Set up the password row
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField("", 20);
        passwordRow.add(passwordLabel);
        passwordRow.add(passwordField);
        // Set up the path row
        JLabel pathLabel = new JLabel("Path:");
        JTextField pathField = new JTextField("", 20);
        pathRow.add(pathLabel);
        pathRow.add(pathField);
        // Set up the address row
        JLabel addressLabel = new JLabel("Server Address:");
        JTextField addressField = new JTextField("", 20);
        addressRow.add(addressLabel);
        addressRow.add(addressField);
        // Set up the port row
        JLabel portLabel = new JLabel("Port:");
        JTextField portField = new JTextField("", 20);
        portRow.add(portLabel);
        portRow.add(portField);
        // Set up the final button
        JButton backupButton = new JButton("Backup");
        backupRow.add(backupButton);

        // Set up the overall content panel
        contentForm.add(usernameRow);
        contentForm.add(passwordRow);
        contentForm.add(pathRow);
        contentForm.add(addressRow);
        contentForm.add(portRow);
        contentForm.add(backupButton);

        // Show the frame
        clientFrame.setContentPane(contentForm);
        clientFrame.setSize(400, 300);
        clientFrame.setVisible(true);

        // On click of "backup" actions
        // Go to the given path. Count all files in all subdirectories. 
        // Make each file into a TransmitFile object, containing:
        // 1. Short path
        // 2. Name only
        // 3. Byte array of file data
        // Values
        backupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int filesFound = 0;
                String path = pathField.getText();
                Queue<String> pathsToTry = new LinkedList<>(); // Linked-list based queue of paths to try.
                ArrayList<TransmitFile> fileArrayList = new ArrayList<>(); // Array list for files that will be transmitted.
                pathsToTry.add(path);
                FileUtilities fu = new FileUtilities();
                
                // Paths to transmit
                ArrayList<String> paths = new ArrayList<>();
                
                // Adding the actual files.
                while (!pathsToTry.isEmpty()) {
                    String curPath = pathsToTry.remove();
                    paths.add(fu.createShortPath(curPath, path)); // Add the path to the ArrayList of paths.
                    System.out.println(fu.createShortPath(curPath, path));
                    File curPathFile = new File(curPath);
                    if (!curPathFile.isDirectory()) {
                        // This is just a file. Do the single file activities.
                        filesFound++; // Increment the number of files found.
                        fileArrayList.add(fu.createTransmissionObject(curPathFile, path)); // Add the file to the file array list
                        continue; // Don't try the rest of the loop.  
                    }

                    // This is a directory, as expected. Open it and go through the files.
                    File[] filesInDirectory = curPathFile.listFiles();
                    // A null check
                    if (filesInDirectory == null) {
                        continue; // Just go on to the next iteration.
                    }

                    for (File file : filesInDirectory) {
                        // Case 1: It's another directory
                        if (file.isDirectory()) {
                            pathsToTry.add(file.getPath()); // We'll enqueue it for processing.
                        } else { // Case 2: It's a file.
                            filesFound++; // Increment the number of files found.
                            fileArrayList.add(fu.createTransmissionObject(file, path)); // Add the file to the file array list
                        }
                    }
                }


                // Connect to the specified server.
                // Transmit username/password
                // Transmit number of files
                // Transmit TransmitFile objects, one by one.
                // Close on complete. 
                // Read the host, username, password, and port numbers
                String host = addressField.getText();
                String username = usernameField.getText();
                String password = passwordField.getText();
                int port = Integer.valueOf(portField.getText()); // Add error handling.
                try {
                    Socket sock = new Socket(host, port);
                    // Create and transmit the transmission object
                    Transmission trans = new Transmission(username, password, fileArrayList, filesFound, paths);
                    ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
                    oos.writeObject(trans);
                    oos.close();
                    System.out.println("Client Side: Sockets Complete!");
                } catch (Exception exc) {
                    exc.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Connection or transmission error. Check inputs and try again.");
                }
            
            }
        });

    }
}

class FileUtilities {
    public TransmitFile createTransmissionObject(File file, String basePath) {
        // Shorten the path
        String fullPath = file.getAbsolutePath();
        String shortPath = this.createShortPath(fullPath, basePath); // Check for off-by-one
        //System.out.println("Full path: " + fullPath + " to Short Path: " + shortPath);
        String name = file.getName();
        // Create the byte array
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] bytes = new byte[9999];
            fis.readNBytes(bytes, 0, 9999);
            fis.close();
            return new TransmitFile(shortPath, name, bytes);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Fatal error. Try again.");
            return null; // Add null checks for error case. 
        }
        
    }
    
    public String createShortPath(String longPath, String basePath) {
        return longPath.substring(basePath.length(), longPath.length());
    }
}