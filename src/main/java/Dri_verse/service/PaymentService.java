package Dri_verse.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Dri_verse.model.Payment;
import Dri_verse.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // Submit a payment request
    public int submitPayment(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment details are required.");
        }

        if (payment.getLearnerId() <= 0) {
            throw new IllegalArgumentException("Invalid learner ID.");
        }

        if (payment.getDescription() == null
                || payment.getDescription().isBlank()) {
            throw new IllegalArgumentException("Payment description is required.");
        }

        BigDecimal amount = payment.getAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        // The repository saves every new request as PENDING.
        return paymentRepository.savePayment(payment);
    }

    // Get payments for one learner
    public List<Payment> getLearnerPayments(int learnerId) {
        if (learnerId <= 0) {
            throw new IllegalArgumentException("Invalid learner ID.");
        }

        return paymentRepository.findByLearnerId(learnerId);
    }

    // Get payments awaiting review
    public List<Payment> getPendingPayments() {
        return paymentRepository.findPendingPayments();
    }

    // Instructor reviews a payment
    @Transactional
    public boolean reviewPayment(
            long paymentId,
            int instructorId,
            String status,
            String rejectionReason) {

        if (paymentId <= 0 || instructorId <= 0) {
            throw new IllegalArgumentException(
                "Valid payment and instructor IDs are required."
            );
        }

        if (status == null) {
            throw new IllegalArgumentException("Review status is required.");
        }

        status = status.trim().toUpperCase();

        if (!status.equals("CONFIRMED") && !status.equals("REJECTED")) {
            throw new IllegalArgumentException(
                "Status must be CONFIRMED or REJECTED."
            );
        }

        if (status.equals("REJECTED")
                && (rejectionReason == null || rejectionReason.isBlank())) {
            throw new IllegalArgumentException(
                "Please provide a reason for rejection."
            );
        }

        int updatedRows = paymentRepository.reviewPayment(
            paymentId,
            instructorId,
            status,
            status.equals("REJECTED") ? rejectionReason.trim() : null
        );

        return updatedRows > 0;
    }
}