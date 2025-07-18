package com.deal4u.fourplease.domain.notification.pushnotification.service;

import com.deal4u.fourplease.domain.notification.pushnotification.dto.PushNotificationCreateCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Transactional
public class PushNotificationSaver {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;


    public void save(List<PushNotificationCreateCommand> pushNotifications) {
        jdbcTemplate.batchUpdate(
                "insert into "
                        + "push_notification(message,type,member_id,clicked,created_at,updated_at) "
                        + "values (?,?,?,?,now(),now())",
                new BatchPreparedStatementSetter() {
                    @SneakyThrows
                    @Override
                    public void setValues(@NonNull PreparedStatement ps, int i)
                            throws SQLException {
                        PushNotificationCreateCommand notifications = pushNotifications.get(i);
                        Map<String, Object> message = notifications.message();
                        ps.setObject(1, objectMapper.writeValueAsString(message), Types.BINARY);
                        ps.setString(2, notifications.type());
                        ps.setLong(3, notifications.memberId());
                        ps.setBoolean(4, false);
                    }

                    @Override
                    public int getBatchSize() {
                        return pushNotifications.size();
                    }
                });

    }
}
