package org.swp391.hotelbookingsystem.handler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

@Configuration
public class RememberMeConfig {

    private final CustomUserDetailsService userDetailsService;

    public RememberMeConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PersistentTokenRepository tokenRepository() {
        return new InMemoryTokenRepositoryImpl();
    }

    @Bean
    public CustomRememberMeServices customRememberMeServices() {
        CustomRememberMeServices services = new CustomRememberMeServices(
            "bKJHkjsdf8723hJKH8sd89fjsd0239JKLHkjasdf987sdf",
            userDetailsService,
            tokenRepository()
        );
        services.setAlwaysRemember(true);
        return services;
    }
}
