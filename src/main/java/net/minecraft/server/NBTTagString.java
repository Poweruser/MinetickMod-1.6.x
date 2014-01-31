package net.minecraft.server;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagString extends NBTBase {

    public String data;

    public NBTTagString(String s) {
        super(s);
    }

    public NBTTagString(String s, String s1) {
        super(s);
        this.data = s1;
        if (s1 == null) {
            throw new IllegalArgumentException("Empty string not allowed");
        }
    }

    void write(DataOutput dataoutput) throws IOException { // Poweruser - added throws IOException
        dataoutput.writeUTF(this.data);
    }

    void load(DataInput datainput, int i) throws IOException { // Poweruser - added throws IOException
        //this.data = datainput.readUTF();
        this.data = getStoredString(datainput.readUTF(), true); // Poweruser
    }

    public byte getTypeId() {
        return (byte) 8;
    }

    public String toString() {
        return "" + this.data;
    }

    public NBTBase clone() {
        return new NBTTagString(this.getName(), this.data);
    }

    public boolean equals(Object object) {
        if (!super.equals(object)) {
            return false;
        } else {
            NBTTagString nbttagstring = (NBTTagString) object;

            return this.data == null && nbttagstring.data == null || this.data != null && this.data.equals(nbttagstring.data);
        }
    }

    public int hashCode() {
        return super.hashCode() ^ this.data.hashCode();
    }
}
