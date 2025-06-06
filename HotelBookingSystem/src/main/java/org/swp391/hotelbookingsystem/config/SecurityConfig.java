package org.swp391.hotelbookingsystem.config;

import org.swp391.hotelbookingsystem.handler.CustomUserDetailsService;
import org.swp391.hotelbookingsystem.handler.FormLoginSuccessHandler;
import org.swp391.hotelbookingsystem.handler.OAuth2LoginSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Autowired
    private FormLoginSuccessHandler formLoginSuccessHandler;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/files/**", "/webhook")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/home", "/error", "webhook",
                                "/login", "/register", "/verify-email-otp", "/resend-otp", "/forgotPassword", "/resetPassword",
                                "/hotel-list", "/hotel-detail",
                                "/css/**", "/js/**", "/images/**", "/assets/**",
                                "/api/files/**", "user-profile", "user-wishlist"
                        ).permitAll()
                        .requestMatchers("/admin-dashboard", "/admin-user-list", "/admin-hotel-list").access(AuthorizationManagers.allOf(
                                new WebExpressionAuthorizationManager("isFullyAuthenticated()"),
                                new WebExpressionAuthorizationManager("hasRole('ADMIN')")
                        ))
                        .requestMatchers("/admin-dashboard").access(AuthorizationManagers.allOf(
                                new WebExpressionAuthorizationManager("isFullyAuthenticated()"),
                                new WebExpressionAuthorizationManager("hasAnyRole('MODERATOR', 'ADMIN')")
                        ))
                        .requestMatchers("/host-dashboard","/add-hotel","/add-room","/request-delete-hotel").access(AuthorizationManagers.allOf(
                                new WebExpressionAuthorizationManager("isFullyAuthenticated()"),
                                new WebExpressionAuthorizationManager("hasAnyRole('HOTEL_OWNER', 'ADMIN')")
                        ))
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(formLoginSuccessHandler)
                        .failureUrl("/login?error=true")
                )
                .rememberMe(r -> r
                        .key("bKJHkjsdf8723hJKH8sd89fjsd0239JKLHkjasdf987sdf")
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                        .userDetailsService(customUserDetailsService)
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .logout(logout -> logout.permitAll())
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(accessDeniedHandler)
                        .authenticationEntryPoint(authenticationEntryPoint)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

// AccessDeniedHandler: user đã login nhưng không đủ quyền
private final AccessDeniedHandler accessDeniedHandler = (request, response, ex) -> {
    System.out.println("ACCESS DENIED | User: " + request.getUserPrincipal() + " | URI: " + request.getRequestURI());
    response.sendRedirect("/error?access-denied");
};

// AuthenticationEntryPoint: chưa login mà truy cập trang cần quyền
private final AuthenticationEntryPoint authenticationEntryPoint = (request, response, ex) -> {
    System.out.println("UNAUTHORIZED ACCESS | URI: " + request.getRequestURI());
    response.sendRedirect("/login?unauthorized");
};
}
