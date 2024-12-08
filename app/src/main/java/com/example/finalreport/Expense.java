package com.example.finalreport;

public class Expense {

    private String id;
    private String name;
    private double amount;

    public Expense() {
        // Default constructor required for Firebase
    }

    public Expense(String id, String name, double amount) {
        this.id = id;
        this.name = name;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }
}

