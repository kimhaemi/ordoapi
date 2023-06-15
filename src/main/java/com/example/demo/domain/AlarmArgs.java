package com.example.demo.domain;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlarmArgs {
    // 알람을 발생시킨 사람
    private Integer fromUserId;
    // 알림이 발생된 주체 post
    private Integer targetId;
}
