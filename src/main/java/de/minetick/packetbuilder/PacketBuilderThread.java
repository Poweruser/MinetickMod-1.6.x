package de.minetick.packetbuilder;

import java.util.Observable;

public class PacketBuilderThread extends Observable implements Runnable {

    private PacketBuilderJobInterface job;
    private boolean active;
    private Object waitObject;
    private Thread thread;

    public PacketBuilderThread() {
        this.active = true;
        this.waitObject = new Object();
        this.thread = new Thread(this);
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
                this.job.buildAndSendPacket();
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
