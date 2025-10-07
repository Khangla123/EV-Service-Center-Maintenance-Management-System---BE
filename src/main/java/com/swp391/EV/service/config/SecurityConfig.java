package com.swp391.EV.service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomJwtDecoder customJwtDecoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints


                        // Swagger access
                        .requestMatchers(HttpMethod.GET, "/api/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/swagger-ui/index.html").permitAll()


                        // AuthController
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll() //đăng nhập
                        .requestMatchers(HttpMethod.POST, "/api/auth/forgot-password").permitAll()// nhập mail quên mk
                        .requestMatchers(HttpMethod.POST, "/api/auth/verify-otp").permitAll()// nhập otp
                        .requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()// mk mới
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").permitAll()// hiển thị thôgn tin user hiện tại
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()// đăng xuất


                        // UserController
                        .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()// đăng kí tài khoản
                        .requestMatchers(HttpMethod.GET, "/api/users/user").permitAll() //danh sách user
                        .requestMatchers(HttpMethod.GET, "/api/users/user/**").permitAll() //lấy thông tin user theo id
                        .requestMatchers(HttpMethod.PUT, "/api/users/user/**").permitAll() //cập nhật user theo id
                        .requestMatchers(HttpMethod.PATCH, "/api/users/user/*/role").permitAll() //cập nhật role user theo id
                        .requestMatchers(HttpMethod.DELETE, "/api/users/user/**").permitAll() //xóa user theo id


                        // CustomerController
                        .requestMatchers(HttpMethod.GET, "/api/customers").permitAll() // danh sách khách hàng
                        .requestMatchers(HttpMethod.POST, "/api/customers").permitAll() // tạo khách hàng mới
                        .requestMatchers(HttpMethod.GET, "/api/customers/*").permitAll() // chi tiết khách hàng
                        .requestMatchers(HttpMethod.PUT, "/api/customers/*").permitAll() // cập nhật khách hàng
                        .requestMatchers(HttpMethod.GET, "/api/customers/me").permitAll() // hồ sơ của tôi
                        .requestMatchers(HttpMethod.PUT, "/api/customers/me").permitAll() // cập nhật hồ sơ

                        // MailController
                        .requestMatchers(HttpMethod.GET, "/api/mail/receive_email").permitAll()




                        .anyRequest().authenticated()
                );

        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer ->
                        jwtConfigurer.decoder(customJwtDecoder)
                                .jwtAuthenticationConverter(converter()))
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint()));

        http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    JwtAuthenticationConverter converter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("http://localhost:3000");
        corsConfiguration.addAllowedOrigin("http://localhost:3001");
        corsConfiguration.addAllowedOrigin("http://localhost:3002");
        corsConfiguration.addAllowedOrigin("http://localhost:8080");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(source);
    }
}