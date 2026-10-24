package com.skillswap.entity;

import com.skillswap.enums.AssessmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assessments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"swap_request_id", "teacher_id"}))
@Getter
@Setter
@NoArgsConstructor
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "swap_request_id")
    private SwapRequest swapRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User teacher;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User learner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentStatus status = AssessmentStatus.OPEN;

    private int attempt = 1;

    private Integer scorePercent;
    private Boolean passed;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<AssessmentQuestion> questions = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
