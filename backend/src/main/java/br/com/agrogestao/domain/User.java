
package br.com.agrogestao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String farmName;

    @Column(nullable = false)
    private String role = "OWNER";

    protected User() {
    }

    public User(String email, String passwordHash, String farmName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.farmName = farmName;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFarmName() {
        return farmName;
    }

    public String getRole() {
        return role;
    }
}
