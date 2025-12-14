package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.InventoryRequest;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.dto.InventoryStatus;
import com.ecommerce.inventory.dto.ReleaseRequest;
import com.ecommerce.inventory.dto.ReservationRequest;
import com.ecommerce.inventory.dto.StockAdjustment;
import com.ecommerce.inventory.dto.StockLevel;
import com.ecommerce.inventory.exception.InsufficientStockException;
import com.ecommerce.inventory.exception.InventoryItemNotFoundException;
import com.ecommerce.inventory.exception.InvalidInventoryOperationException;
import com.ecommerce.inventory.model.InventoryItem;
import com.ecommerce.inventory.repository.InventoryRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class InventoryService {

    private static final Logger log = Logger.getLogger(InventoryService.class);

    @Inject
    InventoryRepository inventoryRepository;

    @Inject
    @Channel("inventory-events")
    Emitter<Map<String, Object>> inventoryEventEmitter;

    @Transactional
    public InventoryResponse createInventoryItem(InventoryRequest request) {
        log.infof("Creating inventory item for product: %s", request.getProductId());
        
        if (inventoryRepository.existsBySkuCode(request.getSkuCode())) {
            throw new InvalidInventoryOperationException("Inventory item already exists for SKU: " + request.getSkuCode());
        }

        InventoryItem item = InventoryItem.builder()
                .productId(request.getProductId())
                .skuCode(request.getSkuCode())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 0)
                .reservedQuantity(request.getReservedQuantity() != null ? request.getReservedQuantity() : 0)
                .lowStockThreshold(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 10)
                .restockThreshold(request.getRestockThreshold() != null ? request.getRestockThreshold() : 20)
                .unitCost(request.getUnitCost())
                .locationCode(request.getLocationCode())
                .binLocation(request.getBinLocation())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        item.calculateAvailableQuantity();
        inventoryRepository.persist(item);
        
        publishInventoryEvent("inventory-created", item);
        
        return InventoryResponse.fromEntity(item);
    }

    public InventoryResponse getInventoryBySkuCode(String skuCode) {
        log.debugf("Fetching inventory for SKU: %s", skuCode);
        InventoryItem item = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new InventoryItemNotFoundException("Inventory not found for SKU: " + skuCode));
        return InventoryResponse.fromEntity(item);
    }

    @Transactional
    @Retry(maxRetries = 3, delay = 100)
    @Fallback(fallbackMethod = "adjustStockFallback")
    public InventoryResponse adjustStock(StockAdjustment adjustment) {
        log.infof("Adjusting stock for SKU: %s by %d", adjustment.getSkuCode(), adjustment.getAdjustment());
        
        InventoryItem item = inventoryRepository.findBySkuCodeForUpdate(adjustment.getSkuCode())
                .orElseThrow(() -> new InventoryItemNotFoundException("Inventory not found for SKU: " + adjustment.getSkuCode()));
        
        item.adjustInventory(adjustment.getAdjustment());
        
        // Panache automatically persists changes to managed entities within a transaction
        
        publishInventoryEvent("stock-adjusted", item, Map.of(
                "adjustment", adjustment.getAdjustment(),
                "reason", adjustment.getReason(),
                "referenceId", adjustment.getReferenceId()
        ));
        
        return InventoryResponse.fromEntity(item);
    }

    @Transactional
    @CircuitBreaker(requestVolumeThreshold = 10, failureRatio = 0.5, delay = 5000)
    @Fallback(fallbackMethod = "reserveStockFallback")
    public InventoryResponse reserveStock(ReservationRequest request) {
        log.infof("Reserving %d units of SKU: %s for reservation ID: %s", 
                request.getQuantity(), request.getSkuCode(), request.getReservationId());
        
        InventoryItem item = inventoryRepository.findBySkuCodeForUpdate(request.getSkuCode())
                .orElseThrow(() -> new InventoryItemNotFoundException("Inventory not found for SKU: " + request.getSkuCode()));
        
        item.reserve(request.getQuantity());
        
        publishInventoryEvent("stock-reserved", item, Map.of(
                "reservationId", request.getReservationId(),
                "quantityReserved", request.getQuantity(),
                "notes", request.getNotes()
        ));
        
        return InventoryResponse.fromEntity(item);
    }

    @Transactional
    public InventoryResponse releaseStock(ReleaseRequest request) {
        log.infof("Releasing %d units of SKU: %s for reservation ID: %s", 
                request.getQuantity(), request.getSkuCode(), request.getReservationId());
        
        InventoryItem item = inventoryRepository.findBySkuCodeForUpdate(request.getSkuCode())
                .orElseThrow(() -> new InventoryItemNotFoundException("Inventory not found for SKU: " + request.getSkuCode()));
        
        item.release(request.getQuantity());
        
        publishInventoryEvent("stock-released", item, Map.of(
                "reservationId", request.getReservationId(),
                "quantityReleased", request.getQuantity(),
                "reason", request.getReason()
        ));
        
        return InventoryResponse.fromEntity(item);
    }

    @Transactional
    public InventoryResponse consumeReservedStock(String skuCode, int quantity, String reservationId) {
        log.infof("Consuming %d reserved units of SKU: %s for reservation ID: %s", 
                quantity, skuCode, reservationId);
        
        InventoryItem item = inventoryRepository.findBySkuCodeForUpdate(skuCode)
                .orElseThrow(() -> new InventoryItemNotFoundException("Inventory not found for SKU: " + skuCode));
        
        // Assuming consumeReservedStock means reducing both quantity and reservedQuantity
        // This logic needs to be carefully implemented in InventoryItem or here.
        // For now, let's adjust quantity and then release the reserved quantity.
        item.adjustInventory(-quantity); // Reduce total quantity
        item.release(quantity); // Release the reserved quantity that was consumed
        
        publishInventoryEvent("reserved-stock-consumed", item, Map.of(
                "reservationId", reservationId,
                "quantityConsumed", quantity
        ));
        
        return InventoryResponse.fromEntity(item);
    }

    public InventoryStatus checkInventoryStatus(String skuCode) {
        return inventoryRepository.getInventoryStatus(skuCode)
                .orElseThrow(() -> new InventoryItemNotFoundException("Inventory not found for SKU: " + skuCode));
    }

    public List<StockLevel> getLowStockItems() {
        return inventoryRepository.findLowStockItems().stream()
                .map(item -> StockLevel.builder()
                        .skuCode(item.getSkuCode())
                        .currentLevel(item.getAvailableQuantity())
                        .lowStockThreshold(item.getLowStockThreshold())
                        .restockThreshold(item.getRestockThreshold())
                        .status(item.isLowStock() ? "LOW_STOCK" : "IN_STOCK") // Simplified status for this method
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void processRestock(String skuCode, int quantity) {
        log.infof("Processing restock of %d units for SKU: %s", quantity, skuCode);
        
        InventoryItem item = inventoryRepository.findBySkuCodeForUpdate(skuCode)
                .orElseThrow(() -> new InventoryItemNotFoundException("Inventory not found for SKU: " + skuCode));
        
        item.adjustInventory(quantity);
        item.setLastRestockedDate(LocalDateTime.now());
        item.setNextRestockDate(LocalDateTime.now().plusWeeks(2));
        
        publishInventoryEvent("inventory-restocked", item, Map.of(
                "quantityAdded", quantity,
                "newQuantity", item.getQuantity()
        ));
    }

    // Fallback methods
    public InventoryResponse adjustStockFallback(StockAdjustment adjustment, Throwable t) {
        log.errorf(t, "Fallback: Failed to adjust stock for SKU: %s. Error: %s", adjustment.getSkuCode(), t.getMessage());
        throw new InvalidInventoryOperationException("Failed to adjust stock. Please try again later.");
    }
    
    public InventoryResponse reserveStockFallback(ReservationRequest request, Throwable t) {
        log.errorf(t, "Fallback: Failed to reserve stock for SKU: %s. Error: %s", request.getSkuCode(), t.getMessage());
        throw new InsufficientStockException("Failed to reserve stock. Please try again later.");
    }

    // Helper methods
    private void publishInventoryEvent(String eventType, InventoryItem item) {
        publishInventoryEvent(eventType, item, null);
    }
    
    private void publishInventoryEvent(String eventType, InventoryItem item, Map<String, Object> additionalData) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("timestamp", LocalDateTime.now().toString());
            event.put("inventoryId", item.getId());
            event.put("productId", item.getProductId());
            event.put("skuCode", item.getSkuCode());
            event.put("quantity", item.getQuantity());
            event.put("availableQuantity", item.getAvailableQuantity());
            event.put("reservedQuantity", item.getReservedQuantity());
            
            if (additionalData != null) {
                event.putAll(additionalData);
            }
            
            inventoryEventEmitter.send(event);
            log.debugf("Published %s event for SKU: %s", eventType, item.getSkuCode());
        } catch (Exception e) {
            log.errorf(e, "Failed to publish inventory event: %s", e.getMessage());
        }
    }
}
