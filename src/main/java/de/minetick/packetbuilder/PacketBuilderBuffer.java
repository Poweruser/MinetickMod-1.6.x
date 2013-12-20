package de.minetick.packetbuilder;

import java.util.ArrayList;

public class PacketBuilderBuffer {

    private ArrayList<byte[]> buildBufferCache = new ArrayList<byte[]>();
    private ArrayList<byte[]> sendBufferCache = new ArrayList<byte[]>();
    
    public PacketBuilderBuffer() {}
    
    public byte[] requestBuildBuffer(int length) {
        for(int i = 0; i < this.buildBufferCache.size(); i++) {
            if(this.buildBufferCache.get(i).length >= length) {
                return this.buildBufferCache.remove(i);
            }
        }
        return new byte[length];
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
            for(int i = 0; i < this.sendBufferCache.size(); i++) {
                if(this.sendBufferCache.get(i).length >= length) {
                    return this.sendBufferCache.remove(i);
                }
            }
        }
        return new byte[length];
    }

    public void clear() {
        synchronized(this.sendBufferCache) {
            while(this.sendBufferCache.size() > 3) {
                this.sendBufferCache.remove(this.sendBufferCache.size() - 1);
            }
        }
        synchronized(this.buildBufferCache) {
            while(this.buildBufferCache.size() > 3) {
                this.buildBufferCache.remove(this.buildBufferCache.size() - 1);
            }
        }
    }
    
}
