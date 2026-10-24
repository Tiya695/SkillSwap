package com.skillswap.repository;

import com.skillswap.entity.Assessment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;

import java.util.List;
import java.util.Optional;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    @EntityGraph(attributePaths = {"teacher", "learner", "skill", "questions", "swapRequest"})
    List<Assessment> findBySwapRequestId(Long swapId);

    @EntityGraph(attributePaths = {"teacher", "learner", "skill", "questions", "swapRequest"})
    Optional<Assessment> findWithDetailsById(Long id);

    List<Assessment> findBySwapRequestIdIn(Collection<Long> swapIds);

    boolean existsBySwapRequestIdAndTeacherId(Long swapId, Long teacherId);
}
