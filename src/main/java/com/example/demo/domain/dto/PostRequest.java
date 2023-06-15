package com.example.demo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@EnableWebMvc
public class PostRequest {
    private String title;
    private String body;


}
