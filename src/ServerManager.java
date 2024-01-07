import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

/**
 * This class handles the server management GUI, including user management and server settings management.
 */
public class ServerManager {
    public static void main(String[] args) {
        // Read the existing server settings file from the filesystem (or show prompts to create a new one).
        ServerUtilities su = new ServerUtilities();
        StorageServer storeServe = su.readFromFilesystem();
        
       // Start the GUI component
        SwingUtilities.invokeLater(new ServerGUI(storeServe));
    }
}

/**
 * Implements the GUI components of the server management utility, including the graphical inputs of user and server 
 * information. 
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
    
    @Override
    public void run() {
        // Immediately set the look and feel
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
        
        JFrame mainFrame = new JFrame("Server Management Utility");
        mainFrame.setSize(1000, 800);
        // Create the stop/start/restart menu bar
        JMenuBar menuBar = new JMenuBar();
        JButton stopButton = new JButton("Stop Server");
        JButton startButton = new JButton("Start Server");
        JButton restartButton = new JButton("Restart Server");
        menuBar.add(stopButton);
        menuBar.add(startButton);
        menuBar.add(restartButton);
        mainFrame.setJMenuBar(menuBar);
        
        
        // Handle the starting and stopping of the server.
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
                //startButton.
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
        
        
        
        
        // Main screen layout
        
        JPanel mainPanel = new JPanel();
        JButton manageUsers = new JButton("Manage Users");
        manageUsers.setPreferredSize(new Dimension(250, 100));
        manageUsers.setIcon(new ImageIcon(getClass().getResource("user.png")));
        manageUsers.setFont(new Font("Arial", Font.BOLD, 14));
        manageUsers.setPreferredSize(new Dimension(250, 100));
        JButton manageServerSettings = new JButton("Server Settings");
        manageServerSettings.setFont(new Font("Arial", Font.BOLD, 14));
        manageServerSettings.setPreferredSize(new Dimension(250, 100));
        manageServerSettings.setIcon(new ImageIcon(getClass().getResource("settings.png")));

        JPanel mainButtonsPanel = new JPanel();
        mainButtonsPanel.add(manageUsers);
        mainButtonsPanel.add(manageServerSettings);
        mainPanel.add(mainButtonsPanel, BorderLayout.CENTER);
        
        // Server settings panel
        JPanel serverSettingsPanel = new JPanel();
        serverSettingsPanel.setLayout(new BoxLayout(serverSettingsPanel, BoxLayout.PAGE_AXIS));
        JPanel portRow = new JPanel();
        JPanel pathRow = new JPanel();
        JPanel savePanel = new JPanel();
        JLabel portLabel = new JLabel("Port:");
        JLabel pathLabel = new JLabel("Path:");
        JTextField portEntry = new JTextField("", 15);
        JFileChooser pathChooser = new JFileChooser();
        JButton chooseDirectory = new JButton("Choose Storage Directory");
        chooseDirectory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ** TODO Add error handling. 
                pathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                pathChooser.setSelectedFile(new File(storeServ.getStorageDirectory()));
                pathChooser.showOpenDialog(serverSettingsPanel);
            }
        });
        
        
        //JTextField pathEntry = JFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)// new JTextField("", 15);
        // Pre-Set Text
        //pathEntry.setText(storeServ.getStorageDirectory());
        portEntry.setText(String.valueOf(storeServ.getPort()));
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
        
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Value check: Make sure that the port is valid.
                int port = 0;
                try {
                    port = Integer.valueOf(portEntry.getText());
                    if (port < 0 || port > 100000) {
                        throw new RuntimeException();
                    }
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(null, "Error! Check your port number.");
                    return;
                }
                File selectedDirectory = pathChooser.getSelectedFile();
                // Save the values.
                storeServ.setPort(port);
                storeServ.setStorageDirectory(selectedDirectory.getAbsolutePath());
                ServerUtilities su = new ServerUtilities();
                su.dumpToFilesystem(storeServ);
                if (sm.getStatus()) {
                    sm.stopInstance();
                    sm.startInstance();
                }
            }
        });
        
        
        
        // Users panel
        JButton addUserButton = new JButton("Add User");
        JButton backAddUserButton = new JButton("Back");
        JPanel userControlsPanel = new JPanel();
        userControlsPanel.add(backAddUserButton);
        userControlsPanel.add(addUserButton);
        JPanel userContentPanel = new JPanel(new BorderLayout());
        JPanel userListPanel = new JPanel();
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.PAGE_AXIS));
        ServerUtilities su = new ServerUtilities();
        su.processChange(sm, storeServ, userContentPanel, mainFrame, userControlsPanel);
        

        addUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create the JOptionPane to take input of the new user's information.
                while (true) {
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
                    int paneResult = JOptionPane.showConfirmDialog(null, newUserDataPanel, "New User Details", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (paneResult == 0) {
                        // The user clicked OK.
                        // Validate inputs
                        String fullName = nameTextField.getText();
                        String userName = usernameTextField.getText();
                        String passwordString = new String(passwordField.getPassword());
                        boolean passed = true; // This will be changed if any test fails.
                        // Dup check
                        for (User u : storeServ.getUsers()) {
                            if (u.getUserName().equals(userName)) {
                                passed = false;
                            }
                        }
                        // Length check
                        if (userName.length() < 1) {
                            passed = false;
                        }
                        if (passwordString.length() < 6) {
                            passed = false;
                        }
                        
                        // Use the checks
                        if (passed) {
                            User newUser = new User(fullName, userName, passwordString);
                            storeServ.addUser(newUser);
                            su.dumpToFilesystem(storeServ);
                            su.processChange(sm, storeServ, userContentPanel, mainFrame, userControlsPanel);
                            break;
                        } else {
                            JOptionPane.showMessageDialog(null, "Error: Your username/password are invalid. Try again.");
                        }
                        
                    } else {
                        break; // The user selected a stopping option. 
                    }
                }
               
                
            }
        });
        
        /*JPanel userGrid = new JPanel(new GridLayout(0, 1));
        User[] users = new User[4];
        JList<User> userList = new JList<User>(users);
        userList.setCellRenderer(new ListCellRenderer<User>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends User> list, User value, int index, boolean isSelected, boolean cellHasFocus) {
                JPanel nJP = new JPanel();
                nJP.add(new JLabel("shit"));
                nJP.add(new JButton("SS"));
                return nJP;
            }
            
        });
        JScrollPane userScrollPane = new JScrollPane();
        userScrollPane.setViewportView(userGrid);
        //S//tring[] users = {"sgg", "gg"};
        //userScrollPane.setPreferredSize(new Dimension(1000, 500));
        userContentPanel.add(userList);//userScrollPane);
        
        //usersPanel.add(userPane);
        */
        /*JPanel userButtonsPanel = new JPanel();
        userButtonsPanel.setLayout(new BoxLayout(userButtonsPanel, BoxLayout.PAGE_AXIS));
        JPanel btnPanel1 = new JPanel();
        JPanel btnPanel2 = new JPanel();
        btnPanel1.add(new JButton("gg"));
        btnPanel2.add(new JButton("tt"));
        userButtonsPanel.add(btnPanel1);
        userButtonsPanel.add(btnPanel2);
        userContentPanel.add(userButtonsPanel);
        */
        // Click actions
        manageServerSettings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.setContentPane(containerPanel);
                mainFrame.setVisible(true);
            }
        });
        manageUsers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.setContentPane(userContentPanel);
                mainFrame.setVisible(true);
            }
        });
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.setContentPane(mainPanel);
                mainFrame.setVisible(true);
            }
        });
        backAddUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.setContentPane(mainPanel);
                mainFrame.setVisible(true);
            }
        });
        
        



        mainFrame.setContentPane(mainPanel);
        mainFrame.setVisible(true);
    }
    

   
}

class ServerUtilities {
    /**
     * Read the storage server contained in myServer.dat from the filesystem if it exists, or create a blank new one if not. 
     */
    public StorageServer readFromFilesystem() {
        StorageServer storeServe = null;
        try {
            FileInputStream fis = new FileInputStream("myServer.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            storeServe = (StorageServer) ois.readObject();
            ois.close();
            // The user already has file settings, so just continue. 
        }  catch (Exception ie) {
            // The user doesn't have server settings, so let's create them.
            // Create the panel
            while (true) {
                try {
                    JPanel setupPanel = new JPanel(new GridLayout(0, 1));
                    JPanel portPanel = new JPanel();
                    JPanel pathPanel = new JPanel();
                    JLabel portLabel = new JLabel("Server Port:");
                    JTextField portInput = new JTextField("", 10);
                    portPanel.add(portLabel);
                    portPanel.add(portInput);
                    JButton pathButton = new JButton("Choose Directory");
                    JLabel pathLabel = new JLabel("Data Storage Path:");
                    JFileChooser pathChooser = new JFileChooser();
                    pathPanel.add(pathLabel);
                    pathPanel.add(pathButton);
                    pathButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // ** TODO Add error handling. 
                            pathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                            pathChooser.showOpenDialog(null);
                        }
                    });
                    String basePath = "";
                    int port = 0;
                    
                    setupPanel.add(portPanel);
                    setupPanel.add(pathPanel);
                   int result = JOptionPane.showConfirmDialog(null, setupPanel, "Initial Setup", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                   if (result == 0) {
                       port = Integer.valueOf(portInput.getText());
                       basePath = pathChooser.getSelectedFile().getAbsolutePath();
                   } else {
                       break; // The user canceled.
                   }
                    // Read the values and create storeServe.
                    storeServe = new StorageServer(port, basePath);

                    // Store the new serve.
                    try {
                        FileOutputStream fos = new FileOutputStream("myServer.dat");
                        ObjectOutputStream oos = new ObjectOutputStream(fos);
                        oos.writeObject(storeServe);
                        oos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.exit(99);
                    }
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Invalid input. Try again.");
                }
            }
           
        }
        return storeServe;
    }
    
    public void dumpToFilesystem(StorageServer storeServe) {
        try {
            FileOutputStream fos = new FileOutputStream("myServer.dat");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(storeServe);
            oos.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Disaster! Unrecoverable error!");
            System.exit(99);
        }
    }

    public void deleteUserAction(StorageServer storeServ, User user) {
        storeServ.removeUser(user);
        this.dumpToFilesystem(storeServ);
    }
    
    public void changeUserPassword(ServerInstance sm, StorageServer storeServ, User user) {
        while (true) {
            // Use an infinite loop to continue asking the user for a valid password until a valid password is entered. 
            JPanel newPassPanel = new JPanel();
            JPasswordField JNewPasswordField = new JPasswordField("", 20);
            JLabel newPasswordFieldLabel = new JLabel("New Password: ");
            newPassPanel.add(newPasswordFieldLabel);
            newPassPanel.add(JNewPasswordField);
            int inputResult = JOptionPane.showConfirmDialog(null, newPassPanel, "New Password Input", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (inputResult == 0) {
                // This means that OK was selected.
                String fieldInput = new String(JNewPasswordField.getPassword());
                if (fieldInput.length() < 6) {
                    JOptionPane.showMessageDialog(null, "Error: Password does not meet minimum requirements.");
                    continue;
                }
                System.out.println("Stored: " + fieldInput);
                user.editPassword(fieldInput);
                this.dumpToFilesystem(storeServ);
                if (sm.getStatus()) {
                    // This way, we only restart the server if its already running.
                    sm.stopInstance();
                    sm.startInstance();
                }
                break;
            } else {
                // The user wants to cancel
                break;
            }
        }
        
        
        
    }
    
    public JPanel generateUserJPanel(ServerInstance sm, User user, StorageServer storeServ, JPanel userContentPanel, JFrame mainFrame, JPanel userControlsPanel) {
        JPanel userJPanel = new JPanel();
        String userJPText = String.format("%-20s     %-20s     ", user.getName(), user.getUserName());
        System.out.println("Displaying:" + String.format("%-20s     %-20s     ", user.getName(), user.getUserName()));
        JLabel userJPTextLabel = new JLabel(userJPText);
        userJPTextLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        userJPanel.add(userJPTextLabel);//user.getName() + " " + user.getUserName()));
        JButton deleteUser = new JButton("Delete User");
        JButton changePassword = new JButton("Change User Password");
        userJPanel.add(deleteUser);
        userJPanel.add(changePassword);
        deleteUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteUserAction(storeServ, user);
                processChange(sm, storeServ, userContentPanel, mainFrame, userControlsPanel);
            }
        });
        changePassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeUserPassword(sm, storeServ, user);
            }
        });
        return userJPanel;
    }
    
    public void processChange(ServerInstance sm, StorageServer storeServ, JPanel userContentPanel, JFrame mainFrame, JPanel userControlsPanel) {
        JPanel userListPanel = new JPanel();
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));
        int userCount = 0;
        for (User u : storeServ.getUsers()) {
            JPanel userJPanel = this.generateUserJPanel(sm, u, storeServ, userContentPanel, mainFrame, userControlsPanel);
            userJPanel.setPreferredSize(new Dimension(800, 50));
            userListPanel.add(userJPanel);
            userCount++;
        }
        JPanel view = new JPanel();
        JScrollPane jsp = new JScrollPane();
        jsp.setPreferredSize(new Dimension(1000, 500));
        jsp.setViewportView(view);
        if (userCount == 0) {
            userListPanel.add(new JLabel("No users yet."));
        }
        view.add(userListPanel);
        userContentPanel.removeAll();
        userContentPanel.repaint();
        userContentPanel.add(userControlsPanel, BorderLayout.NORTH);
        userContentPanel.add(jsp, BorderLayout.CENTER);//userListPanel, BorderLayout.CENTER);
        mainFrame.setVisible(true);
        // Now, we also need to restart the server so that the update takes effect.
        if (sm.getStatus()) {
            // This conditional means that we only restart if it's already running.
            sm.stopInstance();
            sm.startInstance();
        }
       
    }
}
