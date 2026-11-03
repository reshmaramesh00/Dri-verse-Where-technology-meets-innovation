package Dri_verse.repository;

import Dri_verse.model.Payment;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Payment> paymentRowMapper = new RowMapper<Payment>() {
        @Override
        public Payment mapRow(ResultSet rs, int rowNum) throws SQLException {
            Payment payment = new Payment();

            payment.setPaymentId(rs.getLong("payment_id"));
            payment.setLearnerId(rs.getInt("learner_id"));
            payment.setDescription(rs.getString("description"));
            payment.setAmount(rs.getBigDecimal("amount"));
            payment.setPaymentStatus(rs.getString("payment_status"));
            payment.setPaymentMethod(rs.getString("payment_method"));
            payment.setTransactionReference(
                rs.getString("transaction_reference")
            );
            payment.setCreatedAt(rs.getTimestamp("created_at"));
            payment.setPaidAt(rs.getTimestamp("paid_at"));

            payment.setReviewedBy(
                (Integer) rs.getObject("reviewed_by")
            );
            payment.setReviewedAt(rs.getTimestamp("reviewed_at"));
            payment.setRejectionReason(rs.getString("rejection_reason"));

            return payment;
        }
    };

    // Save a learner's payment request
    public int savePayment(Payment payment) {
        String sql = """
            INSERT INTO payments
            (learner_id, description, amount, payment_status,
             payment_method, transaction_reference)
            VALUES (?, ?, ?, 'PENDING', ?, ?)
            """;

        return jdbcTemplate.update(
            sql,
            payment.getLearnerId(),
            payment.getDescription(),
            payment.getAmount(),
            payment.getPaymentMethod(),
            payment.getTransactionReference()
        );
    }

    // View all payments submitted by a learner
    public List<Payment> findByLearnerId(int learnerId) {
        String sql =
            "SELECT * FROM payments WHERE learner_id = ? ORDER BY created_at DESC";

        return jdbcTemplate.query(sql, paymentRowMapper, learnerId);
    }

    // View all payments awaiting instructor review
    public List<Payment> findPendingPayments() {
        String sql =
            "SELECT * FROM payments WHERE payment_status = 'PENDING' ORDER BY created_at DESC";

        return jdbcTemplate.query(sql, paymentRowMapper);
    }

    // Confirm or reject a payment that is still pending
    public int reviewPayment(
        long paymentId,
        int instructorId,
        String status,
        String rejectionReason
    ) {
        String sql = """
            UPDATE payments
            SET payment_status = ?,
                reviewed_by = ?,
                reviewed_at = CURRENT_TIMESTAMP,
                paid_at = CASE
                    WHEN ? = 'CONFIRMED' THEN CURRENT_TIMESTAMP
                    ELSE NULL
                END,
                rejection_reason = ?
            WHERE payment_id = ?
              AND payment_status = 'PENDING'
            """;

        return jdbcTemplate.update(
            sql,
            status,
            instructorId,
            status,
            rejectionReason,
            paymentId
        );
    }
}