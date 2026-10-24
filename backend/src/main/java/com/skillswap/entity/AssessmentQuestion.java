package com.skillswap.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "assessment_questions")
@Getter
@Setter
@NoArgsConstructor
public class AssessmentQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Assessment assessment;

    private int position;

    @Column(nullable = false, length = 500)
    private String text;

    private int maxPoints;

    @Column(length = 2000)
    private String optionsJson;

    private Integer correctOption;

    private Integer selectedOption;

    private Integer awardedPoints;
}
