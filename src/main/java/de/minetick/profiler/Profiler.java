package de.minetick.profiler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Profiler {

	private Map<String, Profile> map;
	private boolean worldAvgsHaveChanged;
	private int interval = 30;
	private int avgsTickInterval = 5 * 20;
	private int index = 0, avgIndex = 0;
	private int counter = 0;
	
	public Profiler() {
		this.worldAvgsHaveChanged = false;
		this.map = Collections.synchronizedMap(new HashMap<String, Profile>());
	}
	
	public Profile getProfile(String ident) {
		return this.map.get(ident);		
	}
	
	public void start(String ident) {
		Profile p = this.map.get(ident);
		if(p == null) {
			p = new Profile(this.interval, ident, this.avgsTickInterval);
			this.map.put(ident, p);
		}
		p.start();		
	}
	
	public Profile stop(String ident) {
		Profile p = this.map.get(ident);
		if(p != null) {
			p.stop();
			return p;
		}
		return null;
	}
	
	public void newTick() {
		this.index++;
		this.avgIndex++;
		if(this.avgIndex >= this.avgsTickInterval) {
			this.avgIndex = 0;
			this.worldAvgsHaveChanged = true;
		}
		if(this.index >= this.interval * 20) {
			this.counter++;
			this.index = 0;
		}
		for(Profile p: this.map.values()) {
			p.newTick(this.index, this.counter);
		}
	}
	
	public boolean checkAvgs() {
		boolean out = this.worldAvgsHaveChanged;
		this.worldAvgsHaveChanged = false;
		return out;
	}
}

