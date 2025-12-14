package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.InventoryRequest;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.dto.InventoryStatus;
import com.ecommerce.inventory.dto.ReleaseRequest;
import com.ecommerce.inventory.dto.ReservationRequest;
import com.ecommerce.inventory.dto.StockAdjustment;
import com.ecommerce.inventory.dto.StockLevel;
import com.ecommerce.inventory.service.InventoryService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/inventory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Inventory API", description = "APIs for managing inventory")
public class InventoryController {

    @Inject
    InventoryService inventoryService;

    @POST
    @Operation(summary = "Create a new inventory item")
    public Response createInventoryItem(@Valid InventoryRequest request) {
        InventoryResponse response = inventoryService.createInventoryItem(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{skuCode}")
    @Operation(summary = "Get inventory by SKU code")
    public InventoryResponse getInventoryBySkuCode(@PathParam("skuCode") String skuCode) {
        return inventoryService.getInventoryBySkuCode(skuCode);
    }

    @POST
    @Path("/adjust")
    @Operation(summary = "Adjust inventory stock level")
    public InventoryResponse adjustStock(@Valid StockAdjustment adjustment) {
        return inventoryService.adjustStock(adjustment);
    }

    @POST
    @Path("/reserve")
    @Operation(summary = "Reserve stock for an order")
    public InventoryResponse reserveStock(@Valid ReservationRequest request) {
        return inventoryService.reserveStock(request);
    }

    @POST
    @Path("/release")
    @Operation(summary = "Release reserved stock")
    public InventoryResponse releaseStock(@Valid ReleaseRequest request) {
        return inventoryService.releaseStock(request);
    }

    @GET
    @Path("/status/{skuCode}")
    @Operation(summary = "Check inventory status by SKU code")
    public InventoryStatus checkInventoryStatus(@PathParam("skuCode") String skuCode) {
        return inventoryService.checkInventoryStatus(skuCode);
    }

    @GET
    @Path("/low-stock")
    @Operation(summary = "Get all low stock items")
    public List<StockLevel> getLowStockItems() {
        return inventoryService.getLowStockItems();
    }

    @POST
    @Path("/{skuCode}/restock")
    @Operation(summary = "Process restock for an item")
    public Response processRestock(
            @PathParam("skuCode") String skuCode,
            @QueryParam("quantity") int quantity) {
        inventoryService.processRestock(skuCode, quantity);
        return Response.noContent().build();
    }

    @POST
    @Path("/{skuCode}/consume")
    @Operation(summary = "Consume reserved stock")
    public InventoryResponse consumeReservedStock(
            @PathParam("skuCode") String skuCode,
            @QueryParam("quantity") int quantity,
            @QueryParam("reservationId") String reservationId) {
        return inventoryService.consumeReservedStock(skuCode, quantity, reservationId);
    }

    @GET
    @Path("/health")
    @Operation(summary = "Health check endpoint")
    public String health() {
        return "Inventory Service is healthy";
    }
}
