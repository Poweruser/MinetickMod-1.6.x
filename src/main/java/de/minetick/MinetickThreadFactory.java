package de.minetick;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class MinetickThreadFactory implements ThreadFactory {

    private int priority;
    private int idCounter = 0;
    private String name = "MinetickModThread";

    public MinetickThreadFactory(int priority) {
        this.priority = Math.max(priority, Thread.MIN_PRIORITY);
        this.priority = Math.min(priority, Thread.MAX_PRIORITY);
    }

    public MinetickThreadFactory(int priority, String name) {
        this(priority);
        this.name = name;
    }

    public MinetickThreadFactory(String name) {
        this(Thread.NORM_PRIORITY);
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable arg0) {
        Thread t = Executors.defaultThreadFactory().newThread(arg0);
        t.setPriority(this.priority);
        t.setName(this.name + "-" + idCounter);
        idCounter++;
        return t;
    }
}
