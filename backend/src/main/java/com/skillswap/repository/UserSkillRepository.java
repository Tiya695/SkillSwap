package com.skillswap.repository;

import com.skillswap.entity.UserSkill;
import com.skillswap.enums.SkillType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    @EntityGraph(attributePaths = "skill")
    List<UserSkill> findByUserId(Long userId);

    @EntityGraph(attributePaths = "skill")
    List<UserSkill> findByUserIdAndType(Long userId, SkillType type);

    @EntityGraph(attributePaths = "skill")
    Optional<UserSkill> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("delete from UserSkill us where us.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndSkillIdAndType(Long userId, Long skillId, SkillType type);

    @Query("select us from UserSkill us join fetch us.user join fetch us.skill where us.user.id <> :userId")
    List<UserSkill> findAllOfOtherUsers(@Param("userId") Long userId);
}
