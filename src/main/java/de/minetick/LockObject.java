package de.minetick;

public class LockObject {
	public final Object updatePlayersLock = new Object();
	public static final Object playerTickLock = new Object();
}
