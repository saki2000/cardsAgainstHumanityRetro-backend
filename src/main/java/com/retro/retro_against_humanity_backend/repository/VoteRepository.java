package com.retro.retro_against_humanity_backend.repository;

import com.retro.retro_against_humanity_backend.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByCommentIdAndVoterId(Long commentId, Long voterId);
}
