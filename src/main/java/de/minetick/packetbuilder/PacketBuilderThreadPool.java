package de.minetick.packetbuilder;

import java.util.ArrayDeque;
import java.util.Observable;
import java.util.Observer;


public class PacketBuilderThreadPool implements Observer {

    private boolean active;
    private ArrayDeque<PacketBuilderThread> availableThreads;
    private ArrayDeque<PacketBuilderThread> allThreads;
    private ArrayDeque<PacketBuilderJobInterface> waitingJobs;
    private Object jobLock = new Object();
    private static PacketBuilderThreadPool pool;
    private static int targetPoolSize;
    
    public PacketBuilderThreadPool(int poolsize) {
        poolsize = Math.max(1, poolsize);
        targetPoolSize = poolsize;
        this.availableThreads = new ArrayDeque<PacketBuilderThread>();
        this.allThreads = new ArrayDeque<PacketBuilderThread>();
        this.waitingJobs = new ArrayDeque<PacketBuilderJobInterface>();
        for(int i = 0; i < poolsize; i++) {
            PacketBuilderThread thread = new PacketBuilderThread();
            thread.addObserver(this);
            this.availableThreads.add(thread);
            this.allThreads.add(thread);
        }
        pool = this;
        this.active = true;
    }

    public static void addJobStatic(PacketBuilderJobInterface job) {
        if(pool != null) {
            pool.addJob(job);
        }
    }

    public void addJob(PacketBuilderJobInterface job) {
        if(this.active) {
            synchronized(this.jobLock) {
                if(this.availableThreads.isEmpty()) {
                    this.waitingJobs.add(job);
                } else {
                    PacketBuilderThread thread = this.availableThreads.poll();
                    thread.addJob(job);
                }
            }
        }
    }

    @Override
    public void update(Observable observable, Object obj) {
        if(observable instanceof PacketBuilderThread) {
            PacketBuilderThread thread = (PacketBuilderThread)observable;
            synchronized(this.jobLock) {
                if(this.active && targetPoolSize >= this.allThreads.size()) {
                    if(!this.waitingJobs.isEmpty()) { 
                        thread.fastAddJob(this.waitingJobs.poll());
                    } else {
                        this.availableThreads.add(thread);
                        if(this.availableThreads.size() == this.allThreads.size()) {
                            for(PacketBuilderThread pbt: this.allThreads) {
                                pbt.clearCache();
                            }
                        }
                    }
                } else {
                    thread.deleteObserver(this);
                    thread.shutdown();
                    this.allThreads.remove(thread);
                }
            }
        }
    }

    public static void shutdownStatic() {
        pool.shutdown();
    }

    private void shutdown() {
        this.active = false;
        synchronized(this.jobLock) {
            for(PacketBuilderThread thread : this.allThreads) {
                thread.deleteObserver(this);
                thread.shutdown();
            }
            this.allThreads.clear();
            this.availableThreads.clear();
        }
    }

    public static void adjustPoolSize(int size) {
        if(size >= 1 && pool != null) {
            targetPoolSize = size;
            if(pool.allThreads.size() < targetPoolSize) {
                synchronized(pool.jobLock) {
                    while(pool.allThreads.size() < targetPoolSize) {
                        PacketBuilderThread thread = new PacketBuilderThread();
                        thread.addObserver(pool);
                        pool.availableThreads.add(thread);
                        pool.allThreads.add(thread);
                    }
                }
            }
        }
    }
}
