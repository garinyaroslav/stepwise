package com.github.stepwise.configuration;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import com.github.stepwise.security.AuthEntryPointJwt;
import com.github.stepwise.security.AuthTokenFilter;
import com.github.stepwise.security.UserDetailsServiceImpl;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {
  @Autowired
  UserDetailsServiceImpl userDetailsService;
  @Autowired
  private AuthEntryPointJwt unauthorizedHandler;

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    // TODO: get url form application properties
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(
            cors -> cors.configurationSource(corsConfigurationSource()))
        .exceptionHandling(
            exceptionHandling -> exceptionHandling.authenticationEntryPoint(unauthorizedHandler))
        .sessionManagement(sessionManagement -> sessionManagement
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorizeRequests -> authorizeRequests.requestMatchers("/api/auth/**", "/swagger-ui/**",
                "/v3/api-docs/**", "api/auth/password/**").permitAll().anyRequest()
                .authenticated());
    http.addFilterBefore(authenticationJwtTokenFilter(),
        UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
