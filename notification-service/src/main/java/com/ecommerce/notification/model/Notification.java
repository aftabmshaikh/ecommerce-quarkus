package com.ecommerce.notification.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String recipientEmail;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    @Column(nullable = false)
    private boolean sent;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime sentAt;
    
    public enum NotificationType {
        ORDER_CONFIRMATION,
        ORDER_SHIPPED,
        ORDER_DELIVERED,
        PAYMENT_CONFIRMATION,
        ACCOUNT_CREATED,
        PASSWORD_RESET,
        PROMOTIONAL
    }
}
