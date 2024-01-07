import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
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
        
        // Go ahead and set the look and feel.

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
            //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }
        


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
        //JTextField pathField = new JTextField("", 20);
        JButton showPathChooser = new JButton("Choose Directory");
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);


        showPathChooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               jfc.showOpenDialog(clientFrame);
            }
        });
        pathRow.add(pathLabel);
        pathRow.add(showPathChooser);
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
                int port = 0;
                String path = "";
                try {
                    File chosenDirectory = jfc.getSelectedFile();
                    path = chosenDirectory.getAbsolutePath();
                    port = Integer.valueOf(portField.getText()); // Add error handling.
                    if (port < 0) {
                        throw new RuntimeException("Negative port numbers not permitted!");
                    }
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(clientFrame, "Error: " + exc.getMessage());
                    return;
                }

                Queue<String> pathsToTry = new LinkedList<>(); // Linked-list based queue of paths to try.
                ArrayList<File> fileArrayList = new ArrayList<>(); // Array list for files that will be transmitted.
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
                        fileArrayList.add(curPathFile); // Add the file to the file array list
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
                            fileArrayList.add(file); // Add the file to the file array list
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
               
                try {
                    Socket sock = new Socket(host, port);
                    // Create the stream to send things on.
                    ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
                    // First: Transmit header data under object TransmissionHeader. Include the number of files to wait for.
                    TransmissionHeader transHead = new TransmissionHeader(username, password, filesFound, paths);
                    oos.writeObject(transHead);
                    
                    // IDEA: TODO implement server sending back ERROR/SUCCESS message. 
                    ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
                    boolean success = ois.readBoolean();
                    System.out.println("Read!");
                    
                    if (!success) {
                        JOptionPane.showMessageDialog(null, "Username/Password Incorrect! Try again.");
                        return;
                    }
                    
                    
                    
                    
                    
                    // Second: Iteratively transmit:
                        // Each file's header (containing number of sequence transmissions)
                        // Each sequence transmission. 
                    for (File file : fileArrayList) {
                        System.out.println("Processing client for: " + file.getAbsolutePath() + file.isDirectory());
                        // Transmit the header
                        // ONLY INCREMENT THE NUMBER OF PACKETS IF NOT ON AN EXACT BOUNDARY!
                        FileHeader fh = new FileHeader(fu.createShortPath(file.getAbsolutePath(), path), file.getName(), (int) ((file.length() / 512) + ((file.length() % 512 >= 1 || file.length() == 0)? 1 : 0)));
                        System.out.println("Sending an estimate of: " + (int) ((file.length() / 512) + (file.length() % 512 >= 1 ? 1 : 0)) + " packets.");
                        oos.writeObject(fh);
                        // Transmit the packets
                        int seqNum = 0; // Start at 0

                        FileInputStream fis = new FileInputStream(file);
                        long fileSize = file.length();
                        // Chunk into groups of 512 bytes.
                        int count = 0; // Number of parts read so far.

                        // Latest idea: Each file is broken into chunks (numbered in series) that are sent and reassembled on the other end. 
                        if (fileSize == 0) {
                            // Case: A starting empty file.
                            BytePacket bp = new BytePacket(new byte[0], seqNum++);
                            oos.writeObject(bp);
                        }
                        while (fileSize > 0) {
                            byte[] moreBytes = new byte[512];
                            fis.readNBytes(moreBytes, 0, 512);
                            count++;
                            BytePacket bp = new BytePacket(moreBytes, seqNum++);
                            oos.writeObject(bp);
                            fileSize = fileSize -  (long) 512;
                            //System.out.println("Count: " + count + "File size: " + fileSize);
                        }
                        System.out.println("Actually need: " + count);
                        
                        fis.close();
                    }
                   // Transmission trans = new Transmission(username, password, fileArrayList, filesFound, paths);
                    //oos.writeObject(trans);
                    oos.close();
                    System.out.println("Client Side: Sockets Complete!");
                } catch (Exception exc) {
                    exc.printStackTrace();
                    JOptionPane.showMessageDialog(clientFrame, "Connection or transmission error. Check inputs and try again.");
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
            System.out.println("Processing: " + file.getName());
            // USE APPEND TO RE-COMBINE THE VARIOUS SETS OF BYTES!
            FileInputStream fis = new FileInputStream(file);
            long fileSize = file.length();
            // Chunk into groups of 512 bytes.
            ArrayList<byte[]> fileBytesList = new ArrayList<>();
            int count = 0; // Number of parts read so far.
            
            // Latest idea: Each file is broken into chunks (numbered in series) that are sent and reassembled on the other end. 
            while (fileSize > 0) {
                byte[] moreBytes = new byte[10000000];
                fis.readNBytes(moreBytes, 0, 10000000);
                count++;
                fileBytesList.add(moreBytes);
                //System.out.println("Upper file size: " + fileSize);
                fileSize = fileSize -  (long) 10000000;
                System.out.println("Count: " + count + "File size: " + fileSize);
            }
            //byte[] bytes = new byte[9999];
            //fis.readNBytes(bytes, 0, 9999);
            fis.close();
            return new TransmitFile(shortPath, name, fileBytesList);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Fatal error. Try again.");
            return null; // Add null checks for error case. 
        }
        
    }
    
    public String createShortPath(String longPath, String basePath) {
        System.out.println("Shorting: " + longPath + " " + basePath);
        return longPath.substring(basePath.length(), longPath.length());
    }
}