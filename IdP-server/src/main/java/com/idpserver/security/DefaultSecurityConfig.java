package com.idpserver.security;

import com.idpserver.config.cors.CorsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

@Configuration
public class DefaultSecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private CorsConfig corsConfig;

    @Value("${auth.paths.login-url}")
    private String ssoLoginUrl;

    @Value("${auth.paths.logout-url}")
    private String sloLogoutUrl;

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(ssoLoginUrl).permitAll()
                        .anyRequest().authenticated()
                )

                .logout(logout -> logout // IdP -> IdP Logout(자체 로그아웃) 요청 성공 후 호출될 Custom Handler
                        .logoutUrl(sloLogoutUrl) // 사용자가 호출할 로그아웃 경로
                        // .logoutSuccessUrl("/") // 로그아웃 성공 시 리디렉션될 IdP 내부 경로
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()) // 성공 시 기본 200 OK 반환 (SPA 등에서 유용)
                        .invalidateHttpSession(true) // 세션 무효화
                        .clearAuthentication(true)   // 인증 정보 제거
                        .deleteCookies("JSESSIONID") // 세션 쿠키 삭제
                )
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화 (테스트용)
                .userDetailsService(userDetailsService);

        return http.build();
    }
}