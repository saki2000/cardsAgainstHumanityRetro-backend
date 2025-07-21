package com.retro.retro_against_humanity_backend.service;

import com.retro.retro_against_humanity_backend.entity.ActiveSession;
import com.retro.retro_against_humanity_backend.entity.SessionHistory;
import com.retro.retro_against_humanity_backend.entity.Users;
import com.retro.retro_against_humanity_backend.errors.Constants;
import com.retro.retro_against_humanity_backend.exceptions.UserNotFoundException;
import com.retro.retro_against_humanity_backend.repository.SessionHistoryRepository;
import com.retro.retro_against_humanity_backend.repository.SessionPlayerRepository;
import com.retro.retro_against_humanity_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SessionPlayerRepository sessionPlayerRepository;
    private final SessionHistoryRepository sessionHistoryRepository;

    public Users getUserByUsername(String userName) {
        return userRepository.findByUsername(userName).orElseThrow(()-> new UserNotFoundException(Constants.Users.USER_NOT_FOUND_MESSAGE));
    }

    public void updateStatsOnGameEnd(ActiveSession session) {
        List<SessionHistory> histories = sessionHistoryRepository.findBySession(session);

        for (SessionHistory history : histories) {
            Users user = history.getUser();
            int score = history.getScore();

            user.setGamesPlayed(user.getGamesPlayed() + 1);
            user.setTotalPoints(user.getTotalPoints() + score);

            if (score > user.getBestScore()) {
                user.setBestScore(score);
            }
            userRepository.save(user);
        }
    }
}
