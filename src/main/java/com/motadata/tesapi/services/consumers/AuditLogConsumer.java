package com.motadata.tesapi.services.consumers;

import com.motadata.tesapi.entities.AuditLog;
import com.motadata.tesapi.repos.AuditLogRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuditLogConsumer {

    private final AuditLogRepo auditLogRepository;



    @KafkaListener(topics = "employee-topic", groupId = "group_id")
    public void consume(String message) {
        AuditLog auditLog = new AuditLog();
        auditLog.setEntity("Employee");
        auditLog.setAction(message);
        auditLog.setTimestamp(new Date());

        auditLogRepository.save(auditLog);
    }
}

