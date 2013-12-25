package de.minetick.packetbuilder;

public interface PacketBuilderJobInterface {

    public void buildAndSendPacket(PacketBuilderBuffer pbb, Object checkAndSendLock);
    public void clear();
}
