package com.example.employeecheckin.schedule;

import java.util.LinkedList;

public class Schedule
{
    public static LinkedList<Technician> technicians = new LinkedList<>();
    public static LinkedList<Customer> customers = new LinkedList<>();
    public static LinkedList<Service> services = new LinkedList<>();

    public static void reset()
    {
        technicians.clear();
        customers.clear();
        services.clear();
    }

    public static void add(Technician technician)
    {
        technicians.add(technician);
    }

    public static void add(Customer customer)
    {
        customers.add(customer);
        services.addAll(customer.requests);
    }

    public static void compute()
    {
        displayHeader();
        displayAbility();

        System.out.println();

        boolean[][] fcfs = firstComeFirstServe();
        displayHeader();
        displayResult(fcfs);

        System.out.println();
        displayBusinessValue(fcfs);
    }

    private static boolean[][] firstComeFirstServe()
    {
        boolean[][] result = new boolean[technicians.size()][services.size()];
        boolean[] assignments = new boolean[services.size()];
        for (int i = 0; i < technicians.size(); i++)
        {
            Technician technician = technicians.get(i);
            for (int j = 0; j < services.size(); j++)
            {
                Service service = services.get(j);
                if (!assignments[j] && technician.abilities.contains(service))
                {
                    result[i][j] = true;
                    assignments[j] = true;
                    break;
                }
            }
        }
        return result;
    }

    private static void displayHeader()
    {
        System.out.print(String.format("%8s", ""));
        for (int i = 0; i < services.size(); i++)
        {
            System.out.print(String.format("%6s", services.get(i)));
        }
        System.out.println("");
    }

    private static void displayAbility()
    {
        for (int i = 0; i < technicians.size(); i++)
        {
            Technician technician = technicians.get(i);
            boolean[] ability = technician.compute(services);
            System.out.print(String.format("%8s", technician.name));
            for (int j = 0; j < ability.length; j++)
            {
                System.out.print(String.format("%6s", ability[j] ? "x" : ""));
            }
            System.out.println();
        }
    }
    private static void displayResult(boolean[][] result)
    {
        for (int i = 0; i < technicians.size(); i++)
        {
            Technician technician = technicians.get(i);
            long[] ability = technician.computeValue(services);
            System.out.print(String.format("%8s", technician.name));
            for (int j = 0; j < ability.length; j++)
            {
                System.out.print(String.format("%5d", ability[j]));
                System.out.print(String.format("%1s", result[i][j] ? "x" : ""));
            }
            System.out.println();
        }
    }

    private static void displayBusinessValue(boolean[][] result)
    {
        long businessValue = 0;
        for (int i = 0; i < result.length; i++)
        {
            boolean[] row = result[i];
            for (int j = 0; j < row.length; j++)
            {
                if (result[i][j])
                {
                    businessValue += services.get(j).value;
                }
            }
        }
        System.out.print(String.format("Business Value = $%d", businessValue));
        System.out.println();
    }
}
