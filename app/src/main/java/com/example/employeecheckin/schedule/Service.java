package com.example.employeecheckin.schedule;

import java.util.Random;

public enum Service
{
    FS("Fullset", 60, 60, true),
    D("Dip", 50, 45, true),
    F("Fill", 45, 45, true),
    M("Manicure", 25, 30, true),
    PP("Perfection Pedicure", 85, 90, false),
    P("Pedicure", 35, 30, false);

    public String name;
    public long value;
    public long duration;
    public boolean isHand;

    Service(String name, long value, long duration, boolean isHand)
    {
        this.name = name;
        this.value = value;
        this.duration = duration;
        this.isHand = isHand;
    }

    public static Service random()
    {
        Random random = new Random();
        return Service.values()[random.nextInt(100) % Service.values().length];
    }


}
