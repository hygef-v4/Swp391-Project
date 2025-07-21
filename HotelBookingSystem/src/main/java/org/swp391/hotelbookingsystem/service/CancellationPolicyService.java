package org.swp391.hotelbookingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swp391.hotelbookingsystem.model.CancellationPolicy;
import org.swp391.hotelbookingsystem.repository.CancellationPolicyRepository;

@Service
public class CancellationPolicyService {

    private final CancellationPolicyRepository cancellationPolicyRepository;

    @Autowired
    public CancellationPolicyService(CancellationPolicyRepository cancellationPolicyRepository) {
        this.cancellationPolicyRepository = cancellationPolicyRepository;
    }

    public void saveCancellationPolicy(CancellationPolicy policy) {
        // Check if policy already exists for this hotel
        CancellationPolicy existing = cancellationPolicyRepository.findByHotelId(policy.getHotelId());
        
        if (existing != null) {
            // Update existing policy
            cancellationPolicyRepository.updateCancellationPolicy(policy);
        } else {
            // Insert new policy
            cancellationPolicyRepository.insertCancellationPolicy(policy);
        }
    }

    public CancellationPolicy getCancellationPolicyByHotelId(int hotelId) {
        return cancellationPolicyRepository.findByHotelId(hotelId);
    }

    public void updateCancellationPolicy(CancellationPolicy policy) {
        cancellationPolicyRepository.updateCancellationPolicy(policy);
    }

    public void deleteCancellationPolicy(int hotelId) {
        cancellationPolicyRepository.deleteCancellationPolicy(hotelId);
    }

    public boolean validatePolicy(CancellationPolicy policy) {
        // Basic validation rules
        if (policy.getPartialRefundDays() < 0 || policy.getPartialRefundDays() > 365) {
            return false;
        }
        if (policy.getPartialRefundPercent() < 0 || policy.getPartialRefundPercent() > 100) {
            return false;
        }
        if (policy.getNoRefundWithinDays() < 0 || policy.getNoRefundWithinDays() > 365) {
            return false;
        }
        // Logical validation: partial refund period should be > no refund period
        if (policy.getPartialRefundDays() <= policy.getNoRefundWithinDays()) {
            return false;
        }
        return true;
    }
} 