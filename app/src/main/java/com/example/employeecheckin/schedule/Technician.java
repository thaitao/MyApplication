package com.example.employeecheckin.schedule;

import java.util.List;

public class Technician
{
    public String name;
    public List<Service> abilities;

    public Technician(String name, List<Service> abilities)
    {
        this.name = name;
        this.abilities = abilities;
    }

    public boolean[] compute(List<Service> services)
    {
        boolean[] result = new boolean[services.size()];
        for (int i = 0; i < result.length; i++)
        {
            result[i] = abilities.contains(services.get(i));
        }
        return result;
    }

    public long[] computeValue(List<Service> services)
    {
        long[] result = new long[services.size()];
        for (int i = 0; i < result.length; i++)
        {
            result[i] = abilities.contains(services.get(i)) ? services.get(i).value : 0;
        }
        return result;
    }
}
