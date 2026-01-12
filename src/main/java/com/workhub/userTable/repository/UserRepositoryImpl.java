package com.workhub.userTable.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.workhub.userTable.entity.UserTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.workhub.userTable.entity.QUserTable.userTable;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<Long, UserTable> findMapByUserIdIn(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        List<UserTable> users = queryFactory
                .selectFrom(userTable)
                .where(userTable.userId.in(userIds))
                .fetch();

        return users.stream()
                .collect(Collectors.toMap(UserTable::getUserId, user -> user));
    }

    @Override
    public List<UserTable> findMapByCompanyIdIn(Long companyId) {

        if (companyId == null || companyId <= 0) {
            return null;
        }

        return queryFactory
                .selectFrom(userTable)
                .where(userTable.companyId.in(companyId))
                .fetch();
    }

    @Override
    public Long countActiveUsersUntil(LocalDateTime until) {
        if (until == null) {
            return 0L;
        }

        Long count = queryFactory
                .select(userTable.count())
                .from(userTable)
                .where(
                        userTable.createdAt.loe(until),
                        userTable.deletedAt.isNull()
                                .or(userTable.deletedAt.after(until))
                )
                .fetchOne();

        return count == null ? 0L : count;
    }
}
