package com.example.demo.security.filter;

import com.example.demo.domain.response.ErrorResponse;
import com.example.demo.domain.response.Response;
import com.example.demo.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class ExceptionHandlerFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        try {
//            filterChain.doFilter(request, response);
//        } catch (PrematureJwtException e) {
//            log.error("접근이 허용되지 않은 토큰입니다.");
//        } catch (ExpiredJwtException e) {
//            log.error("만료된 토큰입니다.");
//
//        } catch (ArrayIndexOutOfBoundsException e) {
//            log.error("Token이 없습니다.");
//            ObjectMapper objectMapper = new ObjectMapper();
//            response.setStatus(HttpStatus.UNAUTHORIZED.value());
//            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//            response.setCharacterEncoding("UTF-8");
//            objectMapper.writeValue(response.getWriter(),
//                    Response.error("ERROR", new ErrorResponse(ErrorCode.INVALID_TOKEN, "")));
//        } catch (Exception e) {
//
//        }
        try {
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            ObjectMapper objectMapper = new ObjectMapper();
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Access-Control-Allow-Origin", "*"); // origin 다를 경우 (로컬 테스트 등) cors block 방지
            objectMapper.writeValue(response.getWriter(),
                    Response.error("ERROR", new ErrorResponse(ErrorCode.INVALID_TOKEN, "인증 요청이 올바르지 않습니다.")));

            // 여기다가 ApiLogger같은걸 쌓아주면 좋다.ApiResult.error ApiResult.success 이런거는 응답을 이쁘게 만들어주기 위해서 응답
//            {
//                "status": "error",
//                "message": "[ADMIN_AUTH_ERR02] 인증 요청이 올바르지 않습니다.",
//                "data": "ADMIN_AUTH_ERR",
//                "code": "FORBIDDEN",
//                "dataSize": 1
//            }
//            ApiResult<String> errorResponse = ApiResult.error(e.getMessage(), "ADMIN_AUTH_ERR", HttpStatus.FORBIDDEN.name());
//            String resJson = mapper.toJsonPretty(errorResponse);
//            adminApiLogger.saveApiLog(adminApiLogger.buildAdminApiLog(request, response, "", resJson));
//            response.getWriter().write(resJson.replaceAll("<","&lt").replaceAll(">","&gt"));
        }


    }
}
