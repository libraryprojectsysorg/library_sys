package org.library.Domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**

 * Entity for library user (Sprint 4+).
 *
 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad
 * @version 1.1
 */
public class User {
    private final String id;
    private final String name;
    private final String email;
    private boolean hasUnpaidFines;
    private List<Fine> fines = new ArrayList<>(); // جعل القائمة final
    private String role; // "USER", "ADMIN", "SUPER_ADMIN"



    public User(String id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    /**
     * Constructor for User.
     */
    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.hasUnpaidFines = false;
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public boolean hasUnpaidFines() { return hasUnpaidFines; }

    /**
     * استرجاع قائمة الغرامات (قراءة فقط) لاستخدامها في FineCalculator.
     * @return قائمة غير قابلة للتعديل من الغرامات.
     */
    
    public List<Fine> getFines() {
        return Collections.unmodifiableList(fines);
    }

    // --- Business Logic ---

    /**
     * إضافة غرامة للمستخدم (for overdue, Sprint 4+).
     * @param fine الغرامة المراد إضافتها
     */
    public void addFine(Fine fine) {
        if (fine == null) {
            throw new IllegalArgumentException("Fine cannot be null.");
        }
        fines.add(fine);
        updateHasUnpaidFines();
    }

    /**
     * دفع غرامة معينة (US2.3).
     * @param fineToPay الغرامة التي سيتم دفعها.
     * @return true إذا تم دفع الغرامة بنجاح.
     */
    public boolean payFine(Fine fineToPay) {
        // البحث عن الكائن المطابق (باستخدام equals/hashCode في Fine)
        int index = fines.indexOf(fineToPay);
        if (index >= 0) {
            Fine fine = fines.get(index);
            fine.setPaid(true); // تعديل حالة الدفع في كائن الغرامة
            updateHasUnpaidFines(); // تحديث حالة المستخدم
            return true;
        }
        return false;
    }


    /**
     * Get total unpaid fine (Sprint 5: US5.3 mixed media).
     * (منطق التجميع هذا يُمكن نقله إلى FineCalculator Service)
     * @return total NIS unpaid
     */
    public int getTotalUnpaidFine() {
        return fines.stream()
                .filter(f -> !f.isPaid())
                .mapToInt(Fine::getAmount)
                .sum();
    }
    public void payAllFines() {
        fines.forEach(f -> f.setPaid(true)); // تعيين جميع الغرامات كمدفوعة
        updateHasUnpaidFines();             // تحديث العلامة الداخلية
    }
    /**
     * تحديث حالة hasUnpaidFines (Internal Logic).
     */
    private void updateHasUnpaidFines() {
        // تحديث العلامة بناءً على ما إذا كان هناك أي غرامة غير مدفوعة
        hasUnpaidFines = fines.stream().anyMatch(f -> !f.isPaid());
    }

    // --- Identity Methods ---

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

    public void setFines(List<Fine> fines) {
        this.fines=fines;
    }
}