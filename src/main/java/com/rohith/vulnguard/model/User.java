package com.rohith.vulnguard.model;

import com.rohith.vulnguard.model.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────────────
    public User() {}

    public User(Long id, String username, String email, String password,
                Role role, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
    }

    // ── Getters ───────────────────────────────────────────────────────
    public Long getId()                 { return id; }
    public String getUsername()         { return username; }
    public String getEmail()            { return email; }
    public String getPassword()         { return password; }
    public Role getRole()               { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ── Setters ───────────────────────────────────────────────────────
    public void setId(Long id)                         { this.id = id; }
    public void setUsername(String username)           { this.username = username; }
    public void setEmail(String email)                 { this.email = email; }
    public void setPassword(String password)           { this.password = password; }
    public void setRole(Role role)                     { this.role = role; }
    public void setCreatedAt(LocalDateTime createdAt)  { this.createdAt = createdAt; }

    // ── Builder ───────────────────────────────────────────────────────
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String username;
        private String email;
        private String password;
        private Role role;

        public Builder id(Long id)               { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email)       { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder role(Role role)           { this.role = role; return this; }

        public User build() {
            User u = new User();
            u.id = this.id;
            u.username = this.username;
            u.email = this.email;
            u.password = this.password;
            u.role = this.role;
            return u;
        }
    }
}
