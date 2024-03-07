package com.example.demo;

public class Employee {
    private Long id;
    private String empCode;
    private String empName;
    private String address;
    private String designation;

    public Employee(Long id, String empCode, String empName, String address, String designation) {
        this.id = id;
        this.empCode = empCode;
        this.empName = empName;
        this.address = address;
        this.designation = designation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmpCode() {
        return empCode;
    }

    public void setEmpCode(String empCode) {
        this.empCode = empCode;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }
}
