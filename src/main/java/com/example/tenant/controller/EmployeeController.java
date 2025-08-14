package com.example.tenant.controller;

import com.example.tenant.entity.Employee;
import com.example.tenant.model.EmployeeDto;
import com.example.tenant.repo.EmployeeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    static AtomicInteger id = new AtomicInteger(1);

    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDto>> getAll() {
        return ok(employeeRepository.findAll().stream().map(e -> new EmployeeDto(e.getName())).toList());
    }

    @PostMapping
    public ResponseEntity<EmployeeDto> create(@RequestBody EmployeeDto employeeDto) {
        Employee employee = new Employee();
        employee.setId(id.getAndIncrement());
        employee.setName(employeeDto.getName());
        employeeRepository.save(employee);
        return ok(employeeDto);
    }
}
