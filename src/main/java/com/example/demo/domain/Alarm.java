package com.example.demo.domain;

import com.example.demo.domain.entity.AlarmEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Getter
@AllArgsConstructor
public class Alarm {
    private Integer id;
    private AlarmType alarmType;
    private Integer fromUserId;
    private Integer targetId;
    private LocalDateTime createdAt;


    public static Alarm fromEntity(AlarmEntity entity) {
        return new Alarm(
            entity.getId(),
            entity.getAlarmType(),
            entity.getFromUserId(),
            entity.getTargetId(),
            entity.getCreatedAt()
        );
    }
}
