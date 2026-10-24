package com.skillswap.repository;

import com.skillswap.entity.SwapRequest;
import com.skillswap.enums.SwapStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SwapRequestRepository extends JpaRepository<SwapRequest, Long> {

    @Override
    @EntityGraph(attributePaths = {"requester", "receiver", "skillOffered", "skillWanted"})
    Optional<SwapRequest> findById(Long id);

    @EntityGraph(attributePaths = {"requester", "receiver", "skillOffered", "skillWanted"})
    Page<SwapRequest> findByReceiverId(Long receiverId, Pageable pageable);

    @EntityGraph(attributePaths = {"requester", "receiver", "skillOffered", "skillWanted"})
    Page<SwapRequest> findByReceiverIdAndStatus(Long receiverId, SwapStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"requester", "receiver", "skillOffered", "skillWanted"})
    Page<SwapRequest> findByRequesterId(Long requesterId, Pageable pageable);

    @EntityGraph(attributePaths = {"requester", "receiver", "skillOffered", "skillWanted"})
    Page<SwapRequest> findByRequesterIdAndStatus(Long requesterId, SwapStatus status, Pageable pageable);

    @Query("select s.id from SwapRequest s where s.requester.id = :userId or s.receiver.id = :userId")
    List<Long> findIdsByParticipant(@Param("userId") Long userId);

    boolean existsByRequesterIdAndReceiverIdAndSkillOfferedIdAndSkillWantedIdAndStatus(
            Long requesterId, Long receiverId, Long skillOfferedId, Long skillWantedId, SwapStatus status);
}
