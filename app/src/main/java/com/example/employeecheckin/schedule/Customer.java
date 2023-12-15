package com.example.employeecheckin.schedule;

import java.util.Arrays;
import java.util.List;

public class Customer
{
    public List<Service> requests;
    public long checkin;
    public long checkout;

    public Customer(List<Service> requests)
    {
        this.requests = requests;
    }

    public static Customer random()
    {
        return new Customer(Arrays.asList(Service.random()));
    }
}
