package com.skf.scm.po.service;

import com.skf.scm.po.dto.PurchaseOrderRequest;
import com.skf.scm.po.dto.PurchaseOrderResponse;
import com.skf.scm.po.entity.PurchaseOrder;
import com.skf.scm.po.entity.PurchaseOrderStatus;
import com.skf.scm.po.event.PurchaseOrderEventPublisher;
import com.skf.scm.po.exception.PurchaseOrderExceptions.InvalidPurchaseOrderStateException;
import com.skf.scm.po.exception.PurchaseOrderExceptions.PurchaseOrderNotFoundException;
import com.skf.scm.po.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseOrderService {

    private final PurchaseOrderRepository repository;
    private final PurchaseOrderEventPublisher eventPublisher;

    private static final List<PurchaseOrderStatus> OPEN_STATUSES =
            List.of(PurchaseOrderStatus.DRAFT, PurchaseOrderStatus.APPROVED);

    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request, boolean autoTriggered) {
        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber(generatePoNumber())
                .supplierCode(request.supplierCode())
                .skuCode(request.skuCode())
                .warehouseCode(request.warehouseCode())
                .quantity(request.quantity())
                .unitCost(request.unitCost())
                .status(PurchaseOrderStatus.DRAFT)
                .autoTriggered(autoTriggered)
                .expectedDeliveryDate(Instant.now().plus(
                        request.expectedLeadTimeDays() != null ? request.expectedLeadTimeDays() : 7, ChronoUnit.DAYS))
                .build();

        PurchaseOrder saved = repository.save(po);
        eventPublisher.publishPoCreated(saved);
        return PurchaseOrderResponse.from(saved);
    }

    /**
     * Called by the LowStockDetectedEvent listener. Guards against firing
     * duplicate auto-POs for the same SKU/warehouse while one is already
     * open (DRAFT or APPROVED) — a low-stock event can legitimately fire
     * more than once before the first PO clears the pipeline.
     */
    public void autoCreateFromLowStock(String supplierCode, String skuCode, String warehouseCode,
                                        int suggestedQuantity, BigDecimal estimatedUnitCost, Integer supplierLeadTimeDays) {
        boolean alreadyOpen = repository.existsBySkuCodeAndWarehouseCodeAndStatusIn(skuCode, warehouseCode, OPEN_STATUSES);
        if (alreadyOpen) {
            return;
        }
        PurchaseOrderRequest request = new PurchaseOrderRequest(
                supplierCode, skuCode, warehouseCode, suggestedQuantity, estimatedUnitCost, supplierLeadTimeDays);
        createPurchaseOrder(request, true);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getByPoNumber(String poNumber) {
        return PurchaseOrderResponse.from(findOrThrow(poNumber));
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getAll() {
        return repository.findAll().stream().map(PurchaseOrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getByStatus(PurchaseOrderStatus status) {
        return repository.findByStatus(status).stream().map(PurchaseOrderResponse::from).toList();
    }

    public PurchaseOrderResponse approve(String poNumber) {
        PurchaseOrder po = findOrThrow(poNumber);
        if (po.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new InvalidPurchaseOrderStateException(poNumber, po.getStatus().name(), "approve");
        }
        po.setStatus(PurchaseOrderStatus.APPROVED);
        PurchaseOrder saved = repository.save(po);
        eventPublisher.publishPoApproved(saved);
        return PurchaseOrderResponse.from(saved);
    }

    public PurchaseOrderResponse markReceived(String poNumber) {
        PurchaseOrder po = findOrThrow(poNumber);
        if (po.getStatus() != PurchaseOrderStatus.APPROVED) {
            throw new InvalidPurchaseOrderStateException(poNumber, po.getStatus().name(), "receive");
        }
        po.setStatus(PurchaseOrderStatus.RECEIVED);
        PurchaseOrder saved = repository.save(po);
        eventPublisher.publishPoReceived(saved); // triggers inventory-service to increment stock
        return PurchaseOrderResponse.from(saved);
    }

    public PurchaseOrderResponse cancel(String poNumber) {
        PurchaseOrder po = findOrThrow(poNumber);
        if (po.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new InvalidPurchaseOrderStateException(poNumber, po.getStatus().name(), "cancel");
        }
        po.setStatus(PurchaseOrderStatus.CANCELLED);
        return PurchaseOrderResponse.from(repository.save(po));
    }

    private PurchaseOrder findOrThrow(String poNumber) {
        return repository.findByPoNumber(poNumber)
                .orElseThrow(() -> new PurchaseOrderNotFoundException(poNumber));
    }

    private String generatePoNumber() {
        return "PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
