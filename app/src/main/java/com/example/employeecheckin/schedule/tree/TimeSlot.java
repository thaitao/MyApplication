package com.example.employeecheckin.schedule.tree;

public class TimeSlot
{
    public long min;
    public long max;
    public Request request;
    public TimeSlot leftChild;
    public TimeSlot rightChild;

    public TimeSlot(long min, long max)
    {
        this.min = min;
        this.max = max;
    }

    public boolean schedule(Request request)
    {
        if (min <= request.start && request.end <= max)
        {
            if (request == null)
            {
                this.request = request;
                if (request.start != min)
                {
                    leftChild = new TimeSlot(min, request.start);
                }

                if (request.end != max)
                {
                    rightChild = new TimeSlot(request.end, max);
                }
                return true;
            }
            else if (request.end <= this.request.start)
            {
                leftChild.schedule(request);
            }
            else if (request.start >= this.request.end)
            {
                rightChild.schedule(request);
            }
        }
        return false;
    }

    public String display()
    {

    }
}
