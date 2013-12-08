package de.minetick.profiler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Profile {

	private ProfileCall[] records;
	private int lastIndex;
	private int index;
	private File folder;
	private File file;
	private BufferedWriter writer;
	private String ident;
	private SimpleDateFormat currentTime;
	private long startTime;
	private long endTime;
	private ArrayList<String> output = new ArrayList<String>();

	private long avgSum = 0L;
	private int avgCount = 0, avgMaxCount;

	private long avgTickEntitiesTime;

	private boolean errorShown = false;

	private int counter;
	private boolean writeEnabled;
	private int writeInterval;
	private int writeStep;
	private int logInterval;

	public Profile(int size, String ident, int avgMaxCount, boolean writeToFile, int writeInterval, int writeSteps) {
	    this.avgTickEntitiesTime = 0L;
	    this.ident = ident;
	    this.avgMaxCount = avgMaxCount;
	    this.writeEnabled = writeToFile;
	    this.writeInterval = writeInterval;
	    this.writeStep = writeSteps;
	    if(writeToFile) {
	        this.currentTime = new SimpleDateFormat ("HH:mm:ss");
	        SimpleDateFormat df = new SimpleDateFormat ("yyyy.MM.dd_HH-mm-ss");
	        Calendar c = Calendar.getInstance();
	        int day = c.get(Calendar.DAY_OF_MONTH);
	        String d = String.valueOf(day);
	        if(d.length() < 2) {
	            d = "0" + d;
	        }
	        int month = c.get(Calendar.MONTH) + 1;
	        String m = String.valueOf(month);
	        if(m.length() < 2) {
	            m = "0" + m;
	        }
	        int year = c.get(Calendar.YEAR);
	        String path = "Profiler\\" + year + "." + m + "\\" + d;
	        this.folder = new File(path);
	        this.file = new File(path + "\\MTProfiler_" + df.format(new Date()) + "_" + ident + ".txt");
	    }
	    this.lastIndex = 0;
	    this.index = 0;
	    this.counter = 0;
	    this.records = new ProfileCall[size*20];
	    this.startTime = 0L;
	    this.endTime = 0L;
	}
	
	public String getIdent() {
		return this.ident;
	}

	public long getLastAvg() {
		return this.avgTickEntitiesTime;
	}
	
	public long getRecord(int i) {
		if(i >= 0 && i <= this.index) {
			return this.records[index].getTime();
		} else {
			ProfileCall pc = this.records[this.index];
			if(pc != null) {
				return pc.getTime();
			}
		}
		return 0L;
	}
	
	public void setCustomRecord(int index, long time) {
		if(this.index < this.records.length) {
			if(this.records[index] == null) {
				this.records[index] = new ProfileCall();
			}
			this.records[index].setTime(time);
		}
	}
	
	public void setCurrentPlayerNumber(int count) {
		if(this.index < this.records.length) {
			this.records[this.index].setPlayerNumber(count);
		}
	}

	public void setGeneratedChunks(int count) {
		if(this.index < this.records.length) {
			this.records[this.index].setGeneratedChunks(count);
		}
	}

	public void start() {
		this.startTime = System.nanoTime();		
	}
	
	public long stop() {
		this.endTime = System.nanoTime();
		long measuredTime = this.endTime - this.startTime;
		this.avgCount++;
		this.avgSum += measuredTime;
		if(this.avgCount >= this.avgMaxCount) {
			this.avgTickEntitiesTime = (this.avgSum / this.avgCount);
			this.avgSum = 0L;
			this.avgCount = 0;
		}
		if(this.index < this.records.length) {
			if(this.records[this.index] == null) {
				this.records[this.index] = new ProfileCall();
			}
			this.records[this.index].add(measuredTime);
			return this.records[this.index].getTime();
		}
		return 0L;
	}
	
	public void newTick(int index, int cnt) {
		this.lastIndex = this.index;
		this.index = index;
		if(cnt > this.counter) {
			this.counter = cnt;

			this.calcRecord();
			if(this.writeEnabled) {
			    if((this.counter % this.writeStep) == 0) {
			        this.writeToFile();
			    }
			}
		}
	}
	
	private String currentTime() {
		return this.currentTime.format(new Date());
	}
	
	private void calcRecord() {
		long avg = 0L, max = Long.MIN_VALUE, calls = 0L;
		int i = 0, playerAvg = 0, chunks = 0;
		for(i = 0; i < this.records.length && i <= this.lastIndex; i++) {
			if(this.records[i] != null) {
				avg += this.records[i].getTime();
				if(this.writeEnabled) {
				    playerAvg += this.records[i].getPlayerNumber();
				    chunks += this.records[i].getGeneratedChunks();
				    calls += this.records[i].getCount();
				    if(this.records[i].getTime() > max) {
				        max = this.records[i].getTime();
				    }
				}
				this.records[i].reset();
			}
		}
		if(i > 0) {
			avg = avg / i;
			playerAvg = playerAvg / i;
			calls = calls / i;
		} else {
			avg = 0L;
			calls = 0L;
		}
		this.avgTickEntitiesTime = avg;
		if(this.writeEnabled) {
		    float favg = ((float)(avg / 1000L)) / 1000.0F;
		    float fmax = ((float)(max / 1000L)) / 1000.0F;
		    String tmp = (" Avg: " + favg + "  Max: "+ fmax + " AvgCalls: " + calls + " Players: " + playerAvg);
		    if(chunks > 0) {
		        tmp += (" GeneratedChunks: " + chunks);
		    }
		    this.output.add("" + this.counter + "  " + this.currentTime() + tmp.replace(".", ","));
		}
	}
	
	private void writeToFile() {
		boolean exists = this.file.exists();
		if(!exists) {
			try {
				this.folder.mkdirs();
				exists = this.file.createNewFile();
			} catch (IOException e) {
				if(!errorShown) {
					errorShown = true;
					Logger a = Logger.getLogger("Minecraft");
					a.log(Level.WARNING, "MinetickMod-Profiler: Could not create the file: " + this.file.getAbsolutePath() + " Reason: "+ e.getMessage());
				}
			}
		}
		int code = 0;
		boolean failed = false;
		if(exists && this.file.canWrite()) {
				try {
					if(this.writer == null) {
						this.writer = new BufferedWriter(new FileWriter(this.file, true), 8 * 1024);
					}
					for(String s: this.output) {
						this.writer.write(s);
						this.writer.newLine();					
					}						
					this.writer.flush();
				} catch (IOException e) {
					failed = true;
					code = 1;
				}
				if(this.writer != null) {
					try {
						this.writer.close();
					} catch (IOException e) {}
				}
				this.writer = null;
		} else {
			failed = true;
			code = 2;
		}
		if(failed && !errorShown) {
			errorShown = true;
			Logger a = Logger.getLogger("Minecraft");
			a.log(Level.WARNING, "MinetickMod-Profiler: Could not write the profiler records of " + ident + " to disk! Code: " + code + "! " + this.file.getAbsolutePath());
		}
		this.output.clear();
	}
}
