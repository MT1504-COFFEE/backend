package com.ucp.aseo_ucp_backend.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.ucp.aseo_ucp_backend.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Habilita @PreAuthorize si lo usas en controladores
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    // Spring inyectará automáticamente tu UserDetailsServiceImpl aquí si existe como Bean
    // private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Usa la configuración CORS definida abajo
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para API stateless
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No crear sesiones
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll() // Endpoints de autenticación públicos
                .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll() // Permitir acceso a archivos subidos
                .requestMatchers(HttpMethod.POST, "/api/upload").authenticated() // Subir archivos requiere autenticación (ajusta si es necesario)
                .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated() // Obtener usuario actual requiere token
                .requestMatchers(HttpMethod.GET, "/api/bathrooms").authenticated() // Ver baños requiere token
                // Endpoints de Actividades e Incidentes requieren rol específico
                 .requestMatchers("/api/cleaning-activities/**").hasAnyAuthority("cleaning_staff", "admin")
                 .requestMatchers("/api/incidents/**").hasAnyAuthority("cleaning_staff", "admin")
                 // Puedes ser más específico, ej:
                 //.requestMatchers(HttpMethod.GET, "/api/cleaning-activities").hasAuthority("admin")
                 //.requestMatchers(HttpMethod.POST, "/api/cleaning-activities").hasAuthority("cleaning_staff")
                .anyRequest().authenticated() // Cualquier otra petición requiere autenticación
            )
            // Añade el filtro JWT antes del filtro de autenticación estándar de Spring
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        // Obtiene el AuthenticationManager de la configuración de Spring Security
        return authenticationConfiguration.getAuthenticationManager();
    }



    // ... (tus otros beans como securityFilterChain, passwordEncoder, etc.) ...

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        String frontendUrl = System.getenv().getOrDefault("FRONTEND_URL", "http://localhost:3000");
        System.out.println("=== CORS Configuration ===");
        System.out.println("Allowed Origins: " + frontendUrl);
        
        // --- CAMBIO AQUÍ ---
        // Permite la URL de tu frontend local Y el patrón de ngrok
        configuration.setAllowedOrigins(List.of(frontendUrl));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With"
        ));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration); // Aplica a toda tu API
        return source;
    }
}