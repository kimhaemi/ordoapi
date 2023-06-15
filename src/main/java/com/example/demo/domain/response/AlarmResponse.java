package com.example.demo.domain.response;

import com.example.demo.domain.Alarm;
import com.example.demo.domain.AlarmArgs;
import com.example.demo.domain.AlarmType;
import com.example.demo.domain.entity.AlarmEntity;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AlarmResponse {
    private Integer id;
    private AlarmType alarmType; // NEW_COMMENT_ON_POST, NEW_LIKE_ON_POST
    private Integer fromUserId;
    private Integer targetId;
    private String text;
    private LocalDateTime createdAt;

    public static AlarmResponse fromAlarm(AlarmEntity alarm) {
        return new AlarmResponse(
            alarm.getId(),
            alarm.getAlarmType(),
            alarm.getFromUserId(),
            alarm.getTargetId(),
            alarm.getText(),
            alarm.getCreatedAt()
        );
    }
}
