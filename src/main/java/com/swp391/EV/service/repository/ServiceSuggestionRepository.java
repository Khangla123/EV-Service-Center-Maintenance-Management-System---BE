package com.swp391.EV.service.repository;

import com.swp391.EV.service.model.ServiceSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceSuggestionRepository extends JpaRepository<ServiceSuggestion, UUID> {
    List<ServiceSuggestion> findByServiceOrderId(UUID serviceOrderId);
    List<ServiceSuggestion> findByServiceOrderIdAndStatus(UUID serviceOrderId, ServiceSuggestion.SuggestionStatus status);
}
