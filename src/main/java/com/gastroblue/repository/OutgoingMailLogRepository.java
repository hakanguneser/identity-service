package com.gastroblue.repository;

import com.gastroblue.model.entity.OutgoingMailLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutgoingMailLogRepository extends JpaRepository<OutgoingMailLogEntity, String> {}
