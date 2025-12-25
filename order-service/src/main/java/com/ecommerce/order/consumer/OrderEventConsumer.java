package com.ecommerce.order.consumer;

import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class OrderEventConsumer {
    private static final Logger log = Logger.getLogger(OrderEventConsumer.class);

    @Inject
    OrderService orderService;

    @Inject
    ObjectMapper objectMapper;

    @Incoming("payment-events")
    public void handlePaymentEvent(String eventJson) {
        try {
            Map<String, Object> event = objectMapper.readValue(eventJson, Map.class);
            String eventType = (String) event.get("eventType");
            UUID orderId = UUID.fromString((String) event.get("orderId"));

            log.infof("Received payment event: %s for order: %s", eventType, orderId);

            switch (eventType) {
                case "payment-received":
                    orderService.updateOrderStatus(
                        orderId,
                        OrderStatus.PAID
                    );
                    break;
                case "payment-failed":
                    orderService.updateOrderStatus(
                        orderId,
                        OrderStatus.CANCELLED
                    );
                    break;
                case "payment-refunded":
                    orderService.updateOrderStatus(
                        orderId,
                        OrderStatus.REFUNDED
                    );
                    break;
                default:
                    log.warnf("Unknown payment event type: %s", eventType);
            }
        } catch (Exception e) {
            log.errorf(e, "Error processing payment event: %s", eventJson);
        }
    }

    @Incoming("inventory-events")
    public void handleInventoryEvent(String eventJson) {
        try {
            Map<String, Object> event = objectMapper.readValue(eventJson, Map.class);
            String eventType = (String) event.get("eventType");
            String orderId = (String) event.get("orderId");

            log.infof("Received inventory event: %s for order: %s", eventType, orderId);

            switch (eventType) {
                case "inventory-reserved":
                    orderService.updateOrderStatus(
                        UUID.fromString(orderId),
                        OrderStatus.PROCESSING
                    );
                    break;
                case "inventory-updated":
                    // Handle inventory update (e.g., update order items if needed)
                    break;
                case "inventory-out-of-stock":
                    orderService.updateOrderStatus(
                        UUID.fromString(orderId),
                        OrderStatus.CANCELLED
                    );
                    break;
                default:
                    log.warnf("Unknown inventory event type: %s", eventType);
            }
        } catch (Exception e) {
            log.errorf(e, "Error processing inventory event: %s", eventJson);
        }
    }
}
