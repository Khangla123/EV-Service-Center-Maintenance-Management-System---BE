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

                        // Swagger access - Cấu hình đầy đủ cho SpringDoc OpenAPI 3
                        .requestMatchers(HttpMethod.GET, "/api/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/swagger-ui/index.html").permitAll()

                        // AuthController
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll() //đăng nhập
                        .requestMatchers(HttpMethod.POST, "/api/auth/forgot-password").permitAll()// nhập mail quên mk
                        .requestMatchers(HttpMethod.POST, "/api/auth/verify-otp").permitAll()// nhập otp
                        .requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()// mk mới
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").permitAll()// hiển thị thông tin user hiện tại
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()// đăng xuất

                        // UserController
                        .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()// đăng kí tài khoản
                        .requestMatchers(HttpMethod.GET, "/api/users").permitAll() //danh sách user
                        .requestMatchers(HttpMethod.GET, "/api/users/**").permitAll() //lấy thông tin user theo id
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").permitAll() //cập nhật user theo id
                        .requestMatchers(HttpMethod.PATCH, "/api/users/*/role").permitAll() //cập nhật role user theo id
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").permitAll() //xóa user theo id

                        // CustomerController
                        .requestMatchers(HttpMethod.GET, "/api/customers").permitAll() // danh sách khách hàng
                        .requestMatchers(HttpMethod.POST, "/api/customers").permitAll() // tạo khách hàng mới
                        .requestMatchers(HttpMethod.GET, "/api/customers/**").permitAll() // chi tiết khách hàng
                        .requestMatchers(HttpMethod.PUT, "/api/customers/**").permitAll() // cập nhật khách hàng
                        .requestMatchers(HttpMethod.DELETE, "/api/customers/**").permitAll() // xóa khách hàng
                        .requestMatchers(HttpMethod.GET, "/api/customers/me").permitAll() // hồ sơ của tôi
                        .requestMatchers(HttpMethod.PUT, "/api/customers/me").permitAll() // cập nhật hồ sơ

                        // MailController
                        .requestMatchers(HttpMethod.GET, "/api/mail/receive_email").permitAll()

                        // ServiceCenterController
                        .requestMatchers(HttpMethod.GET, "/api/service-centers").permitAll() // danh sách trung tâm dịch vụ
                        .requestMatchers(HttpMethod.POST, "/api/service-centers").permitAll() // tạo trung tâm dịch vụ mới
                        .requestMatchers(HttpMethod.GET, "/api/service-centers/**").permitAll() // chi tiết trung tâm dịch vụ
                        .requestMatchers(HttpMethod.PUT, "/api/service-centers/**").permitAll() // cập nhật trung tâm dịch vụ
                        .requestMatchers(HttpMethod.DELETE, "/api/service-centers/**").permitAll() // xóa trung tâm dịch vụ

                        // ServicePackageController
                        .requestMatchers(HttpMethod.GET, "/api/service-packages").permitAll() // danh sách gói dịch vụ
                        .requestMatchers(HttpMethod.POST, "/api/service-packages").permitAll() // tạo gói dịch vụ mới
                        .requestMatchers(HttpMethod.GET, "/api/service-packages/**").permitAll() // chi tiết gói dịch vụ
                        .requestMatchers(HttpMethod.PUT, "/api/service-packages/**").permitAll() // cập nhật gói dịch vụ
                        .requestMatchers(HttpMethod.DELETE, "/api/service-packages/**").permitAll() // xóa gói dịch vụ
                        .requestMatchers(HttpMethod.GET, "/api/service-packages/center/**").permitAll() // gói dịch vụ theo trung tâm

                        // VehicleController
                        .requestMatchers(HttpMethod.GET, "/api/vehicles").permitAll() // tìm kiếm xe (danh sách loại xe)
                        .requestMatchers(HttpMethod.POST, "/api/vehicles").permitAll() // thêm loại xe mới
                        .requestMatchers(HttpMethod.GET, "/api/vehicles/{id}").permitAll() // chi tiết loại xe
                        .requestMatchers(HttpMethod.PUT, "/api/vehicles/{id}").permitAll() // cập nhật loại xe
                        .requestMatchers(HttpMethod.DELETE, "/api/vehicles/{id}").permitAll() // xóa loại xe
                        .requestMatchers(HttpMethod.GET, "/api/vehicles/customers/{customerId}/vehicles").permitAll() // xe của khách hàng
                        .requestMatchers(HttpMethod.POST, "/api/vehicles/customers/{customerId}/vehicles").permitAll() // thêm xe cho khách hàng


                        // VehicleModelController - Quản lý mẫu xe
                        .requestMatchers(HttpMethod.GET, "/api/vehicle-models").permitAll() // danh sách mẫu xe
                        .requestMatchers(HttpMethod.POST, "/api/vehicle-models").permitAll() // tạo mẫu xe mới
                        .requestMatchers(HttpMethod.GET, "/api/vehicle-models/**").permitAll() // chi tiết mẫu xe
                        .requestMatchers(HttpMethod.PUT, "/api/vehicle-models/**").permitAll() // cập nhật mẫu xe
                        .requestMatchers(HttpMethod.DELETE, "/api/vehicle-models/**").permitAll() // xóa mẫu xe
                        .requestMatchers(HttpMethod.GET, "/api/vehicle-models/manufacturer/**").permitAll() // mẫu xe theo hãng

                        // AppointmentController
                        .requestMatchers(HttpMethod.GET, "/api/appointments").permitAll() // danh sách lịch hẹn
                        .requestMatchers(HttpMethod.POST, "/api/appointments").permitAll() // đặt lịch hẹn
                        .requestMatchers(HttpMethod.GET, "/api/appointments/**").permitAll() // chi tiết lịch hẹn
                        .requestMatchers(HttpMethod.PUT, "/api/appointments/**").permitAll() // cập nhật lịch hẹn
                        .requestMatchers(HttpMethod.DELETE, "/api/appointments/**").permitAll() // hủy lịch hẹn
                        .requestMatchers(HttpMethod.GET, "/api/appointments/me").permitAll() // lịch hẹn của tôi
                        .requestMatchers(HttpMethod.GET, "/api/appointments/available").permitAll() // khung giờ trống

                        // ServiceOrderController
                        .requestMatchers(HttpMethod.GET, "/api/service-orders").permitAll() // danh sách đơn dịch vụ
                        .requestMatchers(HttpMethod.POST, "/api/service-orders").permitAll() // tạo đơn dịch vụ
                        .requestMatchers(HttpMethod.GET, "/api/service-orders/technician/me").permitAll() // service orders của technician với checklist
                        .requestMatchers(HttpMethod.GET, "/api/service-orders/appointment/**").permitAll() // service order theo appointment ID
                        .requestMatchers(HttpMethod.GET, "/api/service-orders/**").permitAll() // chi tiết đơn dịch vụ
                        .requestMatchers(HttpMethod.PUT, "/api/service-orders/**").permitAll() // cập nhật đơn dịch vụ
                        .requestMatchers(HttpMethod.POST, "/api/service-orders/from-appointment/**").permitAll() // tạo service order từ appointment
                        .requestMatchers(HttpMethod.PUT, "/api/service-orders/*/assign").permitAll() // phân công thợ
                        .requestMatchers(HttpMethod.PUT, "/api/service-orders/*/status").permitAll() // cập nhật trạng thái
                        .requestMatchers(HttpMethod.GET, "/api/service-orders/my-assignments").permitAll() // công việc được giao

                        // StaffController
                        .requestMatchers(HttpMethod.GET, "/api/staff").permitAll() // danh sách nhân viên
                        .requestMatchers(HttpMethod.POST, "/api/staff").permitAll() // thêm nhân viên mới
                        .requestMatchers(HttpMethod.GET, "/api/staff/{id}").permitAll() // chi tiết nhân viên
                        .requestMatchers(HttpMethod.PUT, "/api/staff/{id}").permitAll() // cập nhật nhân viên
                        .requestMatchers(HttpMethod.DELETE, "/api/staff/{id}").permitAll() // xóa nhân viên
                        .requestMatchers(HttpMethod.GET, "/api/staff/available").permitAll() // nhân viên rảnh
                        .requestMatchers(HttpMethod.GET, "/api/staff/my-profile").permitAll() // hồ sơ của tôi
                        .requestMatchers(HttpMethod.PUT, "/api/staff/my-profile").permitAll() // cập nhật hồ sơ

                        // PartController - Quản lý phụ tùng
                        .requestMatchers(HttpMethod.GET, "/api/parts").permitAll() // danh sách phụ tùng
                        .requestMatchers(HttpMethod.POST, "/api/parts").permitAll() // thêm phụ tùng mới
                        .requestMatchers(HttpMethod.GET, "/api/parts/{id}").permitAll() // chi tiết phụ tùng
                        .requestMatchers(HttpMethod.PUT, "/api/parts/{id}").permitAll() // cập nhật phụ tùng
                        .requestMatchers(HttpMethod.DELETE, "/api/parts/{id}").permitAll() // xóa phụ tùng
                        .requestMatchers(HttpMethod.GET, "/api/parts/low-stock").permitAll() // phụ tùng sắp hết
                        .requestMatchers(HttpMethod.POST, "/api/parts/{id}/restock").permitAll() // nhập kho phụ tùng

                        // InvoiceController - Quản lý hóa đơn
                        .requestMatchers(HttpMethod.GET, "/api/invoices").permitAll() // danh sách hóa đơn
                        .requestMatchers(HttpMethod.POST, "/api/invoices").permitAll() // tạo hóa đơn
                        .requestMatchers(HttpMethod.GET, "/api/invoices/{id}").permitAll() // chi tiết hóa đơn
                        .requestMatchers(HttpMethod.PUT, "/api/invoices/{id}").permitAll() // cập nhật hóa đơn
                        .requestMatchers(HttpMethod.GET, "/api/invoices/me").permitAll() // hóa đơn của tôi

                        // PaymentController - Quản lý thanh toán
                        .requestMatchers(HttpMethod.GET, "/api/payments").permitAll() // danh sách thanh toán
                        .requestMatchers(HttpMethod.POST, "/api/payments").permitAll() // thanh toán
                        .requestMatchers(HttpMethod.GET, "/api/payments/{id}").permitAll() // chi tiết thanh toán
                        .requestMatchers(HttpMethod.PUT, "/api/payments/{id}/verify").permitAll() // xác nhận thanh toán
                        .requestMatchers(HttpMethod.GET, "/api/payments/methods").permitAll() // phương thức thanh toán
                        .requestMatchers(HttpMethod.POST, "/api/payments/vnpay/create").permitAll() // tạo URL thanh toán VNPay
                        .requestMatchers(HttpMethod.GET, "/api/payments/vnpay/callback").permitAll() // VNPay callback
                        .requestMatchers(HttpMethod.POST, "/api/payments/mock/create").permitAll() // tạo URL thanh toán MOCK
                        .requestMatchers(HttpMethod.POST, "/api/payments/mock/callback").permitAll() // MOCK callback

                        // Test endpoints
                        .requestMatchers("/api/test/**").permitAll() // test endpoints
                        .requestMatchers("/api/maintenance-history/test").permitAll() // test maintenance endpoint

                        // MaintenanceHistoryController - Lịch sử bảo dưỡng

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