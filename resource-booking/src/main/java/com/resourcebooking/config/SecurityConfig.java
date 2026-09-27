package com.resourcebooking.config;

import com.resourcebooking.security.CustomUserDetailsService;
import com.resourcebooking.security.JwtAuthFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// @EnableMethodSecurity is required for @PreAuthorize on the controllers to actually do
// anything - without it those annotations are silently ignored (found this the hard way)
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthFilter jwtAuthFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // JwtAuthFilter is annotated @Component, and Spring Boot auto-registers every Filter bean
    // as a plain servlet filter on top of whatever we wire into the security chain below - so
    // without this it would run twice on every request. This bean just tells Boot not to
    // auto-register it; addFilterBefore() further down is the only place it actually runs.
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // not needed, we're stateless with jwt
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // public stuff
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        // resources - GET is open to any logged in user, everything else admin only
                        .requestMatchers("GET", "/api/resources/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/resources/**").hasRole("ADMIN")

                        // status changes are admin-only - matched here too (not just @PreAuthorize
                        // on the controller) so it's still blocked even if method security gets
                        // misconfigured again in the future
                        .requestMatchers("PATCH", "/api/reservations/*/status").hasRole("ADMIN")

                        // reservations - both roles can hit these, controller logic decides what they see
                        .requestMatchers("/api/reservations/**").hasAnyRole("ADMIN", "USER")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
