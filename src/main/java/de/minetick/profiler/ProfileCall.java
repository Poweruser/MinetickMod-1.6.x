package de.minetick.profiler;

public class ProfileCall {

	private long time;
	private int count;
	private int playerNumber;
	private int generatedChunks;

	ProfileCall() {
		this.reset();
	}

	public int getCount() {
		return this.count;
	}

	public long getTime() {
		return this.time;
	}

	public void reset() {
		this.time = 0L;
		this.count = 0;
		this.playerNumber = 0;
		this.generatedChunks = 0;
	}

	public void add(long t) {
		this.time += t;
		this.count++;
	}

	public void setTime(long t) {
		this.time = t;
		this.count++;
	}

	public void setPlayerNumber(int i) {
		this.playerNumber = i;
	}

	public int getPlayerNumber() {
		return this.playerNumber;
	}

	public void setGeneratedChunks(int count) {
		this.generatedChunks += count;
	}

	public int getGeneratedChunks() {
		return this.generatedChunks;
	}
}
