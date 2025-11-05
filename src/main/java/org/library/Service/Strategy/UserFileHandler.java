package org.library.Service.Strategy;

import org.library.Domain.User;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserFileHandler {
    private static final String USERS_FILE = "users.txt";

    /** حفظ مستخدم جديد */
    public static boolean saveUser(String email, String password, String role, String id, String name) {
        // إذا الدور غير محدد أو ليس ADMIN، اعتبره USER
        if (role == null || role.isEmpty() || !role.equalsIgnoreCase("ADMIN")) {
            role = "USER";
            if (id == null || id.isEmpty()) {
                id = "U" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            }
        } else if (role.equalsIgnoreCase("ADMIN")) {
            // فقط Admin ثابت (A001, A002) مسموح لهم
            if (id == null || id.isEmpty()) {
                id = "A001"; // أو حسب الحاجة
            }
        }

        // التنسيق: email,password,role,id,name
        String userData = String.format("%s,%s,%s,%s,%s", email, password, role, id, name);
        try (FileWriter writer = new FileWriter(USERS_FILE, true);
             PrintWriter printWriter = new PrintWriter(writer)) {

            printWriter.println(userData);
        } catch (IOException e) {
            System.err.println("❌ خطأ أثناء حفظ بيانات المستخدم: " + e.getMessage());
            return false;
        }
        return true;
    }

    /** تحميل كل المستخدمين */
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
                    users.add(new User(id, name, email, role)); // ← إضافة role هنا
                }
            }
        } catch (IOException e) {
            System.err.println("❌ خطأ أثناء قراءة ملف المستخدمين: " + e.getMessage());
        }
        return users;
    }

    /** تحقق من بيانات الدخول */
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
        } catch (IOException e) {
            System.err.println("❌ خطأ أثناء البحث عن بيانات الدخول: " + e.getMessage());
        }
        return null;
    }



    /** الحصول على دور المستخدم */
    public static String getUserRole(String email) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].equals(email)) {
                    return parts[2];
                }
            }
        } catch (IOException e) { }
        return null;
    }

    /** حذف مستخدم بالـ ID مع التحقق من الدور */
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
                        updatedLines.add(line); // لا يسمح بحذف Super Admin إلا من قبل Super Admin
                    } else if ("ADMIN".equals(targetRole) && "ADMIN".equals(requesterRole)) {
                        updatedLines.add(line); // Admin لا يحذف Admin آخر
                    } else {
                        removed = true;
                    }
                } else {
                    updatedLines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("❌ خطأ أثناء قراءة الملف: " + e.getMessage());
            return false;
        }

        if (removed) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE, false))) {
                for (String l : updatedLines) writer.println(l);
            } catch (IOException e) {
                System.err.println("❌ خطأ أثناء إعادة كتابة الملف: " + e.getMessage());
                return false;
            }
        }

        return removed;
    }
}
