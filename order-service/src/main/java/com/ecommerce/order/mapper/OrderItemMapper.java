package com.ecommerce.order.mapper;

import com.ecommerce.order.dto.orderitem.OrderItemResponse;
import com.ecommerce.order.model.OrderItem;
import org.mapstruct.*;

@Mapper(componentModel = "cdi",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "productName", source = "productName")
    @Mapping(target = "productSku", source = "productSku")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "totalPrice", source = "totalPrice")
    @Mapping(target = "notes", source = "notes")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);
}
