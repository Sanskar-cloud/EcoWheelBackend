package com.example.demo.services;

import com.example.demo.entities.Subscription;
import com.example.demo.entities.SubscriptionPlan;
import com.example.demo.entities.SubscriptionStatus;
import com.example.demo.entities.User;
import com.example.demo.payloads.PaymentLinkResponse;
import com.example.demo.payloads.PaymentRequest;
import com.example.demo.payloads.SubscriptionPlanDto;
import com.example.demo.repository.CycleRepo;
import com.example.demo.repository.SubscriptionRepo;
import com.example.demo.repository.UserRepo;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private SubscriptionRepo subscriptionRepository;

    @Autowired
    private CycleRepo cycleRepository;

    @Autowired
    private RazorpayClient razorpayClient;

    public PaymentLinkResponse createPaymentLink(PaymentRequest paymentRequest) throws RazorpayException {
        JSONObject paymentLinkRequest = new JSONObject();
        paymentLinkRequest.put("amount", paymentRequest.getSubscriptionDto().getPlan().getPrice() * 100); // Amount in paise
        paymentLinkRequest.put("currency", "INR");
        JSONObject customer = new JSONObject();
        customer.put("name", paymentRequest.getUserName());
        customer.put("email", paymentRequest.getUserEmail());
        paymentLinkRequest.put("customer", customer);

        JSONObject notify = new JSONObject();
        notify.put("email", true);
        paymentLinkRequest.put("notify", notify);

        JSONObject options = new JSONObject();
        JSONObject paymentMethod = new JSONObject();
        paymentMethod.put("upi", true);
        options.put("payment_method", paymentMethod);
        paymentLinkRequest.put("options", options);

        paymentLinkRequest.put("reminder_enable", true);
        paymentLinkRequest.put("callback_url", "https://your-ngrok-url/api/paymentCallbackStudent");
        paymentLinkRequest.put("callback_method", "get");




        PaymentLink paymentLink = razorpayClient.paymentLink.create(paymentLinkRequest);
        String paymentLinkId = paymentLink.get("id");
        String paymentLinkUrl = paymentLink.get("short_url");
        User user = userRepo.findById(Integer.valueOf(paymentRequest.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + paymentRequest.getUserId()));

        SubscriptionPlan plan = convertDtoToEntity(paymentRequest.getSubscriptionDto().getPlan());

        Subscription subscription=new Subscription();
        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setStartDate(LocalDate.now());
        subscription.setEndDate(LocalDate.now().plusMonths(paymentRequest.getSubscriptionDto().getPlan().getDuration()));
        subscription.setPaymentLinkId(paymentLinkId);
        subscription.setStatus(SubscriptionStatus.PENDING);

        subscriptionRepository.save(subscription);



//

        return new PaymentLinkResponse(paymentLinkUrl, paymentLinkId);
    }
    private SubscriptionPlan convertDtoToEntity(SubscriptionPlanDto planDto) {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setId(planDto.getId());
        plan.setName(planDto.getName());
        plan.setPrice(planDto.getPrice());

        // Set the duration based on the subscription name
        switch (planDto.getName().toLowerCase()) {
            case "6 months":
                plan.setDuration(6);
                break;
            case "12 months":
                plan.setDuration(12);
                break;
            // Add more cases if there are more subscription plans
            default:
                throw new IllegalArgumentException("Unknown subscription plan name: " + planDto.getName());
        }

        return plan;
    }
    public List<Subscription> getExpiredSubscriptions() {
        return subscriptionRepository.findAllExpiredSubscriptions(SubscriptionStatus.EXPIRED);
    }

    public List<Subscription> getSubscriptionsEndingInFourDays() {
        LocalDate fourDaysAhead = LocalDate.now().plusDays(4);
        return subscriptionRepository.findAllSubscriptionsEndingInFourDays(fourDaysAhead);
    }
    public boolean markCycleReturned(Long subscriptionId) {
        Optional<Subscription> subscriptionOptional = subscriptionRepository.findById(Math.toIntExact(subscriptionId));
        if (subscriptionOptional.isPresent()) {
            Subscription subscription = subscriptionOptional.get();
            subscription.setReturnStatus(String.valueOf(true)); // Assuming you have a field in Subscription entity to track this
            subscriptionRepository.save(subscription);
            return true;
        }
        return false;
    }



}
