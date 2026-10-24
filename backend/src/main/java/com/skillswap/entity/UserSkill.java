package com.skillswap.entity;

import com.skillswap.enums.SkillLevel;
import com.skillswap.enums.SkillType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_skills",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "skill_id", "type"}))
@Getter
@Setter
@NoArgsConstructor
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillLevel level;

    @Column(length = 300)
    private String notes;

    public UserSkill(User user, Skill skill, SkillType type, SkillLevel level, String notes) {
        this.user = user;
        this.skill = skill;
        this.type = type;
        this.level = level;
        this.notes = notes;
    }
}
