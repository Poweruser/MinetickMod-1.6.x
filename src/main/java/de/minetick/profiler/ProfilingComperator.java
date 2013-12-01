package de.minetick.profiler;

import java.util.Comparator;

import net.minecraft.server.WorldServer;

public class ProfilingComperator implements Comparator<WorldServer> {

	@Override
	public int compare(WorldServer o1, WorldServer o2) {
		long a = o1.getLastTickAvg();
		long b = o2.getLastTickAvg();
		/*
		 * If the last average tick time of a world is greater, it shall be closer
		 * to the head of the priority queue 
		 */
		if(a > b) {
			return -1;
		}
		if(a < b) {
			return 1;
		}
		return 0;
	}

}
