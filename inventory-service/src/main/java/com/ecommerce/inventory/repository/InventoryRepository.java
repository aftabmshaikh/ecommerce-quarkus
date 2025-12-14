package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.dto.InventoryStatus;
import com.ecommerce.inventory.dto.StockLevel;
import com.ecommerce.inventory.model.InventoryItem;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class InventoryRepository implements PanacheRepository<InventoryItem> {

    public Optional<InventoryItem> findBySkuCode(String skuCode) {
        return find("skuCode", skuCode).firstResultOptional();
    }

    public Optional<InventoryItem> findBySkuCodeForUpdate(String skuCode) {
        return find("skuCode", skuCode).withLock(LockModeType.PESSIMISTIC_WRITE).firstResultOptional();
    }

    public List<InventoryItem> findByProductIdIn(List<UUID> productIds) {
        return list("productId IN ?1", productIds);
    }

    public List<InventoryItem> findLowStockItems() {
        return list("availableQuantity <= lowStockThreshold AND isActive = true");
    }

    public List<InventoryItem> findItemsNeedingRestock() {
        return list("availableQuantity <= restockThreshold AND isActive = true");
    }

    public List<InventoryItem> findItemsDueForRestock() {
        return list("nextRestockDate IS NOT NULL AND nextRestockDate <= ?1", LocalDateTime.now());
    }

    public int adjustInventory(String skuCode, int adjustment) {
        return update("quantity = quantity + ?1, availableQuantity = (quantity + ?1) - reservedQuantity WHERE skuCode = ?2 AND (quantity + ?1) >= 0", adjustment, skuCode);
    }

    public int reserveStock(String skuCode, int quantity) {
        return update("reservedQuantity = reservedQuantity + ?1, availableQuantity = quantity - (reservedQuantity + ?1) WHERE skuCode = ?2 AND (quantity - (reservedQuantity + ?1)) >= 0", quantity, skuCode);
    }

    public int releaseStock(String skuCode, int quantity) {
        return update("reservedQuantity = reservedQuantity - ?1, availableQuantity = availableQuantity + ?1 WHERE skuCode = ?2 AND reservedQuantity >= ?1", quantity, skuCode);
    }

    public int consumeReservedStock(String skuCode, int quantity) {
        return update("quantity = quantity - ?1, reservedQuantity = reservedQuantity - ?1 WHERE skuCode = ?2 AND quantity >= ?1 AND reservedQuantity >= ?1", quantity, skuCode);
    }

    public Optional<InventoryStatus> getInventoryStatus(String skuCode) {
        // Panache doesn't directly support constructor expressions with complex logic like Spring Data JPA.
        // We'll fetch the entity and map it manually.
        return findBySkuCode(skuCode).map(item -> {
            String status;
            if (item.getAvailableQuantity() <= 0) {
                status = "OUT_OF_STOCK";
            } else if (item.getAvailableQuantity() <= item.getLowStockThreshold()) {
                status = "LOW_STOCK";
            } else {
                status = "IN_STOCK";
            }
            return InventoryStatus.builder()
                    .skuCode(item.getSkuCode())
                    .inStock(item.getAvailableQuantity() > 0)
                    .availableQuantity(item.getAvailableQuantity())
                    .lowStock(item.getAvailableQuantity() <= item.getLowStockThreshold())
                    .status(status)
                    .build();
        });
    }

    public Optional<StockLevel> getStockLevel(String skuCode) {
        // Panache doesn't directly support constructor expressions with complex logic like Spring Data JPA.
        // We'll fetch the entity and map it manually.
        return findBySkuCode(skuCode).map(item -> {
            String status;
            if (item.getAvailableQuantity() <= 0) {
                status = "OUT_OF_STOCK";
            } else if (item.getAvailableQuantity() <= item.getLowStockThreshold()) {
                status = "LOW_STOCK";
            } else if (item.getAvailableQuantity() <= item.getRestockThreshold()) {
                status = "NEEDS_RESTOCK";
            } else {
                status = "IN_STOCK";
            }
            return StockLevel.builder()
                    .skuCode(item.getSkuCode())
                    .currentLevel(item.getAvailableQuantity())
                    .lowStockThreshold(item.getLowStockThreshold())
                    .restockThreshold(item.getRestockThreshold())
                    .status(status)
                    .build();
        });
    }

    public boolean isInStock(String skuCode, int quantity) {
        return count("skuCode = ?1 AND availableQuantity >= ?2", skuCode, quantity) > 0;
    }

    public boolean existsBySkuCode(String skuCode) {
        return count("skuCode", skuCode) > 0;
    }
}
