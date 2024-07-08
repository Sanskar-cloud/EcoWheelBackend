package com.example.demo.controllers;

import com.example.demo.entities.Cycle;
import com.example.demo.entities.Subscription;
import com.example.demo.entities.SubscriptionStatus;
import com.example.demo.entities.User;
import com.example.demo.payloads.PaymentLinkResponse;
import com.example.demo.payloads.PaymentRequest;
import com.example.demo.repository.CycleRepo;
import com.example.demo.repository.SubscriptionRepo;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class SubscriptionController {

    @Autowired
    SubscriptionService subscriptionService;
    @Autowired
    SubscriptionRepo subscriptionRepo;
    @Autowired
    CycleRepo cycleRepo;


    @PostMapping("/create")
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
            Cycle cycle = subscription.getAllocatedCycle();
            if (cycle != null) {
                cycle.setIsAvailable(String.valueOf(true));
                cycle.setUser(null); // Remove user association
                cycleRepo.save(cycle);
            }
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepo.save(subscription);
        }
    }

}
