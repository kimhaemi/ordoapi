package com.example.demo.security.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.demo.domain.User;
import com.example.demo.domain.entity.UserEntity;
import com.example.demo.service.UserService;
//import com.example.demo.utils.JwtTokenUtil;
import com.example.demo.utils.JwtTokenUtils;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter{
//    private UserDetailsService userDetailsService;

    private final UserService userService;

    private final String secretKey;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException, JwtException {
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String token;
        if (header == null || !header.startsWith("Bearer ")) {
            log.error("Authorization Header does not start with Bearer {}", request.getRequestURI());
            chain.doFilter(request, response);
            return;
        } else {
            token = header.split(" ")[1].trim();
            System.out.println(token);
        }

        String userName = JwtTokenUtils.getUsername(token, secretKey);
        User userDetails = userService.loadUserByUsername(userName);

        if (!JwtTokenUtils.validate(token, userDetails.getUsername(), secretKey)) {
            chain.doFilter(request, response);
            return ;
        }
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null,
                userDetails.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);

    }
}

/** 롯데 Admin API에는 아래와 같이 되어있다..이 프로젝트는 ExceptionHandlerFilter를 써서 리턴이 제대로 안되고..로그만 찍었다.
 * Filter를 여러게 만들어서  @Order(0) ==> AuthTokenApplyHandleFilter  @Order(1)==> AuthTokenApplyFilter @Order(2) ==> ApiPermissionCheckHandleFilter @Order(3) ==> ApiPermissionCheckFilter
 *
 * Filter중에 마직막에 실행되어야 하는 CustomServletWrappingFilter @Order(Ordered.LOWEST_PRECEDENCE)
 * Servlet을 wrapping해주는 filter를 구현했다. OncePerRequestFilter를 상속받아 한 reuqest당 한번의 실행만 되도록 보장하였다. spring bean으로 등록해두면 Filter 적용은 끝난다. *
 * 가장 중요한 점은 마지막 줄에 있다. wrappingResponse.copyBodyToResponse()로 실제 response body에다가 값을 넣어주어야 한다. 이 코드를 안넣어주면 클라이언트가 아무 응답도 받지 못하게 된다.
 *logging은 interceptor로 구현하였다. 직접 구현할 때는 wrapping이 안됐을 경우나 caching된 content가 없을 경우 등, 검증해야 할 부분이 많다.
 * caching된 content는 byte array로 들고 있기 때문에 objectMapper를 이용해 JsonNode로 읽어주고 로깅했다.
 *
 @Slf4j
 @Component
 @Order(1)
 public class AuthTokenApplyFilter extends OncePerRequestFilter {

 private final AdminJwtHandler adminJwtHandler;
 private final MapperService mapper;
 private final List<String> excludeUrls;

 @Value("${spring.profiles.active}")
 private String activeProfile;

 @Value("${server.servlet.context-path}")
 private String contextPath;

 @Autowired
 public AuthTokenApplyFilter(AdminJwtHandler adminJwtHandler, MapperService mapper) {
 this.adminJwtHandler = adminJwtHandler;
 this.mapper = mapper;
 excludeUrls = ExcludeUrlUtils.getAuthTokenExcludeUrls(activeProfile);
 }

 @Override
 protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
 throws ServletException, IOException, JwtException {

 String contextPathRemovedPath = request.getRequestURI().replaceFirst(contextPath, "");

 if (isExcludeUrls(excludeUrls, contextPathRemovedPath)) {
 chain.doFilter(request, response);
 return;
 }
 if (StringUtils.equalsIgnoreCase(request.getMethod(), "OPTIONS")) {
 chain.doFilter(request, response);
 return;
 }

 String jwt = parseJwtFromHeader(request);

 UserAuthToken userAuthToken = adminJwtHandler.validateJwtAndBuildUserAuthToken(jwt);
 checkAdminSiteRole(userAuthToken);

 // 필요시 ip 다시 체크 로직 추가할 것 (허용된 ip 만 api 요청 받아준다든지 하는)

 String tokenString = mapper.toJson(userAuthToken);

 HttpServletRequestWrapper wrapper = wrapRequestWithTokenHeader(request, tokenString);

 // 관리자포털은 컨트롤러 파라미터에 @AuthToken 선언 안해도 Jpa Audit 용 사용할 수 있도록 저장 (UserAuthTokenResolver 타지 않아도 저장됨)
 JpaAuditUserAuthTokenAware.USER_AUTH_TOKEN_THREAD_LOCAL.set(userAuthToken);
 MDC.put(Const.CURRENT_USERNAME, userAuthToken.getUsername());

 chain.doFilter(wrapper, response);
 }

 private HttpServletRequestWrapper wrapRequestWithTokenHeader(HttpServletRequest request, String tokenString) {
 // request 를 HEADER_NAME_USER_AUTH_TOKEN 헤더를 담은 requestWrapper 로 변경한다
 return new HttpServletRequestWrapper(request) {
 @Override
 public String getHeader(String name) {
 if (StringUtils.equalsIgnoreCase(name, Const.HEADER_NAME_USER_AUTH_TOKEN)) {
 return URLEncoder.encode(tokenString, StandardCharsets.UTF_8);
 }
 final String value = request.getParameter(name);
 if (value != null) {
 return value;
 }
 return super.getHeader(name);
 }
 };
 }

 private void checkAdminSiteRole(UserAuthToken userAuthToken) throws JwtException {
 if (!userAuthToken.isAdminSiteRole()) {
 //throw new JwtException("None admin token given. [" + userAuthToken.getUsername() + "]");
 throw new JwtException("[ADMIN_AUTH_ERR09] 인증정보가 유효하지 않습니다.");
 }
 }

 private String parseJwtFromHeader(HttpServletRequest request) {
 String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
 if (authHeader != null && authHeader.startsWith(Const.PREFIX_AUTH_HEADER)) {
 return authHeader.replace(Const.PREFIX_AUTH_HEADER, "");
 }
 //throw new JwtException("Empty token given.");
 throw new JwtException("[ADMIN_AUTH_ERR06] 인증에 필요한 정보가 없습니다.");
 }

 }

=====================위의 validateJwtAndBuildUserAuthToken(jwt) 에서 호출해서 아래에  상화별로 403 Forbidden  throw new JwtException("[ADMIN_AUTH_ERR03] 인증 요청이 올바르지 않습니다.");
 {
 "status": "error",
 "message": "[ADMIN_AUTH_ERR02] 인증 요청이 올바르지 않습니다.",
 "data": "ADMIN_AUTH_ERR",
 "code": "FORBIDDEN",
 "dataSize": 1
 }

Interceptor퐅더밑에 HttpLoggingInterceptor 같은걸 만들어서  logging.admin_api_log에 도 에러를 insert 한다.

 -------------------------------------------------------------------------------------------------------


 package com.liac.lnc.admin.api.config.support;

 import com.liac.lnc.admin.api.config.JwtConfig;
 import com.liac.lnc.common.constants.Const;
 import com.liac.lnc.common.model.UserAuthToken;
 import com.liac.lnc.common.model.enums.Site;
 import com.liac.lnc.common.support.ProfileUtils;
 import com.liac.lnc.redis.Cache;
 import com.liac.lnc.redis.CustomRedisCacheManager;
 import io.jsonwebtoken.Claims;
 import io.jsonwebtoken.Header;
 import io.jsonwebtoken.Jws;
 import io.jsonwebtoken.JwtException;
 import io.jsonwebtoken.Jwts;
 import java.util.List;
 import lombok.RequiredArgsConstructor;
 import lombok.extern.slf4j.Slf4j;
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;

 @Slf4j
 @RequiredArgsConstructor
 @Component
 public class AdminJwtHandler {

 private final JwtConfig jwtConfig;
 private final CustomRedisCacheManager cacheManager;

 @Value("${spring.profiles.active:local}")
 private String activeProfile;

 @SuppressWarnings("unchecked")
 public UserAuthToken validateJwtAndBuildUserAuthToken(String jwt) throws JwtException {
 Jws<Claims> claimsJws = getClaimsJws(jwt);
 Claims claims = getClaimsBody(claimsJws);
 String subject = claims.getSubject();
 if (StringUtils.isBlank(subject) || !subject.equals(claims.get(Const.JWT_USERNAME))) {
 log.error("Token subject unmatched with username! : {}", jwt);
 //throw new JwtException("Token subject unmatched with username!");
 throw new JwtException("[ADMIN_AUTH_ERR03] 인증 요청이 올바르지 않습니다.");
 }

 String profile = (String) claims.get(Const.JWT_PROFILE);
 String boundary = (String) claims.get(Const.JWT_BOUNDARY);
 String issuer = (String) claims.get(Const.JWT_ISSUER);
 List<String> roles = (List<String>) claims.get(Const.JWT_ROLES);

 if (roles == null || roles.isEmpty()) {
 log.error("no roles found in token. : {}", jwt);
 //throw new JwtException("no roles found in token.");
 throw new JwtException("[ADMIN_AUTH_ERR10] 권한이 없습니다.");
 }

 if (ProfileUtils.isProd(activeProfile)) {
 if (!StringUtils.equalsIgnoreCase(profile, activeProfile)) {
 log.error("invalid token profile - [{}], activeProfile [{}]", profile, activeProfile);
 //throw new JwtException("invalid token profile.");
 throw new JwtException("[ADMIN_AUTH_ERR07] 인증정보가 유효하지 않습니다.");
 }
 }
 if (!StringUtils.equalsIgnoreCase(Site.ADMIN.name(), boundary)) {
 log.error("invalid token boundary - [{}]", boundary);
 //throw new JwtException("invalid token boundary.");
 throw new JwtException("[ADMIN_AUTH_ERR08] 인증정보가 유효하지 않습니다.");
 }
 if (!StringUtils.equalsIgnoreCase("adm", issuer)) {
 log.error("invalid token issuer - [{}]", issuer);
 //throw new JwtException("invalid token issuer.");
 throw new JwtException("[ADMIN_AUTH_ERR09] 인증정보가 유효하지 않습니다.");
 }

 UserAuthToken userAuthToken = UserAuthToken.createUserAuthTokenByClaims(claims);

 String multiSessionLogoutTokenUsername =
 cacheManager.getCache(Cache.ADMIN_LOGOUT_TOKEN, userAuthToken.getTokenKey(), String.class);
 if (StringUtils.isNotBlank(multiSessionLogoutTokenUsername) && StringUtils.equals(multiSessionLogoutTokenUsername,
 userAuthToken.getUsername())) {
 log.info("admin-logout-token by multi-session caught, tokenKey : {}, username [{}]",
 userAuthToken.getTokenKey(), userAuthToken.getUsername());
 throw new JwtException("[AUTH_ERR05] 외부에서 로그인이 감지되어 로그아웃 처리됩니다.");
 }

 return userAuthToken;
 }

 private Claims getClaimsBody(Jws<Claims> claimsJws) {
 return claimsJws.getBody();
 }

 private Header getClaimsHeader(Jws<Claims> claimsJws) {
 return claimsJws.getHeader();
 }

 private Jws<Claims> getClaimsJws(String jwt) throws JwtException {
 try {
 return Jwts.parser()
 .setSigningKey(jwtConfig.getPublicKey())
 .parseClaimsJws(jwt);
 } catch (Exception jwtRelated) {
 log.error("getClaimsJws error! : {}\n{}", jwt, jwtRelated.getMessage());
 throw new JwtException("[ADMIN_AUTH_ERR02] 인증 요청이 올바르지 않습니다.");
 }
 }

 }
  *
  *
  * **/

/**
 package com.liac.lnc.admin.api.config.interceptor;

 import com.google.common.collect.Lists;
 import com.liac.lnc.admin.api.config.support.AdminApiLogger;
 import com.liac.lnc.admin.api.config.support.ExcludeUrlUtils;
 import com.liac.lnc.common.constants.Const;
 import com.liac.lnc.common.support.HttpRequestUtils;
 import com.liac.lnc.common.support.MapperService;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import lombok.RequiredArgsConstructor;
 import lombok.extern.slf4j.Slf4j;
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
 import org.springframework.web.servlet.HandlerInterceptor;
 import org.springframework.web.util.ContentCachingRequestWrapper;
 import org.springframework.web.util.ContentCachingResponseWrapper;

// * filter 통과된 후 (jwt 인증 성공) excludeUrls 를 제외한 요청의 로그를 저장한다.
// * common-data 의 WebConfig 에서 등록된다

@Slf4j
@RequiredArgsConstructor
@Component
public class HttpLoggingInterceptor implements HandlerInterceptor {

    private static List<String> loggingExcludeUrls = Lists.newArrayList("/sso/**", "/error",
            Const.FAVICON,
            Const.SSO_OAUTH_URL,
            Const.SSO_LOGIN_URL, Const.SSO_LOGOUT_URL,
            Const.SSO_RESOURCES_URL, Const.SSO_CSS_URL,
            Const.SSO_JS_URL, Const.SSO_FONT_URL,
            Const.SSO_IMAGES_URL, Const.SSO_FONTS_URL,
            Const.SSO_HTML_URL,
            Const.SWAGGERS, Const.SWAGGER_UI,
            Const.SWAGGER_WEBJARS, Const.SWAGGER_CSRF,
            Const.SWAGGER_DOCS, Const.SWAGGER_RESOURCES,
            Const.ACTUATOR_API, Const.LIVENESS_PROBE_URL
    );

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${from-zone}")
    private String fromZone;

    private final AdminApiLogger adminApiLogger;
    private final MapperService mapper;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        if (!shouldLogging(request, response)) { // GET 200 제외
            return;
        }
        String reqBody = "";
        String errorResponse = "";

        if (!(request instanceof StandardMultipartHttpServletRequest)) {
            try {
                // servlet 에 정상 진입하지 않은 경우 (없는 api 호출) ContentCachingResponseWrapper 가 아닌 ResponseFacade 로 와서 exception 터지고 error 페이지 표시됨
                final ContentCachingRequestWrapper cachingRequest = (ContentCachingRequestWrapper) request;
                final ContentCachingResponseWrapper cachingResponse = (ContentCachingResponseWrapper) response;

                if (cachingRequest.getContentType() != null && cachingRequest.getContentType().contains("application/json")) {
                    if (cachingRequest.getContentAsByteArray().length != 0) {
                        reqBody = mapper.readTree(cachingRequest.getContentAsByteArray()).toPrettyString();
                    }
                }
                if (cachingResponse.getContentType() != null && cachingResponse.getContentType().contains("application/json")) {
                    if (cachingResponse.getContentAsByteArray().length != 0) {
                        errorResponse = mapper.readTree(cachingResponse.getContentAsByteArray()).toPrettyString();
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                if (response.getStatus() != 200) {
                    errorResponse = String.valueOf(response.getStatus());
                }
            }
        }

        adminApiLogger.saveApiLog(adminApiLogger.buildAdminApiLog(request, response, reqBody, errorResponse));
    }

    private boolean shouldLogging(HttpServletRequest request, HttpServletResponse response) {
        String contextPathRemovedPath = request.getRequestURI().replaceFirst(contextPath, "");
        String method = request.getMethod();
        int status = response.getStatus();
        if (!HttpRequestUtils.isAllowableMethod(method)) {
            return false;
        }
        if (StringUtils.equalsIgnoreCase("GET", method) && status < 400) {
            return false;
        }
        return !ExcludeUrlUtils.isExcludeUrls(loggingExcludeUrls, contextPathRemovedPath);
    }

}

     * **/