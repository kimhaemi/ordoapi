package com.example.demo.domain.entity;

import com.example.demo.domain.AlarmArgs;
import com.example.demo.domain.AlarmType;
import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.annotations.Where;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "alarm", indexes = {
    @Index(name = "user_id_idx", columnList = "user_id")
})

@Getter
public class AlarmEntity extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 알람을 받은 사람
    @ManyToOne(fetch = FetchType.LAZY )
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;

    private Integer fromUserId;
    private Integer targetId;
    private String text;

    public static AlarmEntity of(UserEntity userEntity, AlarmType alarmType, Integer fromUserId, Integer targetId) {
        AlarmEntity entity = AlarmEntity.builder()
                .user(userEntity)
                .alarmType(alarmType)
                .fromUserId(fromUserId)
                .targetId(targetId)
                .build();
        return entity;
    }
}
