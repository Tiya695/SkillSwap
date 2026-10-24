package com.skillswap.repository;

import com.skillswap.entity.Review;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"swapRequest", "reviewer", "reviewee"})
    List<Review> findByRevieweeIdOrderByCreatedAtDesc(Long revieweeId);

    boolean existsBySwapRequestIdAndReviewerId(Long swapRequestId, Long reviewerId);

    @Modifying
    @Query("delete from Review r where r.swapRequest.id in :ids")
    int deleteBySwapIds(@Param("ids") Collection<Long> ids);

    long countByRevieweeId(Long revieweeId);

    @Query("select avg(r.rating) from Review r where r.reviewee.id = :userId")
    Double averageRating(@Param("userId") Long userId);
}
