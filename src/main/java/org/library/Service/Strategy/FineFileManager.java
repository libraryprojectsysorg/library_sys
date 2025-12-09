package org.library.Service.Strategy;

import org.library.domain.Fine;
import org.library.domain.User;

import java.io.*;
import java.util.ArrayList;

import java.util.List;

public class FineFileManager {

    private static  String FILE_PATH = "fines.txt";


    public static void loadFines(User user) {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            List<Fine> fines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3 && parts[0].equals(user.getName())) {
                    int amount = Integer.parseInt(parts[1]);
                    boolean isPaid = Boolean.parseBoolean(parts[2]);
                    fines.add(new Fine(amount, isPaid));
                }
            }
            user.setFines(fines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void saveAllFines(List<User> users) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (User user : users) {
                for (Fine fine : user.getFines()) {
                    writer.write(user.getName() + "," + fine.getAmount() + "," + fine.isPaid());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void removePaidFines(User user) {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        List<String> remainingLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3 && parts[0].equals(user.getName())) {
                    boolean isPaid = Boolean.parseBoolean(parts[2]);
                    if (!isPaid) remainingLines.add(line);
                } else {
                    remainingLines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : remainingLines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void addFineForUser(User user, Fine fine) {

        user.addFine(fine);


        List<User> allUsers = UserFileHandler.loadAllUsers();


        for (User u : allUsers) {
            if (u.getId().equals(user.getId())) {
                u.setFines(user.getFines());
                break;
            }
        }


        saveAllFines(allUsers);
    }
}
