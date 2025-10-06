package com.swp391.EV.service.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.swp391.EV.service.dto.response.IntrospectResponse;
import com.swp391.EV.service.dto.response.LoginResponse;
import com.swp391.EV.service.dto.response.MeResponse;
import com.swp391.EV.service.exception.AppException;
import com.swp391.EV.service.exception.ErrorCode;
import com.swp391.EV.service.model.PasswordResetToken;
import com.swp391.EV.service.model.User;
import com.swp391.EV.service.repository.PasswordResetTokenRepository;
import com.swp391.EV.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;


import java.sql.Date;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;


@Service
public class AuthService {
    @Value("${jwt.signer-key}")
    private String KEY;
    @Value("${jwt.expiration-duration}")
    private long EXPIRATION_DURATION;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    @Autowired
    private MailService mailService;

    public LoginResponse login(String email, String password) throws JOSEException {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new ResponseStatusException(
                    ErrorCode.EMPTY_CREDENTIALS.getStatusCode(),
                    ErrorCode.EMPTY_CREDENTIALS.getMessage()
            );
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        ErrorCode.USERNAME_OR_PASSWORD_ERROR.getStatusCode(),
                        ErrorCode.USERNAME_OR_PASSWORD_ERROR.getMessage()
                ));

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(
                    ErrorCode.USERNAME_OR_PASSWORD_ERROR.getStatusCode(),
                    ErrorCode.USERNAME_OR_PASSWORD_ERROR.getMessage()
            );
        }

        String token = generateToken(user);

        LoginResponse resp = LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .fullName(user.getFullName())
                .accessToken(token)
                .build();


        System.out.println("Generated Token: " + token);

        return resp;
    }


    // token
    public IntrospectResponse introspect(String token) {
        boolean isValid = true;
        try {
            verifyToken(token);
        } catch (Exception e) {
            isValid = false;
        }
        return IntrospectResponse.builder().valid(isValid).build();
    }

    private void verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        var verified = signedJWT.verify(verifier);
        if (!(verified)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private String generateToken(User user) throws JOSEException { // tao token
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId().toString())
                .issuer("Khanglv")
                .issueTime(Date.from(Instant.now()))
                .expirationTime(Date.from(Instant.now().plus(EXPIRATION_DURATION, ChronoUnit.SECONDS)))
                .claim("scope", user.getRole().toUpperCase())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        jwsObject.sign(new MACSigner(KEY));
        return jwsObject.serialize();
    }

    public String generateTokenForUser(User user) throws JOSEException {
        return generateToken(user);
    }

    public UUID extractUserIdFromToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String subject = signedJWT.getJWTClaimsSet().getSubject();
            return UUID.fromString(subject);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    public void requestPasswordRequest(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String otp = String.format("%06d", new Random().nextInt(1000000));
        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(email);
        token.setOtpCode(otp);
        token.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        tokenRepository.save(token);
        mailService.sendOtpEmail(email,otp);
    }


    public void verifyOtp(String email, String otpCode) {

        PasswordResetToken token = tokenRepository.findByEmailAndOtpCode(email, otpCode)
                .orElseThrow(() -> new AppException(ErrorCode.ERROR_OTP));

        if(token.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.EXPIRY_OTP);
        }
        token.setVerified(true);
        tokenRepository.save(token);
    }

    public void resetPassword(String email, String newPassword) {
        PasswordResetToken token = tokenRepository.findTopByEmailOrderByExpiryTimeDesc(email)
                .orElseThrow(() -> new AppException(ErrorCode.OTP_NOT_FOUND));

        if (!token.isVerified()) {
            throw new AppException(ErrorCode.OTP_NOT_VERIFY);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        tokenRepository.delete(token);
        userRepository.save(user);
    }

    public MeResponse getMe(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String userIdString = authentication.getName();
        UUID userId = UUID.fromString(userIdString);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return MeResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .phone(user.getPhone())
                .build();
    }



}
