package de.minetick.packetbuilder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class PacketBuilderBuffer {

    private ArrayList<WeakReference<byte[]>> sendBufferCache = new ArrayList<WeakReference<byte[]>>();

    public PacketBuilderBuffer() {}

    public void offerSendBuffer(byte[] array) {
        synchronized(this.sendBufferCache) {
            this.sendBufferCache.add(new WeakReference<byte[]>(array));
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
    }

    private byte[] checkInList(ArrayList<WeakReference<byte[]>> list, int length) {
        WeakReference<byte[]> ref;
        byte[] array;
        int size = list.size();
        for(int i = size - 1; i >= 0; i--) {
            ref = list.get(i);
            array = ref.get();
            if(array == null) {
                list.remove(i);
            } else if(array.length >= length) {
                list.remove(i);
                return array;
            }
        }
        return new byte[length];
    }
}
