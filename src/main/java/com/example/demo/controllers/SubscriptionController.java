package com.example.demo.controllers;

import com.example.demo.entities.Cycle;
import com.example.demo.entities.Subscription;
import com.example.demo.entities.SubscriptionStatus;
import com.example.demo.entities.User;
import com.example.demo.payloads.EmailRequest;
import com.example.demo.payloads.PaymentLinkResponse;
import com.example.demo.payloads.PaymentRequest;
import com.example.demo.payloads.SubscriptionUserResponse;
import com.example.demo.repository.CycleRepo;
import com.example.demo.repository.SubscriptionRepo;
import com.example.demo.services.EmailService;
import com.example.demo.services.SubscriptionService;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/subscription")
public class SubscriptionController {

    @Autowired
    SubscriptionService subscriptionService;
    @Autowired
    SubscriptionRepo subscriptionRepo;
    @Autowired
    CycleRepo cycleRepo;
    @Autowired
    EmailService emailService;


    @PostMapping("/payment/create")
    public ResponseEntity<PaymentLinkResponse> createPayment(@RequestBody PaymentRequest paymentRequest) {
        try {
            PaymentLinkResponse paymentLinkResponse = subscriptionService.createPaymentLink(paymentRequest);
            return ResponseEntity.ok(paymentLinkResponse);
        } catch (RazorpayException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new PaymentLinkResponse("Error creating payment link: " + e.getMessage(), null));
        }
    }
    @GetMapping("/paymentCallback")
    public ResponseEntity<?> handlePaymentCallback(@RequestBody String payload) {

        JSONObject jsonObject = new JSONObject(payload);
        String paymentStatus = jsonObject.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity").getString("status");
        String paymentLinkId = jsonObject.getJSONObject("payload").getJSONObject("payment_link").getJSONObject("entity").getString("id");

        Subscription subscription = subscriptionRepo.findByPaymentLinkId(paymentLinkId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid payment link ID"));

        if ("captured".equals(paymentStatus)) {

            Cycle cycle = allocateCycleToUser(subscription.getUser());
            subscription.setAllocatedCycle(cycle);
            subscription.setStatus(SubscriptionStatus.ACTIVE);

            subscriptionRepo.save(subscription);

            return ResponseEntity.ok("Payment successful and cycle allocated");
        } else {

            return ResponseEntity.badRequest().body("Payment failed");
        }
    }

    private Cycle allocateCycleToUser(User user) {

        List<Cycle> availableCycles = cycleRepo.findByIsAvailableTrueOrderByCycleNumberAsc();
        if (availableCycles.isEmpty()) {
            throw new IllegalStateException("No cycles available");
        }
        Cycle cycle = availableCycles.get(0);
        cycle.setIsAvailable(String.valueOf(false));
        cycle.setUser(user);

        cycleRepo.save(cycle);

        return cycle;

    }
    @Component
    public class SubscriptionExpirationTask {

        @Scheduled(cron = "0 0 0 * * *") // Runs daily at midnight
        public void checkSubscriptionExpiration() {
            List<Subscription> subscriptions = subscriptionRepo.findAllByEndDateBeforeAndStatus(LocalDate.now(), SubscriptionStatus.ACTIVE);
            for (Subscription subscription : subscriptions) {
                deallocateCycle(subscription);
            }
        }

        private void deallocateCycle(Subscription subscription) {
            String userEmail = subscription.getUser().getEmail();
            String subject = "Subscription Expiration Notice";
            String message = "Your subscription has expired. Please renew to continue using our service.";

            // Send email notification
            emailService.sendSimpleMessage(userEmail, subject, message);

//            Cycle cycle = subscription.getAllocatedCycle();
//            if (cycle != null) {
//                cycle.setIsAvailable(String.valueOf(true));
//                cycle.setUser(null); // Remove user association
//                cycleRepo.save(cycle);
//            }
//            subscription.setStatus(SubscriptionStatus.EXPIRED);
//            subscriptionRepo.save(subscription);
        }
    }
    @Component
    public class SubscriptionExpirationCycleDeallocate {

        @Scheduled(cron = "0 0 0 * * *") // Runs daily at midnight
        public void checkSubscriptionExpiration() {
            List<Subscription> subscriptions = subscriptionRepo.findAllByEndDateBeforeAndStatus(LocalDate.now().minusDays(2), SubscriptionStatus.ACTIVE);
            for (Subscription subscription : subscriptions) {
                deallocateCycle(subscription);
            }
        }

        private void deallocateCycle(Subscription subscription) {
            String userEmail = subscription.getUser().getEmail();
            String subject = "Reminder of Subscription Expiration Notice";
            String message = "Your subscription has expired 2 days ago. Please contact ECO CLUB to renew and continue using our services.";
            if(Objects.equals(subscription.getReturnStatus(), "false")){
                emailService.sendSimpleMessage(userEmail, subject, message);


            }




            Cycle cycle = subscription.getAllocatedCycle();
            if (cycle != null) {
                cycle.setIsAvailable(String.valueOf(true));
                cycle.setUser(null);
                cycleRepo.save(cycle);
            }
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepo.save(subscription);
        }
    }
    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest emailRequest) {
        try {
            emailService.sendSimpleMessage(emailRequest.getTo(),emailRequest.getSubject(),emailRequest.getBody());
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending email: " + e.getMessage());
        }
    }
    @GetMapping("/expired")
    public ResponseEntity<List<SubscriptionUserResponse>> getExpiredSubscriptions() {
        List<Subscription> expiredSubscriptions = subscriptionService.getExpiredSubscriptions();
        List<SubscriptionUserResponse> response = expiredSubscriptions.stream()
                .map(this::convertToSubscriptionUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ending-soon")
    public ResponseEntity<List<SubscriptionUserResponse>> getSubscriptionsEndingInFourDays() {
        List<Subscription> endingSoonSubscriptions = subscriptionService.getSubscriptionsEndingInFourDays();
        List<SubscriptionUserResponse> response = endingSoonSubscriptions.stream()
                .map(this::convertToSubscriptionUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private SubscriptionUserResponse convertToSubscriptionUserResponse(Subscription subscription) {
        SubscriptionUserResponse response = new SubscriptionUserResponse();
        response.setSubscriptionId((long) subscription.getId());
        response.setUserName(subscription.getUser().getName());
        response.setUserEmail(subscription.getUser().getEmail());
        response.setPhoneNumber(subscription.getUser().getPhoneNumber());
        response.setStartDate(subscription.getStartDate());
        response.setEndDate(subscription.getEndDate());
        response.setSubscriptionPlanName(subscription.getPlan().getName());
        response.setSubscriptionStatus(subscription.getStatus().toString());
        return response;
    }
    @PutMapping("/markReturned/{subscriptionId}")
    public ResponseEntity<String> markCycleReturned(@PathVariable Long subscriptionId) {
        try {
            boolean markedReturned = subscriptionService.markCycleReturned(subscriptionId);
            if (markedReturned) {
                return ResponseEntity.ok("Cycle marked as returned successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Subscription not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error marking cycle as returned: " + e.getMessage());
        }
    }



}
