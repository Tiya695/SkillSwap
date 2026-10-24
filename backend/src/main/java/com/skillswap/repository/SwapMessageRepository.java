package com.skillswap.repository;

import com.skillswap.entity.SwapMessage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface SwapMessageRepository extends JpaRepository<SwapMessage, Long> {

    @Modifying
    @Query("delete from SwapMessage m where m.swapRequest.id in :ids")
    int deleteBySwapIds(@Param("ids") Collection<Long> ids);

    @EntityGraph(attributePaths = "sender")
    List<SwapMessage> findTop200BySwapRequestIdAndIdGreaterThanOrderByIdAsc(Long swapId, Long afterId);
}
