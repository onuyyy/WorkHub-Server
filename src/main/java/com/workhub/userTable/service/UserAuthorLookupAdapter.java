package com.workhub.userTable.service;

import com.workhub.cs.port.AuthorLookupPort;
import com.workhub.cs.port.dto.AuthorProfile;
import com.workhub.userTable.entity.UserTable;
import com.workhub.userTable.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserAuthorLookupAdapter implements AuthorLookupPort {

    private final UserRepository userRepository;

    @Override
    public Optional<AuthorProfile> findByUserId(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }

        return userRepository.findById(userId)
                .map(this::toProfile);
    }

    @Override
    public Map<Long, AuthorProfile> findByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, UserTable> users = userRepository.findMapByUserIdIn(userIds);

        return users.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> toProfile(entry.getValue())));
    }

    private AuthorProfile toProfile(UserTable user) {
        return new AuthorProfile(user.getUserId(), user.getUserName());
    }
}
