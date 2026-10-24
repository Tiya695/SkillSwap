package com.skillswap.repository;

import com.skillswap.entity.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    boolean existsByNameIgnoreCase(String name);

    @Query("select s from Skill s where (:category = '' or s.category = :category) "
            + "and (:q = '' or lower(s.name) like lower(concat('%', :q, '%')))")
    Page<Skill> search(@Param("category") String category, @Param("q") String q, Pageable pageable);

    @Query("select distinct s.category from Skill s order by s.category")
    List<String> findCategories();
}
