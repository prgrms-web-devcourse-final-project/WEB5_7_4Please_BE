package com.deal4u.fourplease.domain.notification.pushnotification.service;

import com.deal4u.fourplease.domain.notification.pushnotification.dto.PushNotificationCreateCommand;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PushNotificationSaver {

    private final JdbcTemplate jdbcTemplate;

    public void save(List<PushNotificationCreateCommand> pushNotifications) {
        jdbcTemplate.batchUpdate("insert into push_notification(message,member_id) values (?,?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        PushNotificationCreateCommand notifications = pushNotifications.get(i);
                        ps.setString(1, notifications.message());
                        ps.setLong(2, notifications.memberId());
                    }

                    @Override
                    public int getBatchSize() {
                        return pushNotifications.size();
                    }
                });

    }
}
