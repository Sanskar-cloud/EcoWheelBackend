package com.example.demo.controllers;

import com.example.demo.entities.User;
import com.example.demo.exceptions.ApiException;
import com.example.demo.exceptions.UserAlreadyExists;
import com.example.demo.payloads.*;
import com.example.demo.repository.UserRepo;
import com.example.demo.security.CustomUserDetailService;
import com.example.demo.security.JwtTokenHelper;
import com.example.demo.services.EmailService;
import com.example.demo.services.OtpService;
import com.example.demo.services.UserService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth/")
public class AuthController {
    @Autowired
    private CustomUserDetailService customUserDetailService;

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    EmailService emailService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;
    @Autowired
    private OtpService otpService;

    @PostMapping("/login")
    public ResponseEntity<?> createToken(@RequestBody LoginRequest request) throws Exception {
        if (isAdminCredentials(request.getUsername(), request.getPassword())) {

            System.out.println(request.getOtp()+"DEVIHO ERBIOV;HIBVOFVHFO IFBEGIOBVFVJFVBGB FBIJ GVNOFUBNFBEFGB FVBJG GF");
            boolean isOtpValid = otpService.verifyOtp(request.getUsername(), String.valueOf(request.getOtp()));
            System.out.println(request.getOtp()+"DEVIHO ERBIOV;HIBVOFVHFO IFBEGIOBVFVJFVBGB FBIJ GVNOFUBNFBEFGB FVBJG GF");

            if(isOtpValid){
                String token = jwtTokenHelper.generateAdminToken(request.getUsername());

                LoginResponse response = new LoginResponse();
                response.setToken(token);
                response.setUser(new UserDto());
                return ResponseEntity.ok(response);

            }
            else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
            }



        } else {
            boolean isOtpValid = otpService.verifyOtp(request.getUsername(), String.valueOf(request.getOtp()));
            if(isOtpValid){
                authenticate(request.getUsername(), request.getPassword());
                UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
                String token = jwtTokenHelper.generateToken(userDetails);

                LoginResponse response = new LoginResponse();
                response.setToken(token);
                response.setUser(mapper.map(userDetails, UserDto.class));
                return ResponseEntity.ok(response);

            }
            else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
            }


        }
    }

    private boolean isAdminCredentials(String username, String password) {
        // Replace with your actual admin credentials logic
        String adminUsername = "sanskarbhadani11@gmail.com";
        String adminPassword = "Sanskar12345@#$@"; // Replace with your actual admin password

        return adminUsername.equals(username) && adminPassword.equals(password);
    }

    private void authenticate(String username, String password) throws Exception {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
                password);

        try {
            authenticationManager.authenticate(authenticationToken);
        } catch (BadCredentialsException e) {
            throw new ApiException("Invalid username or password");
        }
    }


    // register new user api

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        String email=registerRequest.getEmail();
//        if (customUserDetailService.loadUserByUsername(email) != null) {
//            throw new UserAlreadyExists("User with email " + email + " already exists");
//        }

        UserDto registeredUser = this.userService.registerNewUser(registerRequest);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }
    @PostMapping("/sendOtp")
    public ResponseEntity<String> sendOtp(@RequestParam String email) {
       String email2 = email.trim();

        String otp = otpService.generateOtp(email2);
        emailService.sendSimpleMessage(email2,"Your OTP ","OTP is "+otp);

        return ResponseEntity.ok("OTP sent to " + email2+otp);
    }

    // get loggedin user data
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ModelMapper mapper;

    @GetMapping("/current-user/")
    public ResponseEntity<UserDto> getUser(Principal principal) {
        User user = this.userRepo.findByEmail(principal.getName()).get();
        return new ResponseEntity<UserDto>(this.mapper.map(user, UserDto.class), HttpStatus.OK);
    }
    private boolean isValidEmailFormat(String email) {
        // Implement your email validation logic here (e.g., regex check)
        // Example regex for basic validation (not comprehensive)
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(regex);
    }

}

