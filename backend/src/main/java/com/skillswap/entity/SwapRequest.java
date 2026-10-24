package com.skillswap.entity;

import com.skillswap.enums.SwapStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "swap_requests")
@Getter
@Setter
@NoArgsConstructor
public class SwapRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Skill skillOffered;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Skill skillWanted;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SwapStatus status = SwapStatus.PENDING;

    @Column(columnDefinition = "int not null default 14")
    private int durationDays = 14;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(length = 500)
    private String meetingUrl;

    @Column(columnDefinition = "bit not null default 0")
    private boolean requesterFinishedEarly;
    @Column(columnDefinition = "bit not null default 0")
    private boolean receiverFinishedEarly;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public boolean isAssessmentUnlocked() {
        if (status != SwapStatus.ACCEPTED || endDate == null) {
            return false;
        }
        boolean periodOver = !LocalDate.now().isBefore(endDate);
        return periodOver || (requesterFinishedEarly && receiverFinishedEarly);
    }

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
