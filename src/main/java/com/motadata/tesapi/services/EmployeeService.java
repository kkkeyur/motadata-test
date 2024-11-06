package com.motadata.tesapi.services;

import com.motadata.tesapi.entities.Employee;
import com.motadata.tesapi.repos.EmployeeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepo employeeRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC = "employee-topic";


    // Create or Update Employee
    @Transactional
    public Employee saveEmployee(Employee employee) {
        Employee savedEmployee = employeeRepository.save(employee);
        kafkaTemplate.send(TOPIC, "Created/Updated employee: " + savedEmployee.getId());
        return savedEmployee;
    }

    // Delete Employee
    @Transactional
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
        kafkaTemplate.send(TOPIC, "Deleted employee with ID: " + id);
    }

    // Get all Employees
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    // Get Employee by ID
    public Employee getEmployee(Long id) {
        return employeeRepository.findById(id).orElse(null);
    }
}

