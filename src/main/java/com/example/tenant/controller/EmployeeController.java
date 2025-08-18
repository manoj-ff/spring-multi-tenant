package com.example.tenant.controller;

import com.example.tenant.entity.tenant.Employee;
import com.example.tenant.model.EmployeeDto;
import com.example.tenant.repo.tenant.EmployeeRepository;
import com.example.tenant.service.AsyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final AsyncService asyncService;

    public EmployeeController(EmployeeRepository employeeRepository, AsyncService asyncService) {
        this.employeeRepository = employeeRepository;
        this.asyncService = asyncService;
    }

    @PostMapping("/async")
    public ResponseEntity<EmployeeDto> createAsync(@RequestBody EmployeeDto employeeDto) throws ExecutionException, InterruptedException {
        Employee employee = new Employee();
        employee.setName(employeeDto.getName());
        employee = asyncService.saveEmployee(employee).get();

        employeeDto.setId(employee.getId());
        return ok(employeeDto);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeDto>> getAll() {
        return ok(employeeRepository.findAll().stream()
                .map(e -> new EmployeeDto(e.getId(), e.getName()))
                .collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<EmployeeDto> create(@RequestBody EmployeeDto employeeDto) {
        Employee employee = new Employee();
        employee.setName(employeeDto.getName());
        employeeRepository.save(employee);

        employeeDto.setId(employee.getId());
        return ok(employeeDto);
    }
}
