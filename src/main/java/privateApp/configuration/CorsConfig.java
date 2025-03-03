package privateApp.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Applique à tous les endpoints,les règles CORS 
                        .allowedOrigins("http://localhost:3000") // Origine autorisée (frontend React)
                        .allowedMethods("GET", "POST", "PUT", "DELETE") // Méthodes autorisées
                        .allowedHeaders("*") // Permet à l'application de recevoir toutes sortes d'en-têtes HTTP dans les requêtes. Cela peut inclure des en-têtes spécifiques comme Authorization ou Content-Type.
                        .allowCredentials(true); // Autorise les cookies et les informations d'authentification
            }
        };
    }
}