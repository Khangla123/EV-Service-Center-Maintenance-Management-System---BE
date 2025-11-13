package com.swp391.EV.service.repository;

import com.swp391.EV.service.model.ServiceOrderPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceOrderPartRepository extends JpaRepository<ServiceOrderPart, UUID> {
    @Query("SELECT sop FROM ServiceOrderPart sop JOIN FETCH sop.part WHERE sop.serviceOrder.id = :serviceOrderId")
    List<ServiceOrderPart> findByServiceOrderId(@Param("serviceOrderId") UUID serviceOrderId);
    
    void deleteByServiceOrderId(UUID serviceOrderId);
}
