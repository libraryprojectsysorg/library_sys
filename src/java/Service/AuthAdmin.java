
package Service;
import org.example.Admin;

import java.util.ArrayList;
import java.util.List;

public class AuthAdmin {
    private List<Admin> admins = new ArrayList<>();
    private boolean isLoggedIn = false;

    public  AuthAdmin() {
        admins.add(new Admin("ws2022@gmail.com", "ws1234"));
    }

    public boolean login(String email, String password) {
        for (Admin admin : admins) {
            if (admin.getEmail().equals(email) && admin.getPassword().equals(password)) {
                isLoggedIn = true;
                return true;
            }
        }
        return false;
    }

    public void logout() {
        isLoggedIn = false;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }
}
