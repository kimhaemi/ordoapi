package com.example.demo.exception;

import com.example.demo.domain.response.ErrorResponse;
import com.example.demo.domain.response.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws AppException, IOException, ServletException {
        ObjectMapper objectMapper = new ObjectMapper();
        log.error("인증에 실패했습니다. : {}", authException.getMessage());
        AppException e = new AppException(ErrorCode.INVALID_PERMISSION, authException.getMessage());
        response.setStatus(e.getErrorCode().getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(),
                new ErrorResponse(e.getErrorCode(), e.toString()));
    }
}
