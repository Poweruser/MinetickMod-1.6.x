package de.minetick.packetbuilder;

import java.util.Iterator;
import java.util.LinkedList;

public class PacketBuilderBuffer {

    private LinkedList<byte[]> buildBufferCache = new LinkedList<byte[]>();
    private LinkedList<byte[]> sendBufferCache = new LinkedList<byte[]>();
    private int notUsedBuildBuffers = Integer.MAX_VALUE;
    private int notUsedSendBuffers = Integer.MAX_VALUE;

    public PacketBuilderBuffer() {}

    public byte[] requestBuildBuffer(int length) {
        return this.checkInList(this.buildBufferCache, length);
    }

    public byte[] requestBuildBufferAndCopy(int requestLength, int copyLength, byte[] source) {
        byte[] target = this.requestBuildBuffer(requestLength);
        System.arraycopy(source, 0, target, 0, copyLength);
        return target;
    }

    public void offerBuildBuffer(byte[] array) {
        this.buildBufferCache.add(array);
    }

    public void offerSendBuffer(byte[] array) {
        synchronized(this.sendBufferCache) {
            this.sendBufferCache.add(array);
        }
    }

    public byte[] requestSendBuffer(int length) {
        synchronized(this.sendBufferCache) {
            return this.checkInList(this.sendBufferCache, length);
        }
    }

    public void clear() {
        synchronized(this.sendBufferCache) {
            this.sendBufferCache.clear();
        }
        this.buildBufferCache.clear();
    }

    private void checkSendBufferUsage(int newSize) {
        if(newSize < this.notUsedSendBuffers) {
            this.notUsedSendBuffers = newSize;
        }
    }

    private void checkBuildBufferUsage(int newSize) {
        if(newSize < this.notUsedBuildBuffers) {
            this.notUsedBuildBuffers = newSize;
        }
    }

    public void releaseUnusedBuffers() {
        if(this.notUsedBuildBuffers <= this.buildBufferCache.size()) {
            for(int i = 0; i < this.notUsedBuildBuffers; i++) {
                this.buildBufferCache.removeFirst();
            }
            this.notUsedBuildBuffers = Integer.MAX_VALUE;
        }
        if(this.notUsedSendBuffers <= this.sendBufferCache.size()) {
            synchronized(this.sendBufferCache) {
                for(int i = 0; i < this.notUsedSendBuffers; i++) {
                    this.sendBufferCache.removeFirst();
                }
            }
            this.notUsedSendBuffers = Integer.MAX_VALUE;
        }
    }

    private byte[] checkInList(LinkedList<byte[]> list, int length) {
        Iterator<byte[]> iter = list.descendingIterator();
        byte[] tmp;
        int size = list.size();
        while(iter.hasNext()) {
            tmp = iter.next();
            if(tmp.length >= length) {
                iter.remove();
                if(list == this.buildBufferCache) {
                    this.checkBuildBufferUsage(size - 1);
                } else if(list == this.sendBufferCache) {
                    this.checkSendBufferUsage(size - 1);
                }
                return tmp;
            }
        }
        tmp = null;
        return new byte[length];
    }
}
