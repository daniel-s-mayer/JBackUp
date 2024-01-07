import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.*;

/**
 * Class for the client part of the program, including the client-side GUI.
 */
public class ClientInstance {
    public static void main(String[] args) {
        // First, set up the client gui
        SwingUtilities.invokeLater(new ClientGUI());
    }
}

/**
 * The client-side GUI component of the program.
 */
class ClientGUI implements Runnable {
    /**
     * Interface-required run method for the ClientGUI threadable class.
     */
    @Override
    public void run() {
        // Set the look and feel to "Windows" if possible; otherwise, use the
        // default.
        try {
            for (UIManager.LookAndFeelInfo info :
                    UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Don't do anything -- just use the default if necessary.
        }

        // Create the JFrame for the client-side component of the program.
        JFrame clientFrame = new JFrame("Client Manager");
        clientFrame.setLocationRelativeTo(null);
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
        // Use a button-activated JFileChooser to permit the user to choose a
        // directory to back up.
        JButton showPathChooser = new JButton("Choose Directory");
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        // Create an action listener for the "Choose Directory" button.
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

        // Handle the user pressing the "backup" button.
        backupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Set the "default" values for the number of files to transmit,
                // the port, and the path.
                int filesFound = 0;
                int port = 0;
                String path = "";

                // Collect the user's inputs and validate them.
                try {
                    File chosenDirectory = jfc.getSelectedFile();
                    path = chosenDirectory.getAbsolutePath();
                    port = Integer.valueOf(portField.getText());
                    if (port < 10000) {
                        throw new RuntimeException(
                                "Port numbers < 10000 prohibited!");
                    }
                    
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(
                            clientFrame, "Error: " + exc.getMessage());
                    return;
                }

                // The inputs must have been validated by now (otherwise, the
                // method would have returned). Start collecting files and paths
                // by creating data structures.
                Queue<String> pathsToTry = new LinkedList<>();
                ArrayList<File> fileArrayList = new ArrayList<>();
                pathsToTry.add(path);
                FileUtilities fu = new FileUtilities();
                ArrayList<String> paths = new ArrayList<>();

                // Go through the queue of paths to try
                // - Find all non-directory files at the given path.
                // - Add all sub-directories found at the given path to the
                // queue.
                // - Keep going until the queue is empty (all sub-directories
                // have been tried).
                while (!pathsToTry.isEmpty()) {
                    // Try the path at the front of the queue.
                    String curPath = pathsToTry.remove();
                    paths.add(fu.createShortPath(
                            curPath, path)); // Add the path to the ArrayList of
                    // paths (for transmission).
                    File curPathFile = new File(curPath);
                    // If the "directory" in the queue is actually a file, do
                    // the single-file process on it.
                    if (!curPathFile.isDirectory()) {
                        filesFound++; // Increment the number of files found.
                        fileArrayList.add(
                                curPathFile); // Add the file to the file array list
                        continue; // Don't try the rest of the loop.
                    }

                    // The queue element must actually be a directory (the loop
                    // would have continued otherwise), so list the files in the
                    // directory.
                    File[] filesInDirectory = curPathFile.listFiles();
                    // If the directory is empty, move on.
                    if (filesInDirectory == null) {
                        continue; // Just go on to the next iteration.
                    }

                    // Go through each "file" in the directory.
                    for (File file : filesInDirectory) {
                        // Case 1: It's another directory
                        if (file.isDirectory()) {
                            pathsToTry.add(file.getPath()); // We'll enqueue it
                            // for processing.
                        } else { // Case 2: It's a file.
                            filesFound++; // Increment the number of files
                            // found.
                            fileArrayList.add(
                                    file); // Add the file to the file array list
                        }
                    }
                }

                // Transmit the collected files and their corresponding file
                // header.
                String host = addressField.getText();
                String username = usernameField.getText();
                String password = passwordField.getText();
                // Use try/catch to handle connection errors (but NOT
                // authentication errors).
                try {
                    // Create the socket to communicate over.
                    Socket sock = new Socket(host, port);
                    // Create the stream to send things on.
                    ObjectOutputStream oos =
                            new ObjectOutputStream(sock.getOutputStream());
                    // First: Transmit header data under object
                    // TransmissionHeader. Include the number of files to wait
                    // for.
                    TransmissionHeader transHead = new TransmissionHeader(
                            username, password, filesFound, paths);
                    oos.writeUnshared(transHead);

                    // Receive a reply from the server indicated whether the
                    // user successfully authenticated. Do this by creating an
                    // ObjectInputStream over the socket.
                    ObjectInputStream ois =
                            new ObjectInputStream(sock.getInputStream());
                    boolean success = ois.readBoolean();
                    // If the server indicated failure (non-authentication),
                    // indicate that to the user. End saving operations so the
                    // user can try again.
                    if (!success) {
                        JOptionPane.showMessageDialog(
                                null, "Username/Password Incorrect! Try again.");
                        return;
                    }

                    // Second: Iteratively transmit:
                    // Each file's FileHeader (containing number of sequence
                    // transmissions) Each sequence transmission.
                    // Files are broken into 3072-byte packets.
                    for (File file : fileArrayList) {
                        // Transmit the header
                        FileHeader fh = new FileHeader(
                                fu.createShortPath(file.getAbsolutePath(), path),
                                file.getName(),
                                (int) ((file.length() / 3072)
                                        + ((file.length() % 3072 >= 1
                                        || file.length() == 0)
                                        ? 1
                                        : 0)));
                        oos.writeUnshared(fh);

                        // Transmit the packets
                        int seqNum = 0; // Start at 0
                        FileInputStream fis = new FileInputStream(file);
                        long fileSize = file.length();
                        // Case 1: The file has size 0 (empty file).
                        if (fileSize == 0) {
                            // Just write a blank packet as a placeholder.
                            BytePacket bp =
                                    new BytePacket(new byte[0], seqNum++);
                            oos.writeUnshared(bp);
                        }
                        // Case 2: The file has a non-zero size.
                        // Loop while the file's not-yet-sent size > 0.
                        while (fileSize > 0) {
                            // On each iteration, send the next 3072 bytes of the
                            // file.
                            byte[] moreBytes = new byte[3072];
                            // Reset the ObjectOutputStream to fix a MEMORY LEAK in Java (i.e. the "reference table" of 
                            // stream operations).
                            oos.reset();
                            fis.readNBytes(moreBytes, 0, 3072);
                            BytePacket bp = new BytePacket(moreBytes, seqNum++);
                            oos.writeUnshared(bp);
                            // Reduce the unsent fileSize so that the loop
                            // terminates properly.
                            fileSize = fileSize - (long) 3072;
                        }
                        // Close the FileInputStream, as no more bytes need to
                        // be read.
                        fis.close();
                    }
                   // Read the status from the server.
                    boolean successful = ois.readBoolean();
                    if (successful) {
                        JOptionPane.showMessageDialog(null, "All files transmitted successfully!");
                    } else {
                        JOptionPane.showMessageDialog(null, "There was an error transmitting your files.");
                    }
                    
                    // Close the ObjectOutputStream, as nothing else needs to be
                    // sent.
                    oos.close();
                } catch (Exception exc) {
                    // There was some sort of transmission error. Inform the
                    // user, then allow the user to press the backup button
                    // again.
                    exc.printStackTrace();
                    JOptionPane.showMessageDialog(clientFrame,
                            "Connection or transmission error. Check inputs and try again.");
                }
            }
        });
    }
}

/**
 * Basic utility functions for client-side file operations.
 */
class FileUtilities {
    /**
     * Method to shorten a path to be appropriate for transmission to server.
     * @param longPath The full-length path of the file.
     * @param basePath The base path of the file (i.e. extraneous information
     *     that should not be transmitted, like
     *                 C:\Users\...
     * @return The shortened file path.
     */
    public String createShortPath(String longPath, String basePath) {
        return longPath.substring(basePath.length(), longPath.length());
    }
}