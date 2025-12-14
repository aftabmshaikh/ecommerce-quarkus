package com.ecommerce.order.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    private String message;
    
    @Column(name = "status_date")
    private LocalDateTime statusDate;
    
    public static OrderStatusHistory create(OrderStatus status, String message) {
        return new OrderStatusHistory(null, null, status, message, LocalDateTime.now());
    }
}
