package de.minetick.packetbuilder;

import java.util.Observable;

public class PacketBuilderThread extends Observable implements Runnable {

    private PacketBuilderJobInterface job;
    private boolean active;
    private Object waitObject;
    private Thread thread;
    private PacketBuilderBuffer buildBuffer;

    public PacketBuilderThread() {
        this.active = true;
        this.buildBuffer = new PacketBuilderBuffer();
        this.waitObject = new Object();
        this.thread = new Thread(this);
        /*
         *  These threads create so much cpu load, that they have an impact on the main thread
         *  Thread.MIN_PRIORITY = 1
         *  Thread.NORMAL_PRIORITY = 5
         *  Thread.MAX_PRIORITY = 10
         */
        this.thread.setPriority(Thread.NORM_PRIORITY - 2);
        this.thread.start();
    }

    @Override
    public void run() {
        while(this.active) {
            if(this.job == null) {
                synchronized(this.waitObject) {
                    try {
                        this.waitObject.wait();
                    } catch (InterruptedException e) {}
                }
            } else {
                this.job.buildAndSendPacket(this.buildBuffer);
                this.job = null;
                this.setChanged();
                this.notifyObservers();
            }
        }
    }

    public void addJob(PacketBuilderJobInterface job) {
        this.job = job;
        synchronized(this.waitObject) {
            this.waitObject.notifyAll();
        }
    }

    public void fastAddJob(PacketBuilderJobInterface job) {
        this.job = job;
    }

    public void shutdown() {
        this.active = false;
        synchronized(this.waitObject) {
            this.waitObject.notifyAll();
        }        
    }
}
