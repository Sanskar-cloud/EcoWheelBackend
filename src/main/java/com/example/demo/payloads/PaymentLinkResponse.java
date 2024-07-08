package com.example.demo.payloads;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentLinkResponse {
    String paymentLinkUrl;
    String paymentLinkId;

    public PaymentLinkResponse(String paymentLinkUrl, String paymentLinkId) {
    }
}
