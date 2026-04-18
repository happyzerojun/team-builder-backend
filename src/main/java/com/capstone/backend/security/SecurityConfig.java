package com.capstone.backend.security;

import com.capstone.backend.global.jwt.ApiAuthenticationFilter;
import com.capstone.backend.global.jwt.JwtFilter;
import com.capstone.backend.security.oauth.CustomOAuth2UserService;
import com.capstone.backend.security.oauth.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ApiAuthenticationFilter apiAuthenticationFilter;
    private final JwtFilter jwtFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())

                // 🔥 기본 로그인 완전 차단
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/signup", "/api/auth/oauth2/**", "/oauth2/**", "/login/oauth2/**").permitAll()

                        // 🔥 영준님이 추가: 기술 스택 API 누구나 조회 가능하게 열어두기!
                        .requestMatchers("/api/tech-stacks", "/api/tech-stacks/**").permitAll()

                        // 🔥 영준님이 추가: 프론트엔드 팀원들이 Swagger 명세서 볼 수 있게 열어두기! , 스웨거 관련 주소와 테스트 API는 모두 출입 허락 (프리패스!)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/api/test", "/error").permitAll()

                        // 🚀 [신규] 프론트엔드 무원님 API 연동 테스트를 위해 통행증 발급!
                        .requestMatchers("/api/application/**", "/api/project/**", "/api/review/**").permitAll()

                        .requestMatchers("/api/auth/me").authenticated()
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                )

                .addFilterBefore(jwtFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(apiAuthenticationFilter, JwtFilter.class)

                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
