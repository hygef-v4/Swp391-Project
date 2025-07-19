package org.swp391.hotelbookingsystem.config;

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
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.swp391.hotelbookingsystem.handler.CustomRememberMeServices;
import org.swp391.hotelbookingsystem.handler.CustomUserDetailsService;
import org.swp391.hotelbookingsystem.handler.FormLoginSuccessHandler;
import org.swp391.hotelbookingsystem.handler.OAuth2LoginSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    private final FormLoginSuccessHandler formLoginSuccessHandler;

    private final CustomUserDetailsService customUserDetailsService;

    private final CustomRememberMeServices customRememberMeServices;

    public SecurityConfig(OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler, FormLoginSuccessHandler formLoginSuccessHandler, CustomUserDetailsService customUserDetailsService, CustomRememberMeServices customRememberMeServices) {
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
        this.formLoginSuccessHandler = formLoginSuccessHandler;
        this.customUserDetailsService = customUserDetailsService;
        this.customRememberMeServices =  customRememberMeServices;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/files/**", "/webhook", "/booking",
                                "/update-hotel", "/update-room", "/delete-room", "/deactivate-room", "/activate-room", "/update-cancellation-policy", "/ws/**", "/api/chat/**",
                                "/api/notifications/**", "/test-notifications", "/invoice", "/refund", "/send-message"
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/home", "/error", "/webhook", "/contact", "/send-message",
                                "/login", "/register", "/verify-email-otp", "/resend-otp", "/forgotPassword", "/resetPassword",
                                "/hotel-list", "/filter-hotels", "/hotel-detail", "/about", "/faq",
                                "/css/**", "/js/**", "/images/**", "/assets/**",
                                "/api/files/**", "user-profile", "user-wishlist", "/ws/**", "/api/chat/**", "/api/notifications/**", "/test-notifications", "/refund"
                        ).permitAll()
                        .requestMatchers("/admin-dashboard", "/admin-user-list", "/admin-user-detail", "/admin-hotel-list", "/admin/locations/add", "/admin-agent-detail", "admin-agent-list", "admin-booking-list", "/admin-review").access(AuthorizationManagers.allOf(
                                new WebExpressionAuthorizationManager("isFullyAuthenticated()"),
                                new WebExpressionAuthorizationManager("hasRole('ADMIN')")
                        ))
                        .requestMatchers("/admin/user/toggle-status/**").access(AuthorizationManagers.allOf(
                                new WebExpressionAuthorizationManager("isFullyAuthenticated()"),
                                new WebExpressionAuthorizationManager("hasRole('ADMIN')")
                        ))
                        .requestMatchers("/host-dashboard","/add-hotel","/add-room","/host/request-deactivate-hotel",
                                "/host/confirm-deactivate-hotel","/host/activate-hotel","/manage-hotel","/update-hotel","/update-room","/delete-room",
                                "/deactivate-room","/activate-room","/update-cancellation-policy","/host-customers","/host-customer-detail").access(AuthorizationManagers.allOf(
                                new WebExpressionAuthorizationManager("isFullyAuthenticated()"),
                                new WebExpressionAuthorizationManager("hasAnyRole('HOTEL_OWNER', 'ADMIN')")
                        ))
                        .requestMatchers("/contact-options", "/customer-moderators", "/customer-admins", "/host-moderators", "/host-admins",
                                "/admin-hosts", "/admin-customers", "/admin-moderators", "/moderator-hosts", "/moderator-admins", "/moderator-contact-users",
                                "/customer-moderator-contact", "/customer-admin-contact", "/agent-moderator-contact", "/agent-admin-contact",
                                "/admin-agent-contact", "/admin-customer-contact", "/admin-moderator-contact", "/moderator-customer-contact",
                                "/moderator-agent-contact", "/moderator-admin-contact").authenticated()
                        .requestMatchers("/moderator-dashboard", "/moderator-user-list").access(AuthorizationManagers.allOf(
                                new WebExpressionAuthorizationManager("isFullyAuthenticated()"),
                                new WebExpressionAuthorizationManager("hasAnyRole('MODERATOR', 'ADMIN')")
                        ))
                        .requestMatchers("/moderator-hotel-list").access(AuthorizationManagers.allOf(
                                new WebExpressionAuthorizationManager("isFullyAuthenticated()"),
                                new WebExpressionAuthorizationManager("hasAnyRole('MODERATOR', 'ADMIN')")
                        ))
                        .requestMatchers("/moderator-hotel-list/api/moderator/hotels/**").access(AuthorizationManagers.allOf(
                                new WebExpressionAuthorizationManager("isFullyAuthenticated()"),
                                new WebExpressionAuthorizationManager("hasAnyRole('MODERATOR', 'ADMIN')")
                        ))
                        .requestMatchers("/moderator-user-list/flag/**").access(AuthorizationManagers.allOf(
                                new WebExpressionAuthorizationManager("isFullyAuthenticated()"),
                                new WebExpressionAuthorizationManager("hasAnyRole('MODERATOR', 'ADMIN')")
                        ))
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(formLoginSuccessHandler)
                        .failureUrl("/login?error=true")
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .rememberMe(r -> r
                        .rememberMeServices(customRememberMeServices)
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
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
