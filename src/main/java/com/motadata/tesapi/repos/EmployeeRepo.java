package com.motadata.tesapi.repos;

import com.motadata.tesapi.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepo extends JpaRepository<Employee, Long> {
}

