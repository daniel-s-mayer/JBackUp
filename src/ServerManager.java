import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;

/**
 * This class handles the server management GUI, including user management and
 * server settings management.
 */
public class ServerManager {
    public static void main(String[] args) {
        // Read the existing server settings file from the filesystem (or show
        // prompts to create a new one).
        ServerUtilities su = new ServerUtilities();
        StorageServer storeServe = su.readFromFilesystem();

        // Start the GUI component
        SwingUtilities.invokeLater(new ServerGUI(storeServe));
    }
}

/**
 * Implements the GUI components of the server management utility, including the
 * graphical inputs of user and server information.
 *
 * Uses a main JFrame and updates panels within it.
 */
class ServerGUI implements Runnable {
    // The current StorageServer data object being used.
    StorageServer storeServ;

    /**
     * Constructor for a new GUI instance.
     * @param storeServ Data file for the current server of concern.
     */
    ServerGUI(StorageServer storeServ) {
        this.storeServ = storeServ;
    }

    /**
     * Interface-required run method for the management utility GUI.
     */
    @Override
    public void run() {
        // Try to set the Look and Feel to "Windows."
        try {
            for (UIManager.LookAndFeelInfo info :
                    UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Just use the default if Windows is unavailable.
        }

        // Create and initialize the main JFrame for the utility.
        JFrame mainFrame = new JFrame("Server Management Utility");
        mainFrame.setSize(1000, 800);
        mainFrame.setLocationRelativeTo(null);

        // Create the stop/start/restart menu bar
        JMenuBar menuBar = new JMenuBar();
        JButton stopButton = new JButton("Stop Server");
        JButton startButton = new JButton("Start Server");
        JButton restartButton = new JButton("Restart Server");
        menuBar.add(stopButton);
        menuBar.add(startButton);
        menuBar.add(restartButton);
        mainFrame.setJMenuBar(menuBar);

        // Handle the starting and stopping of the server instance.
        // Create a ServerInstance that can be used throughout program
        // operation.
        ServerInstance sm = new ServerInstance(storeServ);
        stopButton.setEnabled(false);
        restartButton.setEnabled(false);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sm.startInstance();
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                restartButton.setEnabled(true);
                // startButton.
            }
        });
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sm.stopInstance();
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                restartButton.setEnabled(false);
            }
        });

        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sm.stopInstance();
                sm.startInstance();
            }
        });

        // Set up the main entry screen.
        JPanel mainPanel = new JPanel();
        JButton manageUsers = new JButton("Manage Users");
        manageUsers.setPreferredSize(new Dimension(250, 100));
        manageUsers.setIcon(new ImageIcon(getClass().getResource("user.png")));
        manageUsers.setFont(new Font("Arial", Font.BOLD, 14));
        manageUsers.setPreferredSize(new Dimension(250, 100));
        JButton manageServerSettings = new JButton("Server Settings");
        manageServerSettings.setFont(new Font("Arial", Font.BOLD, 14));
        manageServerSettings.setPreferredSize(new Dimension(250, 100));
        manageServerSettings.setIcon(
                new ImageIcon(getClass().getResource("settings.png")));

        JPanel mainButtonsPanel = new JPanel();
        mainButtonsPanel.add(manageUsers);
        mainButtonsPanel.add(manageServerSettings);
        mainPanel.add(mainButtonsPanel, BorderLayout.CENTER);

        // Set up the server settings screen.
        JPanel serverSettingsPanel = new JPanel();
        serverSettingsPanel.setLayout(
                new BoxLayout(serverSettingsPanel, BoxLayout.PAGE_AXIS));
        JPanel portRow = new JPanel();
        JPanel pathRow = new JPanel();
        JPanel savePanel = new JPanel();
        JLabel portLabel = new JLabel("Port:");
        JLabel pathLabel = new JLabel("Path:");
        JTextField portEntry = new JTextField("", 15);
        portEntry.setText(String.valueOf(
                storeServ
                        .getPort())); // Pre-set the port to the previously chosen port.
        // Use a JButton activated JFileChooser to allow the user to select a
        // storage path.
        JFileChooser pathChooser = new JFileChooser();
        JButton chooseDirectory = new JButton("Choose Storage Directory");
        chooseDirectory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Force the file chooser to work on directories only and set it
                // to the previously-selected directory.
                pathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                pathChooser.setSelectedFile(
                        new File(storeServ.getStorageDirectory()));
                pathChooser.showOpenDialog(serverSettingsPanel);
            }
        });
        JButton backButton = new JButton("Back");
        JButton saveButton = new JButton("Save");
        portRow.add(portLabel);
        portRow.add(portEntry);
        pathRow.add(pathLabel);
        pathRow.add(chooseDirectory);
        savePanel.add(backButton);
        savePanel.add(saveButton);
        serverSettingsPanel.add(portRow);
        serverSettingsPanel.add(pathRow);
        serverSettingsPanel.add(savePanel);
        JPanel containerPanel = new JPanel();
        serverSettingsPanel.setPreferredSize(new Dimension(600, 200));
        containerPanel.add(serverSettingsPanel);
        // Handle the user clicking the save button on the server settings
        // panel.

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Value check: Make sure that the port is an integer >= 0 and <
                // 100000
                int port = 0;
                try {
                    port = Integer.valueOf(portEntry.getText());
                    if (port < 10000) {
                        throw new RuntimeException();
                    }
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(
                            null, "Error! Check your port number (port must be >= 10000).");
                    return; // Exit the saving operation so that the user can
                    // try again.
                }

                // Try to get the selected directory
                // Use try/catch to catch any errors with the user's selection
                File selectedDirectory = null;
                try {
                    selectedDirectory = pathChooser.getSelectedFile();
                    selectedDirectory.getAbsolutePath();
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(null,
                            "Error! Make sure that you selected a valid directory.");
                    return; // Exit the saving operation so that the user can
                    // try again.
                }

                // Actually save the new values to the filesystem.
                storeServ.setPort(port);
                storeServ.setStorageDirectory(
                        selectedDirectory.getAbsolutePath());
                ServerUtilities su = new ServerUtilities();
                su.dumpToFilesystem(storeServ);

                // If the server is currently running, restart it to apply the
                // changes.
                if (sm.getStatus()) {
                    sm.stopInstance();
                    sm.startInstance();
                }
                
                // Show a JOptionPane confirming that data was saved.
                JOptionPane.showMessageDialog(null, "Settings saved successfully!");
            }
        });

        // Create the user management screen.
        JButton addUserButton = new JButton("Add User");
        JButton backAddUserButton = new JButton("Back");
        JPanel userControlsPanel = new JPanel();
        userControlsPanel.add(backAddUserButton);
        userControlsPanel.add(addUserButton);
        JPanel userContentPanel = new JPanel(new BorderLayout());
        JPanel userListPanel = new JPanel();
        userListPanel.setLayout(
                new BoxLayout(userListPanel, BoxLayout.PAGE_AXIS));
        ServerUtilities su = new ServerUtilities();
        // "Process the change" to initially set up the user list panel.
        su.processChange(
                sm, storeServ, userContentPanel, mainFrame, userControlsPanel);

        // Display a JOptionPane for user addition on request.
        addUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create the JOptionPane to take input of the new user's
                // information. Use an infinite loop to continue requesting the
                // information until the user enters a valid value or quits.
                while (true) {
                    // Create a JPanel for the JOptionPane.
                    JPanel newUserDataPanel = new JPanel(new GridLayout(0, 1));
                    JPanel nameRow = new JPanel();
                    JPanel usernameRow = new JPanel();
                    JPanel passwordRow = new JPanel();
                    nameRow.add(new JLabel("Name:"));
                    JTextField nameTextField = new JTextField("", 20);
                    nameRow.add(nameTextField);
                    usernameRow.add(new JLabel("Username:"));
                    JTextField usernameTextField = new JTextField("", 20);
                    usernameRow.add(usernameTextField);
                    passwordRow.add(new JLabel("Password:"));
                    JPasswordField passwordField = new JPasswordField("", 20);
                    passwordRow.add(passwordField);
                    newUserDataPanel.add(nameRow);
                    newUserDataPanel.add(usernameRow);
                    newUserDataPanel.add(passwordRow);
                    // Show the JOptionPane and collect the result.
                    int paneResult =
                            JOptionPane.showConfirmDialog(null, newUserDataPanel,
                                    "New User Details", JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.PLAIN_MESSAGE);
                    if (paneResult == 0) {
                        // The user clicked OK (i.e. the user wants to save the
                        // new user. Retrieve the inputs.
                        String fullName = nameTextField.getText();
                        String userName = usernameTextField.getText();
                        String passwordString =
                                new String(passwordField.getPassword());
                        // Validate the inputs.
                        boolean passed =
                                true; // This will be changed if any test fails.
                        // Check for duplicates
                        for (User u : storeServ.getUsers()) {
                            if (u.getUserName().equals(userName)) {
                                passed = false;
                                break;
                            }
                        }
                        // Check that the username has a sufficient length.
                        if (userName.length() < 1) {
                            passed = false;
                        }
                        // Check that the password has a sufficient length.
                        if (passwordString.length() < 6) {
                            passed = false;
                        }
                        // Apply the result of the checks.
                        if (passed) {
                            // All checks passed, so save the new user to the
                            // filesystem and exit the loop.
                            User newUser =
                                    new User(fullName, userName, passwordString);
                            storeServ.addUser(newUser);
                            su.dumpToFilesystem(storeServ);
                            su.processChange(sm, storeServ, userContentPanel,
                                    mainFrame, userControlsPanel);
                            break;
                        } else {
                            // Some check failed, so ask the user to check their
                            // inputs and continue the loop.
                            JOptionPane.showMessageDialog(null,
                                    "Error: Your username/password are invalid. Try again.");
                        }

                    } else {
                        break; // The user selected a stopping option.
                    }
                }
            }
        });
        // Create action listeners to handle main-screen button clicks.
        buttonActionListener(mainFrame, manageUsers, manageServerSettings,
                containerPanel, userContentPanel);
        buttonActionListener(
                mainFrame, backAddUserButton, backButton, mainPanel, mainPanel);
        // Actually display the main frame.
        mainFrame.setContentPane(mainPanel);
        mainFrame.setVisible(true);
    }

    /**
     * Method to create action listeners for main-screen buttons.
     * @param mainFrame The frame in which the utility is running.
     * @param manageUsers The manageUsers button on the main screen.
     * @param manageServerSettings The manageServerSettings button on the main
     *     screen.
     * @param containerPanel The containerPanel containing the server settings
     *     screen.
     * @param userContentPanel The userContentPanel containing the user
     *     management screen.
     */
    private void buttonActionListener(JFrame mainFrame, JButton manageUsers,
                                      JButton manageServerSettings, JPanel containerPanel,
                                      JPanel userContentPanel) {
        // Add an action listener to the manageServerSettings button.
        manageServerSettings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.setContentPane(containerPanel);
                mainFrame.setVisible(true);
            }
        });
        // Add an action listener to the manageUsers button.
        manageUsers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.setContentPane(userContentPanel);
                mainFrame.setVisible(true);
            }
        });
    }
}

/**
 * This class provides various basic server utilities, including filesystem
 * access and panel generation for user- specific JPanels.
 */
class ServerUtilities {
    /**
     * Read the storage server contained in myServer.dat from the filesystem if
     * it exists, or create a blank new one if not.
     */
    public StorageServer readFromFilesystem() {
        // storeServe will be either read from the filesystem or created.
        StorageServer storeServe = null;
        // Try to read the current settings from the filesystem.
        try {
            FileInputStream fis = new FileInputStream("myServer.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            storeServe = (StorageServer) ois.readUnshared();
            ois.close();
            // The user already has file settings, so just continue.
        } catch (Exception ie) {
            // The user doesn't have server settings, so display a JOptionPane
            // so that the user can create them. Use an infinite loop to
            // continue asking until a valid input is received.
            while (true) {
                try {
                    // Create the JPanel for the JOptionPane.
                    JPanel setupPanel = new JPanel(new GridLayout(0, 1));
                    JPanel portPanel = new JPanel();
                    JPanel pathPanel = new JPanel();
                    JLabel portLabel = new JLabel("Server Port:");
                    JTextField portInput = new JTextField("", 10);
                    portPanel.add(portLabel);
                    portPanel.add(portInput);
                    JLabel pathLabel = new JLabel("Data Storage Path:");
                    // Use a button-enabled JFileChooser to allow the user to
                    // choose the storage directory.
                    JButton pathButton = new JButton("Choose Directory");
                    JFileChooser pathChooser = new JFileChooser();
                    pathPanel.add(pathLabel);
                    pathPanel.add(pathButton);
                    // Create an action listener to activate the JFileChooser
                    // when the button is clicked.
                    pathButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            pathChooser.setFileSelectionMode(
                                    JFileChooser.DIRECTORIES_ONLY);
                            pathChooser.showOpenDialog(null);
                        }
                    });
                    setupPanel.add(portPanel);
                    setupPanel.add(pathPanel);
                    // Show the JOptionPane to actually get input from the user.
                    int result = JOptionPane.showConfirmDialog(null, setupPanel,
                            "Initial Setup", JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE);
                    // Process the user's inputs.
                    String basePath = "";
                    int port = 0;
                    // If the user selected OK (0), attempt to read the user's
                    // inputs.
                    if (result == 0) {
                        port = Integer.valueOf(portInput.getText());
                        if (port < 10000) {
                            throw new RuntimeException("Invalid port! Ports must be >= 10000");
                        }
                        basePath =
                                pathChooser.getSelectedFile().getAbsolutePath();
                    } else {
                        break; // The user cancelled. Break the loop to stop
                        // asking the user for input.
                    }
                    //  Using the extracted values, create the new StorageServer
                    //  object.
                    storeServe = new StorageServer(port, basePath);
                    // Store the new StorageServer to the filesystem.
                    ServerUtilities su = new ServerUtilities();
                    su.dumpToFilesystem(storeServe);
                    break;
                } catch (Exception e) {
                    // The user's input must have been invalid at some point, so
                    // keep looping and ask again.
                    JOptionPane.showMessageDialog(
                            null, "Invalid input. Try again.");
                }
            }
        }
        return storeServe;
    }

    /**
     * Save the provided StorageServer to the filesystem or tell the user it is
     * impossible.
     * @param storeServe The StorageServer to be stored.
     */
    public void dumpToFilesystem(StorageServer storeServe) {
        try {
            FileOutputStream fos = new FileOutputStream("myServer.dat");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeUnshared(storeServe);
            oos.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null, "Disaster! Unrecoverable error!");
            System.exit(
                    99); // This is an unrecoverable error, so kill the program.
        }
    }

    /**
     * Utility function to handle the deletion of users.
     * @param storeServ The StorageServer containing the user to be deleted.
     * @param user The user to be deleted.
     */
    public void deleteUserAction(StorageServer storeServ, User user) {
        storeServ.removeUser(user);
        this.dumpToFilesystem(storeServ);
    }

    /**
     * Utility function to collect a new password for a user and change that
     * user's password to the new password.
     * @param sm The ServerInstance being used throughout the program.
     * @param storeServ The StorageServer containing the user in question.
     * @param user The user whose password is to be changed.
     */
    public void changeUserPassword(
            ServerInstance sm, StorageServer storeServ, User user) {
        // Use an infinite loop to continue asking the user for a valid password
        // until a valid password is entered.
        while (true) {
            // Create the JPanel to ask the user for a new password with a
            // JOptionPane.
            JPanel newPassPanel = new JPanel();
            JPasswordField JNewPasswordField = new JPasswordField("", 20);
            JLabel newPasswordFieldLabel = new JLabel("New Password: ");
            newPassPanel.add(newPasswordFieldLabel);
            newPassPanel.add(JNewPasswordField);
            // Show the JOptionPane to the user to request input.
            int inputResult = JOptionPane.showConfirmDialog(null, newPassPanel,
                    "New Password Input", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (inputResult == 0) {
                // This means that OK was selected (the user wants to save the
                // password). Read the password and validate the length.
                String fieldInput = new String(JNewPasswordField.getPassword());
                if (fieldInput.length() < 6) {
                    // The password's length was insufficient.
                    JOptionPane.showMessageDialog(null,
                            "Error: Password does not meet minimum requirements (6 characters).");
                    continue; // Continue the loop instead of storage data.
                }
                user.editPassword(fieldInput);
                // Store the new password to the filesystem (this is only
                // reached if the password was valid).
                this.dumpToFilesystem(storeServ);
                // If the server is already running, restart it to apply
                // changes.
                if (sm.getStatus()) {
                    sm.stopInstance();
                    sm.startInstance();
                }
                // Stop the infinite loop.
                break;
            } else {
                // The user selected a cancel option, so break the loop.
                break;
            }
        }
    }

    /**
     * Method to generate a JPanel for a specific user; to be used for the list
     * of users.
     * @param sm The ServerInstance being used throughout the program.
     * @param user The user whose panel is being created.
     * @param storeServ The StorageServer containing the user.
     * @param userContentPanel The overall userContentPanel on which the user
     *     list will be placed.
     * @param mainFrame The main JFrame of the program.
     * @param userControlsPanel The panel with the "Add User" and "Back" buttons
     *     on the user management screen.
     * @return Returns the JPanel for User user.
     */
    public JPanel generateUserJPanel(ServerInstance sm, User user,
                                     StorageServer storeServ, JPanel userContentPanel, JFrame mainFrame,
                                     JPanel userControlsPanel) {
        // Initialize the new JPanel.
        JPanel userJPanel = new JPanel();
        // Put the formatted contents onto the panel.
        String userJPText = String.format(
                "%-20s     %-20s     ", user.getName(), user.getUserName());
        JLabel userJPTextLabel = new JLabel(userJPText);
        userJPTextLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        userJPanel.add(userJPTextLabel);
        JButton deleteUser = new JButton("Delete User");
        JButton changePassword = new JButton("Change User Password");
        userJPanel.add(deleteUser);
        userJPanel.add(changePassword);
        // Create an ActionListener for the "Delete User" button.
        deleteUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Call utility methods to process the change.
                deleteUserAction(storeServ, user);
                processChange(sm, storeServ, userContentPanel, mainFrame,
                        userControlsPanel);
            }
        });
        // Create an ActionListener for the "Change Password" button.
        changePassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Call a utility method to process the change.
                changeUserPassword(sm, storeServ, user);
            }
        });
        return userJPanel;
    }

    /**
     * Utility method to process changes in users (i.e. addition or deletion of
     * a user).
     * @param sm The ServerInstance being used throughout the program.
     * @param storeServ The StorageServer containing (or that will contain) the
     *     user in question.
     * @param userContentPanel The main panel in the manage users section of the
     *     program.
     * @param mainFrame The program's main JFrame.
     * @param userControlsPanel The panel containing "Add User" and "Back"
     *     buttons on the user management screen.
     */
    public void processChange(ServerInstance sm, StorageServer storeServ,
                              JPanel userContentPanel, JFrame mainFrame, JPanel userControlsPanel) {
        // Create a new JPanel for the list of users.
        JPanel userListPanel = new JPanel();
        userListPanel.setLayout(new BoxLayout(userListPanel,
                BoxLayout.Y_AXIS)); // Use a BoxLayout for flexibility
        // with sizing.
        int userCount = 0;
        for (User u : storeServ.getUsers()) {
            // Get each user in storeServe, create a panel for them, and add the
            // user's panel to the list of users.
            JPanel userJPanel = this.generateUserJPanel(sm, u, storeServ,
                    userContentPanel, mainFrame, userControlsPanel);
            userJPanel.setPreferredSize(new Dimension(800, 50));
            userListPanel.add(userJPanel);
            userCount++;
        }
        // Place the list of users within a JScrollPane.
        JPanel view = new JPanel();
        JScrollPane jsp = new JScrollPane();
        jsp.setPreferredSize(new Dimension(1000, 500));
        jsp.setViewportView(view);
        // Display a special message if there are no users.
        if (userCount == 0) {
            userListPanel.add(new JLabel("No users yet."));
        }
        view.add(userListPanel);
        userContentPanel.removeAll();
        userContentPanel.repaint();
        userContentPanel.add(userControlsPanel, BorderLayout.NORTH);
        userContentPanel.add(jsp, BorderLayout.CENTER);
        // Actually show the new user list.
        mainFrame.setVisible(true);
        // If the server is already running, restart the server to apply the
        // change in users.
        if (sm.getStatus()) {
            sm.stopInstance();
            sm.startInstance();
        }
    }
}
