package com.example.user.resource;

import com.example.user.constant.TransactionType;
import com.example.user.dto.P2pPaymentRequest;
import com.example.user.dto.ServicePaymentRequest;
import com.example.user.entity.Transaction;
import com.example.user.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class PaymentResource {

    private final PaymentService paymentService;

    @PostMapping("/{userId}/payment/send")
    public ResponseEntity<?> sendPaymentToUser(@PathVariable("userId") Integer userId,
                                         @AuthenticationPrincipal UserDetails userDetails,
                                         @RequestBody P2pPaymentRequest request){
        log.info("Sending payment to user {}", userId);
        paymentService.sendPayment(userId, userDetails, request);
        log.info("Successfully sent payment to user {}", request.receiverId());

        return ResponseEntity.ok().body("Funds are transferred successfully!");
    }

    @PostMapping("/{userId}/payment/service")
    public ResponseEntity<?> sendPaymentToService(@PathVariable("userId") Integer userId,
                                                  @AuthenticationPrincipal UserDetails userDetails,
                                                  @RequestBody ServicePaymentRequest request){
        log.info("Sending payment to service {}", request.serviceId());
        paymentService.sendPayment(userId, userDetails, request);

        return null;
    }


}
