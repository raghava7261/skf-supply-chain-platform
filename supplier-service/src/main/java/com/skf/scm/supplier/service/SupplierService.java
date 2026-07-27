package com.skf.scm.supplier.service;

import com.skf.scm.supplier.dto.SupplierRequest;
import com.skf.scm.supplier.dto.SupplierResponse;
import com.skf.scm.supplier.entity.Supplier;
import com.skf.scm.supplier.event.SupplierEventPublisher;
import com.skf.scm.supplier.exception.SupplierExceptions.DuplicateSupplierException;
import com.skf.scm.supplier.exception.SupplierExceptions.SupplierNotFoundException;
import com.skf.scm.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierEventPublisher eventPublisher;

    public SupplierResponse createSupplier(SupplierRequest request) {
        if (supplierRepository.existsBySupplierCode(request.supplierCode())) {
            throw new DuplicateSupplierException(request.supplierCode());
        }
        Supplier supplier = Supplier.builder()
                .supplierCode(request.supplierCode())
                .name(request.name())
                .contactEmail(request.contactEmail())
                .country(request.country())
                .leadTimeDays(request.leadTimeDays())
                .reliabilityScore(100.0)
                .active(true)
                .build();

        Supplier saved = supplierRepository.save(supplier);
        eventPublisher.publishSupplierRegistered(saved);
        return SupplierResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public SupplierResponse getBySupplierCode(String supplierCode) {
        return SupplierResponse.from(findOrThrow(supplierCode));
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream().map(SupplierResponse::from).toList();
    }

    public SupplierResponse updateSupplier(String supplierCode, SupplierRequest request) {
        Supplier supplier = findOrThrow(supplierCode);
        supplier.setName(request.name());
        supplier.setContactEmail(request.contactEmail());
        supplier.setCountry(request.country());
        supplier.setLeadTimeDays(request.leadTimeDays());
        return SupplierResponse.from(supplierRepository.save(supplier));
    }

    /**
     * Called by purchase-order-service consumers indirectly (via REST) to adjust
     * a supplier's reliability score after each delivery — on-time deliveries
     * push the score up, late ones pull it down. Feeds the AI forecasting model's
     * confidence weighting per supplier.
     */
    public SupplierResponse adjustReliabilityScore(String supplierCode, boolean onTime) {
        Supplier supplier = findOrThrow(supplierCode);
        double delta = onTime ? 1.0 : -5.0;
        double newScore = Math.max(0, Math.min(100, supplier.getReliabilityScore() + delta));
        supplier.setReliabilityScore(newScore);
        return SupplierResponse.from(supplierRepository.save(supplier));
    }

    public void deactivateSupplier(String supplierCode) {
        Supplier supplier = findOrThrow(supplierCode);
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }

    private Supplier findOrThrow(String supplierCode) {
        return supplierRepository.findBySupplierCode(supplierCode)
                .orElseThrow(() -> new SupplierNotFoundException(supplierCode));
    }
}
