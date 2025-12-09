package org.library.Domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class User {
    private final String id;
    private  String name;
    private final String email;
    private String password;
    private boolean hasUnpaidFines;
    private List<Fine> fines = new ArrayList<>();
    private String role;

    public User(String id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;

        this.role = role;
    }

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.hasUnpaidFines = false;
    }

    public String getId() { return id; }
    public void  setName(String name) { this.name = name; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean hasUnpaidFines() { return hasUnpaidFines; }

    public List<Fine> getFines() {
        return Collections.unmodifiableList(fines);
    }

    public void setFines(List<Fine> fines) {
        this.fines = fines;
        updateHasUnpaidFines();
    }

    public void addFine(Fine fine) {
        if (fine == null) throw new IllegalArgumentException("Fine cannot be null.");
        fines.add(fine);
        updateHasUnpaidFines();
    }

    public boolean payFine(Fine fineToPay) {
        int index = fines.indexOf(fineToPay);
        if (index >= 0) {
            Fine fine = fines.get(index);
            fine.setPaid(true);
            updateHasUnpaidFines();
            return true;
        }
        return false;
    }

    public int getTotalUnpaidFine() {
        return fines.stream()
                .filter(f -> !f.isPaid())
                .mapToInt(Fine::getAmount)
                .sum();
    }

    public void payAllFines() {
        fines.forEach(f -> f.setPaid(true));
        updateHasUnpaidFines();
    }

    private void updateHasUnpaidFines() {
        hasUnpaidFines = fines.stream().anyMatch(f -> !f.isPaid());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
