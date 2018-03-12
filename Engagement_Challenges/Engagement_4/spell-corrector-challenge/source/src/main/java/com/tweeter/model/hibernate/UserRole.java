package com.tweeter.model.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This whole class is for spring security. All users are automatically added to the USER Roles.
 */
@Entity
@Table(name = "user_roles")
public class UserRole { // This whole class is for spring security.
    public UserRole() {}

    @Id
    @GeneratedValue
    private long id;

    @Column(unique = true)
    private String username;

    @Column
    private String role = "ROLE_USER";

    public UserRole(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
