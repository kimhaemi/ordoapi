package com.example.demo.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CommentSimpleResponse {
    private String message;
    private Integer id;
}
