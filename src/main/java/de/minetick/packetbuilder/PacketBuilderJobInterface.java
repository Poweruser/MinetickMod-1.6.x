package de.minetick.packetbuilder;

public interface PacketBuilderJobInterface {

    public void buildAndSendPacket(PacketBuilderBuffer pbb);
    public void clear();
}
