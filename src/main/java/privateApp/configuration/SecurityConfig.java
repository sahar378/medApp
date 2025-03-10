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
                    corsConfiguration.setAllowedOrigins(List.of("http://localhost:3000"));
                    corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
                    corsConfiguration.setAllowedHeaders(List.of("*"));
                    corsConfiguration.setAllowCredentials(true);
                    return corsConfiguration;
                }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Public pour login/logout/change-password
                        .requestMatchers("/api/test/generate-hash").permitAll() // Public pour generate-hash
                        .requestMatchers("/api/intendant/**").hasAuthority("INTENDANT") // Réservé à l'intendant
                        .requestMatchers(HttpMethod.GET, "/api/stock/logs").hasAuthority("INTENDANT") // Logs réservés à l'intendant
                        .requestMatchers(HttpMethod.GET, "/api/stock/**").hasAnyAuthority("INTENDANT", "RESPONSABLE_STOCK") // Autres GET pour les deux rôles
                        .requestMatchers("/api/stock/**").hasAuthority("RESPONSABLE_STOCK") // POST, PUT, DELETE pour RESPONSABLE_STOCK
                        .requestMatchers("/api/medical/**").hasAuthority("PERSONNEL_MEDICAL")
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