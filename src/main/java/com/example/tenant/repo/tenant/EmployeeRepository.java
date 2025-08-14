package com.example.tenant.repo.tenant;

import com.example.tenant.entity.tenant.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
}
