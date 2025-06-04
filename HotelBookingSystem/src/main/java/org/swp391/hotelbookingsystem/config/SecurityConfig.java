package org.swp391.hotelbookingsystem.config;

import org.swp391.hotelbookingsystem.handler.FormLoginSuccessHandler;
import org.swp391.hotelbookingsystem.handler.OAuth2LoginSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
                        .ignoringRequestMatchers("/api/files/**", "/webhook")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login", "/", "/register", "/forgot-password",
                                "/css/**", "/js/**", "/images/**", "/assets/**",
                                "/api/files/**", "/home", "/hotel-list", "/hotel-detail", "/error"
                        ).permitAll()
                        .requestMatchers("/admin-dashboard", "/admin-user-list", "/admin-hotel-list").hasRole("ADMIN")
                        .requestMatchers("/admin-dashboard").hasRole("MODERATOR")
                        .requestMatchers("/host-dashboard","/add-hotel","/add-room","/request-delete-hotel").hasRole("HOTEL_OWNER")
                        .requestMatchers("/webhook").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(formLoginSuccessHandler)
                        .failureUrl("/login?error=true")
                        .permitAll()
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
