package de.minetick;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class MinetickThreadFactory implements ThreadFactory {

    private int priority;

    public MinetickThreadFactory(int priority) {
        this.priority = Math.max(priority, Thread.MIN_PRIORITY);
        this.priority = Math.min(priority, Thread.MAX_PRIORITY);
    }

    @Override
    public Thread newThread(Runnable arg0) {
        Thread t = Executors.defaultThreadFactory().newThread(arg0);
        t.setPriority(this.priority);
        return t;
    }
}
