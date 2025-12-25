package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.exception.PaymentException;
import com.ecommerce.payment.mapper.PaymentMapper;
import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.model.PaymentStatus;
import com.ecommerce.payment.repository.PaymentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class PaymentService {

    private static final Logger LOG = Logger.getLogger(PaymentService.class);

    @Inject
    PaymentRepository paymentRepository;

    @Inject
    PaymentMapper paymentMapper;

    @Inject
    @Channel("payment-events")
    Emitter<Map<String, Object>> paymentEventEmitter;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        LOG.infof("Processing payment for order: %s", request.getOrderId());

        Payment payment = paymentMapper.toEntity(request);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        // Persist once; subsequent changes will be flushed automatically at transaction commit
        paymentRepository.persist(payment);

        try {
            // Simulate payment processing with a third-party gateway (e.g., Stripe)
            // In a real application, this would involve API calls to the payment provider
            Thread.sleep(2000); // Simulate network delay

            // Simulate a successful payment
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId(UUID.randomUUID().toString());
            payment.setUpdatedAt(LocalDateTime.now());

            publishPaymentEvent("payment-completed", payment);
            LOG.infof("Payment successful for order: %s", request.getOrderId());
            return paymentMapper.toResponse(payment);

        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());

            publishPaymentEvent("payment-failed", payment);
            LOG.errorf(e, "Payment failed for order: %s", request.getOrderId());
            throw new PaymentException("Payment processing failed", e);
        }
    }

    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException("Payment not found for order: " + orderId));
        return paymentMapper.toResponse(payment);
    }

    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findByIdOptional(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found with ID: " + paymentId));
        return paymentMapper.toResponse(payment);
    }

    @Transactional
    public PaymentResponse capturePayment(UUID paymentId) {
        Payment payment = paymentRepository.findByIdOptional(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found with ID: " + paymentId));
        
        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
            throw new PaymentException("Only authorized payments can be captured");
        }
        
        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.persist(payment);
        
        publishPaymentEvent("payment-captured", payment);
        return paymentMapper.toResponse(payment);
    }

    @Transactional
    public PaymentResponse refundPayment(UUID paymentId, java.math.BigDecimal amount) {
        Payment payment = paymentRepository.findByIdOptional(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found with ID: " + paymentId));
        
        if (payment.getStatus() != PaymentStatus.CAPTURED && payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new PaymentException("Only captured or completed payments can be refunded");
        }
        
        java.math.BigDecimal refundAmount = amount != null ? amount : payment.getAmount();
        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new PaymentException("Refund amount cannot exceed payment amount");
        }
        
        if (refundAmount.compareTo(payment.getAmount()) < 0) {
            payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
        } else {
            payment.setStatus(PaymentStatus.REFUNDED);
        }
        
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.persist(payment);
        
        publishPaymentEvent("payment-refunded", payment);
        return paymentMapper.toResponse(payment);
    }

    @Transactional
    public PaymentResponse cancelPayment(UUID paymentId) {
        Payment payment = paymentRepository.findByIdOptional(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found with ID: " + paymentId));
        
        LOG.infof("Cancelling payment with ID: %s", paymentId);
        
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new PaymentException("Only pending payments can be cancelled");
        }
        
        payment.setStatus(PaymentStatus.CANCELED);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.persist(payment);
        
        publishPaymentEvent("payment-cancelled", payment);
        return paymentMapper.toResponse(payment);
    }

    public Map<String, Object> getAvailablePaymentMethods() {
        Map<String, Object> methods = new HashMap<>();
        methods.put("paymentMethods", new String[]{"credit_card", "paypal", "bank_transfer"});
        methods.put("defaultCurrency", "USD");
        return methods;
    }

    public void handleStripeWebhook(String payload, String sigHeader) {
        // No-op in mock implementation
        LOG.infof("Received mock webhook: %s", payload);
    }

    private void publishPaymentEvent(String eventType, Payment payment) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("timestamp", LocalDateTime.now().toString());
            event.put("orderId", payment.getOrderId());
            event.put("paymentId", payment.getId());
            event.put("amount", payment.getAmount());
            event.put("currency", payment.getCurrency());
            event.put("status", payment.getStatus().name());

            paymentEventEmitter.send(event);
            LOG.debugf("Published %s event for order: %s", eventType, payment.getOrderId());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to publish payment event: %s", e.getMessage());
        }
    }
}
