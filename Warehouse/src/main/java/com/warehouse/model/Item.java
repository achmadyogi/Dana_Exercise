package com.warehouse.model;

import java.util.List;

public class Item {
    private int id_item;
    private String item_name;
    private int amount;
    private List<Employee> employees;

    public Item() {
    }

    public Item(String item_name, int amount) {
        this.item_name = item_name;
        this.amount = amount;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getId_item() {
        return id_item;
    }

    public void setId_item(int id_item) {
        this.id_item = id_item;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }
}
