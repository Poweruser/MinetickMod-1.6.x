package de.minetick.profiler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Profiler {

	private Map<String, Profile> map;
	private boolean worldAvgsHaveChanged;
	private int logInterval = 10; // seconds
	private int avgsTickInterval = 5; // seconds
	private int index = 0, avgIndex = 0;
	private int counter = 0;
	private boolean writeToFile;
	private int writeInterval; // minutes
	
	public Profiler(int logInterval, boolean writeToFile, int writeInterval) {
	    this.writeToFile = writeToFile;
	    this.logInterval = logInterval;
	    if(this.logInterval < 1) {
	        this.logInterval = 10;
	    }
	    this.writeInterval = writeInterval;
	    if(this.writeInterval < 1) {
	        this.writeInterval = 1;
	        this.writeToFile = false;
	    }
	    this.worldAvgsHaveChanged = false;
		this.map = Collections.synchronizedMap(new HashMap<String, Profile>());
	}
	
	public Profile getProfile(String ident) {
		return this.map.get(ident);		
	}
	
	public void start(String ident) {
		Profile p = this.map.get(ident);
		if(p == null) {
		    int writeSteps = this.writeInterval * 60 / this.logInterval;
			p = new Profile(this.logInterval, ident, this.avgsTickInterval, this.writeToFile, this.writeInterval, writeSteps);
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
		if(this.avgIndex >= this.avgsTickInterval * 20) {
			this.avgIndex = 0;
			this.worldAvgsHaveChanged = true;
		}
		if(this.index >= this.logInterval * 20) {
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

