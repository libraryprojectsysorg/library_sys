/**

 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad

 */





package org.library.Service.Strategy;

import org.library.Domain.User;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserFileHandler {
    private static final String USERS_FILE = "users.txt";
    private static final String ADMIN_EMAIL = "s12217663@stu.najah.edu";

    /** يحفظ مستخدماً جديداً (أو مسؤولاً) في الملف. */
    public static void saveUser(String email, String password, String role, String id, String name) {
        if (role.equals("USER") && (id == null || id.isEmpty())) {
            id = "U" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        } else if (role.equals("ADMIN")) {
            id = "A001";
        }

        // التنسيق: email,password,role,id,name
        String userData = String.format("%s,%s,%s,%s,%s", email, password, role, id, name);
        try (FileWriter writer = new FileWriter(USERS_FILE, true);
             PrintWriter printWriter = new PrintWriter(writer)) {

            printWriter.println(userData);
        } catch (IOException e) {
            System.err.println("❌ خطأ أثناء حفظ بيانات المستخدم: " + e.getMessage());
        }
    }

    /** يحمل جميع المستخدمين العاديين (Role=USER) من الملف. */
    public static List<User> loadAllUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[2].equals("USER")) {
                    String email = parts[0];
                    String id = parts[3];
                    String name = parts[4];
                    users.add(new User(id, name, email));
                }
            }
        } catch (FileNotFoundException e) { }
        catch (IOException e) {
            System.err.println("❌ خطأ أثناء قراءة ملف المستخدمين: " + e.getMessage());
        }
        return users;
    }

    /** يتحقق من بيانات الدخول ويعيد الإيميل في حال النجاح. */
    public static String getUserByCredentials(String email, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    if (parts[0].equals(email) && parts[1].equals(password)) {
                        return email;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("❌ خطأ أثناء البحث عن بيانات الدخول: " + e.getMessage());
        }
        return null;
    }

    /** يتحقق مما إذا كان الإيميل خاصاً بالمسؤول. */
    public static boolean isAdmin(String email) {
        return ADMIN_EMAIL.equals(email);
    }

    /** يحذف مستخدماً من الملف بناءً على الـ ID الخاص به. */
    public static boolean removeUserById(String userId) {
        List<String> updatedLines = new ArrayList<>();
        boolean removed = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                // التحقق من ID المستخدم وأن دوره USER (لتجنب حذف الأدمن)
                if (parts.length >= 4 && parts[3].equals(userId) && parts[2].equals("USER")) {
                    removed = true;
                } else {
                    updatedLines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("❌ خطأ أثناء قراءة ملف المستخدمين للحذف: " + e.getMessage());
            return false;
        }

        if (removed) {
            // إعادة كتابة الملف بالقائمة المحدثة
            try (FileWriter writer = new FileWriter(USERS_FILE, false);
                 PrintWriter printWriter = new PrintWriter(writer)) {

                for (String line : updatedLines) {
                    printWriter.println(line);
                }
            } catch (IOException e) {
                System.err.println("❌ خطأ أثناء إعادة كتابة ملف المستخدمين بعد الحذف: " + e.getMessage());
                return false;
            }
        }

        return removed;
    }
}