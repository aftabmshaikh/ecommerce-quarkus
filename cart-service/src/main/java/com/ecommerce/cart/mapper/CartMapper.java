package com.ecommerce.cart.mapper;

import com.ecommerce.cart.dto.CartItemDto;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Mapper(componentModel = "cdi")
public interface CartMapper {

    @Mapping(target = "totalItems", expression = "java(calculateTotalItems(cart))")
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(cart))")
    @Mapping(target = "tax", expression = "java(calculateTax(cart))")
    @Mapping(target = "shippingFee", expression = "java(calculateShippingFee(cart))")
    @Mapping(target = "total", expression = "java(calculateTotal(cart))")
    @Mapping(target = "items", ignore = true)
    CartResponse toDto(Cart cart);
    
    default CartResponse toDtoWithItems(Cart cart) {
        CartResponse response = toDto(cart);
        if (cart.getItems() != null) {
            response.setItems(cart.getItems().stream()
                    .map(this::toCartItemDto)
                    .collect(Collectors.toList()));
        }
        return response;
    }

    @Mapping(target = "id", source = "id")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "productName", source = "productName")
    @Mapping(target = "productImage", source = "productImage")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "itemTotal", source = "itemTotal")
    @Mapping(target = "inStock", ignore = true) // Will be set in service
    @Mapping(target = "availableStock", ignore = true) // Will be set in service
    CartItemDto toCartItemDto(CartItem cartItem);

    default int calculateTotalItems(Cart cart) {
        return cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    default BigDecimal calculateSubtotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default BigDecimal calculateTax(Cart cart) {
        BigDecimal subtotal = calculateSubtotal(cart);
        return subtotal.multiply(new BigDecimal("0.10")); // 10% tax for example
    }

    default BigDecimal calculateShippingFee(Cart cart) {
        return new BigDecimal("9.99"); // Flat rate for example
    }

    default BigDecimal calculateTotal(Cart cart) {
        BigDecimal subtotal = calculateSubtotal(cart);
        BigDecimal tax = calculateTax(cart);
        BigDecimal shippingFee = calculateShippingFee(cart);
        BigDecimal discount = cart.getDiscountAmount() != null ? cart.getDiscountAmount() : BigDecimal.ZERO;
        
        return subtotal.add(tax).add(shippingFee).subtract(discount);
    }
}
