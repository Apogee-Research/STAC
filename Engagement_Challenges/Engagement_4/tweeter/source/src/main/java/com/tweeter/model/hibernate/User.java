package com.tweeter.model.hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Users are authors and the spring security entity that lets them log in.
 *
 * Spring Security required fields:
 * enabled, username, password
 *
 * Fields used by the avatar system:
 * username, fullname, registrationAddress
 */
@Entity
@Table(name = "users")
public class User implements Comparable<User> {
    public User() {}

    @Id
    @GeneratedValue
    private long id;

    @Column(unique = true)
    private String username;

    @Column
    private String fullname;

    @Column
    private String password;

    @Column
    private boolean enabled = true; // This is here for spring security.

    @Column
    private String registrationAddress;

    @OneToMany
    private Set<User> following;

    public User(String username, String fullname, String registrationAddress) {
        this.username = username;
        this.fullname = fullname;
        this.registrationAddress = registrationAddress;
    }

    public User(String fullname, String username, String password, String registrationAddress) {
        this.fullname = fullname;
        this.username = username;
        this.registrationAddress = registrationAddress;
        this.setPassword(password);
        this.setEnabled(true);
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<User> getFollowing() {
        return following;
    }

    public void setFollowing(Set<User> following) {
        this.following = following;
    }

    public byte[] getUniqueData() {
        final byte[] name = this.username.getBytes(StandardCharsets.UTF_8);
        final byte[] full = this.fullname.getBytes(StandardCharsets.UTF_8);
        final byte[] addr = this.registrationAddress.getBytes(StandardCharsets.UTF_8);

        final byte[] bytes = new byte[name.length + full.length + addr.length];

        int i = 0;

        for (byte b : name) bytes[i++] = b;
        for (byte b : full) bytes[i++] = b;
        for (byte b : addr) bytes[i++] = b;

        return bytes;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!username.equals(user.username)) return false;
        if (!fullname.equals(user.fullname)) return false;
        return registrationAddress.equals(user.registrationAddress);

    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + fullname.hashCode();
        result = 31 * result + registrationAddress.hashCode();
        return result;
    }

    @Override
    public int compareTo(User o) {
        return o.hashCode() - hashCode();
    }
}
