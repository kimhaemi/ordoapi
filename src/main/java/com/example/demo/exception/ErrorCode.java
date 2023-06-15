package com.example.demo.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid token"),
    USERNAME_NOT_FOUND(HttpStatus.NOT_FOUND,"User not founded"),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "Post not founded"),

    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Comment not founded"),

    DUPLICATED_USER_NAME(HttpStatus.CONFLICT, "Duplicated user name"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "Invalid password"),
    INVALID_PERMISSION(HttpStatus.UNAUTHORIZED, "User has invalid permission"),

    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Database error occurs"),
    ALREADY_LIKED(HttpStatus.CONFLICT, "이미 like를 눌렀습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Casting to User class failed")

    ;
    private HttpStatus httpStatus;
    private String message;
}
