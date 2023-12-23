package com.example.employeecheckin.schedule.tree;

public class ScheduleTree
{
    public TimeSlot timeSlot;

    public ScheduleTree(long min, long max)
    {
        timeSlot = new TimeSlot(min, max);
    }

    public boolean schedule(long start, long end)
    {
        return timeSlot.schedule(new Request(start, end));
    }

    public String display()
    {
        return timeSlot.display();
    }
}
