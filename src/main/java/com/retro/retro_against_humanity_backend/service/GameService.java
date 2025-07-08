package com.retro.retro_against_humanity_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GameService {

    public void addPlayerToSession(String sessionCode, Long userId) {
        // Logic to find session, create a new SessionPlayer, and save it
    }

    public void saveSubmission(String payload) {
        // Logic to find the round, player, and save the new submission
    }

    public void processVote(String payload) {
        // Logic to add a vote and ensure a player doesn't vote twice for the same submission
    }

    public void endRound(String sessionCode) {
        // 1. Find the current round and mark its status as COMPLETED.
        // 2. Call a method to calculate scores for that round.
        // 3. Create a new round, select a new judge, draw new cards.
        // 4. Save all changes to the database.
    }

    private void calculateScoresForRound(Long roundId) {
        // 1. Find all submissions for the round.
        // 2. Count votes for each submission.
        // 3. Find the submission(s) with the most votes.
        // 4. Award points (e.g., +3) to the author of the winning submission(s).
        // 5. Update the `score` in the `session_players` table.
    }

    public String getCurrentGameState(String sessionCode) {
        // This is a crucial method!
        // 1. Query your database for all information related to the sessionCode.
        // 2. Assemble everything (players, scores, active cards, submissions, votes) into a single GameStateDto object.
        // 3. This DTO should perfectly match the structure your frontend expects in its Zustand store.
        // 4. Return the DTO.
        return "hello world"; // Placeholder, replace with actual GameStateDto serialization
    }
}
