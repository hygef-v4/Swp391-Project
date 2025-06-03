package org.swp391.hotelbookingsystem.config;

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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Autowired
    private FormLoginSuccessHandler formLoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/files/**") //  disable CSRF for file upload API, need for Postman
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/home", "/error",
                                "/login", "/register", "/forgot-password",
                                "/hotel-list", "/hotel-detail",
                                "/css/**", "/js/**", "/images/**", "/assets/**",
                                "/api/files/**"
                        ).permitAll()
                        .requestMatchers("/admin-dashboard").access(AuthorizationManagers.allOf(
                                new WebExpressionAuthorizationManager("isFullyAuthenticated()"),
                                new WebExpressionAuthorizationManager("hasRole('ADMIN')")
                        ))
                        .requestMatchers("/admin-dashboard").access(AuthorizationManagers.allOf(
                                new WebExpressionAuthorizationManager("isFullyAuthenticated()"),
                                new WebExpressionAuthorizationManager("hasRole('MODERATOR')")
                        ))
                        .requestMatchers("/host-dashboard").access(AuthorizationManagers.allOf(
                                new WebExpressionAuthorizationManager("isFullyAuthenticated()"),
                                new WebExpressionAuthorizationManager("hasRole('HOTEL OWNER')")
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
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .logout(logout -> logout.permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
