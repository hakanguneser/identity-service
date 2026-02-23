package com.gastroblue.mail;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OutgoingMailLogRepository extends JpaRepository<OutgoingMailLogEntity, String> {}
