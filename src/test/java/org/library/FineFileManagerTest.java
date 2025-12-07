package org.library;

import org.junit.jupiter.api.*;
import org.library.Domain.Fine;
import org.library.Domain.User;
import org.library.Service.Strategy.FineFileManager;
import org.library.Service.Strategy.UserFileHandler;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FineFileManagerTest {

    private static File testFile;
    private User testUser;


    @BeforeAll
    static void setupFile() throws Exception {
        testFile = File.createTempFile("fines_test", ".txt");

        Field pathField = FineFileManager.class.getDeclaredField("FILE_PATH");
        pathField.setAccessible(true);
        pathField.set(null, testFile.getAbsolutePath()); // ✅ تغيير static variable
    }

    @BeforeEach
    void setupUser() throws IOException {
        testUser = new User("U1", "Ahmad", "a@test.com", "USER");
        testUser.setFines(new ArrayList<>());

        Files.write(testFile.toPath(), new byte[0]);
    }


    @Test
    void testLoadFines() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(testFile));
        writer.write("Ahmad,50,false");
        writer.newLine();
        writer.write("Ahmad,100,true");
        writer.newLine();
        writer.write("Ali,30,false");
        writer.close();

        FineFileManager.loadFines(testUser);

        assertEquals(2, testUser.getFines().size());
        assertEquals(50, testUser.getFines().get(0).getAmount());
        assertFalse(testUser.getFines().get(0).isPaid());
    }


    @Test
    void testSaveAllFines() throws IOException {
        testUser.addFine(new Fine(20, false));
        testUser.addFine(new Fine(40, true));

        List<User> users = new ArrayList<>();
        users.add(testUser);

        FineFileManager.saveAllFines(users);

        List<String> lines = Files.readAllLines(testFile.toPath());

        assertEquals(2, lines.size());
        assertEquals("Ahmad,20,false", lines.get(0));
        assertEquals("Ahmad,40,true", lines.get(1));
    }


    @Test
    void testRemovePaidFines() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(testFile));
        writer.write("Ahmad,50,true");
        writer.newLine();
        writer.write("Ahmad,30,false");
        writer.newLine();
        writer.write("Ali,40,true");
        writer.close();

        FineFileManager.removePaidFines(testUser);

        List<String> lines = Files.readAllLines(testFile.toPath());

        assertEquals(2, lines.size());
        assertTrue(lines.contains("Ahmad,30,false"));
        assertTrue(lines.contains("Ali,40,true"));
    }


    @Test
    void testAddFineForUser() throws IOException {
        File usersFile = File.createTempFile("users_test", ".txt");
        UserFileHandler.setUsersFile(usersFile.getAbsolutePath());


        UserFileHandler.saveUser("a@test.com", "123", "USER", "U1", "Ahmad");

        Fine fine = new Fine(70, false);

        FineFileManager.addFineForUser(testUser, fine);

        assertEquals(1, testUser.getFines().size());
        assertEquals(70, testUser.getFines().get(0).getAmount());
    }
}
