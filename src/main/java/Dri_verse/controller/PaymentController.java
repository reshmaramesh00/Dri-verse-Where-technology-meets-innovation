package Dri_verse.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Dri_verse.model.Payment;
import Dri_verse.service.PaymentService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Learner submits a payment request
    @PostMapping("/submit")
    public ResponseEntity<?> submitPayment(
            @RequestBody Payment payment,
            HttpSession session) {

        Integer learnerId = getUserId(session);

        if (learnerId == null || !hasRole(session, "STUDENT")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in as a learner first.");
        }

        // Do not trust a learner ID sent in the request body.
        payment.setLearnerId(learnerId);

        try {
            paymentService.submitPayment(payment);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message",
                            "Payment submitted and is awaiting review."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Learner views their own payment history
    @GetMapping("/mine")
    public ResponseEntity<?> myPayments(HttpSession session) {
        Integer learnerId = getUserId(session);

        if (learnerId == null || !hasRole(session, "STUDENT")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please log in as a learner first.");
        }

        List<Payment> payments =
                paymentService.getLearnerPayments(learnerId);

        return ResponseEntity.ok(payments);
    }

    // Instructor views payments awaiting review
    @GetMapping("/pending")
    public ResponseEntity<?> pendingPayments(HttpSession session) {
        Integer instructorId = getUserId(session);

        if (instructorId == null || !hasRole(session, "INSTRUCTOR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Instructor access required.");
        }

        return ResponseEntity.ok(paymentService.getPendingPayments());
    }

    // Instructor confirms or rejects a payment
    @PostMapping("/{paymentId}/review")
    public ResponseEntity<?> reviewPayment(
            @PathVariable long paymentId,
            @RequestBody ReviewRequest request,
            HttpSession session) {

        Integer instructorId = getUserId(session);

        if (instructorId == null || !hasRole(session, "INSTRUCTOR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Instructor access required.");
        }

        if (request == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Review details are required."));
        }

        try {
            boolean updated = paymentService.reviewPayment(
                    paymentId,
                    instructorId,
                    request.getStatus(),
                    request.getRejectionReason()
            );

            if (!updated) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Payment not found or already reviewed.");
            }

            return ResponseEntity.ok(
                    Map.of("message", "Payment review saved.")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private Integer getUserId(HttpSession session) {
        Object userId = session.getAttribute("userId");
        return userId instanceof Integer ? (Integer) userId : null;
    }

    private boolean hasRole(HttpSession session, String expectedRole) {
        Object role = session.getAttribute("role");
        return role instanceof String
                && expectedRole.equalsIgnoreCase((String) role);
    }

    public static class ReviewRequest {
        private String status;
        private String rejectionReason;

        public ReviewRequest() {}

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getRejectionReason() {
            return rejectionReason;
        }

        public void setRejectionReason(String rejectionReason) {
            this.rejectionReason = rejectionReason;
        }
    }
}