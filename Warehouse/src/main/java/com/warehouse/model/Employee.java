package com.warehouse.model;

import java.util.List;

public class Employee {
    private int id_employee;
    private String employee_name;
    private List<Item> items;

    public Employee() {
    }

    public Employee(String employee_name) {
        this.employee_name = employee_name;
    }

    public int getId_employee() {
        return id_employee;
    }

    public void setId_employee(int id_employee) {
        this.id_employee = id_employee;
    }

    public String getEmployee_name() {
        return employee_name;
    }

    public void setEmployee_name(String employee_name) {
        this.employee_name = employee_name;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}
