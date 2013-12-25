package de.minetick;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ArrayBlockingQueue;

import de.minetick.profiler.Profiler;

import net.minecraft.server.WorldServer;

public class ThreadPool {

    private boolean active;
    private int threadCount;
    private int remainingJobs;
    private int currentNumOfJobs;
    private ArrayDeque<WorkThread> availableThreads;
    private ArrayDeque<WorkThread> usedThreads;
    private ArrayDeque<WorkThread> tmpVar;
    private ArrayBlockingQueue<Object> doneSignal;
    private Object signal = new Object();
    private LockObject lockObj = new LockObject();
    private Object threadDoneLock = new Object();
    private LinkedList<WorkThread> allThreads = new LinkedList<WorkThread>();

    public Profiler profiler;

    public ThreadPool(Profiler profiler) {
        this.profiler = profiler;
        this.active = true;
        this.threadCount = 0;
        this.remainingJobs = 0;
        this.availableThreads = new ArrayDeque<WorkThread>();
        this.usedThreads = new ArrayDeque<WorkThread>();
        this.tmpVar = null;
        this.doneSignal = new ArrayBlockingQueue<Object>(1);
    }

    public void prepareTick(int jc) {
        while(!this.availableThreads.isEmpty()) {
            this.usedThreads.addLast(this.availableThreads.removeFirst());
        }
        this.tmpVar = this.availableThreads;
        this.availableThreads = this.usedThreads;
        this.usedThreads = this.tmpVar;
        this.remainingJobs = jc;
        this.currentNumOfJobs = jc;
    }

    public void tickWorld(WorldServer ws) {
        if(this.active) {
            WorkThread wt;
            if(!this.availableThreads.isEmpty()) {
                wt = this.availableThreads.removeFirst();
            } else {
                wt = new WorkThread(this.threadCount, this.lockObj);
                this.allThreads.addLast(wt);
                this.threadCount++;
                //System.out.println("[INFO] MinetickMod: Creating another world thread. #Threads: " + this.threadCount + " #Worlds: " + this.currentNumOfJobs);
            }
            wt.startTicking(ws);
        }
    }

    public void threadDone(WorkThread wt) {
        synchronized(this.threadDoneLock) {
            if(this.active) {
                this.usedThreads.addLast(wt);
            } else {
                wt.shutdown();
            }
            this.remainingJobs--;
            if(this.remainingJobs == 0) {
                this.doneSignal.add(this.signal);
            }
        }
    }

    public void waitUntilDone() {
        while(this.active) {
            try {
                this.doneSignal.take();
                return;
            } catch (InterruptedException e) {}
        }
    }

    public void shutdown() {
        if(this.active) {
            this.active = false;
            for(WorkThread w: this.allThreads) {
                w.shutdown();
            }
        }
    }

    private class WorkThread implements Observer {
        private WorldTicker wt;
        private Thread t;
        private int id;

        public WorkThread(int threadid, LockObject lockObj) {
            this.id = threadid;
            this.wt = new WorldTicker(profiler, lockObj);
            this.wt.addObserver(this);
            this.t = new Thread(this.wt);
            this.t.setPriority(Thread.NORM_PRIORITY + 1);
            this.t.start();
        }

        public void startTicking(WorldServer ws) {
            this.wt.startWorld(ws);			
        }

        @Override
        public void update(Observable o, Object arg) {
            if(o instanceof WorldTicker) {
                threadDone(this);
            }			
        }

        public void shutdown() {
            this.wt.shutdown();
            this.wt.deleteObserver(this);
        }
    }
}
