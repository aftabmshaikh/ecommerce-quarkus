package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.mapper.ProductMapper;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.TransactionSynchronizationRegistry;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductService {

    private static final Logger log = Logger.getLogger(ProductService.class);

    @Inject
    ProductRepository productRepository;

    @Inject
    ProductMapper productMapper;

    @Inject
    @Channel("product-events")
    Emitter<Product> productEventEmitter;

    @Inject
    @Channel("inventory-updates")
    Emitter<StockUpdateEvent> inventoryUpdateEmitter;

    @Inject
    TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    @Transactional
    @CacheInvalidateAll(cacheName = "products")
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("Product with SKU " + request.getSku() + " already exists");
        }

        Product product = productMapper.toEntity(request);
        productRepository.persist(product);
        productRepository.flush(); // Ensure the product is persisted before sending event
        
        // Send event after transaction commits to avoid transaction issues
        Product productForEvent = product; // Capture for use in callback
        transactionSynchronizationRegistry.registerInterposedSynchronization(
            new jakarta.transaction.Synchronization() {
                @Override
                public void beforeCompletion() {
                    // Nothing to do before completion
                }

                @Override
                public void afterCompletion(int status) {
                    if (status == jakarta.transaction.Status.STATUS_COMMITTED) {
                        try {
                            productEventEmitter.send(productForEvent);
                        } catch (Exception e) {
                            log.warnf("Failed to send product event: %s", e.getMessage());
                        }
                    }
                }
            }
        );
        
        log.infof("Created product with id: %s", product.getId());
        ProductResponse response = productMapper.toDto(product);
        if (response == null) {
            throw new IllegalStateException("Failed to map product to response");
        }
        return response;
    }

    @CacheResult(cacheName = "products")
    public ProductResponse getProductById(UUID id) {
        log.infof("Fetching product with id: %s", id);
        Product product = findProductOrThrow(id);
        return productMapper.toDto(product);
    }

    @CacheResult(cacheName = "products")
    public List<ProductResponse> getAllProducts(int pageIndex, int pageSize) {
        log.info("Fetching all products");
        return productRepository.findAll().page(Page.of(pageIndex, pageSize)).list().stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "products")
    public void updateProductRating(UUID productId) {
        productRepository.findByIdOptional(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        log.infof("Updating rating for product with id: %s", productId);
    }


    public List<ProductResponse> searchProducts(String query) {
        return productRepository.searchProducts(query).stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "products")
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product existingProduct = findProductOrThrow(id);
        
        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setStockQuantity(request.getStockQuantity());
        existingProduct.setCategoryId(request.getCategoryId());
        existingProduct.setImageUrl(request.getImageUrl());
        if (request.getActive() != null) {
            existingProduct.setActive(request.getActive());
        }
        
        productRepository.persist(existingProduct);
        
        // Send event after transaction commits
        Product productForEvent = existingProduct;
        transactionSynchronizationRegistry.registerInterposedSynchronization(
            new jakarta.transaction.Synchronization() {
                @Override
                public void beforeCompletion() {
                    // Nothing to do before completion
                }

                @Override
                public void afterCompletion(int status) {
                    if (status == jakarta.transaction.Status.STATUS_COMMITTED) {
                        try {
                            productEventEmitter.send(productForEvent);
                        } catch (Exception e) {
                            log.warnf("Failed to send product event: %s", e.getMessage());
                        }
                    }
                }
            }
        );
        
        log.infof("Updated product with id: %s", id);
        return productMapper.toDto(existingProduct);
    }

    @Transactional
    @CacheInvalidateAll(cacheName = "products")
    public void deleteProduct(UUID id) {
        Product product = findProductOrThrow(id);
        productRepository.delete(product);
        
        // Send event after transaction commits
        Product productForEvent = product;
        transactionSynchronizationRegistry.registerInterposedSynchronization(
            new jakarta.transaction.Synchronization() {
                @Override
                public void beforeCompletion() {
                    // Nothing to do before completion
                }

                @Override
                public void afterCompletion(int status) {
                    if (status == jakarta.transaction.Status.STATUS_COMMITTED) {
                        try {
                            productEventEmitter.send(productForEvent);
                        } catch (Exception e) {
                            log.warnf("Failed to send product event: %s", e.getMessage());
                        }
                    }
                }
            }
        );
        
        log.infof("Deleted product with id: %s", id);
    }

    @Transactional
    @Retry(maxRetries = 3, delay = 100)
    @CacheInvalidateAll(cacheName = "products")
    public ProductResponse updateStock(UUID productId, int quantity) {
        Product product = findProductOrThrow(productId);
        int newStock = product.getStockQuantity() + quantity;
        
        if (newStock < 0) {
            throw new IllegalStateException("Insufficient stock for product: " + productId);
        }
        
        product.setStockQuantity(newStock);
        productRepository.persist(product);
        
        // Send event after transaction commits
        StockUpdateEvent event = new StockUpdateEvent(productId, quantity, newStock);
        transactionSynchronizationRegistry.registerInterposedSynchronization(
            new jakarta.transaction.Synchronization() {
                @Override
                public void beforeCompletion() {
                    // Nothing to do before completion
                }

                @Override
                public void afterCompletion(int status) {
                    if (status == jakarta.transaction.Status.STATUS_COMMITTED) {
                        try {
                            inventoryUpdateEmitter.send(event);
                        } catch (Exception e) {
                            log.warnf("Failed to send inventory update event: %s", e.getMessage());
                        }
                    }
                }
            }
        );
        
        log.infof("Updated stock for product: %s. New quantity: %s", productId, newStock);
        return productMapper.toDto(product);
    }

    public List<ProductResponse> getProductsByIds(List<UUID> productIds) {
        return productRepository.find("id in ?1", productIds).list().stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    public boolean isInStock(UUID productId, int quantity) {
        return productRepository.findByIdOptional(productId)
                .map(p -> p.getStockQuantity() >= quantity)
                .orElse(false);
    }

    private Product findProductOrThrow(UUID id) {
        return productRepository.findByIdOptional(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public record StockUpdateEvent(UUID productId, int quantityChange, int newStock) {}
}
