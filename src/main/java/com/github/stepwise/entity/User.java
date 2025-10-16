package com.github.stepwise.entity;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usr")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "temp_password")
    private String tempPassword;

    @Column(name = "is_temp_password", nullable = false)
    private Boolean isTempPassword = true;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @ManyToMany(mappedBy = "students")
    private List<StudyGroup> groups = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    private Profile profile;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(String username, String password, String email, UserRole role) {
        this.username = username;
        this.password = password;
        this.tempPassword = password;
        this.email = email;
        this.role = role;
        this.profile = new Profile();
    }

    public User(String username, String password, String email, UserRole role, Profile profile) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.profile = profile;
    }

}
