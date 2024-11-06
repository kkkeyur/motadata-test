package com.motadata.tesapi.repos;

import com.motadata.tesapi.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepo extends JpaRepository<AuditLog, Long> {
}

