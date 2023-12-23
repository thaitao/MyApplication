package com.example.employeecheckin;

import org.junit.Test;

import static org.junit.Assert.*;

import com.example.employeecheckin.schedule.Customer;
import com.example.employeecheckin.schedule.Schedule;
import com.example.employeecheckin.schedule.Service;
import com.example.employeecheckin.schedule.Technician;
import com.example.employeecheckin.schedule.tree.ScheduleTree;

import java.util.Arrays;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest
{
    @Test
    public void testSchedule()
    {
        Schedule.add(Customer.random());
        Schedule.add(Customer.random());
        Schedule.add(Customer.random());
        Schedule.add(Customer.random());
        Schedule.add(Customer.random());
        Schedule.add(Customer.random());

        Technician Hannah = new Technician("Han", Arrays.asList(Service.FS, Service.D, Service.F, Service.M, Service.PP, Service.P));
        Technician Anna = new Technician("Anna", Arrays.asList(Service.D, Service.F, Service.M, Service.PP, Service.P));
        Technician Nana = new Technician("Nana", Arrays.asList(Service.M, Service.PP, Service.P));
        Technician Jimmy = new Technician("Jimmy", Arrays.asList(Service.PP, Service.P));
        Technician Thai = new Technician("Thai", Arrays.asList(Service.M, Service.PP, Service.P));

        Schedule.add(Hannah);
        Schedule.add(Anna);
        Schedule.add(Nana);
        Schedule.add(Jimmy);
        Schedule.add(Thai);

        Schedule.compute();
    }

    @Test
    public void testScheduleTree()
    {
        ScheduleTree tree = new ScheduleTree(0, 100);
        tree.schedule(30, 40);

    }
}