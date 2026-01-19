package com.zosh.controller;

import com.zosh.config.JwtProvider;
import com.zosh.domain.AccountStatus;
import com.zosh.domain.USER_ROLE;
import com.zosh.exception.SellerException;
import com.zosh.model.Seller;
import com.zosh.model.SellerReport;
import com.zosh.model.VerificationCode;
import com.zosh.repository.VerificationCodeRepository;
import com.zosh.response.ApiResponse;
import com.zosh.response.AuthResponse;
import com.zosh.service.*;
import com.zosh.service.impl.CustomeUserServiceImplementation;
import com.zosh.utils.OtpUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;
    private final SellerReportService sellerReportService;
    private final EmailService emailService;
    private final VerificationCodeRepository verificationCodeRepository;
    private final VerificationService verificationService;
    private final JwtProvider jwtProvider;
    private final CustomeUserServiceImplementation customeUserServiceImplementation;

    // ===================== LOGIN OTP =====================

    @PostMapping("/sent/login-top")
    public ResponseEntity<ApiResponse> sentLoginOtp(
            @RequestBody VerificationCode req) throws SellerException {

        sellerService.getSellerByEmail(req.getEmail());

        String otp = OtpUtils.generateOTP();
        VerificationCode verificationCode =
                verificationService.createVerificationCode(otp, req.getEmail());

        String subject = "Zosh Bazaar Login OTP";
        String text = "Your login OTP is: ";

        emailService.sendVerificationOtpEmail(
                req.getEmail(),
                verificationCode.getOtp(),
                subject,
                text
        );

        ApiResponse res = new ApiResponse();
        res.setMessage("OTP sent successfully");
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PostMapping("/verify/login-top")
    public ResponseEntity<AuthResponse> verifyLoginOtp(
            @RequestBody VerificationCode req) throws SellerException {

        VerificationCode verificationCode =
                verificationCodeRepository.findByEmail(req.getEmail());

        if (verificationCode == null ||
                !verificationCode.getOtp().equals(req.getOtp())) {
            throw new SellerException("Wrong OTP");
        }

        Authentication authentication = authenticate(req.getEmail());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtProvider.generateToken(authentication);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setMessage("Login Success");
        authResponse.setJwt(token);

        Collection<? extends GrantedAuthority> authorities =
                authentication.getAuthorities();

        String roleName =
                authorities.isEmpty() ? null : authorities.iterator().next().getAuthority();

        authResponse.setRole(USER_ROLE.valueOf(roleName));

        return ResponseEntity.ok(authResponse);
    }

    private Authentication authenticate(String username) {

        UserDetails userDetails =
                customeUserServiceImplementation.loadUserByUsername("seller_" + username);

        if (userDetails == null) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    // ===================== EMAIL VERIFICATION =====================

    @PatchMapping("/verify/{otp}")
    public ResponseEntity<Seller> verifySellerEmail(
            @PathVariable String otp) throws SellerException {

        VerificationCode verificationCode =
                verificationCodeRepository.findByOtp(otp);

        if (verificationCode == null ||
                !verificationCode.getOtp().equals(otp)) {
            throw new SellerException("Wrong OTP");
        }

        Seller seller =
                sellerService.verifyEmail(verificationCode.getEmail(), otp);

        return ResponseEntity.ok(seller);
    }

    // ===================== CREATE SELLER =====================

    @PostMapping
    public ResponseEntity<Seller> createSeller(
            @RequestBody Seller seller) throws SellerException {

        Seller savedSeller = sellerService.createSeller(seller);

        String otp = OtpUtils.generateOTP();
        VerificationCode verificationCode =
                verificationService.createVerificationCode(otp, seller.getEmail());

        String subject = "Zosh Bazaar Email Verification";
        String frontendUrl =
                "https://varijashoppingstore.vercel.app/verify-seller/";
        String text =
                "Welcome to Zosh Bazaar. Verify your account using this link: "
                        + frontendUrl;

        emailService.sendVerificationOtpEmail(
                seller.getEmail(),
                verificationCode.getOtp(),
                subject,
                text
        );

        return new ResponseEntity<>(savedSeller, HttpStatus.CREATED);
    }

    // ===================== SELLER APIs =====================

    @GetMapping("/{id}")
    public ResponseEntity<Seller> getSellerById(
            @PathVariable Long id) throws SellerException {

        return ResponseEntity.ok(sellerService.getSellerById(id));
    }

    @GetMapping("/profile")
    public ResponseEntity<Seller> getSellerByJwt(
            @RequestHeader("Authorization") String jwt) throws SellerException {

        String email = jwtProvider.getEmailFromJwtToken(jwt);
        return ResponseEntity.ok(sellerService.getSellerByEmail(email));
    }

    @GetMapping("/report")
    public ResponseEntity<SellerReport> getSellerReport(
            @RequestHeader("Authorization") String jwt) throws SellerException {

        String email = jwtProvider.getEmailFromJwtToken(jwt);
        Seller seller = sellerService.getSellerByEmail(email);

        return ResponseEntity.ok(
                sellerReportService.getSellerReport(seller)
        );
    }

    @GetMapping
    public ResponseEntity<List<Seller>> getAllSellers(
            @RequestParam(required = false) AccountStatus status) {

        return ResponseEntity.ok(sellerService.getAllSellers(status));
    }

    @PatchMapping
    public ResponseEntity<Seller> updateSeller(
            @RequestHeader("Authorization") String jwt,
            @RequestBody Seller seller) throws SellerException {

        Seller profile = sellerService.getSellerProfile(jwt);
        return ResponseEntity.ok(
                sellerService.updateSeller(profile.getId(), seller)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeller(
            @PathVariable Long id) throws SellerException {

        sellerService.deleteSeller(id);
        return ResponseEntity.noContent().build();
    }
}
