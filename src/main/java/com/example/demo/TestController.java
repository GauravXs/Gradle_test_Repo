package com.example.demo;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class TestController {

    @GetMapping("/employees")
    public List<Employee> getList() {

        List<Employee> employees = new ArrayList<>();
        employees.add(new Employee(1L, "emp001", "Shyam", "Thane", "IT Manager"));
        employees.add(new Employee(2L, "emp002", "Ram", "Thane", "BA"));
        employees.add(new Employee(3L, "emp003", "Sachin", "Thane", "Java Programmer"));

        return employees;
    }
}
