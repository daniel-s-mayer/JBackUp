import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;

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
        JMenuItem stopButton = new JMenuItem("Stop Server");
        JMenuItem startButton = new JMenuItem("Start Server");
        JMenuItem restartButton = new JMenuItem("Restart Server");
        menuBar.add(stopButton);
        menuBar.add(startButton);
        menuBar.add(restartButton);
        mainFrame.setJMenuBar(menuBar);
        
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ServerInstance sm = new ServerInstance(storeServ);
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
        JPanel serverSettingsPanel = new JPanel(new GridLayout(3, 0));
        JPanel portRow = new JPanel();
        JPanel pathRow = new JPanel();
        JPanel savePanel = new JPanel();
        JLabel portLabel = new JLabel("Port:");
        JLabel pathLabel = new JLabel("Path:");
        JTextField portEntry = new JTextField("", 15);
        JTextField pathEntry = new JTextField("", 15);
        JButton backButton = new JButton("Back");
        JButton saveButton = new JButton("Save");
        portRow.add(portLabel);
        portRow.add(portEntry);
        pathRow.add(pathLabel);
        pathRow.add(pathEntry);
        savePanel.add(backButton);
        savePanel.add(saveButton);
        serverSettingsPanel.add(portRow);
        serverSettingsPanel.add(pathRow);
        serverSettingsPanel.add(savePanel);
        JPanel containerPanel = new JPanel();
        serverSettingsPanel.setPreferredSize(new Dimension(300, 200));
        containerPanel.add(serverSettingsPanel);
        
        // Users panel
        JPanel userContentPanel = new JPanel();
        JPanel userListPanel = new JPanel();
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.PAGE_AXIS));
        ServerUtilities su = new ServerUtilities();
        for (User user : storeServ.getUsers()) {
            System.out.println("SU");
            JPanel userJPanel = su.generateUserJPanel(user, storeServ, userListPanel);
            userListPanel.add(userJPanel);
        }
        
        // Add user button
        JButton addUserButton = new JButton("Add User");
        addUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                JTextField passwordField = new JPasswordField("", 20);
                passwordRow.add(passwordField);
                newUserDataPanel.add(nameRow);
                newUserDataPanel.add(usernameRow);
                newUserDataPanel.add(passwordRow);
                JOptionPane.showConfirmDialog(null, newUserDataPanel);
                
                // After submission, create the new user
                User newUser = new User(nameTextField.getText(), usernameTextField.getText(), passwordField.getText());
                storeServ.addUser(newUser);
                su.dumpToFilesystem(storeServ);
                JPanel userJPanel = su.generateUserJPanel(newUser, storeServ, userListPanel);
                userListPanel.add(userJPanel);
                userJPanel.repaint();
                
                
            }
        });
        
        userContentPanel.add(addUserButton);
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
         userContentPanel.add(userListPanel);
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
        
        



        mainFrame.setContentPane(mainPanel);
        mainFrame.setVisible(true);
    }
    
    static void changeUserPassword(StorageServer storeServ, User user) {
        
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
    
    public void changeUserPassword(StorageServer storeServ, User user) {
        user.editPassword(JOptionPane.showInputDialog("Enter a new password:"));
        this.dumpToFilesystem(storeServ);
    }
    
    public JPanel generateUserJPanel(User user, StorageServer storeServ, JPanel userListPanel) {
        JPanel userJPanel = new JPanel();
        userJPanel.add(new JLabel(user.getName() + " " + user.getUserName()));
        JButton deleteUser = new JButton("Delete User");
        JButton changePassword = new JButton("Change User Password");
        deleteUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteUserAction(storeServ, user);
                userListPanel.remove(userJPanel);
            }
        });
        changePassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeUserPassword(storeServ, user);
            }
        });
        return userJPanel;
    }
}
