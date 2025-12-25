package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.exception.PaymentException;
import com.ecommerce.payment.mapper.PaymentMapper;
import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.model.PaymentStatus;
import com.ecommerce.payment.repository.PaymentRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {

    @InjectMock
    PaymentRepository paymentRepository;

    @InjectMock
    PaymentMapper paymentMapper;

    // Note: Emitter is not mocked - it uses the in-memory connector configured in application-test.properties
    // The Emitter will work with the in-memory connector, so we don't need to mock it

    @Inject
    PaymentService paymentService;

    private PaymentRequest paymentRequest;
    private Payment payment;
    private PaymentResponse paymentResponse;
    private UUID paymentId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        paymentId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(orderId);
        paymentRequest.setCustomerId(UUID.randomUUID());
        paymentRequest.setAmount(BigDecimal.valueOf(100.00));
        paymentRequest.setCurrency("USD");
        paymentRequest.setPaymentMethod("CARD");

        payment = new Payment();
        payment.setId(paymentId);
        payment.setOrderId(orderId);
        payment.setAmount(BigDecimal.valueOf(100.00));
        payment.setCurrency("USD");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        paymentResponse = new PaymentResponse();
        paymentResponse.setId(paymentId);
        paymentResponse.setOrderId(orderId);
        paymentResponse.setAmount(BigDecimal.valueOf(100.00));
        paymentResponse.setStatus(PaymentStatus.COMPLETED);
    }

    @Test
    @DisplayName("processPayment - should process payment successfully")
    void processPayment_Success_ReturnsResponse() {
        // Given
        when(paymentMapper.toEntity(paymentRequest)).thenReturn(payment);
        doNothing().when(paymentRepository).persist(any(Payment.class));
        when(paymentMapper.toResponse(any(Payment.class))).thenReturn(paymentResponse);

        // When
        PaymentResponse result = paymentService.processPayment(paymentRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo(orderId);
        verify(paymentRepository).persist(any(Payment.class));
    }

    @Test
    @DisplayName("getPayment - should return payment when found")
    void getPayment_PaymentExists_ReturnsPayment() {
        // Given
        when(paymentRepository.findByIdOptional(paymentId)).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        // When
        PaymentResponse result = paymentService.getPayment(paymentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(paymentId);
        verify(paymentRepository).findByIdOptional(paymentId);
    }

    @Test
    @DisplayName("getPayment - should throw exception when payment not found")
    void getPayment_PaymentNotFound_ThrowsException() {
        // Given
        when(paymentRepository.findByIdOptional(paymentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> paymentService.getPayment(paymentId))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("not found");

        verify(paymentRepository).findByIdOptional(paymentId);
    }

    @Test
    @DisplayName("capturePayment - should capture authorized payment")
    void capturePayment_AuthorizedPayment_CapturesPayment() {
        // Given
        payment.setStatus(PaymentStatus.AUTHORIZED);
        when(paymentRepository.findByIdOptional(paymentId)).thenReturn(Optional.of(payment));
        doNothing().when(paymentRepository).persist(payment);
        paymentResponse.setStatus(PaymentStatus.CAPTURED);
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        // When
        PaymentResponse result = paymentService.capturePayment(paymentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
        verify(paymentRepository).persist(payment);
    }

    @Test
    @DisplayName("capturePayment - should throw exception when payment not authorized")
    void capturePayment_NotAuthorized_ThrowsException() {
        // Given
        payment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findByIdOptional(paymentId)).thenReturn(Optional.of(payment));

        // When/Then
        assertThatThrownBy(() -> paymentService.capturePayment(paymentId))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("Only authorized payments");

        verify(paymentRepository, never()).persist(any(Payment.class));
    }

    @Test
    @DisplayName("refundPayment - should refund captured payment")
    void refundPayment_CapturedPayment_RefundsPayment() {
        // Given
        payment.setStatus(PaymentStatus.CAPTURED);
        BigDecimal refundAmount = BigDecimal.valueOf(50.00);
        when(paymentRepository.findByIdOptional(paymentId)).thenReturn(Optional.of(payment));
        doNothing().when(paymentRepository).persist(payment);
        paymentResponse.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        // When
        PaymentResponse result = paymentService.refundPayment(paymentId, refundAmount);

        // Then
        assertThat(result).isNotNull();
        verify(paymentRepository).persist(payment);
    }

    @Test
    @DisplayName("refundPayment - should throw exception when payment not captured")
    void refundPayment_NotCaptured_ThrowsException() {
        // Given
        payment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findByIdOptional(paymentId)).thenReturn(Optional.of(payment));

        // When/Then
        assertThatThrownBy(() -> paymentService.refundPayment(paymentId, BigDecimal.TEN))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("Only captured");

        verify(paymentRepository, never()).persist(any(Payment.class));
    }

    @Test
    @DisplayName("cancelPayment - should cancel pending payment")
    void cancelPayment_PendingPayment_CancelsPayment() {
        // Given
        payment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findByIdOptional(paymentId)).thenReturn(Optional.of(payment));
        doNothing().when(paymentRepository).persist(payment);
        paymentResponse.setStatus(PaymentStatus.CANCELED);
        when(paymentMapper.toResponse(payment)).thenReturn(paymentResponse);

        // When
        PaymentResponse result = paymentService.cancelPayment(paymentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.CANCELED);
        verify(paymentRepository).persist(payment);
    }

    @Test
    @DisplayName("cancelPayment - should throw exception when payment not pending")
    void cancelPayment_NotPending_ThrowsException() {
        // Given
        payment.setStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findByIdOptional(paymentId)).thenReturn(Optional.of(payment));

        // When/Then
        assertThatThrownBy(() -> paymentService.cancelPayment(paymentId))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("Only pending payments");

        verify(paymentRepository, never()).persist(any(Payment.class));
    }

    @Test
    @DisplayName("getAvailablePaymentMethods - should return payment methods")
    void getAvailablePaymentMethods_ReturnsMethods() {
        // When
        Map<String, Object> result = paymentService.getAvailablePaymentMethods();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).containsKey("paymentMethods");
        assertThat(result).containsKey("defaultCurrency");
    }

    @Test
    @DisplayName("handleStripeWebhook - should handle webhook")
    void handleStripeWebhook_ProcessesWebhook() {
        // Given
        String payload = "{\"type\":\"payment_intent.succeeded\"}";
        String sigHeader = "signature";

        // When
        paymentService.handleStripeWebhook(payload, sigHeader);

        // Then - should not throw exception
        // This is a no-op method, so we just verify it completes
    }
}

