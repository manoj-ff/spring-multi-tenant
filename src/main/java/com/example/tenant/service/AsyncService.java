package com.example.tenant.service;

import com.example.tenant.entity.tenant.Employee;
import com.example.tenant.repo.tenant.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AsyncService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Async
    public CompletableFuture<Employee> saveEmployee(Employee employee) {
        Employee savedEmployee = employeeRepository.save(employee);
        return CompletableFuture.completedFuture(savedEmployee);
    }
}
