package privateApp.configuration;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import privateApp.auth.JwtAuthenticationFilter;
import privateApp.services.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableScheduling
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomLogoutHandler logoutHandler;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          CustomLogoutHandler logoutHandler) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.logoutHandler = logoutHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
        		.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfiguration = new CorsConfiguration();
                    corsConfiguration.setAllowedOrigins(List.of("http://localhost:3000")); // Origines autorisées
                    corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE")); // Méthodes autorisées
                    corsConfiguration.setAllowedHeaders(List.of("*")); // En-têtes autorisés
                    corsConfiguration.setAllowCredentials(true); // Autoriser les cookies
                    return corsConfiguration;
                }))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Public pour login/logout/change-password
                        .requestMatchers("/api/test/generate-hash").permitAll() // Ajout pour autoriser l'accès public à cet endpoint
                        .requestMatchers("/api/intendant/**").hasAuthority("INTENDANT") // Réservé à l'intendant
                     // Autoriser INTENDANT et RESPONSABLE_STOCK pour les requêtes GET sur /api/stock/**
                        .requestMatchers(HttpMethod.GET, "/api/stock/**").hasAnyAuthority("INTENDANT", "RESPONSABLE_STOCK")
                        // Restreindre POST, PUT, DELETE sur /api/stock/** à RESPONSABLE_STOCK uniquement
                        .requestMatchers("/api/stock/**").hasAuthority("RESPONSABLE_STOCK")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(200))
                )
                .userDetailsService(userDetailsService)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}