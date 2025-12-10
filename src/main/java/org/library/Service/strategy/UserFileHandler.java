package org.library.Service.strategy;

import org.library.domain.User;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserFileHandler {

    private UserFileHandler() {
        // Private constructor to prevent instantiation
    }

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";
    private static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";

    private static String  usersFile = "users.txt";

    public static void setUsersFile(String filePath) {
        usersFile = filePath;
    }

    public static boolean saveUser(String email, String password, String role, String id, String name) {


        if (role == null || role.isEmpty() || !role.equalsIgnoreCase(ROLE_ADMIN)) {
            role = ROLE_USER;
            if (id == null || id.isEmpty()) {
                id = "U" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            }
        } else {
            if (id == null || id.isEmpty()) {
                id = "A001";
            }
        }

        String userData = email + "," + password + "," + role + "," + id + "," + name;

        try (FileWriter writer = new FileWriter( usersFile, true);
             PrintWriter printWriter = new PrintWriter(writer)) {

            printWriter.println(userData);

        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public static List<User> loadAllUsers() {
        List<User> users = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader( usersFile))) {
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
        } catch (IOException ignored) {
            // Ignored intentionally
        }

        return users;
    }

    public static User getUserByCredentials(String email, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader( usersFile))) {
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
        } catch (IOException ignored) {
            // Ignored intentionally
        }

        return null;
    }

    public static String getUserRole(String email) {
        try (BufferedReader reader = new BufferedReader(new FileReader( usersFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].equals(email)) {
                    return parts[2];
                }
            }
        } catch (IOException ignored) {
            // Ignored intentionally
        }

        return null;
    }

    public static boolean removeUserById(String userId, String requesterRole) {
        List<String> updatedLines = new ArrayList<>();
        boolean removed = false;

        try (BufferedReader reader = new BufferedReader(new FileReader( usersFile))) {
            String line;
            while ((line = reader.readLine()) != null) {

                if (!isTargetUser(line, userId)) {
                    updatedLines.add(line);
                    continue;
                }

                String targetRole = extractRole(line);

                if (shouldSkipRemoval(targetRole, requesterRole)) {
                    updatedLines.add(line);
                } else {
                    removed = true;
                }
            }

        } catch (IOException e) {
            return false;
        }

        if (removed && !writeUpdatedFile(updatedLines)) {
            return false;
        }

        return removed;
    }

    private static boolean isTargetUser(String line, String userId) {
        String[] parts = line.split(",");
        return parts.length >= 5 && parts[3].equals(userId);
    }

    private static String extractRole(String line) {
        return line.split(",")[2];
    }

    private static boolean shouldSkipRemoval(String targetRole, String requesterRole) {
        if (ROLE_SUPER_ADMIN.equals(targetRole) && !ROLE_SUPER_ADMIN.equals(requesterRole)) {
            return true;
        }
        return ROLE_ADMIN.equals(targetRole) && ROLE_ADMIN.equals(requesterRole);
    }

    private static boolean writeUpdatedFile(List<String> updatedLines) {
        try (PrintWriter writer = new PrintWriter(new FileWriter( usersFile, false))) {
            for (String l : updatedLines) writer.println(l);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean updateUser(User user) {
        List<String> updatedLines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader( usersFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[0].equalsIgnoreCase(user.getEmail())) {
                    String newLine = user.getEmail() + "," + user.getPassword() + "," +
                            user.getRole() + "," + user.getId() + "," + user.getName();
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

        return writeUpdatedFile(updatedLines);
    }
}
