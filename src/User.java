import java.io.Serializable;

public class User implements Serializable {
    private String name;
    private String userName;
    private String password; // For now. We might try something other than a string later.
    
    User(String name, String userName, String password) {
        this.name = name;
        this.userName = userName;
        this.password = password;
    }
    
    public void editName(String newName) {
        this.name = newName;
    }
    
    public void editUsername(String newUsername) {
        this.userName = newUsername;
    }
    
    public void editPassword(String newPassword) {
        this.password = newPassword;
    }
    
    public String getName() {
        return name;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public String getPassword() {
        return password;
    }
}
