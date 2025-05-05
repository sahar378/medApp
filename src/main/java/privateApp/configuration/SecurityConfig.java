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
                		// Routes publiques
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/test/generate-hash").permitAll()
                        // Intendant
                        .requestMatchers("/api/intendant/**").hasAuthority("INTENDANT")
                        .requestMatchers(HttpMethod.GET, "/api/stock/logs").hasAuthority("INTENDANT")
                        .requestMatchers(HttpMethod.POST, "/api/commande/*/approuver").hasAuthority("INTENDANT")
                        .requestMatchers(HttpMethod.POST, "/api/commande/*/envoyer").hasAuthority("INTENDANT")
                        .requestMatchers(HttpMethod.POST, "/api/commande/*/annuler").hasAuthority("INTENDANT")
                        // Stock (INTENDANT et RESPONSABLE_STOCK)
                        .requestMatchers(HttpMethod.GET, "/api/stock/produits/active/materiels").hasAnyAuthority("INTENDANT", "PERSONNEL_MEDICAL") // Updated to include PERSONNEL_MEDICAL

                        .requestMatchers(HttpMethod.GET, "/api/stock/**").hasAnyAuthority("INTENDANT", "RESPONSABLE_STOCK")
                        .requestMatchers(HttpMethod.GET, "/api/stock/alertes/medicaments").hasAnyAuthority("INTENDANT", "RESPONSABLE_STOCK") // Ajout explicite
                        .requestMatchers(HttpMethod.GET, "/api/stock/alertes/materiels").hasAnyAuthority("INTENDANT", "RESPONSABLE_STOCK") // Ajout explicite
                        
                        .requestMatchers(HttpMethod.POST, "/api/stock/**").hasAuthority("RESPONSABLE_STOCK")
                        .requestMatchers(HttpMethod.PUT, "/api/stock/**").hasAnyAuthority("RESPONSABLE_STOCK" , "PERSONNEL_MEDICAL")
                        .requestMatchers(HttpMethod.DELETE, "/api/stock/**").hasAuthority("RESPONSABLE_STOCK")
                        // Commandes
                        .requestMatchers(HttpMethod.GET, "/api/commande/**").hasAnyAuthority("INTENDANT", "RESPONSABLE_STOCK")
                        .requestMatchers(HttpMethod.POST, "/api/commande/creer").hasAuthority("RESPONSABLE_STOCK")
                        .requestMatchers(HttpMethod.POST, "/api/commande/creer-multi").hasAuthority("RESPONSABLE_STOCK") 
                        
                        .requestMatchers(HttpMethod.PUT, "/api/commande/**").hasAuthority("RESPONSABLE_STOCK")
                        .requestMatchers(HttpMethod.POST, "/api/commande/livraison").hasAuthority("RESPONSABLE_STOCK")
                     // Livraisons
                        .requestMatchers(HttpMethod.POST, "/api/livraisons").hasAuthority("RESPONSABLE_STOCK")
                        .requestMatchers(HttpMethod.GET, "/api/livraisons").hasAnyAuthority("INTENDANT", "RESPONSABLE_STOCK")
                        .requestMatchers(HttpMethod.GET, "/api/livraisons/{produitId}").hasAnyAuthority("INTENDANT", "RESPONSABLE_STOCK")
                        // Fournisseurs
                        .requestMatchers(HttpMethod.GET, "/api/fournisseurs/**").hasAnyAuthority("INTENDANT", "RESPONSABLE_STOCK")
                        .requestMatchers(HttpMethod.POST, "/api/fournisseurs/**").hasAuthority("RESPONSABLE_STOCK")
                        .requestMatchers(HttpMethod.PUT, "/api/fournisseurs/**").hasAuthority("RESPONSABLE_STOCK")
                        .requestMatchers(HttpMethod.DELETE, "/api/fournisseurs/**").hasAuthority("RESPONSABLE_STOCK")
                     // Prix (Nouveau)
                        .requestMatchers(HttpMethod.GET, "/api/prix/**").hasAnyAuthority("INTENDANT", "RESPONSABLE_STOCK")
                        .requestMatchers(HttpMethod.POST, "/api/prix/signaler").hasAuthority("INTENDANT")
                        .requestMatchers(HttpMethod.POST, "/api/prix/**").hasAuthority("RESPONSABLE_STOCK")
                        .requestMatchers(HttpMethod.PUT, "/api/prix/**").hasAuthority("RESPONSABLE_STOCK")
                        .requestMatchers(HttpMethod.DELETE, "/api/prix/**").hasAuthority("RESPONSABLE_STOCK")
                       // .requestMatchers(HttpMethod.POST, "/api/prix/signaler").hasAuthority("INTENDANT")
                        .requestMatchers(HttpMethod.GET, "/api/prix/produits").hasAnyAuthority("INTENDANT", "RESPONSABLE_STOCK")
                     // Medical - Fonctionnalités communes (accessibles à PERSONNEL_MEDICAL)
                        .requestMatchers(HttpMethod.GET, "/api/medical/produits").hasAuthority("INFIRMIER") // Pour faire l'inventaire
                        .requestMatchers(HttpMethod.POST, "/api/medical/inventaire/verifier").hasAuthority("INFIRMIER") // Vérification par l'infirmier
                        .requestMatchers(HttpMethod.GET, "/api/medical/inventaire/historique").hasAnyAuthority("INTENDANT", "RESPONSABLE_STOCK") // Historique pour INTENDANT et RESPONSABLE_STOCK
                        .requestMatchers(HttpMethod.GET, "/api/medical/inventaire/{id}").hasAnyAuthority("INTENDANT", "RESPONSABLE_STOCK") // Détails pour INTENDANT et RESPONSABLE_STOCK
                        
                        
                        
                     // Infirmier
                        //Machines
                        .requestMatchers(HttpMethod.GET, "/api/machines/**").hasAnyAuthority("INFIRMIER", "INTENDANT", "PERSONNEL_MEDICAL")
                        .requestMatchers(HttpMethod.POST, "/api/machines/**").hasAuthority("INFIRMIER")
                        .requestMatchers(HttpMethod.PUT, "/api/machines/**").hasAuthority("INFIRMIER")
                        .requestMatchers(HttpMethod.DELETE, "/api/machines/**").hasAuthority("INFIRMIER")
                        //Techniciens
                        .requestMatchers(HttpMethod.GET, "/api/techniciens/**").hasAnyAuthority("INFIRMIER", "INTENDANT")
                        .requestMatchers(HttpMethod.POST, "/api/techniciens/**").hasAuthority("INFIRMIER")
                        .requestMatchers(HttpMethod.PUT, "/api/techniciens/**").hasAuthority("INFIRMIER")
                        .requestMatchers(HttpMethod.DELETE, "/api/techniciens/**").hasAuthority("INFIRMIER")
                        //Interventions
                        .requestMatchers(HttpMethod.GET, "/api/interventions/**").hasAnyAuthority("INFIRMIER", "INTENDANT")
                        .requestMatchers(HttpMethod.POST, "/api/interventions/**").hasAuthority("INFIRMIER")
                        .requestMatchers(HttpMethod.PUT, "/api/interventions/**").hasAuthority("INFIRMIER")

                        
                        //Super Admin
                        .requestMatchers("/api/super-admin/**").hasAuthority("SUPER_ADMIN")
                        
                        // Medical - Patients
                        .requestMatchers(HttpMethod.GET, "/api/medical/patients/actifs").hasAnyAuthority("PERSONNEL_MEDICAL", "MEDECIN", "INTENDANT") // Updated
                        .requestMatchers(HttpMethod.GET, "/api/medical/patients/search").hasAnyAuthority("PERSONNEL_MEDICAL", "MEDECIN", "INTENDANT")
                        .requestMatchers(HttpMethod.GET, "/api/medical/patients/search/actifs").hasAnyAuthority("PERSONNEL_MEDICAL", "MEDECIN","INTENDANT")
                        .requestMatchers(HttpMethod.GET, "/api/medical/patients/inactifs-non-archives").hasAnyAuthority("PERSONNEL_MEDICAL", "MEDECIN" ,"INTENDANT" )
                        .requestMatchers(HttpMethod.GET, "/api/medical/patients/search/inactifs-non-archives").hasAnyAuthority("PERSONNEL_MEDICAL", "MEDECIN" ,"INTENDANT" )
                        .requestMatchers(HttpMethod.GET, "/api/medical/patients").hasAnyAuthority("MEDECIN","INTENDANT")
                        .requestMatchers(HttpMethod.GET, "/api/medical/patients/archived").hasAnyAuthority("MEDECIN", "INTENDANT")

                        .requestMatchers(HttpMethod.GET, "/api/medical/patients/{id}").hasAuthority("MEDECIN")
                        .requestMatchers(HttpMethod.POST, "/api/medical/patients").hasAuthority("MEDECIN")
                        .requestMatchers(HttpMethod.PUT, "/api/medical/patients/{id}").hasAuthority("MEDECIN")
                        .requestMatchers(HttpMethod.PUT, "/api/medical/patients/{id}/toggle-actif").hasAuthority("MEDECIN")
                        .requestMatchers(HttpMethod.PUT, "/api/medical/patients/{id}/archive").hasAuthority("MEDECIN")
                        .requestMatchers(HttpMethod.PUT, "/api/medical/patients/{id}/unarchive").hasAuthority("MEDECIN")
                        .requestMatchers(HttpMethod.GET, "/api/medical/patients/search/archived").hasAnyAuthority("MEDECIN", "INTENDANT")
                        .requestMatchers(HttpMethod.GET, "/api/medical/patients/inactifs-non-archives").hasAnyAuthority("MEDECIN", "INTENDANT")
                       

                        
                     // Medical - Produits Standards
                        .requestMatchers(HttpMethod.GET, "/api/medical/patients/{patientId}/produits-standards").hasAnyAuthority("PERSONNEL_MEDICAL", "MEDECIN", "RESPONSABLE_STOCK","INTENDANT")
                        // Medical - Dialysis History
                        .requestMatchers(HttpMethod.GET, "/api/medical/patients/{patientId}/dialysis-history").hasAnyAuthority("MEDECIN", "INTENDANT") // Updated
                        // Medical - Seances et Mesures
                        .requestMatchers(HttpMethod.GET, "/api/medical/seances").hasAnyAuthority("PERSONNEL_MEDICAL", "INTENDANT")
                        .requestMatchers(HttpMethod.POST, "/api/medical/seances").hasAuthority("PERSONNEL_MEDICAL")
                        
                        .requestMatchers(HttpMethod.GET, "/api/medical/seances/{seanceId}/produits").hasAnyAuthority("PERSONNEL_MEDICAL", "RESPONSABLE_STOCK","INTENDANT")
                        .requestMatchers(HttpMethod.PUT, "/api/medical/seances/**").hasAuthority("PERSONNEL_MEDICAL")
                        .requestMatchers(HttpMethod.POST, "/api/medical/seances/{seanceId}/produits-non-standards").hasAuthority("PERSONNEL_MEDICAL")
                        .requestMatchers(HttpMethod.POST, "/api/medical/seances/{seanceId}/mesures").hasAuthority("PERSONNEL_MEDICAL")
                        .requestMatchers(HttpMethod.GET, "/api/medical/seances/{seanceId}").hasAnyAuthority("PERSONNEL_MEDICAL", "INTENDANT")
                        .requestMatchers(HttpMethod.GET, "/api/medical/seances/{seanceId}/mesures").hasAnyAuthority("PERSONNEL_MEDICAL","INTENDANT")
                        .requestMatchers(HttpMethod.GET, "/api/medical/produits-usage").hasAnyAuthority("PERSONNEL_MEDICAL", "INTENDANT")
                        .requestMatchers(HttpMethod.GET, "/api/medical/seances/patient").hasAnyAuthority("PERSONNEL_MEDICAL", "INTENDANT")
                        .requestMatchers(HttpMethod.GET, "/api/medical/seances/filter").hasAnyAuthority("PERSONNEL_MEDICAL", "RESPONSABLE_STOCK", "INTENDANT")
                        // Agent - Medical Personnel
                        .requestMatchers(HttpMethod.GET, "/api/agent/medical-personnel").hasAuthority("PERSONNEL_MEDICAL")
                        
                     // Toute autre requête nécessite une authentification
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