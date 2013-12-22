package de.minetick;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.server.Packet;
import net.minecraft.server.Packet51MapChunk;

public class PacketFilter {

    private ConcurrentLinkedDeque<Packet> highPriorityFilterLists[];
    private ConcurrentLinkedDeque<Packet> lowPriorityFilterLists[];
    private ConcurrentLinkedDeque<Packet> lowPriority;
    private ConcurrentLinkedDeque<Packet> highPriority;
    private AtomicInteger dataLength;
        
    public PacketFilter(AtomicInteger dataLength) {
        this.dataLength = dataLength;
        this.highPriorityFilterLists = new ConcurrentLinkedDeque[256];
        this.lowPriorityFilterLists = new ConcurrentLinkedDeque[256];
        for(int i = 0; i < 256; i++) {
            this.highPriorityFilterLists[i] = new ConcurrentLinkedDeque<Packet>();
            this.lowPriorityFilterLists[i]  = new ConcurrentLinkedDeque<Packet>();
        }
        this.lowPriority = new ConcurrentLinkedDeque<Packet>();
        this.highPriority = new ConcurrentLinkedDeque<Packet>();
    }

    public Packet getNextHighPriorityPacket() {
        return this.getNextPacket(this.highPriority, this.highPriorityFilterLists);
    }
    
    public Packet getNextLowPriorityPacket() {
        return this.getNextPacket(this.lowPriority, this.lowPriorityFilterLists);
    }
    
    private Packet getNextPacket(Deque<Packet> deque, ConcurrentLinkedDeque<Packet>[] lists) {
        Packet out = null;
        if(!deque.isEmpty()) {
            do {
                out = deque.pollFirst();
                this.dataLength.addAndGet((out.a() + 1) * -1);
                if(out != null) {
                    Packet first = lists[out.n()].peekFirst();
                    if(first != null) {
                        if(out == first) {
                            lists[out.n()].pollFirst();
                        } else {
                            out = null;
                        }
                    }
                }
            } while(!deque.isEmpty() && out == null);
        }
        return out;        
    }
    
    public void addHighPriorityPacket(Packet packet) {
        this.addPacket(packet, this.highPriorityFilterLists, this.highPriority);
    }
    
    public void addLowPriorityPacket(Packet packet) {
        this.addPacket(packet, this.lowPriorityFilterLists, this.lowPriority);
    }
    
    private void addPacket(Packet packet, Deque<Packet>[] array, Deque<Packet> list) {
        Deque<Packet> deque = array[packet.n()];
        Iterator<Packet> iter = deque.iterator();
        if(iter.hasNext()) {
            iter.next();
        }
        while(iter.hasNext()) {
            Packet p = iter.next();
            if(packet.a(p)) {
                iter.remove();
                //System.out.println("[PacketFilter] Rejected: " + p.getClass().getSimpleName());
            }
        }
        synchronized(deque) {
            deque.add(packet);
            list.add(packet);
        }        
    }
    
    public boolean hasHighPriorityPackets() {
        return !this.highPriority.isEmpty();
    }
    
    public boolean hasLowPriorityPackets() {
        return !this.lowPriority.isEmpty();
    }
}
