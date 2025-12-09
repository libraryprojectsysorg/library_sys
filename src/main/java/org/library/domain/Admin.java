/**
 * @author Weam Ahmad
 * @author  Seba Abd Aljwwad
 * @version 1.0

 */
package org.library.domain;


public class Admin {


    private String email;


    private String password;


    /**


     * @param email .
     * @param password .
     */
    public Admin(String email, String password) {
        this.email = email;
        this.password = password;
    }


    /**

     * * @return .
     */
    public String getEmail() {
        return email;
    }


    /**

     * @param email .
     */
    public void setEmail(String email) {
        this.email = email;
    }


    /**

     * @return .
     */
    public String getPassword() {
        return password;
    }


    /**

     * @param password .
     */
    public void setPassword(String password) {
        this.password = password;
    }
}