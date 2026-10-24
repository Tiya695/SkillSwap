package com.skillswap.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "swap_messages", indexes = @Index(columnList = "swap_request_id, id"))
@Getter
@Setter
@NoArgsConstructor
public class SwapMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "swap_request_id")
    private SwapRequest swapRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User sender;

    @Column(nullable = false, length = 1000)
    private String text;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
