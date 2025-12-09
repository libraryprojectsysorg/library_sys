package org.library.Service.Strategy;

import org.library.domain.User;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserFileHandler {


    private static String USERS_FILE = "users.txt";


    public static void setUsersFile(String filePath) {
        USERS_FILE = filePath;
    }

    public static boolean saveUser(String email, String password, String role, String id, String name) {

        if (role == null || role.isEmpty() || !role.equalsIgnoreCase("ADMIN")) {
            role = "USER";
            if (id == null || id.isEmpty()) {
                id = "U" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            }
        } else if (role.equalsIgnoreCase("ADMIN")) {
            if (id == null || id.isEmpty()) {
                id = "A001";
            }
        }

        String userData = email + "," + password + "," + role + "," + id + "," + name;

        try (FileWriter writer = new FileWriter(USERS_FILE, true);
             PrintWriter printWriter = new PrintWriter(writer)) {

            printWriter.println(userData);

        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public static List<User> loadAllUsers() {
        List<User> users = new ArrayList<>();


        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String email = parts[0];
                    String role = parts[2];
                    String id = parts[3];
                    String name = parts[4];
                    users.add(new User(id, name, email, role));
                }
            }
        } catch (IOException ignored) { }

        return users;
    }

    public static User getUserByCredentials(String email, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[0].equals(email) && parts[1].equals(password)) {
                    String role = parts[2];
                    String id = parts[3];
                    String name = parts[4];
                    return new User(id, name, email, role);
                }
            }
        } catch (IOException ignored) { }

        return null;
    }

    public static String getUserRole(String email) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].equals(email)) {
                    return parts[2];
                }
            }
        } catch (IOException ignored) { }

        return null;
    }

    public static boolean removeUserById(String userId, String requesterRole) {
        List<String> updatedLines = new ArrayList<>();
        boolean removed = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[3].equals(userId)) {
                    String targetRole = parts[2];

                    if ("SUPER_ADMIN".equals(targetRole) && !"SUPER_ADMIN".equals(requesterRole)) {
                        updatedLines.add(line);
                    } else if ("ADMIN".equals(targetRole) && "ADMIN".equals(requesterRole)) {
                        updatedLines.add(line);
                    } else {
                        removed = true;
                    }
                } else {
                    updatedLines.add(line);
                }
            }
        } catch (IOException e) {
            return false;
        }

        if (removed) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE, false))) {
                for (String l : updatedLines) writer.println(l);
            } catch (IOException e) {
                return false;
            }
        }

        return removed;
    }

    public static boolean updateUser(User user) {
        List<String> updatedLines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[0].equalsIgnoreCase(user.getEmail())) {
                    String newLine = user.getEmail() + "," + user.getPassword() + "," + user.getRole() + "," + user.getId() + "," + user.getName();
                    updatedLines.add(newLine);
                    found = true;
                } else {
                    updatedLines.add(line);
                }
            }
        } catch (IOException e) {
            return false;
        }

        if (!found) return false;

        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE, false))) {
            for (String l : updatedLines) writer.println(l);
        } catch (IOException e) {
            return false;
        }

        return true;
    }
}