package de.minetick;

import java.util.Observable;

import net.minecraft.server.WorldServer;

import de.minetick.profiler.Profile;
import de.minetick.profiler.Profiler;

public class WorldTicker extends Observable implements Runnable {
	private boolean active;
	private WorldServer worldToTick;
	private String worldName = "None yet";
	private Object waitObj;
	private LockObject lock;

	private Profiler profiler;

	public WorldTicker(Profiler prof, LockObject lock) {
		this.profiler = prof;
		this.active = true;
		this.worldToTick = null;
		this.waitObj = new Object();
		this.lock = lock;
	}

	@Override
	public void run() {
		while(this.active) {
			if(this.worldToTick == null) {
				synchronized(this.waitObj) {
					try {
						this.waitObj.wait();
					} catch (InterruptedException e) {}
				}
			} else {
				this.profiler.start(this.worldName + "_thread");
				try {
                	this.worldToTick.tickEntities();
                } catch (Throwable throwable1) {
                	System.out.println(throwable1.getMessage());
					throwable1.printStackTrace();
                }
				synchronized(this.lock.updatePlayersLock) {
					this.worldToTick.getTracker().updatePlayers();
				}

	            Profile profile = this.profiler.stop(this.worldName + "_thread");
		        if(profile != null) {
		            this.worldToTick.setLastTickAvg(profile.getLastAvg());
		            profile.setCurrentPlayerNumber(this.worldToTick.players.size());
		        }

		        this.worldToTick = null;
                this.setChanged();
                this.notifyObservers();
			}
		}	
	}

	public void startWorld(WorldServer ws) {
		this.worldToTick = ws;
		this.worldName = this.worldToTick.getWorld().getName();
		synchronized(this.waitObj) {
			this.waitObj.notifyAll();
		}
	}
	
	public void shutdown() {
		this.active = false;
		synchronized(this.waitObj) {
			this.waitObj.notifyAll();
		}
	}

	public String getLastTickedWorld() {
		return this.worldName;
	}

	public boolean isBusy() {
		return (this.worldToTick != null);
	}
}
