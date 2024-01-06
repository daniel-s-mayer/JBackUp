import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Arrays;

public class ServerManager {
    public static void main(String[] args) {
        // Handle opening/creating the storage server
        ServerUtilities su = new ServerUtilities();
        StorageServer storeServe = su.readFromFilesystem();
       
       // Start the GUI component
        SwingUtilities.invokeLater(new ServerGUI(storeServe));
    }
}

class ServerGUI implements Runnable {
    StorageServer storeServ;
    ServerGUI(StorageServer storeServ) {
        this.storeServ = storeServ;
    }
    
    @Override
    public void run() {
        JFrame mainFrame = new JFrame("Server Management Utility");
        mainFrame.setSize(800, 800);
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
        GridBagLayout gridLayout = new GridBagLayout();
        JButton manageUsers = new JButton("Manage Users");
        JButton manageServerSettings = new JButton("Server Settings");
        mainPanel.setLayout(gridLayout);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(manageUsers, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        mainPanel.add(manageUsers, gbc);
        mainPanel.add(manageServerSettings);
        
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
            JPanel setupPanel = new JPanel();
            JTextField portInput = new JTextField("Port:");
            JTextField pathInput = new JTextField("Path:");
            setupPanel.add(portInput);
            setupPanel.add(pathInput);
            String.valueOf(JOptionPane.showConfirmDialog(null, setupPanel));

            // Read the values and create storeServe.
            storeServe = new StorageServer(Integer.valueOf(portInput.getText()), pathInput.getText());

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
        userJPanel.add(new JLabel(user.getName() + " " + user.getUserName()));
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
        for (User u : storeServ.getUsers()) {
            JPanel userJPanel = this.generateUserJPanel(sm, u, storeServ, userContentPanel, mainFrame, userControlsPanel);
            userJPanel.setPreferredSize(new Dimension(500, 50));
            userListPanel.add(userJPanel);
        }
        JPanel view = new JPanel();
        JScrollPane jsp = new JScrollPane();
        jsp.setPreferredSize(new Dimension(1000, 500));
        jsp.setViewportView(view);
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
