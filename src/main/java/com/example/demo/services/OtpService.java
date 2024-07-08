package com.example.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class OtpService {

    private static final int OTP_LENGTH = 6;

    private Map<String, OtpInfo> otpInfoMap = new HashMap<>();

    @Autowired
    private EmailService emailService;

    public String generateOtp(String email) {
        String otp = generateRandomOtp();
        OtpInfo otpInfo = new OtpInfo(email, otp);
        otpInfoMap.put(email, otpInfo);
        System.out.println(otpInfoMap);

//
//        emailService.sendOtp(email, otp);

        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        OtpInfo otpInfo = otpInfoMap.get(email);
        if (otpInfo != null  && otpInfo.getOtp().equals(otp)) {

            otpInfoMap.remove(email);
            return true;
        }
        return false;
    }

    private String generateRandomOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }


    private static class OtpInfo {
        private String email;
        private String otp;


        public OtpInfo(String email, String otp) {
            this.email = email;
            this.otp = otp;

        }

        public String getEmail() {
            return email;
        }

        public String getOtp() {
            return otp;
        }


    }
}

