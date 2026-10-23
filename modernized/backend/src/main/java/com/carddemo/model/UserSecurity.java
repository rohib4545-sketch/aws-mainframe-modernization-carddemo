package com.carddemo.model;

import jakarta.persistence.*;

/** SEC-USER-DATA (CSUSR01Y) */
@Entity
@Table(name = "user_security")
public class UserSecurity {
    @Id
    @Column(length = 8)
    private String userId;
    @Column(length = 20)
    private String firstName;
    @Column(length = 20)
    private String lastName;
    @Column(length = 8)
    private String password;
    @Column(length = 1)
    private String userType; // 'A' admin, 'U' user

    public String getUserId() { return userId; }
    public void setUserId(String v) { userId = v; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { firstName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { lastName = v; }
    public String getPassword() { return password; }
    public void setPassword(String v) { password = v; }
    public String getUserType() { return userType; }
    public void setUserType(String v) { userType = v; }
}
