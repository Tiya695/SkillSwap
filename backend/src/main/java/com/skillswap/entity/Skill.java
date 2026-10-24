package com.skillswap.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "skills")
@Getter
@Setter
@NoArgsConstructor
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(length = 500)
    private String description;

    private LocalDateTime createdAt;

    public Skill(String name, String category, String description) {
        this.name = name;
        this.category = category;
        this.description = description;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
